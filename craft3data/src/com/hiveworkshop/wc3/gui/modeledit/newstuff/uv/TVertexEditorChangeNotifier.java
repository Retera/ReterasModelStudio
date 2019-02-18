package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv;

import com.etheller.util.SubscriberSetNotifier;

public class TVertexEditorChangeNotifier extends SubscriberSetNotifier<TVertexEditorChangeListener>
		implements TVertexEditorChangeListener {

	@Override
	public void editorChanged(final TVertexEditor newModelEditor) {
		for (final TVertexEditorChangeListener listener : set) {
			listener.editorChanged(newModelEditor);
		}
	}

}
