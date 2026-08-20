package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.DancerService;
import org.tbee.dancewithme.application.SearchService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Route("")
@AnonymousAllowed
public class SearchView extends DancewithmeAppLayout {

    private static final int PAGE_SIZE = 10;

    private final SecurityService securityService;
    private final SearchService searchService;

    private final VerticalLayout styleRowsLayout = new VerticalLayout();
    private final List<SearchStyleRow> styleRows = new ArrayList<>();
    private final IntegerField ageDistanceField = new IntegerField();
    private final IntegerField weekFrequencyMinField = new IntegerField();
    private final IntegerField weekFrequencyMaxField = new IntegerField();
    private final IntegerField distanceMaxField = new IntegerField();

    private final DancestyleRepository dancestyleRepository;
    private final RoleRepository roleRepository;
    private final SkilllevelRepository skilllevelRepository;

    private final VerticalLayout resultsLayout = new VerticalLayout();
    private final HorizontalLayout pagingLayout = new HorizontalLayout();
    private final Span pageLabel = new Span();

    private List<SearchService.SearchResult> results = List.of();
    private int page = 0;

    public SearchView(SecurityService securityService, LocaleService localeService, SearchService searchService,
                      DancerService dancerService, DancestyleRepository dancestyleRepository, RoleRepository roleRepository,
                      SkilllevelRepository skilllevelRepository) {
        super("search.title", securityService, localeService);
        this.securityService = securityService;
        this.searchService = searchService;
        this.dancestyleRepository = dancestyleRepository;
        this.roleRepository = roleRepository;
        this.skilllevelRepository = skilllevelRepository;
        postConstruct();

        boolean loggedIn = securityService.isLoggedIn();

        // == search form ==
        // style rows: dancestyle, role, skill range (same structure as the profile's "searching for")
        // prefill with what the logged in dancer is searching for (can still be fiddled with)
        List<DancerSearchingFor> searchingFor = securityService.currentDancer()
                .map(currentDancer -> dancerService.searchingForOf(currentDancer.id()))
                .orElse(List.of());
        if (searchingFor.isEmpty()) {
            addStyleRow(null, null, null, null, null);
        }
        else {
            searchingFor.forEach(entry -> addStyleRow(entry.dancestyle(), entry.role(), entry.sex(), entry.skilllevelMin(), entry.skilllevelMax()));
        }
        Button addStyleButton = new Button(VaadinIcon.PLUS.create());
        addStyleButton.getElement().setAttribute("aria-label", getTranslation("form.addDancestyle"));
        addStyleButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        addStyleButton.addClickListener(e -> addStyleRow(null, null, null, null, null));

        styleRowsLayout.setPadding(false);
        styleRowsLayout.setSpacing(true);
        VerticalLayout styleRowsWithButton = new VerticalLayout(styleRowsLayout, addStyleButton);
        styleRowsWithButton.setPadding(false);

        ageDistanceField.setMin(0);
        ageDistanceField.setVisible(loggedIn); // age distance is relative to the logged in user's age
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
        formLayout.addFormItem(styleRowsWithButton, getTranslation("search.dancestyle"));
        if (loggedIn) {
            formLayout.addFormItem(ageDistanceField, getTranslation("search.ageDistance"));
        }
        formLayout.addFormItem(new HorizontalLayout(weekFrequencyMinField, weekFrequencyMaxField), getTranslation("search.weekFrequency"));
        if (loggedIn) {
            formLayout.addFormItem(distanceMaxField, getTranslation("search.distance"));
        }

        Button searchButton = new Button(getTranslation("search.button"), e -> search());
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        VerticalLayout content = new VerticalLayout(formLayout, searchButton, resultsLayout, pagingLayout);
//        content.setMaxWidth("1200px");
        setContent(content);
    }

