package com.hiveworkshop.mdxtinker;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.hiveworkshop.wc3.gui.util.ColorChooserIcon;
import com.hiveworkshop.wc3.gui.util.ColorChooserIcon.ColorListener;

public class PreferencesPanel extends JPanel {
	private final JLabel backgroundColorLabel;
	private final ColorChooserIcon backgroundChooserIcon;
	private final JButton defaultBackgroundButton;

	private final JLabel uiColorSchemeLabel;
	private final JRadioButton colorSchemeDarkButton;
	private final JRadioButton colorSchemeBrightButton;

	public PreferencesPanel(final ThemeChangeListener themeChangeListener, final ColorListener colorListener,
			final Theme theme, final Color viewportBackground) {
		final Dimension defaultButtonSize = new Dimension(80, 20);
		backgroundColorLabel = new JLabel("Background Color:");
		backgroundChooserIcon = new ColorChooserIcon(viewportBackground, colorListener);
		defaultBackgroundButton = new JButton("Default");
		defaultBackgroundButton.setMinimumSize(defaultButtonSize);
		defaultBackgroundButton.setPreferredSize(defaultButtonSize);
		defaultBackgroundButton.setMaximumSize(defaultButtonSize);
		defaultBackgroundButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				backgroundChooserIcon.setCurrentColor(new Color(80, 80, 80));
			}
		});

		// Color scheme
		uiColorSchemeLabel = new JLabel("UI Color Scheme");
		colorSchemeDarkButton = new JRadioButton("Dark");
		colorSchemeDarkButton.setSelected(theme == Theme.DARK);
		colorSchemeDarkButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				themeChangeListener.themeChanged(Theme.DARK);
			}
		});
		colorSchemeBrightButton = new JRadioButton("Bright");
		colorSchemeBrightButton.setSelected(theme == Theme.LIGHT);
		colorSchemeBrightButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				themeChangeListener.themeChanged(Theme.LIGHT);
			}
		});
		final ButtonGroup colorSchemeGroup = new ButtonGroup();
		colorSchemeGroup.add(colorSchemeDarkButton);
		colorSchemeGroup.add(colorSchemeBrightButton);

		final GroupLayout layout = new GroupLayout(this);

		layout.setHorizontalGroup(layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup().addComponent(backgroundColorLabel).addGap(8)
						.addComponent(backgroundChooserIcon).addGap(24).addComponent(defaultBackgroundButton))
				.addGroup(layout.createSequentialGroup().addComponent(uiColorSchemeLabel).addGap(4)
						.addGroup(layout.createParallelGroup().addComponent(colorSchemeDarkButton)
								.addComponent(colorSchemeBrightButton))));
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(backgroundColorLabel)
						.addComponent(backgroundChooserIcon).addComponent(defaultBackgroundButton))
				.addGap(16)
				.addGroup(layout.createParallelGroup().addComponent(uiColorSchemeLabel)
						.addGroup(layout.createSequentialGroup().addComponent(colorSchemeDarkButton)
								.addComponent(colorSchemeBrightButton))));
		setLayout(layout);
	}

	public interface ThemeChangeListener {
		void themeChanged(Theme theme);
	}

	public static enum Theme {
		DARK, LIGHT
	};
}
