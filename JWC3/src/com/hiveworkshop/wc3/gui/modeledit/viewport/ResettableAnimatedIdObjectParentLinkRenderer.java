package com.hiveworkshop.wc3.gui.modeledit.viewport;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Point;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector4f;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.RenderModel;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

public final class ResettableAnimatedIdObjectParentLinkRenderer implements IdObjectVisitor {
	private CoordinateSystem coordinateSystem;
	private Graphics2D graphics;
	private final int vertexSize;
	private NodeIconPalette nodeIconPalette;
	private RenderModel renderModel;

	public ResettableAnimatedIdObjectParentLinkRenderer(final int vertexSize) {
		this.vertexSize = vertexSize;
	}

	public ResettableAnimatedIdObjectParentLinkRenderer reset(final CoordinateSystem coordinateSystem,
			final Graphics2D graphics, final NodeIconPalette nodeIconPalette, final RenderModel renderModel) {
		this.coordinateSystem = coordinateSystem;
		this.graphics = graphics;
		this.nodeIconPalette = nodeIconPalette;
		this.renderModel = renderModel;
		return this;
	}

	@Override
	public void bone(final Bone object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void light(final Light object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	private void drawCrosshair(final Bone object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void helper(final Helper object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void attachment(final Attachment object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void particleEmitter(final ParticleEmitter object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void particleEmitter2(final ParticleEmitter2 object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void ribbonEmitter(final RibbonEmitter object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void eventObject(final EventObject object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void collisionShape(final CollisionShape object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void camera(final Camera camera) {
		// TODO ANIMATE CAMERAS
		System.err.println("TODO ANIMATE CAMERAS");
	}

	private static final Vector4f vertexHeap = new Vector4f();
	private static final Vector4f vertexHeap2 = new Vector4f();

	public static void drawLink(final Graphics2D graphics, final CoordinateSystem coordinateSystem,
			final Vertex pivotPoint, final Vertex target, final Matrix4f worldMatrix,
			final Matrix4f targetWorldMatrix) {
		loadPivotInVertexHeap(pivotPoint, worldMatrix, vertexHeap);
		loadPivotInVertexHeap(target, targetWorldMatrix, vertexHeap2);

		final int xCoord = (int) coordinateSystem
				.convertX(Vertex.getCoord(vertexHeap, coordinateSystem.getPortFirstXYZ()));
		final int yCoord = (int) coordinateSystem
				.convertY(Vertex.getCoord(vertexHeap, coordinateSystem.getPortSecondXYZ()));
		final int xCoord2 = (int) coordinateSystem
				.convertX(Vertex.getCoord(vertexHeap2, coordinateSystem.getPortFirstXYZ()));
		final int yCoord2 = (int) coordinateSystem
				.convertY(Vertex.getCoord(vertexHeap2, coordinateSystem.getPortSecondXYZ()));
		// TODO resettable
		graphics.setPaint(
				new GradientPaint(new Point(xCoord, yCoord), Color.WHITE, new Point(xCoord2, yCoord2), Color.BLACK));

		graphics.drawLine(xCoord, yCoord, xCoord2, yCoord2);
	}

	public static void loadPivotInVertexHeap(final Vertex pivotPoint, final Matrix4f worldMatrix,
			final Vector4f vertexHeap) {
		vertexHeap.x = (float) pivotPoint.x;
		vertexHeap.y = (float) pivotPoint.y;
		vertexHeap.z = (float) pivotPoint.z;
		vertexHeap.w = 1;
		Matrix4f.transform(worldMatrix, vertexHeap, vertexHeap);
	}

}