package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.awt.Color;

import com.hiveworkshop.rms.editor.model.TVertex;

public interface TVertexModelElementRenderer {
	void renderFace(Color borderColor, Color color, TVertex a, TVertex b, TVertex c);

	void renderVertex(Color color, TVertex vertex);
}
