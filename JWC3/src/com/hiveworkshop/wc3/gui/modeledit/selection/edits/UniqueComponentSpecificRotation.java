package com.hiveworkshop.wc3.gui.modeledit.selection.edits;

import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.selection.MutableSelectionComponent;
import com.hiveworkshop.wc3.util.Callback;

public final class UniqueComponentSpecificRotation implements Callback<MutableSelectionComponent> {
	private final Set<MutableSelectionComponent> processedComponents = new HashSet<>();

	private float centerX;

	private float centerY;

	private float centerZ;

	private float radians;

	private Pair<Byte, Byte> axes;

	@Override
	public void run(final MutableSelectionComponent item) {
		if (!processedComponents.contains(item)) {
			item.rotate(centerX, centerY, centerZ, radians, coordinateSystem);
			processedComponents.add(item);
		}
	}

	public UniqueComponentSpecificRotation resetValues(final float centerX, final float centerY, final float centerZ,
			final float radians, final CoordinateSystem coordinateSystem) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.radians = radians;
		this.coordinateSystem = coordinateSystem;
		return this;
	}

	public void reset() {
		processedComponents.clear();
	}
}