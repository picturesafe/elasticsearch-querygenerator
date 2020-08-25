package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.OperationExpression;

import java.util.ArrayList;
import java.util.List;

public class OperationExpressionPanel extends VerticalLayout implements ExpressionPanel {

    private Select<OperationExpression.Operator> operatorSelector;
    private final List<? extends FieldConfiguration> fieldConfigurations;
    private final List<ExtensibleExpressionPanel> expressionPanels = new ArrayList<>();

    public OperationExpressionPanel(List<? extends FieldConfiguration> fieldConfigurations) {
        this.fieldConfigurations = fieldConfigurations;
        operatorSelector = new Select<>(OperationExpression.Operator.AND, OperationExpression.Operator.OR);
        operatorSelector.setValue(OperationExpression.Operator.AND);
        operatorSelector.setLabel("Operator");
        add(operatorSelector);
        addOperandPanel();
        getStyle().set("border","1px solid grey");
    }

    private void addOperandPanel() {
        expressionPanels.forEach(ExtensibleExpressionPanel::removeAddButtons);
        final OperandPanel operandPanel = new OperandPanel();
        add(operandPanel);
        expressionPanels.add(operandPanel);
    }

    private void removeExpressionPanel(ExtensibleExpressionPanel panel) {
        remove((Component) panel);
        expressionPanels.remove(panel);
    }

    private void addOperationExpressionPanel() {
        expressionPanels.forEach(ExtensibleExpressionPanel::removeAddButtons);
        final InnerOperationExpressionPanel operationExpressionPanel = new InnerOperationExpressionPanel(this);
        add(operationExpressionPanel);
        expressionPanels.add(operationExpressionPanel);
    }

    @Override
    public Expression getExpression() {
        final OperationExpression operationExpression = new OperationExpression(operatorSelector.getValue());
        expressionPanels.forEach(panel -> operationExpression.add(panel.getExpression()));
        return operationExpression;
    }

    private class OperandPanel extends HorizontalLayout implements ExtensibleExpressionPanel {

        final FieldPanel fieldPanel;
        Button removeButton;
        Button addOperandButton;
        Button addOperationExpressionButton;

        OperandPanel() {
            fieldPanel = new FieldPanel(fieldConfigurations);
            add(fieldPanel);

            removeButton = new Button("-");
            removeButton.addClickListener(e -> removeExpressionPanel(this));
            add(removeButton);

            addOperandButton = new Button("+");
            addOperandButton.addClickListener(e -> addOperandPanel());
            addOperandButton.setDisableOnClick(true);
            add(addOperandButton);

            addOperationExpressionButton = new Button("++");
            addOperationExpressionButton.addClickListener(e -> addOperationExpressionPanel());
            addOperationExpressionButton.setDisableOnClick(true);
            add(addOperationExpressionButton);

            setVerticalComponentAlignment(Alignment.END, removeButton, addOperandButton, addOperationExpressionButton);
        }

        @Override
        public void removeAddButtons() {
            if (addOperandButton != null) {
                remove(addOperandButton);
                addOperandButton = null;
            }
            if (addOperationExpressionButton != null) {
                remove(addOperationExpressionButton);
                addOperationExpressionButton = null;
            }
        }

        @Override
        public Expression getExpression() {
            return fieldPanel.getExpression();
        }
    }

    private class InnerOperationExpressionPanel extends HorizontalLayout implements ExtensibleExpressionPanel {

        final OperationExpressionPanel parent;
        final OperationExpressionPanel operationExpressionPanel;
        Button removeButton;
        Button addOperandButton;
        Button addOperationExpressionButton;

        public InnerOperationExpressionPanel(OperationExpressionPanel parent) {
            this.parent = parent;

            operationExpressionPanel = new OperationExpressionPanel(fieldConfigurations);
            add(operationExpressionPanel);

            removeButton = new Button("--");
            removeButton.addClickListener(e -> parent.removeExpressionPanel(this));
            add(removeButton);

            addOperandButton = new Button("+");
            addOperandButton.addClickListener(e -> parent.addOperandPanel());
            addOperandButton.setDisableOnClick(true);
            add(addOperandButton);

            addOperationExpressionButton = new Button("++");
            addOperationExpressionButton.addClickListener(e -> parent.addOperationExpressionPanel());
            addOperationExpressionButton.setDisableOnClick(true);
            add(addOperationExpressionButton);

            setVerticalComponentAlignment(Alignment.END, removeButton, addOperandButton, addOperationExpressionButton);
            setPadding(true);
        }

        @Override
        public void removeAddButtons() {
            if (addOperandButton != null) {
                remove(addOperandButton);
                addOperandButton = null;
            }
            if (addOperationExpressionButton != null) {
                remove(addOperationExpressionButton);
                addOperationExpressionButton = null;
            }
        }

        @Override
        public Expression getExpression() {
            return operationExpressionPanel.getExpression();
        }
    }
}
