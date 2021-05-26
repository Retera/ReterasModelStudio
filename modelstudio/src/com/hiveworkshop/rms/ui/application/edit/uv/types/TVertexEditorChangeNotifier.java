package com.hiveworkshop.rms.ui.application.edit.uv.types;

import java.util.HashSet;
import java.util.Set;

public class TVertexEditorChangeNotifier implements TVertexEditorChangeListener {
	Set<TVertexEditorChangeListener> listenerSet = new HashSet<>();

	public void subscribe(final TVertexEditorChangeListener listener) {
		listenerSet.add(listener);
	}

	public void unsubscribe(final TVertexEditorChangeListener listener) {
		listenerSet.remove(listener);
	}

	@Override
	public void editorChanged(TVertexEditor newModelEditor) {
		for (TVertexEditorChangeListener listener : listenerSet) {
			listener.editorChanged(newModelEditor);
		}
	}

}
