package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.editor.model.util.TempSaveModelStuff;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;

public class CloneEditableModel {
	public static EditableModel deepClone(final EditableModel model, final String newName) {
		// Need to do a real save, because of strings being passed by reference.
		// Maybe other objects I didn't think about (or the code does by mistake).

		MdlxModel mdlxModel = TempSaveModelStuff.toMdlx(model);
		final EditableModel newModel = TempOpenModelStuff.createEditableModel(mdlxModel);

		newModel.setName(newName);
		newModel.setFileRef(model.getFile());

		return newModel;
	}

}
