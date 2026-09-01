package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.html.NativeLabel;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;

import java.util.function.Consumer;

public class CandoRow extends SearchingForRow {
    public static final String SKILLLEVEL_COMBO_BOX_ID = SKILLLEVEL_MIN_COMBO_BOX_ID;

    public CandoRow(DancestyleRepository dancestyleRepository, Consumer<CandoRow> removeButtonConsumer) {
        super(dancestyleRepository, (r) -> removeButtonConsumer.accept((CandoRow) r));

        removeAll();
        add(    styleComboBox,
                new NativeLabel(getTranslation("form.role")),
                roleSelect,
                new NativeLabel(getTranslation("form.skill")),
                skilllevelMinComboBox,
                removeButton);
    }
}
