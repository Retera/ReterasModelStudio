package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

import java.awt.*;
import java.awt.event.MouseEvent;

public final class DoNothingActivity implements ModelEditorViewportActivity {

	@Override
	public void onSelectionChanged(SelectionView newSelection) {
	}

	@Override
	public void modelChanged() {
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
	}

	@Override
	public void viewportChanged(CursorManager cursorManager) {
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
	}

	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel) {
	}

	@Override
	public void renderStatic(Graphics2D g, CoordinateSystem coordinateSystem) {
	}

	@Override
	public boolean isEditing() {
		return false;
	}

}
