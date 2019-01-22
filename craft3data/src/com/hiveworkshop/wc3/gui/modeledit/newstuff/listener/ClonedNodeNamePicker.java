package com.hiveworkshop.wc3.gui.modeledit.newstuff.listener;

import java.util.Collection;
import java.util.Map;

import com.hiveworkshop.wc3.mdl.IdObject;

public interface ClonedNodeNamePicker {
	Map<IdObject, String> pickNames(Collection<IdObject> clonedNodes);
}
