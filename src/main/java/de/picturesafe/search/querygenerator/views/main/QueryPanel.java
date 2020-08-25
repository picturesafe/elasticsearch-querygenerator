package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.parameter.SearchParameter;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class QueryPanel extends VerticalLayout {

    private final ElasticsearchService elasticsearchService;
    private final FieldConfigurationProvider fieldConfigurationProvider;

    private Select<String> indexSelector;
    private TextArea queryText;
	private final List<OperationExpressionPanel> operationExpressionPanels = new ArrayList<>();
    private List<? extends FieldConfiguration> fieldConfigurations;

    public QueryPanel(ElasticsearchService elasticsearchService, FieldConfigurationProvider fieldConfigurationProvider) {
        this.elasticsearchService = elasticsearchService;
        this.fieldConfigurationProvider = fieldConfigurationProvider;
        setPadding(false);

        final Button queryButton = new Button("Query");
		queryButton.addClickListener(e -> generateQuery());

        final HorizontalLayout indexPanel = new HorizontalLayout(indexSelector(), queryButton);
        indexPanel.setVerticalComponentAlignment(Alignment.END, queryButton);
		add(indexPanel);
    }

	private Select<String> indexSelector() {
		final List<String> indexNames = new ArrayList<>();
    	elasticsearchService.listIndices().forEach((name, aliases) -> {
			if (CollectionUtils.isNotEmpty(aliases)) {
				indexNames.addAll(aliases);
			} else {
				indexNames.add(name);
			}
		});

    	indexSelector = new Select<>();
    	indexSelector.setItems(indexNames);
		indexSelector.setLabel("Index/Alias");
		indexSelector.setWidth("300px");
		indexSelector.addValueChangeListener(this::selectIndex);
		indexSelector.focus();
		return indexSelector;
	}

    private void selectIndex(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
    	clear();
    	fieldConfigurations = fieldConfigurationProvider.getFieldConfigurations(event.getValue());
		final OperationExpressionPanel operationExpressionPanel = new OperationExpressionPanel(fieldConfigurations);
		add(operationExpressionPanel);
		operationExpressionPanels.add(operationExpressionPanel);
	}

	private void clear() {
    	operationExpressionPanels.forEach(this::remove);
    	operationExpressionPanels.clear();
		if (queryText != null) {
			remove(queryText);
			queryText = null;
		}
	}

	private void generateQuery() {
		if (queryText == null) {
			queryText = new TextArea("Query");
			queryText.setWidth("100%");
			queryText.setHeight("400px");
			queryText.setReadOnly(true);
			add(queryText);
		}

		final Expression expression = operationExpressionPanels.get(0).getExpression(); // ToDo
		final String json = elasticsearchService.createQueryJson(indexSelector.getValue(), expression, SearchParameter.DEFAULT, true);
		queryText.setValue(json);
	}
}
