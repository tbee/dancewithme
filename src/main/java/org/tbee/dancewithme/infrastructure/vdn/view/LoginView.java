package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.QueryParameters;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.WrappedSession;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.web.WebAttributes;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.RememberedEmail;
import org.tbee.dancewithme.infrastructure.vdn.security.EmailNotConfirmedException;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;
import org.tbee.webstack.vdn.component.html.H1;
import org.tbee.webstack.vdn.component.html.Image;

@Route("login")
@AnonymousAllowed
public class LoginView extends DancewithmeAppLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView(SecurityService securityService, LocaleService localeService) {
        super("login.title", securityService, localeService);

        loginForm.setAction("login");
        loginForm.setI18n(i18n());
        loginForm.addForgotPasswordListener(e -> UI.getCurrent().navigate(ForgotPasswordView.class));

        Image logoImage = new Image();
        logoImage.src("images/logoTransparent2048x2048.png");
        logoImage.style("height", "300px")
                .style("padding-top", "30px");

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        verticalLayout.add(new H1(getTranslation("app.title")), new HorizontalLayout(logoImage, loginForm));
        setContent(verticalLayout);
    }

    /**
     * The login form is a prefabricated component, so its labels are set through its i18n object;
     * dancers log in with their email address, not with some separate username.
     */
    private LoginI18n i18n() {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form form = i18n.getForm();
        form.setTitle("");//getTranslation("login.title"));
        form.setUsername(getTranslation("form.email"));
        form.setPassword(getTranslation("form.password"));
        form.setSubmit(getTranslation("menu.login"));
        form.setForgotPassword(getTranslation("login.forgotPassword"));

        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle(getTranslation("login.error.title"));
        errorMessage.setMessage(getTranslation("login.error.message"));

        return i18n;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            return;
        }

        // An unconfirmed email address is not a credentials problem, and showing it as such would be a dead end:
        // send the dancer to the confirmation page, where the code can be entered or the email resent
        if (lastAuthenticationFailure() instanceof EmailNotConfirmedException emailNotConfirmed) {
            RememberedEmail.remember(emailNotConfirmed.email());
            event.forwardTo("confirm", QueryParameters.of("unconfirmed", "true"));
            return;
        }

        loginForm.setError(true);
    }

    /**
     * Spring Security parks the failure in the session before redirecting to {@code login?error};
     * it is consumed here, so a later visit to the login page does not act on a stale failure.
     */
    private Object lastAuthenticationFailure() {
        WrappedSession session = VaadinService.getCurrentRequest() == null ? null
                : VaadinService.getCurrentRequest().getWrappedSession(false);
        if (session == null) {
            return null;
        }
        Object failure = session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
        return failure;
    }
}
