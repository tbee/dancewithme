package org.tbee.webstack.vdn.component;

import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageGallery extends HorizontalLayout {

    List<PhotoPanel> photoPanels = new ArrayList<>();

    public ImageGallery() {
        getStyle().set("flex-wrap", "wrap");
    }

    public ImageGallery addImage(byte[] bytes) {
        return addImage(bytes, null);
    }

    public ImageGallery addImage(byte[] bytes, Runnable deleteCallback) {
        PhotoPanel photoPanel = new PhotoPanel(bytes, deleteCallback);
        photoPanels.add(photoPanel);
        add(photoPanel);
        return this;
    }

    class PhotoPanel extends HorizontalLayout {
        private final byte[] bytes;
        private final Runnable deleteCallback;

        public PhotoPanel(byte[] bytes, Runnable deleteCallback) {
            this.bytes = bytes;
            this.deleteCallback = deleteCallback;

            setSpacing(false);
            setPadding(false);
            setMargin(false);

            Image image = new Image(bytes, "photo");
            image.setWidth("150px");
            image.setHeight("150px");
            image.getStyle()
                    .set("object-fit", "cover") // https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/object-fit
                    .set("border-radius", "var(--lumo-border-radius-m)");
            add(image);
            image.addClickListener(e -> showPopup());
        }

        private void showPopup() {

            // Prepare popup dialog
            ConfirmationDialog confirmationDialog = ConfirmationDialog.confirm("", image(bytes))
                    .sizeFull()
                    .maxHeight(90, Unit.PERCENTAGE)
                    .maxWidth(90, Unit.PERCENTAGE);

            // Add previous and next buttons
            AtomicInteger index = new AtomicInteger(photoPanels.indexOf(this));
            title(index.get(), confirmationDialog);
            Button previousButton = new Button("<").onClick(e -> {
                if (index.get() <= 0) {
                    return;
                }
                replaceImage(index.decrementAndGet(), confirmationDialog);
            });
            Button nextButton = new Button(">").onClick(e -> {
                if (index.get() >= photoPanels.size() - 1) {
                    return;
                }
                replaceImage(index.incrementAndGet(), confirmationDialog);
            });
            confirmationDialog.getHeader().add(previousButton, nextButton);

            // Show remove button is necessary
            if (deleteCallback != null) {
                confirmationDialog
                        .rejectable()
                        .rejectText(getTranslation("form.delete"))
                        .onReject(deleteCallback);
            }

            confirmationDialog.show();
        }

        private Image image(byte[] bytes) {
            Image image = new Image(bytes, "zoom");
            image.getStyle()
                    .set("object-fit", "contain") // https://developer.mozilla.org/en-US/docs/Web/CSS/Reference/Properties/object-fit
                    .set("border-radius", "var(--lumo-border-radius-m)");
            image.setSizeFull();
            return image;
        }

        private void replaceImage(int newIndex, ConfirmationDialog confirmationDialog) {
            title(newIndex, confirmationDialog);
            PhotoPanel nextPhotoPanel = photoPanels.get(newIndex);
            Image newImage = image(nextPhotoPanel.bytes);

            confirmationDialog.removeAll();
            confirmationDialog.add(newImage);
        }

        private void title(int newIndex, ConfirmationDialog confirmationDialog) {
            confirmationDialog.setHeaderTitle((newIndex + 1) + " / " + photoPanels.size());
        }
    }
}
