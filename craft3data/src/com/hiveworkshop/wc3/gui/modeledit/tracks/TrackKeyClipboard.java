package com.hiveworkshop.wc3.gui.modeledit.tracks;

import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.TimelineContainer;

/**
 * In-process clipboard for Tracks keyframes. Values are deep copies; times
 * are kept so a multi-key paste preserves the spacing relative to the first
 * copied key.
 */
public final class TrackKeyClipboard {
	public static final class Entry {
		public final TimelineContainer container;
		public final AnimFlag track;
		public final int time;
		public final Object value, inTan, outTan;

		Entry(final KeyframeRef ref) {
			container = ref.container;
			track = ref.track;
			time = ref.time();
			value = AnimFlag.cloneValue(ref.value());
			inTan = AnimFlag.cloneValue(ref.inTan());
			outTan = AnimFlag.cloneValue(ref.outTan());
		}
	}

	private static final List<Entry> entries = new ArrayList<>();
	private static int baseTime;

	private TrackKeyClipboard() {
	}

	public static void copy(final List<KeyframeRef> refs) {
		entries.clear();
		baseTime = Integer.MAX_VALUE;
		for (final KeyframeRef ref : refs) {
			if (ref.isValid()) {
				entries.add(new Entry(ref));
				baseTime = Math.min(baseTime, ref.time());
			}
		}
		if (entries.isEmpty()) {
			baseTime = 0;
		}
	}

	public static boolean isEmpty() {
		return entries.isEmpty();
	}

	public static List<Entry> getEntries() {
		return entries;
	}

	/** Time of the earliest copied key; paste places it at the clicked time. */
	public static int getBaseTime() {
		return baseTime;
	}
}
