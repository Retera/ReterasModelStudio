package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;

public interface RenderResourceAllocator {
	InternalResource allocateTexture(Bitmap bitmap, ParticleEmitter2 textureSource);

}
