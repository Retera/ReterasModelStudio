package com.hiveworkshop.rms.util.sound;

import com.hiveworkshop.rms.util.Vec4;

public abstract class SplatTarget extends EventTarget {

	public abstract int getBlendMode();
	public abstract int getScale();
	public abstract float getLifespan();
	public abstract float getPauseTime();
	public abstract float getDecay();
	public abstract Vec4 getStartRGBA();
	public abstract Vec4 getMiddleRGBA();
	public abstract Vec4 getEndRGBA();
	public abstract String getSoundTag();
	public abstract int getVersion();
}
