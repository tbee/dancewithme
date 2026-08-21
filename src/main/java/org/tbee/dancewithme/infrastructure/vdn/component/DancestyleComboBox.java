package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.combobox.ComboBox;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;

public class DancestyleComboBox extends ComboBox<Dancestyle> {

    public DancestyleComboBox(DancestyleRepository dancestyleRepository) {
        setItems(dancestyleRepository.findAll());
        setItemLabelGenerator(Dancestyle::name);
    }

    public DancestyleComboBox withValue(Dancestyle v) {
        setValue(v);
        return this;
    }
}
