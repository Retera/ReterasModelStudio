package com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.AbstractScaleManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public final class ScaleTVertexManipulator extends AbstractScaleManipulator {

	public ScaleTVertexManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		Vec3 center = new Vec3().setCoords(dim1, dim2, selectionManager.getUVCenter(0));
		resetScaleVector();
		scaleAction = modelEditor.beginScaling(center);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, Mat4 viewPortAntiRotMat) {
		inverseViewProjectionMatrix.set(viewPortAntiRotMat).invert();
		Vec3 center = new Vec3().setCoords((byte) 0, (byte) 1, selectionManager.getUVCenter(0));
		resetScaleVector();
		scaleAction = modelEditor.beginScaling(center);
	}

	protected double computeScaleFactor(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		Vec2 selCenter = selectionManager.getUVCenter(0);
		Vec2 center = new Vec2(selCenter.getCoord(dim1), selCenter.getCoord(dim2));
		double dxEnd = 0;
		double dyEnd = 0;
		double dxStart = 0;
		double dyStart = 0;
		int flipNeg = 1;

		if (dir.containDirection(dim1)) {
			dxEnd = mouseEnd.x - center.x;
			dxStart = mouseStart.x - center.x;
			flipNeg = getFlipNeg(dxEnd);
		}
		if (dir.containDirection(dim2)) {
			dyEnd = mouseEnd.y - center.y;
			dyStart = mouseStart.y - center.y;
			if (!dir.containDirection(dim1)) {
				// up is -y
				flipNeg = getFlipNeg(-dyEnd);
			}
		}
		double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
		double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));

		return flipNeg * endDist / startDist;
	}

	protected double computeScaleFactor(Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat1) {
		Vec2 center = selectionManager.getUVCenter(0);
		double dxEnd = 0;
		double dyEnd = 0;
		double dxStart = 0;
		double dyStart = 0;
		int flipNeg = 1;

//		if (dir.containDirection(dim1)) {
//			dxEnd = mouseEnd.x - center.getCoord(dim1);
//			dxStart = mouseStart.x - center.getCoord(dim1);
//			flipNeg = getFlipNeg(dxEnd);
//		}
//		if (dir.containDirection(dim2)) {
//			dyEnd = mouseEnd.y - center.getCoord(dim2);
//			dyStart = mouseStart.y - center.getCoord(dim2);
//			if (!dir.containDirection(dim1)) {
//				// up is -y
//				flipNeg = getFlipNeg(-dyEnd);
//			}
//		}
		double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
		double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));

		return flipNeg * endDist / startDist;
	}
}
