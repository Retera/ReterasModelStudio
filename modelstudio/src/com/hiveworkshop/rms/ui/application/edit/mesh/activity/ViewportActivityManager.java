package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.AbstractModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.transAct.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ViewBox;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionListener;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
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
//		this.currentActivity = new MultiManipulatorActivity(action, modelHandler, modelEditorManager);

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

	public void mousePressed(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (isEditing(e)) {
			activeActivity = currentActivity;
		} else if (isSelect(e)) {
			activeActivity = selectActivity;
		}
		if (this.activeActivity != null) {
			activeActivity.mousePressed(e, coordinateSystem);
		}
	}

	public void mouseReleased(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.activeActivity != null) {
			activeActivity.mouseReleased(e, coordinateSystem);
		}
		activeActivity = null;
	}

	public void mouseMoved(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.currentActivity != null) {
			currentActivity.mouseMoved(e, coordinateSystem);
		}
//		if (this.activeActivity != null) {
//			activeActivity.mouseMoved(e, coordinateSystem);
//		}
	}

	public void mouseDragged(MouseEvent e, CoordinateSystem coordinateSystem) {
		if (this.activeActivity != null) {
			activeActivity.mouseDragged(e, coordinateSystem);
		}
	}

	public void render(Graphics2D g, CoordinateSystem coordinateSystem, RenderModel renderModel, boolean isAnimated) {
//		if (this.activeActivity != null) {
//			activeActivity.render(g, coordinateSystem, renderModel, isAnimated);
//		}
		selectActivity.render(g, coordinateSystem, renderModel, isAnimated);
		if (this.currentActivity != null) {
			currentActivity.render(g, coordinateSystem, renderModel, isAnimated);
		}
	}


	public void mousePressed(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (isEditing(e)) {
			activeActivity = currentActivity;
		} else if (isSelect(e)) {
			activeActivity = selectActivity;
		}
		if (this.activeActivity != null) {
			activeActivity.mousePressed(e, viewProjectionMatrix, sizeAdj);
		}
	}
	public void mousePressed(MouseEvent e, ViewBox viewBox, double sizeAdj) {
		if (isSelect(e)) {
			activeActivity = selectActivity;
			selectActivity.mousePressed(e, viewBox, sizeAdj);
		}
	}

	public void mouseReleased(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (this.activeActivity != null) {
			activeActivity.mouseReleased(e, viewProjectionMatrix, sizeAdj);
		}
		activeActivity = null;
	}

	public void mouseReleased(MouseEvent e, ViewBox viewBox, double sizeAdj) {
		selectActivity.mouseReleased(e, viewBox, sizeAdj);
		activeActivity = null;
	}

	public void mouseMoved(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (this.activeActivity != null) {
			activeActivity.mouseMoved(e, viewProjectionMatrix, sizeAdj);
		}
	}

	public void mouseDragged(MouseEvent e, Mat4 viewProjectionMatrix, double sizeAdj) {
		if (this.activeActivity != null) {
			activeActivity.mouseDragged(e, viewProjectionMatrix, sizeAdj);
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
//		else {
//			return selectTransActivity.isEditing();
//		}

//		if (this.activeActivity != null) {
//			return activeActivity.isEditing();
//		} else {
//			return selectTransActivity.isEditing();
//		}

		return false;
	}

	private boolean isSelect(MouseEvent e){
		ProgramPreferences prefs = ProgramGlobals.getPrefs();
		int event_xor_MB = e.getModifiersEx() ^ prefs.getSelectMouseButton();
		return event_xor_MB == 0 // no modifiers
				|| (event_xor_MB ^ prefs.getAddSelectModifier()) == 0 // add selection modifier
				||  (event_xor_MB ^ prefs.getRemoveSelectModifier()) == 0; // remove selection modifier
	}
	private boolean isEditing(MouseEvent e){
		boolean isEd = (ProgramGlobals.getPrefs().getModifyMouseButton() & e.getModifiersEx()) > 0 && !selectionManager.isEmpty();
		System.out.println("is editing action: " + isEd);
		return (ProgramGlobals.getPrefs().getModifyMouseButton() & e.getModifiersEx()) > 0 && !selectionManager.isEmpty();
	}

}
