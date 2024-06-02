package com.hiveworkshop.rms.ui.application.tools.uielement;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.GeosetShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.GeosetListCellRenderer2D;
import com.hiveworkshop.rms.ui.util.SearchableList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GeosetChooser {
	private final List<GeosetShell> selectedGeosetShells = new ArrayList<>();
	private final EditableModel model;

	public GeosetChooser(EditableModel model) {
		this.model = model;
	}

	public Geoset chooseGeoset(Geoset currentGeoset, JComponent parent) {
		SearchableList<GeosetShell> geosetJList = getGeosetJList(currentGeoset);

		JPanel geosetChooserPanel = geosetChooserPanel(geosetJList);

		int option = JOptionPane.showConfirmDialog(parent, geosetChooserPanel, "Choose Geoset", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			GeosetShell selectedValue = geosetJList.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getGeoset();
			}
		}
		return currentGeoset;
	}

	private SearchableList<GeosetShell> getGeosetJList(Geoset currentGeoset) {
		GeosetListCellRenderer2D renderer = new GeosetListCellRenderer2D(model, null);
		renderer.setSelectionList(selectedGeosetShells);

		SearchableList<GeosetShell> geosetJList = new SearchableList<>(this::geosetNameFilter);
		geosetJList.setCellRenderer(renderer);

		GeosetShell currentBoneShell = null;
		geosetJList.add(new GeosetShell(null, model, false).setName("none"));
		for (Geoset geoset : model.getGeosets()) {
			GeosetShell geosetShell = new GeosetShell(geoset, model, false);
			geosetJList.add(geosetShell);
			if (geoset == currentGeoset) {
				currentBoneShell = geosetShell;
			}
		}
		geosetJList.setSelectedValue(currentBoneShell, true);
		return geosetJList;
	}

	public List<Geoset> chooseGeosets(List<Geoset> currentGeosets, JComponent parent) {
		selectedGeosetShells.clear();

		SearchableList<GeosetShell> geosetJList = getGeosetJList(currentGeosets);

		JPanel geosetChooserPanel = geosetChooserPanel(geosetJList);

		int option = JOptionPane.showConfirmDialog(parent, geosetChooserPanel, "Choose Geoset(s)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			return selectedGeosetShells.stream()
					.filter(Objects::nonNull)
					.map(GeosetShell::getGeoset)
					.collect(Collectors.toList());
		}
		return currentGeosets;
	}

	private SearchableList<GeosetShell> getGeosetJList(List<Geoset> currentGeosets) {
		GeosetListCellRenderer2D renderer = new GeosetListCellRenderer2D(model, null);
		renderer.setSelectionList(selectedGeosetShells);

		SearchableList<GeosetShell> geosetJList = new SearchableList<>(this::geosetNameFilter);
		geosetJList.setCellRenderer(renderer);

		for (Geoset geoset : model.getGeosets()) {
			GeosetShell geosetShell = new GeosetShell(geoset, model, false);
			geosetJList.add(geosetShell);
			if (currentGeosets.contains(geoset)) {
				selectedGeosetShells.add(geosetShell);
			}
		}
		geosetJList.addSelectionListener(e -> selectGeoset(e, geosetJList));
		return geosetJList;
	}

	private void selectGeoset(ListSelectionEvent e, SearchableList<GeosetShell> geosetJList) {
		if (e.getValueIsAdjusting()) {
			GeosetShell selectedValue = geosetJList.getSelectedValue();
			if (selectedGeosetShells.contains(selectedValue)) {
				selectedGeosetShells.remove(selectedValue);
			} else if (selectedValue != null) {
				selectedGeosetShells.add(selectedValue);
			}
			geosetJList.setSelectedValue(null, false);
		}
	}

	private JPanel geosetChooserPanel(SearchableList<GeosetShell> geosetJList) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		panel.add(geosetJList.getSearchField(), "growx, wrap");
		panel.add(new JScrollPane(geosetJList), "growx, growy, wrap");
		return panel;
	}

	private boolean geosetNameFilter(GeosetShell geosetShell, String filterText) {
		return geosetShell != null && geosetShell.getName() != null && geosetShell.getName().toLowerCase().contains(filterText.toLowerCase());
	}
}
