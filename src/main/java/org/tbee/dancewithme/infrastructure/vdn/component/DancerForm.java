package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.server.StreamResource;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 * Form used by both the registration view and the profile view.
 * In REGISTER mode it contains the password field and the privacy agreement checkbox,
 * in PROFILE mode those are replaced by the active/publiclyFindable checkboxes and photo management.
 */
public class DancerForm extends VerticalLayout {

    public enum Mode {REGISTER, PROFILE}

    private final Mode mode;
    private final DancestyleRepository dancestyleRepository;
    private final RoleRepository roleRepository;

    private final Binder<Dancer> binder = new Binder<>(Dancer.class);
    private Dancer dancer;

    private final EmailField emailField = new EmailField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField nameField = new TextField();
    private final IntegerField yearOfBirthField = new IntegerField();
    private final ComboBox<City> cityComboBox = new ComboBox<>();
    private final TextArea whoamiField = new TextArea();
    private final TextArea whatdoiwantField = new TextArea();
    private final IntegerField weekFrequencyMinField = new IntegerField();
    private final IntegerField weekFrequencyMaxField = new IntegerField();
    private final IntegerField distanceToPartnerMaxField = new IntegerField();
    private final IntegerField ageDistanceToPartnerMaxField = new IntegerField();
    private final Checkbox activeCheckbox = new Checkbox();
    private final Checkbox publiclyFindableCheckbox = new Checkbox();
    private final Checkbox privacyAgreementCheckbox = new Checkbox();

    private final MemoryBuffer mugshotBuffer = new MemoryBuffer();
    private byte[] mugshotBytes;
    private final VerticalLayout mugshotPreview = new VerticalLayout();

    private final VerticalLayout dancestylesLayout = new VerticalLayout();
    private final List<DancestyleRow> dancestyleRows = new ArrayList<>();

    private final VerticalLayout photosLayout = new VerticalLayout();

