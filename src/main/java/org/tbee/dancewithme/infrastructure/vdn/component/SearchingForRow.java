package org.tbee.dancewithme.infrastructure.vdn.component;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.VaadinIcon;
import org.jspecify.annotations.NonNull;
import org.tbee.dancewithme.domain.Dancestyle;
import org.tbee.dancewithme.domain.repository.DancestyleRepository;
import org.tbee.dancewithme.domain.valueobject.Role;
import org.tbee.dancewithme.domain.valueobject.SearchCriteriaSex;
import org.tbee.webstack.vdn.component.orderedlayout.HorizontalLayout;

import java.util.function.Consumer;

public class SearchingForRow extends HorizontalLayout {
    public static final String STYLE_COMBO_BOX_ID = "styleComboBox";
    public static final String ROLE_SELECT_ID = "roleSelect";
    public static final String SEARCH_CRITERIA_SEX_COMBO_BOX_ID = "searchCriteriaSexComboBox";
    public static final String SKILLLEVEL_MIN_COMBO_BOX_ID = "skilllevelMinComboBox";
    public static final String SKILLLEVEL_MAX_COMBO_BOX_ID = "skilllevelMaxComboBox";
    public static final String REMOVE_BUTTON_IS = "removeButton";
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
        skilllevelMinComboBox = new SkilllevelComboBox();
        skilllevelMaxComboBox = new SkilllevelComboBox();
        removeButton = removeButton(removeButtonConsumer);

        styleComboBox.setId(STYLE_COMBO_BOX_ID);
        roleSelect.setId(ROLE_SELECT_ID);
        searchCriteriaSexComboBox.setId(SEARCH_CRITERIA_SEX_COMBO_BOX_ID);
        skilllevelMinComboBox.setId(SKILLLEVEL_MIN_COMBO_BOX_ID);
        skilllevelMaxComboBox.setId(SKILLLEVEL_MAX_COMBO_BOX_ID);
        removeButton.setId(REMOVE_BUTTON_IS);

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
