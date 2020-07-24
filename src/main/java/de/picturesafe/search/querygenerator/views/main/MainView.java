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
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {

	private final TextField addressField;
	private final Button connectButton;
	private QueryPanel queryPanel;

	public MainView() {
		addressField = new TextField();
		addressField.setLabel("Elasticsearch address");
		addressField.setPlaceholder("host:port");
		addressField.setWidth("300px");
		addressField.setRequired(true);

		connectButton = new Button();
		connectButton.setText("connect");
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
		queryPanel = new QueryPanel(addressField.getValue());
		add(queryPanel);
	}
}
