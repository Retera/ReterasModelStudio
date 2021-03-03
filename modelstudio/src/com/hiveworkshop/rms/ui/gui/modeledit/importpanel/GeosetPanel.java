package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

class GeosetPanel extends JPanel implements ChangeListener {
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
	ImportPanel impPanel;

	public GeosetPanel(final boolean imported, // Is this Geoset an imported one, or an original?
	                   final EditableModel model, final int geoIndex, // which geoset is this for? (starts with 0)
	                   final DefaultListModel<Material> materials, final MaterialListCellRenderer renderer) {
		setLayout(new MigLayout("gap 0"));
		this.materials = materials;
		this.model = model;
		this.renderer = renderer;
		index = geoIndex;
		geoset = model.getGeoset(geoIndex);
		isImported = imported;

		geoTitle = new JLabel(model.getName() + " " + (index + 1));
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));
		add(geoTitle, "align center, wrap");

		doImport = new JCheckBox("Import this Geoset");
		doImport.setSelected(true);
		if (imported) {
			doImport.addChangeListener(this);
		} else {
			doImport.setEnabled(false);
		}
		add(doImport, "left, wrap");

		materialText = new JLabel("Material:");
		add(materialText, "left, wrap");
		// Header for materials list

		materialList = new JList<>(materials);
		materialList.setCellRenderer(renderer);
		materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		materialList.setSelectedValue(geoset.getMaterial(), true);

		materialListPane = new JScrollPane(materialList);
		add(materialListPane, "grow");
	}

	@Override
	public void paintComponent(final Graphics g) {
		renderer.setMaterial(geoset.getMaterial());
		super.paintComponent(g);
	}

	public void setSelected(final boolean flag) {
		if (isImported) {
			doImport.setSelected(flag);
		}
	}

	@Override
	public void stateChanged(final ChangeEvent e) {
		materialText.setEnabled(doImport.isSelected());
		materialList.setEnabled(doImport.isSelected());
		materialListPane.setEnabled(doImport.isSelected());

		getImportPanel().informGeosetVisibility(geoset, doImport.isSelected());
	}

	public ImportPanel getImportPanel() {
		if (impPanel == null) {
			Container temp = getParent();
			while ((temp != null) && (temp.getClass() != ImportPanel.class)) {
				temp = temp.getParent();
			}
			impPanel = (ImportPanel) temp;
		}
		return impPanel;
	}

	public Material getSelectedMaterial() {
		return (Material) materialList.getSelectedValue();
	}
}
