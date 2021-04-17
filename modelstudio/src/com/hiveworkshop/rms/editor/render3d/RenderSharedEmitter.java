package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.IdObject;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderSharedEmitter<MODEL_OBJECT extends IdObject, EMITTER_VIEW extends EmitterView> {
	protected final MODEL_OBJECT modelObject;
	protected List<EmittedObject<EMITTER_VIEW>> objects;
	protected int alive;

	public RenderSharedEmitter(final MODEL_OBJECT modelObject) {
		this.modelObject = modelObject;
		this.objects = new ArrayList<>();
		this.alive = 0;
	}

	public EmittedObject<EMITTER_VIEW> emitObject(final EMITTER_VIEW emitterView, final boolean flag) {
		if (alive == objects.size()) {
			objects.add(createObject());
		}

		final EmittedObject<EMITTER_VIEW> object = objects.get(alive);
		this.alive += 1;
		object.reset(emitterView, flag);

		return object;
	}

	public void update() {
		for (int i = 0; i < alive; i++) {
			final EmittedObject<EMITTER_VIEW> object = objects.get(i);

			object.update();

			if (object.health <= 0) {
				alive -= 1;

				// Swap between this object and the first unused object.
				// Decrement the iterator so the moved object is indexed
				if (i != this.alive) {
					objects.set(i, objects.get(alive));
					objects.set(this.alive, object);
					i -= 1;
				}
			}
		}
//		System.out.println("alive: " + alive);

		this.updateData();
	}

	public void fill(final EMITTER_VIEW emitterView) {
		final float emission = emitterView.currentEmission;

		if (emission >= 1) {
			for (int i = 0; i < emission; i += 1, emitterView.currentEmission--) {
				this.emit(emitterView);
			}
		}
	}

	protected abstract void emit(EMITTER_VIEW emitterView);

	protected abstract void updateData();

	protected abstract EmittedObject<EMITTER_VIEW> createObject();

	protected abstract void render(RenderModel modelView, ParticleEmitterShader shader);

	public void clear(final Object owner) {
		for (final EmittedObject<EMITTER_VIEW> object : this.objects) {
			if (owner == object.emitterView.instance) {
				object.health = 0;
			}
		}
	}
}
