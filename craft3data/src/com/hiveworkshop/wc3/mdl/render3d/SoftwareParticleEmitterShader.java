package com.hiveworkshop.wc3.mdl.render3d;

import org.lwjgl.opengl.GL11;

public class SoftwareParticleEmitterShader implements ParticleEmitterShader {
    @Override
    public void renderParticles(int blendSrc, int blendDst, int rows, int cols, InternalResource texture, float[] bufferData, boolean isRibbonEmitter) {
        GL11.glBlendFunc(blendSrc, blendDst);
        GL11.glBegin(GL11.GL_TRIANGLES);
        texture.bind();
        for(int i = 0; i < bufferData.length; i+= 5) {
            int colorInt = (int)bufferData[i+4];
            GL11.glColor3b((byte)((colorInt>>16)&0xFF),  (byte)((colorInt>>8)&0xFF), (byte)((colorInt)&0xFF));
            int uvInt = (int)bufferData[i+3];
            float uv_u = (byte) ((uvInt >> 16) & 0xFF);
            float uv_v = (byte) ((uvInt >> 8) & 0xFF);
            if(isRibbonEmitter) {
                uv_u /= 255.0f;
                uv_v /= 255.0f;
            }
            GL11.glTexCoord2f(uv_u, uv_v);
            GL11.glVertex3f(bufferData[i], bufferData[i+1],bufferData[i+2]);
        }
        GL11.glEnd();
    }
}
