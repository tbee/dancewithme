package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.tbee.dancewithme.application.DancerService;
import org.tbee.dancewithme.application.SearchService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.domain.DancerSearchingFor;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.component.SearchResultCard;
import org.tbee.dancewithme.infrastructure.vdn.component.SearchingForRow;
import org.tbee.dancewithme.infrastructure.vdn.component.SkilllevelComboBox;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.util.ArrayList;
import java.util.List;

@Route("")
@AnonymousAllowed
public class SearchView extends DancewithmeAppLayout {

    private static final int PAGE_SIZE = 3;

    private final SecurityService securityService;
    private final SearchService searchService;

    private final VerticalLayout styleRowsLayout = new VerticalLayout();
    private final List<SearchingForRow> styleRows = new ArrayList<>();
    private final IntegerField weekFrequencyMinField = new IntegerField();
    private final IntegerField weekFrequencyMaxField = new IntegerField();
    private final IntegerField distanceMaxField = new IntegerField();

    private final DancestyleRepository dancestyleRepository;

    private final VerticalLayout resultsLayout = new VerticalLayout();
    private final HorizontalLayout pagingLayout = new HorizontalLayout();
    private final Span pageLabel = new Span();

    private List<SearchService.SearchResult> results = List.of();
    private int page = 0;

    public SearchView(SecurityService securityService, LocaleService localeService, SearchService searchService,
                      DancerService dancerService, DancestyleRepository dancestyleRepository) {
        super("search.title", securityService, localeService);
        this.securityService = securityService;
        this.searchService = searchService;
        this.dancestyleRepository = dancestyleRepository;

        boolean loggedIn = securityService.isLoggedIn();

        // == search form ==
        // style rows: dancestyle, role, skill range (same structure as the profile's "searching for")
        // prefill with what the logged in dancer is searching for (can still be fiddled with)
        Dancer currentDancer = securityService.loggedInDancer().orElse(null);
        List<DancerSearchingFor> searchingFor = (currentDancer == null ? List.of() : currentDancer.searchingFor());
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

        weekFrequencyMinField.setId("weekFrequencyMinField");
        weekFrequencyMinField.setValue(currentDancer == null ? 0 : currentDancer.weekFrequencyMin());
        weekFrequencyMinField.setPlaceholder(getTranslation("search.age.min"));
        weekFrequencyMinField.setMin(0);
        weekFrequencyMinField.setMax(7);
        weekFrequencyMaxField.setId("weekFrequencyMaxField");
        weekFrequencyMaxField.setValue(currentDancer == null ? 7 : currentDancer.weekFrequencyMax());
        weekFrequencyMaxField.setPlaceholder(getTranslation("search.age.max"));
        weekFrequencyMaxField.setMin(0);
        weekFrequencyMaxField.setMax(7);
        distanceMaxField.setValue(currentDancer == null ? 0 : currentDancer.distanceMax());
        distanceMaxField.setMin(0);
        distanceMaxField.setVisible(loggedIn); // distance search is only available for logged in users

        FormLayout formLayout = new FormLayout();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));
        formLayout.addFormItem(styleRowsWithButton, getTranslation("search.dancestyle"));
        formLayout.addFormItem(new HorizontalLayout(weekFrequencyMinField, weekFrequencyMaxField), getTranslation("search.weekFrequency"));
        if (loggedIn) {
            formLayout.addFormItem(distanceMaxField, getTranslation("search.distance"));
        }

        Button searchButton = new Button(getTranslation("search.button"), e -> search());
        searchButton.setId("searchButton");
        searchButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        VerticalLayout searchContent = new VerticalLayout(formLayout, searchButton);
        searchContent.setWidthFull();
        searchContent.setMaxWidth("1200px");
        Card searchCard = new Card();
//        searchCard.setWidthFull();
//        searchCard.setMaxWidth("1200px");
        searchCard.add(searchContent);

        VerticalLayout resultContent = new VerticalLayout(resultsLayout, pagingLayout);
//        resultContent.setWidthFull();
        resultContent.setMaxWidth("1200px");
        Card resultCard = new Card();
//        resultCard.setWidthFull();
//        resultCard.setMaxWidth("1200px");
        resultCard.add(resultContent);

        VerticalLayout content = new VerticalLayout(searchCard, resultContent);
//        content.setMaxWidth("1200px");
        setContent(content);
    }

    private void addStyleRow(Dancestyle dancestyle, Role role, SearchCriteriaSex sex, Integer skilllevelMin, Integer skilllevelMax) {
        SearchingForRow row = new SearchingForRow(dancestyleRepository, r -> {
            styleRows.remove(r);
            styleRowsLayout.remove(r);
        });
        row.style(dancestyle);
        row.sex(sex);
        row.role(role);
        row.skilllevelMin(skilllevelMin);
        row.skilllevelMax(skilllevelMax);

        styleRows.add(row);
        styleRowsLayout.add(row);
    }

    private void search() {
        SearchService.SearchParameters searchParameters = new SearchService.SearchParameters(){

            @Override
            public int weekFrequencyMin() {
                return ifNull(weekFrequencyMinField.getValue(), 0);
            }

            @Override
            public int weekFrequencyMax() {
                return ifNull(weekFrequencyMaxField.getValue(), 7);
            }

            @Override
            public int distanceMax() {
                return ifNull(distanceMaxField.getValue(), 1000);
            }

            @Override
            public List<? extends SearchService.SearchParametersStyles> searchingFor() {
                return styleRows.stream()
                        .filter(row -> row.style() != null)
                        .map(row -> new SearchService.SearchParametersStyles(){

                            @Override
                            public Dancestyle dancestyle() {
                                return row.style();
                            }

                            @Override
                            public Role role() {
                                return row.role();
                            }

                            @Override
                            public SearchCriteriaSex sex() {
                                return row.sex();
                            }

                            @Override
                            public int skilllevelMin() {
                                return ifNull(row.skilllevelMin(), SkilllevelComboBox.MIN);
                            }

                            @Override
                            public int skilllevelMax() {
                                return ifNull(row.skilllevelMax(), SkilllevelComboBox.MAX);
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

    private int ifNull(Integer v, int d) {
        return (v == null ? d : v);
    }

    private void renderResults() {
        Dancer loggedInDancer = securityService.loggedInDancer().orElse(null);
        resultsLayout.removeAll();
        pagingLayout.removeAll();

        if (results.isEmpty()) {
            resultsLayout.add(new Paragraph(getTranslation("search.noResults")));
            return;
        }
        if (!securityService.isLoggedIn()) {
            resultsLayout.add(new Paragraph(getTranslation("search.loginToSeeDetails")));
        }

        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, results.size());
        results.subList(from, to).forEach(result -> resultsLayout.add(new SearchResultCard(result, loggedInDancer, searchService)));

        // paging
        Button previousButton = new Button(getTranslation("paging.previous"), e -> { page--; renderResults(); });
        previousButton.setEnabled(page > 0);
        Button nextButton = new Button(getTranslation("paging.next"), e -> { page++; renderResults(); });
        nextButton.setEnabled(to < results.size());
        pageLabel.setText((page + 1) + " / " + ((results.size() + PAGE_SIZE - 1) / PAGE_SIZE));
        pagingLayout.add(previousButton, pageLabel, nextButton);
        pagingLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
    }
}
