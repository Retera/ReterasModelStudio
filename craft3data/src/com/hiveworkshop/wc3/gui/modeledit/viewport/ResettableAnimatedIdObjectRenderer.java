package com.hiveworkshop.wc3.gui.modeledit.viewport;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.ArrayList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.PopcornFxEmitter;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

public final class ResettableAnimatedIdObjectRenderer implements IdObjectVisitor {
	private CoordinateSystem coordinateSystem;
	private Graphics2D graphics;
	private final int vertexSize;
	private Color lightColor;
	private Color pivotPointColor;
	private NodeIconPalette nodeIconPalette;
	private RenderModel renderModel;
	private final ViewportRenderableCamera renderableCameraProp = new ViewportRenderableCamera();
	private boolean crosshairIsBox;

	public ResettableAnimatedIdObjectRenderer(final int vertexSize) {
		this.vertexSize = vertexSize;
	}

	public ResettableAnimatedIdObjectRenderer reset(final CoordinateSystem coordinateSystem, final Graphics2D graphics,
			final Color lightColor, final Color pivotPointColor, final NodeIconPalette nodeIconPalette,
			final RenderModel renderModel, final boolean crosshairIsBox) {
		this.coordinateSystem = coordinateSystem;
		this.graphics = graphics;
		this.lightColor = lightColor;
		this.pivotPointColor = pivotPointColor;
		this.nodeIconPalette = nodeIconPalette;
		this.renderModel = renderModel;
		this.crosshairIsBox = crosshairIsBox;
		return this;
	}

	@Override
	public void bone(final Bone object) {
		graphics.setColor(pivotPointColor);
		drawCrosshair(object);
	}

	@Override
	public void light(final Light light) {
		final Image lightImage = nodeIconPalette.getLightImage();
		graphics.setColor(lightColor);
		loadPivotInVertexHeap(light.getPivotPoint(), renderModel.getRenderNode(light).getWorldMatrix(), vertexHeap);
		final int xCoord = (int) coordinateSystem
				.convertX(Vertex.getCoord(vertexHeap, coordinateSystem.getPortFirstXYZ()));
		final int yCoord = (int) coordinateSystem
				.convertY(Vertex.getCoord(vertexHeap, coordinateSystem.getPortSecondXYZ()));
		final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
		// graphics.drawOval(xCoord - vertexSize * 2, yCoord - vertexSize * 2,
		// vertexSize * 4, vertexSize * 4);
		// graphics.setColor(programPreferences.getAmbientLightColor());
		// graphics.drawLine(xCoord - vertexSize * 3, yCoord, xCoord +
		// vertexSize * 3, yCoord);
		// graphics.drawLine(xCoord, yCoord - vertexSize * 3, xCoord, yCoord +
		// vertexSize * 3);
		graphics.drawImage(lightImage, xCoord - (lightImage.getWidth(null) / 2),
				yCoord - (lightImage.getHeight(null) / 2), lightImage.getWidth(null), lightImage.getHeight(null), null);

		final int attenuationStart = (int) (light.getAttenuationStart() * zoom);
		if (attenuationStart > 0) {
			graphics.drawOval(xCoord - attenuationStart, yCoord - attenuationStart, attenuationStart * 2,
					attenuationStart * 2);
		}
		final int attenuationEnd = (int) (light.getAttenuationEnd() * zoom);
		if (attenuationEnd > 0) {
			graphics.drawOval(xCoord - attenuationEnd, yCoord - attenuationEnd, attenuationEnd * 2, attenuationEnd * 2);
		}
	}

	private void drawCrosshair(final Bone object) {
		drawCrosshair(object.getPivotPoint(), renderModel.getRenderNode(object).getWorldMatrix());
	}

	private void drawCrosshair(final Vertex pivotPoint, final Matrix4f worldMatrix) {
		drawCrosshair(graphics, coordinateSystem, vertexSize, pivotPoint, worldMatrix, crosshairIsBox);
	}

	@Override
	public void helper(final Helper object) {
		graphics.setColor(pivotPointColor.darker());
		drawCrosshair(object);
	}

	@Override
	public void attachment(final Attachment attachment) {
		drawNodeImage(attachment, nodeIconPalette.getAttachmentImage(),
				renderModel.getRenderNode(attachment).getWorldMatrix());
	}

	@Override
	public void particleEmitter(final ParticleEmitter particleEmitter) {
		drawNodeImage(particleEmitter, nodeIconPalette.getParticleImage(),
				renderModel.getRenderNode(particleEmitter).getWorldMatrix());
	}

