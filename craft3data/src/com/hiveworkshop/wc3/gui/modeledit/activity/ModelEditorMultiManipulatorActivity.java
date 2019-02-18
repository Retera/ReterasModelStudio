package com.hiveworkshop.wc3.gui.modeledit.activity;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.model.ModelEditorManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;

public class ModelEditorMultiManipulatorActivity extends MultiManipulatorActivity<ModelEditorManipulatorBuilder>
		implements ModelEditorViewportActivity {

	public ModelEditorMultiManipulatorActivity(final ModelEditorManipulatorBuilder manipulatorBuilder,
			final UndoActionListener undoActionListener, final SelectionView selectionView) {
		super(manipulatorBuilder, undoActionListener, selectionView);
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		manipulatorBuilder.modelEditorChanged(newModelEditor);
	}

}
