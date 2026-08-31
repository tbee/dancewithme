package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
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
import org.tbee.webstack.vdn.component.ImageGallery;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;

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

        VerticalLayout content = new VerticalLayout();
        content.setMaxWidth("1200px");

        content.add(dancerCard(dancer));

        if (dancer.whoami() != null && !dancer.whoami().isBlank()) {
            content.add(card(getTranslation("form.whoami"), new Markdown(dancer.whoami())));
        }

        if (dancer.whatdoiwant() != null && !dancer.whatdoiwant().isBlank()) {
            content.add(card(getTranslation("form.whatdoiwant"), new Markdown(dancer.whatdoiwant())));
        }

        if (!dancer.photos().isEmpty()) {
            ImageGallery imageGallery = new ImageGallery();
            dancer.photos().forEach(photo -> {
                imageGallery.addImage(photo.image(), photo.contentType());
            });
            content.add(card(getTranslation("form.photos"), imageGallery));
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
                dd.dancestyle().name() + " — " + getTranslation(dd.role().translationKey()) + " — " + dd.skilllevel() + " - " + getTranslation("skilllevel" + dd.skilllevel()))));

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
        card.setTitle(dancer.name() + (dancer.sex() == null ? "" : " (" + getTranslation(dancer.sex().translationKeyNoun()) + ")"));
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

    private Card card(String title, Component contents) {
        Card card = new Card();
        card.setWidthFull();
        card.setTitle(title);
        card.add(contents);
        return card;
    }
}
