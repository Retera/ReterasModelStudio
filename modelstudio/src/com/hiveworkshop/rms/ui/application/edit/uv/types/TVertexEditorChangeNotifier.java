package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public class TVertexEditorChangeNotifier extends SubscriberSetNotifier<TVertexEditorChangeListener>
		implements TVertexEditorChangeListener {

	@Override
	public void editorChanged(TVertexEditor newModelEditor) {
		for (TVertexEditorChangeListener listener : listenerSet) {
			listener.editorChanged(newModelEditor);
		}
	}

}
