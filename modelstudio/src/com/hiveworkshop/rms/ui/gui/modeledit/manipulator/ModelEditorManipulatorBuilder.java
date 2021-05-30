package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;

public class ModelEditorManipulatorBuilder extends ManipulatorBuilder {
	ModelEditorActionType3 currentAction;

	public ModelEditorManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler, ModelEditorActionType3 currentAction) {
		super(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), modelHandler);
		this.currentAction = currentAction;
		createWidget(currentAction);
	}

	protected Manipulator getManipulator(AbstractSelectionManager selectionManager, MoveDimension directionByMouse) {
		return switch (currentAction) {
			case TRANSLATION -> new MoveManipulator(modelEditor, directionByMouse);
			case ROTATION -> new RotateManipulator(modelEditor, selectionManager, directionByMouse);
			case SCALING -> new ScaleManipulator(modelEditor, selectionManager, directionByMouse);
			case EXTRUDE -> new ExtrudeManipulator(modelEditor, directionByMouse);
			case EXTEND -> new ExtendManipulator(modelEditor, directionByMouse);
			case SQUAT -> new SquatToolManipulator(modelEditor, selectionManager, directionByMouse);
		};
	}

	private void createWidget(ModelEditorActionType3 action) {
		if(action == null){
			widget = new MoverWidget();
		} else {
			switch (action) {
				case TRANSLATION, EXTRUDE, EXTEND -> widget = new MoverWidget();
				case ROTATION, SQUAT -> widget = new RotatorWidget();
				case SCALING -> widget = new ScalerWidget();
			};
		}
	}

	protected void setWidgetPoint(AbstractSelectionManager selectionManager) {
		widget.setPoint(selectionManager.getCenter());
	}
}
