package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.TreeMap;

public class SplineTracker<T> {
	private Entry<T> entryInStart;
	private Entry<T> entryInEnd;
	private Entry<T> entryOutStart;
	private Entry<T> entryOutEnd;
	private TTan<T> tTanDer;
	private AnimFlag<T> timeline;
	private int time;
	private Sequence anim;
	int interStartTime;
	int interEndTime;


	public SplineTracker(AnimFlag<T> timeline, Sequence anim) {
		this.timeline = timeline;
		tTanDer = TTan.getNewTTan(timeline, anim);
		this.anim = anim;
		if (tTanDer != null) {
			entryInEnd = tTanDer.cur.deepCopy();
			entryInStart = tTanDer.cur.deepCopy();
			entryOutEnd = tTanDer.cur.deepCopy();
			entryOutStart = tTanDer.cur.deepCopy();
		}
	}

	public SplineTracker(TTan<T> tTanDer) {
		this.tTanDer = tTanDer;
		if (tTanDer != null) {
			timeline = tTanDer.getTimeline();
			entryInEnd = tTanDer.cur.deepCopy();
			entryInStart = tTanDer.cur.deepCopy();
			entryOutEnd = tTanDer.cur.deepCopy();
			entryOutStart = tTanDer.cur.deepCopy();
		}
	}

	public void setTime(int time, Sequence anim) {
		this.time = time;
		this.anim = anim;
		tTanDer.setFromKF(time, anim);
	}


	public boolean hasDer() {
		return tTanDer != null;
	}

	public void prepareTTan() {
		tTanDer.calcDerivative();
		entryInStart.setTime(0);
		entryInEnd.setTime(100);
		entryOutStart.setTime(100);
		entryOutEnd.setTime(200);
	}

	public void resetEntriesOut(int time) {
		setEntryToTang(entryOutStart);
		setEntrysTo0(entryOutEnd);
		entryOutStart.setTime(100);
		entryOutEnd.setTime(time);
	}

	public void resetEntriesIn(int time) {
		setEntrysTo0(entryInStart);
		setEntryToTang(entryInEnd);
		entryInStart.setTime(time);
		entryInEnd.setTime(100);
	}

	public void setEntryToTang(Entry<T> entry) {
		entry.setValues(tTanDer.tang);
		setEntryValues(entry, 100);
	}

	public void setEntrysTo0(Entry<T> entry) {
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
			((Vec3) entry.value).set(0, 0, 0);
			((Vec3) entry.inTan).set(0, 0, 0);
			((Vec3) entry.outTan).set(0, 0, 0);
		} else if (entry.value instanceof Quat) {
			((Quat) entry.value).set(0, 0, 0, 0);
			((Quat) entry.inTan).set(0, 0, 0, 0);
			((Quat) entry.outTan).set(0, 0, 0, 0);
		}
	}
	public void setEntryValues(Entry<T> entry, int value) {
		if ((entry.value instanceof Float)) {
			Float v = (float) value;
			entry.value = (T) v;
		} else if ((entry.value instanceof Integer)) {
			Integer v = value;
			entry.value = (T) v;
		} else if (entry.value instanceof Vec3) {
			((Vec3) entry.value).set(value, value, value);
		} else if (entry.value instanceof Quat) {
			((Quat) entry.value).set(value, value, value, 0);
		}
	}
	public float[] interpolate(int time) {
		if (time<= 100) {
			switch (timeline.getInterpolationType()) {
				case HERMITE -> tTanDer.hermiteInterp(time, entryInStart, entryInEnd);
				case BEZIER -> tTanDer.bezInterp(time, entryInStart, entryInEnd);
			}
			return entryInEnd.getValueArr();
		} else {
			switch (timeline.getInterpolationType()) {
				case HERMITE -> tTanDer.hermiteInterp(time, entryOutStart, entryOutEnd);
				case BEZIER -> tTanDer.bezInterp(time, entryOutStart, entryOutEnd);
			}
			return entryOutEnd.getValueArr();
		}
	}
	public void interpolateIn(int time) {
		switch (timeline.getInterpolationType()) {
			case HERMITE -> tTanDer.hermiteInterp(time, entryInStart, entryInEnd);
			case BEZIER -> tTanDer.bezInterp(time, entryInStart, entryInEnd);
		}
	}
	public void interpolateOut(int time) {
		switch (timeline.getInterpolationType()) {
			case HERMITE -> tTanDer.hermiteInterp(time, entryOutStart, entryOutEnd);
			case BEZIER -> tTanDer.bezInterp(time, entryOutStart, entryOutEnd);
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
			((Vec3) value).x = newValue;
			return value;
		} else if (value instanceof Quat) {
			((Quat) value).x = newValue;
			return value;
		}
		throw new IllegalArgumentException("Unknown subscripting (set): " + value + ", " + 0 + ", " + newValue);
	}
	public T setObjToNewValue2(T value, float newValue) {
		if ((value instanceof Float)) {
			Float v = newValue;
			value = (T) v;
			return (T) v;
		} else if ((value instanceof Integer)) {
			Integer v = (int) newValue;
			value = (T) v;
			return (T) v;
		} else if (value instanceof Vec3) {
			((Vec3) value).set(newValue, newValue, newValue);
			return value;
		} else if (value instanceof Quat) {
			((Quat) value).set(newValue, newValue, newValue, 0);
			return value;
		}
		throw new IllegalArgumentException("Unknown subscripting (set): " + value + ", " + 0 + ", " + newValue);
	}

