package com.hiveworkshop.wc3.gui.modeledit.selection.edits;

import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.selection.MutableSelectionComponent;
import com.hiveworkshop.wc3.util.Callback;

public final class UniqueComponentTranslation implements Callback<MutableSelectionComponent> {
	private final Set<MutableSelectionComponent> processedComponents = new HashSet<>();
	private final float dx, dy, dz;

	public UniqueComponentTranslation(final float dx, final float dy, final float dz) {
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
	}

	@Override
	public void run(final MutableSelectionComponent item) {
		if (!processedComponents.contains(item)) {
			item.translate((dx), (dy), (dz));
			processedComponents.add(item);
		}
	}

	public void reset() {
		processedComponents.clear();
	}
}