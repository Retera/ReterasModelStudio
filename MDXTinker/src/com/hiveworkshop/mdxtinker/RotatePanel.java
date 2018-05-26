package com.hiveworkshop.mdxtinker;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.hiveworkshop.mdxtinker.util.MyIntFilter;

public class RotatePanel extends JPanel {
	private final DimensionRotationChooser xChooser, yChooser, zChooser;
	private final JRadioButton useOrigin, useCenterOfMass;

	public RotatePanel() {
		xChooser = new DimensionRotationChooser("X");
		yChooser = new DimensionRotationChooser("Y");
		zChooser = new DimensionRotationChooser("Z");

		final ButtonGroup flipCenterGroup = new ButtonGroup();
		useOrigin = new JRadioButton("Use Origin", true);
		useCenterOfMass = new JRadioButton("Use Center of Mass");
		flipCenterGroup.add(useCenterOfMass);
		flipCenterGroup.add(useOrigin);

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(xChooser).addComponent(yChooser)
				.addComponent(zChooser).addComponent(useOrigin).addComponent(useCenterOfMass));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(xChooser).addGap(10).addComponent(yChooser)
				.addGap(10).addComponent(zChooser).addGap(16).addComponent(useOrigin).addComponent(useCenterOfMass));

		setLayout(layout);
	}

	public int getXValue() {
		return xChooser.getValue();
	}

	public int getYValue() {
		return yChooser.getValue();
	}

	public int getZValue() {
		return zChooser.getValue();
	}

	public CenterOfManipulation getCenterOfManipulation() {
		if (useOrigin.isSelected()) {
			return CenterOfManipulation.ORIGIN;
		} else {
			return CenterOfManipulation.CENTER_OF_MASS;
		}
	}

	private static final class DimensionRotationChooser extends JPanel {
		private final JLabel label;
		private final JTextField textField;
		private final JButton leftArrow;
		private final JButton rightArrow;

		public DimensionRotationChooser(final String dimensionName) {
			label = new JLabel(dimensionName + ":");
			label.setFont(new Font("Arial", Font.BOLD, 16));
			textField = new JTextFieldWithDegreeSymbol("0", 6);
			final PlainDocument document = (PlainDocument) textField.getDocument();
			document.setDocumentFilter(new MyIntFilter());
			leftArrow = new JButton(new ImageIcon(RotatePanel.class.getResource("img/leftArrow.png")));
			leftArrow.setMargin(new Insets(0, 0, 0, 0));
			rightArrow = new JButton(new ImageIcon(RotatePanel.class.getResource("img/rightArrow.png")));
			rightArrow.setMargin(new Insets(0, 0, 0, 0));
			leftArrow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					textField.setText(Integer.toString((getValue() - 90) % 360));
				}
			});
			rightArrow.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					textField.setText(Integer.toString((getValue() + 90) % 360));
				}
			});

			final GroupLayout layout = new GroupLayout(this);
			layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(label).addGap(8)
					.addComponent(textField).addGap(12).addComponent(leftArrow).addComponent(rightArrow));
			layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(label)
					.addComponent(textField).addComponent(leftArrow).addComponent(rightArrow));
			setLayout(layout);
		}

		public int getValue() {
			final String numberText = textField.getText();
			final int rotationAmount = Integer.parseInt(numberText);
			return rotationAmount;
		}
	}

	private static final class JTextFieldWithDegreeSymbol extends JTextField {
		public JTextFieldWithDegreeSymbol() {
			super();
		}

		public JTextFieldWithDegreeSymbol(final Document doc, final String text, final int columns) {
			super(doc, text, columns);
		}

		public JTextFieldWithDegreeSymbol(final int columns) {
			super(columns);
		}

		public JTextFieldWithDegreeSymbol(final String text, final int columns) {
			super(text, columns);
		}

		public JTextFieldWithDegreeSymbol(final String text) {
			super(text);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);

			g.drawOval(getWidth() - 6, 2, 4, 4);
		}
	}

}
