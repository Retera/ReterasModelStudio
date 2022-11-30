package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;

/**
 * Bones that make geometry animate.
 *
 * Eric Theller 11/10/2011
 */
public class Bone extends IdObject {
	public Bone() {

	}

	public Bone(String name) {
		this.name = name;
	}

	protected Bone(Bone b) {
		super(b);
	}

	@Override
	public Bone copy() {
		return new Bone(this);
	}

	@Override
	public double getClickRadius() {
		return ProgramGlobals.getPrefs().getNodeBoxSize();
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return 1;
	}
}
