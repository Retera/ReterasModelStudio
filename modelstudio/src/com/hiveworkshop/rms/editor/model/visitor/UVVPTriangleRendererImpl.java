package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;
import java.util.ArrayList;

public class UVVPTriangleRendererImpl extends TriangleVisitor {
	//		private final VertexVisitor vertexRenderer = new VertexVisitor();
	private final java.util.List<Point> previousVertices = new ArrayList<>();
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private int uvLayerIndex;

	private int index = 0;

	public UVVPTriangleRendererImpl reset(Graphics2D graphics, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.uvLayerIndex = uvLayerIndex;
		previousVertices.clear();
		return this;
	}

	@Override
	public void vertex(GeosetVertex vert, Boolean isHd) {
		index = 0;
		for (Vec2 tvert : vert.getTverts()) {
			textureCoords(tvert.x, tvert.y);
		}
//			return null;
//			return vertexRenderer.reset(graphics, coordinateSystem, uvLayerIndex, previousVertices);
	}

	public void textureCoords(final double u, final double v) {
		if (index == uvLayerIndex) {
			System.out.println(index + ", " + previousVertices.size());
//			if (index == 0) {
			final Point point = new Point((int) coordinateSystem.viewX(u), (int) coordinateSystem.viewY(v));
			if (previousVertices.size() > 0) {
				final Point previousPoint = previousVertices.get(previousVertices.size() - 1);
				graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
			}
			previousVertices.add(point);
			// graphics.setColor(programPreferences.getVertexColor());
			// graphics.fillRect((int) firstCoord - vertexSize / 2, (int)secondCoord - vertexSize / 2, vertexSize,vertexSize);
		}
		index++;
	}


	@Override
	public void triangleFinished() {
		System.out.println("previousVertices.size(): " + previousVertices.size());
		if (previousVertices.size() > 1) {
			Point previousPoint = previousVertices.get(previousVertices.size() - 1);
			Point point = previousVertices.get(0);
			graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
		}
	}

//		private final class VertexRendererImpl implements VertexVisitor {
//			private int index = 0;
//
//			public VertexRendererImpl reset() {
//				index = 0;
//				return this;
//			}
//
//			@Override
//			public void textureCoords(final double u, final double v) {
//				if (index == uvLayerIndex) {
//					final Point point = new Point((int) coordinateSystem.viewX(u), (int) coordinateSystem.viewY(v));
//					if (previousVertices.size() > 0) {
//						final Point previousPoint = previousVertices.get(previousVertices.size() - 1);
//						graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
//					}
//					previousVertices.add(point);
//					// graphics.setColor(programPreferences.getVertexColor());
//					// graphics.fillRect((int) firstCoord - vertexSize / 2, (int)secondCoord - vertexSize / 2, vertexSize,vertexSize);
//				}
//				index++;
//			}
//
//		}
}
