package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Material;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

class GeosetPanel extends JPanel {
	private JList<Material> materialList;
	private JScrollPane materialListPane;
	private JCheckBox doImport;
	private JLabel geoTitle;
	private JLabel materialText;
	private MaterialListCellRenderer renderer;
	private ModelHolderThing mht;
	private GeosetShell selectedGeoset;

	public GeosetPanel(ModelHolderThing mht, DefaultListModel<Material> materials) {
		this.mht = mht;
		setLayout(new MigLayout("gap 0"));
		// Geoset/Skin panel for controlling materials and geosets
		renderer = new MaterialListCellRenderer(mht.receivingModel);
		;

		geoTitle = new JLabel("Select a geoset");
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));
		add(geoTitle, "align center, wrap");

		doImport = new JCheckBox("Import this Geoset");
		doImport.setSelected(true);
		doImport.addActionListener(e -> checkboxToggeled());
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

		//Todo change geosetTabs to a JList and remove this
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent e) {
				super.componentShown(e);
				setGeoset(selectedGeoset);
			}
		});
	}

	public void setGeoset(GeosetShell geosetShell) {
		selectedGeoset = geosetShell;
		renderer.setSelectedGeoset(geosetShell);
		EditableModel model = geosetShell.getModel();
		int index = geosetShell.getIndex();

		geoTitle.setText(model.getName() + " " + (index + 1));

//		doImport.setEnabled(geosetShell.isFromDonating());
		if (geosetShell.isFromDonating()) {
			doImport.setSelected(geosetShell.isDoImport());
		}
		materialList.setSelectedValue(selectedGeoset.getMaterial(), true);
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

	private void checkboxToggeled() {
		materialText.setEnabled(doImport.isSelected());
		materialList.setEnabled(doImport.isSelected());
		materialListPane.setEnabled(doImport.isSelected());

		if (selectedGeoset != null && selectedGeoset.isFromDonating()) {
			selectedGeoset.setDoImport(doImport.isSelected());
		}
	}
}
