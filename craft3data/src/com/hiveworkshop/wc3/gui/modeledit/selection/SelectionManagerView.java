package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.util.Set;

public interface SelectionManagerView<T> extends SelectionView {
	Set<T> getSelection();
}
