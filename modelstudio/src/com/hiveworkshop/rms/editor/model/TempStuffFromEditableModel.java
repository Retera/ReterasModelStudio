package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;

public class TempStuffFromEditableModel {
	public static EditableModel deepClone(final EditableModel what, final String newName) {
		// Need to do a real save, because of strings being passed by reference.
		// Maybe other objects I didn't think about (or the code does by mistake).
		final EditableModel newModel = TempOpenModelStuff.createEditableModel(new MdlxModel(TempSaveModelStuff.toMdlx(what).saveMdx()));

		newModel.setName(newName);
		newModel.setFileRef(what.getFile());

		return newModel;
	}

}
