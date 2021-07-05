package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.util.sound.SoundMappings;

import java.util.ArrayList;
import java.util.List;

public class ProgramGlobals {
	private static final SaveProfile profile;
	private static final ProgramPreferences prefs;
	private static final List<ModelPanel> modelPanels;
	private static ModelPanel currentModelPanel;
	private static final MainPanel mainPanel;
	private static final UndoHandler undoHandler;
	private static final SoundMappings soundMappings;

	static {
		profile = SaveProfile.get();
		prefs = profile.getPreferences();
		modelPanels = new ArrayList<>();
		undoHandler = new UndoHandler();
		mainPanel = new MainPanel();
		soundMappings = new SoundMappings();
	}

	public static MainPanel getMainPanel() {
		return mainPanel;
	}

	public static UndoHandler getUndoHandler() {
		return undoHandler;
	}

	public static ModelPanel getCurrentModelPanel() {
		return currentModelPanel;
	}

	public static void setCurrentModelPanel(ModelPanel currentModelPanel) {
		ProgramGlobals.currentModelPanel = currentModelPanel;
	}

	public static List<ModelPanel> getModelPanels() {
		return modelPanels;
	}

	public static void removeModelPanel(ModelPanel modelPanel) {
		if (currentModelPanel == modelPanel) {
			currentModelPanel = null;
		}
		modelPanels.remove(modelPanel);
	}

	public static void addModelPanel(ModelPanel modelPanel) {
		modelPanels.add(modelPanel);
	}

	public static SaveProfile getProfile() {
		return profile;
	}

	public static ProgramPreferences getPrefs() {
		return prefs;
	}

	public static SoundMappings getSoundMappings() {
		return soundMappings;
	}
}
