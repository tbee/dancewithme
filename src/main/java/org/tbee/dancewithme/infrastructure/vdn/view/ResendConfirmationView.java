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
import org.tbee.dancewithme.application.EmailConfirmationService;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.RememberedEmail;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

/**
 * The page reached from the confirmation page, for when the confirmation email never arrived or its code expired:
 * it sends a new confirmation email, which also restarts the expiry.
 * The email address is prefilled with the one used to register or to attempt a login, when known.
 */
@Route("resend-confirmation")
@AnonymousAllowed
public class ResendConfirmationView extends DancewithmeAppLayout {

    private final EmailConfirmationService emailConfirmationService;
    private final VerticalLayout verticalLayout = new VerticalLayout();

    public ResendConfirmationView(SecurityService securityService, LocaleService localeService, EmailConfirmationService emailConfirmationService) {
        super("resend.button", securityService, localeService);
        this.emailConfirmationService = emailConfirmationService;

        verticalLayout.setSizeFull();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        EmailField emailField = new EmailField(getTranslation("form.email"));
        emailField.setWidth("300px");
        emailField.setValue(RememberedEmail.recall());

        Button sendButton = new Button(getTranslation("resend.button"), e -> send(emailField.getValue()));
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        verticalLayout.add(new Span(getTranslation("resend.instruction")), emailField, sendButton);
        setContent(verticalLayout);
    }

    private void send(String email) {
        RememberedEmail.remember(email);
        String code = emailConfirmationService.resendConfirmation(email);

        // in development no email is sent, so go straight to the confirmation page with the new code prefilled,
        // to be able to walk through the whole flow
        if (code != null && emailConfirmationService.isDevelopment()) {
            UI.getCurrent().navigate("confirm?code=" + code);
            return;
        }

        // always report the same, so this cannot be used to find out which email addresses are registered
        verticalLayout.removeAll();
        verticalLayout.add(new Span(getTranslation("resend.sent")));
        verticalLayout.add(new RouterLink(getTranslation("confirm.link"), ConfirmEmailView.class));
    }
}
