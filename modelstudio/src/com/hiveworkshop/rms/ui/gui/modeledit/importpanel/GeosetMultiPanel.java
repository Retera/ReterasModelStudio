package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MaterialListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GeosetMultiPanel extends GeosetPanel{
	List<GeosetShell> selectedValuesList;
	public GeosetMultiPanel(ModelHolderThing mht, TwiList<GeosetShell> geosetShellJList, List<Material> materials) {
		setLayout(new MigLayout("gap 0, fill"));
		this.geosetShellJList = geosetShellJList;

		renderer = new MaterialListCellRenderer(mht.receivingModel);

		geoTitle = new JLabel("Select a geoset");
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));
		add(geoTitle, "align center, wrap");

		doImport = new TriCheckBox("Import these Geosets");
		doImport.addActionListener(e -> setDoImport(doImport.isSelected()));
		add(doImport, "left, wrap");

		materialText = new JLabel("Material:");
		add(materialText, "left, wrap");
		add(getMaterialListPane(materials, this::setGeosetMaterial), "grow");
	}

	private void setGeosetMaterial(Material material) {
		if (material != null) {
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

		geoTitle.setText("Multiple Geosets Selected");
		boolean firstShouldImport = selectedValuesList.get(0).isDoImport();
		if (selectedValuesList.stream().anyMatch(objectShell -> objectShell.isDoImport() != firstShouldImport)){
			doImport.setIndeterminate(true);
			materialText.setEnabled(false);
			materialList.setEnabled(false);
			materialListPane.setEnabled(false);
		} else {
			doImport.setSelected(firstShouldImport);
			materialText.setEnabled(firstShouldImport);
			materialList.setEnabled(firstShouldImport);
		}
		repaint();
	}

	private void setDoImport(boolean doImport) {
		if(selectedValuesList != null){
			materialText.setEnabled(doImport);
			materialList.setEnabled(doImport);
			materialListPane.setEnabled(doImport);

			for(GeosetShell geosetShell : selectedValuesList){
				geosetShell.setDoImport(doImport);
			}
			geosetShellJList.repaint();
		}
	}
}
