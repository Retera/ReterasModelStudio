package com.hiveworkshop.wc3.gui.modeledit.selection;

import java.util.Collection;

public interface SelectionManager<T> extends SelectionManagerView<T> {
	void setSelection(Collection<T> selection);

	void addSelection(Collection<T> selection);

	void removeSelection(Collection<T> selection);

	void addSelectionListener(SelectionListener listener);
}
