package com.hiveworkshop.rms.ui.application.model;

import com.hiveworkshop.rms.editor.model.ExtLog;
import com.hiveworkshop.rms.ui.application.model.editors.ComponentEditorJSpinner;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.ParseException;

public class ExtLogEditor extends JPanel {
	private static final Dimension MAXIMUM_SIZE = new Dimension(99999, 25);
	private final JCheckBox minimumExtentBox;
	private final ComponentEditorJSpinner minimumExtentX;
	private final ComponentEditorJSpinner minimumExtentY;
	private final ComponentEditorJSpinner minimumExtentZ;
	private final JCheckBox maximumExtentBox;
	private final ComponentEditorJSpinner maximumExtentX;
	private final ComponentEditorJSpinner maximumExtentY;
	private final ComponentEditorJSpinner maximumExtentZ;
	private final JCheckBox boundsRadiusBox;
	private final ComponentEditorJSpinner boundsRadius;

	public ExtLogEditor() {
		minimumExtentBox = new JCheckBox("Minimum Extent");

		minimumExtentX = new ComponentEditorJSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.0));
		minimumExtentX.setMaximumSize(MAXIMUM_SIZE);

		minimumExtentY = new ComponentEditorJSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.0));
		minimumExtentY.setMaximumSize(MAXIMUM_SIZE);

		minimumExtentZ = new ComponentEditorJSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.0));
		minimumExtentZ.setMaximumSize(MAXIMUM_SIZE);
		minimumExtentBox.addActionListener(e -> updateMinExtOptionsAvailable());

		maximumExtentBox = new JCheckBox("Maximum Extent");

		maximumExtentX = new ComponentEditorJSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.0));
		maximumExtentX.setMaximumSize(MAXIMUM_SIZE);

		maximumExtentY = new ComponentEditorJSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.0));
		maximumExtentY.setMaximumSize(MAXIMUM_SIZE);

		maximumExtentZ = new ComponentEditorJSpinner(new SpinnerNumberModel(0., -Integer.MAX_VALUE, Integer.MAX_VALUE, 1.0));
		maximumExtentZ.setMaximumSize(MAXIMUM_SIZE);

		maximumExtentBox.addActionListener(e -> updateMaxExtOptionsAvailable());

		boundsRadiusBox = new JCheckBox("Bounds Radius");
		boundsRadius = new ComponentEditorJSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		boundsRadius.setMaximumSize(MAXIMUM_SIZE);
		boundsRadiusBox.addActionListener(e -> updateBoundRadiusOptionsAvailable());

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(16)
				.addGroup(layout.createParallelGroup()
						.addComponent(minimumExtentBox)
						.addGroup(layout.createSequentialGroup()
								.addComponent(minimumExtentX)
								.addComponent(minimumExtentY)
								.addComponent(minimumExtentZ))
						.addComponent(maximumExtentBox)
						.addGroup(layout.createSequentialGroup()
								.addComponent(maximumExtentX)
								.addComponent(maximumExtentY)
								.addComponent(maximumExtentZ))
						.addComponent(boundsRadiusBox)
						.addComponent(boundsRadius)).addGap(16));

		layout.setVerticalGroup(layout.createSequentialGroup().addGap(16)
				.addGroup(layout.createSequentialGroup()
						.addComponent(minimumExtentBox)
						.addGroup(layout.createParallelGroup()
								.addComponent(minimumExtentX)
								.addComponent(minimumExtentY)
								.addComponent(minimumExtentZ))
						.addComponent(maximumExtentBox)
						.addGroup(layout.createParallelGroup()
								.addComponent(maximumExtentX)
								.addComponent(maximumExtentY)
								.addComponent(maximumExtentZ))
						.addComponent(boundsRadiusBox)
						.addComponent(boundsRadius)).addGap(16));

		setLayout(layout);
	}

	public void addActionListener(final Runnable actionListener) {
		minimumExtentX.addActionListener(actionListener);
		minimumExtentY.addActionListener(actionListener);
		minimumExtentZ.addActionListener(actionListener);

		maximumExtentX.addActionListener(actionListener);
		maximumExtentY.addActionListener(actionListener);
		maximumExtentZ.addActionListener(actionListener);

		boundsRadius.addActionListener(actionListener);
		final ActionListener actionAdapter = e -> actionListener.run();
		minimumExtentBox.addActionListener(actionAdapter);
		maximumExtentBox.addActionListener(actionAdapter);
		boundsRadiusBox.addActionListener(actionAdapter);
	}

	private void updateMinExtOptionsAvailable() {
		final boolean minExtSelected = minimumExtentBox.isSelected();
		minimumExtentX.setEnabled(minExtSelected);
		minimumExtentY.setEnabled(minExtSelected);
		minimumExtentZ.setEnabled(minExtSelected);
	}

	private void updateMaxExtOptionsAvailable() {
		final boolean maxExtSelected = maximumExtentBox.isSelected();
		maximumExtentX.setEnabled(maxExtSelected);
		maximumExtentY.setEnabled(maxExtSelected);
		maximumExtentZ.setEnabled(maxExtSelected);
	}

	private void updateBoundRadiusOptionsAvailable() {
		final boolean selected = boundsRadiusBox.isSelected();
		boundsRadius.setEnabled(selected);
	}

	public void setExtLog(final ExtLog extents) {
		final Vec3 minimumExtent = extents == null ? null : extents.getMinimumExtent();
		final boolean hasMinExt = minimumExtent != null;
		minimumExtentBox.setSelected(hasMinExt);
		updateMinExtOptionsAvailable();
		if (hasMinExt) {
			minimumExtentX.reloadNewValue(minimumExtent.x);
			minimumExtentY.reloadNewValue(minimumExtent.y);
			minimumExtentZ.reloadNewValue(minimumExtent.z);
		}

		final Vec3 maximumExtent = extents == null ? null : extents.getMaximumExtent();
		final boolean hasMaxExt = maximumExtent != null;
		maximumExtentBox.setSelected(hasMaxExt);
		updateMaxExtOptionsAvailable();
		if (hasMaxExt) {
			maximumExtentX.reloadNewValue(maximumExtent.x);
			maximumExtentY.reloadNewValue(maximumExtent.y);
			maximumExtentZ.reloadNewValue(maximumExtent.z);
		}

		final boolean hasBoundsRadius = extents != null && extents.hasBoundsRadius();
		boundsRadiusBox.setSelected(hasBoundsRadius);
		updateBoundRadiusOptionsAvailable();
		if (hasBoundsRadius) {
			boundsRadius.reloadNewValue(extents.getBoundsRadius());
		}
	}

	public ExtLog getExtLog() {
		final Vec3 minimumExtent, maximumExtent;
		final double boundsRadius;
		if (minimumExtentBox.isSelected()) {
			minimumExtent = new Vec3(val(minimumExtentX), val(minimumExtentY), val(minimumExtentZ));
		} else {
			minimumExtent = null;
		}
		if (maximumExtentBox.isSelected()) {
			maximumExtent = new Vec3(val(maximumExtentX), val(maximumExtentY), val(maximumExtentZ));
		} else {
			maximumExtent = null;
		}
		if (boundsRadiusBox.isSelected()) {
			boundsRadius = val(this.boundsRadius);
		} else {
			boundsRadius = ExtLog.NO_BOUNDS_RADIUS;
		}
		return new ExtLog(minimumExtent, maximumExtent, boundsRadius);
	}

	private double val(final JSpinner spinner) {
		return ((Number) spinner.getValue()).doubleValue();
	}

	public void commitEdits() {
		try {
			minimumExtentX.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		try {
			minimumExtentY.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		try {
			minimumExtentZ.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		try {
			maximumExtentX.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		try {
			maximumExtentY.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
		try {
			maximumExtentZ.commitEdit();
		} catch (final ParseException e) {
			e.printStackTrace();
		}
	}
}
