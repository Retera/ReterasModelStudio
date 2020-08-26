package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.uv;

import java.awt.Color;

import com.hiveworkshop.rms.util.Vector2;

public interface TVertexModelElementRenderer {
	void renderFace(Color borderColor, Color color, Vector2 a, Vector2 b, Vector2 c);

	void renderVertex(Color color, Vector2 vertex);
}
