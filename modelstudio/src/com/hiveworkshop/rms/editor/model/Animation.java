package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

/**
 * A java object to represent MDL "Sequences" ("Animations").
 *
 * Eric Theller 11/5/2011
 */
public class Animation extends Sequence {
	private String name = "";
	private ExtLog extents;
	private float moveSpeed = 0;
	private boolean nonLooping = false;
	private float rarity = 0;

	public Animation(String name, int start, int end) {
		super(start, end - start);
		this.name = name;
		extents = new ExtLog(ExtLog.DEFAULT_BOUNDSRADIUS);
	}

	private Animation(Animation other) {
		super(other.start, other.length);
		name = other.name;
		moveSpeed = other.moveSpeed;
		nonLooping = other.nonLooping;
		rarity = other.rarity;
		extents = other.extents.deepCopy();
	}

	public boolean equals(final Animation other) {
		return other.name.equals(name) && (other.start == start)
				&& (other.length == length) && (other.moveSpeed == moveSpeed)
				&& (other.nonLooping == nonLooping) && (other.rarity == rarity);
	}

	public float getRarity() {
		return rarity;
	}

	public void setRarity(final float rarity) {
		this.rarity = rarity;
	}

	public void setMoveSpeed(final float moveSpeed) {
		this.moveSpeed = moveSpeed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}

	public ExtLog getExtents() {
		return extents;
	}

	public void setExtents(final ExtLog extents) {
		this.extents = extents;
	}

	public void setName(final String text) {
		name = text;
	}

	public String getName() {
		return name;
	}

	public boolean isNonLooping() {
		return nonLooping;
	}

	public void setNonLooping(final boolean nonLooping) {
		this.nonLooping = nonLooping;
	}

	@Override
	public String toString() {
		return getName();
	}

	public Animation deepCopy() {
		return new Animation(this);
	}

	@Override
	public int compareTo(Sequence o) {
		if (o instanceof GlobalSeq) {
			return 1;
		} else if (o instanceof Animation) {
			int startDiff = getStart() - o.getStart();
			if (startDiff != 0) {
				return startDiff;
			}
			int nameDiff = getName().compareTo(o.getName());
			if (nameDiff != 0) {
				return nameDiff;
			}
			return System.identityHashCode(this) - System.identityHashCode(o);
		}

		return -1;
	}
}
