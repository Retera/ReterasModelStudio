package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class GeosetOverviewPanel extends JPanel {

	public GeosetOverviewPanel(ModelHandler modelHandler) {
		super(new MigLayout("wrap 3", "[]10[Right]10[Right]", ""));
		add(new JLabel("Geoset"));
		add(new JLabel("Vertices"));
		add(new JLabel("Triangles"));
		int verts = 0;
		int tris = 0;
		for (Geoset geoset : modelHandler.getModel().getGeosets()) {
			int vSize = geoset.getVertices().size();
			verts += vSize;
			int tSize = geoset.getTriangles().size();
			tris += tSize;
			add(new JLabel(geoset.getName()));
			add(new JLabel("" + vSize));
			add(new JLabel("" + tSize));
		}

		add(new JLabel("Total"), "gapy 10");
		add(new JLabel("" + verts));
		add(new JLabel("" + tris));
	}
}
