package org.tbee.dancewithme.infrastructure.vdn.web;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.tbee.dancewithme.giwth.StepContext;
import org.tbee.dancewithme.infrastructure.mail.MailSenderStub;
import org.tbee.webstack.tenant.TenantContext;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

@TestPropertySource(locations="classpath:testPostgres.properties")
@org.springframework.context.annotation.Import(InfraTestConfig.class)
abstract public class InfraTestBase {

    // Singleton container pattern: start ONCE per JVM and never stop it (Ryuk cleans up at JVM exit).
    // Using @Testcontainers/@Container would stop the container after each test class's @AfterAll, which
    // breaks subsequent test classes when Maven runs them all in a single surefire JVM (reuseForks=true).
    static final PostgreSQLContainer<?> postgres;
    static {
        // docker-java defaults to Docker API 1.32, which Rancher Desktop / moby rejects (min supported 1.41).
        // Must be set before the Docker client is initialized (i.e. before the container starts).
        System.setProperty("api.version", "1.41");

        postgres = new PostgreSQLContainer<>("postgres:18.3")
                .withDatabaseName("dancewithme")
                .withUsername("dancewithme")
                .withPassword("dancewithme");
        postgres.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    public AutowireCapableBeanFactory beanFactory;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private DataSource dataSource;

    private TestInfo testInfo;

    @BeforeAll
    static public void beforeAll() {
        InfraTestBase.prepareRunningLiquibase();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        this.testInfo = testInfo;
        printTestHeader(testInfo);
        // R is a static singleton bound to the last created Spring context; rebind it to this test's context
//        beanFactory.getBean(nl.softworks.consilio.domain.repository.R.class).init();
        MailSenderStub.sentMimeMessages.clear();
        MailSenderStub.sentSimpleMessages.clear();
        deleteAllData();
    }

    @AfterEach
    public void afterEach() {
        printTestFooter(testInfo);
        TenantContext.clearTenant();
    }

    @AfterAll
    static public void afterAll() {
    }

    static public void prepareRunningLiquibase() {
        System.setProperty("liquibase.secureParsing", "false");
    }

    protected void deleteAllData() {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("SET session_replication_role = 'replica'");
                List<String> tableNames = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery( """
                            SELECT table_name 
                              FROM information_schema.tables 
                             WHERE table_schema = 'public' 
                               AND table_type = 'BASE TABLE'
                           """)) {
                    while (rs.next()) {
                        String name = rs.getString(1);
                        if (!"DATABASECHANGELOG".equalsIgnoreCase(name)
                                && !"DATABASECHANGELOGLOCK".equalsIgnoreCase(name)
                                && !"dancestyle".equalsIgnoreCase(name)
                                && !"country".equalsIgnoreCase(name)
                                && !"city".equalsIgnoreCase(name)) {
                            tableNames.add(name);
                        }
                    }
                }
                if (!tableNames.isEmpty()) {
                    stmt.execute("TRUNCATE TABLE " + tableNames.stream().map(n -> "\"" + n + "\"").collect(joining(", ")) + " CASCADE");
                }
                stmt.execute("SET session_replication_role = 'origin'");
            }
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static public void printTestHeader(TestInfo testInfo) {
//        System.out.println(testInfo.getTestClass().orElseThrow() + "." + testInfo.getDisplayName() + "\n");
    }
    static public void printTestFooter(TestInfo testInfo) {
//        System.out.println("#\n" + testInfo.getTestClass().orElseThrow() + "." + testInfo.getDisplayName() + "\n=====================================================");
    }

    protected LocalTime t(String time) {
        return LocalTime.parse(time);
    }

    protected StepContext context(int port) {
        StepContext stepContext = new StepContext(port, null, beanFactory, transactionManager);
        beanFactory.autowireBean(stepContext);
        return stepContext;
    }

    static public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    static public void sleepForALongTime() {
        sleep(Integer.MAX_VALUE);
    }
}
