package org.tbee.dancewithme.infrastructure.vdn;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Applies the locale chosen by the user (stored in a cookie) to every UI,
 * and provides the language switcher logic.
 */
@Component
public class LocaleService implements VaadinServiceInitListener {

    public static final String COOKIE_NAME = "dancewithme-locale";
    public static final List<Locale> SUPPORTED_LOCALES = List.of(Locale.ENGLISH, Locale.of("nl"));

    @Override
    public void serviceInit(com.vaadin.flow.server.ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> uiEvent.getUI().setLocale(determineLocale()));
    }

    public Locale determineLocale() {
        Locale cookieLocale = readCookieLocale();
        return cookieLocale != null ? cookieLocale : Locale.getDefault();
    }

    private Locale readCookieLocale() {
        VaadinService service = VaadinService.getCurrent();
        if (service == null) {
            return null;
        }
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(cookie -> Locale.forLanguageTag(cookie.getValue()))
                .filter(SUPPORTED_LOCALES::contains)
                .findFirst()
                .orElse(null);
    }

    public void switchLocale(Locale locale) {
        Cookie cookie = new Cookie(COOKIE_NAME, locale.toLanguageTag());
        cookie.setPath("/");
        cookie.setMaxAge(365 * 24 * 3600);
        VaadinService.getCurrentResponse().addCookie(cookie);
        UI.getCurrent().setLocale(locale);
        UI.getCurrent().getPage().reload();
    }
}
