package de.picturesafe.search.querygenerator.views.main;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import de.picturesafe.search.elasticsearch.ElasticsearchService;
import de.picturesafe.search.elasticsearch.FieldConfigurationProvider;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class QueryPanel extends VerticalLayout {

    private final ElasticsearchService elasticsearchService;
    private final FieldConfigurationProvider fieldConfigurationProvider;

	private final List<FieldPanel> fieldPanels = new ArrayList<>();
    private List<? extends FieldConfiguration> fieldConfigurations;

    public QueryPanel(ElasticsearchService elasticsearchService, FieldConfigurationProvider fieldConfigurationProvider) {
        this.elasticsearchService = elasticsearchService;
        this.fieldConfigurationProvider = fieldConfigurationProvider;
        setPadding(false);
		add(indexSelector());
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

    	final Select<String> indexSelector = new Select<>();
    	indexSelector.setItems(indexNames);
		indexSelector.setLabel("Index/Alias");
		indexSelector.setWidth("300px");
		indexSelector.addValueChangeListener(this::selectIndex);
		indexSelector.focus();
		return indexSelector;
	}

    private void selectIndex(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
    	fieldPanels.forEach(this::remove);
    	fieldPanels.clear();

    	fieldConfigurations = fieldConfigurationProvider.getFieldConfigurations(event.getValue());
		final FieldPanel fieldPanel = new FieldPanel(fieldConfigurations);
		add(fieldPanel);
		fieldPanels.add(fieldPanel);
	}
}
