package com.hiveworkshop.rms.ui.browsers.model;

import java.util.Comparator;

public class ModelComparator implements Comparator<Model> {
	@Override
	public int compare(final Model o1, final Model o2) {
		return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
	}

}
