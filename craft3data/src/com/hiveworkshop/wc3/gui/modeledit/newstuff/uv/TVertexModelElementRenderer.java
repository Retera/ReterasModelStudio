package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv;

import java.awt.Color;

import com.hiveworkshop.wc3.mdl.TVertex;

public interface TVertexModelElementRenderer {
	void renderFace(Color borderColor, Color color, TVertex a, TVertex b, TVertex c);

	void renderVertex(Color color, TVertex vertex);
}
