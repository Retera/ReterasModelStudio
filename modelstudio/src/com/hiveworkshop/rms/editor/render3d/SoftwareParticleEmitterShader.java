package com.hiveworkshop.rms.editor.render3d;

import org.lwjgl.opengl.GL11;

public class SoftwareParticleEmitterShader implements ParticleEmitterShader {
	private static final byte TFF = (byte) (255);

	@Override
	public void renderParticles(final int blendSrc, final int blendDst, final int rows, final int cols, final InternalResource texture, final RenderSharedGeometryEmitter.RenderData[] bufferData, final boolean isRibbonEmitter, final int numItemsToRender) {
		texture.bind();
		GL11.glBlendFunc(blendSrc, blendDst);
		GL11.glBegin(GL11.GL_TRIANGLES);
		if ((numItemsToRender) > bufferData.length) {
//			throw new IllegalStateException(numItemsToRender + " alive items, " + bufferData.length + " len buffer");
			new IllegalStateException(numItemsToRender + " alive items, " + bufferData.length + " len buffer").printStackTrace();
			return;
		}
		for (int i = 0; i < numItemsToRender; i++) {
//			System.out.println("i: " + i);
//			final int i = itemIndex * 5;
			final int colorInt = (int) bufferData[i].color;
			final int uvInt = (int) bufferData[i].uv;
			GL11.glColor4ub((byte) ((colorInt >>> 16) & 0xFF), (byte) ((colorInt >>> 8) & 0xFF), (byte) ((colorInt) & 0xFF), (byte) ((uvInt) & 0xFF));
//			GL11.glColor4ub(TFF, TFF, TFF, TFF);
//			System.out.println((byte) ((colorInt >> 16) & 0xFF) + "," + (byte) ((colorInt >> 8) & 0xFF) + ","
//					+ (byte) ((colorInt) & 0xFF) + "," + (byte) ((uvInt) & 0xFF));
			float uv_u = (byte) ((uvInt >> 16) & 0xFF);
			float uv_v = (byte) ((uvInt >> 8) & 0xFF);
			if (isRibbonEmitter) {
				uv_u /= 255.0f;
				uv_v /= 255.0f;
			} else {
				uv_u /= cols;
				uv_v /= rows;
			}
			GL11.glTexCoord2f(uv_u, uv_v);
			GL11.glVertex3f(bufferData[i].v.y, bufferData[i].v.z, bufferData[i].v.x);
		}
		GL11.glEnd();
	}

	@Override
	public void use() {

	}
}
