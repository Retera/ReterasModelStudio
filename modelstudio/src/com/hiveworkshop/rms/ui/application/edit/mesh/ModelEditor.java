package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.CheckableDisplayElement;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ComponentVisibilityListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Collection;
import java.util.List;

/**
 * SelectionManager should've been like this so it didn't wrap the selection in
 * silly Item objects, but the code hasn't been reworked to be this thing yet
 *
 * @author Eric
 */
public interface ModelEditor extends ComponentVisibilityListener {

	UndoAction translate(Vec3 v);

	UndoAction scale(Vec3 center, Vec3 scale);

	UndoAction setPosition(Vec3 center, Vec3 v);

	UndoAction rotate(Vec3 center, Vec3 rotate);

	UndoAction setSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	UndoAction removeSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	UndoAction addSelectedRegion(Vec2 min, Vec2 max, CoordinateSystem coordinateSystem);

	@Override
	UndoAction hideComponent(List<? extends CheckableDisplayElement<?>> selectableComponents,
	                         EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable);

	@Override
	UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler);

	void selectByVertices(Collection<? extends Vec3> newSelection);

	boolean canSelectAt(Vec2 point, CoordinateSystem axes);

	GenericMoveAction beginTranslation();

	GenericScaleAction beginScaling(Vec3 center);

	GenericRotateAction beginRotation(Vec3 center, byte firstXYZ, byte secondXYZ);

	GenericRotateAction beginSquatTool(Vec3 center, byte firstXYZ, byte secondXYZ);

	Vec3 getSelectionCenter();

	// true if we conceptually are editing/operating on top of an animated model,
	// instead of a static one
	// -- this is *definitely* a bit of a hack
	boolean editorWantsAnimation();
}
