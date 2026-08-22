package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.select.Select;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.repository.RoleRepository;

public class RoleSelect extends Select<Role> {

    public RoleSelect(RoleRepository roleRepository) {
        setItems(roleRepository.findAll());
        setItemLabelGenerator(role -> getTranslation(role.translationKey()));
        setWidth("100px");
    }

    public RoleSelect withValue(Role v) {
        setValue(v);
        return this;
    }
}
