package com.hiveworkshop.wc3.mdl.render3d;

public abstract class EmittedObject {
    public int health;

    public EmitterView emitterView;

    public float[] vertices;

    public float lta, lba, rta, rba, rgb;

    public abstract void reset(EmitterView emitterView, boolean flag);

    public abstract void update();
}
