package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public final class ViewportActivityManager implements SelectionListener {
	private ViewportActivity currentActivity;
	private Consumer<Cursor> cursorManager;
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

	public void viewportChanged(Consumer<Cursor> cursorManager) {
		this.cursorManager = cursorManager;
		if (currentActivity != null) {
			currentActivity.viewportChanged(cursorManager);
		}
	}

	public void modelEditorChanged(ModelEditor newModelEditor) {
		this.newModelEditor = newModelEditor;
		if (currentActivity != null) {
			currentActivity.modelEditorChanged(newModelEditor);
		}
	}

	@Override
	public void onSelectionChanged(AbstractSelectionManager newSelection) {
		this.newSelection = newSelection;
		if (currentActivity != null) {
			currentActivity.onSelectionChanged(newSelection);
		}
	}

	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mousePressed(e, coordinateSystem);
		}
	}

	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseReleased(e, coordinateSystem);
		}
	}

	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseMoved(e, coordinateSystem);
		}
	}

	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseDragged(e, coordinateSystem);
		}
	}

	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		if (this.currentActivity != null) {
			currentActivity.render(g, coordinateSystem, renderModel, isAnimated);
		}
	}


	public void mousePressed(MouseEvent e, CameraHandler coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mousePressed(e, coordinateSystem);
		}
	}

	public void mouseReleased(MouseEvent e, CameraHandler coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseReleased(e, coordinateSystem);
		}
	}

	public void mouseMoved(MouseEvent e, CameraHandler coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseMoved(e, coordinateSystem);
		}
	}

	public void mouseDragged(MouseEvent e, CameraHandler coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseDragged(e, coordinateSystem);
		}
	}

//	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
//		if (this.currentActivity != null) {
//			currentActivity.render(g, coordinateSystem, renderModel, isAnimated);
//		}
//	}

	public boolean isEditing() {
		if (this.currentActivity != null) {
			return currentActivity.isEditing();
		}
		return false;
	}

}
