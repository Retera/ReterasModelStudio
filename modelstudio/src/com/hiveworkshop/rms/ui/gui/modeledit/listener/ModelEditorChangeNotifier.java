package com.hiveworkshop.rms.ui.gui.modeledit.listener;

import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;

import java.util.HashSet;
import java.util.Set;

public class ModelEditorChangeNotifier {

	Set<ViewportActivityManager> listenerSet = new HashSet<>();

	public void subscribe(final ViewportActivityManager listener) {
		listenerSet.add(listener);
	}

	public void unsubscribe(final ViewportActivityManager listener) {
		listenerSet.remove(listener);
	}

	public void modelEditorChanged(final ModelEditor newModelEditor) {
		for (ViewportActivityManager listener : listenerSet) {
			listener.modelEditorChanged(newModelEditor);
		}
	}

}
