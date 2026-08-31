package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.tbee.dancewithme.application.DancerService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.component.DancerForm;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;
import org.vaadin.firitin.components.dialog.ConfirmationDialog;

@Route("profile")
@PermitAll
public class ProfileView extends DancewithmeAppLayout {

    private final SecurityService securityService;
    private final DancerService dancerService;
    private final DancerForm form;
    public ProfileView(SecurityService securityService, LocaleService localeService, DancerService dancerService,
                       CityRepository cityRepository, DancestyleRepository dancestyleRepository) {
        super("profile.title", securityService, localeService);
        this.securityService = securityService;
        this.dancerService = dancerService;

        Dancer dancer = securityService.loggedInDancer().orElseThrow();
        Dancer detailedDancer = dancerService.loadWithDetails(dancer.id());

        form = new DancerForm(DancerForm.Mode.UPDATE, cityRepository, dancestyleRepository);
        form.setDancer(detailedDancer);

        Button saveButton = new Button(getTranslation("form.save"), e -> save());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        Button deleteButton = new Button(getTranslation("form.delete"), e -> delete());
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_LARGE);

        VerticalLayout content = new VerticalLayout(form, new HorizontalLayout(saveButton, deleteButton));
        content.setMaxWidth("1200px");
        setContent(content);
    }

    private void save() {
        Dancer dancer = form.validateAndGetDancer();
        if (dancer == null) {
            return;
        }
        try {
            Dancer saved = dancerService.update(dancer);
            form.setDancer(saved);
            showSuccessNotification(getTranslation("form.saved"));
        }
        catch (Exception ex) {
            showException(ex);
        }
    }

    private void delete() {
        new org.tbee.webstack.vdn.component.ConfirmationDialog(getTranslation("form.delete"), new Markdown(getTranslation("form.deleteConsequences")))
                .id("DeleteDialog").overlayRole("DeleteDialogOverlay")
                .cancelable()
                .confirmText(getTranslation("form.delete"))
                .onConfirm(this::deleteConfirmed)
                .open();
    }

    private void deleteConfirmed() {
        Dancer dancer = securityService.loggedInDancer().orElseThrow();
        try {
            dancerService.delete(dancer);
            securityService.logout();
        }
        catch (Exception ex) {
            showException(ex);
        }
    }
}
