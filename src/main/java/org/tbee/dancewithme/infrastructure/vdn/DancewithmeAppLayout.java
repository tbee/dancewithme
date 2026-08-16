package org.tbee.dancewithme.infrastructure.vdn;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tbee.webstack.vdn.component.html.H1;

abstract public class DancewithmeAppLayout extends AppLayout // https://vaadin.com/docs/latest/components/app-layout
implements HasDynamicTitle, AfterNavigationObserver {
	private static final Logger LOG = LoggerFactory.getLogger(DancewithmeAppLayout.class);

	private final String title;

	private final H1 titleH1 = new H1();

	public DancewithmeAppLayout(String title) {
		this.title = title;
	}

	public void postConstruct() {
		// The drawer toggle icon
		DrawerToggle drawerToggle = new DrawerToggle();
		drawerToggle.setId("drawerToggle");

		// Set the title
		titleH1.text(title)
				.style("font-size", "var(--lumo-font-size-l)")
				.style("margin", "0");

		// Navbar
		addToNavbar(drawerToggle, titleH1);
	}

	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		titleH1.text(title);
	}

	@Override
	public String getPageTitle() {
		return "Dancewithme";
	}

	public static void showException(Exception e) {
		LOG.error(e.getMessage(), e);
		showErrorNotification(e.getMessage());
	}

	private static void showErrorNotification(String message) {
		Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
	}

	public static void showSuccessNotification(String message) {
		Notification notification = Notification.show(message, 5000, Notification.Position.BOTTOM_CENTER);
		notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
	}
}
