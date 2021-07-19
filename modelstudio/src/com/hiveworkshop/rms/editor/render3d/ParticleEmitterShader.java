package com.hiveworkshop.rms.editor.render3d;

import com.hiveworkshop.rms.ui.application.viewer.Particle2TextureInstance;
import org.lwjgl.opengl.GL11;

public class ParticleEmitterShader {
	private static final byte TFF = (byte) (255);

	public void renderParticles(int blendSrc, int blendDst, int rows, int cols, Particle2TextureInstance texture, RenderSharedGeometryEmitter.RenderData[] bufferData, boolean isRibbonEmitter, int numItemsToRender) {
		texture.bind();
		GL11.glBlendFunc(blendSrc, blendDst);
		GL11.glBegin(GL11.GL_TRIANGLES);
		if ((numItemsToRender) > bufferData.length) {
			new IllegalStateException(numItemsToRender + " alive items, " + bufferData.length + " len buffer").printStackTrace();
			return;
		}
		for (int i = 0; i < numItemsToRender; i++) {
			int colorInt = (int) bufferData[i].color;
			int uvInt = (int) bufferData[i].uv;
			GL11.glColor4ub((byte) ((colorInt >>> 16) & 0xFF), (byte) ((colorInt >>> 8) & 0xFF), (byte) ((colorInt) & 0xFF), (byte) ((uvInt) & 0xFF));

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
//			GL11.glVertex3f(bufferData[i].v.y, bufferData[i].v.z, bufferData[i].v.x);
			GL11.glVertex3f(bufferData[i].v.x, bufferData[i].v.y, bufferData[i].v.z);
		}
		GL11.glEnd();
	}
	public void renderParticles(EmitterIdObject emitterIdObject, Particle2TextureInstance texture, RenderSharedGeometryEmitter.RenderData[] bufferData, int numItemsToRender) {
		int blendSrc = emitterIdObject.getBlendSrc();
		int blendDst = emitterIdObject.getBlendDst();
		int rows = emitterIdObject.getRows();
		int cols = emitterIdObject.getCols();
		boolean isRibbonEmitter = emitterIdObject.isRibbonEmitter();

		texture.bind();
		GL11.glBlendFunc(blendSrc, blendDst);
		GL11.glBegin(GL11.GL_TRIANGLES);
		if ((numItemsToRender) > bufferData.length) {
			new IllegalStateException(numItemsToRender + " alive items, " + bufferData.length + " len buffer").printStackTrace();
			return;
		}
		for (int i = 0; i < numItemsToRender; i++) {
			int colorInt = (int) bufferData[i].color;
			int uvInt = (int) bufferData[i].uv;
			GL11.glColor4ub((byte) ((colorInt >>> 16) & 0xFF), (byte) ((colorInt >>> 8) & 0xFF), (byte) ((colorInt) & 0xFF), (byte) ((uvInt) & 0xFF));

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
			GL11.glVertex3f(bufferData[i].v.x, bufferData[i].v.y, bufferData[i].v.z);
		}
		GL11.glEnd();
	}

}
