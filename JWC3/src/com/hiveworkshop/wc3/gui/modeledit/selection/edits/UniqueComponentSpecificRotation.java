package com.hiveworkshop.wc3.gui.modeledit.selection.edits;

import java.util.HashSet;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.selection.MutableSelectionComponent;
import com.hiveworkshop.wc3.util.Callback;

public final class UniqueComponentSpecificRotation implements Callback<MutableSelectionComponent> {
	private final Set<MutableSelectionComponent> processedComponents = new HashSet<>();

	private float centerX;

	private float centerY;

	private float centerZ;

	private float radians;

	private byte firstXYZ;

	private byte secondXYZ;

	@Override
	public void run(final MutableSelectionComponent item) {
		if (!processedComponents.contains(item)) {
			item.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
			processedComponents.add(item);
		}
	}

	public UniqueComponentSpecificRotation resetValues(final float centerX, final float centerY, final float centerZ,
			final float radians, final byte firstXYZ, final byte secondXYZ) {
		this.centerX = centerX;
		this.centerY = centerY;
		this.centerZ = centerZ;
		this.radians = radians;
		this.firstXYZ = firstXYZ;
		this.secondXYZ = secondXYZ;
		return this;
	}

	public void reset() {
		processedComponents.clear();
	}

	public UniqueComponentSpecificRotation reverse() {
		return new UniqueComponentSpecificRotation().resetValues(centerX, centerY, centerZ, -radians, firstXYZ,
				secondXYZ);
	}
}