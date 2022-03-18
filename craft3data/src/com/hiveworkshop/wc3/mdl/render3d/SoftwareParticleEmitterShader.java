package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.opengl.GL11;

import com.hiveworkshop.rms.editor.render3d.NGGLDP;

public class SoftwareParticleEmitterShader implements ParticleEmitterShader {
	private static final byte TFF = (byte) (255);

	@Override
	public void renderParticles(final int blendSrc, final int blendDst, final int rows, final int cols,
			final InternalResource texture, final float[] bufferData, final boolean isRibbonEmitter,
			final int numItemsToRender) {
		texture.bind();
		GL11.glBlendFunc(blendSrc, blendDst);
		NGGLDP.pipeline.glBegin(GL11.GL_TRIANGLES);
		if ((numItemsToRender * 5) > bufferData.length) {
//			throw new IllegalStateException(numItemsToRender + " alive items, " + bufferData.length + " len buffer");
			new IllegalStateException(numItemsToRender + " alive items, " + bufferData.length + " len buffer")
					.printStackTrace();
			return;
		}
		for (int itemIndex = 0; itemIndex < numItemsToRender; itemIndex++) {
			final int i = itemIndex * 5;
			final int colorInt = (int) bufferData[i + 4];
			final int uvInt = (int) bufferData[i + 3];
			NGGLDP.pipeline.glColor4ub((byte) ((colorInt >>> 16) & 0xFF), (byte) ((colorInt >>> 8) & 0xFF),
					(byte) ((colorInt) & 0xFF), (byte) ((uvInt) & 0xFF));
//			GL11.glColor4ub(TFF, TFF, TFF, TFF);
//			System.out.println((byte) ((colorInt >> 16) & 0xFF) + "," + (byte) ((colorInt >> 8) & 0xFF) + ","
//					+ (byte) ((colorInt) & 0xFF) + "," + (byte) ((uvInt) & 0xFF));
			float uv_u = (short) ((uvInt >> 16) & 0xFF);
			float uv_v = (short) ((uvInt >> 8) & 0xFF);
			if (isRibbonEmitter) {
				uv_u /= 255.0f;
				uv_v /= 255.0f;
			}
			else {
				uv_u /= cols;
				uv_v /= rows;
			}
			NGGLDP.pipeline.glTexCoord2f(uv_u, uv_v);
			NGGLDP.pipeline.glNormal3f(0, 0, 1);
			NGGLDP.pipeline.glVertex3f(bufferData[i + 1], bufferData[i + 2], bufferData[i]);
		}
		NGGLDP.pipeline.glEnd();
	}

	@Override
	public void use() {

	}
}
