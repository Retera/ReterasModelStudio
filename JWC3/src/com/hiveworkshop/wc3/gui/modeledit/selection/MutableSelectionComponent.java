package com.hiveworkshop.wc3.gui.modeledit.selection;

import com.hiveworkshop.wc3.mdl.Vertex;

public interface MutableSelectionComponent {
	void translate(float x, float y, float z);

	void scale(float centerX, float centerY, float centerZ, float x, float y, float z);

	void rotate(float centerX, float centerY, float centerZ, float radians, byte firstXYZ, byte secondXYZ);

	void rotate(Vertex center, Vertex perpendicular, float radians);

	void delete();
}
