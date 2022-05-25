package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.ExtendAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.util.Arrays;

public final class ExtendManipulator extends AbstractMoveManipulator {

	private UndoAction extendAction;

	public ExtendManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		super.onStart(e, mouseStart, dim1, dim2);
//		extendAction = modelEditor.beginExtendingSelection();
		ModelView modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		extendAction = new ExtendAction(modelView.getSelectedVertices(), new Vec3(0, 0, 0));
		extendAction.redo();
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
		return new CompoundAction("extend", changeListener::geosetsUpdated, extendAction, super.finish(e, mouseStart, mouseEnd, dim1, dim2));
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, Mat4 viewPortAntiRotMat) {
		super.onStart(e, mouseStart, viewPortAntiRotMat);
//		extendAction = modelEditor.beginExtendingSelection();
		ModelView modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		extendAction = new ExtendAction(modelView.getSelectedVertices(), new Vec3(0, 0, 0));
		extendAction.redo();
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat, double sizeAdj) {
		ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
		return new CompoundAction("extend", changeListener::geosetsUpdated, extendAction, super.finish(e, mouseStart, mouseEnd, viewPortAntiRotMat, sizeAdj));
	}

//	@Override
//	protected void buildMoveVector(Double mouseStart, Double mouseEnd, byte dim1, byte dim2) {
//		moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
//		moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
//	}
}