//	public float getEndX() {
//		if ((entryEnd.value instanceof Float || entryEnd.value instanceof Integer)) {
//			return (float) entryEnd.value;
//		} else if (entryEnd.value instanceof Vec3) {
//			return ((Vec3) entryEnd.value).getCoord((byte) 0);
//		} else if (entryEnd.value instanceof Quat) {
//			System.out.println("endX quat: " +((Quat) tTanDer.tang.value).getCoord((byte) 0));
//			return ((Quat) tTanDer.tang.value).getCoord((byte) 0);
//		}
//		throw new IllegalArgumentException("Unknown subscripting (get): " + entryEnd.value + ", " + 0);
//	}
//	public float getEndX() {
//		if ((entryEnd.value instanceof Float || entryEnd.value instanceof Integer)) {
//			return (float) entryEnd.value;
//		} else if (entryEnd.value instanceof Vec3) {
//			return ((Vec3) entryEnd.value).getCoord((byte) 0);
//		} else if (entryEnd.value instanceof Quat) {
//			System.out.println("endX quat: " +((Quat) entryEnd.value).getCoord((byte) 0));
//			return ((Quat) entryEnd.value).getCoord((byte) 0);
//		}
//		throw new IllegalArgumentException("Unknown subscripting (get): " + entryEnd.value + ", " + 0);
//	}
	public float getEndXIn() {
		if ((entryInEnd.value instanceof Float || entryInEnd.value instanceof Integer)) {
			return (float) entryInEnd.value;
		} else if (entryInEnd.value instanceof Vec3) {
			return ((Vec3) entryInEnd.value).x;
		} else if (entryInEnd.value instanceof Quat) {
			return ((Quat) entryInEnd.value).x;
		}
		throw new IllegalArgumentException("Unknown subscripting (get): " + entryInEnd.value + ", " + 0);
	}
	public float getEndXOut() {
		if ((entryOutEnd.value instanceof Float || entryOutEnd.value instanceof Integer)) {
			return (float) entryOutEnd.value;
		} else if (entryOutEnd.value instanceof Vec3) {
			return ((Vec3) entryOutEnd.value).x;
		} else if (entryOutEnd.value instanceof Quat) {
			return ((Quat) entryOutEnd.value).x;
		}
		throw new IllegalArgumentException("Unknown subscripting (get): " + entryOutEnd.value + ", " + 0);
	}


	public float getTension() {
		return tTanDer.tension;
	}

	public float getContinuity() {
		return tTanDer.continuity;
	}

	public float getBias() {
		return tTanDer.bias;
	}

	public void setTCB(float tension, float continuity, float bias) {
		tTanDer.tension = tension;
		tTanDer.continuity = continuity;
		tTanDer.bias = bias;

		tTanDer.isLogsReady = false;
		tTanDer.calcDerivative();

//		throw new UnsupportedOperationException("Not finished here, need to have shared access to storing keyframe data and UndoManager");
	}


	public void initFromKF() {
		if (timeline.tans()) {
			TreeMap<Integer, Entry<T>> entryMap = timeline.getEntryMap(null);

			if (entryMap.ceilingKey(time) != null
					&& (timeline.getInterpolationType() == InterpolationType.HERMITE)) {
				tTanDer.calcSplineParameters();
			}

		}
	}


	public void calculateTCB(Sequence anim, int time) {
		if (timeline.tans()) {
			TreeMap<Integer, Entry<T>> entryMap = timeline.getEntryMap(anim);

			if (entryMap.ceilingKey(time) != null
					&& (timeline.getInterpolationType() == InterpolationType.HERMITE)) {
				tTanDer.calcSplineParameters();
				tTanDer.printTCB();
			}
		}
	}


	public void calculateTCB() {
		if (timeline.tans()) {
			TreeMap<Integer, Entry<T>> entryMap = timeline.getEntryMap(anim);
			if (entryMap.ceilingKey(time) != null
					&& (timeline.getInterpolationType() == InterpolationType.HERMITE)) {
				tTanDer.calcSplineParameters();
				tTanDer.printTCB();
			}
		}
	}
}