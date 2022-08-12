package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import java.util.function.Consumer;

public class SmartNumberSlider extends JPanel {
	//	JSlider jSlider = new JSlider();
	AdaptingRangeModel brm;
	JLabel label;
	JSlider jSlider;
	FilteredTextField textField;
	int minMin;
	int minMax;
	int hardMin = Integer.MAX_VALUE;
	int hardMax = Integer.MIN_VALUE;
	int initial;
	boolean expandMin;
	boolean expandMax;

	public SmartNumberSlider() {
		this("", 0, 100, 10, true, true);
	}

	public SmartNumberSlider(String s) {
		this(s, 0, 100, 10, true, true);
	}

	public SmartNumberSlider(String s, int value, int max) {
		this(s, 0, max, value, true, true);
	}

	public SmartNumberSlider(String s, double value, int max) {
		this(s, 0, max, (int) value, true, true);
	}

	public SmartNumberSlider(String s, double value, int max, Consumer<Integer> consumer) {
		this(s, 0, max, (int) value, true, true);
		addValueConsumer(consumer);
	}

	public SmartNumberSlider(String s, double value, int min, int max, Consumer<Integer> consumer) {
		this(s, min, max, (int) value, true, true);
		addValueConsumer(consumer);
	}

	public SmartNumberSlider(String s, double value, int min, int max, Consumer<Integer> consumer, boolean expandMin, boolean expandMax) {
		this(s, min, max, (int) value, expandMin, expandMax);
		addValueConsumer(consumer);
	}

	public SmartNumberSlider(String s, int minMin, int minMax, int initial, boolean expandMin, boolean expandMax) {
		super(new MigLayout("ins 0, fill", "[left, grow][right][right]"));
		this.minMin = minMin;
		this.minMax = minMax;
		this.initial = initial;
		this.expandMin = expandMin;
		this.expandMax = expandMax;
		brm = new AdaptingRangeModel(initial, 0, minMin, minMax);
		jSlider = new JSlider(brm);
		jSlider.setMaximum(minMax);
//		jSlider.addChangeListener(e -> {getPrintln(e);});
		label = new JLabel(s);
		add(label, "growx");
		add(jSlider, "right");
		textField = new FilteredTextField("" + initial, 3);
//		textField.setAllowedCharacters("-1234567890.eE");
		textField.setAllowedCharacters("-1234567890");
		textField.addOnCaretEventFunction((st) -> setValueFromText(st));
		add(textField, "wmin 25");
	}

	public int getValue() {
		return jSlider.getValue();
	}

	private void setValueFromText(String s) {
		if (s.matches("\\d+")) {
			brm.setValue(Integer.parseInt(s));
		}
	}

	public SmartNumberSlider addValueConsumer(Consumer<Integer> consumer) {
		jSlider.addChangeListener(e -> consumer.accept(jSlider.getValue()));
		//todo fix this to not throw error...
		jSlider.addChangeListener(e -> setTextfieldText());

		return this;
	}

	private void setTextfieldText() {
		try {
			textField.setText("" + jSlider.getValue());
		} catch (Exception e) {

		}
	}

	public SmartNumberSlider setMaxUpperLimit(int maxUpperLimit) {
		brm.setMaxUpperLimit(maxUpperLimit);
		return this;
	}

	public SmartNumberSlider setMinLowerLimit(int minLowerLimit) {
		brm.setMinLowerLimit(minLowerLimit);
		return this;
	}

	public SmartNumberSlider setMaxLowerLimit(int maxLowerLimit) {
		brm.setMaxLowerLimit(maxLowerLimit);
		return this;
	}

	public SmartNumberSlider setMinUpperLimit(int minUpperLimit) {
		brm.setMinUpperLimit(minUpperLimit);
		return this;
	}

	public SmartNumberSlider setExpandMin(boolean expandMin) {
		this.expandMin = expandMin;
		return this;
	}

	public SmartNumberSlider setExpandMax(boolean expandMax) {
		this.expandMax = expandMax;
		return this;
	}

	private void getPrintln(ChangeEvent e) {
		System.out.println(e.getSource() + " is adj: " + ((JSlider) e.getSource()).getValueIsAdjusting());
	}

	private void updateLimits() {

	}

	private void ugg() {
		DefaultBoundedRangeModel d = new DefaultBoundedRangeModel();
	}

	private BoundedRangeModel getBoundedRangeModel() {
		return new BoundedRangeModel() {

			protected transient ChangeEvent changeEvent = null;

			/** The listeners waiting for model changes. */
			protected EventListenerList listenerList = new EventListenerList();

			int minMin;
			int minMax;
			int min;
			int max;
			int minUpperLimit = Integer.MAX_VALUE;
			int minLowerLimit = Integer.MIN_VALUE;
			int maxUpperLimit = Integer.MAX_VALUE;
			int maxLowerLimit = Integer.MIN_VALUE;
			int initial;
			boolean expandMin;
			boolean expandMax;
			int value;

			@Override
			public int getMinimum() {
				return min;
			}

			@Override
			public void setMinimum(int newMinimum) {
				if(minLowerLimit <= newMinimum && newMinimum <= minUpperLimit){
					min = newMinimum;
				}
			}

			@Override
			public int getMaximum() {
				return max;
			}

			@Override
			public void setMaximum(int newMaximum) {
				if(maxLowerLimit <= newMaximum && newMaximum <= maxUpperLimit){
					max = newMaximum;
				}
			}

			@Override
			public int getValue() {
				return value;
			}

			@Override
			public void setValue(int newValue) {
				value = newValue;
			}

			@Override
			public void setValueIsAdjusting(boolean b) {

			}

			@Override
			public boolean getValueIsAdjusting() {
				return false;
			}

			@Override
			public int getExtent() {
				return 0;
			}

			@Override
			public void setExtent(int newExtent) {
			}

			@Override
			public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {

			}

			@Override
			public void addChangeListener(ChangeListener x) {

			}

			@Override
			public void removeChangeListener(ChangeListener x) {

			}
		};
	}

}
