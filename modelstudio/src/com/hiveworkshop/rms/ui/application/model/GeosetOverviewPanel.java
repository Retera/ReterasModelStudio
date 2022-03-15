package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetOverviewPanel extends JPanel {

	public GeosetOverviewPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		JPanel panel = new JPanel(new MigLayout("wrap 6", "[]10[Right]10[Right]10[Right]20[Left]10[Right]", ""));

		panel.add(new JLabel("Geoset"));
		panel.add(new JLabel("Vertices"));
		panel.add(new JLabel("Triangles"));
		panel.add(new JLabel("LoD"));
		panel.add(new JLabel("Material"));
		panel.add(new JLabel("GeosetAnim"));
		int verts = 0;
		int tris = 0;
		EditableModel model = modelHandler.getModel();
		for (Geoset geoset : model.getGeosets()) {
			int vSize = geoset.getVertices().size();
			verts += vSize;
			int tSize = geoset.getTriangles().size();
			tris += tSize;
			panel.add(new JLabel(geoset.getName()));
			panel.add(new JLabel("" + vSize));
			panel.add(new JLabel("" + tSize));
			panel.add(new JLabel("" + geoset.getLevelOfDetail()));

			panel.add(getMaterialLabel(model, geoset));
			panel.add(new JLabel("" + (geoset.getGeosetAnim() == null ? "no" : "yes")));
		}

		panel.add(new JLabel("Total"), "gapy 10");
		panel.add(new JLabel("" + verts));
		panel.add(new JLabel("" + tris));

		add(panel, "growx, growy");
	}

	private JLabel getMaterialLabel(EditableModel model, Geoset geoset) {
		Material material = geoset.getMaterial();
		if(material != null){
			return new JLabel("# " + model.computeMaterialID(material) + " " + material.getName());
		} else {
			return new JLabel("# -1 null");
		}
	}
}
