package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.util.Vec3;

public interface SelectionItemView {
	void render(Graphics2D graphics, CoordinateSystem coordinateSystem);

	boolean hitTest(Point2D point, CoordinateSystem coordinateSystem);

	boolean hitTest(Rectangle2D rectangle, CoordinateSystem coordinateSystem);

	Vec3 getCenter();

	List<? extends SelectionItemView> getConnectedComponents();
}
