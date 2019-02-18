package com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv;

import java.awt.Cursor;
import java.awt.Graphics2D;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.activity.ButtonType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditorChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexSelectionView;
import com.hiveworkshop.wc3.mdl.RenderModel;

public interface TVertexEditorManipulatorBuilder extends TVertexEditorChangeListener {
	Cursor getCursorAt(int x, int y, CoordinateSystem coordinateSystem, TVertexSelectionView selectionView);

	Manipulator buildActivityListener(int x, int y, ButtonType clickedButton, CoordinateSystem coordinateSystem,
			TVertexSelectionView selectionView);

	void render(Graphics2D graphics, CoordinateSystem coordinateSystem, TVertexSelectionView selectionView,
			RenderModel renderModel);

	void renderStatic(Graphics2D graphics, CoordinateSystem coordinateSystem, TVertexSelectionView selectionView);
}
