package com.hiveworkshop.rms.ui.preferences;

import java.awt.*;

public enum ColorThing {
	VERTEX(new Color(127, 076, 178, 178)),
	VERTEX_SELECTED(new Color(255, 000, 000, 178)),
	VERTEX_UNEDITABLE(new Color(102, 076, 178, 102)),
	VERTEX_HIGHLIGHTED(new Color(102, 120, 255, 255)),
	TRIANGLE_AREA(new Color(115, 115, 255, 030)),
	TRIANGLE_AREA_SELECTED(new Color(255, 115, 115, 030)),
	TRIANGLE_AREA_UNEDITABLE(new Color(115, 115, 115, 030)),
	TRIANGLE_AREA_HIGHLIGHTED(new Color(115, 255, 115, 030)),
	TRIANGLE_LINE(new Color(115, 115, 255, 255)),
	TRIANGLE_LINE_SELECTED(new Color(255, 115, 115, 255)),
	TRIANGLE_LINE_UNEDITABLE(new Color(115, 115, 115, 178)),
	TRIANGLE_LINE_HIGHLIGHTED(new Color(115, 255, 115, 255)),
	NODE(new Color(150, 100, 190, 255)),
	NODE_SELECTED(new Color(255, 000, 255, 255)),
	NODE_UNEDITABLE(new Color(50, 100, 255, 102)),
	NODE_HIGHLIGHTED(new Color(50, 200, 255, 255)),
	SELECTED_NODES_CHILDREN(new Color(140, 030, 255, 255)),
	BACKGROUND_COLOR(new Color(80, 80, 80)),
	UV_EDIT_BACKGROUND_COLOR(new Color(80, 80, 80));
	final Color internalColor;

	ColorThing(Color color) {
		internalColor = color;
	}

	public Color getInternalColor() {
		return internalColor;
	}

	public String getTextKeyString() {
		return name();
	}
}
