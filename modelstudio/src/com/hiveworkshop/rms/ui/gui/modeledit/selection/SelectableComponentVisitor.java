package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.IdObject;

public interface SelectableComponentVisitor {
	void accept(Geoset geoset);

	void accept(IdObject node);

	void accept(Camera camera);
}
