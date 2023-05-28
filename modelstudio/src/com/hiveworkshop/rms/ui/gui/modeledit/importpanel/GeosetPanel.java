package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.shells.GeosetShell;
import com.hiveworkshop.rms.ui.gui.modeledit.renderers.MaterialListCellRenderer;
import com.hiveworkshop.rms.ui.util.TwiList;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

class GeosetPanel extends JPanel {
	protected TwiList<Material> materialList;
	protected JScrollPane materialListPane;
	protected TriCheckBox doImport;
	protected JLabel geoTitle;
	protected JLabel materialText;
	protected MaterialListCellRenderer renderer;
	protected GeosetShell selectedGeoset;
	protected TwiList<GeosetShell> geosetShellJList;

	public GeosetPanel() {}

	public GeosetPanel(ModelHolderThing mht, TwiList<GeosetShell> geosetShellJList, List<Material> materials) {
		setLayout(new MigLayout("gap 0, fill"));

		this.geosetShellJList = geosetShellJList;
		renderer = new MaterialListCellRenderer(mht.receivingModel);

		geoTitle = new JLabel("Select a geoset");
		geoTitle.setFont(new Font("Arial", Font.BOLD, 26));
		add(geoTitle, "align center, wrap");

		doImport = new TriCheckBox("Import this Geoset");
		doImport.addActionListener(e -> setDoImport(doImport.isSelected()));
		add(doImport, "left, wrap");

		materialText = new JLabel("Material:");
		add(materialText, "left, wrap");
		add(getMaterialListPane(materials, this::setGeosetMaterial), "grow");
	}

	protected JScrollPane getMaterialListPane(List<Material> materials, Consumer<Material> materialConsumer) {
		materialList = new TwiList<>(materials);
		materialList.setCellRenderer(renderer);
		materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		materialList.addSelectionListener1(materialConsumer);
		materialListPane = new JScrollPane(materialList);
		return materialListPane;
	}

	public void setGeoset(GeosetShell geosetShell) {
		selectedGeoset = geosetShell;
		renderer.setSelectedGeoset(geosetShell);

		geoTitle.setText(geosetShell.getModelName() + " " + (geosetShell.getIndex() + 1));

		doImport.setSelected(geosetShell.isDoImport());
		materialText.setEnabled(geosetShell.isDoImport());
		materialList.setEnabled(geosetShell.isDoImport());
		materialListPane.setEnabled(geosetShell.isDoImport());
		repaint();
	}

	private void setGeosetMaterial(Material material) {
		if (material != null) {
			if (material == selectedGeoset.getOldMaterial()) {
				selectedGeoset.setNewMaterial(null);
			} else {
				selectedGeoset.setNewMaterial(material);
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
