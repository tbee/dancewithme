package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.server.streams.UploadHandler;
import org.jspecify.annotations.NonNull;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerDancestyle;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;
import org.tbee.dancewithme.domain.service.ValidateDancer;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.webstack.vdn.component.ImageUpload;

import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Form used by both the registration view and the profile view.
 * In REGISTER mode it contains the password field and the privacy agreement checkbox,
 * in PROFILE mode those are replaced by the active/publiclyFindable checkboxes and photo management.
 */
public class DancerForm extends VerticalLayout {

    public enum Mode {REGISTER, UPDATE}

    private final Mode mode;
    private final DancestyleRepository dancestyleRepository;
    private final RoleRepository roleRepository;
    private final SkilllevelRepository skilllevelRepository;
    private final ValidateDancer validateDancer = new ValidateDancer();

    private final Binder<Dancer> binder = new Binder<>(Dancer.class);
    private Dancer dancer;

    private final EmailField emailField = new EmailField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField nameField = new TextField();
    private final SexComboBox sexComboBox = new SexComboBox();
    private final IntegerField yearOfBirthField = new IntegerField();
    private final ComboBox<City> cityComboBox;
    private final TextArea whoamiField = new TextArea();
    private final TextArea whatdoiwantField = new TextArea();
    private final IntegerField weekFrequencyMinField = new IntegerField();
    private final IntegerField weekFrequencyMaxField = new IntegerField();
    private final IntegerField distanceToPartnerMaxField = new IntegerField();
    private final IntegerField ageDistanceToPartnerMaxField = new IntegerField();
    private final Checkbox activeCheckbox = new Checkbox();
    private final Checkbox publiclyFindableCheckbox = new Checkbox();
    private final Checkbox privacyAgreementCheckbox = new Checkbox();

    private final ImageUpload mugshotUpload = new ImageUpload()
            .filetypes(new String[]{"image/jpeg", "image/png", "image/webp"})
            .imageWidth("150px")
            .imageHeight("150px");
    private byte[] mugshotBytes;

    private final VerticalLayout dancestylesLayout = noPaddingVerticalLayout();
    private final List<SearchingForRow> dancestyleRows = new ArrayList<>();

    private final VerticalLayout searchingForLayout = noPaddingVerticalLayout();
    private final List<SearchingForRow> searchingForRows = new ArrayList<>();

    private final VerticalLayout photosLayout = noPaddingVerticalLayout();

    public DancerForm(Mode mode, CityRepository cityRepository, DancestyleRepository dancestyleRepository,
                      RoleRepository roleRepository, SkilllevelRepository skilllevelRepository) {
        this.mode = mode;
        this.dancestyleRepository = dancestyleRepository;
        this.roleRepository = roleRepository;
        this.skilllevelRepository = skilllevelRepository;
        this.cityComboBox = new CityComboBox(cityRepository);

        setWidthFull();

        // basic fields
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();
        emailField.setWidthFull();
        yearOfBirthField.setMin(Year.now().getValue() - 100);
        yearOfBirthField.setMax(Year.now().getValue() - 10);
        cityComboBox.setWidthFull();
        whoamiField.setWidthFull();
        whoamiField.setHeight("300px");
        whatdoiwantField.setWidthFull();
        whatdoiwantField.setHeight("300px");

        add(createAboutMeCard());
        add(createMyDancingCard());
        add(createPhotosCard());
        add(createSearchFieldCard());
        add(createSearchDancesCard());

        // ----------------------------

        // privacy agreement
        if (mode == Mode.REGISTER) {
            add(createPrivacyCard());
        }

        // binder
        binder.forField(emailField)
                .asRequired(t("form.required"))
                .withValidator(new EmailValidator(t("form.invalidEmail")))
                .bind(Dancer::email, Dancer::email);
        binder.forField(nameField).asRequired(t("form.required")).bind(Dancer::name, Dancer::name);
        binder.forField(yearOfBirthField).asRequired(t("form.required")).bind(Dancer::yearOfBirth, Dancer::yearOfBirth);
        binder.forField(sexComboBox).bind(Dancer::sex, Dancer::sex);
        binder.forField(cityComboBox).bind(Dancer::city, Dancer::city);
        binder.forField(whoamiField).bind(Dancer::whoami, Dancer::whoami);
        binder.forField(whatdoiwantField).bind(Dancer::whatdoiwant, Dancer::whatdoiwant);
        binder.forField(weekFrequencyMinField).bind(Dancer::weekFrequencyMin, Dancer::weekFrequencyMin);
        binder.forField(weekFrequencyMaxField).bind(Dancer::weekFrequencyMax, Dancer::weekFrequencyMax);
        binder.forField(distanceToPartnerMaxField).bind(Dancer::distanceMax, Dancer::distanceMax);
        binder.forField(ageDistanceToPartnerMaxField).bind(Dancer::ageDistanceMax, Dancer::ageDistanceMax);
        binder.forField(activeCheckbox).bind(Dancer::active, Dancer::active);
        binder.forField(publiclyFindableCheckbox).bind(Dancer::publiclyFindable, Dancer::publiclyFindable);
    }

