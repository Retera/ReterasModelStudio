package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.util.Vec2;

import java.awt.*;

public interface TVertexModelElementRenderer {
	void renderFace(Color borderColor, Color color, Vec2 a, Vec2 b, Vec2 c);

	void renderVertex(Color color, Vec2 vertex);
}
