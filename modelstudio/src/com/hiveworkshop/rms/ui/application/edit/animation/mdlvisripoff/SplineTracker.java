package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.TreeMap;

public class SplineTracker<T> {
	private Entry<T> entryEnd;
	private Entry<T> entryStart;
	private TTan<T> der;
	private AnimFlag<T> timeline;
	private int time;

	public SplineTracker(AnimFlag<T> timeline) {
		this.timeline = timeline;
		der = TTan.getNewTTan(timeline);
		if (der != null) {
			entryEnd = der.cur.deepCopy();
			entryStart = der.cur.deepCopy();
		}
	}

	public SplineTracker(TTan<T> der) {
		this.der = der;
		if (der != null) {
			timeline = der.getTimeline();
			entryEnd = der.cur.deepCopy();
			entryStart = der.cur.deepCopy();
		}
	}

	public void setTime(int time) {
		this.time = time;
		der.setFromKF(time);
	}


	public boolean hasDer() {
		return der != null;
	}

	public void prepareTTan() {
		setEntryValueX(der.prev, 0);
		setEntryValueX(der.cur, 100);
		setEntryValueX(der.next, 0);
		der.calcDerivative();
	}

	public void calcSplineStep(Entry<T> e1, int time, Entry<T> e2) {
		e1.setTime(time);
		setEntryXsTo0(e1);
		setEntryToTang(e2);
	}

	public void calcSplineStepEnd(int time) {
		entryEnd.setTime(time);
		setEntryXsTo0(entryEnd);
		setEntryToTang(entryStart);
	}

	public void calcSplineStepStart(int time) {
		entryStart.setTime(time);
		setEntryXsTo0(entryStart);
		setEntryToTang(entryEnd);
	}

	public void setEntryToTang(Entry<T> entry) {
		entry.time = 100;
		entry.set(der.tang);
		setEntryValueX(entry, 100);
	}

	public void setEntryXsTo0(Entry<T> entry) {
		if ((entry.value instanceof Float)) {
			Float v = 0f;
			entry.value = (T) v;
			entry.inTan = (T) v;
			entry.outTan = (T) v;
		} else if ((entry.value instanceof Integer)) {
			Integer v = 0;
			entry.value = (T) v;
			entry.inTan = (T) v;
			entry.outTan = (T) v;
		} else if (entry.value instanceof Vec3) {
			((Vec3) entry.value).setCoord((byte) 0, 0);
			((Vec3) entry.inTan).setCoord((byte) 0, 0);
			((Vec3) entry.outTan).setCoord((byte) 0, 0);
		} else if (entry.value instanceof Quat) {
			((Quat) entry.value).setCoord((byte) 0, 0);
			((Quat) entry.inTan).setCoord((byte) 0, 0);
			((Quat) entry.outTan).setCoord((byte) 0, 0);
		}
	}

	public void setEntryValueX(Entry<T> entry, int value) {
		if ((entry.value instanceof Float)) {
			Float v = (float) value;
			entry.value = (T) v;
		} else if ((entry.value instanceof Integer)) {
			Integer v = value;
			entry.value = (T) v;
		} else if (entry.value instanceof Vec3) {
			((Vec3) entry.value).setCoord((byte) 0, value);
		} else if (entry.value instanceof Quat) {
			((Quat) entry.value).setCoord((byte) 0, value);
		}
	}

	public void interpolate(int time) {
		switch (timeline.getInterpolationType()) {
			case HERMITE -> der.spline(time, entryStart, entryEnd);
			case BEZIER -> der.bezInterp(time, entryStart, entryEnd);
		}
	}


	public T setObjToNewValue(T value, float newValue) {
		if ((value instanceof Float)) {
			Float v = newValue;
			value = (T) v;
			return (T) v;
		} else if ((value instanceof Integer)) {
			Integer v = (int) newValue;
			value = (T) v;
			return (T) v;
		} else if (value instanceof Vec3) {
			((Vec3) value).setCoord((byte) 0, newValue);
			return value;
		} else if (value instanceof Quat) {
			((Quat) value).setCoord((byte) 0, newValue);
			return value;
		}
		throw new IllegalArgumentException("Unknown subscripting (set): " + value + ", " + 0 + ", " + newValue);
	}

	public int getEndValue(float pixPerUnitY) {
		return Math.round(pixPerUnitY * getEndX());
	}

	public float getEndX() {
		if ((entryEnd.value instanceof Float || entryEnd.value instanceof Integer)) {
			return (float) entryEnd.value;
		} else if (entryEnd.value instanceof Vec3) {
			return ((Vec3) entryEnd.value).getCoord((byte) 0);
		} else if (entryEnd.value instanceof Quat) {
			return ((Quat) entryEnd.value).getCoord((byte) 0);
		}
		throw new IllegalArgumentException("Unknown subscripting (get): " + entryEnd.value + ", " + 0);
	}


	public float getTension() {
		return der.tension;
	}

	public float getContinuity() {
		return der.continuity;
	}

	public float getBias() {
		return der.bias;
	}

	public void setTCB(float tension, float continuity, float bias) {
		der.tension = tension;
		der.continuity = continuity;
		der.bias = bias;

		der.isLogsReady = false;
		der.calcDerivative();

		throw new UnsupportedOperationException("Not finished here, need to have shared access to storing keyframe data and UndoManager");
	}


	public void initFromKF() {
		if (timeline.tans()) {
			TreeMap<Integer, Entry<T>> entryMap = timeline.getEntryMap();

			if (entryMap.ceilingKey(time) != null
					&& (timeline.getInterpolationType() == InterpolationType.HERMITE)) {
				der.calcSplineParameters();
			}

		}
	}
}