    private Card createAboutMeCard() {
        FormLayout formLayout = createFormLayout();

        formLayout.addFormItem(nameField, t("form.name"));
        formLayout.addFormItem(emailField, t("form.email"));
        if (mode == Mode.REGISTER) {
            formLayout.addFormItem(passwordField, t("form.password"));
        }
        else {
            emailField.setReadOnly(true); // the email is the login name, changing it is not supported yet
        }
        formLayout.addFormItem(yearOfBirthField, t("form.yearOfBirth"));
        formLayout.addFormItem(cityComboBox, t("form.city"));

        // sex, with an explanation shown when "unknown" is selected
        formLayout.addFormItem(sexComboBox, t("form.sex"));
        formLayout.addFormItem(whoamiField, t("form.whoami"));

        // flags
        activeCheckbox.setLabel(t("form.active"));
        formLayout.addFormItem(activeCheckbox, "");
        publiclyFindableCheckbox.setLabel(t("form.publiclyFindable"));
        formLayout.addFormItem(publiclyFindableCheckbox, "");

        return createCard(t("form.whoami"), formLayout);
    }

    private Card createMyDancingCard() {
        FormLayout formLayout = createFormLayout();

        // dances I can do
        Button addDancestyleButton = new Button(VaadinIcon.PLUS.create());
        addDancestyleButton.getElement().setAttribute("aria-label", t("form.addDancestyle"));
        addDancestyleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addDancestyleButton.addClickListener(e -> addDancestyleRow(dancestyleRows, dancestylesLayout, true, null, null, null, null, null));
        formLayout.add(dancestylesLayout);
        formLayout.add(addDancestyleButton);

        return createCard(t("form.dancestyles"), formLayout);
    }

