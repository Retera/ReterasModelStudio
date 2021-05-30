package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.editor.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.editor.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Vec3;

/**
 * SelectionManager should've been like this so it didn't wrap the selection in
 * silly Item objects, but the code hasn't been reworked to be this thing yet
 *
 * @author Eric
 */
public abstract class ModelEditor {
	protected final ModelView modelView;
	protected final AbstractSelectionManager selectionManager;

	public ModelEditor(AbstractSelectionManager selectionManager, ModelView modelView) {
		this.selectionManager = selectionManager;
		this.modelView = modelView;
	}

	public abstract UndoAction translate(Vec3 v);

	public abstract UndoAction scale(Vec3 center, Vec3 scale);

	public abstract UndoAction setPosition(Vec3 center, Vec3 v);

	public abstract UndoAction rotate(Vec3 center, Vec3 rotate);

	public abstract GenericMoveAction beginTranslation();

	public abstract GenericScaleAction beginScaling(Vec3 center);
	public abstract GenericRotateAction beginRotation(Vec3 center, byte firstXYZ, byte secondXYZ);
	public abstract GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ);

	// true if we conceptually are editing/operating on top of an animated model,
	// instead of a static one
	// -- this is *definitely* a bit of a hack
	public abstract boolean editorWantsAnimation();
}
