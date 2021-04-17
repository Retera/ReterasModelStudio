package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public interface RenderResourceAllocator {
	InternalResource allocateTexture(Bitmap bitmap, ParticleEmitter2 textureSource);

	RenderResourceAllocator DO_NOTHINGX = (bitmap, textureSource) -> new InternalResource() {
        @Override
        public void bind() {

        }

        @Override
        public InternalInstance addInstance() {
            return new InternalInstance() {

                @Override
                public void show() {
                }

                @Override
                public void setTransformation(final Vec3 worldLocation, final Quat rotation, final Vec3 worldScale) {
                }

                @Override
                public void setSequence(final int index) {
                }

                @Override
                public void setPaused(final boolean paused) {
                }

                @Override
                public void move(final Vec3 deltaPosition) {
                }

                @Override
                public void hide() {
                }
            };
        }
    };
}
