package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.GeosetShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.GeosetListCellRenderer2D;
import com.hiveworkshop.rms.util.IterableListModel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetChooser {
	IterableListModel<GeosetShell> filteredGeosets = new IterableListModel<>();
	IterableListModel<GeosetShell> allGeosetsList;
	JList<GeosetShell> geosetJList;
	JTextField geosetSearch;

	EditableModel model;

	public GeosetChooser(EditableModel model) {
		this.model = model;
		allGeosetsList = new IterableListModel<>();
	}

	public Geoset chooseBone(Geoset currentGeoset, JComponent parent) {
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

		JPanel geosetChooserPanel = geosetChooserPanel();
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

	private JPanel geosetChooserPanel() {
		JPanel panel = new JPanel(new MigLayout("fill, gap 0", "[grow]", "[][][grow]"));

		GeosetListCellRenderer2D renderer = new GeosetListCellRenderer2D(model, null);

		geosetSearch = new JTextField();
		geosetSearch.addCaretListener(e -> filterGeosets());
		panel.add(geosetSearch, "growx, wrap");

		geosetJList = new JList<>(allGeosetsList);
		geosetJList.setCellRenderer(renderer);

		panel.add(new JScrollPane(geosetJList), "growx, growy, wrap");
		return panel;
	}

	private void filterGeosets() {
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
