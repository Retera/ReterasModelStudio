package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;

import java.util.ArrayList;
import java.util.List;

public class ProgramGlobals {
	private static SaveProfile profile;
	private static ProgramPreferences prefs;
	private static List<ModelPanel> modelPanels;
	private static ModelPanel currentModelPanel;

	static {
		profile = SaveProfile.get();
		prefs = profile.getPreferences();
		modelPanels = new ArrayList<>();
	}

	public static ModelPanel getCurrentModelPanel(){
		return currentModelPanel;
	}

	public static void setCurrentModelPanel(ModelPanel currentModelPanel) {
		ProgramGlobals.currentModelPanel = currentModelPanel;
	}

	public static List<ModelPanel> getModelPanels() {
		return modelPanels;
	}

	public static void addModelPanel(ModelPanel modelPanel){
		modelPanels.add(modelPanel);
	}

	public static SaveProfile getProfile() {
		return profile;
	}

	public static ProgramPreferences getPrefs() {
		return prefs;
	}
}
