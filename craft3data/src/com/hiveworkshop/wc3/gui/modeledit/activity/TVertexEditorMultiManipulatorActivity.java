package com.hiveworkshop.wc3.gui.modeledit.activity;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.builder.uv.TVertexEditorManipulatorBuilder;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.TVertexEditor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionView;

public class TVertexEditorMultiManipulatorActivity extends MultiManipulatorActivity<TVertexEditorManipulatorBuilder>
		implements TVertexEditorViewportActivity {

	public TVertexEditorMultiManipulatorActivity(final TVertexEditorManipulatorBuilder manipulatorBuilder,
			final UndoActionListener undoActionListener, final SelectionView selectionView) {
		super(manipulatorBuilder, undoActionListener, selectionView);
	}

	@Override
	public void editorChanged(final TVertexEditor newModelEditor) {
		manipulatorBuilder.editorChanged(newModelEditor);
	}

}
