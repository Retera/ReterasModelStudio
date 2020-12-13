package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.visitor.IdObjectVisitor;
import com.hiveworkshop.rms.parsers.mdlx.MdlxEventObject;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for EventObjects, which include such things as craters, footprints,
 * splashes, blood spurts, and sounds
 * <p>
 * Eric Theller 3/10/2012 3:52 PM
 */
public class EventObject extends IdObject {
	List<Integer> eventTrack = new ArrayList<>();
	Integer globalSeq;
	int globalSeqId = -1;
	boolean hasGlobalSeq = false;

	public EventObject() {

	}

	public EventObject(final String name) {
		this.name = name;
	}

	public EventObject(final EventObject object) {
		copyObject(object);

		eventTrack = new ArrayList<>(object.eventTrack);
		globalSeq = object.globalSeq;
		globalSeqId = object.globalSeqId;
		hasGlobalSeq = object.hasGlobalSeq;
	}

	public EventObject(final MdlxEventObject object) {
		if ((object.flags & 1024) != 1024) {
			System.err.println("MDX -> MDL error: An eventobject '" + object.name
					+ "' not flagged as eventobject in MDX!");
		}

		loadObject((object));

		final int globalSequenceId = object.globalSequenceId;

		if (globalSequenceId >= 0) {
			globalSeqId = globalSequenceId;
			hasGlobalSeq = true;
		}

		for (final long val : object.keyFrames) {
			eventTrack.add((int) val);
		}
	}

	public MdlxEventObject toMdlx() {
		final MdlxEventObject object = new MdlxEventObject();

		objectToMdlx(object);

		if (isHasGlobalSeq()) {
			object.globalSequenceId = getGlobalSeqId();
		}

		final List<Integer> keyframes = getEventTrack();

		object.keyFrames = new long[keyframes.size()];

		for (int i = 0, l = keyframes.size(); i < l; i++) {
			object.keyFrames[i] = keyframes.get(i).longValue();
		}

		return object;
	}

	@Override
	public EventObject copy() {
		return new EventObject(this);
	}

	public int size() {
		return eventTrack.size();
	}

	public static EventObject buildEmptyFrom(final EventObject source) {
		return new EventObject(source);

	}

	public void setValuesTo(final EventObject source) {
		eventTrack = source.eventTrack;
	}

	public void deleteAnim(final Animation anim) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (int index = eventTrack.size() - 1; index >= 0; index--) {
			final int i = eventTrack.get(index);
			if ((i >= anim.getStart()) && (i <= anim.getEnd())) {
				// If this "i" is a part of the anim being removed
				eventTrack.remove(index);
			}
		}

		// BOOM magic happens
	}

	public void timeScale(final int start, final int end, final int newStart, final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (final Integer inte : eventTrack) {
			final int i = inte;
			if ((i >= start) && (i <= end)) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.set(eventTrack.indexOf(inte), (int) (newStart + (ratio * (newEnd - newStart))));
			}
		}

		sort();

		// BOOM magic happens
	}

	public void copyFrom(final EventObject source, final int start, final int end, final int newStart,
						 final int newEnd) {
		// Timescales a part of the AnimFlag from section "start" to "end" into
		// the new time "newStart" to "newEnd"
		for (final Integer inte : source.eventTrack) {
			final int i = inte;
			if ((i >= start) && (i <= end)) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.add((int) (newStart + (ratio * (newEnd - newStart))));
			}
		}

		sort();

		// BOOM magic happens
	}

	public void sort() {
		final int low = 0;
		final int high = eventTrack.size() - 1;

		if (eventTrack.size() > 0) {
			quicksort(low, high);
		}
	}

	private void quicksort(final int low, final int high) {
		// Thanks to Lars Vogel for the quicksort concept code (something to
		// look at), found on google
		// (re-written by Eric "Retera" for use in AnimFlags)
		int i = low, j = high;
		final Integer pivot = eventTrack.get(low + ((high - low) / 2));

		while (i <= j) {
			while (eventTrack.get(i) < pivot) {
				i++;
			}
			while (eventTrack.get(j) > pivot) {
				j--;
			}
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}

		if (low < j) {
			quicksort(low, j);
		}
		if (i < high) {
			quicksort(i, high);
		}
	}

	private void exchange(final int i, final int j) {
		final Integer iTime = eventTrack.get(i);

		eventTrack.set(i, eventTrack.get(j));

		eventTrack.set(j, iTime);
	}

	public void updateGlobalSeqRef(final EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeq = mdlr.getGlobalSeq(globalSeqId);
		}
	}

	public void updateGlobalSeqId(final EditableModel mdlr) {
		if (hasGlobalSeq) {
			globalSeqId = mdlr.getGlobalSeqId(globalSeq);
		}
	}

	/**
	 * @deprecated Use getGlobalSeq
	 */
	@Deprecated
	public int getGlobalSeqId() {
		return globalSeqId;
	}

	/**
	 * @deprecated Use setGlobalSeq
	 */
	@Deprecated
	public void setGlobalSeqId(final int globalSeqId) {
		this.globalSeqId = globalSeqId;
	}

	public boolean isHasGlobalSeq() {
		return hasGlobalSeq;
	}

	public void setHasGlobalSeq(final boolean hasGlobalSeq) {
		this.hasGlobalSeq = hasGlobalSeq;
	}

	public Integer getGlobalSeq() {
		return globalSeq;
	}

	public void setGlobalSeq(final Integer globalSeq) {
		this.globalSeq = globalSeq;
	}

	public List<Integer> getEventTrack() {
		return eventTrack;
	}

	public void setEventTrack(final List<Integer> eventTrack) {
		this.eventTrack = eventTrack;
	}

	@Override
	public void apply(final IdObjectVisitor visitor) {
		visitor.eventObject(this);
	}

	@Override
	public double getClickRadius(final CoordinateSystem coordinateSystem) {
		return DEFAULT_CLICK_RADIUS / CoordinateSystem.Util.getZoom(coordinateSystem);
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return 1;
	}
}
