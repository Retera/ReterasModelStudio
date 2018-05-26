package com.hiveworkshop.mdxtinker;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class FlipPanel extends JPanel {
	private final JCheckBox xDimension, yDimension, zDimension;
	private final JRadioButton useOrigin, useCenterOfMass;

	public FlipPanel() {
		xDimension = new JCheckBox("X Dimension");
		yDimension = new JCheckBox("Y Dimension");
		zDimension = new JCheckBox("Z Dimension");

		final ButtonGroup flipCenterGroup = new ButtonGroup();
		useOrigin = new JRadioButton("Use Origin", true);
		useCenterOfMass = new JRadioButton("Use Center of Mass");
		flipCenterGroup.add(useCenterOfMass);
		flipCenterGroup.add(useOrigin);

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(xDimension).addComponent(yDimension)
				.addComponent(zDimension).addComponent(useOrigin).addComponent(useCenterOfMass)

		);
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(xDimension).addComponent(yDimension)
				.addComponent(zDimension).addGap(16).addComponent(useOrigin).addComponent(useCenterOfMass).addGap(24));

		setLayout(layout);
	}

	public boolean isFlipXSelected() {
		return xDimension.isSelected();
	}

	public boolean isFlipYSelected() {
		return yDimension.isSelected();
	}

	public boolean isFlipZSelected() {
		return zDimension.isSelected();
	}

	public CenterOfManipulation getCenterOfManipulation() {
		if (useOrigin.isSelected()) {
			return CenterOfManipulation.ORIGIN;
		} else {
			return CenterOfManipulation.CENTER_OF_MASS;
		}
	}
}
