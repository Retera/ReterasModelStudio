package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetOverviewPanel extends JPanel {

	public GeosetOverviewPanel(ModelHandler modelHandler) {
		super(new MigLayout("fill, ins 0", "[grow]", "[grow]"));
		JPanel panel = new JPanel(new MigLayout("wrap 3", "[]10[Right]10[Right]", ""));

		panel.add(new JLabel("Geoset"));
		panel.add(new JLabel("Vertices"));
		panel.add(new JLabel("Triangles"));
		int verts = 0;
		int tris = 0;
		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			int vSize = geoset.getVertices().size();
			verts += vSize;
			int tSize = geoset.getTriangles().size();
			tris += tSize;
			panel.add(new JLabel(geoset.getName()));
			panel.add(new JLabel("" + vSize));
			panel.add(new JLabel("" + tSize));
		}

		panel.add(new JLabel("Total"), "gapy 10");
		panel.add(new JLabel("" + verts));
		panel.add(new JLabel("" + tris));

		add(panel, "growx, growy");
	}
}
