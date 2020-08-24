package com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener;

import java.util.Collection;
import java.util.Map;

import com.hiveworkshop.rms.editor.model.IdObject;

public interface ClonedNodeNamePicker {
	Map<IdObject, String> pickNames(Collection<IdObject> clonedNodes);
}
