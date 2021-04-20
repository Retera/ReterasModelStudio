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

	public ModelEditorViewportActivityManager(ModelEditorViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public void setCurrentActivity(ModelEditorViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
		if (this.currentActivity != null) {
			this.currentActivity.viewportChanged(cursorManager);
			this.currentActivity.onSelectionChanged(newSelection);
			this.currentActivity.modelEditorChanged(newModelEditor);
		}
	}

	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		currentActivity.mousePressed(e, coordinateSystem);
	}

	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		currentActivity.mouseReleased(e, coordinateSystem);
	}

	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		currentActivity.mouseMoved(e, coordinateSystem);
	}

	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
		currentActivity.mouseDragged(e, coordinateSystem);
	}

	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel) {
		currentActivity.render(g, coordinateSystem, renderModel);
	}

	@Override
	public void renderStatic(Graphics2D g, CoordinateSystem coordinateSystem) {
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
	public void onSelectionChanged(SelectionView newSelection) {
		this.newSelection = newSelection;
		if (currentActivity != null) {
			currentActivity.onSelectionChanged(newSelection);
		}
	}

	@Override
	public void viewportChanged(CursorManager cursorManager) {
		this.cursorManager = cursorManager;
		if (currentActivity != null) {
			currentActivity.viewportChanged(cursorManager);
		}
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		this.newModelEditor = newModelEditor;
		if (currentActivity != null) {
			currentActivity.modelEditorChanged(newModelEditor);
		}
	}

}
