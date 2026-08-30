package org.tbee.dancewithme.infrastructure.vdn.web;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.tbee.dancewithme.giwth.StepContext;

import java.util.HashMap;
import java.util.Map;

abstract public class WebTestBase extends InfraTestBase {
    private static final Logger LOG = LoggerFactory.getLogger(WebTestBase.class);

    static protected Playwright playwright;
    static protected Browser browser;
    protected Page page;

    @Autowired
    public AutowireCapableBeanFactory beanFactory;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @BeforeAll
    static public void beforeAll() {
        InfraTestBase.beforeAll();
        initBrowser();
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        super.beforeEach(testInfo);
        openBrowser();
    }

    @AfterEach
    public void afterEach() {
        super.afterEach();
        closeBrowser();
    }

    @AfterAll
    static public void afterAll() {
        InfraTestBase.afterAll();
        destructBrowser();
    }

    static protected void initBrowser() {
        Map<String, String> env = new HashMap<>();
        //env.put("PWDEBUG", "1");
        playwright = Playwright.create(new Playwright.CreateOptions().setEnv(env));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false));
    }

    protected void openBrowser() {
        BrowserContext context = browser.newContext();
        page = context.newPage();
        // to be able to debug a page:
        // page.setDefaultTimeout(5 * 60 * 1000); // 5 minutes
    }

    protected void closeBrowser() {
        if (page != null) {
            page.close();
        }
    }

    static protected void destructBrowser() {
        if (playwright != null) {
            playwright.close();
        }
    }

    @Override
    protected StepContext context(int port) {
        StepContext stepContext = new StepContext(port, page, beanFactory, transactionManager);
        beanFactory.autowireBean(stepContext);
        return stepContext;
    }
}
