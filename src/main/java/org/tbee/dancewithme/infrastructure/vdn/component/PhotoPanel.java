package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.tbee.webstack.vdn.component.ConfirmationDialog;

public class PhotoPanel extends HorizontalLayout {

    public PhotoPanel(byte[] bytes) {
        this(bytes, null);
    }

    public PhotoPanel(byte[] bytes, Runnable deleteCallback) {

        Image image = new Image(bytes, "photo");
        image.setWidth("150px");
        image.setHeight("150px");
        image.getStyle()
                .set("object-fit", "cover") // https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/object-fit
                .set("border-radius", "var(--lumo-border-radius-m)");
        add(image);
        image.addClickListener(e -> showPopup(bytes, deleteCallback));

        setSpacing(false);
        setPadding(false);
        setMargin(false);
    }

    private void showPopup(byte[] bytes, Runnable deleteCallback) {
        Image image = new Image(bytes, "zoom");
        image.getStyle()
                .set("object-fit", "contain") // https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/object-fit
                .set("border-radius", "var(--lumo-border-radius-m)");
        image.setSizeFull();

        ConfirmationDialog confirmationDialog = ConfirmationDialog.confirm("", image)
                .sizeFull()
                .maxHeight(90, Unit.PERCENTAGE)
                .maxWidth(90, Unit.PERCENTAGE);
        if (deleteCallback != null) {
            confirmationDialog
                    .rejectable()
                    .rejectText(getTranslation("form.delete"))
                    .onReject(deleteCallback);
        }
        confirmationDialog.show();
    }

}
