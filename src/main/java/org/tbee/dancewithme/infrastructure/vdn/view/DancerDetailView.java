package org.tbee.dancewithme.infrastructure.vdn.view;

import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
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
        H2 nameH2 = new H2(dancer.name());

        String cityText = dancer.city() != null ? dancer.city().name() : "";
        HorizontalLayout cityLayout = new HorizontalLayout(VaadinIcon.MAP_MARKER.create(), new Span(cityText));
        cityLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);

        // dancestyles including role and skill, one per line
        VerticalLayout stylesLayout = new VerticalLayout();
        stylesLayout.setPadding(false);
        stylesLayout.setSpacing(false);
        dancer.dancestyles().forEach(dd -> stylesLayout.add(new Span(
                dd.dancestyle().name() + " — " + dd.role().name() + " — " + getTranslation("skilllevel." + dd.skilllevel().code()))));

        // mutual match badge: we know the dancer matches our search parameters, but do wo also match the search of the `viewed` dancer? Are we a match?
        Dancer loggedInDancer = securityService.loggedInDancer().orElseThrow();
        boolean matched = !searchService.match(dancer, List.of(loggedInDancer), dancer).isEmpty();
        HorizontalLayout nameBadges = new HorizontalLayout(nameH2);
        Badge matchBadge = matched ? new Badge(getTranslation("card.match")) : new Badge(getTranslation("card.noMatch"));
        matchBadge.addThemeVariants(matched ? BadgeVariant.SUCCESS : BadgeVariant.WARNING);
        nameBadges.add(matchBadge);
        nameBadges.setAlignItems(HorizontalLayout.Alignment.CENTER);

        VerticalLayout content = new VerticalLayout();
        content.setMaxWidth("1200px");

        // mugshot + header
        HorizontalLayout header = new HorizontalLayout();
        if (dancer.mugshot() != null) {
            Image image = new Image(dancer.mugshot(), dancer.name());
            image.setWidth("200px");
            image.setHeight("200px");
            image.getStyle().set("object-fit", "cover").set("border-radius", "var(--lumo-border-radius-m)");
            header.add(image);
        }
        header.add(new VerticalLayout(nameBadges,
                cityLayout,
                new Span(getTranslation("detail.maxDistance", dancer.distanceMax())),
                stylesLayout,
                new Span(getTranslation("detail.weekFrequency", dancer.weekFrequencyMin(), dancer.weekFrequencyMax()))
                ));
        content.add(header);

        if (dancer.whoami() != null && !dancer.whoami().isBlank()) {
            content.add(new H3(getTranslation("form.whoami")), new Markdown(dancer.whoami()));
        }
        if (dancer.whatdoiwant() != null && !dancer.whatdoiwant().isBlank()) {
            content.add(new H3(getTranslation("form.whatdoiwant")), new Markdown(dancer.whatdoiwant()));
        }

        // photo gallery
        if (!dancer.photos().isEmpty()) {
            content.add(new H3(getTranslation("form.photos")));
            HorizontalLayout gallery = new HorizontalLayout();
            dancer.photos().forEach(photo -> {
                Image image = new Image(photo.image(), dancer.name());
                image.setWidth("200px");
                image.setHeight("200px");
                image.getStyle().set("object-fit", "cover").set("border-radius", "var(--lumo-border-radius-m)");
                gallery.add(image);
            });
            gallery.getStyle().set("flex-wrap", "wrap");
            content.add(gallery);
        }

        setContent(content);
    }
}
