package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.awt.Color;

import com.hiveworkshop.rms.util.Vertex2;

public interface TVertexModelElementRenderer {
	void renderFace(Color borderColor, Color color, Vertex2 a, Vertex2 b, Vertex2 c);

	void renderVertex(Color color, Vertex2 vertex);
}
