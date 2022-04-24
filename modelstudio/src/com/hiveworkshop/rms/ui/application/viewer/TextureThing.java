package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.GPUReadyTexture;
import com.hiveworkshop.rms.ui.application.viewer.ObjectRenderers.ShaderPipeline;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.glEnable;

public class TextureThing {
	public static final boolean LOG_EXCEPTIONS = true;
	private final ProgramPreferences programPreferences;
	private final HashMap<Bitmap, Integer> textureMap = new HashMap<>();

	public TextureThing(ProgramPreferences programPreferences) {
		this.programPreferences = programPreferences;
	}

	public Integer loadToTexMap(EditableModel model, Bitmap tex) {
		if (tex != null && textureMap.get(tex) == null) {
			String path = tex.getRenderableTexturePath();
			if (!path.isEmpty() && !programPreferences.getAllowLoadingNonBlpTextures()) {
				path = path.replaceAll("\\.\\w+", "") + ".blp";
			}
			try {
				DataSource workingDirectory = model.getWrappedDataSource();
				Integer texture = loadTexture(BLPHandler.get().loadTexture2(workingDirectory, path), tex);
				textureMap.put(tex, texture);
				return texture;
			} catch (final Exception exc) {
				if (LOG_EXCEPTIONS) {
					exc.printStackTrace();
				}
			}
		}
		return getTextureID(tex);
	}

	public int loadTexture(final GPUReadyTexture texture, final Bitmap bitmap) {
		if (texture == null || bitmap == null) {
			return -1;
		}
		ByteBuffer buffer = texture.getBuffer();
		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:

		int textureID = GL11.glGenTextures(); // Generate texture ID
//		System.out.println("texture: " + bitmap.getName() + ", id: " + textureID);
		int width = texture.getWidth();
		int height = texture.getHeight();
		boolean wrapW = bitmap.isWrapWidth();
		boolean wrapH = bitmap.isWrapHeight();

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
		GL42.glTexStorage2D(GL11.GL_TEXTURE_2D, 1, GL11.GL_RGBA8, width, height);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		GL30.glGenerateMipmap(GL11.GL_TEXTURE_2D);  //Generate num_mipmaps number of mipmaps here.
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapW ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapH ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);


		// Setup texture scaling filtering
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		// Send texel data to OpenGL
//		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
	}
