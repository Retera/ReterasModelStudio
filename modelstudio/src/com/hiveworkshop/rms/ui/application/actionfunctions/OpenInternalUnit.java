package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalUnit extends ActionFunction {
	public OpenInternalUnit(){
		super(TextKey.UNIT, OpenInternalUnit::fetchUnit, "control U");
	}

	public static void fetchUnit() {
		GameObject unitFetched = UnitOptionPanel.getGameObject(ProgramGlobals.getMainPanel());
		if (unitFetched != null) {
			ImageIcon icon = unitFetched.getScaledIcon(16);

			InternalFileLoader.loadFromStream(unitFetched.getField("file"), icon, true);
		}
	}
}
