package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Named;

class VisibilityShell {
	Named source;
	EditableModel model;

	public VisibilityShell(final Named n, final EditableModel whichModel) {
		source = n;
		model = whichModel;
	}
}
