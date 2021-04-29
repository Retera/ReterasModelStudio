package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordSysUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.util.List;

public final class ResettableIdObjectRenderer implements IdObjectVisitor {
	private CoordinateSystem coordinateSystem;
	private Graphics2D graphics;
	private final int vertexSize;
	private Color lightColor;
	private Color pivotPointColor;
	private NodeIconPalette nodeIconPalette;
	private boolean crosshairIsBox;

	public ResettableIdObjectRenderer(int vertexSize) {
		this.vertexSize = vertexSize;
	}

	public static void drawNodeImage(Graphics2D graphics,
	                                 CoordinateSystem coordinateSystem,
	                                 IdObject attachment, Image nodeImage) {
		Vec2 coord = CoordSysUtils.convertToViewVec2(coordinateSystem, attachment.getPivotPoint());

		Vec2 imageSize = new Vec2(nodeImage.getWidth(null), nodeImage.getHeight(null));
		coord.sub(imageSize.getScaled(.5f));


		graphics.drawImage(nodeImage, (int) coord.x, (int) coord.y, (int) imageSize.x, (int) imageSize.y, null);
	}

	public static void drawCrosshair(Graphics2D graphics, CoordinateSystem coordinateSystem, int vertexSize, Vec3 pivotPoint) {
		Vec2 coord = CoordSysUtils.convertToViewVec2(coordinateSystem, pivotPoint);

		Vec2 vertSize = new Vec2(vertexSize, vertexSize);

		Vec2 ovalStart = Vec2.getDif(coord, vertSize);

		Vec2 outerMin = Vec2.getDif(coord, vertSize.getScaled(1.5f));
		Vec2 outerMax = Vec2.getSum(coord, vertSize.getScaled(1.5f));

		graphics.drawOval((int) ovalStart.x, (int) ovalStart.y, vertexSize * 2, vertexSize * 2);
		graphics.drawLine((int) outerMin.x, (int) coord.y, (int) outerMax.x, (int) coord.y);
		graphics.drawLine((int) coord.x, (int) outerMin.y, (int) coord.x, (int) outerMax.y);
	}

	public static void drawBox(Graphics2D graphics, CoordinateSystem coordinateSystem, int vertexSize, Vec3 pivotPoint) {
		vertexSize *= 3;

		int xCoord = (int) coordinateSystem.viewX(pivotPoint.getCoord(coordinateSystem.getPortFirstXYZ()));
		int yCoord = (int) coordinateSystem.viewY(pivotPoint.getCoord(coordinateSystem.getPortSecondXYZ()));
		graphics.fillRect(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
	}

	public static void drawCollisionShape(Graphics2D graphics, Color color, CoordinateSystem coordinateSystem,
	                                      int vertexSize,
	                                      CollisionShape collisionShape, Image collisionImage, boolean crosshairIsBox) {
		List<Vec3> vertices = collisionShape.getVertices();
		graphics.setColor(color);

		Vec2 coord = CoordSysUtils.convertToViewVec2(coordinateSystem, collisionShape.getPivotPoint());

		if (collisionShape.getType() == MdlxCollisionShape.Type.BOX) {
			if (vertices.size() > 1) {
				Vec3 vertex1 = vertices.get(0);
				Vec3 vertex2 = vertices.get(1);

				Vec2 firstCoord = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex1);
				Vec2 secondCoord = CoordSysUtils.convertToViewVec2(coordinateSystem, vertex2);

				Vec2 minCoord = new Vec2(firstCoord).minimize(secondCoord);
				Vec2 maxCoord = new Vec2(firstCoord).maximize(secondCoord);

				Vec2 diff = Vec2.getDif(maxCoord, minCoord);

				graphics.drawRoundRect((int) minCoord.x, (int) minCoord.y, (int) diff.x, (int) diff.y, vertexSize, vertexSize);
			}
		} else {
			if (collisionShape.getExtents() != null) {
				double zoom = CoordSysUtils.getZoom(coordinateSystem);
				double boundsRadius = collisionShape.getExtents().getBoundsRadius() * zoom;
				graphics.drawOval((int) (coord.x - boundsRadius), (int) (coord.y - boundsRadius), (int) (boundsRadius * 2), (int) (boundsRadius * 2));
			}
		}
		drawNodeImage(graphics, coordinateSystem, collisionShape, collisionImage);

		for (Vec3 vertex : vertices) {
			if (crosshairIsBox) {
				drawBox(graphics, coordinateSystem, vertexSize, vertex);
			} else {
				drawCrosshair(graphics, coordinateSystem, vertexSize, vertex);
			}
		}
	}

