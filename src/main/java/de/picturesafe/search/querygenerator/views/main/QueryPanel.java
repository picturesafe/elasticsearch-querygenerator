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

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.expression.Expression;
import de.picturesafe.search.parameter.SearchParameter;
import de.picturesafe.search.parameter.SortOption;

import java.util.List;

public class QueryPanel extends VerticalLayout implements QueryLayout {

    private final ElasticsearchService elasticsearchService;
    private final String indexAlias;

    private TextArea queryText;
	private final OperationExpressionPanel operationExpressionPanel;
	private final SortPanel sortPanel;

    public QueryPanel(ElasticsearchService elasticsearchService, String indexAlias, List<? extends FieldConfiguration> fieldConfigurations) {
        this.elasticsearchService = elasticsearchService;
		this.indexAlias = indexAlias;
        setPadding(false);

        operationExpressionPanel = new OperationExpressionPanel(fieldConfigurations);
		add(operationExpressionPanel);

		sortPanel = new SortPanel(fieldConfigurations);
		add(sortPanel);

        final Button queryButton = new Button("Query");
		queryButton.addClickListener(e -> generateQuery());
		add(queryButton);
    }

	private void generateQuery() {
		if (queryText == null) {
			queryText = new TextArea("Query");
			queryText.setWidth(QUERY_TEXT_WIDTH);
			queryText.setHeight(QUERY_TEXT_HEIGHT);
			queryText.setReadOnly(true);
			add(queryText);
		}

		final Expression expression = operationExpressionPanel.getExpression();
		final List<SortOption> sortOptions = sortPanel.getSortOptions();
		final String json = elasticsearchService.createQueryJson(indexAlias, expression, SearchParameter.builder().sortOptions(sortOptions).build(), true);
		queryText.setValue(json);
	}
}
