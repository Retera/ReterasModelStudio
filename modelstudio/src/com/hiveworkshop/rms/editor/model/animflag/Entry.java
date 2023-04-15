package com.hiveworkshop.rms.editor.model.animflag;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.Objects;

public class Entry<T> {
	public Integer time;
	public T value, inTan, outTan;

	public Entry(Integer time, T value, T inTan, T outTan) {
		this.time = time;
		this.value = value;
		this.inTan = inTan;
		this.outTan = outTan;
	}

	public Entry(Integer time, T value) {
		this.time = time;
		this.value = value;
	}

	public Entry(Entry<T> other) {
		this.time = other.time;
		this.value = cloneEntryValue(other.value);
		this.inTan = cloneEntryValue(other.inTan);
		this.outTan = cloneEntryValue(other.outTan);
	}

	public void cloneFrom(Entry<T> other) {
		time = other.time;
		value = cloneEntryValue(other.value);
		inTan = cloneEntryValue(other.inTan);
		outTan = cloneEntryValue(other.outTan);
	}

	private T cloneEntryValue(T value) {
		if (value == null) {
			return null;
		}

		if (value instanceof Integer || value instanceof Float) {
			return value;
		} else if (value instanceof Vec3) {
			return (T) new Vec3((Vec3) value);
		} else if (value instanceof Quat) {
			return (T) new Quat((Quat) value);
		} else if (value instanceof Bitmap) {
			return value;
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}
	public Entry<T> setSetValue(T oValue) {
		if (value instanceof Integer || value instanceof Float || value instanceof Bitmap) {
			value = oValue;
		} else if (value instanceof Vec3 && oValue instanceof Vec3) {
			((Vec3) value).set((Vec3) oValue);
		} else if (value instanceof Quat && oValue instanceof Quat) {
			((Quat) value).set((Quat) oValue);
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
		return this;
	}


	public Entry<T> setSetInTan(T oInTan) {
		if (value instanceof Integer || value instanceof Float || value instanceof Bitmap) {
			inTan = oInTan;
		} else if (value instanceof Vec3) {
			if (inTan != null && oInTan != null) {
				((Vec3) inTan).set((Vec3) oInTan);
			} else {
				inTan = cloneEntryValue(oInTan);
			}
		} else if (value instanceof Quat) {
			if (inTan != null && oInTan != null) {
				((Quat) inTan).set((Quat) oInTan);
			} else {
				inTan = cloneEntryValue(oInTan);
			}
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
		return this;
	}

	public Entry<T> setSetOutTan(T oOutTan) {
		if (value instanceof Integer || value instanceof Float || value instanceof Bitmap) {
			outTan = oOutTan;
		} else if (value instanceof Vec3) {
			if (outTan != null && oOutTan != null) {
				((Vec3) outTan).set((Vec3) oOutTan);
			} else {
				outTan = cloneEntryValue(oOutTan);
			}
		} else if (value instanceof Quat) {
			if (outTan != null && oOutTan != null) {
				((Quat) outTan).set((Quat) oOutTan);
			} else {
				outTan = cloneEntryValue(oOutTan);
			}
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
		return this;
	}

	private boolean setValue(T value, T otherValue) {
		if (value instanceof Integer || value instanceof Float) {
			return false;
		} else if (value instanceof Vec3 && otherValue instanceof Vec3) {
			((Vec3) value).set((Vec3) otherValue);
			return true;
		} else if (value instanceof Quat && otherValue instanceof Quat) {
			((Quat) value).set((Quat) otherValue);
			return true;
		} else {
			return false;
//			throw new IllegalStateException(value.getClass().getName());
		}
	}
	public Entry<T> setValues(Entry<T> other) {
		time = other.time;
		if (value instanceof Integer || value instanceof Float || value instanceof Bitmap) {
			value = other.value;
			inTan = other.inTan;
			outTan = other.outTan;
		} else if (value instanceof Vec3 && other.value instanceof Vec3) {
			((Vec3) value).set((Vec3) other.value);
			if (inTan != null && outTan != null && other.inTan != null && other.outTan != null) {
				((Vec3) inTan).set((Vec3) other.inTan);
				((Vec3) outTan).set((Vec3) other.outTan);
			} else if (inTan == null && outTan == null && other.inTan != null && other.outTan != null) {
				inTan = cloneEntryValue(other.inTan);
				outTan = cloneEntryValue(other.outTan);
			}
		} else if (value instanceof Quat && other.value instanceof Quat) {
			((Quat) value).set((Quat) other.value);
			if (inTan != null && outTan != null && other.inTan != null && other.outTan != null) {
				((Quat) inTan).set((Quat) other.inTan);
				((Quat) outTan).set((Quat) other.outTan);
			} else if (inTan == null && outTan == null && other.inTan != null && other.outTan != null) {
				inTan = cloneEntryValue(other.inTan);
				outTan = cloneEntryValue(other.outTan);
			}
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
		return this;
	}

	public Integer getTime() {
		return time;
	}

	public Entry<T> setTime(Integer time) {
		this.time = time;
		return this;
	}

	public T getValue() {
		return value;
	}

	public Entry<T> setValue(T value) {
		this.value = value;
		return this;
	}

	public T getInTan() {
		return inTan;
	}

	public Entry<T> setInTan(T inTan) {
		this.inTan = inTan;
		return this;
	}

	public T getOutTan() {
		return outTan;
	}

	public Entry<T> setOutTan(T outTan) {
		this.outTan = outTan;
		return this;
	}

	public Entry<T> linearize() {
		inTan = null;
		outTan = null;
		return this;
	}

	public Entry<T> unLinearize() {
		if (inTan == null) {
//			inTan = cloneEntryValue(value);
			inTan = getZeroValue();
		}
		if (outTan == null) {
//			outTan = cloneEntryValue(value);
			outTan = getZeroValue();
		}
		return this;
	}

	public boolean isTangential() {
		return inTan != null && outTan != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o instanceof Entry) {
			Entry<?> entry = (Entry<?>) o;
			if (value instanceof Vec3) {
				return entry.value instanceof Vec3 && time.equals(entry.time) && ((Vec3) value).equalLocs((Vec3) entry.value)
						&& (inTan == null && entry.inTan == null || inTan != null && ((Vec3) inTan).equalLocs((Vec3) entry.inTan))
						&& (outTan == null && entry.outTan == null || outTan != null && ((Vec3) outTan).equalLocs((Vec3) entry.outTan));
			} else {
				return time.equals(entry.time) && value.equals(entry.value) && Objects.equals(inTan, entry.inTan) && Objects.equals(outTan, entry.outTan);
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(time, value, inTan, outTan);
	}

	public Entry<T> deepCopy() {
		return new Entry<>(this);
	}

	public String toString() {
		return "time: " + time + "\nvalue: " + value + "\ninTan: " + inTan + "\noutTan: " + outTan;
//		if(inTan == null && outTan == null){
//			return "time: " + time + ", value: " + value;
//		}
//		return "time: " + time + ", value: " + value + ", inTan: " + inTan + ", outTan: " + outTan;
//		if(time == 0){
//			return "\ntime: " + time + "\tvalue: " + value + "\n";
//		}
//		else return "";
//		if(inTan != null){
//			return "\ntime: " + time + "\tvalue: " + value + "\tinTan: " + inTan + "\toutTan: " + outTan + "\t";
//		} else {
//			return "\ntime: " + time + "\tvalue: " + value + "\t";
//		}
	}

	private T getZeroValue(){
		if (value == null) {
			return null;
		}

		if (value instanceof Integer) {
			return (T) Integer.valueOf(0);
		} else if (value instanceof Float) {
			return (T) Float.valueOf(0);
		} else if (value instanceof Vec3) {
			return (T) new Vec3(0,0,0);
		} else if (value instanceof Quat) {
			return (T) new Quat(0,0,0,1);
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}

//	@Override
//	public int compareTo(Entry<?> o) {
//		return time - o.time;
//	}



	public float[] getValueArr() {
		return getAsArr(value);
	}

	public float[] getInTanArr() {
		return getAsArr(inTan);
	}

	public float[] getOutTanArr() {
		return getAsArr(outTan);
	}

	private float[] getAsArr(T o){
		T o2;
		if(o == null){
			o2 = getZeroValue();
		} else {
			o2 = o;
		}
		if (o2 instanceof Integer) {
			int oi = (Integer) o2;
			// this is to account for float accuracy when casting to int or long
			float of = oi + Math.copySign(0.001f, oi);
			return new float[]{of};
		} else if (o2 instanceof Float) {
			return new float[]{(Float) o2};
		} else if (o2 instanceof Vec3) {
			return ((Vec3)o2).toFloatArray();
		} else if (o2 instanceof Quat) {
			return ((Quat) o2).toFloatArray();
		} else if (o2 instanceof Bitmap) {
			return null;
		} else {
			throw new IllegalStateException(value.getClass().getName());
		}
	}
}
