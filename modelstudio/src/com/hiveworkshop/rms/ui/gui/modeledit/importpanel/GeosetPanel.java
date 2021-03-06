package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

class GeosetPanel extends JPanel {
	// Geoset/Skin panel for controlling materials and geosets
	DefaultListModel<Material> materials;
	JList<Material> materialList;
	JScrollPane materialListPane;
	JCheckBox doImport;
	JLabel geoTitle;
	JLabel materialText;
	EditableModel model;
	Geoset geoset;
	int index;
	boolean isImported;
	MaterialListCellRenderer renderer;
	ModelHolderThing mht;
	GeosetShell selectedGeoset;

	public GeosetPanel(ModelHolderThing mht, DefaultListModel<Material> materials, MaterialListCellRenderer renderer) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		this.materials = materials;
		this.renderer = renderer;

		geoTitle = new JLabel("Select a geoset");
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));
		add(geoTitle, "align center, wrap");

		doImport = new JCheckBox("Import this Geoset");
		doImport.setSelected(true);
		doImport.addChangeListener(e -> checkboxToggeled());
		add(doImport, "left, wrap");

		materialText = new JLabel("Material:");
		add(materialText, "left, wrap");
		// Header for materials list

		materialList = new JList<>(materials);
		materialList.setCellRenderer(renderer);
		materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		materialList.addListSelectionListener(this::setGeosetMaterial);


		materialListPane = new JScrollPane(materialList);
		add(materialListPane, "grow");
	}

	public void setGeoset(GeosetShell geosetShell) {
		selectedGeoset = geosetShell;
		geoset = geosetShell.getGeoset();
		this.model = geosetShell.getModel();
		index = geosetShell.getIndex();
		isImported = geosetShell.isImported();

		geoTitle.setText(model.getName() + " " + (index + 1));

		doImport.setEnabled(geosetShell.isImported());
		if (geosetShell.isImported()) {
			doImport.setSelected(geosetShell.isDoImport());
		}
		materialList.setSelectedValue(geoset.getMaterial(), true);
	}

	@Override
	public void paintComponent(final Graphics g) {
		renderer.setMaterial(geoset.getMaterial());
		super.paintComponent(g);
	}

	private void setGeosetMaterial(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && materialList.getSelectedValue() != null) {
			if (materialList.getSelectedValue() == selectedGeoset.getNewMaterial()) {
				selectedGeoset.setNewMaterial(null);
			} else {
				selectedGeoset.setNewMaterial(materialList.getSelectedValue());
			}
		}
	}

	private void checkboxToggeled() {
		materialText.setEnabled(doImport.isSelected());
		materialList.setEnabled(doImport.isSelected());
		materialListPane.setEnabled(doImport.isSelected());

		if (selectedGeoset != null && selectedGeoset.isImported()) {
			selectedGeoset.setDoImport(doImport.isSelected());
		}

		informGeosetVisibility(geoset, doImport.isSelected());
	}


	public void informGeosetVisibility(final Geoset g, final boolean flag) {
		for (int i = 0; i < mht.geosetAnimTabs.getTabCount(); i++) {
			final BoneAttachmentPanel geoPanel = (BoneAttachmentPanel) mht.geosetAnimTabs.getComponentAt(i);
			if (geoPanel.geoset == g) {
				mht.geosetAnimTabs.setEnabledAt(i, flag);
			}
		}
	}
}
