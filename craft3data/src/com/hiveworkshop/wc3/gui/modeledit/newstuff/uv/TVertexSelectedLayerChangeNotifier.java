package com.hiveworkshop.wc3.gui.modeledit.newstuff.uv;

import com.etheller.util.SubscriberSetNotifier;

public class TVertexSelectedLayerChangeNotifier extends SubscriberSetNotifier<TVertexSelectedLayerChangeListener>
		implements TVertexSelectedLayerChangeListener {

	@Override
	public void indexChanged(final int uvLayerIndex) {
		for (final TVertexSelectedLayerChangeListener listener : set) {
			listener.indexChanged(uvLayerIndex);
		}
	}

}
