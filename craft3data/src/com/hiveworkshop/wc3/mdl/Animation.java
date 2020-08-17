package com.hiveworkshop.wc3.mdl;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxSequence;

import com.hiveworkshop.wc3.gui.animedit.BasicTimeBoundProvider;

/**
 * A java object to represent MDL "Sequences" ("Animations").
 *
 * Eric Theller 11/5/2011
 */
public class Animation implements BasicTimeBoundProvider {
	private String name = "";
	private int intervalStart = 0;
	private int intervalEnd = -1;
	private ArrayList<String> tags = new ArrayList<String>();// These are strings tags, i.e.
	// "MoveSpeed X," "Rarity X,"
	// "NonLooping," etc.
	private ExtLog extents;

	private Animation() {

	}

	public boolean equalsAnim(final Animation other) {
		return other.name.equals(this.name) && (other.intervalStart == intervalStart)
				&& (other.intervalEnd == intervalEnd) && other.tags.equals(tags);
	}

	public Animation(final String name, final int intervalStart, final int intervalEnd) {
		this.name = name;
		this.intervalStart = intervalStart;
		this.intervalEnd = intervalEnd;
		extents = new ExtLog(ExtLog.DEFAULT_MINEXT, ExtLog.DEFAULT_MAXEXT, ExtLog.DEFAULT_BOUNDSRADIUS);
	}

	public Animation(final String name, final int intervalStart, final int intervalEnd, final Vertex minimumExt,
			final Vertex maximumExt, final double boundsRad) {
		this.name = name;
		this.intervalStart = intervalStart;
		this.intervalEnd = intervalEnd;
		extents = new ExtLog(minimumExt, maximumExt, boundsRad);
	}

	// construct for simple animation object, within geoset
	public Animation(final ExtLog extents) {
		name = "";
		this.extents = extents;
	}

	public Animation(final MdlxSequence sequence) {
		long[] interval = sequence.interval;

		name = sequence.name;
		intervalStart = (int)interval[0];
		intervalEnd = (int)interval[1];
		extents = new ExtLog(sequence.extent);

		float moveSpeed = sequence.moveSpeed;

		if (moveSpeed != 0) {
			addTag("MoveSpeed " + moveSpeed);
		}

		if (sequence.flags == 1) {
			addTag("NonLooping");
		}

		float rarity = sequence.rarity;

		if (rarity > 0) {
			addTag("Rarity " + rarity);
		}
	}

	public MdlxSequence toMdlx() {
		MdlxSequence sequence = new MdlxSequence();

		sequence.name = name;
		sequence.interval[0] = intervalStart;
		sequence.interval[1] = intervalEnd;
		sequence.extent = extents.toMdlx();

		for (final String tag : getTags()) {
			if (tag.startsWith("MoveSpeed")) {
				sequence.moveSpeed = Float.parseFloat(tag.split(" ")[1]);
			} else if (tag.startsWith("NonLooping")) {
				sequence.flags = 1;
			} else if (tag.startsWith("Rarity")) {
				sequence.rarity = Float.parseFloat(tag.split(" ")[1]);
			}
		}

		return sequence;
	}

	public float getRarity() {
		for (final String tag : tags) {
			if (tag.startsWith("Rarity")) {
				return Float.parseFloat(tag.split(" ")[1]);
			}
		}
		return 0.0f;
	}

	public void setRarity(final float newRarity) {
		boolean foundTag = false;
		for (int i = 0; (i < tags.size()) && !foundTag; i++) {
			final String tag = tags.get(i);
			if (tag.startsWith("Rarity")) {
				tags.set(i, "Rarity " + newRarity);
				foundTag = true;
			}
		}
		if (!foundTag) {
			tags.add("Rarity " + newRarity);
		}
	}

	public void setMoveSpeed(final float newMoveSpeed) {
		boolean foundTag = false;
		for (int i = 0; (i < tags.size()) && !foundTag; i++) {
			final String tag = tags.get(i);
			if (tag.startsWith("MoveSpeed")) {
				tags.set(i, "MoveSpeed " + newMoveSpeed);
				foundTag = true;
			}
		}
		if (!foundTag) {
			tags.add("MoveSpeed " + newMoveSpeed);
		}
	}

	public float getMoveSpeed() {
		for (final String tag : tags) {
			if (tag.startsWith("MoveSpeed")) {
				return Float.parseFloat(tag.split(" ")[1]);
			}
		}
		return 0.0f;
	}

	public Animation(final Animation other) {
		this.name = other.name;
		intervalStart = other.intervalStart;
		intervalEnd = other.intervalEnd;
		tags = new ArrayList<String>(other.tags);
		extents = new ExtLog(other.extents);
	}

