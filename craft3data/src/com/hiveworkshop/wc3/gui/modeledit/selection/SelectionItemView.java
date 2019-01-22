package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.mdl.Vertex;

public interface SelectionItemView {
	void render(Graphics2D graphics, CoordinateSystem coordinateSystem);

	boolean hitTest(Point2D point, CoordinateSystem coordinateSystem);

	boolean hitTest(Rectangle2D rectangle, CoordinateSystem coordinateSystem);

	Vertex getCenter();

	ListView<? extends SelectionItemView> getConnectedComponents();
}