	@Override
	public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
		drawNodeImage(particleEmitter, nodeIconPalette.getParticle2Image(),
				renderModel.getRenderNode(particleEmitter).getWorldMatrix());
	}

	@Override
	public void popcornFxEmitter(final PopcornFxEmitter popcornFxEmitter) {
		drawNodeImage(popcornFxEmitter, nodeIconPalette.getParticleImage(),
				renderModel.getRenderNode(popcornFxEmitter).getWorldMatrix());
	}

	@Override
	public void ribbonEmitter(final RibbonEmitter ribbonEmitter) {
		drawNodeImage(ribbonEmitter, nodeIconPalette.getRibbonImage(),
				renderModel.getRenderNode(ribbonEmitter).getWorldMatrix());
	}

	@Override
	public void eventObject(final EventObject eventObject) {
		drawNodeImage(eventObject, nodeIconPalette.getEventImage(),
				renderModel.getRenderNode(eventObject).getWorldMatrix());
	}

	@Override
	public void collisionShape(final CollisionShape collisionShape) {
		drawCollisionShape(graphics, pivotPointColor, coordinateSystem, coordinateSystem.getPortFirstXYZ(),
				coordinateSystem.getPortSecondXYZ(), vertexSize, collisionShape, nodeIconPalette.getCollisionImage(),
				renderModel.getRenderNode(collisionShape).getWorldMatrix(), crosshairIsBox);
	}

	@Override
	public void camera(final Camera camera) {
		graphics.setColor(Color.GREEN.darker());
		final Graphics2D g2 = ((Graphics2D) graphics.create());
		final Vertex ver = camera.getPosition();
		final Vertex targ = camera.getTargetPosition();
		loadPivotInVertexHeap(ver, renderModel.getRenderNode(camera.getSourceNode()).getWorldMatrix(), vertexHeap);
		final float startX = Vertex.getCoord(vertexHeap, (byte) 0);
		final float startY = Vertex.getCoord(vertexHeap, (byte) 1);
		final float startZ = Vertex.getCoord(vertexHeap, (byte) 2);
		final Point start = new Point((int) Math.round(coordinateSystem.convertX(startX)),
				(int) Math.round(coordinateSystem.convertY(startY)));
		loadPivotInVertexHeap(targ, renderModel.getRenderNode(camera.getTargetNode()).getWorldMatrix(), vertexHeap);
		final float endX = Vertex.getCoord(vertexHeap, (byte) 0);
		final float endY = Vertex.getCoord(vertexHeap, (byte) 1);
		final float endZ = Vertex.getCoord(vertexHeap, (byte) 2);
		final Point end = new Point((int) Math.round(coordinateSystem.convertX(endX)),
				(int) Math.round(coordinateSystem.convertY(endY)));

		Double renderRotationScalar = renderModel.getAnimatedRenderEnvironment() == null ? Double.valueOf(0)
				: camera.getSourceNode().getRenderRotationScalar(renderModel.getAnimatedRenderEnvironment());
		if (renderRotationScalar == null) {
			renderRotationScalar = 0.;
		}
		renderableCameraProp.render(g2, coordinateSystem, startX, startY, startZ, endX, endY, endZ,
				renderRotationScalar);

		// g2.translate(end.x, end.y);
		// g2.rotate(-(Math.PI / 2 + Math.atan2(end.x - start.x, end.y - start.y)));
		// final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
		// final int size = (int) (20 * zoom);
		// final double dist = start.distance(end);
		//
		// // if (verSel) {
		// // g2.setColor(Color.orange.darker());
		// // }
		// // Cam
		// g2.fillRect((int) dist - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 +
		// vertexSize * 2);
		// g2.drawRect((int) dist - size, -size, size * 2, size * 2);
		//
		// // if (tarSel) {
		// // g2.setColor(Color.orange.darker());
		// // } else if (verSel) {
		// // g2.setColor(Color.green.darker());
		// // }
		// // Target
		// g2.fillRect(0 - vertexSize, 0 - vertexSize, 1 + vertexSize * 2, 1 +
		// vertexSize * 2);
		// g2.drawLine(0, 0, size, size);//
		// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())+5)),
		// // (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())+5)));
		// g2.drawLine(0, 0, size, -size);//
		// (int)Math.round(vp.convertX(targ.getCoord(vp.getPortFirstXYZ())-5)),
		// // (int)Math.round(vp.convertY(targ.getCoord(vp.getPortSecondXYZ())-5)));
		//
		// // if (!verSel && tarSel) {
		// // g2.setColor(Color.green.darker());
		// // }
		// g2.drawLine(0, 0, (int) dist, 0);
	}

	private void drawNodeImage(final IdObject attachment, final Image nodeImage, final Matrix4f worldMatrix) {
		drawNodeImage(graphics, coordinateSystem.getPortFirstXYZ(), coordinateSystem.getPortSecondXYZ(),
				coordinateSystem, attachment, nodeImage, worldMatrix);
	}

	public static void drawNodeImage(final Graphics2D graphics, final byte xDimension, final byte yDimension,
			final CoordinateSystem coordinateSystem, final IdObject attachment, final Image nodeImage,
			final Matrix4f worldMatrix) {
		loadPivotInVertexHeap(attachment.getPivotPoint(), worldMatrix, vertexHeap);
		final int xCoord = (int) coordinateSystem.convertX(Vertex.getCoord(vertexHeap, xDimension));
		final int yCoord = (int) coordinateSystem.convertY(Vertex.getCoord(vertexHeap, yDimension));
		graphics.drawImage(nodeImage, xCoord - (nodeImage.getWidth(null) / 2), yCoord - (nodeImage.getHeight(null) / 2),
				nodeImage.getWidth(null), nodeImage.getHeight(null), null);
	}

	private static final Vector4f vertexHeap = new Vector4f();
	private static final Vector4f vertexHeap2 = new Vector4f();

	public static void drawCrosshair(final Graphics2D graphics, final CoordinateSystem coordinateSystem, int vertexSize,
			final Vertex pivotPoint, final Matrix4f worldMatrix, final boolean crosshairIsBox) {
		loadPivotInVertexHeap(pivotPoint, worldMatrix, vertexHeap);

		final int xCoord = (int) coordinateSystem
				.convertX(Vertex.getCoord(vertexHeap, coordinateSystem.getPortFirstXYZ()));
		final int yCoord = (int) coordinateSystem
				.convertY(Vertex.getCoord(vertexHeap, coordinateSystem.getPortSecondXYZ()));
		if (crosshairIsBox) {
			vertexSize *= 3;
			graphics.fillRect(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
		} else {
			graphics.drawOval(xCoord - vertexSize, yCoord - vertexSize, vertexSize * 2, vertexSize * 2);
			graphics.drawLine(xCoord - (int) (vertexSize * 1.5f), yCoord, xCoord + (int) (vertexSize * 1.5f), yCoord);
			graphics.drawLine(xCoord, yCoord - (int) (vertexSize * 1.5f), xCoord, yCoord + (int) (vertexSize * 1.5f));
		}
	}

	public static void loadPivotInVertexHeap(final Vertex pivotPoint, final Matrix4f worldMatrix,
			final Vector4f vertexHeap) {
		vertexHeap.x = (float) pivotPoint.x;
		vertexHeap.y = (float) pivotPoint.y;
		vertexHeap.z = (float) pivotPoint.z;
		vertexHeap.w = 1;
		Matrix4f.transform(worldMatrix, vertexHeap, vertexHeap);
	}

	public static void drawCollisionShape(final Graphics2D graphics, final Color color,
			final CoordinateSystem coordinateSystem, final byte xDimension, final byte yDimension, final int vertexSize,
			final CollisionShape collisionShape, final Image collisionImage, final Matrix4f worldMatrix,
			final boolean crosshairIsBox) {
		final Vertex pivotPoint = collisionShape.getPivotPoint();
		final ArrayList<Vertex> vertices = collisionShape.getVertices();
		graphics.setColor(color);
		loadPivotInVertexHeap(pivotPoint, worldMatrix, vertexHeap);
		final int xCoord = (int) coordinateSystem.convertX(Vertex.getCoord(vertexHeap, xDimension));
		final int yCoord = (int) coordinateSystem.convertY(Vertex.getCoord(vertexHeap, yDimension));
		if (collisionShape.getFlags().contains("Box")) {
			if (vertices.size() > 1) {
				final Vertex vertex = vertices.get(0);
				final Vertex vertex2 = vertices.get(1);
				loadPivotInVertexHeap(vertex, worldMatrix, vertexHeap);
				loadPivotInVertexHeap(vertex2, worldMatrix, vertexHeap2);
				final int firstXCoord = (int) coordinateSystem.convertX(Vertex.getCoord(vertexHeap2, xDimension));
				final int firstYCoord = (int) coordinateSystem.convertY(Vertex.getCoord(vertexHeap2, yDimension));
				final int secondXCoord = (int) coordinateSystem.convertX(Vertex.getCoord(vertexHeap, xDimension));
				final int secondYCoord = (int) coordinateSystem.convertY(Vertex.getCoord(vertexHeap, yDimension));
				final int minXCoord = Math.min(firstXCoord, secondXCoord);
				final int minYCoord = Math.min(firstYCoord, secondYCoord);
				final int maxXCoord = Math.max(firstXCoord, secondXCoord);
				final int maxYCoord = Math.max(firstYCoord, secondYCoord);

				graphics.drawRoundRect(minXCoord, minYCoord, maxXCoord - minXCoord, maxYCoord - minYCoord, vertexSize,
						vertexSize);
				drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
						worldMatrix);
			} else {
				drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
						worldMatrix);
			}
		} else {
			if (collisionShape.getExtents() != null) {
				final double zoom = CoordinateSystem.Util.getZoom(coordinateSystem);
				final double boundsRadius = collisionShape.getExtents().getBoundsRadius() * zoom;
				graphics.drawOval((int) (xCoord - boundsRadius), (int) (yCoord - boundsRadius),
						(int) (boundsRadius * 2), (int) (boundsRadius * 2));
				drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
						worldMatrix);
			} else {
				drawNodeImage(graphics, xDimension, yDimension, coordinateSystem, collisionShape, collisionImage,
						worldMatrix);
			}
		}
		for (final Vertex vertex : vertices) {
			drawCrosshair(graphics, coordinateSystem, vertexSize, vertex, worldMatrix, crosshairIsBox);
		}
	}
}