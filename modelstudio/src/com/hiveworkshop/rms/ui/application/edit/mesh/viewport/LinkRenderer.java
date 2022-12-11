package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;

public class LinkRenderer {
	ModelView modelView;

	public LinkRenderer() {
	}

	public static void drawLink(Graphics2D graphics, CoordinateSystem coordinateSystem,
	                            Vec3 pivotPoint, Vec3 target,
	                            Mat4 worldMatrix, Mat4 targetWorldMatrix) {
		Vec3 vertexHeap = Vec3.getTransformed(pivotPoint, worldMatrix);
		Vec3 vertexHeap2 = Vec3.getTransformed(target, targetWorldMatrix);

		Vec2 pivP = coordinateSystem.viewV(vertexHeap);
		Vec2 targP = coordinateSystem.viewV(vertexHeap2);

		graphics.setPaint(new GradientPaint(pivP.x, pivP.y, Color.WHITE, targP.x, targP.y, Color.BLACK));
		graphics.drawLine((int) pivP.x, (int) pivP.y, (int) targP.x, (int) targP.y);
	}

	public void renderLinks(Graphics2D graphics, CoordinateSystem coordinateSystem, ModelHandler modelHandler) {
		this.modelView = modelHandler.getModelView();

		EditableModel model = modelHandler.getModel();

		for (IdObject object : model.getIdObjects()) {
			if (modelView.getEditableIdObjects().contains(object) || (object == modelView.getHighlightedNode())) {
				if (object.getParent() != null) {
					drawLink(graphics, coordinateSystem, object.getPivotPoint(), object.getParent().getPivotPoint(),
							modelHandler.getRenderModel().getRenderNode(object).getWorldMatrix(),
							modelHandler.getRenderModel().getRenderNode(object.getParent()).getWorldMatrix());
				}
			}
		}
	}
}
