package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.combobox.ComboBox;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;

public class DancestyleComboBox extends ComboBox<Dancestyle> {

    public DancestyleComboBox(DancestyleRepository dancestyleRepository) {
        setItems(dancestyleRepository.findAll());
        setItemLabelGenerator(Dancestyle::name);
        setPlaceholder(getTranslation("search.dancestyle.placeholder"));
    }

    public DancestyleComboBox withValue(Dancestyle v) {
        setValue(v);
        return this;
    }
}
