package com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.AbstractMoveManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;

public final class MoveTVertexManipulator extends AbstractMoveManipulator {

	public MoveTVertexManipulator(ModelEditor modelEditor, AbstractSelectionManager selectionManager, MoveDimension dir) {
		super(modelEditor, selectionManager, dir);
	}
}
