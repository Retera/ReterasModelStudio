package com.matrixeater.colorizer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.LabelView;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

public class MaydayColorizer extends JPanel {
	private Color leftColor = Color.RED;
	private Color rightColor = Color.BLACK;
	private final JTextArea inputTextArea;
	private final JEditorPane editorPane;

	public MaydayColorizer() {
		final JLabel label = new JLabel("Enter text:");
		inputTextArea = new JTextArea();
		inputTextArea.setLineWrap(true);
		final JButton leftColorDisplay = new JButton() {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				g.setColor(leftColor);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		final JButton rightColorDisplay = new JButton() {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				g.setColor(rightColor);
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		final JLabel gradientDisplay = new JLabel() {
			@Override
			protected void paintComponent(final Graphics g) {
				super.paintComponent(g);
				((Graphics2D) g).setPaint(new GradientPaint(0, 0, leftColor, getWidth(), 0, rightColor));
				g.fillRect(0, 0, getWidth(), getHeight());
			}
		};
		leftColorDisplay.setMinimumSize(new Dimension(55, 25));
		leftColorDisplay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Color userValue = JColorChooser.showDialog(MaydayColorizer.this, "Choose Color", leftColor);
				if (userValue != null) {
					leftColor = userValue;
					repaint();
				}
			}
		});
		rightColorDisplay.setMinimumSize(new Dimension(55, 25));
		rightColorDisplay.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final Color userValue = JColorChooser.showDialog(MaydayColorizer.this, "Choose Color", rightColor);
				if (userValue != null) {
					rightColor = userValue;
					repaint();
				}
			}
		});
		gradientDisplay.setMinimumSize(new Dimension(500, 25));
		final JButton applyLeftColor = new JButton("Apply Color");
		applyLeftColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				applyColor(leftColor);
			}
		});
		final JButton applyRightColor = new JButton("Apply Color");
		applyRightColor.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				applyColor(rightColor);
			}
		});
		final JButton applyGradient = new JButton("Apply Gradient");
		applyGradient.setMinimumSize(new Dimension(500, 25));
		applyGradient.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				applyColor(leftColor, rightColor);
			}
		});
		final JCheckBox valueLink = new JCheckBox("Value Link");
		valueLink.setEnabled(false);
		final JButton newLine = new JButton("New Line");
		newLine.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				addNewline();
			}
		});
		editorPane = new JEditorPane();
		editorPane.setBackground(new Color(0, 25, 70));
		editorPane.setText("<span style=\"color:white\">");
//		newLine.setMaximumSize(new Dimension(99999, 25));
		applyGradient.setMaximumSize(new Dimension(99999, 25));
		applyLeftColor.setMaximumSize(new Dimension(99999, 25));
		applyRightColor.setMaximumSize(new Dimension(99999, 25));
		applyGradient.setMaximumSize(new Dimension(99999, 25));
		gradientDisplay.setMaximumSize(new Dimension(99999, 25));
		leftColorDisplay.setMaximumSize(new Dimension(99999, 25));
		rightColorDisplay.setMaximumSize(new Dimension(99999, 25));
		final GroupLayout layout = new GroupLayout(this);

		inputTextArea.setMinimumSize(new Dimension(20, 40));
		editorPane.setMinimumSize(new Dimension(20, 40));
		final JScrollPane inputScroll = new JScrollPane(inputTextArea);
		final JScrollPane editorPaneScroll = new JScrollPane(editorPane);
		inputScroll.setMinimumSize(new Dimension(1, 1));
		editorPaneScroll.setMinimumSize(new Dimension(1, 1));
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(label).addComponent(inputScroll)

//				.addGroup(layout.createSequentialGroup()
//						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//								.addComponent(leftColorDisplay).addComponent(applyLeftColor))
//						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER, true)
//								.addComponent(gradientDisplay).addComponent(applyGradient))
//						.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
//								.addComponent(rightColorDisplay).addComponent(applyRightColor))
//						.addComponent(gradientDisplay).addComponent(rightColorDisplay)
//
//				)

//				.addGroup(layout.createSequentialGroup().addComponent(valueLink).addComponent(newLine)

