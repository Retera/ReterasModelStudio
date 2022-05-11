package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.render3d.RenderParticle2Inst;
import com.hiveworkshop.rms.editor.render3d.RenderParticleEmitter2;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.util.Queue;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBegin;

public class ParticleRenderer {
	private TextureThing textureThing;
	private RenderModel renderModel;

	public ParticleRenderer(){
	}
	public ParticleRenderer setModel(RenderModel renderModel, TextureThing textureThing){
		this.textureThing = textureThing;
		this.renderModel = renderModel;
		return this;
	}


	public void render(RenderParticleEmitter2 emitter) {
		renderParticles(emitter);
	}

	private void renderParticles(RenderParticleEmitter2 emitter) {
		ParticleEmitter2 particleEmitter2 = emitter.getParticleEmitter2();
		int blendSrc = particleEmitter2.getBlendSrc();
		int blendDst = particleEmitter2.getBlendDst();
		int rows = particleEmitter2.getRows();
		int cols = particleEmitter2.getCols();
		boolean isRibbonEmitter = particleEmitter2.isRibbonEmitter();

		bind(particleEmitter2);
		GL11.glBlendFunc(blendSrc, blendDst);
		GL11.glBegin(GL11.GL_TRIANGLES);
		Queue<RenderParticle2Inst> aliveQueue = emitter.getAliveQueue();
		for (RenderParticle2Inst inst : aliveQueue) {
			int colorInt = (int) inst.getColor();
			if(inst.getVert(0) != null){
				for (int j = 0; j<6; j++){
					int uvInt = (int) inst.getUv(j);
					GL11.glColor4ub((byte) ((colorInt >>> 16) & 0xFF), (byte) ((colorInt >>> 8) & 0xFF), (byte) ((colorInt) & 0xFF), (byte) ((uvInt) & 0xFF));

					float uv_u = (byte) ((uvInt >> 16) & 0xFF);
					float uv_v = (byte) ((uvInt >> 8) & 0xFF);

//				float uv_u = (byte) ((bufferDatum.getUv_u(i) >> 16) & 0xFF);
//				float uv_v = (byte) ((bufferDatum.getUv_u(i) >> 8) & 0xFF);
					if (isRibbonEmitter) {
						uv_u /= 255.0f;
						uv_v /= 255.0f;
					} else {
						uv_u /= cols;
						uv_v /= rows;
					}
					GL11.glTexCoord2f(uv_u, uv_v);

					Vec3 vert = inst.getVert(j);
					GL11.glVertex3f(vert.x, vert.y, vert.z);
				}
			}
		}
		GL11.glEnd();

//		drawTestTriangle();
	}

	private void drawTestTriangle() {
		glBegin(GL_TRIANGLES);
		GL11.glNormal3f(0, 0, 0);

		GL11.glColor4f(1, 0,0,1);
		GL11.glVertex3f(0.0f, 0.0f, 2.0f);
		GL11.glVertex3f(2.0f, 2.0f, 0.0f);
		GL11.glVertex3f(-2.0f, -2.0f, 0.0f);
		GL11.glColor4f(0, 1,0,1);
		GL11.glVertex3f(0.0f, 0.0f, 2.0f);
		GL11.glVertex3f(-2.0f, -2.0f, 0.0f);
		GL11.glVertex3f(2.0f, 2.0f, 0.0f);
		GL11.glEnd();
	}


	public void bind(ParticleEmitter2 particleEmitter2) {
		Bitmap bitmap = particleEmitter2.getTexture();
		textureThing.loadToTexMap(renderModel.getModel(), bitmap);
		textureThing.bindParticleTexture(particleEmitter2, bitmap);
	}
}
