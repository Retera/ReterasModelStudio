package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;

import java.awt.*;
import java.awt.event.MouseEvent;

public final class ViewportActivityManager implements SelectionListener, ModelEditorChangeListener {
	private ViewportActivity currentActivity;
	private CursorManager cursorManager;
	private ModelEditor newModelEditor;
	private AbstractSelectionManager newSelection;

	public ViewportActivityManager(ViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
	}

	public void setCurrentActivity(ViewportActivity currentActivity) {
		this.currentActivity = currentActivity;
		if (this.currentActivity != null) {
			this.currentActivity.viewportChanged(cursorManager);
			this.currentActivity.onSelectionChanged(newSelection);
			this.currentActivity.modelEditorChanged(newModelEditor);
		}
	}

//	@Override
	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mousePressed(e, coordinateSystem);
		}
	}

//	@Override
	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseReleased(e, coordinateSystem);
		}
	}

//	@Override
	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseMoved(e, coordinateSystem);
		}
	}

//	@Override
	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseDragged(e, coordinateSystem);
		}
	}

//	@Override
	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (this.currentActivity != null) {
			currentActivity.render(g, coordinateSystem, renderModel, isAnimated);
		}
	}

//	@Override
	public boolean isEditing() {
		if (this.currentActivity != null) {
			return currentActivity.isEditing();
		}
		return false;
	}

	@Override
	public void onSelectionChanged(AbstractSelectionManager newSelection) {
		this.newSelection = newSelection;
		if (currentActivity != null) {
			currentActivity.onSelectionChanged(newSelection);
		}
	}

//	@Override
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
