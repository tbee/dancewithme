package org.tbee.dancewithme;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.lumo.Lumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.tbee.dancewithme.infrastructure.jpa.CustomRepositoryImpl;

import java.util.Locale;

// TODO:
// Zoom popup when clicking on a photo
// Add photos in a flow layout
// Allow to change the emailaddress
// i18n SmtpEmailService
// Privacy / cookie policy text
// Integration / web tests
// Chat, two or more profiles should be able to exchange text messages. Visually it should look like the Whatsapp styling with text bubbles on two sides of the timeline.
// A profile should be able to block other profiles into sending them messages or initiating new chats.
// When a profile is deleted, his chat messages no longer refer to the sending profile (and thus be labeled as send by "unknown"). But underwater the email address of the sender should be copied into each message when it was send.

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = CustomRepositoryImpl.class, enableDefaultTransactions = false)
@EnableScheduling
@StyleSheet(Lumo.STYLESHEET)
public class Dancewithme implements AppShellConfigurator {
    private static final Logger LOG = LoggerFactory.getLogger(Dancewithme.class);

    public static void main(String[] args) {
        Locale.setDefault(Locale.forLanguageTag("NL"));
        System.setProperty("liquibase.secureParsing", "false");
        SpringApplication.run(Dancewithme.class, args);
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver(); // Resolves the locale and stores it in a cookie stored on the user's machine. https://lokalise.com/blog/spring-boot-internationalization/
        localeResolver.setDefaultLocale(Locale.getDefault());
        return localeResolver;
    }
}
