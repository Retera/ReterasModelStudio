package com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderGeoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.application.viewer.TextureThing;
import com.hiveworkshop.rms.ui.preferences.ColorThing;
import com.hiveworkshop.rms.ui.preferences.EditorColorPrefs;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class GeosetRendererBuf2 {
	private final ProgramPreferences programPreferences;
	private CameraHandler cameraHandler;
	private final VertRendererThingBuf vertRendererThing;
	private TextureThing textureThing;
	private RenderModel renderModel;
	private TimeEnvironmentImpl renderEnv;
	private EditorColorPrefs colorPrefs;
	private ModelView modelView;
	private int levelOfDetail = -1;
	private float[] rgba = new float[4];

	private Vec2 uvTemp = new Vec2();
	private Mat4 uvTransform = new Mat4();

	VertexBuffers vertexBuffers = new VertexBuffers();

//	ShaderThing shaderThing = new ShaderThing();
//	VertexBuffers dotBuffers = new VertexBuffers();

	boolean texLoaded = true;
	public GeosetRendererBuf2(CameraHandler cameraHandler, ProgramPreferences programPreferences){
		this.cameraHandler = cameraHandler;
		this.programPreferences = programPreferences;
		this.colorPrefs = ProgramGlobals.getEditorColorPrefs();
		vertRendererThing = new VertRendererThingBuf((float) (cameraHandler.sizeAdj() * 4));

//		initShaderThing();


//		System.out.println("Geometry "+GL20.glCreateShader(GL32.GL_GEOMETRY_SHADER));
//		System.out.println("Fragment "+GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER));
	}

	public void initShaderThing() {
		try {
//			shaderThing.init();
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	public GeosetRendererBuf2 updateModel(RenderModel renderModel, ModelView modelView, TextureThing textureThing) {
		this.renderModel = renderModel;
		if(renderModel != null){
			renderEnv = renderModel.getTimeEnvironment();
			this.modelView = modelView;
			this.textureThing = textureThing;
		} else {
			renderEnv = null;
			this.modelView = null;
			this.textureThing = null;
		}
		return this;
	}

	public GeosetRendererBuf2 doRender(SimpleDiffuseShaderPipeline pipeline, boolean renderTextures, boolean wireFrame, boolean showNormals, boolean show3dVerts){
		vertexBuffers.resetIndex();
		int formatVersion = modelView.getModel().getFormatVersion();

		for (Geoset geo : modelView.getVisibleGeosets()) {
//			System.out.println("rendering geoset: " + geo);
//			if(modelView.getHighlightedGeoset() == geo){
//				drawHighlightedGeosets(formatVersion, renderTextures);
//			} else {
//			}
			renderGeoset1(geo, pipeline, formatVersion, renderTextures, wireFrame);
//			System.out.println("rendered geoset: " + geo + "!!");

//			if (showNormals) {
//				glDepthMask(true);
//				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
//				glTexEnvi(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_BLEND);
//				glDisable(GL_TEXTURE_2D);
//				renderNormals(geo, formatVersion);
//				shaderThing.glDisableIfNeeded(GL_TEXTURE_2D);
//			}
//			if (show3dVerts) {
//				GL11.glDepthMask(true);
//				GL11.glDepthMask(true);
//				GL11.glEnable(GL11.GL_CULL_FACE);
//
//				glDisable(GL_ALPHA_TEST);
//				glDisable(GL_TEXTURE_2D);
//				glDisable(GL_SHADE_MODEL);
//				shaderThing.glDisableIfNeeded(GL_ALPHA_TEST);
//				shaderThing.glDisableIfNeeded(GL_TEXTURE_2D);
//				shaderThing.glDisableIfNeeded(GL_SHADE_MODEL);
////				disableGlThings(GL_ALPHA_TEST, GL_TEXTURE_2D, GL_SHADE_MODEL);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//				GL11.glEnable(GL11.GL_BLEND);
//				shaderThing.glEnableIfNeeded(GL11.GL_BLEND);
//				renderVertDots(geo, formatVersion);
//			}
		}
		return this;
	}

	private void drawHighlightedGeosets(int formatVersion, boolean renderTextures) {
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
//		shaderThing.glDisableIfNeeded(GL11.GL_TEXTURE_2D);
		if (programPreferences != null && programPreferences.getHighlighTriangleColor() != null) {
			final Color highlightTriangleColor = programPreferences.getHighlighTriangleColor();
			glColor3f(highlightTriangleColor.getRed() / 255f, highlightTriangleColor.getGreen() / 255f, highlightTriangleColor.getBlue() / 255f);
		} else {
			glColor3f(1f, 3f, 1f);
		}
//		renderGeoset(modelView.getHighlightedGeoset(), formatVersion, true, renderTextures, false);
	}

	// ToDo investigate why transparent Geosets don't render when renderTextures is false
	private void renderGeoset1(Geoset geo, SimpleDiffuseShaderPipeline pipeline, int formatVersion, boolean renderTextures, boolean wireFrame) {


		if (wireFrame) {
			pipeline.glDisableIfNeeded(GL_CULL_FACE);
			pipeline.glDisableIfNeeded(GL_ALPHA_TEST);
			GL11.glDepthMask(false);
		} else {
			pipeline.glEnableIfNeeded(GL_CULL_FACE);
			pipeline.glEnableIfNeeded(GL_ALPHA_TEST);
		}
		if (texLoaded && renderTextures) {
			pipeline.glEnableIfNeeded(GL11.GL_BLEND);
			pipeline.glEnableIfNeeded(GL11.GL_TEXTURE_2D);
		} else {
			pipeline.glDisableIfNeeded(GL11.GL_BLEND);
			pipeline.glDisableIfNeeded(GL_TEXTURE_2D);
		}
		if (modelView.shouldRender(geo)) {
			renderGeoset(geo, pipeline, formatVersion, false, renderTextures, wireFrame);
		}
	}

	private void renderGeoset(Geoset geo, SimpleDiffuseShaderPipeline pipeline, int formatVersion, boolean overriddenColors, boolean renderTextures, boolean wireFrame) {
		if (!correctLoD(geo, formatVersion) || geo.getVertices().isEmpty()) return;

		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);

		if (renderGeoset != null) {
			Vec4 renderColor = renderGeoset.getRenderColor();
			if (renderColor.w < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
				return;
			}


			if (overriddenColors) {
				resetColorsA(1f);
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
			} else {
				getStandardColors(renderColor, 1, FilterMode.TRANSPARENT);
			}

			Mat4 uvTransform = this.uvTransform.setIdentity();
//			vertexBuffers.resetIndex();
//			vertexBuffers.ensureCapacity(geo.getTriangles().size() * 3);

			int renderedVertices = 0;
			float[] color;
			for (Triangle tri : geo.getTriangles()) {
				if (programPreferences != null && !renderTextures && cameraHandler.isOrtho()) {
					if(wireFrame){
						color = getTriLineColor(tri);
					} else {
						color = getTriAreaColor(tri);
					}
				} else {
					color = rgba;
				}
				GeosetVertex[] verts = tri.getVerts();
				if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
					for(GeosetVertex vertex : tri.getVerts()){
						RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
						Vec3 renderPos = renderVert.getRenderPos();
						Vec3 renderNorm = renderVert.getRenderNorm();

						Vec2 texcoord = getUV(0, vertex, uvTransform);
						pipeline.glVertex3f(renderPos);
						pipeline.glNormal3f(renderNorm);
						pipeline.glTexCoord2f(texcoord);
						pipeline.glColor4f(color);
//						vertexBuffers.setMultiple(renderPos, renderNorm, texcoord, color);
						renderedVertices++;
					}
				}
			}


			Material material = geo.getMaterial();
//			for (int i = 0; i < material.getLayers().size(); i++) {
			for (int i = 0; i < 1 && i < material.getLayers().size(); i++) {
				if (ModelUtils.isShaderStringSupported(formatVersion)
						&& (material.getShaderString() != null)
						&& (material.getShaderString().length() > 0)
						&& (i > 0)) {
					break; // HD-materials is not supported
				}
				Layer layer = material.getLayers().get(i);

				Bitmap bitmap = bindTexture(formatVersion, material, layer);

				if (overriddenColors) {
					resetColorsA(1f);
//					GL11.glDisable(GL11.GL_ALPHA_TEST);
				} else {
					getStandardColors(renderColor, layer.getRenderVisibility(renderEnv), layer.getFilterMode());
				}


//				if(renderTextures){
//					uvTransform = getUVTransform(layer);
//					int renderedTVs = 0;
//					for (Triangle tri : geo.getTriangles()) {
//						GeosetVertex[] verts = tri.getVerts();
//						if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
//							color = rgba;
//							for(GeosetVertex vertex : tri.getVerts()){
//
//								if(layer.getTextureAnim() != null){
//									Vec2 texcoord = getUV(layer.getCoordId(), vertex, uvTransform);
//									vertexBuffers.setTexCoordAlt(renderedTVs, texcoord);
//								}
//								vertexBuffers.setColorRGBA(renderedTVs, color);
//								renderedTVs++;
//							}
//						}
//					}
//
//				}

//				System.out.println("starting setting states");
//
//				glEnableClientState(GL_VERTEX_ARRAY);
//				glEnableClientState(GL_NORMAL_ARRAY);
//				glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//				glEnableClientState(GL_COLOR_ARRAY);
//
//				System.out.println("starting binding");
//
//				shaderThing.start(textureThing.getTextureID(bitmap));
////				GL20.glVertexAttribPointer(0, 3, false, 0, vertexBuffers.getPositions());
//				shaderThing.bindPos(vertexBuffers.getPositions());
////				GL20.glVertexAttribPointer(1, 3, false, 0, vertexBuffers.getNormals());
//				shaderThing.bindNorm(vertexBuffers.getNormals());
////				glNormalPointer(0, vertexBuffers.getNormals());
//
//				if(renderTextures && layer.getTextureAnim() != null){
////					GL20.glVertexAttribPointer(2, 2, false, 0, vertexBuffers.getTexCoordsAlt());
//					shaderThing.bindTexture(vertexBuffers.getTexCoordsAlt());
////					glTexCoordPointer(2, 0, vertexBuffers.getTexCoordsAlt());
//				} else {
////					GL20.glVertexAttribPointer(2, 2, false, 0, vertexBuffers.getTexCoords());
//					shaderThing.bindTexture(vertexBuffers.getTexCoords());
////					glTexCoordPointer(2, 0, vertexBuffers.getTexCoords());
//				}
//
//				shaderThing.bindColor(vertexBuffers.getColors());
////				GL20.glVertexAttribPointer(3, 4, false, 0, vertexBuffers.getColors());
//
//
//
////				glVertexPointer(3, 0, vertexBuffers.getPositions());
////				glNormalPointer(0, vertexBuffers.getNormals());
////
////				if(renderTextures && layer.getTextureAnim() != null){
////					glTexCoordPointer(2, 0, vertexBuffers.getTexCoordsAlt());
////				} else {
////					glTexCoordPointer(2, 0, vertexBuffers.getTexCoords());
////				}
////
////				glColorPointer(4, 0, vertexBuffers.getColors());
//				System.out.println("starting painting");
//				glDrawArrays(GL_TRIANGLES, 0, renderedVertices);
//				System.out.println("done painting");
//				System.out.println("ending shader");
//				shaderThing.end();
//
//
//				glDisableClientState(GL_VERTEX_ARRAY);
//				glDisableClientState(GL_NORMAL_ARRAY);
//				glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//				glDisableClientState(GL_COLOR_ARRAY);
//				System.out.println("disabled states");
			}
		}
	}

	private float getGeosetAnimVisibility(GeosetAnim geosetAnim) {
		if (geosetAnim != null) {
			return geosetAnim.getRenderVisibility(renderEnv);
		}
		return 1.0f;
	}

	private Bitmap bindTexture(int formatVersion, Material material, Layer layer) {
		Bitmap tex = layer.getRenderTexture(renderEnv, modelView.getModel());

		if (tex != null) {
			textureThing.bindLayerTexture(layer, tex, formatVersion, material, 0);
		}
		return tex;
	}


	private void renderMesh3(Geoset geo, Layer layer, boolean renderTextures, boolean wireFrame) {
		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		if (renderGeoset != null) {
			Mat4 uvTransform = getUVTransform(layer);
			vertexBuffers.resetIndex();
			vertexBuffers.ensureCapacity(geo.getTriangles().size() * 3);

			int renderedVertices = 0;
			float[] color;
			for (Triangle tri : geo.getTriangles()) {
				if (programPreferences != null && !renderTextures && cameraHandler.isOrtho()) {
					if(wireFrame){
						color = getTriLineColor(tri);
					} else {
						color = getTriAreaColor(tri);
					}
				} else {
					color = rgba;
				}
				GeosetVertex[] verts = tri.getVerts();
				if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
					for(GeosetVertex vertex : tri.getVerts()){
						RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
						Vec3 renderPos = renderVert.getRenderPos();
						Vec3 renderNorm = renderVert.getRenderNorm();

						Vec2 texcoord = getUV(layer.getCoordId(), vertex, uvTransform);
						vertexBuffers.setMultiple(renderPos, renderNorm, texcoord, color);
						renderedVertices++;
					}
				}
			}


			glEnableClientState(GL_VERTEX_ARRAY);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glEnableClientState(GL_NORMAL_ARRAY);
			glEnableClientState(GL_COLOR_ARRAY);

			glVertexPointer(3, 0, vertexBuffers.getPositions());
			glTexCoordPointer(2, 0, vertexBuffers.getTexCoords());
			glNormalPointer(0, vertexBuffers.getNormals());
			glColorPointer(4, 0, vertexBuffers.getColors());

			glDrawArrays(GL_TRIANGLES, 0, renderedVertices);

			glDisableClientState(GL_VERTEX_ARRAY);
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			glDisableClientState(GL_NORMAL_ARRAY);
			glDisableClientState(GL_COLOR_ARRAY);
		}
	}

