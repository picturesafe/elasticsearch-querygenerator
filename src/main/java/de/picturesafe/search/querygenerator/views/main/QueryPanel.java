package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.parameter.SearchParameter;

import java.util.List;

public class QueryPanel extends VerticalLayout {

    private final ElasticsearchService elasticsearchService;
    private final String indexAlias;

    private TextArea queryText;
	private final OperationExpressionPanel operationExpressionPanel;

    public QueryPanel(ElasticsearchService elasticsearchService, String indexAlias, List<? extends FieldConfiguration> fieldConfigurations) {
        this.elasticsearchService = elasticsearchService;
		this.indexAlias = indexAlias;
        setPadding(false);

        operationExpressionPanel = new OperationExpressionPanel(fieldConfigurations);
		add(operationExpressionPanel);

        final Button queryButton = new Button("Query");
		queryButton.addClickListener(e -> generateQuery());
		add(queryButton);
    }

	private void generateQuery() {
		if (queryText == null) {
			queryText = new TextArea("Query");
			queryText.setWidth("100%");
			queryText.setHeight("400px");
			queryText.setReadOnly(true);
			add(queryText);
		}

		final Expression expression = operationExpressionPanel.getExpression();
		final String json = elasticsearchService.createQueryJson(indexAlias, expression, SearchParameter.DEFAULT, true);
		queryText.setValue(json);
	}
}
