package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.ModelEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public class ModelEditorMultiManipulatorActivity extends MultiManipulatorActivity<ModelEditorManipulatorBuilder>
		implements ModelEditorViewportActivity {

	public ModelEditorMultiManipulatorActivity(ModelEditorManipulatorBuilder manipulatorBuilder,
	                                           UndoActionListener undoActionListener,
	                                           SelectionView selectionView) {
		super(manipulatorBuilder, undoActionListener, selectionView);
	}

	@Override
	public void modelEditorChanged(ModelEditor newModelEditor) {
		manipulatorBuilder.modelEditorChanged(newModelEditor);
	}

}