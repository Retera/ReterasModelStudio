package com.hiveworkshop.rms.ui.preferences.panels;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.GUITheme;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserIconLabel;
import com.hiveworkshop.rms.util.ThemeLoadingUtils;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ColorPrefPanel extends JPanel {

	public ColorPrefPanel(JFrame frame, ProgramPreferences pref){
		super(new MigLayout("gap 0, ins 0, fill"));
		EditorColorPrefs colorPrefs = pref.getEditorColorPrefs();
		final JPanel innerPanel = new JPanel(new MigLayout("gap 0"));

		innerPanel.add(new JLabel("Window Borders (Theme):"));
		ThemeTracker themeTracker = new ThemeTracker(frame, this, pref);
		final TwiComboBox<GUITheme> themeCheckBox2 = new TwiComboBox<>(GUITheme.values(), GUITheme.DARK_BLUE_GREEN)
				.selectOrFirst(pref.getTheme())
				.addOnSelectItemListener(themeTracker::themeChanged);
		innerPanel.add(themeCheckBox2, "wrap");

		EditorColorsPrefPanel colorsPrefPanel = new EditorColorsPrefPanel(colorPrefs);
		innerPanel.add(colorsPrefPanel, "wrap");

		JScrollPane scrollPane = new JScrollPane(innerPanel);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setBorder(null);
		add(scrollPane, "growx, growy");
	}

	public void addAtRow(JPanel modelEditorPanel, ColorChooserIconLabel colorIcon, String s) {
		modelEditorPanel.add(new JLabel(s));
		modelEditorPanel.add(colorIcon, "wrap");
	}

	private static class ThemeTracker {
		boolean hasWarned = false;
		JFrame frame;
		JComponent parent;
		ProgramPreferences pref;

		ThemeTracker(JFrame frame, JComponent parent, ProgramPreferences pref){
			this.parent = parent;
			this.pref = pref;
			this.frame = frame;
		}

		public void themeChanged(GUITheme selectedItem) {
			pref.setTheme(selectedItem);
			if (selectedItem != null && parent.getRootPane() != null) {
				System.out.println("setting theme");
				ThemeLoadingUtils.setTheme(selectedItem);
				SwingUtilities.updateComponentTreeUI(frame);
				SwingUtilities.updateComponentTreeUI(ProgramGlobals.getMainPanel().getRootPane());
//				SwingUtilities.updateComponentTreeUI(parent.getRootPane());
			}

			if (!hasWarned) {
				hasWarned = true;
				JOptionPane.showMessageDialog(parent,
						"Some settings may not take effect until you restart the application.", "Warning",
						JOptionPane.WARNING_MESSAGE);
			}
		}
	}
}
