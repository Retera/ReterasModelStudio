package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.QuaternionRotation;

import org.lwjgl.util.vector.Vector3f;

public interface InternalInstance {
    void setTransformation(Vector3f worldLocation, QuaternionRotation rotation, Vector3f worldScale);
    void setSequence(int index);
    void show();
    void setPaused(boolean paused);
    void move(Vector3f deltaPosition);

    void hide();
}
