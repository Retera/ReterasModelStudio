package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import java.util.Collection;

public interface SelectionManager<T> extends SelectionManagerView<T> {
	void setSelection(Collection<? extends T> selection);

	void addSelection(Collection<? extends T> selection);

	void removeSelection(Collection<? extends T> selection);

	void addSelectionListener(SelectionListener listener);

	void removeSelectionListener(SelectionListener listener);
}
