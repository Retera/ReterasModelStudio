package com.hiveworkshop.wc3.mdl.render3d;

import com.hiveworkshop.wc3.mdl.IdObject;

import java.util.ArrayList;
import java.util.List;

public abstract class RenderSharedEmitter<MODEL_OBJECT extends IdObject> {
    protected final MODEL_OBJECT modelObject;
    protected List<EmittedObject> objects;
    protected int alive;

    public RenderSharedEmitter(MODEL_OBJECT modelObject) {
        this.modelObject = modelObject;
        this.objects = new ArrayList<>();
        this.alive = 0;
    }

    public EmittedObject emitObject(EmitterView emitterView, boolean flag) {
        if(alive==objects.size()) {
            objects.add(createObject());
        }

        EmittedObject object = objects.get(alive);
        this.alive += 1;
        object.reset(emitterView, flag);

        return object;
    }

    public void update() {
        for(int i = 0; i < alive; i++) {
            EmittedObject object = objects.get(i);

            object.update();

            if(object.health <= 0) {
                alive -= 1;

                // Swap between this object and the first unused object.
                // Decrement the iterator so the moved object is indexed
                if(i != this.alive) {
                    objects.set(i, objects.get(alive));
                    objects.set(this.alive, object);
                    i -= 1;
                }
            }
        }

        this.updateData();
    }

    public void fill(EmitterView emitterView) {
        int emission = emitterView.currentEmission;
        
        if(emission >= 1) {
            for(int i = 0; i < emission; i+= 1, emitterView.currentEmission--) {
                this.emit(emitterView);
            }
        }
    }

    protected abstract void emit(EmitterView emitterView);

    protected abstract void updateData();

    protected abstract EmittedObject createObject();

    protected abstract void render(RenderModel modelView, ParticleEmitterShader shader);

    public void clear(Object owner) {
        for(EmittedObject object: this.objects) {
            if(owner == object.emitterView.instance) {
                object.health = 0;
            }
        }
    }
}
