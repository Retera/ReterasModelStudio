package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;

public class CubePainter {
	static double A90 = (Math.PI/2.0);
	Color ugg = new Color(255, (int)(255 * .2f), 255);
	Color ugg2 = new Color(255, 255, 255);


	public static void paintRekt(Vec3 start, Vec3 end1, Vec3 end2, Vec3 end3, CameraHandler cameraHandler) {

//		glBegin(GL11.GL_TRIANGLES);
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
//		GL11.glDepthMask(false);
//		glDisable(GL_DEPTH_TEST);
//		glBegin(GL11.GL_LINES);


		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
//		disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		glColor3f(255f, 1f, 255f);
		glColor4f(.7f, .7f, .7f, .4f);
		glBegin(GL11.GL_LINES);
		GL11.glNormal3f(0, 0, 0);

		float frnt = 1;


		glColor4f(1f, .2f, 1f, .7f);

		GL11.glNormal3f(0, frnt, 0);
		doGlQuad(start, end1, end2, end1);

		doGlQuad(end2, end3, start, end3);

		glEnd();
	}


	public static void paintCameraLookAt(CameraHandler cameraHandler) {
		Vec3 cameraLookAt = cameraHandler.getCameraLookAt();
		float lineLength = 20;
		GL11.glDepthMask(false);
		GL11.glEnable(GL11.GL_BLEND);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		glColor3f(255f, 1f, 255f);
		glColor4f(.7f, .7f, .7f, .4f);
		glBegin(GL11.GL_LINES);
		GL11.glNormal3f(0, 0, 0);

		glColor4f(.7f, 1f, .7f, .4f);
		GL11.glVertex3f(cameraLookAt.x, cameraLookAt.y - lineLength, cameraLookAt.z);
		GL11.glVertex3f(cameraLookAt.x, cameraLookAt.y + lineLength, cameraLookAt.z);

		glColor4f(1f, .7f, .7f, .4f);
		GL11.glVertex3f(cameraLookAt.x - lineLength, cameraLookAt.y, cameraLookAt.z);
		GL11.glVertex3f(cameraLookAt.x + lineLength, cameraLookAt.y, cameraLookAt.z);

		glColor4f(.5f, .5f, 1f, .7f);
		GL11.glVertex3f(cameraLookAt.x, cameraLookAt.y, cameraLookAt.z - lineLength);
		GL11.glVertex3f(cameraLookAt.x, cameraLookAt.y, cameraLookAt.z + lineLength);

		glEnd();
	}


	private static void doGlQuad(Vec3 RT, Vec3 LT, Vec3 RB, Vec3 LB) {
		GL11.glVertex3f(RT.x, RT.y, RT.z);
		GL11.glVertex3f(LT.x, LT.y, LT.z);
		GL11.glVertex3f(LB.x, LB.y, LB.z);
		GL11.glVertex3f(RB.x, RB.y, RB.z);
	}

	// uppp (0,0, 1)
	// down (0,0,-1)
	// back (-1,0,0)
	// frnt ( 1,0,0)
	// left (0, 1,0)
	// rght (0,-1,0)
}
