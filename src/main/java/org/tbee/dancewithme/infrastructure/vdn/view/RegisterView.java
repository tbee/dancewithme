package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.DancerService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.component.DancerForm;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

@Route("register")
@AnonymousAllowed
public class RegisterView extends DancewithmeAppLayout {

    public RegisterView(SecurityService securityService, LocaleService localeService, DancerService dancerService,
                        CityRepository cityRepository, DancestyleRepository dancestyleRepository, RoleRepository roleRepository,
                        SkilllevelRepository skilllevelRepository) {
        super("register.title", securityService, localeService);
        postConstruct();

        DancerForm form = new DancerForm(DancerForm.Mode.REGISTER, cityRepository, dancestyleRepository, roleRepository, skilllevelRepository);
        form.setDancer(new Dancer());

        Button registerButton = new Button(getTranslation("form.register"), e -> {
            Dancer dancer = form.validateAndGetDancer();
            if (dancer == null) {
                return;
            }
            try {
                dancerService.register(dancer, form.rawPassword());
                showSuccessNotification(getTranslation("form.registered"));
                UI.getCurrent().navigate(LoginView.class);
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
