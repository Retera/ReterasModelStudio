package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.AbstractCamera;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.SelectionBoxHelper;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.MouseEventHelpers;
import com.hiveworkshop.rms.util.Mat4;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public final class ViewportActivityManager implements SelectionListener {
	private final AbstractModelEditorManager modelEditorManager;
	private final AbstractSelectionManager selectionManager;
	private final ModelHandler modelHandler;
	private final SelectActivity selectActivity;
	private ViewportActivity activeActivity;
	private ViewportActivity currentActivity;
	private Consumer<Cursor> cursorManager;
	private ModelEditor newModelEditor;
	private AbstractSelectionManager newSelection;

	public ViewportActivityManager(ModelHandler modelHandler, AbstractModelEditorManager modelEditorManager) {
		this.currentActivity = null;
		this.modelHandler = modelHandler;
		this.modelEditorManager = modelEditorManager;
		selectActivity = new SelectActivity(modelHandler, modelEditorManager);
		selectionManager = modelEditorManager.getSelectionView();
		newSelection = modelEditorManager.getSelectionView();

		modelEditorManager.setSelectionListener(this);
		modelEditorManager.setViewportActivityManager(this);
	}

	public void setCurrentActivity(ViewportActivity currentActivity) {
		// ToDo check if this should be current or active
		this.currentActivity = currentActivity;
		if (this.currentActivity != null) {
			this.currentActivity.viewportChanged(cursorManager);
			this.currentActivity.onSelectionChanged(newSelection);
			this.currentActivity.modelEditorChanged(newModelEditor);
		}
	}

	public void setCurrentActivity(ModelEditorActionType3 action) {
		this.currentActivity =  switch (action) {
			case TRANSLATION -> new MoveActivity(modelHandler, modelEditorManager);
			case ROTATION -> new RotateActivity(modelHandler, modelEditorManager);
			case SCALING -> new ScaleActivity(modelHandler, modelEditorManager);
			case EXTRUDE -> new ExtrudeActivity(modelHandler, modelEditorManager);
			case EXTEND -> new ExtendActivity(modelHandler, modelEditorManager);
			case SQUAT -> new SquatActivity(modelHandler, modelEditorManager);
		};
		this.currentActivity.viewportChanged(cursorManager);
		this.currentActivity.onSelectionChanged(newSelection);
		this.currentActivity.modelEditorChanged(newModelEditor);
	}

	public void viewportChanged(Consumer<Cursor> cursorManager) {
		this.cursorManager = cursorManager;
		selectActivity.viewportChanged(cursorManager);
		if (currentActivity != null) {
			currentActivity.viewportChanged(cursorManager);
		}
	}

	public void modelEditorChanged(ModelEditor newModelEditor) {
		this.newModelEditor = newModelEditor;
		selectActivity.modelEditorChanged(newModelEditor);
		if (currentActivity != null) {
			currentActivity.modelEditorChanged(newModelEditor);
		}
	}

	@Override
	public void onSelectionChanged(AbstractSelectionManager newSelection) {
		this.newSelection = newSelection;
		selectActivity.onSelectionChanged(newSelection);
		if (currentActivity != null) {
			currentActivity.onSelectionChanged(newSelection);
		}
	}


	public void render(Graphics2D g, AbstractCamera coordinateSystem, RenderModel renderModel, boolean isAnimated) {
		selectActivity.render(g, coordinateSystem, renderModel, isAnimated);
		if (this.currentActivity != null) {
			currentActivity.render(g, coordinateSystem, renderModel, isAnimated);
		}
	}


	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, SelectionBoxHelper viewBox, double sizeAdj) {
		if (isEditing(e)) {
			activeActivity = currentActivity;
			activeActivity.mousePressed(e, viewProjectionMatrix, sizeAdj);
		} else if (isSelect(e)) {
			activeActivity = selectActivity;
			selectActivity.mousePressed(e, viewBox, sizeAdj);
		} else if (this.activeActivity != null){
			System.out.println("active Activity: " + this.activeActivity);
			activeActivity.mousePressed(e, viewProjectionMatrix, sizeAdj);
		}
	}

	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (this.activeActivity != null) {
			activeActivity.mouseReleased(e, viewProjectionMatrix, sizeAdj);
		}
		activeActivity = null;
	}

	public void mouseReleased(MouseEvent e, SelectionBoxHelper viewBox, double sizeAdj) {
		selectActivity.mouseReleased(e, viewBox, sizeAdj);
		activeActivity = null;
	}

	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (this.activeActivity != null) {
			activeActivity.mouseMoved(e, viewProjectionMatrix, sizeAdj);
		} else if (currentActivity != null) {
			currentActivity.mouseMoved(e, viewProjectionMatrix, sizeAdj);
		} else {
			selectActivity.mouseMoved(e, viewProjectionMatrix, sizeAdj);
		}
	}

	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (this.activeActivity != null) {
			activeActivity.mouseDragged(e, viewProjectionMatrix, sizeAdj);
		} else {
			mouseMoved(e, viewProjectionMatrix, sizeAdj);
		}
	}


	public boolean isEditing() {
		if (this.currentActivity != null) {
			return currentActivity.isEditing();
		}

		return false;
	}
	public boolean isSelecting() {
		return selectActivity.isEditing();
	}

	private boolean isSelect(MouseEvent e){
		ProgramPreferences prefs = ProgramGlobals.getPrefs();
		return MouseEventHelpers.matches(e, prefs.getSelectMouseButton(), prefs.getAddSelectModifier(), prefs.getRemoveSelectModifier());
	}
	private boolean isEditing(MouseEvent e){
		ProgramPreferences prefs = ProgramGlobals.getPrefs();
		return MouseEventHelpers.matches(e, prefs.getModifyMouseButton(),prefs.getSnapTransformModifier()) && !(selectionManager.isEmpty() && currentActivity.selectionNeeded());
	}

}
