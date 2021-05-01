package com.hiveworkshop.rms.editor.model.visitor;


import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;

import java.awt.*;
import java.util.ArrayList;

public class GeosetVisitor {
	protected final ArrayList<Point> previousVertices = new ArrayList<>();
	protected ProgramPreferences programPreferences;
	protected CoordinateSystem coordinateSystem;
	protected Graphics2D graphics;
	protected Geoset geoset;
	protected RenderModel renderModel;

	public void beginTriangle(boolean isHD) {
	}

	public void triangleFinished() {
		if (previousVertices.size() > 1) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			Point point = previousVertices.get(0);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
	}
}