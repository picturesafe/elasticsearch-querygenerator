package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.InExpression;
import de.picturesafe.search.expression.KeywordExpression;
import de.picturesafe.search.expression.RangeValueExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.expression.internal.EmptyExpression;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FieldPanel extends HorizontalLayout implements ExpressionPanel {

    private enum ExpressionType {VALUE, QUERY_STRING, KEYWORD, RANGE, IN, DAY, DAY_RANGE}

    private final Select<FieldConfiguration> fieldSelector;
    private Select<ExpressionType> expressionSelector;
    private final List<TextField> valueFields = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public FieldPanel(List<? extends FieldConfiguration> fieldConfigurations) {
        fieldSelector = new Select<>();
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
        clear();
        final FieldConfiguration fieldConfiguration = event.getValue();
        final ElasticsearchType type = ElasticsearchType.valueOf(fieldConfiguration.getElasticsearchType().toUpperCase(Locale.ROOT));
        if (type != ElasticsearchType.OBJECT && type != ElasticsearchType.NESTED && type != ElasticsearchType.COMPLETION) {
            addExpressionSelector(fieldConfiguration, type);
        }
	}

	private void clear() {
        if (expressionSelector != null) {
            remove(expressionSelector);
        }
        removeValueFields();
    }

    private void removeValueFields() {
        valueFields.forEach(this::remove);
        valueFields.clear();
    }

    private void addExpressionSelector(FieldConfiguration fieldConfiguration, ElasticsearchType type) {
        final Set<ExpressionType> expressionTypes = expressionTypes(fieldConfiguration, type);
        expressionSelector = new Select<>();
        expressionSelector.setItems(expressionTypes);
		expressionSelector.setLabel("Expression");
        expressionSelector.addValueChangeListener(this::selectExpression);
        add(expressionSelector);
        expressionSelector.setValue(expressionTypes.stream().findFirst().orElse(null));
    }

    private Set<ExpressionType> expressionTypes(FieldConfiguration fieldConfiguration, ElasticsearchType type) {
        switch (type) {
            case TEXT:
                final boolean hasKeywordField = fieldConfiguration.isSortable() || fieldConfiguration.isAggregatable();
                return hasKeywordField ? EnumSet.of(ExpressionType.QUERY_STRING, ExpressionType.KEYWORD) : EnumSet.of(ExpressionType.QUERY_STRING);
            case KEYWORD:
                return EnumSet.of(ExpressionType.KEYWORD);
            case DATE:
                return EnumSet.of(ExpressionType.VALUE, ExpressionType.RANGE, ExpressionType.DAY, ExpressionType.DAY_RANGE);
            case LONG:
            case INTEGER:
            case SHORT:
            case BYTE:
            case DOUBLE:
            case FLOAT:
                return EnumSet.of(ExpressionType.VALUE, ExpressionType.RANGE, ExpressionType.IN);
            default:
                return EnumSet.of(ExpressionType.VALUE);
        }
    }

	private void selectExpression(AbstractField.ComponentValueChangeEvent<Select<ExpressionType>, ExpressionType> event) {
        removeValueFields();

        final ExpressionType type = event.getValue();
        if (type == ExpressionType.RANGE || type == ExpressionType.DAY_RANGE) {
            add(valueField("From", true));
            add(valueField("To", false));
        } else if (type == ExpressionType.IN) {
            add(valueField("Values (comma separated)", true));
        } else {
            add(valueField("Value", true));
        }
    }

    private TextField valueField(String label, boolean focus) {
        final TextField valueField = new TextField();
        valueField.setLabel(label);
        valueField.setPlaceholder("value");
        valueField.setWidth("300px");
        if (focus) {
            valueField.focus();
        }
        valueFields.add(valueField);
        return valueField;
    }

    @Override
    public Expression getExpression() {
        final String fieldName = fieldSelector.getValue().getName();
        switch (expressionSelector.getValue()) {
            case VALUE:
            case QUERY_STRING:
                return new ValueExpression(fieldName, valueFields.get(0).getValue());
            case KEYWORD:
                return new KeywordExpression(fieldName, valueFields.get(0).getValue());
            case RANGE:
                return new RangeValueExpression(fieldName, valueFields.get(0).getValue(), valueFields.get(1).getValue());
            case IN:
                return new InExpression(fieldName, valueFields.get(0).getValue().split(","));
            case DAY:
            case DAY_RANGE:
                // ToDo
            default:
                return new EmptyExpression();
        }
    }
}
