package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.util.ArrayList;

//public class UVVPGeosetRendererImpl extends GeosetVisitor {
public class UVVPGeosetRendererImpl {
	protected final ArrayList<Point> previousVertices = new ArrayList<>();
	protected ProgramPreferences programPreferences;
	protected CoordinateSystem coordinateSystem;
	protected Graphics2D graphics;
	protected Geoset geoset;
	protected RenderModel renderModel;
	private Vec2[] triV2 = new Vec2[3];
	private int uvLayerIndex;
	private int vIndex = 0;

	public UVVPGeosetRendererImpl reset(Graphics2D graphics,
	                                    CoordinateSystem coordinateSystem,
	                                    int uvLayerIndex,
	                                    Geoset geoset) {
		this.geoset = geoset;
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.uvLayerIndex = uvLayerIndex;
		return this;
	}

	public void beginTriangle(boolean isHD) {
		renderGeosetTries(geoset, isHD);
	}


	private void renderGeosetTries(Geoset geoset, boolean isHD) {
		for (Triangle triangle : geoset.getTriangles()) {
			previousVertices.clear();
			vIndex = 0;
			for (GeosetVertex vertex : triangle.getVerts()) {

				triV2[vIndex] = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex.getTVertex(0));

				vIndex++;
			}
			GU.drawPolygon(graphics, triV2);
		}
	}

}
