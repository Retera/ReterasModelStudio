package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public class TVertexEditorChangeNotifier extends SubscriberSetNotifier<TVertexEditorChangeListener>
		implements TVertexEditorChangeListener {

	@Override
	public void editorChanged(final TVertexEditor newModelEditor) {
		for (final TVertexEditorChangeListener listener : set) {
			listener.editorChanged(newModelEditor);
		}
	}

}
