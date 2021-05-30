package com.hiveworkshop.rms.ui.application.edit.uv;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.GeosetAnim;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.MeshVisitor;
import com.hiveworkshop.rms.editor.model.visitor.TriangleVisitor;
import com.hiveworkshop.rms.editor.model.visitor.VertexVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UVViewportModelRenderer implements MeshVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final GeosetRendererImpl geosetRenderer;
	private ViewportView viewportView;
	private CoordinateSystem coordinateSystem;
	// TODO Now that I added modelView to this class, why does RenderByViewModelRenderer exist???
	private ModelView modelView;
	private int uvLayerIndex;

	public UVViewportModelRenderer() {
		geosetRenderer = new GeosetRendererImpl();
	}

	public UVViewportModelRenderer reset(final Graphics2D graphics, final ProgramPreferences programPreferences,
	                                     final ViewportView viewportView, final CoordinateSystem coordinateSystem, final ModelView modelView) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.viewportView = viewportView;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelView;
		return this;
	}

	@Override
	public GeosetVisitor beginGeoset(final int geosetId, final Material material, final GeosetAnim geosetAnim) {
		graphics.setColor(programPreferences.getTriangleColor());
		if (modelView.getHighlightedGeoset() == modelView.getModel().getGeoset(geosetId)) {
			graphics.setColor(programPreferences.getHighlighTriangleColor());
		}
		return geosetRenderer.reset();
	}

	private final class GeosetRendererImpl implements GeosetVisitor {
		private final TriangleRendererImpl triangleRenderer = new TriangleRendererImpl();

		public GeosetRendererImpl reset() {
			return this;
		}

		@Override
		public TriangleVisitor beginTriangle() {
			return triangleRenderer.reset();
		}

		@Override
		public void geosetFinished() {
		}

	}

	private final class TriangleRendererImpl implements TriangleVisitor {
		private final VertexRendererImpl vertexRenderer = new VertexRendererImpl();
		private final List<Point> previousVertices = new ArrayList<>();

		public TriangleRendererImpl reset() {
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

			@Override
			public void vertexFinished() {
			}

		}
	}

}