//	private void renderGeoset(Geoset geo, int formatVersion, boolean overriddenColors, boolean renderTextures, boolean wireFrame) {
//		if (!correctLoD(geo, formatVersion) || geo.getVertices().isEmpty()) return;
//
//		GeosetAnim geosetAnim = geo.getGeosetAnim();
//		Vec3 renderColor = null;
//		float geosetAnimVisibility = 1;
//		if (geosetAnim != null) {
//			geosetAnimVisibility = geosetAnim.getRenderVisibility(renderEnv);
//			// do not show invisible geosets
//			if (geosetAnimVisibility < RenderModel.MAGIC_RENDER_SHOW_CONSTANT) {
////				System.out.println("Wont render");
//				return;
//			}
//			renderColor = geosetAnim.getRenderColor(renderEnv);
//		}
//
//		Material material = geo.getMaterial();
//		for (int i = 0; i < material.getLayers().size(); i++) {
//			if (ModelUtils.isShaderStringSupported(formatVersion)
//					&& (material.getShaderString() != null)
//					&& (material.getShaderString().length() > 0)
//					&& (i > 0)) {
//				break; // HD-materials is not supported
//			}
//			Layer layer = material.getLayers().get(i);
//
//			bindTexture(formatVersion, material, layer);
//
//			if (overriddenColors) {
//				getStandardColors(null, 1f, FilterMode.NONE);
//				GL11.glDisable(GL11.GL_ALPHA_TEST);
//			} else {
//				getStandardColors(renderColor, geosetAnimVisibility * layer.getRenderVisibility(renderEnv), layer.getFilterMode());
//			}
//
//			renderMesh3(geo, layer, renderTextures, wireFrame);
//		}
//	}
//
//	private void bindTexture(int formatVersion, Material material, Layer layer) {
//		Bitmap tex = layer.getRenderTexture(renderEnv, modelView.getModel());
//
//		if (tex != null) {
//			textureThing.bindLayerTexture(layer, tex, formatVersion, material);
//		}
//	}
//
//
//	private void renderMesh3(Geoset geo, Layer layer, boolean renderTextures, boolean wireFrame) {
//		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
//		if (renderGeoset != null) {
//			Mat4 uvTransform = getUVTransform(layer);
//			vertexBuffers.resetIndex();
//			vertexBuffers.ensureCapacity(geo.getTriangles().size() * 3);
//
//			int renderedVertices = 0;
//			float[] color;
//			for (Triangle tri : geo.getTriangles()) {
//				if (programPreferences != null && !renderTextures && cameraHandler.isOrtho()) {
//					if(wireFrame){
//						color = getTriLineColor(tri);
//					} else {
//						color = getTriAreaColor(tri);
//					}
//				} else {
//					color = rgba;
//				}
//				GeosetVertex[] verts = tri.getVerts();
//				if (!modelView.isHidden(verts[0]) && !modelView.isHidden(verts[1]) && !modelView.isHidden(verts[2])) {
//					for(GeosetVertex vertex : tri.getVerts()){
//						RenderGeoset.RenderVert renderVert = renderGeoset.getRenderVert(vertex);
//						Vec3 renderPos = renderVert.getRenderPos();
//						Vec3 renderNorm = renderVert.getRenderNorm();
//
//						Vec2 texcoord = getUV(layer, vertex, uvTransform);
//						vertexBuffers.setMultiple(renderPos, renderNorm, texcoord, color);
//						renderedVertices++;
//					}
//				}
//			}
//
//
//			glEnableClientState(GL_VERTEX_ARRAY);
//			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
//			glEnableClientState(GL_NORMAL_ARRAY);
//			glEnableClientState(GL_COLOR_ARRAY);
//
//			glVertexPointer(3, 0, vertexBuffers.getPositions());
//			glTexCoordPointer(2, 0, vertexBuffers.getTexCoords());
//			glNormalPointer(0, vertexBuffers.getNormals());
//			glColorPointer(4, 0, vertexBuffers.getColors());
//
//			glDrawArrays(GL_TRIANGLES, 0, renderedVertices);
//
//			glDisableClientState(GL_VERTEX_ARRAY);
//			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
//			glDisableClientState(GL_NORMAL_ARRAY);
//			glDisableClientState(GL_COLOR_ARRAY);
//		}
//	}

	private Vec2 getUV(int coordId, GeosetVertex vertex, Mat4 uvTransform) {
		List<Vec2> uvs = vertex.getTverts();
		if (coordId >= uvs.size()) {
			coordId = uvs.size() - 1;
		}
		if(uvTransform != null){
			uvTemp.set(uvs.get(coordId));
			uvTemp.transform2(uvTransform);
			return uvTemp;
		}
		return uvs.get(coordId);
	}

	private Mat4 getUVTransform(Layer layer) {
		if(layer.getTextureAnim() != null){
			uvTransform.setIdentity();

			uvTransform.fromRotationTranslationScale(
					layer.getTextureAnim().getInterpolatedQuat(renderEnv, MdlUtils.TOKEN_ROTATION, Quat.IDENTITY),
					layer.getTextureAnim().getInterpolatedVector(renderEnv, MdlUtils.TOKEN_TRANSLATION, Vec3.ZERO),
					layer.getTextureAnim().getInterpolatedVector(renderEnv, MdlUtils.TOKEN_SCALING, Vec3.ONE)
			);
			return uvTransform;
		}
		return null;
	}


	private float[] getStandardColors(Vec3 renderColor, float alphaValue, FilterMode filterMode) {
		if (renderColor != null) {
			if (filterMode == FilterMode.ADDITIVE) {
				rgba[0] = renderColor.x * alphaValue;
				rgba[1] = renderColor.y * alphaValue;
				rgba[2] = renderColor.z * alphaValue;
			} else {
				rgba[0] = renderColor.x;
				rgba[1] = renderColor.y;
				rgba[2] = renderColor.z;
			}
		} else {
			rgba[0] = 1f;
			rgba[1] = 1f;
			rgba[2] = 1f;
		}
		rgba[3] = alphaValue;
		return rgba;
	}


	private float[] getStandardColors(Vec4 renderColor, float layerAlpha, FilterMode filterMode) {
		if (renderColor != null) {
			if (filterMode == FilterMode.ADDITIVE) {
				rgba[0] = renderColor.x * layerAlpha * renderColor.w;
				rgba[1] = renderColor.y * layerAlpha * renderColor.w;
				rgba[2] = renderColor.z * layerAlpha * renderColor.w;
			} else {
				rgba[0] = renderColor.x;
				rgba[1] = renderColor.y;
				rgba[2] = renderColor.z;
			}
			rgba[3] = layerAlpha * renderColor.w;
		} else {
			rgba[0] = 1f;
			rgba[1] = 1f;
			rgba[2] = 1f;
			rgba[3] = layerAlpha;
		}
		return rgba;
	}

	private float[] resetColorsA(float layerAlpha) {
		rgba[0] = 1f;
		rgba[1] = 1f;
		rgba[2] = 1f;
		rgba[3] = layerAlpha;
		return rgba;
	}



	private void renderNormals(Geoset geo, int formatVersion) {
		glColor3f(1f, 1f, 3f);
		rgba[0] = 1f;
		rgba[1] = 1f;
		rgba[2] = 3f;
		rgba[3] = 1f;

		RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);
		if (correctLoD(geo, formatVersion) && renderGeoset != null) {

			int renderedVertices = 0;
			Vec3 normEndPos = new Vec3();

			vertexBuffers.ensureCapacity(geo.getVertices().size() * 2);
			float sizeFactor = (float) (6 * 4 * cameraHandler.sizeAdj());

			for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
				Vec3 renderPos = renderVert.getRenderPos();
				Vec3 renderNorm = renderVert.getRenderNorm();
				normEndPos.set(renderNorm).scale(sizeFactor).add(renderPos);

				vertexBuffers.setMultiple(renderPos, renderNorm, rgba);
				vertexBuffers.setMultiple(normEndPos, renderNorm, rgba);
				renderedVertices++;
			}

			glEnableClientState(GL_VERTEX_ARRAY);
			glEnableClientState(GL_COLOR_ARRAY);

			glVertexPointer(3, 0, vertexBuffers.getPositions());
			glColorPointer(3, 0, vertexBuffers.getColors());

			glDrawArrays(GL_LINES, 0, renderedVertices * 2);

			glDisableClientState(GL_VERTEX_ARRAY);
			glDisableClientState(GL_COLOR_ARRAY);
		}
	}


	private void renderVertDots(Geoset geo, int formatVersion) {

		if (correctLoD(geo, formatVersion) && modelView.shouldRender(geo)) {
			vertRendererThing.updateSquareSize((float) (cameraHandler.sizeAdj() * 4));
			RenderGeoset renderGeoset = renderModel.getRenderGeoset(geo);

			if (renderGeoset != null) {
				int renderedVertices = 0;
				vertexBuffers.resetIndex();
				vertexBuffers.ensureCapacity(geo.getVertices().size() * 6);

//				glPolygonMode(GL_FRONT, GL_FILL);
				glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
				glDisable(GL_DEPTH_TEST);

				EditorColorPrefs colorPrefs = ProgramGlobals.getEditorColorPrefs();
				for (RenderGeoset.RenderVert renderVert : renderGeoset.getRenderVerts()) {
					if (renderVert != null && !modelView.isHidden(renderVert.getVertex())) {
						float[] rgba = getRGBA(colorPrefs, renderVert);
						VertRendererThingBuf thingBuf = vertRendererThing.transform(cameraHandler.getInverseCameraRotation(), renderVert.getRenderPos());
						thingBuf.doGlGeom(vertexBuffers, rgba);
						renderedVertices++;
					}
				}


				glEnableClientState(GL_VERTEX_ARRAY);
				glEnableClientState(GL_COLOR_ARRAY);

				glVertexPointer(3, 0, vertexBuffers.getPositions());
				glColorPointer(4, 0, vertexBuffers.getColors());

				glDrawArrays(GL_TRIANGLES, 0, renderedVertices * 6);

				glDisableClientState(GL_VERTEX_ARRAY);
				glDisableClientState(GL_COLOR_ARRAY);

			}
		}

	}

	private float[] getRGBA(EditorColorPrefs colorPrefs, RenderGeoset.RenderVert renderVert) {
		float[] rgba;
		if (modelView.isEditable(renderVert.getVertex()) && modelView.isSelected(renderVert.getVertex())) {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX_SELECTED);
		} else if (modelView.isEditable(renderVert.getVertex())) {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX);
		} else {
			rgba = colorPrefs.getColorComponents(ColorThing.VERTEX_UNEDITABLE);
		}
		return rgba;
	}

	private float[] getTriAreaColor(Triangle triangle) {
		float[] color;
		if (triangle.getGeoset() == modelView.getHighlightedGeoset()) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA_HIGHLIGHTED);
		} else if (triFullySelected(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA_SELECTED);
		} else if (triFullyEditable(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA);
		} else {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_AREA_UNEDITABLE);
		}

		return color;
	}

	private float[] getTriLineColor(Triangle triangle) {
		float[] color;
		if (triangle.getGeoset() == modelView.getHighlightedGeoset()) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_HIGHLIGHTED);
		} else if (triFullySelected(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_SELECTED);
		} else if (triFullyEditable(triangle)) {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE);
		} else {
			color = colorPrefs.getColorComponents(ColorThing.TRIANGLE_LINE_UNEDITABLE);
		}

		return color;
	}


	private boolean triFullySelected(Triangle triangle){
		return modelView.isSelected(triangle.get(0)) && modelView.isSelected(triangle.get(1)) && modelView.isSelected(triangle.get(2));
	}
	private boolean triFullyEditable(Triangle triangle){
		return modelView.isEditable(triangle.get(0)) && modelView.isEditable(triangle.get(1)) && modelView.isEditable(triangle.get(2));
	}
	private boolean correctLoD(Geoset geo, int formatVersion) {
		if ((ModelUtils.isLevelOfDetailSupported(formatVersion)) && (geo.getLevelOfDetailName() != null) && (geo.getLevelOfDetailName().length() > 0) && levelOfDetail > -1) {
			return geo.getLevelOfDetail() == levelOfDetail;
		}
		return true;
	}

	public boolean renderTextures() {
		return texLoaded && ((programPreferences == null) || programPreferences.textureModels());
	}

	private void enableGlThings(int... thing) {
		for (int t : thing) {
			glEnable(t);
		}
	}

	private void disableGlThings(int... thing) {
		for (int t : thing) {
			glDisable(t);
		}
	}
}
