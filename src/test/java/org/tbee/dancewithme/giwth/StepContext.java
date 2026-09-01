package org.tbee.dancewithme.giwth;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.repository.DancerRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public class StepContext {
    final public com.microsoft.playwright.Page page;
    final public String baseUrl;

    public AutowireCapableBeanFactory beanFactory;
    private PlatformTransactionManager transactionManager;
//    public DataSource dataSource;
    public Supplier<LocalDate> todaySupplier = LocalDate::now;
    public Supplier<LocalDateTime> nowSupplier = LocalDateTime::now;

    public StepContext(int port, com.microsoft.playwright.Page page, AutowireCapableBeanFactory beanFactory, PlatformTransactionManager transactionManager) {
        this.baseUrl = "http://localhost:"  + port + "/";
        this.page = page;
        this.beanFactory = beanFactory;
        this.transactionManager = transactionManager;
    }

    public void english() {
//        beanFactory.getBean(LocaleService.class).switchLocale(Locale.ENGLISH);
    }

    public DancerRepository dancerRepository() {
        return beanFactory.getBean(DancerRepository.class);
    }
    public DancestyleRepository dancestyleRepository() {
        return beanFactory.getBean(DancestyleRepository.class);
    }

    public Dancestyle ballroom() {
        return dancestyleRepository().findBallroom();
    }
    public Dancestyle latin() {
        return dancestyleRepository().findLatin();
    }

    public void inTransaction(Runnable runnable) {
        new TransactionTemplate(transactionManager).execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                runnable.run();
            }
        });
    }

    public <T> T inTransaction(java.util.function.Supplier<T> supplier) {
        return new TransactionTemplate(transactionManager).execute(status -> supplier.get());
    }

    public void waitForMainLayoutToBecomeVisible() {
        page.waitForSelector("#drawerToggle");
        if (page.url().contains("/vdn/")) {
            PlaywrightAssertions.assertThat(page.locator("vaadin-connection-indicator")).not().hasAttribute("loading", "", new LocatorAssertions.HasAttributeOptions().setTimeout(30*1000));
        }
    }

    public void waitForBusyIndicator() {
        Locator locator = page.locator("css=div.v-loading-indicator");
        long startTime = System.currentTimeMillis();
        while (!locator.getAttribute("style").contains("display: none")) {
            sleep(100);
            if (System.currentTimeMillis() > startTime + 5000) {
                throw new RuntimeException("Timeout waiting for busy indicator");
            }
        }
    }

    public void waitForNotification(String text) {
        page.locator("vaadin-notification-card")
                .getByText(text)
                .waitFor();
    }

    public void waitForUrlToContain(String substring) {
        page.waitForURL(url -> url.contains(substring));
    }

    public void sleep(int ms) {
        try {
            Thread.sleep(ms);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
