package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.renderers.renderparts.RenderGeoset;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec3;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.*;

public class CubePainter {
	public static void paintVertCubes(ModelView modelView, RenderModel renderModel, Geoset geo) {

//		glBegin(GL11.GL_TRIANGLES);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL_QUADS);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		float boxRad = .5f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;

		Vec3 upBackRght_adj = new Vec3(frnt * boxRad, rght * boxRad, uppp * boxRad);
		Vec3 dwBackRght_adj = new Vec3(frnt * boxRad, rght * boxRad, down * boxRad);
		Vec3 upFrntRght_adj = new Vec3(back * boxRad, rght * boxRad, uppp * boxRad);
		Vec3 dwFrntRght_adj = new Vec3(back * boxRad, rght * boxRad, down * boxRad);
		Vec3 upBackLeft_adj = new Vec3(frnt * boxRad, left * boxRad, uppp * boxRad);
		Vec3 dwBackLeft_adj = new Vec3(frnt * boxRad, left * boxRad, down * boxRad);
		Vec3 upFrntLeft_adj = new Vec3(back * boxRad, left * boxRad, uppp * boxRad);
		Vec3 dwFrntLeft_adj = new Vec3(back * boxRad, left * boxRad, down * boxRad);

		// uppp (0,0, 1)
		// down (0,0,-1)
		// back (-1,0,0)
		// frnt ( 1,0,0)
		// left (0, 1,0)
		// rght (0,-1,0)

		Vec3 upFrntRght = new Vec3(0, 0, 0);
		Vec3 upBackRght = new Vec3(0, 0, 0);
		Vec3 dwFrntRght = new Vec3(0, 0, 0);
		Vec3 dwBackRght = new Vec3(0, 0, 0);
		Vec3 upFrntLeft = new Vec3(0, 0, 0);
		Vec3 upBackLeft = new Vec3(0, 0, 0);
		Vec3 dwFrntLeft = new Vec3(0, 0, 0);
		Vec3 dwBackLeft = new Vec3(0, 0, 0);
		if (renderGeoset != null) {
			for (GeosetVertex vertex : geo.getVertices()) {
				if (modelView.isSelected(vertex)) {
					glColor4f(1f, .0f, .0f, .7f);
				} else {
					glColor4f(.5f, .3f, .7f, .7f);
				}
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
				if (renderVert != null) {
					Vec3 renderPos = renderVert.getRenderPos();

					upBackRght.set(renderPos).add(upBackRght_adj);
					upBackLeft.set(renderPos).add(upBackLeft_adj);
					upFrntRght.set(renderPos).add(upFrntRght_adj);
					upFrntLeft.set(renderPos).add(upFrntLeft_adj);
					dwBackRght.set(renderPos).add(dwBackRght_adj);
					dwBackLeft.set(renderPos).add(dwBackLeft_adj);
					dwFrntRght.set(renderPos).add(dwFrntRght_adj);
					dwFrntLeft.set(renderPos).add(dwFrntLeft_adj);


//				//Up
					GL11.glNormal3f(0, uppp, 0);
					GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
					GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
					GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
					GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
//
//				//Down
					GL11.glNormal3f(0, down, 0);
					GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
					GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
					GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
					GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
//
//				glColor4f(.7f, .7f, .0f, .7f);
//				//Back
					GL11.glNormal3f(0, 0, back);
					GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
					GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
					GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
					GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
//
//				glColor4f(.0f, .7f, .7f, .7f);
//				//Front
					GL11.glNormal3f(0, 0, frnt);
					GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
					GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
					GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
					GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
//
//				glColor4f(.7f, .0f, .0f, .7f);
//				//Right
					GL11.glNormal3f(rght, 0, 0);
					GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
					GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
					GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
					GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
//
//				glColor4f(0.f, .7f, .0f, .7f);
//				//Left
					GL11.glNormal3f(left, 0, 0);
					GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
					GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
					GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
					GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);

					// uppp (0,0, 1)
					// down (0,0,-1)
					// back (-1,0,0)
					// frnt ( 1,0,0)
					// left (0, 1,0)
					// rght (0,-1,0)
				}
			}
		}
		glEnd();
	}

	public static void paintVertCubes3(Vec3 vec2, CameraHandler cameraHandler) {

//		glBegin(GL11.GL_TRIANGLES);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL_QUADS);
		float boxRadLength = 100f;
		float boxRadHeight = 5f;
		float boxRadWidth = 2f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;

		Vec3 upBackRght_adj = new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwBackRght_adj = new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight);
		Vec3 upFrntRght_adj = new Vec3(back * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwFrntRght_adj = new Vec3(back * boxRadLength, rght * boxRadWidth, down * boxRadHeight);
		Vec3 upBackLeft_adj = new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwBackLeft_adj = new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight);
		Vec3 upFrntLeft_adj = new Vec3(back * boxRadLength, left * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwFrntLeft_adj = new Vec3(back * boxRadLength, left * boxRadWidth, down * boxRadHeight);

		// uppp (0,0, 1)
		// down (0,0,-1)
		// back (-1,0,0)
		// frnt ( 1,0,0)
		// left (0, 1,0)
		// rght (0,-1,0)

		Vec3 upFrntRght = new Vec3(0, 0, 0);
		Vec3 upBackRght = new Vec3(0, 0, 0);
		Vec3 dwFrntRght = new Vec3(0, 0, 0);
		Vec3 dwBackRght = new Vec3(0, 0, 0);
		Vec3 upFrntLeft = new Vec3(0, 0, 0);
		Vec3 upBackLeft = new Vec3(0, 0, 0);
		Vec3 dwFrntLeft = new Vec3(0, 0, 0);
		Vec3 dwBackLeft = new Vec3(0, 0, 0);


		glColor4f(1f, .2f, 1f, .7f);


		Vec3 renderPos = new Vec3(vec2.x, vec2.y, vec2.z);

//		upBackRght.set(upBackRght_adj).add(renderPos);
//		upBackLeft.set(upBackLeft_adj).add(renderPos);
//		upFrntRght.set(upFrntRght_adj).add(renderPos);
//		upFrntLeft.set(upFrntLeft_adj).add(renderPos);
//		dwBackRght.set(dwBackRght_adj).add(renderPos);
//		dwBackLeft.set(dwBackLeft_adj).add(renderPos);
//		dwFrntRght.set(dwFrntRght_adj).add(renderPos);
//		dwFrntLeft.set(dwFrntLeft_adj).add(renderPos);

		upBackRght.set(upBackRght_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);
		upBackLeft.set(upBackLeft_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);
		upFrntRght.set(upFrntRght_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);
		upFrntLeft.set(upFrntLeft_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);
		dwBackRght.set(dwBackRght_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);
		dwBackLeft.set(dwBackLeft_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);
		dwFrntRght.set(dwFrntRght_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);
		dwFrntLeft.set(dwFrntLeft_adj).transform(cameraHandler.getViewPortAntiRotMat2()).add(renderPos);


//				//Top
		GL11.glNormal3f(0, uppp, 0);
		GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
		GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
		GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
		GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
//
//				//Bottom
		GL11.glNormal3f(0, down, 0);
		GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
		GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
		GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
		GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
//
//				glColor4f(.7f, .7f, .0f, .7f);
//				//South
		GL11.glNormal3f(0, 0, back);
		GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
		GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
		GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
		GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
//
//				glColor4f(.0f, .7f, .7f, .7f);
//				//North
		GL11.glNormal3f(0, 0, frnt);
		GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
		GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
		GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
		GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
//
//				glColor4f(.7f, .0f, .0f, .7f);
//				//West
		GL11.glNormal3f(rght, 0, 0);
		GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
		GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
		GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
		GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
//
//				glColor4f(0.f, .7f, .0f, .7f);
//				//East
		GL11.glNormal3f(left, 0, 0);
		GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
		GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
		GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
		GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);

		// uppp (0,0, 1)
		// down (0,0,-1)
		// back (-1,0,0)
		// frnt ( 1,0,0)
		// left (0, 1,0)
		// rght (0,-1,0)
		glEnd();
	}


	public static void paintRekt(Vec3 start, Vec3 end1, Vec3 end2, Vec3 end3, CameraHandler cameraHandler) {

//		glBegin(GL11.GL_TRIANGLES);
//		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL11.GL_LINES);

		float frnt = 1;

		glColor4f(1f, .2f, 1f, .7f);

		GL11.glNormal3f(0, frnt, 0);
		GL11.glVertex3f(start.x, start.y, start.z);
		GL11.glVertex3f(end1.x, end1.y, end1.z);

		GL11.glVertex3f(end1.x, end1.y, end1.z);
		GL11.glVertex3f(end2.x, end2.y, end2.z);

		GL11.glVertex3f(end2.x, end2.y, end2.z);
		GL11.glVertex3f(end3.x, end3.y, end3.z);

		GL11.glVertex3f(end3.x, end3.y, end3.z);
		GL11.glVertex3f(start.x, start.y, start.z);

		glEnd();
	}

	public static void paintVertCubes2(ModelView modelView, RenderModel renderModel, Geoset geo, CameraHandler cameraHandler) {

//		glBegin(GL11.GL_TRIANGLES);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL_QUADS);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		float boxRad = .5f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;


		Vec3 upBackRght_adj = new Vec3(frnt * boxRad, rght * boxRad, uppp * boxRad);
		Vec3 dwBackRght_adj = new Vec3(frnt * boxRad, rght * boxRad, down * boxRad);
		Vec3 upFrntRght_adj = new Vec3(back * boxRad, rght * boxRad, uppp * boxRad);
		Vec3 dwFrntRght_adj = new Vec3(back * boxRad, rght * boxRad, down * boxRad);
		Vec3 upBackLeft_adj = new Vec3(frnt * boxRad, left * boxRad, uppp * boxRad);
		Vec3 dwBackLeft_adj = new Vec3(frnt * boxRad, left * boxRad, down * boxRad);
		Vec3 upFrntLeft_adj = new Vec3(back * boxRad, left * boxRad, uppp * boxRad);
		Vec3 dwFrntLeft_adj = new Vec3(back * boxRad, left * boxRad, down * boxRad);

		// uppp (0,0, 1)
		// down (0,0,-1)
		// back (-1,0,0)
		// frnt ( 1,0,0)
		// left (0, 1,0)
		// rght (0,-1,0)

		Mat4 wM = new Mat4().setIdentity().fromQuat(cameraHandler.getInverseCameraRotation());

		Vec3 upFrntRght = new Vec3(0, 0, 0);
		Vec3 upBackRght = new Vec3(0, 0, 0);
		Vec3 dwFrntRght = new Vec3(0, 0, 0);
		Vec3 dwBackRght = new Vec3(0, 0, 0);
		Vec3 upFrntLeft = new Vec3(0, 0, 0);
		Vec3 upBackLeft = new Vec3(0, 0, 0);
		Vec3 dwFrntLeft = new Vec3(0, 0, 0);
		Vec3 dwBackLeft = new Vec3(0, 0, 0);
		if (renderGeoset != null) {
			for (GeosetVertex vertex : geo.getVertices()) {
				if (modelView.isSelected(vertex)) {
					glColor4f(1f, .0f, .0f, .7f);
				} else {
					glColor4f(.5f, .3f, .7f, .7f);
				}
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
				if (renderVert != null) {
					Vec3 renderPos = renderVert.getRenderPos();

//					upBackRght.set(renderPos).add(upBackRght_adj).transform(wM);
//					upBackLeft.set(renderPos).add(upBackLeft_adj).transform(wM);
//					upFrntRght.set(renderPos).add(upFrntRght_adj).transform(wM);
//					upFrntLeft.set(renderPos).add(upFrntLeft_adj).transform(wM);
//					dwBackRght.set(renderPos).add(dwBackRght_adj).transform(wM);
//					dwBackLeft.set(renderPos).add(dwBackLeft_adj).transform(wM);
//					dwFrntRght.set(renderPos).add(dwFrntRght_adj).transform(wM);
//					dwFrntLeft.set(renderPos).add(dwFrntLeft_adj).transform(wM);
					upBackRght.set(upBackRght_adj).transform(wM).add(renderPos);
					upBackLeft.set(upBackLeft_adj).transform(wM).add(renderPos);
					upFrntRght.set(upFrntRght_adj).transform(wM).add(renderPos);
					upFrntLeft.set(upFrntLeft_adj).transform(wM).add(renderPos);
					dwBackRght.set(dwBackRght_adj).transform(wM).add(renderPos);
					dwBackLeft.set(dwBackLeft_adj).transform(wM).add(renderPos);
					dwFrntRght.set(dwFrntRght_adj).transform(wM).add(renderPos);
					dwFrntLeft.set(dwFrntLeft_adj).transform(wM).add(renderPos);


//				//Up
					GL11.glNormal3f(0, uppp, 0);
					GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
					GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
					GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
					GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);

					glColor4f(.0f, .0f, .7f, .7f);
//				//Down
					GL11.glNormal3f(0, down, 0);
					GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
					GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
					GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
					GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
//
					glColor4f(.7f, .7f, .0f, .7f);
//				//Back
					GL11.glNormal3f(0, 0, back);
					GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
					GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
					GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
					GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
//
					glColor4f(.0f, .7f, .7f, .7f);
//				//Front
					GL11.glNormal3f(0, 0, frnt);
					GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
					GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
					GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
					GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
//
					glColor4f(.7f, .0f, .0f, .7f);
//				//Right
					GL11.glNormal3f(rght, 0, 0);
					GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
					GL11.glVertex3f(dwBackRght.x, dwBackRght.y, dwBackRght.z);
					GL11.glVertex3f(upBackRght.x, upBackRght.y, upBackRght.z);
					GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
//
					glColor4f(0.f, .7f, .0f, .7f);
//				//Left
					GL11.glNormal3f(left, 0, 0);
					GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
					GL11.glVertex3f(upBackLeft.x, upBackLeft.y, upBackLeft.z);
					GL11.glVertex3f(dwBackLeft.x, dwBackLeft.y, dwBackLeft.z);
					GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);

					// uppp (0,0, 1)
					// down (0,0,-1)
					// back (-1,0,0)
					// frnt ( 1,0,0)
					// left (0, 1,0)
					// rght (0,-1,0)
				}
			}
		}
		glEnd();
	}

	public static void paintVertSquares(ModelView modelView, RenderModel renderModel, Geoset geo, CameraHandler cameraHandler) {

//		glBegin(GL11.GL_TRIANGLES);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL_QUADS);
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		float boxRad = .5f;
		float boxRadLength = .001f;
		float boxRadHeight = .5f;
		float boxRadWidth = .5f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;


		Vec3 upFrntRght_adj = new Vec3(frnt * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwFrntRght_adj = new Vec3(frnt * boxRadLength, rght * boxRadWidth, down * boxRadHeight);
		Vec3 upFrntLeft_adj = new Vec3(frnt * boxRadLength, left * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwFrntLeft_adj = new Vec3(frnt * boxRadLength, left * boxRadWidth, down * boxRadHeight);
		Vec3 upBackRght_adj = new Vec3(back * boxRadLength, rght * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwBackRght_adj = new Vec3(back * boxRadLength, rght * boxRadWidth, down * boxRadHeight);
		Vec3 upBackLeft_adj = new Vec3(back * boxRadLength, left * boxRadWidth, uppp * boxRadHeight);
		Vec3 dwBackLeft_adj = new Vec3(back * boxRadLength, left * boxRadWidth, down * boxRadHeight);

		// uppp (0,0, 1)
		// down (0,0,-1)
		// back (-1,0,0)
		// frnt ( 1,0,0)
		// left (0, 1,0)
		// rght (0,-1,0)

		Mat4 wM = new Mat4().setIdentity().fromQuat(cameraHandler.getInverseCameraRotation());

		Vec3 upBackRght = new Vec3(0, 0, 0);
		Vec3 upFrntRght = new Vec3(0, 0, 0);
		Vec3 dwBackRght = new Vec3(0, 0, 0);
		Vec3 dwFrntRght = new Vec3(0, 0, 0);
		Vec3 upBackLeft = new Vec3(0, 0, 0);
		Vec3 upFrntLeft = new Vec3(0, 0, 0);
		Vec3 dwBackLeft = new Vec3(0, 0, 0);
		Vec3 dwFrntLeft = new Vec3(0, 0, 0);
		if (renderGeoset != null) {
			for (GeosetVertex vertex : geo.getVertices()) {
				if (modelView.isSelected(vertex)) {
					glColor4f(1f, .0f, .0f, .7f);
				} else {
					glColor4f(.5f, .3f, .7f, .7f);
				}
				RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
				if (renderVert != null) {
					Vec3 renderPos = renderVert.getRenderPos();

//					upFrntRght.set(renderPos).add(upFrntRght_adj).transform(wM);
//					upFrntLeft.set(renderPos).add(upFrntLeft_adj).transform(wM);
//					upBackRght.set(renderPos).add(upBackRght_adj).transform(wM);
//					upBackLeft.set(renderPos).add(upBackLeft_adj).transform(wM);
//					dwFrntRght.set(renderPos).add(dwFrntRght_adj).transform(wM);
//					dwFrntLeft.set(renderPos).add(dwFrntLeft_adj).transform(wM);
//					dwBackRght.set(renderPos).add(dwBackRght_adj).transform(wM);
//					dwBackLeft.set(renderPos).add(dwBackLeft_adj).transform(wM);
					upFrntRght.set(upFrntRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);
					upFrntLeft.set(upFrntLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);
					upBackRght.set(upBackRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);
					upBackLeft.set(upBackLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);
					dwFrntRght.set(dwFrntRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);
					dwFrntLeft.set(dwFrntLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);
					dwBackRght.set(dwBackRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);
					dwBackLeft.set(dwBackLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(renderPos);

//
//				glColor4f(.0f, .7f, .7f, .7f);
//				//Front
					GL11.glNormal3f(frnt, 0, 0);
					GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
					GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
					GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
					GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
//
				}
			}
		}
		glEnd();
	}

	public static void paintSquare(Vec3 vec3, CameraHandler cameraHandler) {

//		glBegin(GL11.GL_TRIANGLES);
		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		glBegin(GL_QUADS);
		float boxRad = .5f;
		float frnt = 1;
		float left = 1;
		float uppp = 1;
		float back = -frnt;
		float rght = -left;
		float down = -uppp;


		Vec3 upFrntRght_adj = new Vec3(frnt * boxRad, rght * boxRad, uppp * boxRad);
		Vec3 dwFrntRght_adj = new Vec3(frnt * boxRad, rght * boxRad, down * boxRad);
		Vec3 upBackRght_adj = new Vec3(back * boxRad, rght * boxRad, uppp * boxRad);
		Vec3 dwBackRght_adj = new Vec3(back * boxRad, rght * boxRad, down * boxRad);
		Vec3 upFrntLeft_adj = new Vec3(frnt * boxRad, left * boxRad, uppp * boxRad);
		Vec3 dwFrntLeft_adj = new Vec3(frnt * boxRad, left * boxRad, down * boxRad);
		Vec3 upBackLeft_adj = new Vec3(back * boxRad, left * boxRad, uppp * boxRad);
		Vec3 dwBackLeft_adj = new Vec3(back * boxRad, left * boxRad, down * boxRad);
		// uppp (0,0, 1)
		// down (0,0,-1)
		// frnt (-1,0,0)
		// back ( 1,0,0)
		// left (0, 1,0)
		// rght (0,-1,0)

		Mat4 wM = new Mat4().setIdentity().fromQuat(cameraHandler.getInverseCameraRotation());

		Vec3 upBackRght = new Vec3(0, 0, 0);
		Vec3 upFrntRght = new Vec3(0, 0, 0);
		Vec3 dwBackRght = new Vec3(0, 0, 0);
		Vec3 dwFrntRght = new Vec3(0, 0, 0);
		Vec3 upBackLeft = new Vec3(0, 0, 0);
		Vec3 upFrntLeft = new Vec3(0, 0, 0);
		Vec3 dwBackLeft = new Vec3(0, 0, 0);
		Vec3 dwFrntLeft = new Vec3(0, 0, 0);


		glColor4f(1f, .3f, 1f, .7f);
		if (vec3 != null) {


			upFrntRght.set(upFrntRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);
			upFrntLeft.set(upFrntLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);
			upBackRght.set(upBackRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);
			upBackLeft.set(upBackLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);
			dwFrntRght.set(dwFrntRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);
			dwFrntLeft.set(dwFrntLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);
			dwBackRght.set(dwBackRght_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);
			dwBackLeft.set(dwBackLeft_adj).scale((float) (1f / cameraHandler.getZoom())).transform(wM).add(vec3);

//
//				glColor4f(.0f, .7f, .7f, .7f);
//				//Front
			GL11.glNormal3f(frnt, 0, 0);
			GL11.glVertex3f(dwFrntRght.x, dwFrntRght.y, dwFrntRght.z);
			GL11.glVertex3f(dwFrntLeft.x, dwFrntLeft.y, dwFrntLeft.z);
			GL11.glVertex3f(upFrntLeft.x, upFrntLeft.y, upFrntLeft.z);
			GL11.glVertex3f(upFrntRght.x, upFrntRght.y, upFrntRght.z);
//
		}
		glEnd();
	}
}
