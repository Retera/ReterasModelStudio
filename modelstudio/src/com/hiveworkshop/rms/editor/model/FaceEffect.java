package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxFaceEffect;

public class FaceEffect {
	public String faceEffectTarget = "";
	public String faceEffect = "";

	public FaceEffect() {

	}

	public FaceEffect(final String faceEffectTarget, final String faceEffect) {
		this.faceEffectTarget = faceEffectTarget;
		this.faceEffect = faceEffect;
	}

	public FaceEffect(final FaceEffect effect) {
		faceEffectTarget = effect.faceEffectTarget;
		faceEffect = effect.faceEffect;
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
