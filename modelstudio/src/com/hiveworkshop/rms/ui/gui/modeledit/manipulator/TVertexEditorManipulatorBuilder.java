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
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorWidgetType;

public class TVertexEditorManipulatorBuilder extends ManipulatorBuilder {
	ModelEditorWidgetType currentAction;


	public TVertexEditorManipulatorBuilder(TVertexEditorManager modelEditorManager,
	                                       ModelHandler modelHandler,
	                                       ModelEditorWidgetType currentAction) {
		super(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), modelHandler);
		this.currentAction = currentAction;
		createWidget(currentAction);
	}

	protected Manipulator getManipulator(AbstractSelectionManager selectionManager, MoveDimension directionByMouse) {
		return switch (currentAction) {
			case TRANSLATION -> new MoveTVertexManipulator(modelEditor, directionByMouse);
			case ROTATION -> new RotateTVertexManipulator(modelEditor, selectionManager, directionByMouse);
			case SCALING -> new ScaleTVertexManipulator(modelEditor, selectionManager, directionByMouse);

		};
	}

	private void createWidget(ModelEditorWidgetType action) {
		if (action == null) {
			widget = new MoverWidget();
		} else {
			switch (action) {
				case TRANSLATION -> widget = new MoverWidget();
				case ROTATION -> widget = new RotatorWidget();
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
