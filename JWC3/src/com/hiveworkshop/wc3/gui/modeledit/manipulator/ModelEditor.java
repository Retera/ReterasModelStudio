package com.hiveworkshop.wc3.gui.modeledit.manipulator;

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
public interface ModelEditor {
	void cloneSelectedComponents();

	void translate(double x, double y, double z);

	void scale(double centerX, double centerY, double centerZ, double scaleX, double scaleY, double scaleZ);

	void rotate2d(double centerX, double centerY, double centerZ, double radians, byte firstXYZ, byte secondXYZ);

	void rotate3d(Vertex center, Vertex axis, double radians);

	void renderSelection(ModelElementRenderer renderer, final CoordinateSystem coordinateSystem);
}
