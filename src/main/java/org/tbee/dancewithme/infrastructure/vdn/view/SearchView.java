package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.SearchService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.io.ByteArrayInputStream;
import java.time.Year;
import java.util.List;

@Route("")
@AnonymousAllowed
public class SearchView extends DancewithmeAppLayout {

    private static final int PAGE_SIZE = 10;

    private final transient SecurityService securityService;
    private final transient SearchService searchService;

    private final ComboBox<Dancestyle> dancestyleComboBox = new ComboBox<>();
    private final Select<Role> roleSelect = new Select<>();
    private final IntegerField ageMinField = new IntegerField();
    private final IntegerField ageMaxField = new IntegerField();
    private final IntegerField weekFrequencyMinField = new IntegerField();
    private final IntegerField weekFrequencyMaxField = new IntegerField();
    private final IntegerField distanceMaxField = new IntegerField();

    private final VerticalLayout resultsLayout = new VerticalLayout();
    private final HorizontalLayout pagingLayout = new HorizontalLayout();
    private final Span pageLabel = new Span();

    private List<SearchService.SearchResult> results = List.of();
    private int page = 0;

    public SearchView(SecurityService securityService, LocaleService localeService, SearchService searchService,
                      DancestyleRepository dancestyleRepository, RoleRepository roleRepository) {
        super("search.title", securityService, localeService);
        this.securityService = securityService;
        this.searchService = searchService;
        postConstruct();

        boolean loggedIn = securityService.isLoggedIn();

        // == search form ==
        H2 titleH2 = new H2(getTranslation("search.title"));
        Paragraph subtitle = new Paragraph(getTranslation("search.subtitle"));

        dancestyleComboBox.setItems(dancestyleRepository.findAll());
        dancestyleComboBox.setItemLabelGenerator(Dancestyle::name);
        dancestyleComboBox.setPlaceholder(getTranslation("search.dancestyle.placeholder"));

        roleSelect.setItems(roleRepository.findAll());
        // label generator must be null-safe: Vaadin also applies it to the empty-selection item
        roleSelect.setItemLabelGenerator(role -> role == null ? "" : role.name());
        roleSelect.setEmptySelectionCaption(getTranslation("search.role.any"));
        roleSelect.setEmptySelectionAllowed(true);

        ageMinField.setPlaceholder(getTranslation("search.age.min"));
        ageMinField.setMin(0);
        ageMaxField.setPlaceholder(getTranslation("search.age.max"));
        ageMaxField.setMin(0);
        weekFrequencyMinField.setPlaceholder(getTranslation("search.age.min"));
        weekFrequencyMinField.setMin(0);
        weekFrequencyMinField.setMax(7);
        weekFrequencyMaxField.setPlaceholder(getTranslation("search.age.max"));
        weekFrequencyMaxField.setMin(0);
        weekFrequencyMaxField.setMax(7);
        distanceMaxField.setMin(0);
        distanceMaxField.setVisible(loggedIn); // distance search is only available for logged in users

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.addFormItem(dancestyleComboBox, getTranslation("search.dancestyle"));
        formLayout.addFormItem(roleSelect, getTranslation("search.role"));
        formLayout.addFormItem(new HorizontalLayout(ageMinField, ageMaxField), getTranslation("search.age"));
        formLayout.addFormItem(new HorizontalLayout(weekFrequencyMinField, weekFrequencyMaxField), getTranslation("search.weekFrequency"));
        if (loggedIn) {
            formLayout.addFormItem(distanceMaxField, getTranslation("search.distance"));
        }

        Button searchButton = new Button(getTranslation("search.button"), e -> search());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        VerticalLayout content = new VerticalLayout(titleH2, subtitle, formLayout, searchButton, resultsLayout, pagingLayout);
        content.setMaxWidth("900px");
        setContent(content);
    }

    private void search() {
        SearchService.SearchCriteria criteria = new SearchService.SearchCriteria(
                dancestyleComboBox.getValue(),
                roleSelect.getValue(),
                ageMinField.getValue(),
                ageMaxField.getValue(),
                weekFrequencyMinField.getValue(),
                weekFrequencyMaxField.getValue(),
                distanceMaxField.getValue());
        try {
            results = searchService.search(criteria, securityService.currentDancer().orElse(null));
            page = 0;
            renderResults();
        }
        catch (Exception e) {
            showException(e);
        }
    }

