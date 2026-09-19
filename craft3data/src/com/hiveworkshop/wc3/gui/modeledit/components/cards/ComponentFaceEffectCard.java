package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import com.hiveworkshop.wc3.mdx.FaceEffectsChunk.FaceEffect;

public class ComponentFaceEffectCard extends ComponentCard<FaceEffect> {
	public ComponentFaceEffectCard() {
		addTextField("Target", effect -> effect.faceEffectTarget, (effect, value) -> effect.faceEffectTarget = value);
		addTextField("FaceFX Path", effect -> effect.faceEffect, (effect, value) -> effect.faceEffect = value);
	}
}
