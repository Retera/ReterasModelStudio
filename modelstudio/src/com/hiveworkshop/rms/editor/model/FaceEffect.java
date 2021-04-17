package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.parsers.mdlx.MdlxFaceEffect;

public class FaceEffect {
	private String faceEffectTarget = "";
	private String faceEffect = "";

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

	public String getFaceEffectTarget() {
		return faceEffectTarget;
	}

	public FaceEffect setFaceEffectTarget(String faceEffectTarget) {
		this.faceEffectTarget = faceEffectTarget;
		return this;
	}

	public String getFaceEffect() {
		return faceEffect;
	}

	public FaceEffect setFaceEffect(String faceEffect) {
		this.faceEffect = faceEffect;
		return this;
	}
}
