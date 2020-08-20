package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxEventObject;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

/**
 * A class for EventObjects, which include such things as craters, footprints,
 * splashes, blood spurts, and sounds
 *
 * Eric Theller 3/10/2012 3:52 PM
 */
public class EventObject extends IdObject {
	List<Integer> eventTrack = new ArrayList<>();
	Integer globalSeq;
	int globalSeqId = -1;
	boolean hasGlobalSeq;

	private EventObject() {

	}

	protected EventObject(final EventObject source) {

	}

	public EventObject(final String name) {
		this.name = name;
	}

	public EventObject(final MdlxEventObject object) {
		if ((object.flags & 1024) != 1024) {
			System.err.println("MDX -> MDL error: An eventobject '" + object.name
					+ "' not flagged as eventobject in MDX!");
		}

		loadObject((object));

		int globalSequenceId = object.globalSequenceId;

		if (globalSequenceId >= 0) {
			globalSeqId = globalSequenceId;
			hasGlobalSeq = true;
		}

		for (final long val : object.keyFrames) {
			eventTrack.add(Integer.valueOf((int)val));
		}
	}

	public MdlxEventObject toMdlx() {
		MdlxEventObject object = new MdlxEventObject();

		objectToMdlx(object);

		if (isHasGlobalSeq()) {
			object.globalSequenceId = getGlobalSeqId();
		}

		List<Integer> keyframes = getEventTrack();

		object.keyFrames = new long[keyframes.size()];

		for (int i = 0, l = keyframes.size(); i < l; i++) {
			object.keyFrames[i] = keyframes.get(i).longValue();
		}

		return object;
	}

	@Override
	public EventObject copy() {
		final EventObject x = new EventObject();

		x.name = name;
		x.pivotPoint = new Vertex(pivotPoint);
		x.objectId = objectId;
		x.parentId = parentId;
		x.setParent(getParent());
		x.eventTrack = new ArrayList<>(eventTrack);
		x.addAll(getAnimFlags());
		
		return x;
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
			final int i = eventTrack.get(index).intValue();
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
			final int i = inte.intValue();
			if ((i >= start) && (i <= end)) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.set(eventTrack.indexOf(inte), Integer.valueOf((int) (newStart + (ratio * (newEnd - newStart)))));
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
			final int i = inte.intValue();
			if ((i >= start) && (i <= end)) {
				// If this "i" is a part of the anim being rescaled
				final double ratio = (double) (i - start) / (double) (end - start);
				eventTrack.add(Integer.valueOf((int) (newStart + (ratio * (newEnd - newStart)))));
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
			while (eventTrack.get(i).intValue() < pivot.intValue()) {
				i++;
			}
			while (eventTrack.get(j).intValue() > pivot.intValue()) {
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

	@Override
	public void add(final String flag) {
		System.err.println("ERROR: EventObject given unknown flag: " + flag);
	}

	/**
	 * @return
	 * @deprecated Use getGlobalSeq
	 */
	@Deprecated
	public int getGlobalSeqId() {
		return globalSeqId;
	}

	/**
	 * @param globalSeqId
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
