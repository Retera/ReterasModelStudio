package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ButtonType;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

import java.awt.*;

public interface ManipulatorBuilder {
	Cursor getCursorAt(int x, int y, CoordinateSystem coordinateSystem, SelectionView selectionView);

	Manipulator buildActivityListener(int x, int y, ButtonType clickedButton, CoordinateSystem coordinateSystem, SelectionView selectionView);

	void render(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView, RenderModel renderModel);

	void renderStatic(Graphics2D graphics, CoordinateSystem coordinateSystem, SelectionView selectionView);
}
