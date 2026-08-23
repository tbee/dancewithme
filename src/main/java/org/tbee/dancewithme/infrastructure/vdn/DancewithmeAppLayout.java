package org.tbee.dancewithme.infrastructure.vdn;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.dancewithme.infrastructure.vdn.security.SecurityService;
import org.tbee.dancewithme.infrastructure.vdn.view.LoginView;
import org.tbee.dancewithme.infrastructure.vdn.view.ProfileView;
import org.tbee.dancewithme.infrastructure.vdn.view.RegisterView;
import org.tbee.dancewithme.infrastructure.vdn.view.SearchView;
import org.tbee.webstack.vdn.component.html.H1;

import java.util.List;
import java.util.Locale;

abstract public class DancewithmeAppLayout extends AppLayout // https://vaadin.com/docs/latest/components/app-layout
implements HasDynamicTitle, AfterNavigationObserver {
	private static final Logger LOG = LoggerFactory.getLogger(DancewithmeAppLayout.class);

	private final String titleKey;
	private final SecurityService securityService;
	private final LocaleService localeService;

	private final H1 titleH1 = new H1();

	public DancewithmeAppLayout(String titleKey, SecurityService securityService, LocaleService localeService) {
		this.titleKey = titleKey;
		this.securityService = securityService;
		this.localeService = localeService;
	}

	public void postConstruct() {
		// Set the title, absolutely positioned on the left so the navigation can be truly centered
		titleH1.text(getTranslation(titleKey))
				.style("font-size", "var(--lumo-font-size-l)")
				.style("margin", "0")
				.style("position", "absolute")
				.style("left", "var(--vaadin-padding-l)");

		// Navigation is in the middle of the navbar; no drawer needed
		Button searchButton = new Button(getTranslation("menu.search"), e -> getUI().ifPresent(ui -> ui.navigate(SearchView.class)));
		searchButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		HorizontalLayout navigation = new HorizontalLayout(searchButton);
		if (securityService.isLoggedIn()) {
			Button profileButton = new Button(getTranslation("menu.profile"), e -> getUI().ifPresent(ui -> ui.navigate(ProfileView.class)));
			profileButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			navigation.add(profileButton);
		}
		else {
			Button registerButton = new Button(getTranslation("menu.register"), e -> getUI().ifPresent(ui -> ui.navigate(RegisterView.class)));
			registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			navigation.add(registerButton);
		}
		navigation.getStyle().set("flex", "1");
		navigation.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
		navigation.setAlignItems(FlexComponent.Alignment.CENTER);

		// Right side of the navbar: language switcher, username, login/logout
		Button nlButton = new Button(getTranslation("language.nl"), e -> localeService.switchLocale(Locale.of("nl")));
		Button enButton = new Button(getTranslation("language.en"), e -> localeService.switchLocale(Locale.ENGLISH));
		nlButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		enButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);

		HorizontalLayout rightSide = new HorizontalLayout(nlButton, enButton);
		rightSide.setAlignItems(FlexComponent.Alignment.CENTER);

		securityService.loggedInDancer().ifPresentOrElse(dancer -> {
			Span username = new Span(dancer.name());
			Button logoutButton = new Button(getTranslation("menu.logout"), e -> securityService.logout());
			logoutButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
			rightSide.add(username, logoutButton);
		}, () -> {
			Button loginButton = new Button(getTranslation("menu.login"), e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
			loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			rightSide.add(loginButton);
		});

		// Navbar
		addToNavbar(titleH1, navigation, rightSide);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		titleH1.text(getTranslation(titleKey));
	}

	@Override
	public String getPageTitle() {
		return "Dance With Me";
	}

	public static void showException(Exception e) {
		LOG.error(e.getMessage(), e);
		showErrorNotification(e.getMessage());
	}

	public static void showErrorNotification(String message) {
		Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
	}

	public static void showErrorNotifications(List<String> messages) {
		VerticalLayout content = new VerticalLayout();
		content.setPadding(false);
		content.setSpacing(false);
		messages.forEach(message -> content.add(new Span(message)));
		Notification notification = new Notification(content);
		notification.setDuration(5000);
		notification.setPosition(Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
		notification.open();
	}

	public static void showSuccessNotification(String message) {
		Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	}
}
