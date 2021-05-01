package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public class UVVPGeosetRendererImpl extends GeosetVisitor {
	private int uvLayerIndex;
	private int index = 0;

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

	@Override
	public void beginTriangle(boolean isHD) {
		renderGeosetTries(geoset, isHD);
	}


	private void renderGeosetTries(Geoset geoset, boolean isHD) {
		for (Triangle triangle : geoset.getTriangles()) {
			previousVertices.clear();
			for (GeosetVertex vertex : triangle.getVerts()) {
				index = 0;
				for (Vec2 tvert : vertex.getTverts()) {
					if (index == uvLayerIndex) {
						Point point = new Point((int) coordinateSystem.viewX(tvert.x), (int) coordinateSystem.viewY(tvert.y));
						if (previousVertices.size() > 0) {
							Point previousPoint = previousVertices.get(previousVertices.size() - 1);
							graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
						}
						previousVertices.add(point);
					}
					index++;
				}
			}
			triangleFinished();
		}
	}

	public void vertex(GeosetVertex vert, Boolean isHd) {
		index = 0;
		for (Vec2 tvert : vert.getTverts()) {
			if (index == uvLayerIndex) {
				Point point = new Point((int) coordinateSystem.viewX(tvert.x), (int) coordinateSystem.viewY(tvert.y));
				if (previousVertices.size() > 0) {
					Point previousPoint = previousVertices.get(previousVertices.size() - 1);
					graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
				}
				previousVertices.add(point);
			}
			index++;
		}
	}

	public void triangleFinished() {
		if (previousVertices.size() > 1) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			Point point = previousVertices.get(0);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
	}
}
