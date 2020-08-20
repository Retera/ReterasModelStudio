package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public interface InternalInstance {
    void setTransformation(Vector3f worldLocation, Quaternion rotation, Vector3f worldScale);
    void setSequence(int index);
    void show();
    void setPaused(boolean paused);
    void move(Vector3f deltaPosition);

    void hide();
}