//				)

				.addGroup(layout.createSequentialGroup().addComponent(leftColorDisplay).addComponent(gradientDisplay)
						.addComponent(rightColorDisplay))
				.addGroup(layout.createSequentialGroup().addComponent(applyLeftColor).addComponent(applyGradient)
						.addComponent(applyRightColor)

				).addGroup(layout.createSequentialGroup().addComponent(valueLink).addComponent(newLine)

				)

				.addComponent(editorPaneScroll)

		);
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(label).addComponent(inputScroll)
				.addGroup(layout.createParallelGroup().addComponent(leftColorDisplay).addComponent(gradientDisplay)
						.addComponent(rightColorDisplay))
				.addGroup(layout.createParallelGroup().addComponent(applyLeftColor).addComponent(applyGradient)
						.addComponent(applyRightColor)

				).addGroup(layout.createParallelGroup().addComponent(valueLink).addComponent(newLine)

				)

				.addComponent(editorPaneScroll)

		);
		setLayout(layout);
		editorPane.setContentType("text/html");
		editorPane.setEditorKit(new WrappedHtmlEditorKit());
		setMinimumSize(new Dimension(1, 1));

	}

	private void applyColor(final Color color) {
		int selectionStart = inputTextArea.getSelectionStart();
		int selectionEnd = inputTextArea.getSelectionEnd();
		if (((selectionStart == -1) && (selectionEnd == -1))) {
			// invalid
			selectionStart = 0;
			selectionEnd = inputTextArea.getText().length();
		}
		try {
			final String selectedText = inputTextArea.getText(selectionStart, selectionEnd - selectionStart);
			String hexString = Integer.toHexString(color.getRGB());
			while (hexString.length() < 8) {
				hexString = "0" + hexString;
			}
			final String resultingGeneratedText = inputTextArea.getText(0, selectionStart) + "|c" + hexString
					+ selectedText + "|r"
					+ inputTextArea.getText(selectionEnd, inputTextArea.getText().length() - selectionEnd);
			inputTextArea.setText(resultingGeneratedText);

			final String fixForEditorPane = fixForEditorPane(resultingGeneratedText);
			editorPane.setText(fixForEditorPane);
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	private void addNewline() {
		int selectionStart = inputTextArea.getSelectionStart();
		int selectionEnd = inputTextArea.getSelectionEnd();
		if (((selectionStart == -1) && (selectionEnd == -1))) {
			// invalid
			selectionStart = 0;
			selectionEnd = inputTextArea.getText().length();
		}
		try {
			final String resultingGeneratedText = inputTextArea.getText(0, selectionStart) + "|n"
					+ inputTextArea.getText(selectionEnd, inputTextArea.getText().length() - selectionEnd);
			inputTextArea.setText(resultingGeneratedText);

			final String fixForEditorPane = fixForEditorPane(resultingGeneratedText);
			editorPane.setText(fixForEditorPane);
		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	private static final String HEXADECIMAL = "0123456789ABCDEF";

	private static String fixForEditorPane(final String text) {
		final StringBuilder sb = new StringBuilder("<span style=\"color:white\">");
		for (int i = 0; i < text.length(); i++) {
			final char character = text.charAt(i);
			boolean skip = false;
			if (character == '|') {
				if ((i + 1) <= text.length()) {
					if (text.charAt(i + 1) == 'c') {
						if ((i + 10) <= text.length()) {
							boolean validHexString = true;
							for (int j = i + 2; (j < (i + 10)) && validHexString; j++) {
								if (HEXADECIMAL.indexOf(Character.toUpperCase(text.charAt(j))) == -1) {
									validHexString = false;
								}
							}
							if (validHexString) {
								skip = true;
								sb.append("<span style=\"color:#" + text.substring(i + 4, i + 10)
										+ text.substring(i + 2, i + 4) + "\">");
								i += 9;
							}
						}
					} else if (text.charAt(i + 1) == 'n') {
						i++;
						skip = true;
						sb.append("<br>");
					} else if (text.charAt(i + 1) == 'r') {
						i++;
						skip = true;
						sb.append("</span>");
					}
				}
			}
			if (!skip) {
				sb.append(character);
			}
		}
		return sb.toString();
	}

	private void applyColor(final Color leftColor, final Color rightColor) {
		int selectionStart = inputTextArea.getSelectionStart();
		int selectionEnd = inputTextArea.getSelectionEnd();
		if (((selectionStart == -1) && (selectionEnd == -1))) {
			// invalid
			selectionStart = 0;
			selectionEnd = inputTextArea.getText().length();
		}
		try {
			final StringBuilder resultingText = new StringBuilder();
			final String selectedText = inputTextArea.getText(selectionStart, selectionEnd - selectionStart);
			final int r1 = leftColor.getRed();
			final int g1 = leftColor.getGreen();
			final int b1 = leftColor.getBlue();
			final int a1 = leftColor.getAlpha();
			final int r2 = rightColor.getRed();
			final int g2 = rightColor.getGreen();
			final int b2 = rightColor.getBlue();
			final int a2 = rightColor.getAlpha();
			final int length = Math.max(1, selectedText.length());
			final int drm = r2 - r1;
			final int dr = drm / length;
			final int dgm = g2 - g1;
			final int dg = dgm / length;
			final int dbm = b2 - b1;
			final int db = dbm / length;
			final int dam = a2 - a1;
			final int da = dam / length;
			for (int i = 0; i < length; i++) {
				final char character = selectedText.charAt(i);
				int r = r1 + (dr * i);
				if (Math.abs(r - r1) > Math.abs(drm)) {
					r = r2;
				}
				int g = g1 + (dg * i);
				if (Math.abs(g - g1) > Math.abs(dgm)) {
					g = g2;
				}
				int b = b1 + (db * i);
				if (Math.abs(b - b1) > Math.abs(dbm)) {
					b = b2;
				}
				int a = a1 + (da * i);
				if (Math.abs(a - a1) > Math.abs(dam)) {
					a = a2;
				}
				String hexString = Integer.toHexString(new Color(r, g, b, a).getRGB());
				while (hexString.length() < 8) {
					hexString = "0" + hexString;
				}
				resultingText.append("|c");
				resultingText.append(hexString);
				resultingText.append(character);
				resultingText.append("|r");
			}
			final String resultingGeneratedText = inputTextArea.getText(0, selectionStart) + resultingText.toString()
					+ inputTextArea.getText(selectionEnd, inputTextArea.getText().length() - selectionEnd);
			inputTextArea.setText(resultingGeneratedText);
			final String fixForEditorPane = fixForEditorPane(resultingGeneratedText);
			editorPane.setText(fixForEditorPane);

		} catch (final BadLocationException e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		final JFrame frame = new JFrame("Colorizer");
		frame.setContentPane(new MaydayColorizer());
		frame.setBounds(0, 0, 800, 600);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	public static class WrappedHtmlEditorKit extends HTMLEditorKit {
		private static final long serialVersionUID = 1L;

		private ViewFactory viewFactory = null;

		public WrappedHtmlEditorKit() {
			super();
			this.viewFactory = new WrappedHtmlFactory();
			return;
		}

		@Override
		public ViewFactory getViewFactory() {
			return this.viewFactory;
		}

		private static class WrappedHtmlFactory extends HTMLEditorKit.HTMLFactory {
			@Override
			public View create(final Element elem) {
				final View v = super.create(elem);

				if (v instanceof LabelView) {
					final Object o = elem.getAttributes().getAttribute(StyleConstants.NameAttribute);

					if ((o instanceof HTML.Tag) && (o == HTML.Tag.BR)) {
						return v;
					}

					return new WrapLabelView(elem);
				}

				return v;
			}

			private static class WrapLabelView extends LabelView {
				public WrapLabelView(final Element elem) {
					super(elem);
					return;
				}

				@Override
				public float getMinimumSpan(final int axis) {
					switch (axis) {
					case View.X_AXIS: {
						return 0;
					}
					case View.Y_AXIS: {
						return super.getMinimumSpan(axis);
					}
					default: {
						throw new IllegalArgumentException("Invalid axis: " + axis);
					}
					}
				}
			}
		}
	}
}
