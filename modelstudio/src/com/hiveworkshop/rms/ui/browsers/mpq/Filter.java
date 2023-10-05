package com.hiveworkshop.rms.ui.browsers.mpq;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;

public class Filter {
	private final String[] extensions;
	private final String name;
	private final JCheckBoxMenuItem filterCheckBoxItem;
	private boolean isOtherFilter;

	public Filter(String name, String[] extensions) {
		this.name = name;
		this.extensions = extensions;
		filterCheckBoxItem = new JCheckBoxMenuItem(getDescription(), true);
	}

	public Filter(String name, boolean isOtherFilter) {
		this.name = name;
		this.isOtherFilter = isOtherFilter;
		extensions = new String[] {};
		filterCheckBoxItem = new JCheckBoxMenuItem(name, true);
	}

	public String[] getExtensions() {
		return extensions;
	}

	public boolean getFilterState() {
		return filterCheckBoxItem.getState();
	}

	public boolean passes(String path) {
		for (String extension : extensions) {
			if (path.endsWith(extension)) {
				return true;
			}
		}
		return false;
	}

	public boolean isOtherFilter() {
		return isOtherFilter;
	}

	public String getDescription() {
		StringBuilder descBuilder = new StringBuilder(name);
		descBuilder.append(" (");
		if (extensions.length > 0) {
			descBuilder.append("*");
			descBuilder.append(extensions[0]);
			for (int i = 1; i < extensions.length; i++) {
				descBuilder.append(", *");
				descBuilder.append(extensions[i]);
			}
		}
		descBuilder.append(")");
		return descBuilder.toString();
	}

	public void addItemListener(ItemListener itemListener) {
		filterCheckBoxItem.addItemListener(itemListener);
	}

	public void addActionListener(ActionListener listener) {
		filterCheckBoxItem.addActionListener(listener);
	}

	public JCheckBoxMenuItem getFilterCheckBoxItem() {
		return filterCheckBoxItem;
	}

	public Filter setSelected(boolean selected) {
		filterCheckBoxItem.setSelected(selected);
		return this;
	}

	public boolean isSelected() {
		return filterCheckBoxItem.isSelected();
	}
}
