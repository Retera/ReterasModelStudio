package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;

public abstract class AbstractScaleManipulator extends Manipulator {
	private final ModelEditor modelEditor;
	private final SelectionView selectionView;
	private GenericScaleAction scaleAction;
	MoveDimension dir;
	boolean isNeg = false;

	public AbstractScaleManipulator(ModelEditor modelEditor, SelectionView selectionView, MoveDimension dir) {
		this.modelEditor = modelEditor;
		this.selectionView = selectionView;
		this.dir = dir;
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		super.onStart(e, mouseStart, dim1, dim2);
		Vec3 center = selectionView.getCenter();
		scaleAction = modelEditor.beginScaling(center.x, center.y, center.z);
	}

	@Override
	public void update(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		Vec3 center = selectionView.getCenter();
		double scaleFactor = computeScaleFactor(mouseStart, mouseEnd, center, dim1, dim2);
		scaleWithFactor(modelEditor, center, scaleFactor, dim1, dim2);
	}

	protected final GenericScaleAction getScaleAction() {
		return scaleAction;
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		update(e, mouseStart, mouseEnd, dim1, dim2);
		isNeg = false;
		return scaleAction;
	}

	protected abstract void scaleWithFactor(ModelEditor modelEditor, Vec3 center, double scaleFactor, byte dim1, byte dim2);

	protected abstract Vec3 buildScaleVector(double scaleFactor, byte dim1, byte dim2);

	protected double computeScaleFactor(Vec2 startingClick, Vec2 endingClick, Vec3 center, byte dim1, byte dim2) {
		double dxEnd = 0;
		double dyEnd = 0;
		double dxStart = 0;
		double dyStart = 0;
		int flipNeg = 1;

		if (dir.containDirection(dim1)) {
			dxEnd = endingClick.x - center.getCoord(dim1);
			dxStart = startingClick.x - center.getCoord(dim1);
			flipNeg = getFlipNeg(dxEnd);
		}
		if (dir.containDirection(dim2)) {
			dyEnd = endingClick.y - center.getCoord(dim2);
			dyStart = startingClick.y - center.getCoord(dim2);
			if (!dir.containDirection(dim1)) {
				// up is -y
//				flipNeg = getFlipNeg(-dyEnd);
				flipNeg = getFlipNeg(dyEnd);
			}
		}
		double endDist = Math.sqrt((dxEnd * dxEnd) + (dyEnd * dyEnd));
		double startDist = Math.sqrt((dxStart * dxStart) + (dyStart * dyStart));

		return flipNeg * endDist / startDist;
	}

	private int getFlipNeg(double dEnd) {
		int flipNeg;
		flipNeg = (!isNeg && dEnd < 0) || (isNeg && dEnd > 0) ? -1 : 1;
		isNeg = (flipNeg < 0) != isNeg;
		return flipNeg;
	}

}
