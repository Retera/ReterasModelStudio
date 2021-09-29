package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MaterialListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

class GeosetPanel extends JPanel {
	protected JList<Material> materialList;
	protected JScrollPane materialListPane;
	protected JCheckBox doImport;
	protected JLabel geoTitle;
	protected JLabel materialText;
	protected MaterialListCellRenderer renderer;
	protected GeosetShell selectedGeoset;

	public GeosetPanel() {
	}

	public GeosetPanel(ModelHolderThing mht, DefaultListModel<Material> materials) {
		setLayout(new MigLayout("gap 0"));

		renderer = new MaterialListCellRenderer(mht.receivingModel);

		geoTitle = new JLabel("Select a geoset");
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));
		add(geoTitle, "align center, wrap");

		doImport = new JCheckBox("Import this Geoset");
		doImport.addActionListener(e -> setDoImport(doImport.isSelected()));
		add(doImport, "left, wrap");

		materialText = new JLabel("Material:");
		add(materialText, "left, wrap");
		add(getMaterialListPane(materials), "grow");
	}

	private JScrollPane getMaterialListPane(DefaultListModel<Material> materials) {
		materialList = new JList<>(materials);
		materialList.setCellRenderer(renderer);
		materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		materialList.addListSelectionListener(this::setGeosetMaterial);
		materialListPane = new JScrollPane(materialList);
		return materialListPane;
	}

	public void setGeoset(GeosetShell geosetShell) {
		selectedGeoset = geosetShell;
		renderer.setSelectedGeoset(geosetShell);

		geoTitle.setText(geosetShell.getModel().getName() + " " + (geosetShell.getIndex() + 1));
//		setDoImport(geosetShell.isDoImport());
		doImport.setSelected(geosetShell.isDoImport());
//		materialList.setSelectedValue(selectedGeoset.getMaterial(), true);
		repaint();
	}

	private void setGeosetMaterial(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && materialList.getSelectedValue() != null) {
			if (materialList.getSelectedValue() == selectedGeoset.getOldMaterial()) {
				selectedGeoset.setNewMaterial(null);
			} else {
				selectedGeoset.setNewMaterial(materialList.getSelectedValue());
			}
		}
	}

	private void setDoImport(boolean selected) {
		System.out.println("setting single doImport to: " + doImport);
		materialText.setEnabled(selected);
		materialList.setEnabled(selected);
		materialListPane.setEnabled(selected);

		if (selectedGeoset != null) {
			selectedGeoset.setDoImport(selected);
		}
	}
}
