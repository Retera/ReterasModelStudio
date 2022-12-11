package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.editor.AbstractTransformAction;
import com.hiveworkshop.rms.editor.actions.editor.StaticMeshShrinkFattenAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.util.Mat4;
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

	public abstract UndoAction translate(Vec3 v, Mat4 rotMat);

	public abstract UndoAction scale(Vec3 center, Vec3 scale, Mat4 rotMat);

	public abstract UndoAction setPosition(Vec3 center, Vec3 v);

	public abstract UndoAction rotate(Vec3 center, Vec3 rotate, Mat4 rotMat);

	public abstract UndoAction shrinkFatten(float amount);

	public abstract AbstractTransformAction beginTranslation(Mat4 rotMat);

	public abstract AbstractTransformAction beginExtrude(Mat4 rotMat);

	public abstract AbstractTransformAction beginExtend(Mat4 rotMat);

	public abstract AbstractTransformAction beginScaling(Vec3 center, Mat4 rotMat);

	public abstract AbstractTransformAction beginRotation(Vec3 center, Vec3 axis, Mat4 rotMat);

	public abstract AbstractTransformAction beginSquatTool(Vec3 center, Vec3 axis, Mat4 rotMat);

	public abstract StaticMeshShrinkFattenAction beginShrinkFatten(float amount);

	// true if we conceptually are editing/operating on top of an animated model,
	// instead of a static one
	// -- this is *definitely* a bit of a hack
	public abstract boolean editorWantsAnimation();
}
