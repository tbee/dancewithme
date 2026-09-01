package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.badge.Badge;
import com.vaadin.flow.component.badge.BadgeVariant;
import com.vaadin.flow.component.card.Card;
import com.vaadin.flow.component.card.CardVariant;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouterLink;
import org.tbee.dancewithme.application.SearchService;
import org.tbee.dancewithme.domain.Dancer;
import org.tbee.dancewithme.infrastructure.vdn.view.DancerDetailView;
import org.tbee.dancewithme.infrastructure.vdn.view.LoginView;

import java.util.List;

public class SearchResultCard extends Card {

    public SearchResultCard(SearchService.SearchResult searchResult, Dancer loggedInDancer, SearchService searchService) {
        Dancer dancer = searchResult.dancer();
        boolean loggedIn = (loggedInDancer != null);
        setId(this.getClass().getSimpleName() + "-" + dancer.id());

        // city + distance
        String city = dancer.city() != null ? dancer.city().name() : "";
        if (searchResult.distanceKm() != null) {
            city += ", " + getTranslation("card.km", Math.round(searchResult.distanceKm()));
        }
        HorizontalLayout cityLayout = new HorizontalLayout(VaadinIcon.MAP_MARKER.create(), new Span(city));
        cityLayout.setAlignItems(HorizontalLayout.Alignment.CENTER);
        cityLayout.setSpacing(false);

        // whoami excerpt
        String whoami = dancer.whoami() == null ? "" : dancer.whoami();
        int maxlength = 500;
        Markdown whoamiMarkdown = new Markdown(whoami.length() < maxlength ? whoami : whoami.substring(0, maxlength) + "...");

        // role + style badges
        HorizontalLayout badgeBar = new HorizontalLayout();
        badgeBar.getStyle().set("margin-left", "auto");
        dancer.dancestyles().stream()
                .map(dd -> getTranslation(dd.role().translationKey()) + " " + dd.dancestyle().name())
                .distinct()
                .forEach(s -> badgeBar.add(new Badge(s)));
        // mutual match badge: does their search match us?
        if (loggedIn) {
            boolean match = !searchService.match(dancer, List.of(loggedInDancer), dancer).isEmpty();
            Badge matchBadge = match
                               ? new Badge(getTranslation("card.match"))
                               : new Badge(getTranslation("card.noMatch"));
            matchBadge.addThemeVariants(match ? BadgeVariant.SUCCESS : BadgeVariant.WARNING);
            badgeBar.add(matchBadge);
        }

        Span frequency = new Span(getTranslation("card.perWeek", dancer.weekFrequencyMin(), dancer.weekFrequencyMax()));

        // buttons
        // a link (so it can be opened in a new tab), styled as a button
        RouterLink viewProfileLink = loggedIn
                                     ? new RouterLink(getTranslation("card.viewProfile"), DancerDetailView.class, new RouteParameters("dancerId", String.valueOf(dancer.id())))
                                     : new RouterLink(getTranslation("card.viewProfile"), LoginView.class);
        viewProfileLink.getElement().setAttribute("theme", "button");

        VerticalLayout middle = new VerticalLayout(frequency, whoamiMarkdown);
        middle.setPadding(false);
        middle.setSpacing(false);

        if (loggedIn && dancer.mugshot() != null) {
            setMedia(new Image(dancer.mugshot(), dancer.name()));
        }
        else if (!loggedIn) {
            Avatar avatar = new Avatar(dancer.name());
            avatar.setSizeFull();
            avatar.setWidth("200px");
            avatar.setHeight("200px");
            setMedia(avatar);
        }
        
        setTitle(dancer.name());
        setSubtitle(cityLayout);
        add(middle);
        setWidthFull();
        setHeaderSuffix(badgeBar);
        addToFooter(viewProfileLink);
        addThemeVariants(CardVariant.HORIZONTAL, CardVariant.COVER_MEDIA);
    }
}
