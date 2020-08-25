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
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.Elasticsearch;
import de.picturesafe.search.elasticsearch.connect.aggregation.resolve.FacetConverterChain;
import de.picturesafe.search.elasticsearch.connect.aggregation.search.AggregationBuilderFactoryRegistry;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import de.picturesafe.search.elasticsearch.connect.impl.ElasticsearchAdminImpl;
import de.picturesafe.search.elasticsearch.connect.impl.ElasticsearchImpl;
import de.picturesafe.search.elasticsearch.connect.query.QueryFactory;
import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.impl.MappingFieldConfigurationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@SpringComponent
@UIScope
public class MainPanel extends VerticalLayout {

	private final List<QueryFactory> queryFactories;
	private final List<FilterFactory> filterFactories;
	private final String timeZone;
	private final AggregationBuilderFactoryRegistry aggregationBuilderFactoryRegistry;
	private final FacetConverterChain facetConverterChain;

	private TextField addressField;
	private Button connectButton;
	private QueryPanel queryPanel;


	@Autowired
	public MainPanel(List<QueryFactory> queryFactories, List<FilterFactory> filterFactories, @Qualifier("elasticsearchTimeZone") String timeZone,
					 AggregationBuilderFactoryRegistry aggregationBuilderFactoryRegistry, FacetConverterChain facetConverterChain) {
		this.queryFactories = queryFactories;
		this.filterFactories = filterFactories;
		this.timeZone = timeZone;
		this.aggregationBuilderFactoryRegistry = aggregationBuilderFactoryRegistry;
		this.facetConverterChain = facetConverterChain;
		init();
	}

	private void init() {
		addressField = new TextField();
		addressField.setLabel("Elasticsearch address");
		addressField.setPlaceholder("localhost:9200");
		addressField.setWidth("300px");

		connectButton = new Button("connect");
		connectButton.addClickListener(e -> connect());

		final HorizontalLayout connectPanel = new HorizontalLayout(addressField, connectButton);
		connectPanel.setVerticalComponentAlignment(Alignment.END, connectButton);

		add(
				new H2("Elasticsearch Query Generator"),
				new H3("powered by picturesafe-search"),
				connectPanel
		);
	}

	private void connect() {
		if (queryPanel != null) {
			remove(queryPanel);
		}
		final String address = addressField.isEmpty() ? addressField.getPlaceholder() : addressField.getValue();
		queryPanel = queryPanel(address);
		add(queryPanel);
	}

	private QueryPanel queryPanel(String elasticsearchAddress) {
		final RestClientConfiguration clientConfig = new RestClientConfiguration(elasticsearchAddress);
    	final ElasticsearchAdminImpl elasticsearchAdmin = new ElasticsearchAdminImpl(clientConfig);
    	elasticsearchAdmin.init();
		final MappingFieldConfigurationProvider fieldConfigurationProvider = new MappingFieldConfigurationProvider(elasticsearchAdmin);
		return new QueryPanel(elasticsearchService(elasticsearchAdmin, clientConfig, fieldConfigurationProvider), fieldConfigurationProvider);
	}

	private ElasticsearchService elasticsearchService(ElasticsearchAdminImpl elasticsearchAdmin, RestClientConfiguration clientConfig,
													  FieldConfigurationProvider fieldConfigurationProvider) {
		return new ElasticsearchServiceImpl(elasticsearch(elasticsearchAdmin, clientConfig),
				indexAlias -> new StandardIndexPresetConfiguration(indexAlias, 1, 0),  // ToDo: Read actual index settings
				fieldConfigurationProvider);
	}

	private Elasticsearch elasticsearch(ElasticsearchAdminImpl elasticsearchAdmin, RestClientConfiguration clientConfig) {
		final ElasticsearchImpl elasticsearch = new ElasticsearchImpl(elasticsearchAdmin, clientConfig, queryFactories, filterFactories, timeZone);
		elasticsearch.setAggregationBuilderFactoryRegistry(aggregationBuilderFactoryRegistry);
		elasticsearch.setFacetConverterChain(facetConverterChain);
		elasticsearch.init();
		return elasticsearch;
	}
}
