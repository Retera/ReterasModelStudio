package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

public interface RenderResourceAllocator {
	InternalResource allocateTexture(Bitmap bitmap, ParticleEmitter2 textureSource);

	RenderResourceAllocator DO_NOTHINGX = new RenderResourceAllocator() {
		@Override
		public InternalResource allocateTexture(final Bitmap bitmap, final ParticleEmitter2 textureSource) {
			return new InternalResource() {
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
						public void setTransformation(final Vector3f worldLocation, final Quaternion rotation,
								final Vector3f worldScale) {
						}

						@Override
						public void setSequence(final int index) {
						}

						@Override
						public void setPaused(final boolean paused) {
						}

						@Override
						public void move(final Vector3f deltaPosition) {
						}

						@Override
						public void hide() {
						}
					};
				}
			};
		}
	};
}
