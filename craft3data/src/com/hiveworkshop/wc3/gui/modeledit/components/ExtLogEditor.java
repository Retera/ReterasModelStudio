package com.hiveworkshop.wc3.gui.modeledit.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Vertex;

public class ExtLogEditor extends JPanel {
	private final JCheckBox minimumExtentBox;
	private final JSpinner minimumExtentX;
	private final JSpinner minimumExtentY;
	private final JSpinner minimumExtentZ;
	private final JCheckBox maximumExtentBox;
	private final JSpinner maximumExtentX;
	private final JSpinner maximumExtentY;
	private final JSpinner maximumExtentZ;
	private final JCheckBox boundsRadiusBox;
	private final JSpinner boundsRadius;

	public ExtLogEditor() {
		minimumExtentBox = new JCheckBox("Minimum Extent");
		minimumExtentX = new JSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		minimumExtentY = new JSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		minimumExtentZ = new JSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		minimumExtentBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updateMinExtOptionsAvailable();
			}
		});
		maximumExtentBox = new JCheckBox("Maximum Extent");
		maximumExtentX = new JSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		maximumExtentY = new JSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		maximumExtentZ = new JSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		maximumExtentBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updateMaxExtOptionsAvailable();
			}
		});
		boundsRadiusBox = new JCheckBox("Bounds Radius");
		boundsRadius = new JSpinner(new SpinnerNumberModel(0., -Long.MAX_VALUE, Long.MAX_VALUE, 1.0));
		boundsRadiusBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				updateBoundRadiusOptionsAvailable();
			}
		});

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addGap(16)
				.addGroup(layout.createParallelGroup().addComponent(minimumExtentBox)
						.addGroup(layout.createSequentialGroup().addComponent(minimumExtentX)
								.addComponent(minimumExtentY).addComponent(minimumExtentZ))
						.addComponent(maximumExtentBox)
						.addGroup(layout.createSequentialGroup().addComponent(maximumExtentX)
								.addComponent(maximumExtentY).addComponent(maximumExtentZ))
						.addComponent(boundsRadiusBox).addComponent(boundsRadius))
				.addGap(16));
		layout.setVerticalGroup(
				layout.createSequentialGroup().addGap(16)
						.addGroup(layout.createSequentialGroup().addComponent(minimumExtentBox)
								.addGroup(layout.createParallelGroup().addComponent(minimumExtentX)
										.addComponent(minimumExtentY).addComponent(minimumExtentZ))
								.addComponent(maximumExtentBox)
								.addGroup(layout.createParallelGroup().addComponent(maximumExtentX)
										.addComponent(maximumExtentY).addComponent(maximumExtentZ))
								.addComponent(boundsRadiusBox).addComponent(boundsRadius))
						.addGap(16));
		setLayout(layout);
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
		final Vertex minimumExtent = extents.getMinimumExtent();
		final boolean hasMinExt = minimumExtent != null;
		minimumExtentBox.setSelected(hasMinExt);
		updateMinExtOptionsAvailable();
		if (hasMinExt) {
			minimumExtentX.setValue(minimumExtent.x);
			minimumExtentY.setValue(minimumExtent.y);
			minimumExtentZ.setValue(minimumExtent.z);
		}

		final Vertex maximumExtent = extents.getMaximumExtent();
		final boolean hasMaxExt = maximumExtent != null;
		maximumExtentBox.setSelected(hasMaxExt);
		updateMaxExtOptionsAvailable();
		if (hasMaxExt) {
			maximumExtentX.setValue(maximumExtent.x);
			maximumExtentY.setValue(maximumExtent.y);
			maximumExtentZ.setValue(maximumExtent.z);
		}

		final boolean hasBoundsRadius = extents.hasBoundsRadius();
		boundsRadiusBox.setSelected(hasBoundsRadius);
		updateBoundRadiusOptionsAvailable();
		if (hasBoundsRadius) {
			boundsRadius.setValue(extents.getBoundsRadius());
		}
	}

	public ExtLog getExtLog() {
		final Vertex minimumExtent, maximumExtent;
		final double boundsRadius;
		if (minimumExtentBox.isSelected()) {
			minimumExtent = new Vertex(val(minimumExtentX), val(minimumExtentY), val(minimumExtentZ));
		} else {
			minimumExtent = null;
		}
		if (maximumExtentBox.isSelected()) {
			maximumExtent = new Vertex(val(maximumExtentX), val(maximumExtentY), val(maximumExtentZ));
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

	private final double val(final JSpinner spinner) {
		return ((Number) spinner.getValue()).doubleValue();
	}
}
