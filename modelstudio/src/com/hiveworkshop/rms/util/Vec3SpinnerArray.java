package com.hiveworkshop.rms.util;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.function.Consumer;

public class Vec3SpinnerArray {
	private JSpinner[] spinners = new JSpinner[3];
	private JLabel[] labels = new JLabel[3];
	private String labelWrap = "wrap";
	private String spinnerWrap = "";
	private String labelConst = "";
	private String spinnerConst = "";
	private boolean isEnabled = true;

	Consumer<Vec3> vec3Consumer;

	boolean isUpdating = false;

	public Vec3SpinnerArray() {
		this(new Vec3(), "", "", "");
	}

	public Vec3SpinnerArray(String l1, String l2, String l3) {
		this(new Vec3(), l1, l2, l3);
	}

	public Vec3SpinnerArray(Vec3 startV, String l1, String l2, String l3) {
		spinners[0] = getStandardSpinner(startV.x);
		spinners[1] = getStandardSpinner(startV.y);
		spinners[2] = getStandardSpinner(startV.z);


		spinners[0].addChangeListener(this::runConsumer);
		spinners[1].addChangeListener(this::runConsumer);
		spinners[2].addChangeListener(this::runConsumer);

		labels[0] = new JLabel(l1);
		labels[1] = new JLabel(l2);
		labels[2] = new JLabel(l3);
	}

	static JSpinner getStandardSpinner(double startValue) {
		return new JSpinner(new SpinnerNumberModel(startValue, -100000.00, 100000.00, 0.1));
	}

	public JPanel spinnerPanel() {
		JPanel spinnerPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		JPanel xPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		xPanel.add(labels[0], labelConst + ", " + labelWrap);
		xPanel.add(spinners[0], spinnerConst);
		JPanel yPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		yPanel.add(labels[1], labelConst + ", " + labelWrap);
		yPanel.add(spinners[1], spinnerConst);
		JPanel zPanel = new JPanel(new MigLayout("gap 0, ins 0"));
		zPanel.add(labels[2], labelConst + ", " + labelWrap);
		zPanel.add(spinners[2], spinnerConst);

		spinnerPanel.add(xPanel, spinnerWrap);
		spinnerPanel.add(yPanel, spinnerWrap);
		spinnerPanel.add(zPanel, spinnerWrap);

		return spinnerPanel;
	}

	public Vec3 getValue() {
		double vX = ((Number) spinners[0].getValue()).doubleValue();
		double vY = ((Number) spinners[1].getValue()).doubleValue();
		double vZ = ((Number) spinners[2].getValue()).doubleValue();
		return new Vec3(vX, vY, vZ);
	}

	public Vec3SpinnerArray setValues(Vec3 newValues) {
		isUpdating = true;
		spinners[0].setValue(newValues.x);
		spinners[1].setValue(newValues.y);
		spinners[2].setValue(newValues.z);
		isUpdating = false;
		return this;
	}

	public Vec3SpinnerArray setEnabled(boolean b) {
		isEnabled = b;
		spinners[0].setEnabled(b);
		spinners[1].setEnabled(b);
		spinners[2].setEnabled(b);
		return this;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public Vec3SpinnerArray setLabelWrap(boolean b) {
		labelWrap = b ? "wrap" : "";
		return this;
	}

	public Vec3SpinnerArray setSpinnerWrap(boolean b) {
		spinnerWrap = b ? "wrap" : "";
		return this;
	}

	public Vec3SpinnerArray setLabelConstrains(String constrains) {
		labelConst = constrains;
		return this;
	}

	public Vec3SpinnerArray setSpinnerConstrains(String constrains) {
		spinnerConst = constrains;
		return this;
	}

	boolean ugg;

//	public Vec3SpinnerArray addActionListener(Runnable actionListener){
////		spinners[0].addChangeListener(e -> System.out.println("ChangeListener"));
////		spinners[0].addPropertyChangeListener(e -> System.out.println("PropertyChange"));
//		ChangeListener changeListener = e -> {
//			if (!isUpdating) {
//				if (ugg) {
//					actionListener.run();
//				}
//				ugg = !ugg; // this is just an ugly temporary hack to run the actionListener only on the second change event fire
//			}
//		};
//		spinners[0].addChangeListener(changeListener);
//		spinners[1].addChangeListener(changeListener);
//		spinners[2].addChangeListener(changeListener);
//		return this;
//	}

	public Vec3SpinnerArray addActionListener(Consumer<Vec3> consumer) {
		ChangeListener changeListener = e -> {
			if (!isUpdating) {
				if (ugg) {
					consumer.accept(getValue()); // this is just an ugly temporary hack to run the actionListener only on the second change event fire
				}
				ugg = !ugg;
			}
		};
		spinners[0].addChangeListener(changeListener);
		spinners[1].addChangeListener(changeListener);
		spinners[2].addChangeListener(changeListener);
		return this;
	}

	public Vec3SpinnerArray setVec3Consumer(Consumer<Vec3> consumer) {
		this.vec3Consumer = consumer;
		return this;
	}


	private void runConsumer() {
		if (vec3Consumer != null) {
			vec3Consumer.accept(getValue());
		}
	}

	private void runConsumer(ChangeEvent e) {
		if (vec3Consumer != null) {
			if (!isUpdating) {
				if (ugg) {
					vec3Consumer.accept(getValue());
				}
				ugg = !ugg;
			}
		}
	}
}
