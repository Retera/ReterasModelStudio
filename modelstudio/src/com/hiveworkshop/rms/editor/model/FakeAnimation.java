package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;

/**
 * A java object to represent MDL "Sequences" ("Animations").
 *
 * Eric Theller 11/5/2011
 */
public class FakeAnimation extends Animation {
	private Animation realAnim;
//	private String name = "";
//	private ExtLog extents;
//	private float moveSpeed = 0;
//	private boolean nonLooping = false;
//	private float rarity = 0;

	public FakeAnimation(String name, Animation animation) {
		super(name, animation.getStart(), animation.getEnd());
		this.realAnim = animation;
	}

	private FakeAnimation(FakeAnimation other) {
		this(other.getName(), other.realAnim);
		setMoveSpeed(other.getMoveSpeed());
		setNonLooping(other.isNonLooping());
		setRarity(other.getRarity());
		setExtents(other.getExtents().deepCopy());
	}

	public boolean equals(final FakeAnimation other) {
		return other != null && super.equals(other) && realAnim == other.realAnim;
	}

	public Animation getRealAnim() {
		return realAnim;
	}

	public FakeAnimation setRealAnim(Animation realAnim) {
		this.realAnim = realAnim;
		return this;
	}

	@Override
	public int hashCode() {
		return realAnim.hashCode();
	}

	@Override
	public int getStart() {
		return realAnim.getStart();
	}

	@Override
	public int getLength() {
		return realAnim.getLength();
	}

	@Override
	public int getEnd() {
		return realAnim.getEnd();
	}

	@Override
	public Sequence setStart(int start) {
		return this;
	}

	@Override
	public Sequence setLength(int length) {
		return this;
	}

	@Override
	public String toString() {
		return "[F] " + getName() + " (" + realAnim.getName() + ")";
	}

	public FakeAnimation deepCopy() {
		return new FakeAnimation(this);
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
			if (o instanceof FakeAnimation) {
				return System.identityHashCode(this) - System.identityHashCode(o);
			} else {
				return 1;
			}
		}

		return -1;
	}
}