    private void renderResults() {
        resultsLayout.removeAll();
        pagingLayout.removeAll();

        resultsLayout.add(new H2(getTranslation("search.results")));
        if (results.isEmpty()) {
            resultsLayout.add(new Paragraph(getTranslation("search.noResults")));
            return;
        }
        if (!securityService.isLoggedIn()) {
            resultsLayout.add(new Paragraph(getTranslation("search.loginToSeeDetails")));
        }

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, results.size());
        results.subList(from, to).forEach(result -> resultsLayout.add(createCard(result)));

        // paging
        Button previousButton = new Button(getTranslation("paging.previous"), e -> { page--; renderResults(); });
        previousButton.setEnabled(page > 0);
        Button nextButton = new Button(getTranslation("paging.next"), e -> { page++; renderResults(); });
        nextButton.setEnabled(to < results.size());
        pageLabel.setText((page + 1) + " / " + ((results.size() + PAGE_SIZE - 1) / PAGE_SIZE));
        pagingLayout.add(previousButton, pageLabel, nextButton);
        pagingLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
    }

    private HorizontalLayout createCard(SearchService.SearchResult result) {
        Dancer dancer = result.dancer();
        boolean loggedIn = securityService.isLoggedIn();

        // name, age
        int age = Year.now().getValue() - dancer.yearOfBirth();
        Span nameAge = new Span(dancer.name() + ", " + getTranslation("card.yearsOld", age));
        nameAge.getStyle().set("font-weight", "bold").set("font-size", "var(--lumo-font-size-l)");

        // city + distance
        String cityText = dancer.city() != null ? dancer.city().name() : "";
        if (result.distanceKm() != null) {
            cityText += ", " + getTranslation("card.km", Math.round(result.distanceKm()));
        }
        HorizontalLayout cityLayout = new HorizontalLayout(VaadinIcon.MAP_MARKER.create(), new Span(cityText));
        cityLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        cityLayout.setSpacing(false);

        // whoami excerpt
        String whoami = dancer.whoami() == null ? "" : dancer.whoami();
        if (whoami.length() > 200) {
            whoami = whoami.substring(0, 200) + "...";
        }
        Paragraph whoamiParagraph = new Paragraph(whoami);

        // role + style badge
        String badgeText = dancer.dancestyles().stream()
                .map(dd -> dd.role().name() + " " + dd.dancestyle().name())
                .distinct()
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        Span badge = new Span(badgeText);
        badge.getStyle()
                .set("background", "var(--lumo-primary-color)")
                .set("color", "var(--lumo-primary-contrast-color)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("padding", "var(--lumo-space-xs) var(--lumo-space-s)")
                .set("font-size", "var(--lumo-font-size-s)");

        Span frequency = new Span(getTranslation("card.perWeek", dancer.weekFrequencyMin(), dancer.weekFrequencyMax()));

        // buttons
        Button viewProfileButton = new Button(getTranslation("card.viewProfile"), e -> {
            if (loggedIn) {
                UI.getCurrent().navigate(DancerDetailView.class, new RouteParameters("dancerId", String.valueOf(dancer.id())));
            }
            else {
                UI.getCurrent().navigate(LoginView.class);
            }
        });
        Button sendMessageButton = new Button(getTranslation("card.sendMessage"), e ->
                Notification.show(getTranslation("card.comingSoon")));
        sendMessageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout badgeBar = new HorizontalLayout(badge);
        badgeBar.getStyle().set("margin-left", "auto");

        HorizontalLayout headerLine = new HorizontalLayout(nameAge, badgeBar);
        headerLine.setWidthFull();

        VerticalLayout middle = new VerticalLayout(headerLine, cityLayout, whoamiParagraph, frequency,
                new HorizontalLayout(sendMessageButton, viewProfileButton));
        middle.setPadding(false);
        middle.setSpacing(false);

        HorizontalLayout card = new HorizontalLayout(middle);
        if (loggedIn && dancer.mugshot() != null) {
            StreamResource resource = new StreamResource("mugshot" + dancer.id() + ".png", () -> new ByteArrayInputStream(dancer.mugshot()));
            Image image = new Image(resource, dancer.name());
            image.setWidth("160px");
            image.setHeight("160px");
            image.getStyle().set("object-fit", "cover").set("border-radius", "var(--lumo-border-radius-m)");
            card.addComponentAsFirst(image);
        }
        else if (!loggedIn) {
            Avatar avatar = new Avatar(dancer.name());
            avatar.setWidth("80px");
            avatar.setHeight("80px");
            card.addComponentAsFirst(avatar);
        }
        card.getStyle()
                .set("border", "1px solid var(--lumo-contrast-10pct)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("padding", "var(--lumo-space-m)")
                .set("box-shadow", "var(--lumo-box-shadow-xs)");
        card.setWidthFull();
        return card;
    }
}
