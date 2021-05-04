package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.ui.application.viewer.Particle2TextureInstance;

public abstract class EmitterView {
    public float currentEmission;

    public RenderModel instance;

    public abstract void addToScene(Particle2TextureInstance internalInstance);
}
