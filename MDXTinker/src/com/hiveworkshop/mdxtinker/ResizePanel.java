package com.hiveworkshop.mdxtinker;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;

import com.hiveworkshop.mdxtinker.util.MyDoubleFilter;

public class ResizePanel extends JPanel {
	private static final BasicStroke SIZE_2_STROKE = new BasicStroke(2);
	private final DimensionValueChooser xChooser, yChooser, zChooser;
	private final JRadioButton useOrigin, useCenterOfMass;

	private final JButton lockDimensions;
	private boolean dimensionsLinked = true;

	public ResizePanel() {
		xChooser = new DimensionValueChooser("X");
		xChooser.addDimensionChangeListener(new DimensionLinker(xChooser));
		yChooser = new DimensionValueChooser("Y");
		yChooser.addDimensionChangeListener(new DimensionLinker(yChooser));
		zChooser = new DimensionValueChooser("Z");
		zChooser.addDimensionChangeListener(new DimensionLinker(zChooser));

		final ButtonGroup flipCenterGroup = new ButtonGroup();
		useOrigin = new JRadioButton("Use Origin", true);
		useCenterOfMass = new JRadioButton("Use Center of Mass");
		flipCenterGroup.add(useCenterOfMass);
		flipCenterGroup.add(useOrigin);

		try {
			lockDimensions = new JButton(
					new ImageIcon(ImageIO.read(ResizePanel.class.getResource("img/UI_LinkIcon.png"))
							.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		lockDimensions.setMargin(new Insets(0, 0, 0, 0));
		lockDimensions.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				dimensionsLinked = !dimensionsLinked;
				if (dimensionsLinked) {
					dimensionLinkerIsUpdating = true;
					yChooser.setValue(xChooser.getValue());
					zChooser.setValue(xChooser.getValue());
					dimensionLinkerIsUpdating = false;
				}
				repaint();
			}
		});

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(xChooser).addComponent(yChooser)
						.addComponent(zChooser).addComponent(useOrigin).addComponent(useCenterOfMass))
				.addGap(32).addComponent(lockDimensions));
		layout.setVerticalGroup(
				layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
								.addGroup(layout.createSequentialGroup().addComponent(xChooser).addGap(10)
										.addComponent(yChooser).addGap(10).addComponent(zChooser))
								.addComponent(lockDimensions))

						.addGap(16).addComponent(useOrigin).addComponent(useCenterOfMass));

		setLayout(layout);
	}

	private boolean dimensionLinkerIsUpdating = false;

	private final class DimensionLinker implements DimensionChangeListener {
		private final DimensionValueChooser chooser;

		public DimensionLinker(final DimensionValueChooser chooser) {
			this.chooser = chooser;
		}

		@Override
		public void dimensionChanged() {
			if (!dimensionLinkerIsUpdating) {
				dimensionLinkerIsUpdating = true;
				if (dimensionsLinked) {
					if (xChooser != chooser) {
						xChooser.setValue(chooser.getValue());
					}
					if (yChooser != chooser) {
						yChooser.setValue(chooser.getValue());
					}
					if (zChooser != chooser) {
						zChooser.setValue(chooser.getValue());
					}
				}
				dimensionLinkerIsUpdating = false;
			}
		}

	}

	public double getXValue() {
		return xChooser.getValue();
	}

	public double getYValue() {
		return yChooser.getValue();
	}

	public double getZValue() {
		return zChooser.getValue();
	}

	public CenterOfManipulation getCenterOfManipulation() {
		if (useOrigin.isSelected()) {
			return CenterOfManipulation.ORIGIN;
		} else {
			return CenterOfManipulation.CENTER_OF_MASS;
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.LIGHT_GRAY);
		final int xChooserYCenter = xChooser.getY() + xChooser.getHeight() / 2;
		final int yChooserYCenter = yChooser.getY() + yChooser.getHeight() / 2;
		final int zChooserYCenter = zChooser.getY() + zChooser.getHeight() / 2;
		final int chooserEndingX = xChooser.getX() + xChooser.getWidth();
		final int lineStartX = chooserEndingX + 4;
		final int lineEndX = chooserEndingX + 16;
		final Stroke oldStroke = ((Graphics2D) g).getStroke();
		((Graphics2D) g).setStroke(SIZE_2_STROKE);
		g.drawLine(lineStartX, xChooserYCenter, lineEndX, xChooserYCenter);
		g.drawLine(lineStartX, yChooserYCenter, lineEndX, yChooserYCenter);
		g.drawLine(lineStartX, zChooserYCenter, lineEndX, zChooserYCenter);
		if (dimensionsLinked) {
			g.drawLine(lineEndX, xChooserYCenter, lineEndX, zChooserYCenter);
			g.drawLine(lineEndX, yChooserYCenter, lockDimensions.getX(), yChooserYCenter);
		} else {
			g.drawLine(lineEndX, xChooserYCenter, lineEndX, yChooserYCenter - 16);
			g.drawLine(lineEndX, zChooserYCenter, lineEndX, yChooserYCenter + 16);
		}

		((Graphics2D) g).setStroke(oldStroke);
	}

	private static final class DimensionValueChooser extends JPanel {
		private final JLabel label;
		private final JTextField textField;

		public DimensionValueChooser(final String dimensionName) {
			label = new JLabel(dimensionName + ":");
			label.setFont(new Font("Arial", Font.BOLD, 16));
			textField = new JTextField("1.00", 6);
			final PlainDocument document = (PlainDocument) textField.getDocument();
			document.setDocumentFilter(new MyDoubleFilter());

			final GroupLayout layout = new GroupLayout(this);
			layout.setHorizontalGroup(
					layout.createSequentialGroup().addComponent(label).addGap(8).addComponent(textField));
			layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(label)
					.addComponent(textField));
			setLayout(layout);
		}

		public double getValue() {
			final String numberText = textField.getText();
			if ("".equals(numberText)) {
				return 0;
			}
			final double value = Double.parseDouble(numberText);
			return value;
		}

		public void setValue(final double value) {
			textField.setText(Double.toString(value));
		}

		public void addDimensionChangeListener(final DimensionChangeListener listener) {
			textField.getDocument().addDocumentListener(new DocumentListener() {
				@Override
				public void changedUpdate(final DocumentEvent e) {
					listener.dimensionChanged();
				}

				@Override
				public void removeUpdate(final DocumentEvent e) {
					listener.dimensionChanged();
				}

				@Override
				public void insertUpdate(final DocumentEvent e) {
					listener.dimensionChanged();
				}
			});
		}
	}

	public interface DimensionChangeListener {
		void dimensionChanged();
	}
}
