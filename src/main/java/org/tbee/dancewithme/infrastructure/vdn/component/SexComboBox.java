package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.combobox.ComboBox;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.dancewithme.domain.valueobject.Sex;

public class SexComboBox extends ComboBox<Sex> {

    public SexComboBox() {
        setItems(Sex.values());
        setItemLabelGenerator(sex -> getTranslation("sex." + sex.name().toLowerCase()));
        setWidth("150px");
    }

    public SexComboBox withValue(Sex v) {
        setValue(v);
        return this;
    }
}
