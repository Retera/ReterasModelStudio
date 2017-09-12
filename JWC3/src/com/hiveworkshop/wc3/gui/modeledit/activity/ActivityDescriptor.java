package com.hiveworkshop.wc3.gui.modeledit.activity;

import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public interface ActivityDescriptor {
	ViewportActivity createActivity(ModelEditorManager modelEditorManager, ModelView modelView,
			UndoActionListener undoActionListener);
}
