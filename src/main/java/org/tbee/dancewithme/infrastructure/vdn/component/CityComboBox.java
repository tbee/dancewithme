package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.combobox.ComboBox;
import org.tbee.dancewithme.domain.City;
import org.tbee.dancewithme.domain.repository.CityRepository;
import org.tbee.dancewithme.domain.valueobject.Sex;

public class CityComboBox extends ComboBox<City> {

    public CityComboBox(CityRepository cityRepository) {
        setItems(cityRepository.findAllByOrderByNameAsc());
        setItemLabelGenerator(City::name);
    }

    public CityComboBox withValue(City v) {
        setValue(v);
        return this;
    }
}
