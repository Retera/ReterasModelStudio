package com.hiveworkshop.wc3.mdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.etheller.warsmash.parsers.mdlx.MdlxLayer;
import com.etheller.warsmash.parsers.mdlx.MdlxLayer.FilterMode;
import com.etheller.warsmash.util.MdlUtils;
import com.hiveworkshop.wc3.gui.modelviewer.AnimatedRenderEnvironment;
import com.hiveworkshop.wc3.mdl.v2.timelines.Animatable;

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
	private Vertex fresnelColor = new Vertex(1, 1, 1);
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
		result = (prime * result) + this.coordId;
		result = (prime * result) + this.TVertexAnimId;
		result = (prime * result) + ((this.animFlags == null) ? 0 : this.animFlags.hashCode());
		long temp;
		temp = Double.doubleToLongBits(this.emissiveGain);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.fresnelOpacity);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(this.fresnelTeamColor);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((this.filterMode == null) ? 0 : this.filterMode.hashCode());
		// result = (prime * result) + ((flags == null) ? 0 : flags.hashCode());
		temp = Double.doubleToLongBits(this.staticAlpha);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		result = (prime * result) + ((this.texture == null) ? 0 : this.texture.hashCode());
		result = (prime * result) + ((this.textureAnim == null) ? 0 : this.textureAnim.hashCode());
		result = (prime * result) + this.textureId;
		result = (prime * result) + ((this.textures == null) ? 0 : this.textures.hashCode());
		if (this.fresnelColor != null) {
			temp = Double.doubleToLongBits(this.fresnelColor.x);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.fresnelColor.y);
			result = (prime * result) + (int) (temp ^ (temp >>> 32));
			temp = Double.doubleToLongBits(this.fresnelColor.z);
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
		if (this.coordId != other.coordId) {
			return false;
		}
		if (this.TVertexAnimId != other.TVertexAnimId) {
			return false;
		}
		if (this.animFlags == null) {
			if (other.animFlags != null) {
				return false;
			}
		} else if (!this.animFlags.equals(other.animFlags)) {
			return false;
		}
		if (Double.doubleToLongBits(this.emissiveGain) != Double.doubleToLongBits(other.emissiveGain)) {
			return false;
		}
		if (Double.doubleToLongBits(this.fresnelOpacity) != Double.doubleToLongBits(other.fresnelOpacity)) {
			return false;
		}
		if (Double.doubleToLongBits(this.fresnelTeamColor) != Double.doubleToLongBits(other.fresnelTeamColor)) {
			return false;
		}
		if (this.fresnelColor == null) {
			if (other.fresnelColor != null) {
				return false;
			}
		} else {
			if (other.fresnelColor == null) {
				return false;
			}
			if (!this.fresnelColor.equalLocs(other.fresnelColor)) {
				return false;
			}
		}
		if (this.filterMode == null) {
			if (other.filterMode != null) {
				return false;
			}
		} else if (!this.filterMode.equals(other.filterMode)) {
			return false;
		}

		if (this.emissiveGain != other.emissiveGain) {
			return false;
		}

		if (!this.fresnelColor.equalLocs(other.fresnelColor)) {
			return false;
		}

		if (this.fresnelOpacity != other.fresnelOpacity) {
			return false;
		}

		if (this.fresnelTeamColor != other.fresnelTeamColor) {
			return false;
		}

		if ((this.unshaded != other.unshaded) || (this.sphereEnvMap != other.sphereEnvMap)
				|| (this.twoSided != other.twoSided) || (this.unfogged != other.unfogged)
				|| (this.noDepthTest != other.noDepthTest) || (this.noDepthSet != other.noDepthSet)
				|| (this.unlit != other.unlit)) {
			return false;
		}

		if (Double.doubleToLongBits(this.staticAlpha) != Double.doubleToLongBits(other.staticAlpha)) {
			return false;
		}
		if (this.texture == null) {
			if (other.texture != null) {
				return false;
			}
		} else if (!this.texture.equals(other.texture)) {
			return false;
		}
		if (this.textureAnim == null) {
			if (other.textureAnim != null) {
				return false;
			}
		} else if (!this.textureAnim.equals(other.textureAnim)) {
			return false;
		}
		if (this.textureId != other.textureId) {
			return false;
		}
		if (this.textures == null) {
			if (other.textures != null) {
				return false;
			}
		} else if (!this.textures.equals(other.textures)) {
			return false;
		}
		return true;
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
		this.filterMode = other.filterMode;
		this.textureId = other.textureId;
		this.TVertexAnimId = other.TVertexAnimId;
		this.coordId = other.coordId;
		this.texture = new Bitmap(other.texture);
		if (other.textureAnim != null) {
			this.textureAnim = new TextureAnim(other.textureAnim);
		} else {
			this.textureAnim = null;
		}
		this.staticAlpha = other.staticAlpha;
		this.emissiveGain = other.emissiveGain;
		this.fresnelColor = new Vertex(other.fresnelColor);
		this.fresnelOpacity = other.fresnelOpacity;
		this.fresnelTeamColor = other.fresnelTeamColor;
		this.unshaded = other.unshaded;
		this.sphereEnvMap = other.sphereEnvMap;
		this.twoSided = other.twoSided;
		this.unfogged = other.unfogged;
		this.noDepthTest = other.noDepthTest;
		this.noDepthSet = other.noDepthSet;
		this.unshaded = other.unshaded;
		this.unlit = other.unlit;
		this.textures = new ArrayList<>();
		addAll(other.getAnimFlags());
		if (other.textures != null) {
			for (final Bitmap bmp : other.textures) {
				this.textures.add(new Bitmap(bmp));
			}
		} else {
			this.textures = null;
		}
	}

	public Layer(final MdlxLayer layer) {
		this(layer.filterMode.toString(), layer.textureId);

		final int shadingFlags = layer.flags;
		if ((shadingFlags & 0x1) != 0) {
			this.unshaded = true;
		}
		if ((shadingFlags & 0x2) != 0) {
			this.sphereEnvMap = true;
		}
		if ((shadingFlags & 0x10) != 0) {
			this.twoSided = true;
		}
		if ((shadingFlags & 0x20) != 0) {
			this.unfogged = true;
		}
		if ((shadingFlags & 0x40) != 0) {
			this.noDepthTest = true;
		}
		if ((shadingFlags & 0x80) != 0) {
			this.noDepthSet = true;
		}
		if ((shadingFlags & 0x100) != 0) {
			this.unlit = true;
		}

		setTVertexAnimId(layer.textureAnimationId);
		setCoordId((int) layer.coordId);
		setStaticAlpha(layer.alpha);

		// > 800
		this.emissiveGain = layer.emissiveGain;
		// > 900
		setFresnelColor(new Vertex(MdlxUtils.flipRGBtoBGR(layer.fresnelColor)));
		this.fresnelOpacity = layer.fresnelOpacity;
		this.fresnelTeamColor = layer.fresnelTeamColor;

		loadTimelines(layer);
	}

	public MdlxLayer toMdlx() {
		final MdlxLayer layer = new MdlxLayer();

		layer.filterMode = this.filterMode;

		if (this.unshaded) {
			layer.flags |= 0x1;
		}

		if (this.sphereEnvMap) {
			layer.flags |= 0x2;
		}

		if (this.twoSided) {
			layer.flags |= 0x10;
		}

		if (this.unfogged) {
			layer.flags |= 0x20;
		}

		if (this.noDepthTest) {
			layer.flags |= 0x40;
		}

		if (this.noDepthSet) {
			layer.flags |= 0x80;
		}

		if (this.unlit) {
			layer.flags |= 0x100;
		}

		layer.textureId = getTextureId();
		layer.textureAnimationId = getTVertexAnimId();
		layer.coordId = getCoordId();
		layer.alpha = (float) getStaticAlpha();

		// > 800
		layer.emissiveGain = layer.emissiveGain;
		// > 900
		layer.fresnelColor = MdlxUtils.flipRGBtoBGR(this.fresnelColor.toFloatArray());
		layer.fresnelOpacity = (float) this.fresnelOpacity;
		layer.fresnelTeamColor = (float) this.fresnelTeamColor;

		timelinesToMdlx(layer);

		return layer;
	}

	public Bitmap firstTexture() {
		if (this.texture != null) {
			return this.texture;
		} else {
			if ((this.textures != null) && (this.textures.size() > 0)) {
				return this.textures.get(0);
			}
			return null;
		}
	}

	public Bitmap getRenderTexture(final AnimatedRenderEnvironment animatedRenderEnvironment,
			final EditableModel model) {
		final AnimFlag textureFlag = find(MdlUtils.TOKEN_TEXTURE_ID);
		if ((textureFlag != null) && (animatedRenderEnvironment != null)) {
			if (animatedRenderEnvironment.getCurrentAnimation() == null) {
				if (this.textures.size() > 0) {
					return this.textures.get(0);
				} else {
					return this.texture;
				}
			}
			final Integer textureIdAtTime = (Integer) textureFlag.interpolateAt(animatedRenderEnvironment);
			if (textureIdAtTime >= model.getTextures().size()) {
				return this.texture;
			}
			final Bitmap textureAtTime = model.getTextures().get(textureIdAtTime);
			return textureAtTime;
		} else {
			return this.texture;
		}
	}

	@Override
	public float getRenderVisibility(final AnimatedRenderEnvironment animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) this.staticAlpha);
	}

	public void setTextureAnim(final TextureAnim texa) {
		this.textureAnim = texa;
	}

	public void setTextureAnim(final List<TextureAnim> list) { // Sets the
		// texture
		// anim
		// reference
		// to the
		// one from
		// the list
		// corresponding
		// to the
		// TVertexAnimId
		this.textureAnim = list.get(this.TVertexAnimId);
	}

	private transient Map<Integer, Bitmap> ridiculouslyWrongTextureIDToTexture = new HashMap<>();

	public void buildTextureList(final EditableModel mdlr) {
		this.textures = new ArrayList<>();
		final AnimFlag txFlag = find(MdlUtils.TOKEN_TEXTURE_ID);
		for (int i = 0; i < txFlag.values.size(); i++) {
			final int txId = ((Integer) txFlag.values.get(i)).intValue();
			final Bitmap texture2 = mdlr.getTexture(txId);
			this.textures.add(texture2);
			this.ridiculouslyWrongTextureIDToTexture.put(txId, texture2);
		}
	}

	public void updateIds(final EditableModel mdlr) {
		this.textureId = mdlr.getTextureId(this.texture);
		this.TVertexAnimId = mdlr.getTextureAnimId(this.textureAnim);
		if (this.textures != null) {
			final AnimFlag txFlag = find(MdlUtils.TOKEN_TEXTURE_ID);
			for (int i = 0; i < txFlag.values.size(); i++) {
				final Bitmap textureFoundFromDirtyId = this.ridiculouslyWrongTextureIDToTexture
						.get(((Integer) txFlag.values.get(i)).intValue());
				final int newerTextureId = mdlr.getTextureId(textureFoundFromDirtyId);
				txFlag.values.set(i, newerTextureId);
				this.ridiculouslyWrongTextureIDToTexture.put(newerTextureId, textureFoundFromDirtyId);
			}
		}
	}

	public void updateRefs(final EditableModel mdlr) {
		if ((this.textureId >= 0) && (this.textureId < mdlr.getTextures().size())) {
			this.texture = mdlr.getTexture(this.textureId);
		}
		if ((this.TVertexAnimId >= 0) && (this.TVertexAnimId < mdlr.texAnims.size())) {
			this.textureAnim = mdlr.texAnims.get(this.TVertexAnimId);
		}
		final AnimFlag txFlag = find(MdlUtils.TOKEN_TEXTURE_ID);
		if (txFlag != null) {
			buildTextureList(mdlr);
		}
	}

	public boolean hasCoordId() {
		return this.coordId != 0;
	}

	public boolean hasTexAnim() {
		return this.TVertexAnimId != -1;
	}

	@Override
	public String getName() {
		if (this.texture != null) {
			return this.texture.getName() + " layer (mode " + this.filterMode + ") ";
		}
		return "multi-textured layer (mode " + this.filterMode + ") ";
	}

	@Override
	public String visFlagName() {
		return "Alpha";
	}

	public int getTextureId() {
		return this.textureId;
	}

	public void setTextureId(final int textureId) {
		this.textureId = textureId;
	}

	public int getTVertexAnimId() {
		return this.TVertexAnimId;
	}

	public void setTVertexAnimId(final int tVertexAnimId) {
		this.TVertexAnimId = tVertexAnimId;
	}

	public Bitmap getTextureBitmap() {
		return this.texture;
	}

	public void setTexture(final Bitmap texture) {
		this.texture = texture;
	}

	public double getStaticAlpha() {
		return this.staticAlpha;
	}

	public void setStaticAlpha(final double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public List<Bitmap> getTextures() {
		return this.textures;
	}

	public void setTextures(final List<Bitmap> textures) {
		this.textures = textures;
	}

	public TextureAnim getTextureAnim() {
		return this.textureAnim;
	}

	public void setFilterMode(final FilterMode filterMode) {
		this.filterMode = filterMode;
	}

	public FilterMode getFilterMode() {
		return this.filterMode;
	}

	public void setCoordId(final int coordId) {
		this.coordId = coordId;
	}

	public int getCoordId() {
		return this.coordId;
	}

	public double getEmissive() {
		return this.emissiveGain;
	}

	public void setEmissive(final double emissive) {
		this.emissiveGain = emissive;
	}

	public Animatable<Bitmap> getTexture() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public Animatable<Double> getAlpha() {
		throw new UnsupportedOperationException("not yet implemented");
	}

	public Vertex getFresnelColor() {
		return this.fresnelColor;
	}

	public void setFresnelColor(final Vertex fresnelColor) {
		this.fresnelColor = fresnelColor;
	}

	public double getFresnelOpacity() {
		return this.fresnelOpacity;
	}

	public void setFresnelOpacity(final double fresnelOpacity) {
		this.fresnelOpacity = fresnelOpacity;
	}

	public double getFresnelTeamColor() {
		return this.fresnelTeamColor;
	}

	public void setFresnelTeamColor(final double fresnelTeamColor) {
		this.fresnelTeamColor = fresnelTeamColor;
	}

	public boolean getUnshaded() {
		return this.unshaded;
	}

	public void setUnshaded(final boolean unshaded) {
		this.unshaded = unshaded;
	}

	public boolean getSphereEnvMap() {
		return this.sphereEnvMap;
	}

	public void setSphereEnvMap(final boolean sphereEnvMap) {
		this.sphereEnvMap = sphereEnvMap;
	}

	public boolean getTwoSided() {
		return this.twoSided;
	}

	public void setTwoSided(final boolean twoSided) {
		this.twoSided = twoSided;
	}

	public boolean getUnfogged() {
		return this.unfogged;
	}

	public void setUnfogged(final boolean unfogged) {
		this.unfogged = unfogged;
	}

	public boolean getNoDepthTest() {
		return this.noDepthTest;
	}

	public void setNoDepthTest(final boolean noDepthTest) {
		this.noDepthTest = noDepthTest;
	}

	public boolean getNoDepthSet() {
		return this.noDepthSet;
	}

	public void setNoDepthSet(final boolean noDepthSet) {
		this.noDepthSet = noDepthSet;
	}

	public boolean getUnlit() {
		return this.unlit;
	}

	public void setUnlit(final boolean unlit) {
		this.unlit = unlit;
	}
}
