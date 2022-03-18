package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;

import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;

public interface RenderResourceAllocator {
	InternalResource allocateTexture(Bitmap bitmap, ParticleEmitter2 textureSource);

	InternalResource allocateMaterial(Material material, RibbonEmitter ribbonEmitter);

	public static final RenderResourceAllocator DO_NOTHINGX = new RenderResourceAllocator() {
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

		@Override
		public InternalResource allocateMaterial(final Material material, final RibbonEmitter ribbonEmitter) {
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
