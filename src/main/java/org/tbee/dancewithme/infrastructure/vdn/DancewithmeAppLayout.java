package org.tbee.dancewithme.infrastructure.vdn;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBarVariant;
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
import org.tbee.webstack.vdn.component.icon.Icon;
import org.tbee.webstack.vdn.component.menubar.MenuBar;

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

		// Set the title, absolutely positioned on the left so the navigation can be truly centered
		String title = getTranslation(titleKey);
		titleH1.text(title)
				.style("font-size", "var(--lumo-font-size-l)")
				.style("margin", "0")
				.style("position", "absolute")
				.style("left", "var(--vaadin-padding-l)")
				.style("padding-top", "5px")
				.style("width", title.length() +"em");

		MenuBar menuBar = new MenuBar() //  https://vaadin.com/directory/component/app-layout-add-on   https://vaadin.com/docs/latest/components/menu-bar
				.id("navbar")
				.widthFull()
				.themeVariants(MenuBarVariant.LUMO_TERTIARY, MenuBarVariant.LUMO_END_ALIGNED);

		// Search
		Button searchButton = new Button(getTranslation("menu.search"), e -> getUI().ifPresent(ui -> ui.navigate(SearchView.class)));
		searchButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
		menuBar.addItem(searchButton);

		// Register
		if (!securityService.isLoggedIn()) {
			Button registerButton = new Button(getTranslation("menu.register"), e -> getUI().ifPresent(ui -> ui.navigate(RegisterView.class)));
			registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
			menuBar.addItem(registerButton);
		}

		// Languages
		MenuItem languageMenuItem = menuBar.addItem(createSubmenu(getTranslation("language"), VaadinIcon.CHEVRON_DOWN));
		languageMenuItem.setId("languageMenuItem");
		SubMenu languageSubMenu = languageMenuItem.getSubMenu();
		languageSubMenu.addItem(getTranslation("language.nl"), event -> localeService.switchLocale(Locale.of("nl")));
		languageSubMenu.addItem(getTranslation("language.en"), event -> localeService.switchLocale(Locale.ENGLISH));

		// Profile submenu or login button
		securityService.loggedInDancer().ifPresentOrElse(dancer -> {
			MenuItem accountMenuItem = menuBar.addItem(createSubmenu(dancer.email(), VaadinIcon.CHEVRON_DOWN));
			accountMenuItem.setId("accountMenuItem");
			SubMenu accountSubMenu = accountMenuItem.getSubMenu();
			accountSubMenu.addItem(getTranslation("menu.profile"), event -> getUI().ifPresent(ui -> ui.navigate(ProfileView.class)));
			accountSubMenu.addItem(getTranslation("menu.logout"), event -> securityService.logout());
		}, () -> {
			Button loginButton = new Button(getTranslation("menu.login"), e -> getUI().ifPresent(ui -> ui.navigate(LoginView.class)));
			loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			menuBar.addItem(loginButton);
		});

		// Navbar
		addToNavbar(titleH1, menuBar);
	}

	public void postConstruct() {
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

	private HorizontalLayout createSubmenu(String text, VaadinIcon suffixIcon) {
		Icon submenuIcon = new Icon(suffixIcon)
				.size("0.8em")
				.style("margin-left", "0.2em")
				.style("margin-top", "0.3em");
		HorizontalLayout horizontalLayout = new HorizontalLayout(new Span(text), submenuIcon);
		horizontalLayout.setSpacing(false);
		return horizontalLayout;
	}
}
