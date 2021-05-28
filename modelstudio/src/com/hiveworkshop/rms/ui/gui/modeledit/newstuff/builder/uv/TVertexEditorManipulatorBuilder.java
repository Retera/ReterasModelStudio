package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv;

import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.MoverWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.RotatorWidget;
import com.hiveworkshop.rms.ui.application.edit.mesh.widgets.ScalerWidget;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.ManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.Manipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.MoveDimension;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.MoveTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.RotateTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.manipulator.uv.ScaleTVertexManipulator;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.AbstractSelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.toolbar.ModelEditorActionType2;

public class TVertexEditorManipulatorBuilder extends ManipulatorBuilder {
	ModelEditorActionType2 currentAction;


	public TVertexEditorManipulatorBuilder(TVertexEditorManager modelEditorManager,
	                                       ModelHandler modelHandler,
	                                       ModelEditorActionType2 currentAction) {
		super(modelEditorManager.getModelEditor(), modelEditorManager.getViewportSelectionHandler(), modelHandler);
		this.currentAction = currentAction;
		createWidget(currentAction);
	}

	protected Manipulator getBuilder(AbstractSelectionManager selectionManager, MoveDimension directionByMouse) {
		return switch (currentAction) {
			case TRANSLATION -> new MoveTVertexManipulator(getModelEditor(), directionByMouse);
			case ROTATION -> new RotateTVertexManipulator(getModelEditor(), selectionManager, directionByMouse);
			case SCALING -> new ScaleTVertexManipulator(getModelEditor(), selectionManager, directionByMouse);

		};
	}
	private void createWidget(ModelEditorActionType2 action) {
		if(action == null){
			widget = new MoverWidget();
		} else {
			switch (action) {
				case TRANSLATION -> widget = new MoverWidget();
				case ROTATION -> widget = new RotatorWidget();
				case SCALING -> widget = new ScalerWidget();
			};
		}
	}

	protected void setWidgetPoint(AbstractSelectionManager selectionManager) {
//		widget.setPoint(selectionView.getUVCenter(getModelEditor().getUVLayerIndex()));
		widget.setPoint(selectionManager.getUVCenter(0));
	}
}
