package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.*;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;

public class ModelEditorManipulatorBuilder extends ManipulatorBuilder {
	ModelEditorActionType3 currentAction;

	public ModelEditorManipulatorBuilder(ModelEditorManager modelEditorManager, ModelHandler modelHandler, ModelEditorActionType3 currentAction) {
		super(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), modelHandler);
		this.currentAction = currentAction;
		createWidget(currentAction);
	}

	protected Manipulator getBuilder(AbstractSelectionManager selectionManager, MoveDimension directionByMouse) {
		return switch (currentAction) {
			case TRANSLATION -> new MoveManipulator(getModelEditor(), directionByMouse);
			case ROTATION -> new RotateManipulator(getModelEditor(), selectionManager, directionByMouse);
			case SCALING -> new ScaleManipulator(getModelEditor(), selectionManager, directionByMouse);
			case EXTRUDE -> new ExtrudeManipulator(getModelEditor(), directionByMouse);
			case EXTEND -> new ExtendManipulator(getModelEditor(), directionByMouse);
			case SQUAT -> new SquatToolManipulator(getModelEditor(), selectionManager, directionByMouse);
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