    private Card createPhotosCard() {
        FormLayout formLayout = createFormLayout();

        // mugshot
        formLayout.add(new H5(t("form.mugshot")), mugshotUpload);

        // extra photos
        Upload photoUpload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            dancer.addPhoto(bytes);
            refreshPhotosLayout();
        }));
        photoUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        photoUpload.setUploadButton(new Button(t("form.upload")));
        formLayout.add(new H5(t("form.photos")), photoUpload, photosLayout);

        return createCard(t("form.photos"), formLayout);
    }

    private Card createSearchFieldCard() {
        FormLayout formLayout = createFormLayout();

        formLayout.addFormItem(whatdoiwantField, t("form.whatdoiwant"));
        formLayout.addFormItem(new HorizontalLayout(weekFrequencyMinField, weekFrequencyMaxField), t("form.weekFrequency"));
        formLayout.addFormItem(distanceToPartnerMaxField, t("form.distanceMax"));
        formLayout.addFormItem(ageDistanceToPartnerMaxField, t("form.ageDistanceMax"));

        return createCard(t("form.whatdoiwant"), formLayout);
    }

    private Card createSearchDancesCard() {
        FormLayout formLayout = createFormLayout();

        // searching for
        Button addSearchingForButton = new Button(VaadinIcon.PLUS.create());
        addSearchingForButton.getElement().setAttribute("aria-label", t("form.addDancestyle"));
        addSearchingForButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addSearchingForButton.addClickListener(e -> addDancestyleRow(searchingForRows, searchingForLayout, false, null, null, null, null, null));
        formLayout.add(searchingForLayout);
        formLayout.add(addSearchingForButton);

        return createCard(t("form.searchingFor"), formLayout);
    }

    private Card createPrivacyCard() {
        FormLayout formLayout = createFormLayout();

        privacyAgreementCheckbox.setLabel(t("privacy.text"));
        formLayout.add(privacyAgreementCheckbox);

        return createCard(t("form.privacyAgreement.required"), formLayout);
    }

    private @NonNull Card createCard(String title, FormLayout formLayout) {
        Card card = new Card();
        card.setWidthFull();
        card.add(formLayout);
        card.setTitle(title);
        return card;
    }

    private static @NonNull FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.setWidthFull();
        return formLayout;
    }

    public void setDancer(Dancer dancer) {
        this.dancer = dancer;
        binder.setBean(dancer);
        this.mugshotBytes = dancer.mugshot();
        if (mugshotBytes != null && mugshotBytes.length > 0) {
            // browsers sniff the actual image type, so a generic mime is fine
            mugshotUpload.src("data:image/*;base64," + java.util.Base64.getEncoder().encodeToString(mugshotBytes));
        }
        dancestyleRows.clear();
        dancestylesLayout.removeAll();
        dancer.dancestyles().forEach(dd -> addDancestyleRow(dancestyleRows, dancestylesLayout, true, dd.dancestyle(), dd.role(), null, dd.skilllevel(), null));
        searchingForRows.clear();
        searchingForLayout.removeAll();
        dancer.searchingFor().forEach(sf -> addDancestyleRow(searchingForRows, searchingForLayout, false, sf.dancestyle(), sf.role(), sf.sex(), sf.skilllevelMin(), sf.skilllevelMax()));
        if (mode == Mode.UPDATE) {
            refreshPhotosLayout();
        }
    }

    /**
     * Validates the form. If valid, returns the dancer with all values applied.
     */
    public Dancer validateAndGetDancer() {
        if (!binder.validate().isOk()) {
            DancewithmeAppLayout.showErrorNotification(t("form.validationFailed"));
            return null;
        }

        // apply mugshot
        if (mugshotUpload.hasUpload()) {
            try (InputStream inputStream = mugshotUpload.inputStream()) {
                mugshotBytes = inputStream.readAllBytes();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        dancer.mugshot(mugshotBytes);

        // dancestyles
        // reuse existing entries (matched by dancestyle) so a merge does not insert duplicates of existing rows
        List<DancerDancestyle> dancestyles = dancestyleRows.stream()
                .filter(row -> row.style() != null
                        && row.role() != null
                        && row.skilllevelMin() != null)
                .map(row -> {
                    // Search for a row matching on dancestyle, if not found create a new one
                    DancerDancestyle dd = dancer.dancestyles().stream()
                            .filter(existing -> existing.dancestyle().equals(row.style()))
                            .findFirst()
                            .orElseGet(() -> new DancerDancestyle().dancestyle(row.style()));
                    // Populate the found or new
                    return dd.role(row.role())
                            .skilllevel(row.skilllevelMin());
                })
                .toList();
        dancer.dancestyles(dancestyles);

        // searchingFor
        // reuse existing entries (matched by dancestyle) so a merge does not insert duplicates of existing rows
        List<DancerSearchingFor> searchingFor = searchingForRows.stream()
                .filter(row -> row.style() != null
                        && row.role() != null
                        && row.sex() != null
                        && row.skilllevelMin() != null
                        && row.skilllevelMax() != null)
                .map(row -> {
                    // Search for a row matching on dancestyle, if not found create a new one
                    DancerSearchingFor sf = dancer.searchingFor().stream()
                            .filter(existing -> existing.dancestyle().equals(row.style()))
                            .findFirst()
                            .orElseGet(() -> new DancerSearchingFor().dancestyle(row.style()));
                    // Populate the found or new
                    return sf.role(row.role())
                            .sex(row.sex())
                            .skilllevelMin(row.skilllevelMin())
                            .skilllevelMax(row.skilllevelMax());
                })
                .toList();
        dancer.searchingFor(searchingFor);

        // domain validation
        String rawPassword = (mode == Mode.REGISTER) ? passwordField.getValue() : null;
        List<ValidateDancer.Problem> problems = validateDancer.validate(dancer, rawPassword, privacyAgreementCheckbox.getValue());
        if (!problems.isEmpty()) {
            showValidationProblems(problems);
            return null;
        }
        return dancer;
    }

    /**
     * Translates the domain validation problems to UI feedback (field errors and a toast).
     */
    private void showValidationProblems(List<ValidateDancer.Problem> problems) {
        for (ValidateDancer.Problem problem : problems) {
            switch (problem) {
                case PASSWORD_TOO_SHORT -> {
                    passwordField.setInvalid(true);
                    passwordField.setErrorMessage(t("form.passwordTooShort"));
                }
                case PRIVACY_AGREEMENT_REQUIRED -> {
                    privacyAgreementCheckbox.setInvalid(true);
                    privacyAgreementCheckbox.setErrorMessage(t("form.privacyAgreement.required"));
                }
                case DUPLICATE_DANCESTYLE -> markDuplicateDancestyles(dancestyleRows, "form.dancestyles.duplicate");
                case DUPLICATE_SEARCHING_FOR -> markDuplicateDancestyles(searchingForRows, "form.searchingFor.duplicate");
            }
        }
        DancewithmeAppLayout.showErrorNotification(t("form.validationFailed"));
    }

    /**
     * Marks the duplicate dancestyle rows invalid. The detection itself is
     * done in {@link ValidateDancer}; this method only renders the outcome on the UI.
     */
    private void markDuplicateDancestyles(List<SearchingForRow> rows, String errorKey) {
        Set<Dancestyle> seen = new HashSet<>();
        for (SearchingForRow row : rows) {
            Dancestyle style = row.style();
            if (style != null && !seen.add(style)) {
                row.styleComboBox.setInvalid(true);
                row.styleComboBox.setErrorMessage(t(errorKey));
            }
        }
    }

    public String rawPassword() {
        return passwordField.getValue();
    }

    private void refreshPhotosLayout() {
        photosLayout.removeAll();
        HorizontalLayout row = new HorizontalLayout();
        row.getStyle().set("flex-wrap", "wrap");
        dancer.photos().forEach(photo -> {
            Image image = new Image(photo.image(), "photo");
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

    private void addDancestyleRow(List<SearchingForRow> rows, VerticalLayout layout, boolean aboutDancer, Dancestyle dancestyle, Role role, SearchCriteriaSex sex, Skilllevel skilllevel, Skilllevel skilllevelMax) {
        SearchingForRow row;
        if (aboutDancer) {
            row = new DancestyleRow(dancestyleRepository, roleRepository, skilllevelRepository, r -> {
                rows.remove(r);
                layout.remove(r);
            });
        }
        else {
            row = new SearchingForRow(dancestyleRepository, roleRepository, skilllevelRepository, r -> {
                rows.remove(r);
                layout.remove(r);
            });
        }
        row.searchCriteriaSexComboBox.setValue(sex);
        row.skilllevelMaxComboBox.setValue(skilllevelMax);
        row.styleComboBox.setValue(dancestyle);
        row.roleSelect.setValue(role);
        row.skilllevelMinComboBox.setValue(skilllevel);
        layout.add(row);
        rows.add(row);
    }

    private static @NonNull VerticalLayout noPaddingVerticalLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setMargin(false);
        return layout;
    }

    private String t(String key, Object... params) {
        return getTranslation(key, params);
    }
}
