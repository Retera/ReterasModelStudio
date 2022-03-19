package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Vec3;

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
		extents = new ExtLog(ExtLog.DEFAULT_MINEXT, ExtLog.DEFAULT_MAXEXT, ExtLog.DEFAULT_BOUNDSRADIUS);
	}

	public Animation(String name, int start, int end, Vec3 minimumExt, Vec3 maximumExt, double boundsRad) {
		super(start, end - start);
		this.name = name;
		extents = new ExtLog(minimumExt, maximumExt, boundsRad);
	}

	// construct for simple animation object, within geoset
	public Animation(ExtLog extents) {
		super(0);
		name = "";
		this.extents = extents;
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

	public void setInterval(int start, int end) {
		this.start = start;
		this.length = end - start;
	}

	public void setAnimStuff(int start, int length) {
		this.start = start;
		this.length = length;
	}

//	public void copyToInterval(int newStart, int newEnd, Animation animation,
//	                           List<AnimFlag<?>> sourceFlags, List<EventObject> sourceEventObjs,
//	                           List<AnimFlag<?>> newFlags, List<EventObject> newEventObjs) {
//		for (final AnimFlag<?> af : newFlags) {
//			if (!af.hasGlobalSeq()) {
//				AnimFlag<?> source = sourceFlags.get(newFlags.indexOf(af));
//				af.copyFrom(source, this, start, end, animation, newStart, newEnd);
//			}
//		}
//		for (final EventObject e : newEventObjs) {
//			if (!e.hasGlobalSeq()) {
//				e.copyFrom(sourceEventObjs.get(newEventObjs.indexOf(e)), start, end, newStart, newEnd);
//			}
//		}
//	}

//	public void copyFromInterval(Animation animation, int offset, List<AnimFlag<?>> flags, List<EventObject> eventObjs) {
//		for (AnimFlag<?> af : flags) {
//			if (!af.hasGlobalSeq()) {
//				af.copyFrom(af, animation, 0, animation.length, this, offset, length + offset);
//			}
//		}
//		for (EventObject e : eventObjs) {
//			if (!e.hasGlobalSeq()) {
//				e.copyFrom(e.copy(), animation, this);
//			}
//		}
//	}

//	public void copyToInterval(Animation animation, List<EventObject> eventObjs) {
//		for (EventObject e : eventObjs) {
//			if (!e.hasGlobalSeq()) {
//				e.copyFrom(e.copy(), this.start, this.end, animation.getStart(), animation.getEnd());
//			}
//		}
//	}
//	public void copyFromAnimation(Animation animation, List<EventObject> eventObjs) {
//		for (EventObject e : eventObjs) {
//			if (!e.hasGlobalSeq()) {
//				e.copyFrom(e.copy(), animation, this);
//			}
//		}
//	}

//	public void reverse(List<AnimFlag<?>> flags, List<EventObject> eventObjs) {
//		for (AnimFlag<?> af : flags) {
//			if (!af.hasGlobalSeq() && ((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
//				af.timeScale(this, length, 0);
//			}
//		}
//		for (EventObject e : eventObjs) {
//			e.timeScale(start, end, end, start);
//		}
//		// for( AnimFlag af: flags )
//		// {
//		// if( !af.hasGlobalSeq && (af.getTypeId() == 1 || af.getTypeId() == 2
//		// || af.getTypeId() == 3 ) ) // wouldn't want to mess THAT up...
//		// af.timeScale(m_intervalStart, m_intervalEnd, m_intervalStart+30,
//		// m_intervalStart+2);
//		// }
//		// for( EventObject e: eventObjs )
//		// {
//		// e.timeScale(m_intervalStart, m_intervalEnd, m_intervalStart+30,
//		// m_intervalStart+2);
//		// }
//	}
//
//	public void clearData(List<AnimFlag<?>> flags, List<EventObject> eventObjs) {
//		for (AnimFlag<?> af : flags) {
//			if (((af.getTypeId() == 1) || (af.getTypeId() == 2) || (af.getTypeId() == 3))) {
//				// !af.hasGlobalSeq && was above before
//				af.deleteAnim(this);
//			}
//		}
//		for (EventObject e : eventObjs) {
//			e.deleteAnim(this);
//		}
//	}

//	public static void setInterval(Animation anim, int start, int end, EditableModel mdl) {
//		List<AnimFlag<?>> animFlags = mdl.getAllAnimFlags();
//		List<EventObject> eventObjs = mdl.getEvents();
//
//		for (final AnimFlag<?> af : animFlags) {
//			if (!af.hasGlobalSeq()) {
//				af.timeScale2(anim, end - start, 0);
//			}
//		}
//		for (final EventObject e : eventObjs) {
//			if (!e.hasGlobalSeq()) {
//				e.timeScale(anim.start, anim.end, start, end);
//			}
//		}
//		anim.start = start;
//		anim.end = end;
//	}

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
			int nameDiff = getName().compareTo(((Animation) o).getName());
			if (nameDiff != 0) {
				return nameDiff;
			}
			return System.identityHashCode(this) - System.identityHashCode(o);
		}

		return -1;
	}
}
