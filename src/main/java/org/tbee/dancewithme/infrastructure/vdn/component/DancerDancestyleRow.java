package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jspecify.annotations.NonNull;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;

import java.util.function.Consumer;

public class DancerDancestyleRow extends SearchingForDancestyleRow {

    public DancerDancestyleRow(DancestyleRepository dancestyleRepository, RoleRepository roleRepository, SkilllevelRepository skilllevelRepository, Consumer<DancerDancestyleRow> removeButtonConsumer) {
        super(dancestyleRepository, roleRepository, skilllevelRepository, (r) -> removeButtonConsumer.accept((DancerDancestyleRow) r));

        removeAll();
        add(    styleComboBox,
                new NativeLabel(getTranslation("form.role")),
                roleSelect,
                new NativeLabel(getTranslation("form.skill")),
                skilllevelMinComboBox,
                removeButton);
    }
}
