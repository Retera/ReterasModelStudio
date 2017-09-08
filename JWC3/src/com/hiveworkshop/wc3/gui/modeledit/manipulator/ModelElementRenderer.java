package com.hiveworkshop.wc3.gui.modeledit.manipulator;

import java.awt.Color;

import com.hiveworkshop.wc3.gui.modeledit.viewport.NodeIconPalette;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Vertex;

public interface ModelElementRenderer {
	void renderFace(Color borderColor, Color color, Vertex a, Vertex b, Vertex c);

	void renderVertex(Color color, Vertex vertex);

	void renderIdObject(IdObject object, NodeIconPalette nodeIconPalette, Color lightColor, Color pivotPointColor);

	void renderCamera(Color boxColor, Vertex position, Color targetColor, Vertex targetPosition);
}
