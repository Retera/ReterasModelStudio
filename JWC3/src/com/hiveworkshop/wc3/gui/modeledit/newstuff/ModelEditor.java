package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.util.Collection;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * SelectionManager should've been like this so it didn't wrap the selection in
 * silly Item objects, but the code hasn't been reworked to be this thing yet
 *
 * @author Eric
 *
 * @param <T>
 */
public interface ModelEditor {
	UndoAction autoCenterSelectedBones();

	UndoAction setSelectedBoneName(String name);

	UndoAction addTeamColor(ModelStructureChangeListener modelStructureChangeListener);

	// should move to a Util at a later date, if it does not require internal
	// knowledge of center point from state holders
	UndoAction translate(double x, double y, double z);

	UndoAction setPosition(double x, double y, double z);

	UndoAction rotate(double rotateX, double rotateY, double rotateZ);

	UndoAction setMatrix(Collection<Bone> bones);

	UndoAction deleteSelectedComponents(ModelStructureChangeListener modelStructureChangeListener);

	UndoAction snapNormals();

	UndoAction mirror(byte dim, boolean flipModel);

	UndoAction flipSelectedFaces();

	UndoAction flipSelectedNormals();

	UndoAction snapSelectedVertices();

	UndoAction snapSelectedNormals();

	UndoAction beginExtrudingSelection();

	UndoAction beginExtendingSelection();

	UndoAction cloneSelectedComponents(ModelStructureChangeListener modelStructureChangeListener,
			ClonedNodeNamePicker clonedNodeNamePicker);

	void rawTranslate(double x, double y, double z);

	void rawScale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ);

	void rawRotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ);

	void rawRotate3d(Vertex center, Vertex axis, double radians);

	// TODO maybe put this on selection view
	void renderSelection(ModelElementRenderer renderer, final CoordinateSystem coordinateSystem);
}
