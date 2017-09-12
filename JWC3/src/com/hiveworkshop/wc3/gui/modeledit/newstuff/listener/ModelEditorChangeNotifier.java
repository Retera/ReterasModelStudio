package com.hiveworkshop.wc3.gui.modeledit.newstuff.listener;

import com.etheller.util.SubscriberSetNotifier;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditor;

public class ModelEditorChangeNotifier extends SubscriberSetNotifier<ModelEditorChangeListener>
		implements ModelEditorChangeListener {

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		for (final ModelEditorChangeListener listener : set) {
			listener.modelEditorChanged(newModelEditor);
		}
	}

}
