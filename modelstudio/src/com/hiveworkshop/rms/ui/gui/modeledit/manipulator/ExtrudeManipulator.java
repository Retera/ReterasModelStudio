package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.ExtrudeAction;
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

public final class ExtrudeManipulator extends AbstractMoveManipulator {

	private UndoAction beginExtrudingSelection;

	public ExtrudeManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, byte dim1, byte dim2) {
		super.onStart(e, mouseStart, dim1, dim2);
		ModelView modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		beginExtrudingSelection = new ExtrudeAction(modelView.getSelectedVertices(), new Vec3(0, 0, 0));
		beginExtrudingSelection.redo();
//		beginExtrudingSelection = modelEditor.beginExtrudingSelection();
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
		return new CompoundAction("extrude", changeListener::geosetsUpdated, beginExtrudingSelection, super.finish(e, mouseStart, mouseEnd, dim1, dim2));
	}

	@Override
	protected void onStart(MouseEvent e, Vec2 mouseStart, Mat4 viewPortAntiRotMat) {
		super.onStart(e, mouseStart, viewPortAntiRotMat);
		ModelView modelView = ProgramGlobals.getCurrentModelPanel().getModelView();
		beginExtrudingSelection = new ExtrudeAction(modelView.getSelectedVertices(), new Vec3(0, 0, 0));
		beginExtrudingSelection.redo();
//		beginExtrudingSelection = modelEditor.beginExtrudingSelection();
	}

	@Override
	public UndoAction finish(MouseEvent e, Vec2 mouseStart, Vec2 mouseEnd, Mat4 viewPortAntiRotMat, double sizeAdj) {
		ModelStructureChangeListener changeListener = ModelStructureChangeListener.changeListener;
		return new CompoundAction("extrude", changeListener::geosetsUpdated, beginExtrudingSelection, super.finish(e, mouseStart, mouseEnd, viewPortAntiRotMat, sizeAdj));
	}

}
