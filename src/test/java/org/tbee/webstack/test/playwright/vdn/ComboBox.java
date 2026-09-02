package org.tbee.webstack.test.playwright.vdn;

import com.microsoft.playwright.Locator;

public class ComboBox {

    private final Locator locator;

    public ComboBox(Locator locator) {
        this.locator = locator;
    }

    public ComboBox select(String text) {
        return select(text, true);
    }

    public ComboBox select(String text, boolean exactMatch) {
        locator.locator("#toggleButton").click();
        locator.locator("vaadin-combo-box-item")
                .getByText(text, new Locator.GetByTextOptions().setExact(exactMatch))
                .click();
        // TODO: scrolling
        return this;
    }
}
