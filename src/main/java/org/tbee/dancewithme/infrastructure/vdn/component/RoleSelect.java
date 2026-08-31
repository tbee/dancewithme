package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.select.Select;
import org.tbee.dancewithme.domain.valueobject.Role;

public class RoleSelect extends Select<Role> {

    public RoleSelect() {
        setItems(Role.values());
        setItemLabelGenerator(role -> getTranslation(role.translationKey()));
        setWidth("100px");
    }

    public RoleSelect withValue(Role v) {
        setValue(v);
        return this;
    }
}