//	public int loadTexture(final GPUReadyTexture texture, final Bitmap bitmap) {
//		if (texture == null || bitmap == null) {
//			return -1;
//		}
//		ByteBuffer buffer = texture.getBuffer();
//		// You now have a ByteBuffer filled with the color data of each pixel.
//		// Now just create a texture ID and bind it. Then you can load it using
//		// whatever OpenGL method you want, for example:
//
//		int textureID = GL11.glGenTextures(); // Generate texture ID
//		// Test the texture (necessary?)
//		GL13.glActiveTexture(GL13.GL_TEXTURE0);
//		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
//
//		// Setup texture scaling filtering
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
//		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
//
//		// Send texel data to OpenGL
//		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
//
//		// Return the texture ID so we can bind it later again
//		return textureID;
//	}

	public void bindTexture(Bitmap bitmap, int textureSlot) {
		int textureHandle = getTextureID(bitmap);
//		printThing("texture: " + bitmap.getName() + ", id: " + textureHandle, 500);
		boolean wrapW = bitmap != null && bitmap.isWrapWidth();
		boolean wrapH = bitmap != null && bitmap.isWrapHeight();
		bindTexture(textureHandle, textureSlot, wrapW, wrapH);
	}

	public void loadAndBindTexture(EditableModel model, Bitmap bitmap, int textureSlot) {
//		int textureHandle = getTextureID(bitmap);
		int textureHandle = loadToTexMap(model, bitmap);
//		printThing("texture: " + bitmap.getName() + ", id: " + textureHandle, 500);
		boolean wrapW = bitmap != null && bitmap.isWrapWidth();
		boolean wrapH = bitmap != null && bitmap.isWrapHeight();
		bindTexture(textureHandle, textureSlot, wrapW, wrapH);
	}


	public void bindTexture(Integer texture, int textureSlot, boolean wrapW, boolean wrapH) {
		if(!textureMap.isEmpty()){
			GL13.glActiveTexture(GL13.GL_TEXTURE0 + textureSlot);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
//			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, wrapW ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
//			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, wrapH ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);

//			GL13.glClientActiveTexture(GL13.GL_TEXTURE0 + textureSlot);
		}
	}

	long lastPrint = 0;
	private void printThing(String text, int ms){
		if(lastPrint < System.currentTimeMillis() || true){
			lastPrint = System.currentTimeMillis() + ms;
			System.out.println(text);
		}
	}


	public void reMakeTextureMap(EditableModel model) {
		deleteAllTextures(textureMap);
//		loadGeosetMaterials();
		loadModelTextures(model);
	}

	public void clearTextureMap() {
		deleteAllTextures(textureMap);
	}
	public void loadGeosetMaterials(EditableModel model) {
		final List<Geoset> geosets = model.getGeosets();
		for (final Geoset geo : geosets) {
			for (int i = 0; i < geo.getMaterial().getLayers().size(); i++) {
				if (ModelUtils.isShaderStringSupported(model.getFormatVersion())) {
					if ((geo.getMaterial().getShaderString() != null) && (geo.getMaterial().getShaderString().length() > 0)) {
						if (i > 0) {
							break;
						}
					}
				}
				final Layer layer = geo.getMaterial().getLayers().get(i);
				if (layer.getTextureBitmap() != null) {
					loadToTexMap(model, layer.getTextureBitmap());
				}
				if (layer.getTextures() != null) {
					for (final Bitmap tex : layer.getTextures()) {
						loadToTexMap(model, tex);
					}
				}
			}
		}
	}
	public void loadModelTextures(EditableModel model) {
		List<Bitmap> textures = model.getTextures();
		for (Bitmap texture : textures) {
			loadToTexMap(model, texture);
		}
	}

	public static void deleteAllTextures(HashMap<Bitmap, Integer> textureMap) {
		for (final Integer textureId : textureMap.values()) {
			GL11.glDeleteTextures(textureId);
		}
		textureMap.clear();
	}

	public int getTextureID(Bitmap tex){
		Integer texture = textureMap.get(tex);
		if (texture != null) {
			return texture;
		}
		return 0;
	}

	public void bindLayerTexture(Layer layer, Bitmap tex, int formatVersion, Material parent, int textureSlot) {
		bindTexture(tex, textureSlot);

		boolean depthMask = false;
		switch (layer.getFilterMode()) {
			case BLEND -> setBlendWOAlpha(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			case ADDITIVE, ADDALPHA -> setBlendWOAlpha(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			case MODULATE -> setBlendWOAlpha(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			case MODULATE2X -> setBlendWOAlpha(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			case NONE -> {
				GL11.glDisable(GL11.GL_ALPHA_TEST);
				GL11.glDisable(GL11.GL_BLEND);
				depthMask = true;
			}
			case TRANSPARENT -> {
				GL11.glEnable(GL11.GL_ALPHA_TEST);
				GL11.glAlphaFunc(GL11.GL_GREATER, 0.75f);
				GL11.glDisable(GL11.GL_BLEND);
				depthMask = true;
			}
		}
		if (layer.getTwoSided() || ((ModelUtils.isShaderStringSupported(formatVersion)) && parent.getTwoSided())) {
			GL11.glDisable(GL11.GL_CULL_FACE);
		} else {
			GL11.glEnable(GL11.GL_CULL_FACE);
		}
		if (layer.getNoDepthTest()) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}
		if (layer.getNoDepthSet()) {
			GL11.glDepthMask(false);
		} else {
			GL11.glDepthMask(depthMask);
		}
		if (layer.getUnshaded()) {
			GL11.glDisable(GL_LIGHTING);
		} else {
			glEnable(GL_LIGHTING);
		}
	}

	public void bindParticleTexture(ParticleEmitter2 particle2, Bitmap tex) {
		bindTexture(tex, 0);

		switch (particle2.getFilterMode()) {
			case BLEND -> setBlendWOAlpha(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			case ADDITIVE, ALPHAKEY -> setBlendWOAlpha(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			case MODULATE -> setBlendWOAlpha(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			case MODULATE2X -> setBlendWOAlpha(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
		}
		if (particle2.getUnshaded()) {
			GL11.glDisable(GL_LIGHTING);
		} else {
			glEnable(GL_LIGHTING);
		}
	}

	private static void setBlendWOAlpha(int sFactor, int dFactor) {
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(sFactor, dFactor);
	}





	public void bindLayer(ShaderPipeline pipeline, ParticleEmitter2 particle2, Bitmap tex, Integer texture) {
		bindTexture(tex, texture);
		switch (particle2.getFilterMode()) {
			case BLEND -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			case ADDITIVE, ALPHAKEY -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}
			case MODULATE -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			}
			case MODULATE2X -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			}
		}
		if (particle2.getUnshaded()) {
			pipeline.glDisableIfNeeded(GL11.GL_LIGHTING);
		}
		else {
			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
		}
	}






	public void bindLayerTexture(ShaderPipeline pipeline, Layer layer, boolean doSetUpFilterMode, int textureSlot, boolean twoSided, Bitmap tex) {
		int textureHandle = getTextureID(tex);
//		printThing("texture: " + tex.getName() + ", id: " + textureHandle + ", slot: " + textureSlot, 1000);
		bindTexture(tex, textureSlot);

		if (doSetUpFilterMode) {
			bindLayer(pipeline, layer);
			if (twoSided) {
				GL11.glDisable(GL11.GL_CULL_FACE);
			} else {
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
		}
	}


	public void loadAndBindLayerTexture(ShaderPipeline pipeline, EditableModel model, Layer layer, boolean doSetUpFilterMode, int textureSlot, boolean twoSided, Bitmap tex) {
		int textureHandle = getTextureID(tex);
//		printThing("texture: " + tex.getName() + ", id: " + textureHandle + ", slot: " + textureSlot, 1000);
		loadAndBindTexture(model, tex, textureSlot);

		if (doSetUpFilterMode) {
			bindLayer(pipeline, layer);
			if (twoSided) {
				GL11.glDisable(GL11.GL_CULL_FACE);
			} else {
				GL11.glEnable(GL11.GL_CULL_FACE);
			}
		}
	}

	public void bindLayer(ShaderPipeline pipeline, Layer layer) {
		boolean depthMask = false;
		switch (layer.getFilterMode()) {
			case BLEND -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			}
			case ADDITIVE, ADDALPHA -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
			}
			case MODULATE -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_ZERO, GL11.GL_SRC_COLOR);
			}
			case MODULATE2X -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glEnable(GL11.GL_BLEND);
				GL11.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR);
			}
			case NONE -> {
				pipeline.glDisableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glDisable(GL11.GL_BLEND);
				depthMask = true;
			}
			case TRANSPARENT -> {
				pipeline.glEnableIfNeeded(GL11.GL_ALPHA_TEST);
				GL11.glAlphaFunc(GL11.GL_GREATER, 0.75f);
				GL11.glDisable(GL11.GL_BLEND);
				depthMask = true;
			}
		}

		if (layer.getNoDepthTest()) {
			GL11.glDisable(GL11.GL_DEPTH_TEST);
		} else {
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		}

		if (layer.getNoDepthSet()) {
			GL11.glDepthMask(false);
		} else {
			GL11.glDepthMask(depthMask);
		}
//		GL11.glColorMask(layer.getFilterMode() == FilterMode.ADDITIVE, layer.getFilterMode() == FilterMode.ADDITIVE,
//				layer.getFilterMode() == FilterMode.ADDITIVE, layer.getFilterMode() == FilterMode.ADDITIVE);
		if (layer.getUnshaded()) {
			pipeline.glDisableIfNeeded(GL11.GL_LIGHTING);
		}
		else {
			pipeline.glEnableIfNeeded(GL11.GL_LIGHTING);
		}
	}
}
