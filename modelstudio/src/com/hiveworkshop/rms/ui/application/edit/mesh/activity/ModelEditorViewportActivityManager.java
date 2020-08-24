package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

import java.awt.*;
import java.awt.event.MouseEvent;

public final class ModelEditorViewportActivityManager implements ModelEditorViewportActivity {
	private ModelEditorViewportActivity currentActivity;
	private CursorManager cursorManager;
	private CoordinateSystem coordinateSystem;
	private ModelEditor newModelEditor;
	private SelectionView newSelection;

	public ModelEditorViewportActivityManager(final ModelEditorViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public void setCurrentActivity(final ModelEditorViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
		if (this.currentActivity != null) {
			this.currentActivity.viewportChanged(cursorManager);
			this.currentActivity.onSelectionChanged(newSelection);
			this.currentActivity.modelEditorChanged(newModelEditor);
		}
	}

	@Override
	public void mousePressed(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		currentActivity.mousePressed(e, coordinateSystem);
	}

	@Override
	public void mouseReleased(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		currentActivity.mouseReleased(e, coordinateSystem);
	}

	@Override
	public void mouseMoved(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		currentActivity.mouseMoved(e, coordinateSystem);
	}

	@Override
	public void mouseDragged(final MouseEvent e, final CoordinateSystem coordinateSystem) {
		currentActivity.mouseDragged(e, coordinateSystem);
	}

	@Override
	public void render(final Graphics2D g, final CoordinateSystem coordinateSystem, final RenderModel renderModel) {
		currentActivity.render(g, coordinateSystem, renderModel);
	}

	@Override
	public void renderStatic(final Graphics2D g, final CoordinateSystem coordinateSystem) {
		currentActivity.renderStatic(g, coordinateSystem);
	}

	@Override
	public void modelChanged() {
		currentActivity.modelChanged();
	}

	@Override
	public boolean isEditing() {
		return currentActivity.isEditing();
	}

	@Override
	public void onSelectionChanged(final SelectionView newSelection) {
		this.newSelection = newSelection;
		if (currentActivity != null) {
			currentActivity.onSelectionChanged(newSelection);
		}
	}

	@Override
	public void viewportChanged(final CursorManager cursorManager) {
		this.cursorManager = cursorManager;
		if (currentActivity != null) {
			currentActivity.viewportChanged(cursorManager);
		}
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		this.newModelEditor = newModelEditor;
		if (currentActivity != null) {
			currentActivity.modelEditorChanged(newModelEditor);
		}
	}

}