    public DancerForm(Mode mode, CityRepository cityRepository, DancestyleRepository dancestyleRepository, RoleRepository roleRepository) {
        this.mode = mode;
        this.dancestyleRepository = dancestyleRepository;
        this.roleRepository = roleRepository;

        // == basic fields ==
        nameField.setRequiredIndicatorVisible(true);
        yearOfBirthField.setMin(1900);
        yearOfBirthField.setMax(Year.now().getValue() - 18);
        cityComboBox.setItems(cityRepository.findAllByOrderByNameAsc());
        cityComboBox.setItemLabelGenerator(City::name);
        whoamiField.setWidthFull();
        whoamiField.setMaxHeight("150px");
        whatdoiwantField.setWidthFull();
        whatdoiwantField.setMaxHeight("150px");

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setWidthFull();
        formLayout.addFormItem(emailField, getTranslation("form.email"));
        if (mode == Mode.REGISTER) {
            formLayout.addFormItem(passwordField, getTranslation("form.password"));
        }
        else {
            emailField.setReadOnly(true); // the email is the login name, changing it is not supported yet
        }
        formLayout.addFormItem(nameField, getTranslation("form.name"));
        formLayout.addFormItem(yearOfBirthField, getTranslation("form.yearOfBirth"));
        formLayout.addFormItem(cityComboBox, getTranslation("form.city"));
        formLayout.addFormItem(whoamiField, getTranslation("form.whoami"));
        formLayout.addFormItem(whatdoiwantField, getTranslation("form.whatdoiwant"));
        formLayout.addFormItem(new HorizontalLayout(weekFrequencyMinField, weekFrequencyMaxField), getTranslation("form.weekFrequency"));
        formLayout.addFormItem(distanceToPartnerMaxField, getTranslation("form.distanceToPartnerMax"));
        formLayout.addFormItem(ageDistanceToPartnerMaxField, getTranslation("form.ageDistanceToPartnerMax"));
        add(formLayout);

        // == flags ==
        if (mode == Mode.PROFILE) {
            activeCheckbox.setLabel(getTranslation("form.active"));
            publiclyFindableCheckbox.setLabel(getTranslation("form.publiclyFindable"));
            add(new HorizontalLayout(activeCheckbox, publiclyFindableCheckbox));
        }

        // == mugshot ==
        Upload mugshotUpload = new Upload(mugshotBuffer);
        mugshotUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        mugshotUpload.setMaxFiles(1);
        mugshotUpload.setUploadButton(new Button(getTranslation("form.upload")));
        mugshotUpload.addSucceededListener(event -> {
            try {
                mugshotBytes = mugshotBuffer.getInputStream().readAllBytes();
                showMugshotPreview();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });
        add(new H3(getTranslation("form.mugshot")), mugshotUpload, mugshotPreview);

        // == dancestyles ==
        add(new H3(getTranslation("form.dancestyles")), dancestylesLayout);
        Button addDancestyleButton = new Button(getTranslation("form.addDancestyle"), e -> addDancestyleRow(null, null));
        addDancestyleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        add(addDancestyleButton);

        // == photos (profile only) ==
        if (mode == Mode.PROFILE) {
            MemoryBuffer photoBuffer = new MemoryBuffer();
            Upload photoUpload = new Upload(photoBuffer);
            photoUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
            photoUpload.setUploadButton(new Button(getTranslation("form.upload")));
            photoUpload.addSucceededListener(event -> {
                try {
                    dancer.addPhoto(photoBuffer.getInputStream().readAllBytes());
                    refreshPhotosLayout();
                }
                catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
            add(new H3(getTranslation("form.photos")), photoUpload, photosLayout);
        }

        // == privacy agreement (register only) ==
        if (mode == Mode.REGISTER) {
            Button privacyLink = new Button(getTranslation("privacy.title"));
            privacyLink.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            privacyLink.addClickListener(e -> {
                Dialog dialog = new Dialog(new Paragraph(getTranslation("privacy.placeholder")));
                dialog.setHeaderTitle(getTranslation("privacy.title"));
                dialog.open();
            });
            privacyAgreementCheckbox.setLabel(getTranslation("form.privacyAgreement"));
            add(new HorizontalLayout(privacyAgreementCheckbox, privacyLink));
        }

        // == binder ==
        binder.forField(emailField)
                .asRequired(getTranslation("form.required"))
                .withValidator(new EmailValidator(getTranslation("form.invalidEmail")))
                .bind(Dancer::email, Dancer::email);
        binder.forField(nameField).asRequired(getTranslation("form.required")).bind(Dancer::name, Dancer::name);
        binder.forField(yearOfBirthField).asRequired(getTranslation("form.required")).bind(Dancer::yearOfBirth, Dancer::yearOfBirth);
        binder.forField(cityComboBox).bind(Dancer::city, Dancer::city);
        binder.forField(whoamiField).bind(Dancer::whoami, Dancer::whoami);
        binder.forField(whatdoiwantField).bind(Dancer::whatdoiwant, Dancer::whatdoiwant);
        binder.forField(weekFrequencyMinField).bind(Dancer::weekFrequencyMin, Dancer::weekFrequencyMin);
        binder.forField(weekFrequencyMaxField).bind(Dancer::weekFrequencyMax, Dancer::weekFrequencyMax);
        binder.forField(distanceToPartnerMaxField).bind(Dancer::distanceToPartnerMax, Dancer::distanceToPartnerMax);
        binder.forField(ageDistanceToPartnerMaxField).bind(Dancer::ageDistanceToPartnerMax, Dancer::ageDistanceToPartnerMax);
        if (mode == Mode.PROFILE) {
            binder.forField(activeCheckbox).bind(Dancer::active, Dancer::active);
            binder.forField(publiclyFindableCheckbox).bind(Dancer::publiclyFindable, Dancer::publiclyFindable);
        }
    }

    public void setDancer(Dancer dancer) {
        this.dancer = dancer;
        binder.setBean(dancer);
        this.mugshotBytes = dancer.mugshot();
        showMugshotPreview();
        dancestyleRows.clear();
        dancestylesLayout.removeAll();
        dancer.dancestyles().forEach(dd -> addDancestyleRow(dd.dancestyle(), dd.role()));
        if (mode == Mode.PROFILE) {
            refreshPhotosLayout();
        }
    }

    /**
     * Validates the form. If valid, returns the dancer with all values applied.
     */
    public Dancer validateAndGetDancer() {
        if (!binder.validate().isOk()) {
            return null;
        }
        if (mode == Mode.REGISTER) {
            if (passwordField.getValue().length() < 8) {
                passwordField.setInvalid(true);
                passwordField.setErrorMessage(getTranslation("form.passwordTooShort"));
                return null;
            }
            if (!privacyAgreementCheckbox.getValue()) {
                privacyAgreementCheckbox.setInvalid(true);
                privacyAgreementCheckbox.setErrorMessage(getTranslation("form.privacyAgreement.required"));
                return null;
            }
        }
        // apply mugshot and dancestyles
        dancer.mugshot(mugshotBytes);
        List<DancerDancestyle> dancestyles = dancestyleRows.stream()
                .filter(row -> row.styleComboBox.getValue() != null && row.roleSelect.getValue() != null)
                .map(row -> new DancerDancestyle().dancestyle(row.styleComboBox.getValue()).role(row.roleSelect.getValue()))
                .toList();
        dancer.dancestyles(dancestyles);
        return dancer;
    }

    public String rawPassword() {
        return passwordField.getValue();
    }

    private void showMugshotPreview() {
        mugshotPreview.removeAll();
        if (mugshotBytes != null && mugshotBytes.length > 0) {
            StreamResource resource = new StreamResource("mugshot.png", () -> new ByteArrayInputStream(mugshotBytes));
            Image image = new Image(resource, "");
            image.setWidth("150px");
            image.setHeight("150px");
            image.getStyle().set("object-fit", "cover").set("border-radius", "var(--lumo-border-radius-m)");
            mugshotPreview.add(image);
        }
    }

    private void refreshPhotosLayout() {
        photosLayout.removeAll();
        HorizontalLayout row = new HorizontalLayout();
        row.getStyle().set("flex-wrap", "wrap");
        dancer.photos().forEach(photo -> {
            StreamResource resource = new StreamResource("photo.png", () -> new ByteArrayInputStream(photo.image()));
            Image image = new Image(resource, "");
            image.setWidth("150px");
            image.setHeight("150px");
            image.getStyle().set("object-fit", "cover").set("border-radius", "var(--lumo-border-radius-m)");
            Button deleteButton = new Button(VaadinIcon.TRASH.create());
            deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            deleteButton.addClickListener(e -> {
                List<org.tbee.dancewithme.domain.DancerPhoto> remaining = new ArrayList<>(dancer.photos());
                remaining.remove(photo);
                dancer.photos(remaining);
                refreshPhotosLayout();
            });
            row.add(new VerticalLayout(image, deleteButton));
        });
        photosLayout.add(row);
    }

    private void addDancestyleRow(Dancestyle dancestyle, Role role) {
        DancestyleRow row = new DancestyleRow();
        row.styleComboBox.setItems(dancestyleRepository.findAll());
        row.styleComboBox.setItemLabelGenerator(Dancestyle::name);
        row.styleComboBox.setValue(dancestyle);
        row.roleSelect.setItems(roleRepository.findAll());
        row.roleSelect.setItemLabelGenerator(Role::name);
        row.roleSelect.setValue(role);
        Button removeButton = new Button(VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(e -> {
            dancestyleRows.remove(row);
            dancestylesLayout.remove(row.layout);
        });
        row.layout = new HorizontalLayout(row.styleComboBox, row.roleSelect, removeButton);
        row.layout.setAlignItems(Alignment.CENTER);
        dancestyleRows.add(row);
        dancestylesLayout.add(row.layout);
    }

    private class DancestyleRow {
        private final ComboBox<Dancestyle> styleComboBox = new ComboBox<>();
        private final Select<Role> roleSelect = new Select<>();
        private HorizontalLayout layout;
    }
}
