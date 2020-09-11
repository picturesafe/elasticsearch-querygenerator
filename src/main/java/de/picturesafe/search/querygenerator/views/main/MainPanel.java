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

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.elasticsearch.config.RestClientConfiguration;
import de.picturesafe.search.elasticsearch.config.impl.StandardIndexPresetConfiguration;
import de.picturesafe.search.elasticsearch.connect.Elasticsearch;
import de.picturesafe.search.elasticsearch.connect.aggregation.resolve.FacetConverterChain;
import de.picturesafe.search.elasticsearch.connect.aggregation.search.AggregationBuilderFactoryRegistry;
import de.picturesafe.search.elasticsearch.connect.filter.FilterFactory;
import de.picturesafe.search.elasticsearch.connect.impl.ElasticsearchAdminImpl;
import de.picturesafe.search.elasticsearch.connect.impl.ElasticsearchImpl;
import de.picturesafe.search.elasticsearch.connect.impl.MissingValueSortPosition;
import de.picturesafe.search.elasticsearch.connect.query.QueryFactory;
import de.picturesafe.search.elasticsearch.impl.ElasticsearchServiceImpl;
import de.picturesafe.search.elasticsearch.impl.MappingFieldConfigurationProvider;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.List;

@SpringComponent
@UIScope
public class MainPanel extends VerticalLayout implements QueryLayout {

	private final List<QueryFactory> queryFactories;
	private final List<FilterFactory> filterFactories;
	private final String timeZone;
	private final AggregationBuilderFactoryRegistry aggregationBuilderFactoryRegistry;
	private final FacetConverterChain facetConverterChain;

	private HorizontalLayout connectPanel;
	private TextField addressField;
	private Select<String> indexSelector;
	private QueryPanel queryPanel;

	// Workaround for missing values because the ElasticsearchImpl instances are created dynamically and not via the Spring context.
	@Value("${elasticsearch.service.check_cluster_status_timeout:10000}")
    protected long checkClusterStatusTimeout;
    @Value("${elasticsearch.service.indexing_bulk_size:1000}")
    protected int indexingBulkSize;
    @Value("${elasticsearch.service.missing_value_sort_position:LAST}")
    protected MissingValueSortPosition missingValueSortPosition;


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
		addressField.setWidth(FIELD_WIDTH);

		final Button connectButton = new Button("connect");
		connectButton.addClickListener(e -> connect());

		connectPanel = new HorizontalLayout(addressField, connectButton);
		connectPanel.setVerticalComponentAlignment(Alignment.END, connectButton);

		final Anchor picturesafeSearchLink = new Anchor("https://picturesafe-search.io/", "picturesafe-search");
		picturesafeSearchLink.setTarget("_blank");
		add(
				new H2("Elasticsearch Query Generator"),
				new H3(new Text("powered by "), picturesafeSearchLink),
				connectPanel
		);
	}

	private void connect() {
		clear();
		final ElasticsearchConnection connection = createConnection();
		indexSelector = indexSelector(connection);
		connectPanel.add(indexSelector);
	}

	private void clear() {
		if (indexSelector != null) {
			connectPanel.remove(indexSelector);
			indexSelector = null;
		}
		if (queryPanel != null) {
			remove(queryPanel);
			queryPanel = null;
		}
	}

	private ElasticsearchConnection createConnection() {
		final String elasticsearchAddress = addressField.isEmpty() ? addressField.getPlaceholder() : addressField.getValue();
		final RestClientConfiguration clientConfig = new RestClientConfiguration(elasticsearchAddress);
    	final ElasticsearchAdminImpl elasticsearchAdmin = new ElasticsearchAdminImpl(clientConfig);
    	elasticsearchAdmin.init();
		final MappingFieldConfigurationProvider fieldConfigurationProvider = new MappingFieldConfigurationProvider(elasticsearchAdmin);
		return new ElasticsearchConnection(elasticsearchService(elasticsearchAdmin, clientConfig, fieldConfigurationProvider), fieldConfigurationProvider);
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
		elasticsearch.setCheckClusterStatusTimeout(checkClusterStatusTimeout);
		elasticsearch.setIndexingBulkSize(indexingBulkSize);
		elasticsearch.setMissingValueSortPosition(missingValueSortPosition);
		elasticsearch.init();
		return elasticsearch;
	}

	private Select<String> indexSelector(ElasticsearchConnection connection) {
		final List<String> indexNames = new ArrayList<>();
    	connection.elasticsearchService.listIndices().forEach((name, aliases) -> {
			if (CollectionUtils.isNotEmpty(aliases)) {
				indexNames.addAll(aliases);
			} else {
				indexNames.add(name);
			}
		});

    	final Select<String> indexSelector = new Select<>();
    	indexSelector.setItems(indexNames);
		indexSelector.setLabel("Index/Alias");
		indexSelector.setWidth(FIELD_WIDTH);
		indexSelector.addValueChangeListener(e -> selectIndex(connection, e.getValue()));
		indexSelector.focus();
		return indexSelector;
	}

    private void selectIndex(ElasticsearchConnection connection, String indexAlias) {
		if (queryPanel != null) {
			remove(queryPanel);
		}
    	final List<? extends FieldConfiguration> fieldConfigurations = connection.fieldConfigurationProvider.getFieldConfigurations(indexAlias);
		queryPanel = new QueryPanel(connection.elasticsearchService, indexSelector.getValue(), fieldConfigurations);
		add(queryPanel);
	}

	private static class ElasticsearchConnection {

		final ElasticsearchService elasticsearchService;
		final FieldConfigurationProvider fieldConfigurationProvider;

		ElasticsearchConnection(ElasticsearchService elasticsearchService, FieldConfigurationProvider fieldConfigurationProvider) {
			this.elasticsearchService = elasticsearchService;
			this.fieldConfigurationProvider = fieldConfigurationProvider;
		}
	}
}