    private void addStyleRow(Dancestyle dancestyle, Role role, SearchCriteriaSex sex, Skilllevel skilllevelMin, Skilllevel skilllevelMax) {
        SearchStyleRow row = new SearchStyleRow();
        row.styleComboBox.setItems(dancestyleRepository.findAll());
        row.styleComboBox.setItemLabelGenerator(Dancestyle::name);
        row.styleComboBox.setValue(dancestyle);
        row.styleComboBox.setPlaceholder(getTranslation("search.dancestyle.placeholder"));
        row.roleSelect.setItems(roleRepository.findAll());
        row.roleSelect.setItemLabelGenerator(Role::name);
        row.roleSelect.setValue(role);
        row.roleSelect.setWidth("100px");
        row.sexComboBox.setItems(SearchCriteriaSex.values());
        row.sexComboBox.setItemLabelGenerator(sexOption -> getTranslation("sex." + sexOption.name().toLowerCase()));
        row.sexComboBox.setValue(sex);
        row.skilllevelMinComboBox.setItems(skilllevelRepository.findAllByOrderByLevelAsc());
        row.skilllevelMinComboBox.setItemLabelGenerator(sl -> getTranslation("skilllevel." + sl.code()));
        row.skilllevelMinComboBox.setValue(skilllevelMin);
        row.skilllevelMaxComboBox.setItems(skilllevelRepository.findAllByOrderByLevelAsc());
        row.skilllevelMaxComboBox.setItemLabelGenerator(sl -> getTranslation("skilllevel." + sl.code()));
        row.skilllevelMaxComboBox.setValue(skilllevelMax);

        Button removeButton = new Button(VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(e -> {
            styleRows.remove(row);
            styleRowsLayout.remove(row.layout);
        });
        row.layout = new HorizontalLayout(row.styleComboBox, new NativeLabel(getTranslation("search.role")),
                row.roleSelect, row.sexComboBox, new NativeLabel(getTranslation("search.skillFrom")), row.skilllevelMinComboBox,
                new NativeLabel(getTranslation("search.skillTo")), row.skilllevelMaxComboBox, removeButton);
        row.layout.setAlignItems(HorizontalLayout.Alignment.CENTER);
//        row.layout.setWidthFull();
        row.layout.getStyle().set("flex-wrap", "wrap");
        row.layout.getStyle().set("row-gap", "var(--lumo-space-s)");
        row.layout.setPadding(false);
        row.layout.setMargin(false);
        styleRows.add(row);
        styleRowsLayout.add(row.layout);
    }

    private static class SearchStyleRow {
        private final ComboBox<Dancestyle> styleComboBox = new ComboBox<>();
        private final ComboBox<Role> roleSelect = new ComboBox<>();
        private final ComboBox<SearchCriteriaSex> sexComboBox = new ComboBox<>();
        private final ComboBox<Skilllevel> skilllevelMinComboBox = new ComboBox<>();
        private final ComboBox<Skilllevel> skilllevelMaxComboBox = new ComboBox<>();
        private HorizontalLayout layout;
    }

    private void search() {
        SearchService.SearchParameters searchParameters = new SearchService.SearchParameters(){
            @Override
            public int ageDistanceMax() {
                return ageDistanceField.getValue();
            }

            @Override
            public int weekFrequencyMin() {
                return weekFrequencyMinField.getValue();
            }

            @Override
            public int weekFrequencyMax() {
                return weekFrequencyMaxField.getValue();
            }

            @Override
            public int distanceMax() {
                return distanceMaxField.getValue();
            }

            @Override
            public List<? extends SearchService.SearchParametersStyles> searchingFor() {
                return styleRows.stream()
                        .filter(row -> row.styleComboBox.getValue() != null)
                        .map(row -> new SearchService.SearchParametersStyles(){

                            @Override
                            public Dancestyle dancestyle() {
                                return row.styleComboBox.getValue();
                            }

                            @Override
                            public Role role() {
                                return row.roleSelect.getValue();
                            }

                            @Override
                            public SearchCriteriaSex sex() {
                                return row.sexComboBox.getValue();
                            }

                            @Override
                            public Skilllevel skilllevelMin() {
                                return row.skilllevelMinComboBox.getValue();
                            }

                            @Override
                            public Skilllevel skilllevelMax() {
                                return row.skilllevelMaxComboBox.getValue();
                            }
                        })
                        .toList();
            }
        };
        try {
            results = searchService.search(searchParameters);
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
        Dancer currentDancer = securityService.currentDancer().orElseGet(() -> null);
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
        Markdown whoamiParagraph = new Markdown(whoami);

        // role + style badges
        HorizontalLayout badgeBar = new HorizontalLayout();
        dancer.dancestyles().stream()
                .map(dd -> dd.role().name() + " " + dd.dancestyle().name())
                .distinct()
                .forEach(s -> badgeBar.add(new Badge(s)));
        // mutual match badge: does their search match us?
        boolean match = !searchService.match(dancer, List.of(currentDancer)).isEmpty();
        Badge matchBadge = match
                ? new Badge(getTranslation("card.match"))
                : new Badge(getTranslation("card.noMatch"));
        matchBadge.addThemeVariants(match ? BadgeVariant.SUCCESS : BadgeVariant.WARNING);
        badgeBar.add(matchBadge);
        badgeBar.getStyle().set("margin-left", "auto");

        Span frequency = new Span(getTranslation("card.perWeek", dancer.weekFrequencyMin(), dancer.weekFrequencyMax()));

        // buttons
        // a link (so it can be opened in a new tab), styled as a button
        RouterLink viewProfileLink = loggedIn
                ? new RouterLink(getTranslation("card.viewProfile"), DancerDetailView.class, new RouteParameters("dancerId", String.valueOf(dancer.id())))
                : new RouterLink(getTranslation("card.viewProfile"), LoginView.class);
        viewProfileLink.getElement().setAttribute("theme", "button");


        HorizontalLayout headerLine = new HorizontalLayout(nameAge, badgeBar);
        headerLine.setWidthFull();

        VerticalLayout middle = new VerticalLayout(headerLine, cityLayout, whoamiParagraph, frequency, viewProfileLink);
        middle.setPadding(false);
        middle.setSpacing(false);

        HorizontalLayout card = new HorizontalLayout(middle);
        if (loggedIn && dancer.mugshot() != null) {
            Image image = new Image(dancer.mugshot(), dancer.name());
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
