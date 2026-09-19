package com.hiveworkshop.wc3.gui.modeledit.components.cards;

import com.hiveworkshop.wc3.mdl.ParticleEmitter;

public class ComponentParticleEmitterCard extends ComponentNodeCard<ParticleEmitter> {
	@Override
	protected void addTypeRows() {
		beginSection("Particle Emitter");
		addCheckBox("Emitter uses MDL (spawns a model)", ParticleEmitter::isMDLEmitter, ParticleEmitter::setMDLEmitter);
		addTextField("Path", ParticleEmitter::getPath, ParticleEmitter::setPath);
		endSection();
		addFloatValue("Emission Rate", ParticleEmitter::getEmissionRate, ParticleEmitter::setEmissionRate, e -> e,
				ParticleEmitter::getAnimFlags, "EmissionRate");
		addFloatValue("Gravity", ParticleEmitter::getGravity, ParticleEmitter::setGravity, e -> e,
				ParticleEmitter::getAnimFlags, "Gravity");
		addFloatValue("Longitude", ParticleEmitter::getLongitude, ParticleEmitter::setLongitude, e -> e,
				ParticleEmitter::getAnimFlags, "Longitude");
		addFloatValue("Latitude", ParticleEmitter::getLatitude, ParticleEmitter::setLatitude, e -> e,
				ParticleEmitter::getAnimFlags, "Latitude");
		addFloatValue("Life Span", ParticleEmitter::getLifeSpan, ParticleEmitter::setLifeSpan, e -> e,
				ParticleEmitter::getAnimFlags, "LifeSpan");
		addFloatValue("Initial Velocity", ParticleEmitter::getInitVelocity, ParticleEmitter::setInitVelocity, e -> e,
				ParticleEmitter::getAnimFlags, "InitVelocity");
	}
}
