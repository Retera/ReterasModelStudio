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


	Vec2 pivPTemp = new Vec2();
	Vec2 targPTemp = new Vec2();
	Vec3 vertexHeap = new Vec3();
	Vec3 vertexHeap2 = new Vec3();
	public void drawLink(Graphics2D graphics, CoordinateSystem coordinateSystem,
	                            Vec3 pivotPoint, Vec3 target,
	                            Mat4 worldMatrix, Mat4 targetWorldMatrix) {

		vertexHeap.set(pivotPoint).transform(worldMatrix);
		vertexHeap2.set(target).transform(targetWorldMatrix);

		pivPTemp.set(coordinateSystem.viewV(vertexHeap));
		targPTemp.set(coordinateSystem.viewV(vertexHeap2));

		graphics.setPaint(new GradientPaint(pivPTemp.x, pivPTemp.y, Color.WHITE, targPTemp.x, targPTemp.y, Color.BLACK));
		graphics.drawLine((int) pivPTemp.x, (int) pivPTemp.y, (int) targPTemp.x, (int) targPTemp.y);
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
