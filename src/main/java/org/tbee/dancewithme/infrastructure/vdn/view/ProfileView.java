package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
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

@Route("profile")
@PermitAll
public class ProfileView extends DancewithmeAppLayout {

    public ProfileView(SecurityService securityService, LocaleService localeService, DancerService dancerService,
                       CityRepository cityRepository, DancestyleRepository dancestyleRepository, RoleRepository roleRepository,
                       SkilllevelRepository skilllevelRepository) {
        super("profile.title", securityService, localeService);
        postConstruct();

        Dancer dancer = securityService.loggedInDancer().orElseThrow();
        Dancer detailedDancer = dancerService.loadWithDetails(dancer.id());

        DancerForm form = new DancerForm(DancerForm.Mode.UPDATE, cityRepository, dancestyleRepository, roleRepository, skilllevelRepository);
        form.setDancer(detailedDancer);

        Button saveButton = new Button(getTranslation("form.save"), e -> {
            Dancer updated = form.validateAndGetDancer();
            if (updated == null) {
                return;
            }
            try {
                dancerService.update(updated);
                showSuccessNotification(getTranslation("form.saved"));
            }
            catch (Exception ex) {
                showException(ex);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        VerticalLayout content = new VerticalLayout(form, saveButton);
        content.setMaxWidth("1200px");
        setContent(content);
    }
}
