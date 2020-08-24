package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public class ModelEditorChangeNotifier extends SubscriberSetNotifier<ModelEditorChangeListener>
		implements ModelEditorChangeListener {

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		for (final ModelEditorChangeListener listener : set) {
			listener.modelEditorChanged(newModelEditor);
		}
	}

}
