package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.util.Vec3;

public abstract class EmittedObject<EMITTER_VIEW extends EmitterView> {
	public float health;

	public EMITTER_VIEW emitterView;

	public Vec3[] verticesV;

	public float lta, lba, rta, rba, rgb;

	public abstract void reset(EMITTER_VIEW emitterView, boolean flag);

	public abstract void update();
}
