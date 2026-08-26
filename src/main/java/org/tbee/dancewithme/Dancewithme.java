package org.tbee.dancewithme;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.aura.Aura;
import com.vaadin.flow.theme.lumo.Lumo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.tbee.dancewithme.infrastructure.jpa.CustomRepositoryImpl;

import java.util.Locale;

// TODO:
// Attempt login when email is not confirmed does not report the correct error (goto confirm email page?)
// Privacy / cookie policy text
// About popup
// Integration / web rests
// Forgot password logic (login page)
// Chat

@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = CustomRepositoryImpl.class, enableDefaultTransactions = false)
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
