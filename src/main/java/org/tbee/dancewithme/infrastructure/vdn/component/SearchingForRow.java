package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import org.jspecify.annotations.NonNull;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.webstack.vdn.component.orderedlayout.HorizontalLayout;

import java.util.function.Consumer;

public class SearchingForRow extends HorizontalLayout {
    protected final DancestyleComboBox styleComboBox;
    protected final RoleSelect roleSelect;
    protected final SearchCriteriaSexComboBox searchCriteriaSexComboBox;
    protected final SkilllevelComboBox skilllevelMinComboBox;
    protected final SkilllevelComboBox skilllevelMaxComboBox;
    protected final Button removeButton;

    public SearchingForRow(DancestyleRepository dancestyleRepository, Consumer<SearchingForRow> removeButtonConsumer) {
        styleComboBox = new DancestyleComboBox(dancestyleRepository);
        roleSelect = new RoleSelect();
        searchCriteriaSexComboBox = new SearchCriteriaSexComboBox();
        searchCriteriaSexComboBox.setId(SearchCriteriaSexComboBox.class.getSimpleName());
        skilllevelMinComboBox = new SkilllevelComboBox();
        skilllevelMaxComboBox = new SkilllevelComboBox();
        removeButton = removeButton(removeButtonConsumer);

        styleComboBox.setId(DancestyleComboBox.class.getSimpleName());
        roleSelect.setId(roleSelect.getClass().getSimpleName());
        skilllevelMinComboBox.setId(SkilllevelComboBox.class.getSimpleName() + "Min");
        skilllevelMaxComboBox.setId(SkilllevelComboBox.class.getSimpleName() + "Max");
        removeButton.setId("RemoveButton");

        padding(false);
        margin(false);
        centered();
        widthFull();
        // the fields together can be wider than the card; allow them to wrap to a second line
        wrap();

        add(    styleComboBox,
                //new NativeLabel(getTranslation("search.role")),
                searchCriteriaSexComboBox,
                roleSelect,
                new NativeLabel(getTranslation("search.skillFrom")),
                skilllevelMinComboBox,
                new NativeLabel(getTranslation("search.skillTo")),
                skilllevelMaxComboBox,
                removeButton);
        flexGrow(1, skilllevelMinComboBox);
        flexGrow(1, skilllevelMaxComboBox);
    }

    private @NonNull Button removeButton(Consumer<SearchingForRow> consumer) {
        Button removeButton = new Button(VaadinIcon.TRASH.create());
        removeButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        removeButton.addClickListener(t -> consumer.accept(this));
        return removeButton;
    }

    public Dancestyle style() {
        return styleComboBox.getValue();
    }
    public SearchingForRow style(Dancestyle v) {
        styleComboBox.setValue(v);
        return this;
    }

    public Role role() {
        return roleSelect.getValue();
    }
    public SearchingForRow role(Role v) {
        roleSelect.setValue(v);
        return this;
    }

    public SearchCriteriaSex sex() {
        return searchCriteriaSexComboBox.getValue();
    }
    public SearchingForRow sex(SearchCriteriaSex v) {
        searchCriteriaSexComboBox.setValue(v);
        return this;
    }

    public Integer skilllevelMin() {
        return skilllevelMinComboBox.getValue();
    }
    public SearchingForRow skilllevelMin(Integer v) {
        skilllevelMinComboBox.setValue(v);
        return this;
    }

    public Integer skilllevelMax() {
        return skilllevelMaxComboBox.getValue();
    }
    public SearchingForRow skilllevelMax(Integer v) {
        skilllevelMaxComboBox.setValue(v);
        return this;
    }
}
