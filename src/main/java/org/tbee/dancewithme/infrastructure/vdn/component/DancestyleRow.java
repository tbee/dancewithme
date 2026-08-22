package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.html.NativeLabel;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;

import java.util.function.Consumer;

public class DancestyleRow extends SearchingForRow {

    public DancestyleRow(DancestyleRepository dancestyleRepository, RoleRepository roleRepository, SkilllevelRepository skilllevelRepository, Consumer<DancestyleRow> removeButtonConsumer) {
        super(dancestyleRepository, roleRepository, skilllevelRepository, (r) -> removeButtonConsumer.accept((DancestyleRow) r));

        removeAll();
        add(    styleComboBox,
                new NativeLabel(getTranslation("form.role")),
                roleSelect,
                new NativeLabel(getTranslation("form.skill")),
                skilllevelMinComboBox,
                removeButton);
    }
}
