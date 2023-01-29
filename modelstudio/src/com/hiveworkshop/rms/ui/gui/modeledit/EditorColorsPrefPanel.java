package com.hiveworkshop.rms.ui.gui.modeledit;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.util.colorchooser.ColorChooserIconLabel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class EditorColorsPrefPanel extends JPanel {
	private final EditorColorPrefs colorPrefs;

	public EditorColorsPrefPanel() {
		super(new MigLayout("fill, ins 0, gap 0", "[]10[]"));

		colorPrefs = ProgramGlobals.getPrefs().getEditorColorPrefsCopy();

		for (ColorThing thing : ColorThing.values()) {
			add(new JLabel(thing.getTextKeyString()));
			add(new ColorChooserIconLabel(colorPrefs.getColor(thing), color -> colorPrefs.setColor(thing, color)), "wrap");
		}
	}

	public EditorColorPrefs getColorPrefs() {
		return colorPrefs;
	}
}