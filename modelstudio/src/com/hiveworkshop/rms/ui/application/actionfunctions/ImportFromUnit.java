package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.parsers.slk.GameObject;
import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.browsers.unit.UnitOptionPanel;
import com.hiveworkshop.rms.ui.language.TextKey;

import java.awt.*;

public class ImportFromUnit extends ActionFunction{
	public ImportFromUnit(){
		super(TextKey.IMPORT_FROM_UNIT, () -> importUnitActionRes(), "control shift U");
	}

	public static void importUnitActionRes(){
		ImportFileActions.importMdxObject(fetchUnitPath(ProgramGlobals.getMainPanel()));
	}


	public static String fetchUnitPath(Component component) {
		GameObject choice = UnitOptionPanel.getGameObject(component);
		if (choice != null) {
			return choice.getField("file");
		}
		return null;
	}
}
