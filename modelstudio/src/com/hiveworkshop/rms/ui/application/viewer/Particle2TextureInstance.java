package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public class Particle2TextureInstance {
	private Bitmap bitmap;
	private ParticleEmitter2 particle;
	private boolean loaded = false;
	private final ProgramPreferences programPreferences;
	private final TextureThing textureThing;
	private final ModelView modelView;

	public Particle2TextureInstance(Bitmap bitmap, ParticleEmitter2 particle, TextureThing textureThing, ModelView modelView, ProgramPreferences programPreferences) {
		this.bitmap = bitmap;
		this.particle = particle;
		this.textureThing = textureThing;
		this.modelView = modelView;
		this.programPreferences = programPreferences;
	}
	public Particle2TextureInstance(TextureThing textureThing, ModelView modelView, ProgramPreferences programPreferences) {
		this.textureThing = textureThing;
		this.modelView = modelView;
		this.programPreferences = programPreferences;
	}

	public Particle2TextureInstance generate(Bitmap bitmap, ParticleEmitter2 particle){
		return new Particle2TextureInstance(bitmap, particle, textureThing, modelView, programPreferences);
	}

	public void setTransformation(Vec3 worldLocation, Quat rotation, Vec3 worldScale) {
	}

	public void setSequence(int index) {
	}

	public void show() {
	}

	public void setPaused(boolean paused) {
	}

	public void move(Vec3 deltaPosition) {
	}

	public void hide() {
	}

	public void bind() {
		if (!loaded) {
			textureThing.loadToTexMap(bitmap);
			loaded = true;
		}
		textureThing.bindParticleTexture(particle, bitmap);
	}

	public Particle2TextureInstance addInstance() {
		return this;
	}
}