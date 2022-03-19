package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MaterialListCellRenderer;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.util.List;

public class MultiGeosetPanel extends GeosetPanel{
	List<GeosetShell> selectedValuesList;
	public MultiGeosetPanel(ModelHolderThing mht, DefaultListModel<Material> materials) {
		setLayout(new MigLayout("gap 0, fill", "[grow]", "[]8[grow]"));
		setLayout(new MigLayout("gap 0"));

		renderer = new MaterialListCellRenderer(mht.receivingModel);

		geoTitle = new JLabel("Select a geoset");
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));
		add(geoTitle, "align center, wrap");

		doImport = new JCheckBox("Import these Geosets");
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

	private void setGeosetMaterial(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting() && materialList.getSelectedValue() != null) {
			for(GeosetShell geosetShell : selectedValuesList){
				if (materialList.getSelectedValue() == geosetShell.getOldMaterial()) {
					geosetShell.setNewMaterial(null);
				} else {
					geosetShell.setNewMaterial(materialList.getSelectedValue());
				}
			}
			materialList.setSelectedValue(null, false);
		}
	}

	public void setGeosets(List<GeosetShell> selectedValuesList) {
		this.selectedValuesList = selectedValuesList;
		renderer.setSelectedGeosets(selectedValuesList);

		geoTitle.setText("multiple Geosets Selected");
		boolean firstShouldImport = selectedValuesList.get(0).isDoImport();
		if (selectedValuesList.stream().anyMatch(objectShell -> objectShell.isDoImport() != firstShouldImport)){
			doImport.setSelected(false);
			materialText.setEnabled(false);
			materialList.setEnabled(false);
			materialListPane.setEnabled(false);
			doImport.setBackground(Color.ORANGE);
		} else {
			doImport.setSelected(firstShouldImport);
			doImport.setBackground(this.getBackground());
		}
		repaint();
	}

	private void setDoImport(boolean doImport) {
		System.out.println("setting multi doImport to: " + doImport);
		materialText.setEnabled(doImport);
		materialList.setEnabled(doImport);
		materialListPane.setEnabled(doImport);

		for(GeosetShell geosetShell : selectedValuesList){
			geosetShell.setDoImport(doImport);
		}
	}
}
