package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.PasswordResetService;

import java.util.List;
import java.util.Map;

/**
 * The page reached by clicking the reset link in the email, offering to set a new password.
 */
@Route("reset-password")
@PageTitle("Dancewithme")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements BeforeEnterObserver {

    private final PasswordResetService passwordResetService;

    public ResetPasswordView(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();
        String email = first(params, "email");
        String token = first(params, "token");
        if (email == null || token == null) {
            showResult(false);
            return;
        }

        PasswordField passwordField = new PasswordField(getTranslation("form.password"));
        passwordField.setWidth("300px");
        PasswordField confirmPasswordField = new PasswordField(getTranslation("form.password.confirm"));
        confirmPasswordField.setWidth("300px");

        Button resetButton = new Button(getTranslation("reset.button"), e -> {
            if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
                confirmPasswordField.setInvalid(true);
                confirmPasswordField.setErrorMessage(getTranslation("form.password.mismatch"));
                return;
            }
            showResult(passwordResetService.resetPassword(email, token, passwordField.getValue()));
        });
        resetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new Span(getTranslation("reset.instruction", email)), passwordField, confirmPasswordField, resetButton);
    }

    private void showResult(boolean reset) {
        removeAll();
        add(new Span(getTranslation(reset ? "reset.success" : "reset.failure")));
        add(new RouterLink(getTranslation("menu.login"), LoginView.class));
    }

    private static String first(Map<String, List<String>> params, String key) {
        List<String> values = params.getOrDefault(key, List.of());
        return values.isEmpty() ? null : values.getFirst();
    }
}
