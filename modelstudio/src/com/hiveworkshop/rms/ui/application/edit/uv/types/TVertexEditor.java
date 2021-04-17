package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.panel.UVPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ComponentVisibilityListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

/**
 * So, in some ideal future this would be an implementation of the ModelEditor
 * interface, I believe, and the editor would be operating on an interface who
 * could capture clicks and convert them into 2D operations regardless of
 * whether the underlying thing being editor was UV or Mesh.
 *
 * It isn't like that right now, though, so this is just going to be a 2D copy
 * pasta.
 */
public interface TVertexEditor extends ComponentVisibilityListener {
	// should move to a Util at a later date, if it does not require internal
	// knowledge of center point from state holders
	UndoAction translate(double x, double y);

	UndoAction setPosition(Vec2 center, double x, double y);

	UndoAction rotate(Vec2 center, double rotateRadians);

	UndoAction mirror(byte dim, double centerX, double centerY);

	UndoAction snapSelectedVertices();

	UndoAction setSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction removeSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction addSelectedRegion(Rectangle2D region, CoordinateSystem coordinateSystem);

	UndoAction expandSelection();

	UndoAction invertSelection();

	UndoAction selectAll();

	UndoAction selectFromViewer(SelectionView viewerSelectionView);

	void selectByVertices(Collection<? extends Vec3> newSelection);

	boolean canSelectAt(Point point, CoordinateSystem axes);

	GenericMoveAction beginTranslation();

	GenericScaleAction beginScaling(double centerX, double centerY);

	GenericRotateAction beginRotation(double centerX, double centerY, byte dim1, byte dim2);

	void rawTranslate(double x, double y);

	void rawScale(double centerX, double centerY, double scaleX, double scaleY);

	void rawRotate2d(double centerX, double centerY, double radians, byte firstXYZ, byte secondXYZ);

	Vec2 getSelectionCenter();

	void setUVLayerIndex(int uvLayerIndex);

	int getUVLayerIndex();

	UndoAction remap(byte xDim, byte yDim, UVPanel.UnwrapDirection unwrapDirection);

}
