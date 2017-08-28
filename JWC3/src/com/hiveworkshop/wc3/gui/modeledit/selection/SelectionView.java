package com.hiveworkshop.wc3.gui.modeledit.selection;

import com.hiveworkshop.wc3.mdl.Vertex;

public interface SelectionView {
	Vertex getCenter();

	double getCircumscribedSphereRadius(Vertex center);
}
