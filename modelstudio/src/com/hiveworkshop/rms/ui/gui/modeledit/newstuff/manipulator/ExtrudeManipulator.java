package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actions.mesh.ExtrudeAction;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.event.MouseEvent;
import java.util.Arrays;

public final class ExtrudeManipulator extends AbstractMoveManipulator {

	private UndoAction beginExtrudingSelection;

	public ExtrudeManipulator(ModelView modelView, ModelEditor modelEditor, MoveDimension dir) {
		super(modelView, modelEditor, dir);
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
		return new CompoundAction("extrude", Arrays.asList(beginExtrudingSelection, super.finish(e, mouseStart, mouseEnd, dim1, dim2)));
	}

	@Override
	protected void buildMoveVector(Vec2 mouseStart, Vec2 mouseEnd, byte dim1, byte dim2) {
		if (dir.containDirection(dim1)) {
			moveVector.setCoord(dim1, mouseEnd.x - mouseStart.x);
		}
		if (dir.containDirection(dim2)) {
			moveVector.setCoord(dim2, mouseEnd.y - mouseStart.y);
		}
	}

}
