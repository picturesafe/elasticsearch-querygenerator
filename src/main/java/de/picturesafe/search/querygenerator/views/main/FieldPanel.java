package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class FieldPanel extends HorizontalLayout {

    private Select<String> expressionSelector;

    @SuppressWarnings("unchecked")
    public FieldPanel(List<? extends FieldConfiguration> fieldConfigurations) {
        final Select<FieldConfiguration> fieldSelector = new Select<>();
        fieldSelector.setItemLabelGenerator(FieldConfiguration::getName);
        fieldSelector.setItems((List<FieldConfiguration>) fieldConfigurations);
		fieldSelector.setLabel("Field");
		fieldSelector.setWidth("300px");
		fieldSelector.addValueChangeListener(this::selectField);

		add(fieldSelector);
		setPadding(false);
		fieldSelector.focus();
    }

    private void selectField(AbstractField.ComponentValueChangeEvent<Select<FieldConfiguration>, FieldConfiguration> event) {
        if (expressionSelector != null) {
            remove(expressionSelector);
        }
        final FieldConfiguration fieldConfiguration = event.getValue();
        final ElasticsearchType type = ElasticsearchType.valueOf(fieldConfiguration.getElasticsearchType().toUpperCase(Locale.ROOT));
        if (type != ElasticsearchType.OBJECT && type != ElasticsearchType.NESTED && type != ElasticsearchType.COMPLETION) {
            expressionSelector = expressionSelector(type);
            add(expressionSelector);
        }
	}

    private Select<String> expressionSelector(ElasticsearchType type) {
        final Select<String> expressionSelector = new Select<>();
        expressionSelector.setItems(fieldExpression(type));
		expressionSelector.setLabel("Expression");
		expressionSelector.addValueChangeListener(this::selectExpression);
		expressionSelector.focus();
		return expressionSelector;
    }

    private List<String> fieldExpression(ElasticsearchType type) {
        switch (type) {
            case TEXT:
                return Arrays.asList("value", "fulltext");
            case DATE:
                return Arrays.asList("value", "day", "day range");
            case LONG:
            case INTEGER:
            case SHORT:
            case BYTE:
            case DOUBLE:
            case FLOAT:
                return Arrays.asList("value", "range", "in");
            default:
                return Collections.singletonList("value");
        }
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
