package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.GeosetShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.GeosetListCellRenderer2D;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class GeosetChooser {
	private final IterableListModel<GeosetShell> filteredGeosets = new IterableListModel<>();
	private final IterableListModel<GeosetShell> allGeosetsList = new IterableListModel<>();

	private final List<GeosetShell> selectedGeosetShells = new ArrayList<>();

	private final EditableModel model;

	public GeosetChooser(EditableModel model) {
		this.model = model;
	}

	public Geoset chooseGeoset(Geoset currentGeoset, JComponent parent) {
		GeosetShell currentBoneShell = null;
		allGeosetsList.clear();
		allGeosetsList.addElement(new GeosetShell(null, model, false));
		for (Geoset geoset : model.getGeosets()) {
			GeosetShell geosetShell = new GeosetShell(geoset, model, false);
			allGeosetsList.addElement(geosetShell);
			if (geoset == currentGeoset) {
				currentBoneShell = geosetShell;
			}
		}

		JList<GeosetShell> geosetJList = getGeosetShellJList();
		JPanel geosetChooserPanel = geosetChooserPanel(geosetJList);
		geosetJList.setSelectedValue(currentBoneShell, true);

		int option = JOptionPane.showConfirmDialog(parent, geosetChooserPanel, "Choose Geoset", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			GeosetShell selectedValue = geosetJList.getSelectedValue();
			if (selectedValue != null) {
				return selectedValue.getGeoset();
			}
		}
		return currentGeoset;
	}

	public List<Geoset> chooseGeosets(List<Geoset> currentGeosets, JComponent parent) {
		allGeosetsList.clear();
		selectedGeosetShells.clear();

		for (Geoset geoset : model.getGeosets()) {
			GeosetShell geosetShell = new GeosetShell(geoset, model, false);
			allGeosetsList.addElement(geosetShell);
			if (currentGeosets.contains(geoset)) {
				selectedGeosetShells.add(geosetShell);
			}
		}

		JList<GeosetShell> geosetJList = getGeosetShellJList();
		JPanel geosetChooserPanel = geosetChooserPanel(geosetJList);
		geosetJList.addListSelectionListener(e -> selectGeoset(e, geosetJList));

		int option = JOptionPane.showConfirmDialog(parent, geosetChooserPanel, "Choose Geoset(s)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null);
		if (option == JOptionPane.OK_OPTION) {
			return selectedGeosetShells.stream()
					.filter(Objects::nonNull)
					.map(GeosetShell::getGeoset)
					.collect(Collectors.toList());
		}
		return currentGeosets;
	}

	private void selectGeoset(ListSelectionEvent e, JList<GeosetShell> geosetJList){
		if(e.getValueIsAdjusting()){
			GeosetShell selectedValue = geosetJList.getSelectedValue();
			if(selectedGeosetShells.contains(selectedValue)){
				selectedGeosetShells.remove(selectedValue);
			} else if(selectedValue != null){
				selectedGeosetShells.add(selectedValue);
			}
			geosetJList.setSelectedValue(null, false);
		}
	}

	private JPanel geosetChooserPanel(JList<GeosetShell> geosetJList) {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		JTextField geosetSearch = new JTextField();
		geosetSearch.addCaretListener(e -> filterGeosets(geosetSearch, geosetJList));
		panel.add(geosetSearch, "growx, wrap");

		panel.add(new JScrollPane(geosetJList), "growx, growy, wrap");
		return panel;
	}

	private JList<GeosetShell> getGeosetShellJList() {
		GeosetListCellRenderer2D renderer = new GeosetListCellRenderer2D(model, null);
		renderer.setSelectionList(selectedGeosetShells);

		JList<GeosetShell> geosetJList = new JList<>(allGeosetsList);
		geosetJList.setCellRenderer(renderer);
		return geosetJList;
	}

	private void filterGeosets(JTextField geosetSearch, JList<GeosetShell> geosetJList) {
		String filterText = geosetSearch.getText();
		if (!filterText.equals("")) {
			filteredGeosets.clear();
			for (GeosetShell geosetShell : allGeosetsList) {
				if (geosetShell.getName().toLowerCase().contains(filterText.toLowerCase())) {
					filteredGeosets.addElement(geosetShell);
				}
			}
			geosetJList.setModel(filteredGeosets);
		} else {
			geosetJList.setModel(allGeosetsList);
		}
	}
}
