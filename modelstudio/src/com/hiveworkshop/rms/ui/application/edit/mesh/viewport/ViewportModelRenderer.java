package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.GeosetVisitor;
import com.hiveworkshop.rms.editor.model.visitor.ModelVisitor;
import com.hiveworkshop.rms.editor.model.visitor.TriangleVisitor;
import com.hiveworkshop.rms.editor.model.visitor.VertexVisitor;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.ResettableIdObjectRenderer;
import com.hiveworkshop.rms.ui.gui.modeledit.VertexFilter;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class ViewportModelRenderer implements ModelVisitor {
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final GeosetRendererImpl geosetRenderer;
	private final int vertexSize;
	private byte xDimension;
	private byte yDimension;
	private ViewportView viewportView;
	private CoordinateSystem coordinateSystem;
	private final ResettableIdObjectRenderer idObjectRenderer;
	// TODO Now that I added modelView to this class, why does
	// RenderByViewModelRenderer exist???
	private ModelView modelView;

	public ViewportModelRenderer(final int vertexSize) {
		this.vertexSize = vertexSize;
		geosetRenderer = new GeosetRendererImpl();
		idObjectRenderer = new ResettableIdObjectRenderer(vertexSize);
	}

	public ViewportModelRenderer reset(final Graphics2D graphics, final ProgramPreferences programPreferences,
									   final byte xDimension, final byte yDimension, final ViewportView viewportView,
									   final CoordinateSystem coordinateSystem, final ModelView modelView) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.viewportView = viewportView;
		this.coordinateSystem = coordinateSystem;
		this.modelView = modelView;
		idObjectRenderer.reset(coordinateSystem, graphics, programPreferences.getLightsColor(),
				programPreferences.getPivotPointsColor(), NodeIconPalette.UNSELECTED,
				programPreferences.isUseBoxesForPivotPoints());
		return this;
	}

	@Override
	public GeosetVisitor beginGeoset(final int geosetId, final Material material, final GeosetAnim geosetAnim) {
		graphics.setColor(programPreferences.getTriangleColor());
		if (modelView.getHighlightedGeoset() == modelView.getModel().getGeoset(geosetId)) {
			graphics.setColor(programPreferences.getHighlighTriangleColor());
		} else {
			final Geoset geoset = modelView.getModel().getGeoset(geosetId);
			if (!modelView.getEditableGeosets().contains(geoset)) {
				graphics.setColor(programPreferences.getVisibleUneditableColor());
			}
		}
		return geosetRenderer.reset();
	}

	@Override
	public void bone(final Bone object) {
		resetIdObjectRendererWithNode(object);
		idObjectRenderer.bone(object);
	}

	private void resetIdObjectRendererWithNode(final IdObject object) {
		idObjectRenderer.reset(coordinateSystem, graphics,
				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor()
						: programPreferences.getLightsColor(),
				modelView.getHighlightedNode() == object ? programPreferences.getHighlighVertexColor()
						: programPreferences.getPivotPointsColor(),
				modelView.getHighlightedNode() == object ? NodeIconPalette.HIGHLIGHT : NodeIconPalette.UNSELECTED,
				programPreferences.isUseBoxesForPivotPoints());
	}

	@Override
	public void light(final Light light) {
		resetIdObjectRendererWithNode(light);
		idObjectRenderer.light(light);
	}

	@Override
	public void helper(final Helper object) {
		resetIdObjectRendererWithNode(object);
		idObjectRenderer.helper(object);
	}

	@Override
	public void attachment(final Attachment attachment) {
		resetIdObjectRendererWithNode(attachment);
		idObjectRenderer.attachment(attachment);
	}

	@Override
	public void particleEmitter(final ParticleEmitter particleEmitter) {
		resetIdObjectRendererWithNode(particleEmitter);
		idObjectRenderer.particleEmitter(particleEmitter);
	}

	@Override
	public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
		resetIdObjectRendererWithNode(particleEmitter);
		idObjectRenderer.particleEmitter2(particleEmitter);
	}

	@Override
	public void popcornFxEmitter(final ParticleEmitterPopcorn popcornFxEmitter) {
		resetIdObjectRendererWithNode(popcornFxEmitter);
		idObjectRenderer.popcornFxEmitter(popcornFxEmitter);
	}

	@Override
	public void ribbonEmitter(final RibbonEmitter ribbonEmitter) {
		resetIdObjectRendererWithNode(ribbonEmitter);
		idObjectRenderer.ribbonEmitter(ribbonEmitter);
	}

	@Override
	public void eventObject(final EventObject eventObject) {
		resetIdObjectRendererWithNode(eventObject);
		idObjectRenderer.eventObject(eventObject);
	}

	@Override
	public void collisionShape(final CollisionShape collisionShape) {
		resetIdObjectRendererWithNode(collisionShape);
		idObjectRenderer.collisionShape(collisionShape);

	}

	@Override
	public void camera(final Camera cam) {
		idObjectRenderer.camera(cam);
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
			// TODO Auto-generated method stub

		}

	}

	private final class TriangleRendererImpl implements TriangleVisitor {
		private final List<Point> previousVertices = new ArrayList<>();

		public TriangleRendererImpl reset() {
			previousVertices.clear();
			return this;
		}

		@Override
		public VertexVisitor vertex(final double x, final double y, final double z, final double normalX,
									final double normalY, final double normalZ, final List<Bone> bones) {
			final double firstCoord;
			final double secondCoord;
			switch (xDimension) {
				case 0:
					firstCoord = x;
					break;
				case 1:
					firstCoord = y;
					break;
				case 2:
					firstCoord = z;
					break;
				default:
					throw new IllegalStateException("Invalid x dimension");
			}
			switch (yDimension) {
				case 0:
					secondCoord = x;
					break;
				case 1:
					secondCoord = y;
					break;
				case 2:
					secondCoord = z;
					break;
				default:
					throw new IllegalStateException("Invalid y dimension");
			}
			final Point point = new Point((int) coordinateSystem.convertX(firstCoord),
					(int) coordinateSystem.convertY(secondCoord));
			if (previousVertices.size() > 0) {
				final Point previousPoint = previousVertices.get(previousVertices.size() - 1);
				graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
			}
			previousVertices.add(point);
			// graphics.setColor(programPreferences.getVertexColor());
			// graphics.fillRect((int) firstCoord - vertexSize / 2, (int)
			// secondCoord - vertexSize / 2, vertexSize,
			// vertexSize);
			if (programPreferences.showNormals()) {
				final Color triangleColor = graphics.getColor();
				final double firstNormalCoord;
				final double secondNormalCoord;
				switch (xDimension) {
					case 0:
						firstNormalCoord = normalX;
						break;
					case 1:
						firstNormalCoord = normalY;
						break;
					case 2:
						firstNormalCoord = normalZ;
						break;
					default:
						throw new IllegalStateException("Invalid x dimension");
				}
				switch (yDimension) {
					case 0:
						secondNormalCoord = normalX;
						break;
					case 1:
						secondNormalCoord = normalY;
						break;
					case 2:
						secondNormalCoord = normalZ;
						break;
					default:
						throw new IllegalStateException("Invalid y dimension");
				}
				graphics.setColor(programPreferences.getNormalsColor());
				final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
				final Point endPoint = new Point(
						(int) coordinateSystem.convertX(firstCoord + ((firstNormalCoord * 12) / zoom)),
						(int) coordinateSystem.convertY(secondCoord + ((secondNormalCoord * 12) / zoom)));
				graphics.drawLine(point.x, point.y, endPoint.x, endPoint.y);
				graphics.setColor(triangleColor);
			}
			return VertexVisitor.NO_ACTION;
		}

		@Override
		public VertexVisitor hdVertex(final double x, final double y, final double z, final double normalX,
									  final double normalY, final double normalZ, final Bone[] skinBones, final short[] skinBoneWeights) {
			return vertex(x, y, z, normalX, normalY, normalZ, null);
		}

		@Override
		public void triangleFinished() {
			if (previousVertices.size() > 1) {
				final Point previousPoint = previousVertices.get(previousVertices.size() - 1);
				final Point point = previousVertices.get(0);
				graphics.drawLine(previousPoint.x, previousPoint.y, point.x, point.y);
			}
		}

	}

	/**
	 * Copied directly from MDLDisplay and then made static.
	 *
	 * @param model
	 * @param g
	 * @param bounds
	 * @param a
	 * @param b
	 * @param filter
	 * @param extraHighlightPoint
	 */
	public static void drawFittedTriangles(final EditableModel model, final Graphics g, final Rectangle bounds, final byte a,
										   final byte b, final VertexFilter<? super GeosetVertex> filter, final Vec3 extraHighlightPoint) {
		final List<Triangle> triangles = new ArrayList<>();
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		g.setColor(Color.GRAY);
		for (final Geoset geo : model.getGeosets()) {
			for (final Triangle t : geo.getTriangles()) {
				boolean drawTriangle = false;
				for (final GeosetVertex vertex : t.getVerts()) {
					if (filter.isAccepted(vertex)) {
						drawTriangle = true;
					}
				}
				if (drawTriangle) {
					triangles.add(t);
				}
				final double[] x = t.getCoords(a);
				for (final double xval : x) {
					if (xval < minX) {
						minX = xval;
					}
					if (xval > maxX) {
						maxX = xval;
					}
				}
				final double[] y = t.getCoords(b);
				for (final double yval : y) {
					final double yCoord = -yval;
					if (yCoord < minY) {
						minY = yCoord;
					}
					if (yCoord > maxY) {
						maxY = yCoord;
					}
				}
			}
		}
		final double deltaX = maxX - minX;
		final double deltaY = maxY - minY;
		final double boxSize = Math.max(deltaX, deltaY);
		minX -= (boxSize - deltaX) / 2;
		minY -= (boxSize - deltaY) / 2;
		final AffineTransform transform = ((Graphics2D) g).getTransform();
		((Graphics2D) g).scale(bounds.getWidth() / boxSize, bounds.getHeight() / boxSize);
		((Graphics2D) g).translate(-minX, -minY);
		for (final Geoset geo : model.getGeosets()) {
			for (final Triangle t : geo.getTriangles()) {
				drawTriangle(g, a, b, t);
			}
		}
		g.setColor(Color.RED);
		for (final Triangle t : triangles) {
			drawTriangle(g, a, b, t);
		}
		g.setColor(Color.YELLOW);
		if (extraHighlightPoint != null) {
			final int x = (int) extraHighlightPoint.getCoord(a);
			final int y = (int) -extraHighlightPoint.getCoord(b);
			g.drawOval(x - 5, y - 5, 10, 10);
			g.drawLine(x, y - 10, x, y + 10);
			g.drawLine(x - 10, y, x + 10, y);
		}
		((Graphics2D) g).setTransform(transform);
	}

	/**
	 * Copied directly from MDLDisplay and then made static.
	 *
	 * @param g
	 * @param a
	 * @param b
	 * @param t
	 */
	private static void drawTriangle(final Graphics g, final byte a, final byte b, final Triangle t) {
		final double[] x = t.getCoords(a);
		final double[] y = t.getCoords(b);
		final int[] xint = new int[4];
		final int[] yint = new int[4];
		for (int ix = 0; ix < 3; ix++) {
			xint[ix] = (int) Math.round(x[ix]);
			yint[ix] = (int) Math.round(-y[ix]);
		}
		xint[3] = xint[0];
		yint[3] = yint[0];
		g.drawPolyline(xint, yint, 4);
	}

}
