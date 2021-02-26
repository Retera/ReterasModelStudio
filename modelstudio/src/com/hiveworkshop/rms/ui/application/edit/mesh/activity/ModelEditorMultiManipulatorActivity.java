package com.hiveworkshop.rms.ui.application.edit.mesh.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.model.ModelEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public class ModelEditorMultiManipulatorActivity extends MultiManipulatorActivity<ModelEditorManipulatorBuilder>
		implements ModelEditorViewportActivity {

	public ModelEditorMultiManipulatorActivity(final ModelEditorManipulatorBuilder manipulatorBuilder,
	                                           final UndoActionListener undoActionListener,
	                                           final SelectionView selectionView) {
		super(manipulatorBuilder, undoActionListener, selectionView);
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		manipulatorBuilder.modelEditorChanged(newModelEditor);
	}

}
