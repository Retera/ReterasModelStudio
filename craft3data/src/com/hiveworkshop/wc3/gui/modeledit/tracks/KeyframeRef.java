package com.hiveworkshop.wc3.gui.modeledit.tracks;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.TimelineContainer;

/** One keyframe: the track, the component that owns it, and the key's index. */
public final class KeyframeRef {
	public final TimelineContainer container;
	public final AnimFlag track;
	public final int index;

	public KeyframeRef(final TimelineContainer container, final AnimFlag track, final int index) {
		this.container = container;
		this.track = track;
		this.index = index;
	}

	public int time() {
		return track.getTimes().get(index);
	}

	public Object value() {
		return track.getValues().get(index);
	}

	public Object inTan() {
		return track.tans() && (index < track.getInTans().size()) ? track.getInTans().get(index) : null;
	}

	public Object outTan() {
		return track.tans() && (index < track.getOutTans().size()) ? track.getOutTans().get(index) : null;
	}

	/** True while the index still addresses a key of the track. */
	public boolean isValid() {
		return (index >= 0) && (index < track.getTimes().size());
	}
}
