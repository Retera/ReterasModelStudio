package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.HashMap;

public class Particle2TextureInstance {
	private Bitmap bitmap;
	private ParticleEmitter2 particle;
	private boolean loaded = false;
	private ProgramPreferences programPreferences;
	private HashMap<Bitmap, Integer> textureMap;
	private ModelView modelView;

	public Particle2TextureInstance(Bitmap bitmap, ParticleEmitter2 particle, HashMap<Bitmap, Integer> textureMap, ModelView modelView, ProgramPreferences programPreferences) {
		this.bitmap = bitmap;
		this.particle = particle;
		this.textureMap = textureMap;
		this.modelView = modelView;
		this.programPreferences = programPreferences;
	}
	public Particle2TextureInstance(HashMap<Bitmap, Integer> textureMap, ModelView modelView, ProgramPreferences programPreferences) {
		this.textureMap = textureMap;
		this.modelView = modelView;
		this.programPreferences = programPreferences;
	}

	public Particle2TextureInstance generate(Bitmap bitmap, ParticleEmitter2 particle){
		return new Particle2TextureInstance(bitmap, particle, textureMap, modelView, programPreferences);
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
			TextureThing.loadToTexMap(modelView, programPreferences, textureMap, bitmap);
			loaded = true;
		}
		Integer texture = textureMap.get(bitmap);
		TextureThing.bindParticleTexture(textureMap, particle, bitmap, texture);
	}

	public Particle2TextureInstance addInstance() {
		return this;
	}
}