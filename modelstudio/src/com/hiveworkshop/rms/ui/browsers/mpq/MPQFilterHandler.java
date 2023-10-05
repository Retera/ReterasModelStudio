package com.hiveworkshop.rms.ui.browsers.mpq;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

public class MPQFilterHandler {
	private final Map<String, Filter> extensionToFilter = new HashMap<>();
	private final Map<String, Boolean> currentlyFiltered = new HashMap<>();
	private String currentSearch = "";
	private final List<Filter> filters;
	private final Filter otherFilter;
	private final JCheckBoxMenuItem checkAll;
	private final Runnable updater;
	private boolean allowPropagation = true;

	public MPQFilterHandler(Runnable updater) {
		this.updater = updater;
		filters = new ArrayList<>();
		otherFilter = new Filter("Other", true).setSelected(false);

		checkAll = new JCheckBoxMenuItem("All", true);
		checkAll.addActionListener(e -> setFilterAll(checkAll.getState()));
		checkAll.putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);
	}

	public MPQFilterHandler setUseOtherFilter(boolean useOtherFilter) {
		otherFilter.setSelected(useOtherFilter);
		if (useOtherFilter) {
			filters.add(otherFilter);
		}
		return this;
	}

	public MPQFilterHandler setUseFilterAll(boolean useFilterAll) {
		allowPropagation = false;
		checkAll.setSelected(useFilterAll);
		allowPropagation = true;
		return this;
	}

	public MPQFilterHandler addFilter(String name, String... extensions) {
		Filter filter = new Filter(name, extensions);
		filters.add(filter);
		for (String ext : filter.getExtensions()) {
			extensionToFilter.put(ext, filter);
			currentlyFiltered.put(ext, filter.isSelected());
		}
		return this;
	}

	public JMenu getSearchMenu() {
		JMenu searchMenu = new JMenu("Search");
		searchMenu.putClientProperty("Menu.doNotCloseOnMouseExited", false);
		searchMenu.getPopupMenu().setLayout(new MigLayout());

		JTextField searchField = new JTextField();
		Dimension prefSize = searchField.getPreferredSize();
		prefSize.width = 100;
		searchField.setMinimumSize(prefSize);
		searchField.setPreferredSize(prefSize);
		searchField.addKeyListener(getSearchOnEnter(searchField));
		searchMenu.getPopupMenu().add(searchField, "growx, span 2, wrap");

		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(e -> searchFilter(searchField.getText()));
		searchMenu.getPopupMenu().add(searchButton, "growx");

		JButton clearButton = new JButton("Clear");
		clearButton.addActionListener(e -> {
			searchFilter("");
			searchField.setText("");
		});
		searchMenu.getPopupMenu().add(clearButton, "growx");
		return searchMenu;
	}

	public KeyAdapter getSearchOnEnter(JTextField searchField) {
		return new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				System.out.println("keyCode: " + e.getKeyCode());
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchFilter(searchField.getText());
				}
			}
		};
	}

	public JMenu getFilterMenu() {
		JMenu filtersMenu = new JMenu("Filters");
		filtersMenu.putClientProperty("Menu.doNotCloseOnMouseExited", false);
		for (Filter filter : filters) {
			filtersMenu.add(filter.getFilterCheckBoxItem());
			filter.addActionListener(e -> setFilteredExtensionsAndUpdate(filter.getExtensions(), filter.getFilterState()));

			filter.getFilterCheckBoxItem().putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", true);

			for (String ext : filter.getExtensions()) {
				extensionToFilter.put(ext, filter);
				currentlyFiltered.put(ext, filter.isSelected());
			}
		}
		filtersMenu.add(checkAll);
		return filtersMenu;
	}


	// Set the all the filter checkboxes to the value of the "All" checkbox and updates the filter map
	private void setFilterAll(boolean b) {
		if (allowPropagation) {
			System.out.println("filtering all: " + b);
			for (Filter filter : filters) {
				filter.getFilterCheckBoxItem().setSelected(b);
				setFilteredExtensions(filter.getExtensions(), b);
			}
		}
		updater.run();
	}


	// Updates the map used to look up filtered extensions,
	// sets the "All" checkbox to match if all checkboxes
	// and checked and refreshes the tree
	public void setFilteredExtensionsAndUpdate(String[] extensions, boolean filtered) {
		System.out.println("filtering : " + filtered + " - " + Arrays.toString(extensions));
		setFilteredExtensions(extensions, filtered);
		updater.run();
	}

	private void setFilteredExtensions(String[] extensions, boolean filtered) {
		for (String extension : extensions) {
			currentlyFiltered.put(extension, filtered);
		}
		checkAll.setState(!currentlyFiltered.containsValue(false));
	}

	private void searchFilter(String searchText) {
		currentSearch = searchText;
		updater.run();
	}

	public boolean isFiltered(MPQTreeNode node) {
		Boolean filtered = currentlyFiltered.get(node.getExtension());
		boolean fitsSearch = currentSearch.equals("")
				|| (node.isLeaf()
					&& node.getSubPathName().toLowerCase(Locale.ROOT)
					.contains(currentSearch.toLowerCase(Locale.ROOT)));
		return filtered != null && filtered && fitsSearch
				|| filtered == null && otherFilter.isSelected() && fitsSearch;
	}

	public boolean isFiltered1(String extension) {
		Boolean isCurrentlyFiltered = currentlyFiltered.get(extension);

		return (isCurrentlyFiltered == null && !otherFilter.isSelected())
				|| (isCurrentlyFiltered != null && !isCurrentlyFiltered);
	}

	public boolean isFiltered(String extension) {
		Boolean isCurrentlyFiltered = currentlyFiltered.get(extension);
		if (isCurrentlyFiltered == null) {
			return !otherFilter.isSelected();
		} else {
			return !isCurrentlyFiltered;
		}
	}

	public boolean shouldBeVisible(String extension) {
		Boolean isCurrentlyFiltered = currentlyFiltered.getOrDefault(extension, otherFilter.isSelected());
		return Objects.requireNonNullElseGet(isCurrentlyFiltered, () -> otherFilter.isSelected());
	}

	public boolean shouldBeVisible1(String extension) {
		Boolean isCurrentlyFiltered = currentlyFiltered.get(extension);

		boolean isOtherAndHidden = isCurrentlyFiltered == null && !otherFilter.isSelected();
		boolean isExtAndHidden = isCurrentlyFiltered != null && !isCurrentlyFiltered;
		return !(isOtherAndHidden || isExtAndHidden);
	}

	public boolean shouldBeVisible2(String extension) {
		Boolean isCurrentlyFiltered = currentlyFiltered.get(extension);

		boolean isOtherAndVis = isCurrentlyFiltered == null && otherFilter.isSelected();
		boolean isExtAndVis = isCurrentlyFiltered != null && isCurrentlyFiltered;

		boolean shouldBeVis = isOtherAndVis || isExtAndVis;

		boolean isOtherAndHidden = isCurrentlyFiltered == null && !otherFilter.isSelected();
		boolean isExtAndHidden = isCurrentlyFiltered != null && !isCurrentlyFiltered;
		return !(isOtherAndHidden || isExtAndHidden);
	}

}
