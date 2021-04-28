package com.hiveworkshop.rms.editor.model.visitor;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UVVPGeosetRendererImpl implements GeosetVisitor {
	private final UVVPTriangleRendererImpl triangleRenderer = new UVVPTriangleRendererImpl();
	private Graphics2D graphics;
	private CoordinateSystem coordinateSystem;
	private int uvLayerIndex;

	public UVVPGeosetRendererImpl reset(Graphics2D graphics, CoordinateSystem coordinateSystem, int uvLayerIndex) {
		this.graphics = graphics;
		this.coordinateSystem = coordinateSystem;
		this.uvLayerIndex = uvLayerIndex;
		return this;
	}

	@Override
	public TriangleVisitor beginTriangle() {
		return triangleRenderer.reset(graphics, coordinateSystem, uvLayerIndex);
	}

	@Override
	public void geosetFinished() {
	}

	class UVVPTriangleRendererImpl implements TriangleVisitor {
		private final VertexRendererImpl vertexRenderer = new VertexRendererImpl();
		private final java.util.List<Point> previousVertices = new ArrayList<>();
		private Graphics2D graphics;
		private CoordinateSystem coordinateSystem;
		private int uvLayerIndex;

		public UVVPTriangleRendererImpl reset(Graphics2D graphics, CoordinateSystem coordinateSystem, int uvLayerIndex) {
			this.graphics = graphics;
			this.coordinateSystem = coordinateSystem;
			this.uvLayerIndex = uvLayerIndex;
			previousVertices.clear();
			return this;
		}

		@Override
		public VertexVisitor vertex(Vec3 vert, Vec3 normal, final List<Bone> bones) {
			return vertexRenderer.reset();
		}

		@Override
		public VertexVisitor hdVertex(Vec3 vert, Vec3 normal, final Bone[] skinBones, final short[] skinBoneWeights) {
			return vertexRenderer.reset();
		}

		@Override
		public void triangleFinished() {
			if (previousVertices.size() > 1) {
				final Point previousPoint = previousVertices.get(previousVertices.size() - 1);
				final Point point = previousVertices.get(0);
				graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
			}
		}

		private final class VertexRendererImpl implements VertexVisitor {
			private int index = 0;

			public VertexRendererImpl reset() {
				index = 0;
				return this;
			}

			@Override
			public void textureCoords(final double u, final double v) {
				if (index == uvLayerIndex) {
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

		}
	}
}
