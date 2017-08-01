package com.hiveworkshop.wc3.gui.modeledit.viewport;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.renderer.GeosetRenderer;
import com.hiveworkshop.wc3.mdl.renderer.ModelRenderer;
import com.hiveworkshop.wc3.mdl.renderer.TriangleRenderer;
import com.hiveworkshop.wc3.mdl.v2.MaterialView;
import com.hiveworkshop.wc3.mdl.v2.visitor.VertexVisitor;

public class ViewportModelRenderer implements ModelRenderer {
	private final Image attachmentImage;
	private final Image eventImage;
	private final Image lightImage;
	private final Image particleImage;
	private final Image particle2Image;
	private final Image ribbonImage;
	private final Image collisionImage;
	private Graphics2D graphics;
	private ProgramPreferences programPreferences;
	private final GeosetRendererImpl geosetRenderer;
	private final int vertexSize;
	private byte xDimension;
	private byte yDimension;
	private ViewportView viewportView;
	private CoordinateSystem coordinateSystem;

	public ViewportModelRenderer(final int vertexSize) {
		this.vertexSize = vertexSize;
		geosetRenderer = new GeosetRendererImpl();

		try {
			attachmentImage = ImageIO.read(getClass().getResource("icons/nodes/attachment.png"));
			eventImage = ImageIO.read(getClass().getResource("icons/nodes/event.png"));
			lightImage = ImageIO.read(getClass().getResource("icons/nodes/light.png"));
			particleImage = ImageIO.read(getClass().getResource("icons/nodes/particle1.png"));
			particle2Image = ImageIO.read(getClass().getResource("icons/nodes/particle2.png"));
			ribbonImage = ImageIO.read(getClass().getResource("icons/nodes/ribbon.png"));
			collisionImage = ImageIO.read(getClass().getResource("icons/nodes/collision.png"));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
	}

	public ViewportModelRenderer reset(final Graphics2D graphics, final ProgramPreferences programPreferences,
			final byte xDimension, final byte yDimension, final ViewportView viewportView,
			final CoordinateSystem coordinateSystem) {
		this.graphics = graphics;
		this.programPreferences = programPreferences;
		this.xDimension = xDimension;
		this.yDimension = yDimension;
		this.viewportView = viewportView;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	@Override
	public GeosetRenderer beginGeoset(final MaterialView material, final GeosetAnim geosetAnim) {
		return geosetRenderer.reset();
	}

	@Override
	public void bone(final Bone object) {
		graphics.setColor(programPreferences.getPivotPointsColor());
		drawCrosshair(object);
	}

	private void drawCrosshair(final Bone object) {
		final int xCoord = (int) coordinateSystem.convertX(object.getPivotPoint().getCoord(xDimension));
		final int yCoord = (int) coordinateSystem.convertY(object.getPivotPoint().getCoord(yDimension));
		graphics.drawOval(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
		graphics.drawLine(xCoord - (int) (vertexSize * 1.5f), yCoord, xCoord + (int) (vertexSize * 1.5f), yCoord);
		graphics.drawLine(xCoord, yCoord - (int) (vertexSize * 1.5f), xCoord, yCoord + (int) (vertexSize * 1.5f));
	}

	@Override
	public void light(final Light light) {
		graphics.setColor(programPreferences.getLightsColor());
		final int xCoord = (int) coordinateSystem.convertX(light.getPivotPoint().getCoord(xDimension));
		final int yCoord = (int) coordinateSystem.convertY(light.getPivotPoint().getCoord(yDimension));
		// graphics.drawOval(xCoord - vertexSize * 2, yCoord - vertexSize * 2,
		// vertexSize * 4, vertexSize * 4);
		// graphics.setColor(programPreferences.getAmbientLightColor());
		// graphics.drawLine(xCoord - vertexSize * 3, yCoord, xCoord +
		// vertexSize * 3, yCoord);
		// graphics.drawLine(xCoord, yCoord - vertexSize * 3, xCoord, yCoord +
		// vertexSize * 3);
		graphics.drawImage(lightImage, xCoord - lightImage.getWidth(null) / 2, yCoord - lightImage.getHeight(null) / 2,
				lightImage.getWidth(null), -lightImage.getHeight(null), null);

		final int attenuationStart = (int) (light.getAttenuationStart() * viewportView.getZoom());
		if (attenuationStart > 0) {
			graphics.drawOval(xCoord - attenuationStart, yCoord - attenuationStart, attenuationStart * 2,
					attenuationStart * 2);
		}
		final int attenuationEnd = (int) (light.getAttenuationEnd() * viewportView.getZoom());
		if (attenuationEnd > 0) {
			graphics.drawOval(xCoord - attenuationEnd, yCoord - attenuationEnd, attenuationEnd * 2, attenuationEnd * 2);
		}
	}

	@Override
	public void helper(final Helper object) {
		graphics.setColor(programPreferences.getPivotPointsColor().darker());
		drawCrosshair(object);
	}

	@Override
	public void attachment(final Attachment attachment) {
		drawNodeImage(attachment, attachmentImage);
	}

	private void drawNodeImage(final IdObject attachment, final Image nodeImage) {
		final int xCoord = (int) coordinateSystem.convertX(attachment.getPivotPoint().getCoord(xDimension));
		final int yCoord = (int) coordinateSystem.convertY(attachment.getPivotPoint().getCoord(yDimension));
		graphics.drawImage(nodeImage, xCoord - nodeImage.getWidth(null) / 2, yCoord - nodeImage.getHeight(null) / 2,
				nodeImage.getWidth(null), nodeImage.getHeight(null), null);
	}

	@Override
	public void particleEmitter(final ParticleEmitter particleEmitter) {
		drawNodeImage(particleEmitter, particleImage);
	}

	@Override
	public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
		drawNodeImage(particleEmitter, particle2Image);
	}

	@Override
	public void ribbonEmitter(final RibbonEmitter ribbonEmitter) {
		drawNodeImage(ribbonEmitter, ribbonImage);
	}

	@Override
	public void eventObject(final EventObject eventObject) {
		drawNodeImage(eventObject, eventImage);
	}

	@Override
	public void collisionShape(final CollisionShape collisionShape) {
		final Vertex pivotPoint = collisionShape.getPivotPoint();
		final ArrayList<Vertex> vertices = collisionShape.getVertices();
		graphics.setColor(programPreferences.getPivotPointsColor().brighter());
		final int xCoord = (int) coordinateSystem.convertX(pivotPoint.getCoord(xDimension));
		final int yCoord = (int) coordinateSystem.convertY(pivotPoint.getCoord(yDimension));
		if (collisionShape.getFlags().contains("Box")) {
			if (vertices.size() > 0) {
				final Vertex vertex = vertices.get(0);
				graphics.drawRoundRect(xCoord, yCoord,
						(int) coordinateSystem.convertX(vertex.getCoord(xDimension)) - xCoord,
						(int) coordinateSystem.convertY(vertex.getCoord(yDimension)) - yCoord, vertexSize, vertexSize);
			} else {
				drawNodeImage(collisionShape, collisionImage);
			}
		} else {
			if (collisionShape.getExtents() != null) {
				final double boundsRadius = collisionShape.getExtents().getBoundsRadius() * viewportView.getZoom();
				graphics.drawOval((int) (xCoord - boundsRadius), (int) (yCoord - boundsRadius),
						(int) (boundsRadius * 2), (int) (boundsRadius * 2));
			} else {
				drawNodeImage(collisionShape, collisionImage);
			}
		}
	}

	@Override
	public void camera(final Camera cam) {
		graphics.setColor(Color.GREEN.darker());
		final Graphics2D g2 = ((Graphics2D) graphics.create());
		final Vertex ver = cam.getPosition();
		final Vertex targ = cam.getTargetPosition();
		// final boolean verSel = selection.contains(ver);
		// final boolean tarSel = selection.contains(targ);
		final Point start = new Point(
				(int) Math.round(coordinateSystem.convertX(ver.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.convertY(ver.getCoord(coordinateSystem.getPortSecondXYZ()))));
		final Point end = new Point(
				(int) Math.round(coordinateSystem.convertX(targ.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.convertY(targ.getCoord(coordinateSystem.getPortSecondXYZ()))));
		// if (dispCameraNames) {
		// boolean changedCol = false;
		//
		// if (verSel) {
		// g2.setColor(Color.orange.darker());
		// changedCol = true;
		// }
		// g2.drawString(cam.getName(), (int)
		// Math.round(vp.convertX(ver.getCoord(vp.getPortFirstXYZ()))),
		// (int) Math.round(vp.convertY(ver.getCoord(vp.getPortSecondXYZ()))));
		// if (tarSel) {
		// g2.setColor(Color.orange.darker());
		// changedCol = true;
		// } else if (verSel) {
		// g2.setColor(Color.green.darker());
		// changedCol = false;
		// }
		// g2.drawString(cam.getName() + "_target",
		// (int) Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ()))),
		// (int) Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ()))));
		// if (changedCol) {
		// g2.setColor(Color.green.darker());
		// }
		// }

		g2.translate(end.x, end.y);
		g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
		final int size = (int) (20 * viewportView.getZoom());
		final double dist = start.distance(end);

		// if (verSel) {
		// g2.setColor(Color.orange.darker());
		// }
		// Cam
		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
		g2.drawRect((int) dist - size, -size, size * 2, size * 2);

		// if (tarSel) {
		// g2.setColor(Color.orange.darker());
		// } else if (verSel) {
		// g2.setColor(Color.green.darker());
		// }
		// Target
		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 + vertexSize * 2);
		g2.drawLine(0, 0, size, size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())+5)),
										// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())+5)));
		g2.drawLine(0, 0, size, -size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())-5)),
										// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())-5)));

		// if (!verSel && tarSel) {
		// g2.setColor(Color.green.darker());
		// }
		g2.drawLine(0, 0, (int) dist, 0);
	}

	private final class GeosetRendererImpl implements GeosetRenderer {
		private final TriangleRendererImpl triangleRenderer = new TriangleRendererImpl();

		public GeosetRendererImpl reset() {
			return this;
		}

		@Override
		public TriangleRenderer beginTriangle() {
			graphics.setColor(programPreferences.getTriangleColor());
			return triangleRenderer.reset();
		}

		@Override
		public void geosetFinished() {
			// TODO Auto-generated method stub

		}

	}

	private final class TriangleRendererImpl implements TriangleRenderer {
		private final List<Point> previousVertices = new ArrayList<>();

		public TriangleRendererImpl reset() {
			previousVertices.clear();
			return this;
		}

		@Override
		public VertexVisitor vertex(final double x, final double y, final double z, final double normalX,
				final double normalY, final double normalZ, final List<Bone> bones) {
			double firstCoord, secondCoord;
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
			return VertexVisitor.NO_ACTION;
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

}
