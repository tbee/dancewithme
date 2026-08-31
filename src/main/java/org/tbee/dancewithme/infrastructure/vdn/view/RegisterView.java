package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.DancerService;
import org.tbee.dancewithme.application.EmailConfirmationService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.RememberedEmail;
import org.tbee.dancewithme.infrastructure.vdn.component.DancerForm;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.util.ArrayList;
import java.util.List;

@Route("register")
@AnonymousAllowed
public class RegisterView extends DancewithmeAppLayout {

    private final EmailConfirmationService emailConfirmationService;

    public RegisterView(SecurityService securityService, LocaleService localeService, DancerService dancerService,
                        EmailConfirmationService emailConfirmationService,
                        CityRepository cityRepository, DancestyleRepository dancestyleRepository, SkilllevelRepository skilllevelRepository) {
        super("register.title", securityService, localeService);
        this.emailConfirmationService = emailConfirmationService;

        DancerForm form = new DancerForm(DancerForm.Mode.REGISTER, cityRepository, dancestyleRepository, skilllevelRepository);
        form.setDancer(new Dancer());

        Button registerButton = new Button(getTranslation("form.register"), e -> {
            Dancer dancer = form.validateAndGetDancer();
            if (dancer == null) {
                return;
            }
            try {
                RememberedEmail.remember(dancer.email());
                DancerService.Registration registration = dancerService.registerOrResend(dancer, form.rawPassword());
                List<String> params = new ArrayList<>();
                if (emailConfirmationService.isDevelopment()) {
                    params.add("code=" + registration.dancer().emailConfirmationToken());
                }
                if (registration.resentForExisting()) {
                    params.add("resent=true");
                }
                UI.getCurrent().navigate(params.isEmpty() ? "confirm" : "confirm?" + String.join("&", params));

            }
            catch (DancerService.EmailAlreadyRegisteredException ex) {
                showErrorNotification(getTranslation("register.emailInUse"));
            }
            catch (Exception ex) {
                showException(ex);
            }
        });
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        VerticalLayout content = new VerticalLayout(form, registerButton);
        content.setMaxWidth("1200px");
        setContent(content);
    }
}
