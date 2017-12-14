package com.hiveworkshop.wc3.gui.modeledit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;

import com.hiveworkshop.wc3.gui.ProgramPreferences;

public final class ProgramPreferencesPanel extends JTabbedPane {
	private final ProgramPreferences programPreferences;

	public ProgramPreferencesPanel(final ProgramPreferences programPreferences) {
		this.programPreferences = programPreferences;

		final JPanel generalPrefsPanel = new JPanel();
		final JLabel viewModeLabel = new JLabel("3D View Mode");
		final JRadioButton wireframeViewMode = new JRadioButton("Wireframe");
		final JRadioButton solidViewMode = new JRadioButton("Solid");
		final ActionListener viewModeUpdater = new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				programPreferences.setViewMode(wireframeViewMode.isSelected() ? 0 : 1);
			}
		};
		wireframeViewMode.setSelected(programPreferences.viewMode() == 0);
		wireframeViewMode.addActionListener(viewModeUpdater);
		solidViewMode.setSelected(programPreferences.viewMode() == 1);
		solidViewMode.addActionListener(viewModeUpdater);
		final ButtonGroup viewModes = new ButtonGroup();
		viewModes.add(wireframeViewMode);
		viewModes.add(solidViewMode);

		generalPrefsPanel.add(viewModeLabel);
		generalPrefsPanel.add(wireframeViewMode);
		generalPrefsPanel.add(solidViewMode);
		final BoxLayout boxLayout = new BoxLayout(generalPrefsPanel, BoxLayout.PAGE_AXIS);
		generalPrefsPanel.setLayout(boxLayout);

		addTab("General", generalPrefsPanel);

		final JPanel modelEditorPanel = new JPanel();
		addTab("Editor", modelEditorPanel);
	}
}
