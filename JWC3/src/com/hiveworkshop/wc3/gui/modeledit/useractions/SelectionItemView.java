package com.hiveworkshop.wc3.gui.modeledit.useractions;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Point2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Vertex;

public interface SelectionItemView {
	void render(Graphics2D graphics, CoordinateSystem coordinateSystem);

	boolean hitTest(Point2D point);

	boolean hitTest(Rectangle rectangle);

	Vertex getCenter();
}
