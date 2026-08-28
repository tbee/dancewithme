package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.PasswordResetService;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The page reached from the login form's "forgot password" link, offering to send a reset email.
 */
@Route("forgot-password")
@AnonymousAllowed
public class ForgotPasswordView extends DancewithmeAppLayout {

    private final PasswordResetService passwordResetService;
    private final VerticalLayout verticalLayout = new VerticalLayout();

    public ForgotPasswordView(SecurityService securityService, LocaleService localeService, PasswordResetService passwordResetService) {
        super("login.forgotPassword", securityService, localeService);
        this.passwordResetService = passwordResetService;

        verticalLayout.setSizeFull();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        EmailField emailField = new EmailField(getTranslation("form.email"));
        emailField.setWidth("300px");

        Button sendButton = new Button(getTranslation("forgot.button"), e -> send(emailField.getValue()));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        verticalLayout.add(new Span(getTranslation("forgot.instruction")), emailField, sendButton);
        setContent(verticalLayout);
    }

    private void send(String email) {
        String token = passwordResetService.requestReset(email);
        if (passwordResetService.isDevelopment() && token != null) {
            UI.getCurrent().navigate("reset-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&token=" + token);
            return;
        }
        verticalLayout.removeAll();
        verticalLayout.add(new Span(getTranslation("forgot.sent")));
        verticalLayout.add(new RouterLink(getTranslation("menu.login"), LoginView.class));
    }
}
