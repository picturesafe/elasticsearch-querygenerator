package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.expression.BoostableExpression;
import de.picturesafe.search.expression.DayExpression;
import de.picturesafe.search.expression.DayRangeExpression;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.InExpression;
import de.picturesafe.search.expression.IsNullExpression;
import de.picturesafe.search.expression.KeywordExpression;
import de.picturesafe.search.expression.RangeValueExpression;
import de.picturesafe.search.expression.ValueExpression;
import de.picturesafe.search.expression.internal.EmptyExpression;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FieldPanel extends HorizontalLayout implements ExpressionPanel, QueryLayout {

    private enum ExpressionType {VALUE, QUERY_STRING, KEYWORD, RANGE, IN, DAY, DAY_RANGE}

    private final Select<FieldConfiguration> fieldSelector;
    private Select<ExpressionType> expressionSelector;
    private final List<AbstractField<?, ?>> valueFields = new ArrayList<>();
    private NumberField boostField;
    private final Set<ExpressionType> boostableExpressions = EnumSet.of(ExpressionType.VALUE, ExpressionType.QUERY_STRING, ExpressionType.IN);

    @SuppressWarnings("unchecked")
    public FieldPanel(List<? extends FieldConfiguration> fieldConfigurations) {
        fieldSelector = new Select<>();
        fieldSelector.setItemLabelGenerator(FieldConfiguration::getName);
        fieldSelector.setItems((List<FieldConfiguration>) fieldConfigurations);
		fieldSelector.setLabel("Field");
		fieldSelector.setWidth(FIELD_WIDTH);
		fieldSelector.addValueChangeListener(this::selectField);

		add(fieldSelector);
		setPadding(false);
		fieldSelector.focus();
    }

    private void selectField(AbstractField.ComponentValueChangeEvent<Select<FieldConfiguration>, FieldConfiguration> event) {
        clear();
        final FieldConfiguration fieldConfiguration = event.getValue();
        final ElasticsearchType elasticType = elasticType(fieldConfiguration);
        if (elasticType != ElasticsearchType.OBJECT && elasticType != ElasticsearchType.NESTED && elasticType != ElasticsearchType.COMPLETION) {
            addExpressionSelector(fieldConfiguration, elasticType);
        }
	}

    private ElasticsearchType elasticType(FieldConfiguration fieldConfiguration) {
        return ElasticsearchType.valueOf(fieldConfiguration.getElasticsearchType().toUpperCase(Locale.ROOT));
    }

	private void clear() {
        if (expressionSelector != null) {
            remove(expressionSelector);
        }
        removeFields();
    }

    private void removeFields() {
        valueFields.forEach(this::remove);
        valueFields.clear();
        if (boostField != null) {
            remove(boostField);
            boostField = null;
        }
    }

    private void addExpressionSelector(FieldConfiguration fieldConfiguration, ElasticsearchType elasticType) {
        final Set<ExpressionType> expressionTypes = expressionTypes(fieldConfiguration, elasticType);
        expressionSelector = new Select<>();
        expressionSelector.setItems(expressionTypes);
		expressionSelector.setLabel("Expression");
        expressionSelector.addValueChangeListener(e -> selectExpression(e.getValue(), elasticType));
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
                return EnumSet.of(ExpressionType.VALUE, ExpressionType.DAY, ExpressionType.DAY_RANGE);
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

	private void selectExpression(ExpressionType expressionType, ElasticsearchType elasticType) {
        removeFields();

        if (expressionType == ExpressionType.RANGE || expressionType == ExpressionType.DAY_RANGE) {
            addValueField(expressionType, elasticType, "From", true);
            addValueField(expressionType, elasticType, "To", false);
        } else if (expressionType == ExpressionType.IN) {
            addValueField(expressionType, ElasticsearchType.TEXT, "Values (comma separated)", true);
        } else {
            addValueField(expressionType, elasticType, "Value", true);
        }

        if (boostableExpressions.contains(expressionType)) {
            addBoostField();
        }
    }

    private void addValueField(ExpressionType expressionType, ElasticsearchType elasticType, String label, boolean focus) {
        final AbstractField<?, ?> valueField;
        switch (expressionType) {
            case VALUE:
            case RANGE:
                if (elasticType == ElasticsearchType.BOOLEAN) {
                    final Select<Boolean> selector = new Select<>(Boolean.TRUE, Boolean.FALSE);
                    selector.setValue(Boolean.TRUE);
                    selector.setLabel(label);
                    valueField = selector;
                } else {
                    valueField = textField(elasticType, label);
                }
                break;
            case DAY:
            case DAY_RANGE:
                valueField = new DatePicker(label);
                break;
            default:
                valueField = textField(elasticType, label);
        }

        ((HasSize) valueField).setWidth(FIELD_WIDTH);
        if (focus && valueField instanceof Focusable) {
            ((Focusable<?>) valueField).focus();
        }
        add(valueField);
        valueFields.add(valueField);
    }

    private AbstractField<?, ?> textField(ElasticsearchType elasticType, String label) {
        final AbstractField<?, ?> textField;
        switch (elasticType) {
            case LONG:
            case INTEGER:
            case SHORT:
                textField = new IntegerField(label, "0");
                break;
            case DOUBLE:
            case FLOAT:
                textField = new NumberField(label, "0.0");
                break;
            default:
                textField = new TextField(label);
        }
        return textField;
    }

    private void addBoostField() {
        boostField = new NumberField("Boost");
        boostField.setWidth("50px");
        boostField.setMin(0.1);
        boostField.setMax(10.0);
        boostField.setStep(0.1);
        boostField.setErrorMessage("Set number from 0.1 to 10.0");
        add(boostField);
    }

    @Override
    public Expression getExpression() {
        if (fieldSelector.isEmpty()) {
            return new EmptyExpression();
        }

        final String fieldName = fieldSelector.getValue().getName();
        final Expression expression;
        switch (expressionSelector.getValue()) {
            case VALUE:
                expression = isEmptyValue(0) ? new IsNullExpression(fieldName) : new ValueExpression(fieldName, value(0));
                break;
            case QUERY_STRING:
                expression = new ValueExpression(fieldName, stringValue(0));
                break;
            case KEYWORD:
                expression = isEmptyValue(0) ? new IsNullExpression(fieldName) : new KeywordExpression(fieldName, stringValue(0));
                break;
            case RANGE:
                expression = new RangeValueExpression(fieldName, value(0), value(1));
                break;
            case IN:
                expression = isEmptyValue(0) ? new IsNullExpression(fieldName) : new InExpression(fieldName, inValues(0));
                break;
            case DAY:
                expression = isEmptyValue(0) ? new IsNullExpression(fieldName) : new DayExpression(fieldName, dateValue(0));
                break;
            case DAY_RANGE:
                expression = new DayRangeExpression(fieldName, dateValue(0), dateValue(1));
                break;
            default:
                expression = new EmptyExpression();
        }

        return (hasBoost() && expression instanceof BoostableExpression)
                ? ((BoostableExpression<?>) expression).boost(boostField.getValue().floatValue())
                : expression;
    }

    private boolean isEmptyValue(int index) {
        return valueFields.get(index).isEmpty();
    }

    private Object value(int index) {
        return valueFields.get(index).getValue();
    }

    private String stringValue(int index) {
        return (String) valueFields.get(index).getValue();
    }

    private Date dateValue(int index) {
        final LocalDate localDate = (LocalDate) valueFields.get(index).getValue();
        return (localDate != null) ? Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()) : null;
    }

    private Object[] inValues(int index) {
        final String[] values = stringValue(index).split(",");
        final ElasticsearchType elasticType = elasticType(fieldSelector.getValue());
        switch (elasticType) {
            case LONG:
                return Arrays.stream(values).map(Long::parseLong).toArray();
            case INTEGER:
                return Arrays.stream(values).map(Integer::parseInt).toArray();
            case SHORT:
                return Arrays.stream(values).map(Short::parseShort).toArray();
            case DOUBLE:
                return Arrays.stream(values).map(Double::parseDouble).toArray();
            case FLOAT:
                return Arrays.stream(values).map(Float::parseFloat).toArray();
            default:
                return values;
        }
    }

    private boolean hasBoost() {
        return boostField != null && boostField.getValue() != null;
    }
}
