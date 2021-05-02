package com.hiveworkshop.rms.ui.application.edit.uv.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.CursorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

import java.awt.*;
import java.awt.event.MouseEvent;

public final class TVertexEditorViewportActivityManager implements TVertexEditorViewportActivity {
	private TVertexEditorViewportActivity currentActivity;
	private CursorManager cursorManager;
	private TVertexEditor newModelEditor;
	private SelectionView newSelection;

	public TVertexEditorViewportActivityManager(TVertexEditorViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public void setCurrentActivity(TVertexEditorViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
		if (this.currentActivity != null) {
			this.currentActivity.viewportChanged(cursorManager);
			this.currentActivity.onSelectionChanged(newSelection);
			this.currentActivity.editorChanged(newModelEditor);
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
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		currentActivity.render(g, coordinateSystem, renderModel, isAnimated);
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
	public void editorChanged(TVertexEditor newModelEditor) {
		this.newModelEditor = newModelEditor;
		if (currentActivity != null) {
			currentActivity.editorChanged(newModelEditor);
		}
	}

}
