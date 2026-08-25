package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.PasswordResetService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The page reached from the login form's "forgot password" link, offering to send a reset email.
 */
@Route("forgot-password")
@PageTitle("Dancewithme")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout {

    private final PasswordResetService passwordResetService;

    public ForgotPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        EmailField emailField = new EmailField(getTranslation("form.email"));
        emailField.setWidth("300px");

        Button sendButton = new Button(getTranslation("forgot.button"), e -> send(emailField.getValue()));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new Span(getTranslation("forgot.instruction")), emailField, sendButton);
    }

    private void send(String email) {
        String token = passwordResetService.requestReset(email);
        if (passwordResetService.isDevelopment() && token != null) {
            UI.getCurrent().navigate("reset-password?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&token=" + token);
            return;
        }
        removeAll();
        add(new Span(getTranslation("forgot.sent")));
        add(new RouterLink(getTranslation("menu.login"), LoginView.class));
    }
}
