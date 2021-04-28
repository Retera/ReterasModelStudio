package com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.NodeIconPalette;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;

import java.awt.*;

public final class ResettableAnimatedIdObjectParentLinkRenderer implements IdObjectVisitor {
	private CoordinateSystem coordinateSystem;
	private Graphics2D graphics;
	private final int vertexSize;
	private NodeIconPalette nodeIconPalette;
	private RenderModel renderModel;

	public ResettableAnimatedIdObjectParentLinkRenderer(int vertexSize) {
		this.vertexSize = vertexSize;
	}

	public static void drawLink(Graphics2D graphics, CoordinateSystem coordinateSystem,
	                            Vec3 pivotPoint, Vec3 target,
	                            Mat4 worldMatrix, Mat4 targetWorldMatrix) {
		Vec4 vertexHeap = loadPivotInVertexHeap(pivotPoint, worldMatrix);
		Vec4 vertexHeap2 = loadPivotInVertexHeap(target, targetWorldMatrix);

		int xCoord = (int) coordinateSystem.viewX(vertexHeap.getCoord(coordinateSystem.getPortFirstXYZ()));
		int yCoord = (int) coordinateSystem.viewY(vertexHeap.getCoord(coordinateSystem.getPortSecondXYZ()));
		int xCoord2 = (int) coordinateSystem.viewX(vertexHeap2.getCoord(coordinateSystem.getPortFirstXYZ()));
		int yCoord2 = (int) coordinateSystem.viewY(vertexHeap2.getCoord(coordinateSystem.getPortSecondXYZ()));
		// TODO resettable
		graphics.setPaint(new GradientPaint(new Point(xCoord, yCoord), Color.WHITE, new Point(xCoord2, yCoord2), Color.BLACK));

		graphics.drawLine(xCoord, yCoord, xCoord2, yCoord2);
	}

	public static Vec4 loadPivotInVertexHeap(Vec3 pivotPoint, Mat4 worldMatrix) {
		Vec4 vertexHeap = new Vec4(pivotPoint, 1);
		return vertexHeap.transform(worldMatrix);
	}

	public ResettableAnimatedIdObjectParentLinkRenderer reset(CoordinateSystem coordinateSystem, Graphics2D graphics, NodeIconPalette nodeIconPalette, RenderModel renderModel) {
		this.coordinateSystem = coordinateSystem;
		this.graphics = graphics;
		this.nodeIconPalette = nodeIconPalette;
		this.renderModel = renderModel;
		return this;
	}
	private void drawCrosshair(Bone object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void visitIdObject(IdObject object) {
		if (object.getParent() != null) {
			drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
					renderModel.getRenderNode(object).getWorldMatrix(),
					renderModel.getRenderNode(object.getParent()).getWorldMatrix());
		}
	}

	@Override
	public void camera(Camera camera) {
		// TODO ANIMATE CAMERAS
		System.err.println("TODO ANIMATE CAMERAS");
	}

}