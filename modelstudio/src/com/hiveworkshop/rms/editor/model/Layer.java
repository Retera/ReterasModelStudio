package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.IntAnimFlag;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
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

	private transient Map<Integer, Bitmap> ridiculouslyWrongTextureIDToTexture = new HashMap<>();

	public Layer(String filterMode, int textureId) {
		this.filterMode = FilterMode.nameToFilter(filterMode);
		this.textureId = textureId;
	}

	public Layer() {

	}

	public Layer(String filterMode, Bitmap texture) {
		this.filterMode = FilterMode.nameToFilter(filterMode);
		this.texture = texture;
	}

	public Layer(FilterMode filterMode, Bitmap texture) {
		this.filterMode = filterMode;
		this.texture = texture;
	}

	public Layer(Layer other) {
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
			add(animFlag.deepCopy());
		}

		textures = new ArrayList<>();
		if (other.textures != null) {
			textures.addAll(other.textures);
		} else {
			textures = null;
		}
	}

	@Override
	public int hashCode() {
		int prime = 31;
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
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Layer other = (Layer) obj;
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

	public Bitmap getRenderTexture(TimeEnvironmentImpl animatedRenderEnvironment, EditableModel model) {
		IntAnimFlag textureFlag = (IntAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);
		if ((textureFlag != null) && (animatedRenderEnvironment != null)) {
			if (animatedRenderEnvironment.getCurrentSequence() == null) {
				if (textures.size() > 0) {
					return textures.get(0);
				} else {
					return texture;
				}
			}
			Integer textureIdAtTime = textureFlag.interpolateAt(animatedRenderEnvironment);
//			System.out.println("layer texId: " + textureIdAtTime);
			if (textureIdAtTime >= model.getTextures().size()) {
				return texture;
			}
			return model.getTextures().get(textureIdAtTime);
		} else {
			return texture;
		}
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) staticAlpha);
	}

	public void setTextureAnim(TextureAnim texa) {
		textureAnim = texa;
	}

	public void setTextureAnim(List<TextureAnim> list) {
		// Sets the texture anim reference to the one from
		// the list corresponding to the TVertexAnimId
		textureAnim = list.get(TVertexAnimId);
	}

	public void buildTextureList(EditableModel mdlr) {
		textures = new ArrayList<>();
		IntAnimFlag txFlag = (IntAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);

		for (Sequence anim : txFlag.getAnimMap().keySet()){
			for (int i = 0; i < txFlag.size(); i++) {
				int txId = txFlag.getValueFromIndex(anim, i);
				Bitmap texture2 = mdlr.getTexture(txId);
				textures.add(texture2);
				ridiculouslyWrongTextureIDToTexture.put(txId, texture2);
			}
		}
	}

	public void updateIds(EditableModel model) {
		setTextureId(model.getTextureId(getTextureBitmap()));
		setTVertexAnimId(model.getTextureAnimId(getTextureAnim()));
		if (getTextures() != null) {
			IntAnimFlag txFlag = (IntAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);
			for (Sequence anim : txFlag.getAnimMap().keySet()){
				for (int i = 0; i < txFlag.size(); i++) {
					Bitmap textureFoundFromDirtyId = ridiculouslyWrongTextureIDToTexture.get(txFlag.getValueFromIndex(anim, i));
					int newerTextureId = model.getTextureId(textureFoundFromDirtyId);

					txFlag.getEntryAt(anim, txFlag.getTimeFromIndex(anim, i)).setValue(newerTextureId);
					ridiculouslyWrongTextureIDToTexture.put(newerTextureId, textureFoundFromDirtyId);
				}
			}
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

	public void setTextureId(int textureId) {
		this.textureId = textureId;
	}

	public int getTVertexAnimId() {
		return TVertexAnimId;
	}

	public void setTVertexAnimId(int tVertexAnimId) {
		TVertexAnimId = tVertexAnimId;
	}

	public Bitmap getTextureBitmap() {
		return texture;
	}

	public void setTexture(Bitmap texture) {
		this.texture = texture;
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public List<Bitmap> getTextures() {
		return textures;
	}

	public void setTextures(List<Bitmap> textures) {
		this.textures = textures;
	}

	public TextureAnim getTextureAnim() {
		return textureAnim;
	}

	public void setFilterMode(FilterMode filterMode) {
		this.filterMode = filterMode;
	}

	public FilterMode getFilterMode() {
		return filterMode;
	}

	public void setCoordId(int coordId) {
		this.coordId = coordId;
	}

	public int getCoordId() {
		return coordId;
	}

	public double getEmissive() {
		return emissiveGain;
	}

	public void setEmissive(double emissive) {
		emissiveGain = emissive;
	}

	public Vec3 getFresnelColor() {
		return fresnelColor;
	}

	public void setFresnelColor(Vec3 fresnelColor) {
		this.fresnelColor = fresnelColor;
	}

	public double getFresnelOpacity() {
		return fresnelOpacity;
	}

	public void setFresnelOpacity(double fresnelOpacity) {
		this.fresnelOpacity = fresnelOpacity;
	}

	public double getFresnelTeamColor() {
		return fresnelTeamColor;
	}

	public void setFresnelTeamColor(double fresnelTeamColor) {
		this.fresnelTeamColor = fresnelTeamColor;
	}

	public boolean getUnshaded() {
		return unshaded;
	}

	public void setUnshaded(boolean unshaded) {
		this.unshaded = unshaded;
	}

	public boolean getSphereEnvMap() {
		return sphereEnvMap;
	}

	public void setSphereEnvMap(boolean sphereEnvMap) {
		this.sphereEnvMap = sphereEnvMap;
	}

	public boolean getTwoSided() {
		return twoSided;
	}

	public void setTwoSided(boolean twoSided) {
		this.twoSided = twoSided;
	}

	public boolean getUnfogged() {
		return unfogged;
	}

	public void setUnfogged(boolean unfogged) {
		this.unfogged = unfogged;
	}

	public boolean getNoDepthTest() {
		return noDepthTest;
	}

	public void setNoDepthTest(boolean noDepthTest) {
		this.noDepthTest = noDepthTest;
	}

	public boolean getNoDepthSet() {
		return noDepthSet;
	}

	public void setNoDepthSet(boolean noDepthSet) {
		this.noDepthSet = noDepthSet;
	}

	public boolean getUnlit() {
		return unlit;
	}

	public void setUnlit(boolean unlit) {
		this.unlit = unlit;
	}
}
