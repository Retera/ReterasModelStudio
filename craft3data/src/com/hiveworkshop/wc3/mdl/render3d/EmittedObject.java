package com.hiveworkshop.wc3.mdl.render3d;

public abstract class EmittedObject<EMITTER_VIEW extends EmitterView> {
	public float health;

	public EMITTER_VIEW emitterView;

	public float[] vertices;

	public float lta, lba, rta, rba, rgb;

	public abstract void reset(EMITTER_VIEW emitterView, boolean flag);

	public abstract void update();
}
