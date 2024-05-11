package com.hiveworkshop.rms.util;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import java.util.function.Consumer;

public class TwiNumberSlider extends JSlider {
	AdaptingRangeModel brm;
	int minMin;
	int minMax;
	int initial;
	boolean expandMin;
	boolean expandMax;

	public TwiNumberSlider() {
		this(0, 100, 10, true, true);
	}

	public TwiNumberSlider(int value, int max) {
		this(0, max, value, true, true);
	}

	public TwiNumberSlider(double value, int max) {
		this(0, max, (int) value, true, true);
	}

	public TwiNumberSlider(double value, int max, Consumer<Integer> consumer) {
		this(0, max, (int) value, true, true);
		addValueConsumer(consumer);
	}

	public TwiNumberSlider(double value, int min, int max, Consumer<Integer> consumer) {
		this(min, max, (int) value, true, true);
		addValueConsumer(consumer);
	}

	public TwiNumberSlider(double value, int min, int max, Consumer<Integer> consumer, boolean expandMin, boolean expandMax) {
		this(min, max, (int) value, expandMin, expandMax);
		addValueConsumer(consumer);
	}

	public TwiNumberSlider(int minSoftLimit, int maxSoftLimit, int initial, boolean expandMin, boolean expandMax) {
		this.minMin = minSoftLimit;
		this.minMax = maxSoftLimit;
		this.initial = initial;
		this.expandMin = expandMin;
		this.expandMax = expandMax;
		brm = new AdaptingRangeModel(initial, 0, minSoftLimit, maxSoftLimit);
		brm.setMaxLowerLimit(maxSoftLimit);
		brm.setMinUpperLimit(minSoftLimit);
		setModel(brm);

		if (!expandMax) {
			brm.setMaxUpperLimit(maxSoftLimit);
		}
		if (!expandMin) {
			brm.setMinLowerLimit(minSoftLimit);
		}

	}

	public TwiNumberSlider setValue_(int value) {
		setValue(value);
		return this;
	}

	private void setValueFromText(String s) {
		if (s.matches("\\d+")) {
			brm.setValue(Integer.parseInt(s));
		}
	}

	public TwiNumberSlider addValueConsumer(Consumer<Integer> consumer) {
		addChangeListener(e -> consumer.accept(getValue()));

		return this;
	}


	public TwiNumberSlider setMaxUpperLimit(int maxUpperLimit) {
		brm.setMaxUpperLimit(maxUpperLimit);
		return this;
	}

	public TwiNumberSlider setMinLowerLimit(int minLowerLimit) {
		brm.setMinLowerLimit(minLowerLimit);
		return this;
	}

	public TwiNumberSlider setMaxLowerLimit(int maxLowerLimit) {
		brm.setMaxLowerLimit(maxLowerLimit);
		return this;
	}

	public TwiNumberSlider setMinUpperLimit(int minUpperLimit) {
		brm.setMinUpperLimit(minUpperLimit);
		return this;
	}

	public TwiNumberSlider setExpandMin(boolean expandMin) {
		this.expandMin = expandMin;

		if (!expandMin) {
			brm.setMinLowerLimit(minMin);
		}
//		brm.setMinLowerLimit(expandMin ? Integer.MIN_VALUE : minMin);

		return this;
	}

	public TwiNumberSlider setExpandMax(boolean expandMax) {
		this.expandMax = expandMax;
		if (!expandMax) {
			brm.setMaxUpperLimit(minMax);
		}
//		brm.setMaxUpperLimit(expandMax ? Integer.MIN_VALUE : minMax);
		return this;
	}

	private void getPrintln(ChangeEvent e) {
		System.out.println(e.getSource() + " is adj: " + ((JSlider) e.getSource()).getValueIsAdjusting());
	}

	private void updateLimits() {
	}

}
