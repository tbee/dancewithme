package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.combobox.ComboBox;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;

public class SearchCriteriaSexComboBox extends ComboBox<SearchCriteriaSex> {

    public SearchCriteriaSexComboBox() {
        setItems(SearchCriteriaSex.values());
        setItemLabelGenerator(sexOption -> getTranslation(sexOption.translationKey()));
        setWidth("150px");
    }

    public SearchCriteriaSexComboBox withValue(SearchCriteriaSex v) {
        setValue(v);
        return this;
    }
}
