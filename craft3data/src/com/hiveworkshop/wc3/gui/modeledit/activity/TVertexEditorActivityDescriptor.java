package com.hiveworkshop.wc3.gui.modeledit.activity;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.uv.viewport.TVertexEditorManager;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public interface TVertexEditorActivityDescriptor {
	TVertexEditorViewportActivity createActivity(TVertexEditorManager modelEditorManager, ModelView modelView,
			UndoActionListener undoActionListener);
}
