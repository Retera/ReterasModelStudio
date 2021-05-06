package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;

public class ProgramGlobals {
	public static SaveProfile profile = SaveProfile.get();
	public static ProgramPreferences prefs = profile.getPreferences();
//	public static List<ModelPanel> modelPanels;
//	public static ModelPanel currentModelPanel;
}
