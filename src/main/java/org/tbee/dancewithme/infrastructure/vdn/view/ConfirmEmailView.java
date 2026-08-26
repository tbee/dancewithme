package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.EmailConfirmationService;

import java.util.List;
import java.util.Map;

/**
 * The confirmation page shown after registration, offering to click the link in the email
 * or to enter the confirmation code manually.
 * When opened with a {@code code} query parameter (the email link), it confirms directly.
 * When opened with a {@code prefill} query parameter (development), the code is prefilled in the field.
 */
@Route("confirm")
@PageTitle("Dancewithme")
@AnonymousAllowed
public class ConfirmEmailView extends VerticalLayout implements BeforeEnterObserver {

    private final EmailConfirmationService emailConfirmationService;

    public ConfirmEmailView(EmailConfirmationService emailConfirmationService) {
        this.emailConfirmationService = emailConfirmationService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Map<String, List<String>> params = event.getLocation().getQueryParameters().getParameters();

        // manual code entry
        TextField codeField = new TextField(getTranslation("confirm.code"));
        codeField.setWidth("200px");

        // clicked the confirmation link in the email
        String code = first(params, "code");
        if (code != null) {
            codeField.setValue(code);
        }

        Button confirmButton = new Button(getTranslation("confirm.button"),e -> showResult(emailConfirmationService.confirm(codeField.getValue())));
        confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new Span(getTranslation("confirm.instruction")), codeField, confirmButton);
    }

    private void showResult(boolean confirmed) {
        removeAll();
        add(new Span(getTranslation(confirmed ? "confirm.success" : "confirm.failure")));
        add(new RouterLink(getTranslation("menu.login"), LoginView.class));
    }

    private static String first(Map<String, List<String>> params, String key) {
        List<String> values = params.getOrDefault(key, List.of());
        return values.isEmpty() ? null : values.getFirst();
    }
}
