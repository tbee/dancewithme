package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.jspecify.annotations.NonNull;

import java.util.stream.IntStream;

public class SkilllevelComboBox extends ComboBox<Integer> {

    public static final int MIN = 1;
    public static final int MAX = 10;

    public SkilllevelComboBox() {
        setItems(IntStream.rangeClosed(MIN, MAX).boxed().toList());
        setItemLabelGenerator(this::label);

        // show the description as a tooltip when hovering over the unfolded options
        setRenderer(new ComponentRenderer<>(sl -> {
            Span name = new Span(label(sl));
            Tooltip tooltip = Tooltip.forComponent(name)
                    .withText(getTranslation("skilllevel" + sl + ".description"))
                    .withHoverDelay(300);
            tooltip.setPosition(Tooltip.TooltipPosition.END);
            return name;
        }));
    }

    private @NonNull String label(Integer sl) {
        return sl + " - " + getTranslation("skilllevel" + sl);
    }

    public SkilllevelComboBox withValue(Integer v) {
        setValue(v);
        return this;
    }
}
