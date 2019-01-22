package com.hiveworkshop.wc3.gui.modeledit.selection;

import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.IdObject;

public interface SelectableComponentVisitor {
	void accept(Geoset geoset);

	void accept(IdObject node);

	void accept(Camera camera);
}
