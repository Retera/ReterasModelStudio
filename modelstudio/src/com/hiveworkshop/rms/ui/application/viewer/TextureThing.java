package com.hiveworkshop.rms.ui.application.viewer;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.ParticleEmitter2;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;
import com.hiveworkshop.rms.parsers.blp.GPUReadyTexture;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.nio.ByteBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_LIGHTING;
import static org.lwjgl.opengl.GL11.glEnable;

public class TextureThing {
	public static final boolean LOG_EXCEPTIONS = true;

	public static int loadTexture(final GPUReadyTexture texture, final Bitmap bitmap) {
		if (texture == null) {
			return -1;
		}
		final ByteBuffer buffer = texture.getBuffer();
		// You now have a ByteBuffer filled with the color data of each pixel.
		// Now just create a texture ID and bind it. Then you can load it using
		// whatever OpenGL method you want, for example:

		final int textureID = GL11.glGenTextures(); // Generate texture ID
		bindTexture(bitmap, textureID);

		// Setup texture scaling filtering
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		// Send texel data to OpenGL
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, texture.getWidth(), texture.getHeight(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		// Return the texture ID so we can bind it later again
		return textureID;
	}

	public static void bindTexture(Bitmap tex, Integer texture) {
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, tex.isWrapWidth() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, tex.isWrapHeight() ? GL11.GL_REPEAT : GL12.GL_CLAMP_TO_EDGE);
	}

	public static void loadToTexMap(ModelView modelView, ProgramPreferences programPreferences, HashMap<Bitmap, Integer> textureMap, final Bitmap tex) {
		if (textureMap.get(tex) == null) {
			String path = tex.getPath();
			if (!path.isEmpty() && !programPreferences.getAllowLoadingNonBlpTextures()) {
				path = path.replaceAll("\\.\\w+", "") + ".blp";
			}
			Integer texture = null;
			try {
				final DataSource workingDirectory = modelView.getModel().getWrappedDataSource();
				texture = loadTexture(BLPHandler.get().loadTexture2(workingDirectory, path, tex), tex);
			} catch (final Exception exc) {
				if (LOG_EXCEPTIONS) {
					exc.printStackTrace();
				}
			}
			if (texture != null) {
				textureMap.put(tex, texture);
			}
		}
	}

	public static void deleteAllTextures(HashMap<Bitmap, Integer> textureMap) {
		for (final Integer textureId : textureMap.values()) {
			GL11.glDeleteTextures(textureId);
		}
		textureMap.clear();
	}

	public static void bindLayerTexture(int textureMapSize, final Layer layer, final Bitmap tex, final Integer texture, final int formatVersion, final Material parent) {
		if (texture != null) {
			bindTexture(tex, texture);
		} else {
			if (textureMapSize > 0) {
				bindTexture(tex, 0);
			}
		}
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

	public static void bindParticleTexture(HashMap<Bitmap, Integer> textureMap, final ParticleEmitter2 particle2, final Bitmap tex, final Integer texture) {
		if (texture != null) {
			bindTexture(tex, texture);
		} else if (textureMap.size() > 0) {
			bindTexture(tex, 0);
		}
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
}
