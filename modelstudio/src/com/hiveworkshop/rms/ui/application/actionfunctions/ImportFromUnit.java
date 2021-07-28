package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportFromUnit extends ActionFunction{
	public ImportFromUnit(){
		super(TextKey.IMPORT_FROM_UNIT, () -> ImportFileActions.importUnitActionRes(), "control shift U");
	}
}
