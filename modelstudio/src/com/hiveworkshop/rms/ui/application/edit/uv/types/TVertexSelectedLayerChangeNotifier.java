package com.hiveworkshop.rms.ui.application.edit.uv.types;

import com.hiveworkshop.rms.util.SubscriberSetNotifier;

public class TVertexSelectedLayerChangeNotifier extends SubscriberSetNotifier<TVertexSelectedLayerChangeListener>
		implements TVertexSelectedLayerChangeListener {

	@Override
	public void indexChanged(final int uvLayerIndex) {
		for (final TVertexSelectedLayerChangeListener listener : set) {
			listener.indexChanged(uvLayerIndex);
		}
	}

}
