package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetOverviewPanel extends OverviewPanel {
	private final JPanel infoPanel;
	public GeosetOverviewPanel(ModelHandler modelHandler) {
		super(modelHandler, new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		infoPanel = new JPanel(new MigLayout("wrap 6", "[]10[Right]10[Right]10[Right]20[Left]10[Right]", ""));

		fillInfoPanel();

		add(infoPanel, "growx, growy");
	}

	private void fillInfoPanel() {
		infoPanel.add(new JLabel("Geoset"));
		infoPanel.add(new JLabel("Vertices"));
		infoPanel.add(new JLabel("Triangles"));
		infoPanel.add(new JLabel("LoD"));
		infoPanel.add(new JLabel("Material"));
		infoPanel.add(new JLabel("GeosetAnim"));
		int verts = 0;
		int tris = 0;
		EditableModel model = modelHandler.getModel();
		for (Geoset geoset : model.getGeosets()) {
			int vSize = geoset.getVertices().size();
			verts += vSize;
			int tSize = geoset.getTriangles().size();
			tris += tSize;
			infoPanel.add(new JLabel(geoset.getName()));
			infoPanel.add(new JLabel("" + vSize));
			infoPanel.add(new JLabel("" + tSize));
			infoPanel.add(new JLabel("" + geoset.getLevelOfDetail()));

			infoPanel.add(getMaterialLabel(model, geoset));
			infoPanel.add(new JLabel("" + (geoset.getGeosetAnim() == null ? "no" : "yes")));
		}

		infoPanel.add(new JLabel("Total"), "gapy 10");
		infoPanel.add(new JLabel("" + verts));
		infoPanel.add(new JLabel("" + tris));
	}

	private JLabel getMaterialLabel(EditableModel model, Geoset geoset) {
		Material material = geoset.getMaterial();
		if(material != null){
			return new JLabel("# " + model.computeMaterialID(material) + " " + material.getName());
		} else {
			return new JLabel("# -1 null");
		}
	}

	@Override
	public void update() {
		infoPanel.removeAll();
		fillInfoPanel();
		revalidate();
		repaint();
	}
}
