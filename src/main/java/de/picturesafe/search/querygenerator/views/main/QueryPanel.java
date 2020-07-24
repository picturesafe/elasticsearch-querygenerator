package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;

public class QueryPanel extends VerticalLayout {

    private final String elasticsearchAddress;

    public QueryPanel(String elasticsearchAddress) {
        this.elasticsearchAddress = elasticsearchAddress;
        setPadding(false);

        final Select<String> indexSelect = new Select<>("index-1", "index-2", "index-3");
		indexSelect.setLabel("Index/Alias");
		indexSelect.setWidth("300px");
		indexSelect.addValueChangeListener(this::selectIndex);
		add(indexSelect);
		indexSelect.focus();
    }

    private void selectIndex(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
		final Select<String> fieldSelect = new Select<>("field-1", "field-2", "field-3");
		fieldSelect.setLabel("Field");
		fieldSelect.setWidth("300px");
		fieldSelect.addValueChangeListener(this::selectField);

		final HorizontalLayout fieldPanel = new HorizontalLayout(fieldSelect);
		fieldPanel.setPadding(false);
		add(fieldPanel);
		fieldSelect.focus();
	}

	private void selectField(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
        final Select<String> expressionSelect = new Select<>("value", "fulltext");
		expressionSelect.setLabel("Expression");
		expressionSelect.addValueChangeListener(this::selectExpression);
		event.getSource().getParent().ifPresent(c -> ((HasComponents) c).add(expressionSelect));
		expressionSelect.focus();
	}

	private void selectExpression(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
        final TextField valueField = new TextField();
        valueField.setLabel("Value");
        valueField.setPlaceholder("value");
        valueField.setWidth("300px");
        event.getSource().getParent().ifPresent(c -> ((HasComponents) c).add(valueField));
        valueField.focus();
    }
}
