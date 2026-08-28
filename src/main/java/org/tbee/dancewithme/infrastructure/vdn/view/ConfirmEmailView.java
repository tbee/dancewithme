package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.EmailConfirmationService;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.util.List;
import java.util.Map;

/**
 * The confirmation page shown after registration, offering to click the link in the email
 * or to enter the confirmation code manually.
 * When opened with a {@code code} query parameter (the email link), the code is prefilled.
 * When opened with a {@code resent} query parameter, the registration was for an email address that was already
 * registered but not confirmed, so the confirmation was resent for that existing profile.
 * When opened with an {@code unconfirmed} query parameter, a login attempt was refused because the email address
 * is not confirmed yet.
 * When the email never arrived or the code expired, {@link ResendConfirmationView} is one link away.
 */
@Route("confirm")
@AnonymousAllowed
public class ConfirmEmailView extends DancewithmeAppLayout implements BeforeEnterObserver {

    private final EmailConfirmationService emailConfirmationService;
    private final VerticalLayout verticalLayout = new VerticalLayout();

    private final TextField codeField = new TextField();

    public ConfirmEmailView(SecurityService securityService, LocaleService localeService, EmailConfirmationService emailConfirmationService) {
        super("reset.button", securityService, localeService);
        this.emailConfirmationService = emailConfirmationService;

        verticalLayout.setSizeFull();
        verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        verticalLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        setContent(verticalLayout);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        verticalLayout.removeAll();
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();

        // arrived here because logging in was refused: the email address is not confirmed yet
        if (first(params, "unconfirmed") != null) {
            verticalLayout.add(new Span(getTranslation("confirm.loginRefused")));
        }
        // registering an already registered but unconfirmed email address only resends the confirmation
        if (first(params, "resent") != null) {
            verticalLayout.add(new Span(getTranslation("confirm.alreadyRegistered")));
        }

        // manual code entry, prefilled when the confirmation link in the email was clicked
        codeField.setLabel(getTranslation("confirm.code"));
        codeField.setWidth("200px");
        String code = first(params, "code");
        if (code != null) {
            codeField.setValue(code);
        }

        Button confirmButton = new Button(getTranslation("confirm.button"), e -> confirm());
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        verticalLayout.add(new Span(getTranslation("confirm.instruction")), codeField, confirmButton,
                new RouterLink(getTranslation("resend.link"), ResendConfirmationView.class));
    }

    private void confirm() {
        if (emailConfirmationService.confirm(codeField.getValue())) {
            verticalLayout.removeAll();
            verticalLayout.add(new Span(getTranslation("confirm.success")));
            verticalLayout.add(new RouterLink(getTranslation("menu.login"), LoginView.class));
            return;
        }
        // keep the form, the code may simply be mistyped; the resend link is right there when it is expired
        DancewithmeAppLayout.showErrorNotification(getTranslation("confirm.failure"));
    }

    private static String first(Map<String, List<String>> params, String key) {
        List<String> values = params.getOrDefault(key, List.of());
        return values.isEmpty() ? null : values.getFirst();
    }
}