	public ResettableIdObjectRenderer reset(CoordinateSystem coordinateSystem, Graphics2D graphics,
	                                        Color lightColor, Color pivotPointColor,
	                                        NodeIconPalette nodeIconPalette, boolean crosshairIsBox) {
		this.coordinateSystem = coordinateSystem;
		this.graphics = graphics;
		this.lightColor = lightColor;
		this.pivotPointColor = pivotPointColor;
		this.nodeIconPalette = nodeIconPalette;
		this.crosshairIsBox = crosshairIsBox;
		return this;
	}

	private void drawCrosshair(Bone object) {
		drawCrosshair(object.getPivotPoint());
	}

	private void drawCrosshair(Vec3 pivotPoint) {
		if (crosshairIsBox) {
			drawBox(graphics, coordinateSystem, vertexSize, pivotPoint);
		} else {
			drawCrosshair(graphics, coordinateSystem, vertexSize, pivotPoint);
		}
	}

	private void drawNodeImage(IdObject object, Image nodeImage) {
		drawNodeImage(graphics, coordinateSystem, object, nodeImage);
	}

	@Override
	public void visitIdObject(IdObject object) {
		if (object instanceof Bone) {
			graphics.setColor(pivotPointColor);
			drawCrosshair((Bone) object);
		} else if (object instanceof Light) {
			light((Light) object);
		} else if (object instanceof CollisionShape) {
			collisionShape((CollisionShape) object);
		} else {
			drawNodeImage(object, nodeIconPalette.getObjectImage(object));
		}
	}

	public void collisionShape(CollisionShape object) {
		drawCollisionShape(graphics, pivotPointColor, coordinateSystem, vertexSize, object, nodeIconPalette.getCollisionImage(), crosshairIsBox);
	}

	public void light(Light object) {
		Image lightImage = nodeIconPalette.getLightImage();
		graphics.setColor(lightColor);
		int xCoord = (int) coordinateSystem.viewX(object.getPivotPoint().getCoord(coordinateSystem.getPortFirstXYZ()));
		int yCoord = (int) coordinateSystem.viewY(object.getPivotPoint().getCoord(coordinateSystem.getPortSecondXYZ()));
		double zoom = CoordSysUtils.getZoom(coordinateSystem);

		int height = lightImage.getHeight(null);
		int width = lightImage.getWidth(null);
		graphics.drawImage(lightImage, xCoord - (width / 2), yCoord - (height / 2), width, height, null);

		int attenuationStart = (int) (object.getAttenuationStart() * zoom);
		if (attenuationStart > 0) {
			graphics.drawOval(xCoord - attenuationStart, yCoord - attenuationStart, attenuationStart * 2, attenuationStart * 2);
		}
		int attenuationEnd = (int) (object.getAttenuationEnd() * zoom);
		if (attenuationEnd > 0) {
			graphics.drawOval(xCoord - attenuationEnd, yCoord - attenuationEnd, attenuationEnd * 2, attenuationEnd * 2);
		}
	}

	@Override
	public void camera(Camera camera) {
		graphics.setColor(Color.GREEN.darker());
		Graphics2D g2 = ((Graphics2D) graphics.create());
		Vec3 ver = camera.getPosition();
		Vec3 targ = camera.getTargetPosition();
		Point start = CoordSysUtils.convertToViewPoint(coordinateSystem, ver);
		Point end = CoordSysUtils.convertToViewPoint(coordinateSystem, targ);
//		Point start = new Point(
//				(int) Math.round(coordinateSystem.viewX(ver.getCoord(coordinateSystem.getPortFirstXYZ()))),
//				(int) Math.round(coordinateSystem.viewY(ver.getCoord(coordinateSystem.getPortSecondXYZ()))));
//		Point end = new Point(
//				(int) Math.round(coordinateSystem.viewX(targ.getCoord(coordinateSystem.getPortFirstXYZ()))),
//				(int) Math.round(coordinateSystem.viewY(targ.getCoord(coordinateSystem.getPortSecondXYZ()))));

		g2.translate(end.x, end.y);
		g2.rotate(-((Math.PI / 2) + Math.atan2(end.x - start.x, end.y - start.y)));
		double zoom = CoordSysUtils.getZoom(coordinateSystem);
		int size = (int) (20 * zoom);
		double dist = start.distance(end);

		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
		g2.drawRect((int) dist - size, -size, size * 2, size * 2);

		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
		g2.drawLine(0, 0, size, size);
		g2.drawLine(0, 0, size, -size);

		g2.drawLine(0, 0, (int) dist, 0);
	}
}