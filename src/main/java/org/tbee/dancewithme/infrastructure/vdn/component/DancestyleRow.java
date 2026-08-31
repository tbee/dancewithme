package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.html.NativeLabel;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;

import java.util.function.Consumer;

public class DancestyleRow extends SearchingForRow {

    public DancestyleRow(DancestyleRepository dancestyleRepository, Consumer<DancestyleRow> removeButtonConsumer) {
        super(dancestyleRepository, (r) -> removeButtonConsumer.accept((DancestyleRow) r));

        removeAll();
        add(    styleComboBox,
                new NativeLabel(getTranslation("form.role")),
                roleSelect,
                new NativeLabel(getTranslation("form.skill")),
                skilllevelMinComboBox,
                removeButton);
    }
}
