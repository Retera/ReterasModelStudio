package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import java.util.Set;

public interface SelectionManagerView<T> extends SelectionView {
	Set<T> getSelection();
}
