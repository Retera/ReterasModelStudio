package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.InternalFileLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPane;
import com.hiveworkshop.rms.ui.language.TextKey;

import javax.swing.*;

public class OpenInternalUnit extends ActionFunction {
	public OpenInternalUnit(){
		super(TextKey.UNIT, () -> fetchUnit(), "control U");
	}



	public static void fetchUnit() {
//		GameObject unitFetched = ImportFileActions.fetchUnit();
		GameObject unitFetched = UnitOptionPane.fetchUnit(ProgramGlobals.getMainPanel());;
		if (unitFetched != null) {

			String filepath = ImportFileActions.convertPathToMDX(unitFetched.getField("file"));
			ImageIcon icon = unitFetched.getScaledIcon(16);

			InternalFileLoader.loadFromStream(filepath, icon);
		}
	}
}
