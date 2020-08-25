package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public final class DoNothingActivity implements ModelEditorViewportActivity {

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modelChanged() {
		// TODO Auto-generated method stub

	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewportChanged(final CursorManager cursorManager) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render(final Graphics2D g, final CoordinateSystem coordinateSystem, final RenderModel renderModel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void renderStatic(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isEditing() {
		// TODO Auto-generated method stub
		return false;
	}

}
