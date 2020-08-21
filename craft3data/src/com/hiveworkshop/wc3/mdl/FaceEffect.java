package com.hiveworkshop.wc3.mdl;

import com.etheller.warsmash.parsers.mdlx.MdlxFaceEffect;

public class FaceEffect {
	public String faceEffectTarget = "";
	public String faceEffect = "";

	public FaceEffect() {

	}

	public FaceEffect(String faceEffectTarget, String faceEffect) {
		this.faceEffectTarget = faceEffectTarget;
		this.faceEffect = faceEffect;
	}

	public FaceEffect(final FaceEffect effect) {
		this.faceEffectTarget = effect.faceEffectTarget;
		this.faceEffect = effect.faceEffect;
	}

	public FaceEffect(final MdlxFaceEffect effect) {
		faceEffectTarget = effect.type;
		faceEffect = effect.path;
	}

	public MdlxFaceEffect toMdlx() {
		final MdlxFaceEffect effect = new MdlxFaceEffect();

		effect.type = faceEffectTarget;
		effect.path = faceEffect;

		return effect;
	}

	public FaceEffect copy() {
		return new FaceEffect(this);
	}
}
