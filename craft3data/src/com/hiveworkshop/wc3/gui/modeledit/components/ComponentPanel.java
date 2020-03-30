package com.hiveworkshop.wc3.gui.modeledit.components;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.mdl.MDL;

public interface ComponentPanel {
	void save(MDL model, UndoActionListener undoListener, ModelStructureChangeListener changeListener);
}
