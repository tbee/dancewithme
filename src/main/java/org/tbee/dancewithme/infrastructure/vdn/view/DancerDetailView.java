package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.copilot.shaded.checkerframework.checker.units.qual.C;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import org.tbee.dancewithme.application.DancerService;
import org.tbee.dancewithme.application.SearchService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.infrastructure.vdn.DancewithmeAppLayout;
import org.tbee.dancewithme.infrastructure.vdn.LocaleService;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

import java.time.Year;
import java.util.List;

@Route("dancer/:dancerId")
@PermitAll
public class DancerDetailView extends DancewithmeAppLayout implements BeforeEnterObserver {

    private final DancerService dancerService;
    private final SecurityService securityService;
    private final SearchService searchService;

    public DancerDetailView(SecurityService securityService, LocaleService localeService, DancerService dancerService, SearchService searchService) {
        super("detail.title", securityService, localeService);
        this.dancerService = dancerService;
        this.securityService = securityService;
        this.searchService = searchService;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        long dancerId = event.getRouteParameters().getLong("dancerId").orElseThrow();
        Dancer dancer;
        try {
            dancer = dancerService.loadWithDetails(dancerId);
        }
        catch (Exception e) {
            setContent(new Paragraph(getTranslation("detail.notFound")));
            return;
        }
        render(dancer);
    }

    private void render(Dancer dancer) {

        Card dancerCard = dancerCard(dancer);

        VerticalLayout content = new VerticalLayout(dancerCard);
        content.setMaxWidth("1200px");

        if (dancer.whoami() != null && !dancer.whoami().isBlank()) {
            Card card = new Card();
            card.setWidthFull();
            card.setTitle(getTranslation("form.whoami"));
            card.add(new Markdown(dancer.whoami()));
            content.add(card);
        }
        if (dancer.whatdoiwant() != null && !dancer.whatdoiwant().isBlank()) {
            Card card = new Card();
            card.setWidthFull();
            card.setTitle(getTranslation("form.whatdoiwant"));
            card.add(new Markdown(dancer.whatdoiwant()));
            content.add(card);
        }

        // photo gallery
        if (!dancer.photos().isEmpty()) {
            Card card = new Card();
            card.setWidthFull();
            card.setTitle(getTranslation("form.photos"));
            content.add(card);
            HorizontalLayout gallery = new HorizontalLayout();
            dancer.photos().forEach(photo -> {
                Image image = new Image(photo.image(), dancer.name());
                image.setWidth("200px");
                image.setHeight("200px");
                image.getStyle().set("object-fit", "cover").set("border-radius", "var(--lumo-border-radius-m)");
                gallery.add(image);
            });
            gallery.getStyle().set("flex-wrap", "wrap");
            card.add(gallery);
        }

        setContent(content);
    }

    private Card dancerCard(Dancer dancer) {

        String cityText = dancer.city() != null ? dancer.city().name() : "";
        Span cityLayout = new Span(VaadinIcon.MAP_MARKER.create(), new Span(cityText));

        // dancestyles including role and skill, one per line
        VerticalLayout stylesLayout = new VerticalLayout();
        stylesLayout.setPadding(false);
        stylesLayout.setSpacing(false);
        stylesLayout.setMargin(false);
        dancer.dancestyles().forEach(dd -> stylesLayout.add(new Span(
                dd.dancestyle().name() + " — " + dd.role().name() + " — " + getTranslation("skilllevel." + dd.skilllevel().code()))));

        // mutual match badge: we know the dancer matches our search parameters, but do wo also match the search of the `viewed` dancer? Are we a match?
        Dancer loggedInDancer = securityService.loggedInDancer().orElseThrow();
        boolean matched = !searchService.match(dancer, List.of(loggedInDancer), dancer).isEmpty();
        Badge matchBadge = matched ? new Badge(getTranslation("card.match")) : new Badge(getTranslation("card.noMatch"));
        matchBadge.addThemeVariants(matched ? BadgeVariant.SUCCESS : BadgeVariant.WARNING);

        Card card = new Card();
        card.setWidthFull();
        if (dancer.mugshot() != null) {
            card.setMedia(new Image(dancer.mugshot(), dancer.name()));
        }
        card.addThemeVariants(CardVariant.HORIZONTAL, CardVariant.COVER_MEDIA);
        card.setTitle(dancer.name());
        card.setSubtitle(cityLayout);
        card.setHeaderSuffix(matchBadge);
        VerticalLayout verticalLayout = new VerticalLayout(
                new Span(getTranslation("detail.maxDistance", dancer.distanceMax())), stylesLayout,
                new Span(getTranslation("detail.weekFrequency", dancer.weekFrequencyMin(), dancer.weekFrequencyMax())));
        verticalLayout.setMargin(false);
        verticalLayout.setPadding(false);
        card.add(verticalLayout);
        return card;
    }
}
