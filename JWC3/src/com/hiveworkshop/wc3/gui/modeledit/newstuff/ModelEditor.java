package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * SelectionManager should've been like this so it didn't wrap the selection in silly Item objects, but the code hasn't
 * been reworked to be this thing yet
 *
 * @author Eric
 *
 * @param <T>
 */
public interface ModelEditor {
	UndoAction autoCenterSelectedBones();

	UndoAction setSelectedBoneName(String name);

	UndoAction addTeamColor();

	UndoAction splitGeoset();

	// should move to a Util at a later date, if it does not require internal
	// knowledge of center point from state holders
	UndoAction translate(double x, double y, double z);

	UndoAction setPosition(Vertex center, double x, double y, double z);

	UndoAction rotate(Vertex center, double rotateX, double rotateY, double rotateZ);

	UndoAction addVertex(double x, double y, double z, Vertex preferredNormalFacingVector);

	GenericMoveAction addPlane(double x, double y, double x2, double y2, byte dim1, byte dim2, Vertex facingVector,
			int numberOfWidthSegments, int numberOfHeightSegments);

	GenericMoveAction addBox(double x, double y, double x2, double y2, byte dim1, byte dim2, Vertex facingVector,
			int numberOfLengthSegments, int numberOfWidthSegments, int numberOfHeightSegments);

	UndoAction setMatrix(Collection<Bone> bones);

	UndoAction createKeyframe(ModelEditorActionType actionType);

	UndoAction deleteSelectedComponents();

	UndoAction snapNormals();

	UndoAction recalcNormals();

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

	UndoAction invertSelection();

	UndoAction selectAll();

	UndoAction hideComponent(ListView<? extends SelectableComponent> selectableComponents,
			EditabilityToggleHandler editabilityToggleHandler, Runnable refreshGUIRunnable);

	UndoAction showComponent(EditabilityToggleHandler editabilityToggleHandler);

	void selectByVertices(Collection<? extends Vertex> newSelection);

	boolean canSelectAt(Point point, CoordinateSystem axes);

	GenericMoveAction beginTranslation();

	GenericScaleAction beginScaling(double centerX, double centerY, double centerZ);

	GenericRotateAction beginRotation(double centerX, double centerY, double centerZ, byte firstXYZ, byte secondXYZ);

	GenericRotateAction beginSquatTool(double centerX, double centerY, double centerZ, byte firstXYZ, byte secondXYZ);

	void rawTranslate(double x, double y, double z);

	void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ);

	void rawRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ);

	void rawRotate3d(Vertex center, Vertex axis, double radians);

	Vertex getSelectionCenter();

	CopiedModelData copySelection();

	// true if we conceptually are editing/operating on top of an animated model, instead of a static one
	// -- this is *definitely* a bit of a hack
	boolean editorWantsAnimation();

	UndoAction createFaceFromSelection(Vertex preferredFacingVector);
}
