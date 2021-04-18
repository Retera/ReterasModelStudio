package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxCollisionShape;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
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
	                                 byte xDimension, byte yDimension,
	                                 CoordinateSystem coordinateSystem,
	                                 IdObject attachment, Image nodeImage) {
		int xCoord = (int) coordinateSystem.viewX(attachment.getPivotPoint().getCoord(xDimension));
		int yCoord = (int) coordinateSystem.viewY(attachment.getPivotPoint().getCoord(yDimension));

		int width = nodeImage.getWidth(null);
		int height = nodeImage.getHeight(null);
		graphics.drawImage(nodeImage, xCoord - (width / 2), yCoord - (height / 2), width, height, null);
	}

	public static void drawCrosshair(Graphics2D graphics, CoordinateSystem coordinateSystem, int vertexSize, Vec3 pivotPoint) {

		int xCoord = (int) coordinateSystem.viewX(pivotPoint.getCoord(coordinateSystem.getPortFirstXYZ()));
		int yCoord = (int) coordinateSystem.viewY(pivotPoint.getCoord(coordinateSystem.getPortSecondXYZ()));

		graphics.drawOval(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
		graphics.drawLine(xCoord - (int) (vertexSize * 1.5f), yCoord, xCoord + (int) (vertexSize * 1.5f), yCoord);
		graphics.drawLine(xCoord, yCoord - (int) (vertexSize * 1.5f), xCoord, yCoord + (int) (vertexSize * 1.5f));
	}

	public static void drawBox(Graphics2D graphics, CoordinateSystem coordinateSystem, int vertexSize, Vec3 pivotPoint) {
		vertexSize *= 3;

		int xCoord = (int) coordinateSystem.viewX(pivotPoint.getCoord(coordinateSystem.getPortFirstXYZ()));
		int yCoord = (int) coordinateSystem.viewY(pivotPoint.getCoord(coordinateSystem.getPortSecondXYZ()));
		graphics.fillRect(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
	}

	public static void drawCollisionShape(Graphics2D graphics, Color color, CoordinateSystem coordinateSystem,
	                                      byte xDimension, byte yDimension, int vertexSize,
	                                      CollisionShape collisionShape, Image collisionImage,
	                                      boolean crosshairIsBox) {
		Vec3 pivotPoint = collisionShape.getPivotPoint();
		List<Vec3> vertices = collisionShape.getVertices();
		graphics.setColor(color);
		int xCoord = (int) coordinateSystem.viewX(pivotPoint.getCoord(xDimension));
		int yCoord = (int) coordinateSystem.viewY(pivotPoint.getCoord(yDimension));

		if (collisionShape.getType() == MdlxCollisionShape.Type.BOX) {
			if (vertices.size() > 1) {
				Vec3 vertex = vertices.get(0);
				Vec3 vertex2 = vertices.get(1);

				int firstXCoord = (int) coordinateSystem.viewX(vertex2.getCoord(xDimension));
				int firstYCoord = (int) coordinateSystem.viewY(vertex2.getCoord(yDimension));
				int secondXCoord = (int) coordinateSystem.viewX(vertex.getCoord(xDimension));
				int secondYCoord = (int) coordinateSystem.viewY(vertex.getCoord(yDimension));

				int minXCoord = Math.min(firstXCoord, secondXCoord);
				int minYCoord = Math.min(firstYCoord, secondYCoord);
				int maxXCoord = Math.max(firstXCoord, secondXCoord);
				int maxYCoord = Math.max(firstYCoord, secondYCoord);

				graphics.drawRoundRect(minXCoord, minYCoord, maxXCoord - minXCoord, maxYCoord - minYCoord, vertexSize, vertexSize);
			}
		} else {
			if (collisionShape.getExtents() != null) {
				double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
				double boundsRadius = collisionShape.getExtents().getBoundsRadius() * zoom;
				graphics.drawOval((int) (xCoord - boundsRadius), (int) (yCoord - boundsRadius), (int) (boundsRadius * 2), (int) (boundsRadius * 2));
			}
		}
		drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage);

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

	@Override
	public void bone(Bone object) {
		graphics.setColor(pivotPointColor);
		drawCrosshair(object);
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

	@Override
	public void helper(Helper object) {
		graphics.setColor(pivotPointColor.darker());
		drawCrosshair(object);
	}

	@Override
	public void attachment(Attachment attachment) {
		drawNodeImage(attachment, nodeIconPalette.getAttachmentImage());
	}

	@Override
	public void particleEmitter(ParticleEmitter particleEmitter) {
		drawNodeImage(particleEmitter, nodeIconPalette.getParticleImage());
	}

	@Override
	public void popcornFxEmitter(ParticleEmitterPopcorn popcornFxEmitter) {
		drawNodeImage(popcornFxEmitter, nodeIconPalette.getParticleImage());
	}

	@Override
	public void particleEmitter2(ParticleEmitter2 particleEmitter) {
		drawNodeImage(particleEmitter, nodeIconPalette.getParticle2Image());
	}

	@Override
	public void ribbonEmitter(RibbonEmitter ribbonEmitter) {
		drawNodeImage(ribbonEmitter, nodeIconPalette.getRibbonImage());
	}

	@Override
	public void eventObject(EventObject eventObject) {
		drawNodeImage(eventObject, nodeIconPalette.getEventImage());
	}

	@Override
	public void collisionShape(CollisionShape collisionShape) {
		drawCollisionShape(graphics, pivotPointColor, coordinateSystem, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ(), vertexSize, collisionShape, nodeIconPalette.getCollisionImage(), crosshairIsBox);
	}

	@Override
	public void camera(Camera camera) {
		graphics.setColor(Color.GREEN.darker());
		Graphics2D g2 = ((Graphics2D) graphics.create());
		Vec3 ver = camera.getPosition();
		Vec3 targ = camera.getTargetPosition();
		// boolean verSel = selection.contains(ver);
		// boolean tarSel = selection.contains(targ);
		Point start = new Point(
				(int) Math.round(coordinateSystem.viewX(ver.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.viewY(ver.getCoord(coordinateSystem.getPortSecondXYZ()))));
		Point end = new Point(
				(int) Math.round(coordinateSystem.viewX(targ.getCoord(coordinateSystem.getPortFirstXYZ()))),
				(int) Math.round(coordinateSystem.viewY(targ.getCoord(coordinateSystem.getPortSecondXYZ()))));
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
		g2.rotate(-((Math.PI / 2) + Math.atan2(end.x - start.x, end.y - start.y)));
		double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
		int size = (int) (20 * zoom);
		double dist = start.distance(end);

		// if (verSel) {
		// g2.setColor(Color.orange.darker());
		// }
		// Cam
		g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
		g2.drawRect((int) dist - size, -size, size * 2, size * 2);

		// if (tarSel) {
		// g2.setColor(Color.orange.darker());
		// } else if (verSel) {
		// g2.setColor(Color.green.darker());
		// }
		// Target
		g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + (vertexSize * 2), 1 + (vertexSize * 2));
		g2.drawLine(0, 0, size, size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())+5)),
		// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())+5)));
		g2.drawLine(0, 0, size, -size);// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())-5)),
		// (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())-5)));

		// if (!verSel && tarSel) {
		// g2.setColor(Color.green.darker());
		// }
		g2.drawLine(0, 0, (int) dist, 0);
	}

	private void drawNodeImage(IdObject attachment, Image nodeImage) {
		drawNodeImage(graphics, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ(), coordinateSystem, attachment, nodeImage);
	}

	@Override
	public void light(Light light) {
		Image lightImage = nodeIconPalette.getLightImage();
		graphics.setColor(lightColor);
		int xCoord = (int) coordinateSystem.viewX(light.getPivotPoint().getCoord(coordinateSystem.getPortFirstXYZ()));
		int yCoord = (int) coordinateSystem.viewY(light.getPivotPoint().getCoord(coordinateSystem.getPortSecondXYZ()));
		double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
		// graphics.drawOval(xCoord - vertexSize * 2, yCoord - vertexSize * 2,
		// vertexSize * 4, vertexSize * 4);
		// graphics.setColor(programPreferences.getAmbientLightColor());
		// graphics.drawLine(xCoord - vertexSize * 3, yCoord, xCoord +
		// vertexSize * 3, yCoord);
		// graphics.drawLine(xCoord, yCoord - vertexSize * 3, xCoord, yCoord +
		// vertexSize * 3);
		graphics.drawImage(lightImage, xCoord - (lightImage.getWidth(null) / 2), yCoord - (lightImage.getHeight(null) / 2), lightImage.getWidth(null), lightImage.getHeight(null), null);

		int attenuationStart = (int) (light.getAttenuationStart() * zoom);
		if (attenuationStart > 0) {
			graphics.drawOval(xCoord - attenuationStart, yCoord - attenuationStart, attenuationStart * 2, attenuationStart * 2);
		}
		int attenuationEnd = (int) (light.getAttenuationEnd() * zoom);
		if (attenuationEnd > 0) {
			graphics.drawOval(xCoord - attenuationEnd, yCoord - attenuationEnd, attenuationEnd * 2, attenuationEnd * 2);
		}
	}
}