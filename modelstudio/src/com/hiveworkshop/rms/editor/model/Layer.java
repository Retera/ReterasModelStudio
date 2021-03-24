package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Layers for MDLToolkit/MatrixEater.
 * <p>
 * Eric Theller 3/8/2012
 */
public class Layer extends TimelineContainer implements Named {
	private FilterMode filterMode = FilterMode.NONE;
	int textureId = -1;
	int TVertexAnimId = -1;
	private int coordId = 0;
	Bitmap texture;
	TextureAnim textureAnim;
	private double emissiveGain = 0;
	private Vec3 fresnelColor = new Vec3(1, 1, 1);
	private double fresnelOpacity = 0;
	private double fresnelTeamColor = 0;
	private double staticAlpha = 1;// Amount of static alpha (opacity)
	List<Bitmap> textures;
	boolean unshaded = false;
	boolean sphereEnvMap = false;
	boolean twoSided = false;
	boolean unfogged = false;
	boolean noDepthTest = false;
	boolean noDepthSet = false;
	boolean unlit = false;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + coordId;
		result = (prime * result) + TVertexAnimId;
		result = (prime * result) + ((animFlags == null) ? 0 : animFlags.hashCode());
		long temp;
		temp = Double.doubleToLongBits(emissiveGain);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fresnelOpacity);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(fresnelTeamColor);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((filterMode == null) ? 0 : filterMode.hashCode());
		// result = (prime * result) + ((flags == null) ? 0 : flags.hashCode());
		temp = Double.doubleToLongBits(staticAlpha);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((texture == null) ? 0 : texture.hashCode());
		result = (prime * result) + ((textureAnim == null) ? 0 : textureAnim.hashCode());
		result = (prime * result) + textureId;
		result = (prime * result) + ((textures == null) ? 0 : textures.hashCode());
		if (fresnelColor != null) {
			temp = Double.doubleToLongBits(fresnelColor.x);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(fresnelColor.y);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(fresnelColor.z);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
		}
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Layer other = (Layer) obj;
		if (coordId != other.coordId) {
			return false;
		}
		if (TVertexAnimId != other.TVertexAnimId) {
			return false;
		}
		if (animFlags == null) {
			if (other.animFlags != null) {
				return false;
			}
		} else if (!animFlags.equals(other.animFlags)) {
			return false;
		}
		if (Double.doubleToLongBits(emissiveGain) != Double.doubleToLongBits(other.emissiveGain)) {
			return false;
		}
		if (Double.doubleToLongBits(fresnelOpacity) != Double.doubleToLongBits(other.fresnelOpacity)) {
			return false;
		}
		if (Double.doubleToLongBits(fresnelTeamColor) != Double.doubleToLongBits(other.fresnelTeamColor)) {
			return false;
		}
		if (fresnelColor == null) {
			if (other.fresnelColor != null) {
				return false;
			}
		} else {
			if (other.fresnelColor == null) {
				return false;
			}
			if (!fresnelColor.equalLocs(other.fresnelColor)) {
				return false;
			}
		}
		if (filterMode == null) {
			if (other.filterMode != null) {
				return false;
			}
		} else if (!filterMode.equals(other.filterMode)) {
			return false;
		}

		if (emissiveGain != other.emissiveGain) {
			return false;
		}

		if (!fresnelColor.equalLocs(other.fresnelColor)) {
			return false;
		}

		if (fresnelOpacity != other.fresnelOpacity) {
			return false;
		}

		if (fresnelTeamColor != other.fresnelTeamColor) {
			return false;
		}

		if ((unshaded != other.unshaded) || (sphereEnvMap != other.sphereEnvMap)
				|| (twoSided != other.twoSided) || (unfogged != other.unfogged)
				|| (noDepthTest != other.noDepthTest) || (noDepthSet != other.noDepthSet)
				|| (unlit != other.unlit)) {
			return false;
		}

		if (Double.doubleToLongBits(staticAlpha) != Double.doubleToLongBits(other.staticAlpha)) {
			return false;
		}
		if (texture == null) {
			if (other.texture != null) {
				return false;
			}
		} else if (!texture.equals(other.texture)) {
			return false;
		}
		if (textureAnim == null) {
			if (other.textureAnim != null) {
				return false;
			}
		} else if (!textureAnim.equals(other.textureAnim)) {
			return false;
		}
		if (textureId != other.textureId) {
			return false;
		}
		if (textures == null) {
			return other.textures == null;
		} else {
			return textures.equals(other.textures);
		}
	}

	public Layer() {

	}

	public Layer(final String filterMode, final int textureId) {
		this.filterMode = FilterMode.nameToFilter(filterMode);
		this.textureId = textureId;
	}

	public Layer(final String filterMode, final Bitmap texture) {
		this.filterMode = FilterMode.nameToFilter(filterMode);
		this.texture = texture;
	}

	public Layer(final Layer other) {
		filterMode = other.filterMode;
		textureId = other.textureId;
		TVertexAnimId = other.TVertexAnimId;
		coordId = other.coordId;
		texture = other.texture;
		if (other.textureAnim != null) {
			textureAnim = new TextureAnim(other.textureAnim);
		} else {
			textureAnim = null;
		}
		staticAlpha = other.staticAlpha;
		emissiveGain = other.emissiveGain;
		fresnelColor = new Vec3(other.fresnelColor);
		fresnelOpacity = other.fresnelOpacity;
		fresnelTeamColor = other.fresnelTeamColor;
		unshaded = other.unshaded;
		sphereEnvMap = other.sphereEnvMap;
		twoSided = other.twoSided;
		unfogged = other.unfogged;
		noDepthTest = other.noDepthTest;
		noDepthSet = other.noDepthSet;
		unlit = other.unlit;

		for (AnimFlag<?> animFlag : other.getAnimFlags()) {
			add(AnimFlag.createFromAnimFlag(animFlag));
		}

		textures = new ArrayList<>();
		if (other.textures != null) {
//			for (final Bitmap bmp : other.textures) {
//				textures.add(new Bitmap(bmp));
//			}
			for (final Bitmap bmp : other.textures) {
				textures.add(bmp);
			}
		} else {
			textures = null;
		}
	}

	public Layer(final MdlxLayer layer) {
		this(layer.filterMode.toString(), layer.textureId);

		final int shadingFlags = layer.flags;
		if ((shadingFlags & 0x1) != 0) {
			unshaded = true;
		}
		if ((shadingFlags & 0x2) != 0) {
			sphereEnvMap = true;
		}
		if ((shadingFlags & 0x10) != 0) {
			twoSided = true;
		}
		if ((shadingFlags & 0x20) != 0) {
			unfogged = true;
		}
		if ((shadingFlags & 0x40) != 0) {
			noDepthTest = true;
		}
		if ((shadingFlags & 0x80) != 0) {
			noDepthSet = true;
		}
		if ((shadingFlags & 0x100) != 0) {
			unlit = true;
		}

		setTVertexAnimId(layer.textureAnimationId);
		setCoordId((int) layer.coordId);
		setStaticAlpha(layer.alpha);

		// > 800
		emissiveGain = layer.emissiveGain;
		// > 900
		setFresnelColor(new Vec3(ModelUtils.flipRGBtoBGR(layer.fresnelColor)));
		fresnelOpacity = layer.fresnelOpacity;
		fresnelTeamColor = layer.fresnelTeamColor;

		loadTimelines(layer);
	}

	public MdlxLayer toMdlx() {
		final MdlxLayer layer = new MdlxLayer();

		layer.filterMode = filterMode;

		if (unshaded) {
			layer.flags |= 0x1;
		}

		if (sphereEnvMap) {
			layer.flags |= 0x2;
		}

		if (twoSided) {
			layer.flags |= 0x10;
		}

		if (unfogged) {
			layer.flags |= 0x20;
		}

		if (noDepthTest) {
			layer.flags |= 0x40;
		}

		if (noDepthSet) {
			layer.flags |= 0x80;
		}

		if (unlit) {
			layer.flags |= 0x100;
		}

		layer.textureId = getTextureId();
		layer.textureAnimationId = getTVertexAnimId();
		layer.coordId = getCoordId();
		layer.alpha = (float) getStaticAlpha();

		// > 800
		layer.emissiveGain = layer.emissiveGain;
		// > 900
		layer.fresnelColor = ModelUtils.flipRGBtoBGR(fresnelColor.toFloatArray());
		layer.fresnelOpacity = (float) fresnelOpacity;
		layer.fresnelTeamColor = (float) fresnelTeamColor;

		timelinesToMdlx(layer);

		return layer;
	}

	public Bitmap firstTexture() {
		if (texture != null) {
			return texture;
		} else {
			if ((textures != null) && (textures.size() > 0)) {
				return textures.get(0);
			}
			return null;
		}
	}

	public Bitmap getRenderTexture(final AnimatedRenderEnvironment animatedRenderEnvironment,
			final EditableModel model) {
		final IntAnimFlag textureFlag = (IntAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);
		if ((textureFlag != null) && (animatedRenderEnvironment != null)) {
			if (animatedRenderEnvironment.getCurrentAnimation() == null) {
				if (textures.size() > 0) {
					return textures.get(0);
				} else {
					return texture;
				}
			}
			final Integer textureIdAtTime = textureFlag.interpolateAt(animatedRenderEnvironment);
			if (textureIdAtTime >= model.getTextures().size()) {
				return texture;
			}
			return model.getTextures().get(textureIdAtTime);
		} else {
			return texture;
		}
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) staticAlpha);
	}

	public void setTextureAnim(final TextureAnim texa) {
		textureAnim = texa;
	}

	public void setTextureAnim(final List<TextureAnim> list) {
		// Sets the texture anim reference to the one from
		// the list corresponding to the TVertexAnimId
		textureAnim = list.get(TVertexAnimId);
	}

	private final transient Map<Integer, Bitmap> ridiculouslyWrongTextureIDToTexture = new HashMap<>();

	public void buildTextureList(final EditableModel mdlr) {
		textures = new ArrayList<>();
		final IntAnimFlag txFlag = (IntAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);
		for (int i = 0; i < txFlag.getValues().size(); i++) {
			final int txId = txFlag.getValues().get(i);
			final Bitmap texture2 = mdlr.getTexture(txId);
			textures.add(texture2);
			ridiculouslyWrongTextureIDToTexture.put(txId, texture2);
		}
	}

	public void updateIds(final EditableModel mdlr) {
		textureId = mdlr.getTextureId(texture);
		TVertexAnimId = mdlr.getTextureAnimId(textureAnim);
		if (textures != null) {
			final IntAnimFlag txFlag = (IntAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);
			for (int i = 0; i < txFlag.getValues().size(); i++) {
				final Bitmap textureFoundFromDirtyId = ridiculouslyWrongTextureIDToTexture
						.get(txFlag.getValues().get(i));
				final int newerTextureId = mdlr.getTextureId(textureFoundFromDirtyId);
				txFlag.getValues().set(i, newerTextureId);
				ridiculouslyWrongTextureIDToTexture.put(newerTextureId, textureFoundFromDirtyId);
			}
		}
	}

	public void updateRefs(final EditableModel mdlr) {
		if ((textureId >= 0) && (textureId < mdlr.getTextures().size())) {
			texture = mdlr.getTexture(textureId);
		}
		if ((TVertexAnimId >= 0) && (TVertexAnimId < mdlr.texAnims.size())) {
			textureAnim = mdlr.texAnims.get(TVertexAnimId);
		}
		final IntAnimFlag txFlag = (IntAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);
		if (txFlag != null) {
			buildTextureList(mdlr);
		}
	}

	public boolean hasCoordId() {
		return coordId != 0;
	}

	public boolean hasTexAnim() {
		return TVertexAnimId != -1;
	}

	@Override
	public String getName() {
		if (texture != null) {
			return texture.getName() + " layer (mode " + filterMode + ") ";
		}
		return "multi-textured layer (mode " + filterMode + ") ";
	}

	@Override
	public void setName(String text) {
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public int getTextureId() {
		return textureId;
	}

	public void setTextureId(final int textureId) {
		this.textureId = textureId;
	}

	public int getTVertexAnimId() {
		return TVertexAnimId;
	}

	public void setTVertexAnimId(final int tVertexAnimId) {
		TVertexAnimId = tVertexAnimId;
	}

	public Bitmap getTextureBitmap() {
		return texture;
	}

	public void setTexture(final Bitmap texture) {
		this.texture = texture;
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(final double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public List<Bitmap> getTextures() {
		return textures;
	}

	public void setTextures(final List<Bitmap> textures) {
		this.textures = textures;
	}

	public TextureAnim getTextureAnim() {
		return textureAnim;
	}

	public void setFilterMode(final FilterMode filterMode) {
		this.filterMode = filterMode;
	}

	public FilterMode getFilterMode() {
		return filterMode;
	}

	public void setCoordId(final int coordId) {
		this.coordId = coordId;
	}

	public int getCoordId() {
		return coordId;
	}

	public double getEmissive() {
		return emissiveGain;
	}

	public void setEmissive(final double emissive) {
		emissiveGain = emissive;
	}

	public Vec3 getFresnelColor() {
		return fresnelColor;
	}

	public void setFresnelColor(final Vec3 fresnelColor) {
		this.fresnelColor = fresnelColor;
	}

	public double getFresnelOpacity() {
		return fresnelOpacity;
	}

	public void setFresnelOpacity(final double fresnelOpacity) {
		this.fresnelOpacity = fresnelOpacity;
	}

	public double getFresnelTeamColor() {
		return fresnelTeamColor;
	}

	public void setFresnelTeamColor(final double fresnelTeamColor) {
		this.fresnelTeamColor = fresnelTeamColor;
	}

	public boolean getUnshaded() {
		return unshaded;
	}

	public void setUnshaded(final boolean unshaded) {
		this.unshaded = unshaded;
	}

	public boolean getSphereEnvMap() {
		return sphereEnvMap;
	}

	public void setSphereEnvMap(final boolean sphereEnvMap) {
		this.sphereEnvMap = sphereEnvMap;
	}

	public boolean getTwoSided() {
		return twoSided;
	}

	public void setTwoSided(final boolean twoSided) {
		this.twoSided = twoSided;
	}

	public boolean getUnfogged() {
		return unfogged;
	}

	public void setUnfogged(final boolean unfogged) {
		this.unfogged = unfogged;
	}

	public boolean getNoDepthTest() {
		return noDepthTest;
	}

	public void setNoDepthTest(final boolean noDepthTest) {
		this.noDepthTest = noDepthTest;
	}

	public boolean getNoDepthSet() {
		return noDepthSet;
	}

	public void setNoDepthSet(final boolean noDepthSet) {
		this.noDepthSet = noDepthSet;
	}

	public boolean getUnlit() {
		return unlit;
	}

	public void setUnlit(final boolean unlit) {
		this.unlit = unlit;
	}
}
