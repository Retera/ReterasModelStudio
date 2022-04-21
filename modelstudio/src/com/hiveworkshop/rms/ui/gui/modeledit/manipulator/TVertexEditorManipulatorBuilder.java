package com.hiveworkshop.rms.ui.gui.modeledit.manipulator;

import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv.MoveTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv.RotateTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.manipulator.uv.ScaleTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType3;

public class TVertexEditorManipulatorBuilder extends ManipulatorBuilder {
	ModelEditorActionType3 currentAction2;


	public TVertexEditorManipulatorBuilder(TVertexEditorManager modelEditorManager,
	                                       ModelHandler modelHandler,
	                                       ModelEditorActionType3 currentAction) {
		super(modelEditorManager.getModelEditor(), modelEditorManager.getSelectionView(), modelHandler);
		this.currentAction2 = currentAction;
		createWidget(currentAction);
	}

	protected Manipulator getManipulator(AbstractSelectionManager selectionManager, MoveDimension directionByMouse) {
		return switch (currentAction2) {
			case TRANSLATION, EXTRUDE, EXTEND -> new MoveTVertexManipulator(modelEditor, selectionManager, directionByMouse);
			case ROTATION, SQUAT -> new RotateTVertexManipulator(modelEditor, selectionManager, directionByMouse);
			case SCALING -> new ScaleTVertexManipulator(modelEditor, selectionManager, directionByMouse);
		};
	}

	private void createWidget(ModelEditorActionType3 action) {
		if (action == null) {
			widget = new MoverWidget();
		} else {
			switch (action) {
				case TRANSLATION, EXTRUDE, EXTEND -> widget = new MoverWidget();
				case ROTATION, SQUAT -> widget = new RotatorWidget();
				case SCALING -> widget = new ScalerWidget();
			}
			;
		}
	}

	protected void setWidgetPoint(AbstractSelectionManager selectionManager) {
//		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		widget.setPoint(selectionManager.getUVCenter(0));
	}
}
