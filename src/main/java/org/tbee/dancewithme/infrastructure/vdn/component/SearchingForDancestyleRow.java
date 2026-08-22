package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.jspecify.annotations.NonNull;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.Role;
import org.tbee.dancewithme.domain.Skilllevel;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.repository.RoleRepository;
import org.tbee.dancewithme.domain.repository.SkilllevelRepository;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;

import java.util.function.Consumer;

public class SearchingForDancestyleRow extends HorizontalLayout {
    protected final DancestyleComboBox styleComboBox;
    protected final RoleSelect roleSelect;
    protected final SearchCriteriaSexComboBox searchCriteriaSexComboBox;
    protected final SkilllevelComboBox skilllevelMinComboBox;
    protected final SkilllevelComboBox skilllevelMaxComboBox;
    protected final Button removeButton;

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

    public Dancestyle style() {
        return styleComboBox.getValue();
    }
    public SearchingForDancestyleRow style(Dancestyle v) {
        styleComboBox.setValue(v);
        return this;
    }

    public Role role() {
        return roleSelect.getValue();
    }
    public SearchingForDancestyleRow role(Role v) {
        roleSelect.setValue(v);
        return this;
    }

    public SearchCriteriaSex sex() {
        return searchCriteriaSexComboBox.getValue();
    }
    public SearchingForDancestyleRow sex(SearchCriteriaSex v) {
        searchCriteriaSexComboBox.setValue(v);
        return this;
    }

    public Skilllevel skilllevelMin() {
        return skilllevelMinComboBox.getValue();
    }
    public SearchingForDancestyleRow skilllevelMin(Skilllevel v) {
        skilllevelMinComboBox.setValue(v);
        return this;
    }

    public Skilllevel skilllevelMax() {
        return skilllevelMaxComboBox.getValue();
    }
    public SearchingForDancestyleRow skilllevelMax(Skilllevel v) {
        skilllevelMaxComboBox.setValue(v);
        return this;
    }
}
