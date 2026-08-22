package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jspecify.annotations.NonNull;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;

import java.util.function.Consumer;

public class SearchingForDancestyleRow extends HorizontalLayout {
    public final DancestyleComboBox styleComboBox;
    public final RoleSelect roleSelect;
    public final SearchCriteriaSexComboBox searchCriteriaSexComboBox;
    public final SkilllevelComboBox skilllevelMinComboBox;
    public final SkilllevelComboBox skilllevelMaxComboBox;
    public final Button removeButton;

    public SearchingForDancestyleRow(DancestyleRepository dancestyleRepository, RoleRepository roleRepository, SkilllevelRepository skilllevelRepository, Consumer<SearchingForDancestyleRow> removeButtonConsumer) {
        styleComboBox = new DancestyleComboBox(dancestyleRepository);
        roleSelect = new RoleSelect(roleRepository);
        searchCriteriaSexComboBox = new SearchCriteriaSexComboBox();
        skilllevelMinComboBox = new SkilllevelComboBox(skilllevelRepository);
        skilllevelMaxComboBox = new SkilllevelComboBox(skilllevelRepository);
        removeButton = removeButton(removeButtonConsumer);

        noPaddingHorizontalLayout();

        add(    styleComboBox,
                //new NativeLabel(getTranslation("search.role")),
                searchCriteriaSexComboBox,
                roleSelect,
                new NativeLabel(getTranslation("search.skillFrom")),
                skilllevelMinComboBox,
                new NativeLabel(getTranslation("search.skillTo")),
                skilllevelMaxComboBox,
                removeButton);
        setFlexGrow(1, skilllevelMinComboBox);
        setFlexGrow(1, skilllevelMaxComboBox);
    }

    private void noPaddingHorizontalLayout() {
        setPadding(false);
        setMargin(false);
        setAlignItems(FlexComponent.Alignment.CENTER);
        setWidthFull();
        // the fields together can be wider than the card; allow them to wrap to a second line
        getStyle().set("flex-wrap", "wrap");
        getStyle().set("row-gap", "var(--lumo-space-s)");
    }

    private @NonNull Button removeButton(Consumer<SearchingForDancestyleRow> consumer) {
        Button removeButton = new Button(VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(t -> consumer.accept(this));
        return removeButton;
    }
}
