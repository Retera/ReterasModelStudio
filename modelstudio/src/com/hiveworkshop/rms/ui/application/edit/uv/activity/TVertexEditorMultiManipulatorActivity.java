package com.hiveworkshop.rms.ui.application.edit.uv.activity;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.MultiManipulatorActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.builder.uv.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionView;

public class TVertexEditorMultiManipulatorActivity extends MultiManipulatorActivity<TVertexEditorManipulatorBuilder> implements TVertexEditorViewportActivity {

	public TVertexEditorMultiManipulatorActivity(final TVertexEditorManipulatorBuilder manipulatorBuilder,
	                                             final UndoActionListener undoActionListener,
	                                             final SelectionView selectionView) {
		super(manipulatorBuilder, undoActionListener, selectionView);
	}

	@Override
	public void editorChanged(final TVertexEditor newModelEditor) {
		manipulatorBuilder.editorChanged(newModelEditor);
	}

}
