package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.activity.TVertexEditorViewportActivity;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

import java.awt.*;
import java.awt.event.MouseEvent;

public final class DoNothingTVertexActivity implements TVertexEditorViewportActivity {

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
	}

	@Override
	public void modelChanged() {
	}

	@Override
	public void editorChanged(final TVertexEditor newModelEditor) {
	}

	@Override
	public void viewportChanged(final CursorManager cursorManager) {
	}

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) {
	}

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) {
	}

	@Override
	public void render(final Graphics2D g, final CoordinateSystem coordinateSystem, final RenderModel renderModel) {
	}

	@Override
	public void renderStatic(final Graphics2D g, final CoordinateSystem coordinateSystem) {
	}

	@Override
	public boolean isEditing() {
		return false;
	}

}
