package com.hiveworkshop.rms.util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.io.Serializable;
import java.util.EventListener;

//public class AdaptingRangeModel {
//}
public class AdaptingRangeModel implements BoundedRangeModel, Serializable {
	/**
	 * Only one <code>ChangeEvent</code> is needed per model instance since the
	 * event's only (read-only) state is the source property.  The source
	 * of events generated here is always "this".
	 */
	protected transient ChangeEvent changeEvent = null;

	/**
	 * The listeners waiting for model changes.
	 */
	protected EventListenerList listenerList = new EventListenerList();

	private int value = 0;
	private int extent = 0; // = max-value (?)
	private int min = 0;
	private int max = 100;
	private boolean isAdjusting = false;
	private int minMin = -10000;
	private int minMax = 10000;


	/**
	 * Initializes all of the properties with default values.
	 * Those values are:
	 * <ul>
	 * <li><code>value</code> = 0
	 * <li><code>extent</code> = 0
	 * <li><code>minimum</code> = 0
	 * <li><code>maximum</code> = 100
	 * <li><code>adjusting</code> = false
	 * </ul>
	 */
	public AdaptingRangeModel() {
	}


	/**
	 * Initializes value, extent, minimum and maximum. Adjusting is false.
	 * Throws an <code>IllegalArgumentException</code> if the following
	 * constraints aren't satisfied:
	 * <pre>
	 * min &lt;= value &lt;= value+extent &lt;= max
	 * </pre>
	 *
	 * @param value  an int giving the current value
	 * @param extent the length of the inner range that begins at the model's value
	 * @param min    an int giving the minimum value
	 * @param max    an int giving the maximum value
	 */
	public AdaptingRangeModel(int value, int extent, int min, int max) {

		this.min = Math.min(min, max);
		this.max = Math.max(min, max);
		this.value = Math.max(min, Math.min(value, max));
		this.extent = Math.min(extent, max - value);
		if ((max >= min) &&
				(value >= min) &&
				((value + extent) >= value) &&
				((value + extent) <= max)) {
			this.value = value;
			this.extent = extent;
			this.min = min;
			this.max = max;
		} else {
			throw new IllegalArgumentException("invalid range properties");
		}
	}

	public int getValue() {
		return value;
	}

	public int getExtent() {
		return extent;
	}

	public int getMinimum() {
		return min;
	}

	public int getMaximum() {
		return max;
	}


	/**
	 * Sets the current value of the model. For a slider, that
	 * determines where the knob appears. Ensures that the new
	 * value, <I>n</I> falls within the model's constraints:
	 * <pre>
	 *     minimum &lt;= value &lt;= value+extent &lt;= maximum
	 * </pre>
	 *
	 * @see BoundedRangeModel#setValue
	 */
	public void setValue(int n) {
//		System.out.println("setValue: " + n);
		n = Math.min(n, Integer.MAX_VALUE - extent);

		int newValue = Math.max(n, min);
		if (newValue + extent > max) {
			newValue = max - extent;
		}
		setRangeProperties(newValue, extent, min, max, isAdjusting);
	}


	/**
	 * Sets the extent to <I>n</I> after ensuring that <I>n</I>
	 * is greater than or equal to zero and falls within the model's
	 * constraints:
	 * <pre>
	 *     minimum &lt;= value &lt;= value+extent &lt;= maximum
	 * </pre>
	 *
	 * @see BoundedRangeModel#setExtent
	 */
	public void setExtent(int n) {
//		System.out.println("setExtent: " + n);
		int newExtent = Math.max(0, n);
		if (value + newExtent > max) {
			newExtent = max - value;
		}
		setRangeProperties(value, newExtent, min, max, isAdjusting);
	}


	public void setMinimum(int newMin) {
//		System.out.println("setMinimum: " + newMin);
		int newMax = Math.max(newMin, max);
		int newValue = Math.max(newMin, value);
		int newExtent = Math.min(newMax - newValue, extent);
		setRangeProperties(newValue, newExtent, newMin, newMax, isAdjusting);
	}

	public void setMaximum(int newMax) {
//		System.out.println("setMaximum: " + newMax);
		int newMin = Math.min(newMax, min);
		int newExtent = Math.min(newMax - newMin, extent);
		int newValue = Math.min(newMax - newExtent, value);
		setRangeProperties(newValue, newExtent, newMin, newMax, isAdjusting);
	}

	public void setValueIsAdjusting(boolean b) {
//		System.out.println("setValueIsAdjusting: " + b);
		setRangeProperties(value, extent, min, max, b);
	}

	public boolean getValueIsAdjusting() {
		return isAdjusting;
	}

	public void setRangeProperties(int newValue, int newExtent, int newMin, int newMax, boolean adjusting) {
		if (newMin > newMax) {
			newMin = newMax;
		}
		if (newValue > newMax) {
			newMax = newValue;
		}
		if (newValue < newMin) {
			newMin = newValue;
		}

		/* Convert the addends to long so that extent can be
		 * Integer.MAX_VALUE without rolling over the sum.
		 * A JCK test covers this, see bug 4097718.
		 */
		if (((long) newExtent + (long) newValue) > newMax) {
			newExtent = newMax - newValue;
		}

		if (newExtent < 0) {
			newExtent = 0;
		}

		boolean isChange = (newValue != value) ||
				(newExtent != extent) ||
				(newMin != min) ||
				(newMax != max) ||
				(adjusting != isAdjusting);

		if (isChange) {
			value = newValue;
			extent = newExtent;
			min = newMin;
			max = newMax;
			isAdjusting = adjusting;

			fireStateChanged();
		}
	}

	public void addChangeListener(ChangeListener l) {
		listenerList.add(ChangeListener.class, l);
	}

	public void removeChangeListener(ChangeListener l) {
		listenerList.remove(ChangeListener.class, l);
	}

	public ChangeListener[] getChangeListeners() {
		return listenerList.getListeners(ChangeListener.class);
	}


	/**
	 * Runs each <code>ChangeListener</code>'s <code>stateChanged</code> method.
	 *
	 * @see #setRangeProperties
	 * @see EventListenerList
	 */
	protected void fireStateChanged() {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ChangeListener.class) {
				if (changeEvent == null) {
					changeEvent = new ChangeEvent(this);
				}
				((ChangeListener) listeners[i + 1]).stateChanged(changeEvent);
			}
		}
	}

	public String toString() {
		String modelString =
				"value=" + getValue() + ", " +
						"extent=" + getExtent() + ", " +
						"min=" + getMinimum() + ", " +
						"max=" + getMaximum() + ", " +
						"adj=" + getValueIsAdjusting();

		return getClass().getName() + "[" + modelString + "]";
	}

	public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
		return listenerList.getListeners(listenerType);
	}
}