package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.jspecify.annotations.NonNull;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;

public class SkilllevelComboBox extends ComboBox<Skilllevel> {

    public SkilllevelComboBox(SkilllevelRepository skilllevelRepository) {
        setItems(skilllevelRepository.findAllByOrderByLevelAsc());
        setItemLabelGenerator(this::label);

        // show the description as a tooltip when hovering over the unfolded options
        setRenderer(new ComponentRenderer<>(sl -> {
            Span name = new Span(label(sl));
            Tooltip tooltip = Tooltip.forComponent(name)
                    .withText(getTranslation("skilllevel." + sl.code() + ".description"))
                    .withHoverDelay(300);
            tooltip.setPosition(Tooltip.TooltipPosition.END);
            return name;
        }));
    }

    private @NonNull String label(Skilllevel sl) {
        return sl.level() + " - " + getTranslation("skilllevel." + sl.code());
    }

    public SkilllevelComboBox withValue(Skilllevel v) {
        setValue(v);
        return this;
    }
}
