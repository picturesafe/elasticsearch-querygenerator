/*
 * Copyright 2020 picturesafe media/data/bank GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.expression.OperationExpression;

import java.util.ArrayList;
import java.util.List;

public class OperationExpressionPanel extends VerticalLayout implements ExpressionPanel {

    private final HorizontalLayout operatorPanel;
    private final Select<OperationExpression.Operator> operatorSelector;
    private final List<? extends FieldConfiguration> fieldConfigurations;
    private final List<ExpressionPanel> expressionPanels = new ArrayList<>();

    public OperationExpressionPanel(List<? extends FieldConfiguration> fieldConfigurations) {
        this.fieldConfigurations = fieldConfigurations;
        operatorSelector = new Select<>(OperationExpression.Operator.AND, OperationExpression.Operator.OR);
        operatorSelector.setValue(OperationExpression.Operator.AND);
        operatorSelector.setLabel("Operator");
        operatorSelector.getStyle().set("margin-right", "auto");

        operatorPanel = new HorizontalLayout(operatorSelector);
        operatorPanel.setWidth("100%");
        add(operatorPanel);
        addOperandPanelAfter(operatorPanel);
        getStyle().set("border", "1px solid grey");
    }

    private void addOperandPanelAfter(Component component) {
        final OperandPanel operandPanel = new OperandPanel();
        addComponentAtIndex(indexOf(component) + 1, operandPanel);
        expressionPanels.add(operandPanel);
    }

    private void removeExpressionPanel(ExpressionPanel panel) {
        remove((Component) panel);
        expressionPanels.remove(panel);

        if (expressionPanels.isEmpty()) {
            final Button addOperandButton = new Button("+");
            addOperandButton.addClickListener(e -> {
                addOperandPanelAfter(operatorPanel);
                remove(addOperandButton);
            });
            addOperandButton.setDisableOnClick(true);
            add(addOperandButton);
        }
    }

    private void addOperationExpressionPanelAfter(Component component) {
        final InnerOperationExpressionPanel operationExpressionPanel = new InnerOperationExpressionPanel(this);
        addComponentAtIndex(indexOf(component) + 1, operationExpressionPanel);
        expressionPanels.add(operationExpressionPanel);
    }

    @Override
    public Expression getExpression() {
        final OperationExpression operationExpression = new OperationExpression(operatorSelector.getValue());
        expressionPanels.forEach(panel -> operationExpression.add(panel.getExpression()));
        return operationExpression;
    }

    private class OperandPanel extends HorizontalLayout implements ExpressionPanel {

        final FieldPanel fieldPanel;

        OperandPanel() {
            fieldPanel = new FieldPanel(fieldConfigurations);
            add(fieldPanel);

            final ContextMenu menu = new ContextMenu();
            menu.addItem("Add field", e -> addOperandPanelAfter(this));
            menu.addItem("Add operation", e -> addOperationExpressionPanelAfter(this));
            menu.addItem("Remove field", e -> removeExpressionPanel(this));
            final Button menuButton = new Button(VaadinIcon.CARET_UP.create());
            menu.setTarget(menuButton);
            menu.setOpenOnClick(true);
            add(menuButton);
            setVerticalComponentAlignment(Alignment.END, menuButton);
        }

        @Override
        public Expression getExpression() {
            return fieldPanel.getExpression();
        }
    }

    private class InnerOperationExpressionPanel extends HorizontalLayout implements ExpressionPanel {

        final OperationExpressionPanel parent;
        final OperationExpressionPanel operationExpressionPanel;

        InnerOperationExpressionPanel(OperationExpressionPanel parent) {
            this.parent = parent;

            operationExpressionPanel = new OperationExpressionPanel(fieldConfigurations);
            add(operationExpressionPanel);

            final ContextMenu menu = new ContextMenu();
            menu.addItem("Add field", e -> addOperandPanelAfter(this));
            menu.addItem("Add operation", e -> addOperationExpressionPanelAfter(this));
            menu.addItem("Remove operation", e -> removeExpressionPanel(this));
            final Button menuButton = new Button(VaadinIcon.CARET_SQUARE_UP_O.create());
            menu.setTarget(menuButton);
            menu.setOpenOnClick(true);
            operationExpressionPanel.operatorPanel.add(menuButton);

            setPadding(true);
        }

        @Override
        public Expression getExpression() {
            return operationExpressionPanel.getExpression();
        }
    }
}
