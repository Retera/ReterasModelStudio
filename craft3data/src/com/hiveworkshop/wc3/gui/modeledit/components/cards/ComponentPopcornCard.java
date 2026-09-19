package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;

public class ComponentPopcornCard extends ComponentNodeCard<ParticleEmitterPopcorn> {
	private static final String[] OPTION_FLAGS = { "SortPrimsFarZ", "Unshaded", "Unfogged", "AlwaysUpdate" };

	@Override
	protected void addTypeRows() {
		beginSection("Popcorn FX Emitter");
		addTextField("Path", ParticleEmitterPopcorn::getPath, ParticleEmitterPopcorn::setPath);
		addTextField("Visibility Guide", ParticleEmitterPopcorn::getAnimVisibilityGuide,
				ParticleEmitterPopcorn::setAnimVisibilityGuide);
		addIntSpinner("Replaceable ID", 0, Integer.MAX_VALUE, ParticleEmitterPopcorn::getReplaceableId,
				ParticleEmitterPopcorn::setReplaceableId);
		for (final String flag : OPTION_FLAGS) {
			addFlagCheckBox(flag, ParticleEmitterPopcorn::getFlags);
		}
		endSection();
		addColorValue("Color", ParticleEmitterPopcorn::getColor, ParticleEmitterPopcorn::setColor, e -> e,
				ParticleEmitterPopcorn::getAnimFlags, "Color");
		addFloatValue("Alpha", e -> (double) e.getAlpha(), (e, v) -> e.setAlpha(v.floatValue()), e -> e,
				ParticleEmitterPopcorn::getAnimFlags, "Alpha");
		addFloatValue("Emission Rate", e -> (double) e.getEmissionRate(), (e, v) -> e.setEmissionRate(v.floatValue()),
				e -> e, ParticleEmitterPopcorn::getAnimFlags, "EmissionRate");
		addFloatValue("Life Span", e -> (double) e.getLifeSpan(), (e, v) -> e.setLifeSpan(v.floatValue()), e -> e,
				ParticleEmitterPopcorn::getAnimFlags, "LifeSpan");
		addFloatValue("Speed", e -> (double) e.getSpeed(), (e, v) -> e.setSpeed(v.floatValue()), e -> e,
				ParticleEmitterPopcorn::getAnimFlags, "Speed");
	}
}