	public void addTag(final String tag) {
		tags.add(tag);
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(final ArrayList<String> tags) {
		this.tags = tags;
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(final ExtLog extents) {
		this.extents = extents;
	}

	public void setName(final String text) {
		this.name = text;
	}

	public String getName() {
		return this.name;
	}

	public boolean isNonLooping() {
		return tags.contains("NonLooping");
	}

	public void setNonLooping(final boolean nonLooping) {
		if (isNonLooping()) {
			if (!nonLooping) {
				tags.remove("NonLooping");
			}
		} else {
			if (nonLooping) {
				tags.add("NonLooping");
			}
		}
	}

	public int length() {
		return intervalEnd - intervalStart;
	}

	public void setInterval(final int start, final int end) {
		intervalStart = start;
		intervalEnd = end;
	}

	public void setIntervalStart(final int intervalStart) {
		this.intervalStart = intervalStart;
	}

	public void setIntervalEnd(final int intervalEnd) {
		this.intervalEnd = intervalEnd;
	}

	public void copyToInterval(final int start, final int end, final List<AnimFlag> flags,
			final List<EventObject> eventObjs, final List<AnimFlag> newFlags, final List<EventObject> newEventObjs) {
		for (final AnimFlag af : newFlags) {
			if (!af.hasGlobalSeq) {
				af.copyFrom(flags.get(newFlags.indexOf(af)), intervalStart, intervalEnd, start, end);
			}
		}
		for (final EventObject e : newEventObjs) {
			if (!e.hasGlobalSeq) {
				e.copyFrom(eventObjs.get(newEventObjs.indexOf(e)), intervalStart, intervalEnd, start, end);
			}
		}
	}

	public void copyToInterval(final int start, final int end, final List<AnimFlag> flags,
			final List<EventObject> eventObjs) {
		for (final AnimFlag af : flags) {
			if (!af.hasGlobalSeq) {
				af.copyFrom(new AnimFlag(af), intervalStart, intervalEnd, start, end);
			}
		}
		for (final EventObject e : eventObjs) {
			if (!e.hasGlobalSeq) {
				e.copyFrom(e.copy(), intervalStart, intervalEnd, start, end);
			}
		}
	}

	public void setInterval(final int start, final int end, final List<AnimFlag> flags,
			final List<EventObject> eventObjs) {
		for (final AnimFlag af : flags) {
			if (!af.hasGlobalSeq) {
				af.timeScale(intervalStart, intervalEnd, start, end);
			}
		}
		for (final EventObject e : eventObjs) {
			if (!e.hasGlobalSeq) {
				e.timeScale(intervalStart, intervalEnd, start, end);
			}
		}
		intervalStart = start;
		intervalEnd = end;
	}

	public void reverse(final List<AnimFlag> flags, final List<EventObject> eventObjs) {
		for (final AnimFlag af : flags) {
			if (!af.hasGlobalSeq && ((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
				af.timeScale(intervalStart, intervalEnd, intervalEnd, intervalStart);
			}
		}
		for (final EventObject e : eventObjs) {
			e.timeScale(intervalStart, intervalEnd, intervalEnd, intervalStart);
		}
		// for( AnimFlag af: flags )
		// {
		// if( !af.hasGlobalSeq && (af.getTypeId() == 1 || af.getTypeId() == 2
		// || af.getTypeId() == 3 ) ) // wouldn't want to mess THAT up...
		// af.timeScale(m_intervalStart, m_intervalEnd, m_intervalStart+30,
		// m_intervalStart+2);
		// }
		// for( EventObject e: eventObjs )
		// {
		// e.timeScale(m_intervalStart, m_intervalEnd, m_intervalStart+30,
		// m_intervalStart+2);
		// }
	}

	public void clearData(final List<AnimFlag> flags, final List<EventObject> eventObjs) {
		for (final AnimFlag af : flags) {
			if (((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
				// !af.hasGlobalSeq && was above before
				af.deleteAnim(this);// timeScale(m_intervalStart, m_intervalEnd,
									// m_intervalEnd, m_intervalStart);
			}
		}
		for (final EventObject e : eventObjs) {
			e.deleteAnim(this);
		}
	}

	public void setInterval(final int start, final int end, final EditableModel mdlr) {
		final List<AnimFlag> aniFlags = mdlr.getAllAnimFlags();
		final ArrayList eventObjs = mdlr.sortedIdObjects(EventObject.class);
		setInterval(start, end, aniFlags, eventObjs);
	}

	@Override
	public int getStart() {
		return intervalStart;
	}

	@Override
	public int getEnd() {
		return intervalEnd;
	}

	@Override
	public String toString() {
		return getName();
	}

	public int getIntervalStart() {
		return intervalStart;
	}

	public int getIntervalEnd() {
		return intervalEnd;
	}
}
