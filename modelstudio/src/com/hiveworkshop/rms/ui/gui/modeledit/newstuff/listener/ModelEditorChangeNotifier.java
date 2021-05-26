package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;

import java.util.HashSet;
import java.util.Set;

public class ModelEditorChangeNotifier implements ModelEditorChangeListener {

	Set<ModelEditorChangeListener> listenerSet = new HashSet<>();

	public void subscribe(final ModelEditorChangeListener listener) {
		listenerSet.add(listener);
	}

	public void unsubscribe(final ModelEditorChangeListener listener) {
		listenerSet.remove(listener);
	}

	@Override
	public void modelEditorChanged(final ModelEditor newModelEditor) {
		for (ModelEditorChangeListener listener : listenerSet) {
			listener.modelEditorChanged(newModelEditor);
		}
	}

}
