package com.hiveworkshop.rms.ui.application.edit.uv.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoManager;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public class TVertexEditorMultiManipulatorActivity extends MultiManipulatorActivity<TVertexEditorManipulatorBuilder> implements TVertexEditorViewportActivity {

	public TVertexEditorMultiManipulatorActivity(TVertexEditorManipulatorBuilder manipulatorBuilder,
	                                             UndoManager undoManager,
	                                             SelectionView selectionView) {
		super(manipulatorBuilder, undoManager, selectionView);
	}

	@Override
	public void editorChanged(TVertexEditor newModelEditor) {
		manipulatorBuilder.editorChanged(newModelEditor);
	}

}
