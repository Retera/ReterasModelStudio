package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportFromObjectEditor extends ActionFunction{
	public ImportFromObjectEditor(){
		super(TextKey.IMPORT_FROM_OBJECT_EDITOR, () -> ImportFileActions.importGameObjectActionRes());
	}
}
