package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;
import org.tbee.webstack.vdn.component.html.H1;

@Route("login")
@AnonymousAllowed
public class LoginView extends DancewithmeAppLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView(SecurityService securityService, LocaleService localeService) {
        super("login.title", securityService, localeService);
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        loginForm.setAction("login");
        loginForm.setI18n(i18n());
        loginForm.addForgotPasswordListener(e -> UI.getCurrent().navigate(ForgotPasswordView.class));
        verticalLayout.add(new H1("Dance With Me"), loginForm);

        setContent(verticalLayout);
    }

    /**
     * The login form is a prefabricated component, so its labels are set through its i18n object;
     * dancers log in with their email address, not with some separate user name.
     */
    private LoginI18n i18n() {
        LoginI18n i18n = LoginI18n.createDefault();

        LoginI18n.Form form = i18n.getForm();
        form.setTitle(getTranslation("login.title"));
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
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
