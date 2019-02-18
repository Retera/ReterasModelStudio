package com.hiveworkshop.wc3.gui.modeledit.activity;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;
import com.hiveworkshop.wc3.mdl.RenderModel;

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
