package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class ImageGallery extends HorizontalLayout {

    public ImageGallery() {
        getStyle().set("flex-wrap", "wrap");
    }

    public ImageGallery addImage(byte[] bytes) {
        return addImage(bytes, null);
    }

    public ImageGallery addImage(byte[] bytes, Runnable deleteCallback) {
        add(new PhotoPanel(bytes, deleteCallback));
        return this;
    }
}
