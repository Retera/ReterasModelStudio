package com.hiveworkshop.rms.ui.application.edit.mesh;

import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ComponentVisibilityListener;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.List;

/**
 * SelectionManager should've been like this so it didn't wrap the selection in
 * silly Item objects, but the code hasn't been reworked to be this thing yet
 *
 * @author Eric
 */
public interface ModelEditor extends ComponentVisibilityListener {
	UndoAction autoCenterSelectedBones();

	UndoAction setSelectedBoneName(String name);

	UndoAction addSelectedBoneSuffix(String name);

	UndoAction addTeamColor();

	UndoAction splitGeoset();

	// should move to a Util at a later date, if it does not require internal
	// knowledge of center point from state holders
	UndoAction translate(double x, double y, double z);

	UndoAction translate(Vec3 v);

	UndoAction setPosition(Vec3 center, double x, double y, double z);

	UndoAction setPosition(Vec3 center, Vec3 v);

	UndoAction rotate(Vec3 center, double rotateX, double rotateY, double rotateZ);

	UndoAction rotate(Vec3 center, Vec3 rotate);

	UndoAction addVertex(double x, double y, double z, Vec3 preferredNormalFacingVector);

	UndoAction addBone(double x, double y, double z);

	GenericMoveAction addPlane(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                           int numberOfWidthSegments, int numberOfHeightSegments);

	GenericMoveAction addBox(Vec2 p1, Vec2 p2, byte dim1, byte dim2, Vec3 facingVector,
	                         int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments);

	UndoAction setMatrix(Collection<Bone> bones);

	UndoAction setHDSkinning(Bone[] bones, short[] skinWeights);

	UndoAction setParent(IdObject node);

	UndoAction createKeyframe(ModelEditorActionType actionType);

	UndoAction deleteSelectedComponents();

	UndoAction snapNormals();

	UndoAction recalcNormals(double maxAngle, boolean useTries);

	UndoAction recalcExtents(boolean onlyIncludeEditableGeosets);

	UndoAction mirror(byte dim, boolean flipModel, double centerX, double centerY, double centerZ);

	UndoAction flipSelectedFaces();

	UndoAction flipSelectedNormals();

	UndoAction snapSelectedVertices();

	UndoAction snapSelectedNormals();

	UndoAction beginExtrudingSelection();

	UndoAction beginExtendingSelection();

	UndoAction cloneSelectedComponents(ClonedNodeNamePicker clonedNodeNamePicker);

	UndoAction setSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction removeSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction addSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction expandSelection();

	RigAction rig();

	UndoAction invertSelection();

	UndoAction selectAll();

	@Override
	UndoAction hideComponent(List<? extends SelectableComponent> selectableComponents,
							 EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable);

	@Override
	UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler);

	void selectByVertices(Collection<? extends Vec3> newSelection);

	boolean canSelectAt(Point point, CoordinateSystem axes);

	GenericMoveAction beginTranslation();

	GenericScaleAction beginScaling(double centerX, double centerY, double centerZ);

	GenericScaleAction beginScaling(Vec3 center);

	GenericRotateAction beginRotation(double centerX, double centerY, double centerZ, byte firstXYZ, byte secondXYZ);

	GenericRotateAction beginSquatTool(double centerX, double centerY, double centerZ, byte firstXYZ, byte secondXYZ);

	void rawTranslate(double x, double y, double z);

	void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ);

	void rawScale(Vec3 center, Vec3 scale);

	void rawRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ);

	void rawRotate3d(Vec3 center, Vec3 axis, double radians);

	Vec3 getSelectionCenter();

	CopiedModelData copySelection();

	// true if we conceptually are editing/operating on top of an animated model,
	// instead of a static one
	// -- this is *definitely* a bit of a hack
	boolean editorWantsAnimation();

	UndoAction createFaceFromSelection(Vec3 preferredFacingVector);

	String getSelectedMatricesDescription();

	String getSelectedHDSkinningDescription();
}
