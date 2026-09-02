package org.tbee.webstack.test.playwright.vdn;

import com.microsoft.playwright.Locator;

public class Select {

    private final Locator locator;

    public Select(Locator locator) {
        this.locator = locator;
    }

    public Select select(String text) {
        return select(text, true);
    }

    public Select select(String text, boolean exactMatch) {
        locator.locator("vaadin-select-value-button").click();
        locator.locator("vaadin-select-item")
                .getByText(text, new Locator.GetByTextOptions().setExact(exactMatch))
                .click();
        // TODO: scrolling
        return this;
    }
}
