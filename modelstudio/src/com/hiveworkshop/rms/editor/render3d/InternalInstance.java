package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public interface InternalInstance {
    void setTransformation(Vec3 worldLocation, Quat rotation, Vec3 worldScale);
    void setSequence(int index);
    void show();
    void setPaused(boolean paused);
    void move(Vec3 deltaPosition);

    void hide();
}
