package org.tbee.webstack.vdn.component;

import com.vaadin.flow.component.Unit;
import org.tbee.webstack.vdn.component.html.Image;
import org.tbee.webstack.vdn.component.orderedlayout.HorizontalLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageGallery extends HorizontalLayout {

    List<PhotoPanel> photoPanels = new ArrayList<>();

    public ImageGallery() {
        wrap();
    }

    public ImageGallery addImage(byte[] bytes, String contentType) {
        return addImage(bytes, contentType, null);
    }

    public ImageGallery addImage(byte[] bytes, String contentType, Runnable deleteCallback) {
        PhotoPanel photoPanel = new PhotoPanel(bytes, contentType, deleteCallback);
        photoPanels.add(photoPanel);
        add(photoPanel);
        return this;
    }

    class PhotoPanel extends HorizontalLayout {
        private final byte[] bytes;
        private final String contentType;
        private final Runnable deleteCallback;

        public PhotoPanel(byte[] bytes, String contentType, Runnable deleteCallback) {
            this.bytes = bytes;
            this.contentType = contentType;
            this.deleteCallback = deleteCallback;

            spacing(false);
            padding(false);
            margin(false);

            add(new Image(bytes, "photo")
                    .fit(Image.Fit.COVER)
                    .size(150, 150, Unit.PIXELS)
                    .onClick(e -> showPopup()));
        }

        private void showPopup() {

            // Prepare popup dialog
            ConfirmationDialog confirmationDialog = ConfirmationDialog.confirm("", image(bytes, contentType))
                    .closeIsCancel()
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
            confirmationDialog.getHeader().addComponentAsFirst(nextButton);
            confirmationDialog.getHeader().addComponentAsFirst(previousButton);

            // Show remove button is necessary
            if (deleteCallback != null) {
                confirmationDialog
                        .rejectable()
                        .rejectText(getTranslation("form.delete"))
                        .onReject(deleteCallback);
            }

            confirmationDialog.show();
        }

        private Image image(byte[] bytes, String contentType) {
            return new Image(bytes, "zoom", contentType)
                    .fit(Image.Fit.CONTAIN)
                    .sizeFull();
        }

        private void replaceImage(int newIndex, ConfirmationDialog confirmationDialog) {
            title(newIndex, confirmationDialog);
            PhotoPanel nextPhotoPanel = photoPanels.get(newIndex);
            Image newImage = image(nextPhotoPanel.bytes, nextPhotoPanel.contentType);

            confirmationDialog.removeAll();
            confirmationDialog.add(newImage);
        }

        private void title(int newIndex, ConfirmationDialog confirmationDialog) {
            confirmationDialog.setHeaderTitle((newIndex + 1) + " / " + photoPanels.size());
        }
    }
}
