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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import de.picturesafe.search.elasticsearch.config.ElasticsearchType;
import de.picturesafe.search.elasticsearch.config.FieldConfiguration;
import de.picturesafe.search.parameter.SortOption;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.COMPLETION;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.NESTED;
import static de.picturesafe.search.elasticsearch.config.ElasticsearchType.OBJECT;
import static de.picturesafe.search.querygenerator.views.main.util.FieldConfigurationUtils.elasticType;

public class SortOptionPanel extends HorizontalLayout implements QueryLayout {

    private final static Set<ElasticsearchType> UNSUPPORTED_ELASTIC_TYPES = EnumSet.of(NESTED, OBJECT, COMPLETION);
    private static final String RELEVANCE_LABEL = "<Relevance>";

    private final Select<String> sortSelector;
    private Select<SortOption.Direction> directionSelector;

    public SortOptionPanel(List<? extends FieldConfiguration> fieldConfigurations) {
        final List<String> sortNames = fieldConfigurations.stream().filter(this::isSupported).map(FieldConfiguration::getName).sorted()
                .collect(Collectors.toList());
        sortNames.add(0, "");
        sortNames.add(RELEVANCE_LABEL);
        sortSelector = new Select<>();
        sortSelector.setItems(sortNames);
        sortSelector.setLabel("Sort");
        sortSelector.setWidth(FIELD_WIDTH);
        sortSelector.addValueChangeListener(this::selectSort);
        add(sortSelector);
    }

    private boolean isSupported(FieldConfiguration fieldConfiguration) {
        return !UNSUPPORTED_ELASTIC_TYPES.contains(elasticType(fieldConfiguration)) && fieldConfiguration.isSortable();
    }

    private void selectSort(AbstractField.ComponentValueChangeEvent<Select<String>, String> event) {
        final String sortValue = event.getValue();
        if (sortValue.isEmpty() || sortValue.equals(RELEVANCE_LABEL)) {
            if (directionSelector != null) {
                remove(directionSelector);
                directionSelector = null;
            }
        } else if (directionSelector == null) {
            directionSelector = new Select<>(SortOption.Direction.ASC, SortOption.Direction.DESC);
            directionSelector.setLabel("Direction");
            directionSelector.setWidth("88px");
            directionSelector.setValue(SortOption.Direction.ASC);
            add(directionSelector);
        }
    }

    public SortOption getSortOption() {
        final String sortValue = sortSelector.getValue();
        if (StringUtils.isEmpty(sortValue)) {
            return null;
        } else if (sortValue.equals(RELEVANCE_LABEL)) {
            return SortOption.relevance();
        } else {
            return (directionSelector.getValue() == SortOption.Direction.ASC) ? SortOption.asc(sortValue) : SortOption.desc(sortValue);
        }
    }
}
