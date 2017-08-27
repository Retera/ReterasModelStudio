package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Rectangle;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Vertex;

/**
 * SelectionManager should've been like this so it didn't wrap the selection in
 * silly Item objects, but the code hasn't been reworked to be this thing yet
 *
 * @author Eric
 *
 * @param <T>
 */
public interface ModelEditor<T> {
	Set<T> getSelection();

	Vertex getSelectionCenter();

	void setSelectedRegion(Rectangle region, CoordinateSystem coordinateSystem);

	void removeSelectedRegion(Rectangle region, CoordinateSystem coordinateSystem);

	void addSelectedRegion(Rectangle region, CoordinateSystem coordinateSystem);

	void expandSelection();

	void invertSelection();

	void selectAll();

	void cloneSelectedComponents();

	void translate(float x, float y, float z);

	void scale(float centerX, float centerY, float centerZ, float scaleX, float scaleY, float scaleZ);

	void rotate2d(float centerX, float centerY, float centerZ, float radians, byte firstXYZ, byte secondXYZ);

	void rotate3d(Vertex center, Vertex axis, float radians);

	void renderSelection(ModelElementRenderer renderer, final CoordinateSystem coordinateSystem);
}
