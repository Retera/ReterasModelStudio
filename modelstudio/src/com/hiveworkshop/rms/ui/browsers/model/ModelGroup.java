package com.hiveworkshop.rms.ui.browsers.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ModelGroup {
	private final String name;
	private final List<Model> models = new ArrayList<>();

	public ModelGroup(final String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	public List<Model> getModels() {
		return models;
	}

	public ModelGroup addModel(Model model) {
		models.add(model);
		return this;
	}

	public ModelGroup sortModels() {
		Collections.sort(models);
		return this;
	}

	public ModelGroup fill(Map<String, NamedList<String>> modelData) {
		for (String str : modelData.keySet()) {
			NamedList<String> unitList = modelData.get(str);
			String displayName = getDisplayName(unitList);
			Model nextModel = new Model()
					.setDisplayName(displayName)
					.setFilepath(unitList.getName())
					.setCachedIcon(unitList.getCachedIconPath());

			addModel(nextModel);
		}
		sortModels();
		return this;
	}

	private String getDisplayName(NamedList<String> unitList) {
		StringBuilder nameOutput = new StringBuilder();
		for (String unitName : unitList) {
			if (nameOutput.length() > 0) {
				nameOutput.append(", ");
			}
			if ((nameOutput.length() + unitName.length()) > 120) {
				nameOutput.append("...");
				break;
			} else {
				nameOutput.append(unitName);
			}
		}
		return nameOutput.toString();
	}
}
