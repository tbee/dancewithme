package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.renderer.ComponentRenderer;
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
import org.tbee.dancewithme.domain.valueobject.Sex;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;
import org.tbee.webstack.vdn.component.ImageUpload;

import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

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

    private final Binder<Dancer> binder = new Binder<>(Dancer.class);
    private Dancer dancer;

    private final EmailField emailField = new EmailField();
    private final PasswordField passwordField = new PasswordField();
    private final TextField nameField = new TextField();
    private final ComboBox<Sex> sexComboBox = new ComboBox<>();
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

    private final ImageUpload mugshotUpload = new ImageUpload()
            .filetypes(new String[]{"image/jpeg", "image/png", "image/webp"})
            .imageWidth("150px")
            .imageHeight("150px");
    private byte[] mugshotBytes;

    private final VerticalLayout dancestylesLayout = new VerticalLayout();
    private final List<DancestyleRow> dancestyleRows = new ArrayList<>();

    private final VerticalLayout searchingForLayout = new VerticalLayout();
    private final List<DancestyleRow> searchingForRows = new ArrayList<>();

    private final VerticalLayout photosLayout = new VerticalLayout();

    public DancerForm(Mode mode, CityRepository cityRepository, DancestyleRepository dancestyleRepository,
                      RoleRepository roleRepository, SkilllevelRepository skilllevelRepository) {
        this.mode = mode;
        this.dancestyleRepository = dancestyleRepository;
        this.roleRepository = roleRepository;
        this.skilllevelRepository = skilllevelRepository;

        setWidthFull();

        // basic fields
        nameField.setRequiredIndicatorVisible(true);
        nameField.setWidthFull();
        emailField.setWidthFull();
        yearOfBirthField.setMin(Year.now().getValue() - 100);
        yearOfBirthField.setMax(Year.now().getValue() - 10);
        cityComboBox.setItems(cityRepository.findAllByOrderByNameAsc());
        cityComboBox.setItemLabelGenerator(City::name);
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
                .asRequired(getTranslation("form.required"))
                .withValidator(new EmailValidator(getTranslation("form.invalidEmail")))
                .bind(Dancer::email, Dancer::email);
        binder.forField(nameField).asRequired(getTranslation("form.required")).bind(Dancer::name, Dancer::name);
        binder.forField(yearOfBirthField).asRequired(getTranslation("form.required")).bind(Dancer::yearOfBirth, Dancer::yearOfBirth);
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

        formLayout.addFormItem(nameField, getTranslation("form.name"));
        formLayout.addFormItem(emailField, getTranslation("form.email"));
        if (mode == Mode.REGISTER) {
            formLayout.addFormItem(passwordField, getTranslation("form.password"));
        }
        else {
            emailField.setReadOnly(true); // the email is the login name, changing it is not supported yet
        }
        formLayout.addFormItem(yearOfBirthField, getTranslation("form.yearOfBirth"));
        formLayout.addFormItem(cityComboBox, getTranslation("form.city"));

        // sex, with an explanation shown when "unknown" is selected
        sexComboBox.setItems(Sex.values());
        sexComboBox.setItemLabelGenerator(sex -> getTranslation("sex." + sex.name().toLowerCase()));
        formLayout.addFormItem(sexComboBox, getTranslation("form.sex"));
        formLayout.addFormItem(whoamiField, getTranslation("form.whoami"));

        // flags
        activeCheckbox.setLabel(getTranslation("form.active"));
        formLayout.addFormItem(activeCheckbox, "");
        publiclyFindableCheckbox.setLabel(getTranslation("form.publiclyFindable"));
        formLayout.addFormItem(publiclyFindableCheckbox, "");

        return createCard(getTranslation("form.whoami"), formLayout);
    }

    private Card createMyDancingCard() {
        FormLayout formLayout = createFormLayout();

        // dances I can do
        Button addDancestyleButton = new Button(VaadinIcon.PLUS.create());
        addDancestyleButton.getElement().setAttribute("aria-label", getTranslation("form.addDancestyle"));
        addDancestyleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addDancestyleButton.addClickListener(e -> addDancestyleRow(dancestyleRows, dancestylesLayout, true, null, null, null, null, null));
        formLayout.add(dancestylesLayout);
        formLayout.add(addDancestyleButton);

        return createCard(getTranslation("form.dancestyles"), formLayout);
    }

    private Card createPhotosCard() {
        FormLayout formLayout = createFormLayout();

        // mugshot
        formLayout.add(new H5(getTranslation("form.mugshot")), mugshotUpload);

        // extra photos
        Upload photoUpload = new Upload(UploadHandler.inMemory((metadata, bytes) -> {
            dancer.addPhoto(bytes);
            refreshPhotosLayout();
        }));
        photoUpload.setAcceptedFileTypes("image/jpeg", "image/png", "image/webp");
        photoUpload.setUploadButton(new Button(getTranslation("form.upload")));
        formLayout.add(new H5(getTranslation("form.photos")), photoUpload, photosLayout);

        return createCard(getTranslation("form.photos"), formLayout);
    }

    private Card createSearchFieldCard() {
        FormLayout formLayout = createFormLayout();

        formLayout.addFormItem(whatdoiwantField, getTranslation("form.whatdoiwant"));
        formLayout.addFormItem(new HorizontalLayout(weekFrequencyMinField, weekFrequencyMaxField), getTranslation("form.weekFrequency"));
        formLayout.addFormItem(distanceToPartnerMaxField, getTranslation("form.distanceToPartnerMax"));
        formLayout.addFormItem(ageDistanceToPartnerMaxField, getTranslation("form.ageDistanceToPartnerMax"));

        return createCard(getTranslation("form.whatdoiwant"), formLayout);
    }

    private Card createSearchDancesCard() {
        FormLayout formLayout = createFormLayout();

        // searching for
        Button addSearchingForButton = new Button(VaadinIcon.PLUS.create());
        addSearchingForButton.getElement().setAttribute("aria-label", getTranslation("form.addDancestyle"));
        addSearchingForButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addSearchingForButton.addClickListener(e -> addDancestyleRow(searchingForRows, searchingForLayout, false, null, null, null, null, null));
        formLayout.add(searchingForLayout);
        formLayout.add(addSearchingForButton);

        return createCard(getTranslation("form.searchingFor"), formLayout);
    }

    private Card createPrivacyCard() {
        FormLayout formLayout = createFormLayout();

        privacyAgreementCheckbox.setLabel(getTranslation("privacy.text"));
        formLayout.add(privacyAgreementCheckbox);

        return createCard(getTranslation("form.searchingFor"), formLayout);
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
        if (mugshotUpload.hasUpload()) {
            try (InputStream inputStream = mugshotUpload.inputStream()) {
                mugshotBytes = inputStream.readAllBytes();
            }
            catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        dancer.mugshot(mugshotBytes);
        // reuse existing entries (matched by dancestyle) so a merge does not insert duplicates of existing rows
        List<DancerDancestyle> dancestyles = dancestyleRows.stream()
                .filter(row -> row.styleComboBox.getValue() != null && row.roleSelect.getValue() != null && row.skilllevelComboBox.getValue() != null)
                .map(row -> {
                    DancerDancestyle dd = dancer.dancestyles().stream()
                            .filter(existing -> existing.dancestyle().equals(row.styleComboBox.getValue()))
                            .findFirst()
                            .orElseGet(() -> new DancerDancestyle().dancestyle(row.styleComboBox.getValue()));
                    return dd.role(row.roleSelect.getValue()).skilllevel(row.skilllevelComboBox.getValue());
                })
                .toList();
        dancer.dancestyles(dancestyles);
        List<DancerSearchingFor> searchingFor = searchingForRows.stream()
                .filter(row -> row.styleComboBox.getValue() != null && row.roleSelect.getValue() != null
                        && row.sexComboBox.getValue() != null
                        && row.skilllevelComboBox.getValue() != null && row.skilllevelMaxComboBox.getValue() != null)
                .map(row -> {
                    DancerSearchingFor sf = dancer.searchingFor().stream()
                            .filter(existing -> existing.dancestyle().equals(row.styleComboBox.getValue()))
                            .findFirst()
                            .orElseGet(() -> new DancerSearchingFor().dancestyle(row.styleComboBox.getValue()));
                    return sf.role(row.roleSelect.getValue())
                            .sex(row.sexComboBox.getValue())
                            .skilllevelMin(row.skilllevelComboBox.getValue())
                            .skilllevelMax(row.skilllevelMaxComboBox.getValue());
                })
                .toList();
        dancer.searchingFor(searchingFor);
        return dancer;
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

    private void addDancestyleRow(List<DancestyleRow> rows, VerticalLayout layout, boolean canDo, Dancestyle dancestyle, Role role, SearchCriteriaSex sex, Skilllevel skilllevel, Skilllevel skilllevelMax) {
        DancestyleRow row = new DancestyleRow();
        row.styleComboBox.setItems(dancestyleRepository.findAll());
        row.styleComboBox.setItemLabelGenerator(Dancestyle::name);
        row.styleComboBox.setValue(dancestyle);
        row.roleSelect.setItems(roleRepository.findAll());
        row.roleSelect.setItemLabelGenerator(Role::name);
        row.roleSelect.setValue(role);
        row.roleSelect.setWidth("100px");
        row.layout = new HorizontalLayout(row.styleComboBox, new NativeLabel("als"));
        // what the dancer can do: a single skill; what the dancer searches for: sex + a min/max range
        if (!canDo) {
            row.sexComboBox = new ComboBox<>();
            row.sexComboBox.setItems(SearchCriteriaSex.values());
            row.sexComboBox.setItemLabelGenerator(sexOption -> getTranslation("sex." + sexOption.name().toLowerCase()));
            row.sexComboBox.setValue(sex);
            row.layout.add(row.sexComboBox);
        }
        row.layout.add(row.roleSelect);
        row.skilllevelComboBox = createSkilllevelComboBox(skilllevel);
        if (canDo) {
            row.layout.add(new NativeLabel(getTranslation("form.skill")), row.skilllevelComboBox);
            row.layout.setFlexGrow(1, row.skilllevelComboBox);
        }
        else {
            row.layout.add(new NativeLabel(getTranslation("form.skillFrom")), row.skilllevelComboBox);
            row.layout.setFlexGrow(1, row.skilllevelComboBox);
            row.skilllevelMaxComboBox = createSkilllevelComboBox(skilllevelMax);
            row.layout.add(new NativeLabel(getTranslation("form.skillTo")), row.skilllevelMaxComboBox);
            row.layout.setFlexGrow(1, row.skilllevelMaxComboBox);
        }
        Button removeButton = new Button(VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(e -> {
            rows.remove(row);
            layout.remove(row.layout);
        });
        row.layout.add(removeButton);
        row.layout.setAlignItems(Alignment.CENTER);
        row.layout.setWidthFull();
        row.layout.setMargin(false);
        row.layout.setSpacing(true);
        row.layout.setPadding(false);
        // the fields together can be wider than the card; allow them to wrap to a second line
        row.layout.getStyle().set("flex-wrap", "wrap");
        row.layout.getStyle().set("row-gap", "var(--lumo-space-s)");
        rows.add(row);
        layout.add(row.layout);
    }

    private ComboBox<Skilllevel> createSkilllevelComboBox(Skilllevel skilllevel) {
        ComboBox<Skilllevel> comboBox = new ComboBox<>();
        comboBox.setItems(skilllevelRepository.findAllByOrderByLevelAsc());
        comboBox.setItemLabelGenerator(sl -> getTranslation("skilllevel." + sl.code()));
        // show the description as a tooltip when hovering over the unfolded options
        comboBox.setRenderer(new ComponentRenderer<>(sl -> {
            Span name = new Span(getTranslation("skilllevel." + sl.code()));
            Tooltip tooltip = Tooltip.forComponent(name)
                    .withText(getTranslation("skilllevel." + sl.code() + ".description"))
                    .withHoverDelay(300);
            tooltip.setPosition(Tooltip.TooltipPosition.END);
            return name;
        }));
        comboBox.setValue(skilllevel);
        return comboBox;
    }

    private class DancestyleRow {
        private final ComboBox<Dancestyle> styleComboBox = new ComboBox<>();
        private final Select<Role> roleSelect = new Select<>();
        // sex and a min/max skill for searching rows; a single skill for can-do rows
        private ComboBox<SearchCriteriaSex> sexComboBox;
        private ComboBox<Skilllevel> skilllevelComboBox;
        private ComboBox<Skilllevel> skilllevelMaxComboBox;
        private HorizontalLayout layout;
    }
}
