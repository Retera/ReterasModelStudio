package com.hiveworkshop.rms.ui.application.actionfunctions;

import com.hiveworkshop.rms.ui.application.ImportFileActions;
import com.hiveworkshop.rms.ui.language.TextKey;

public class ImportWC3Model extends ActionFunction{
	public ImportWC3Model(){
		super(TextKey.IMPORT_FROM_WC3_MODEL, () -> ImportFileActions.importGameModelActionRes());
	}
}
