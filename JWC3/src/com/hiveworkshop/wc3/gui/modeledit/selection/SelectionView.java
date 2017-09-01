package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.awt.Point;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.manipulator.ModelElementRenderer;
import com.hiveworkshop.wc3.mdl.Vertex;

public interface SelectionView {
	Vertex getCenter();

	// needs to be coord system, not coord axes, so that
	// vertex selection view knows the zoom level,
	// so that the width and height of a vertex in pixels
	// is zoom independent
	boolean canSelectAt(Point point, CoordinateSystem axes);

	double getCircumscribedSphereRadius(Vertex center);

	void renderSelection(ModelElementRenderer renderer, final CoordinateSystem coordinateSystem);
}
