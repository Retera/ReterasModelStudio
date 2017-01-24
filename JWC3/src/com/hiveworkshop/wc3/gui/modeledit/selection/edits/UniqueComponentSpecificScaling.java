package com.hiveworkshop.wc3.gui.modeledit.selection.edits;

import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.selection.MutableSelectionComponent;
import com.hiveworkshop.wc3.util.Callback;

public final class UniqueComponentSpecificScaling implements Callback<MutableSelectionComponent> {
	private final Set<MutableSelectionComponent> processedComponents = new HashSet<>();

	private float dx, dy, dz;

	private float centerX;

	private float centerY;

	private float centerZ;

	@Override
	public void run(final MutableSelectionComponent item) {
		if (!processedComponents.contains(item)) {
			item.scale(centerX, centerY, centerZ, dx, dy, dz);
			processedComponents.add(item);
		}
	}

	public UniqueComponentSpecificScaling resetValues(final float centerX, final float centerY, final float centerZ,
			final float dx, final float dy, final float dz) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.dx = dx;
		this.dy = dy;
		this.dz = dz;
		return this;
	}

	public void reset() {
		processedComponents.clear();
	}
}