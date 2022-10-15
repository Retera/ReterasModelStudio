package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.util.FilterMode;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeEnvironmentImpl;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

/**
 * Layers for MDLToolkit/MatrixEater.
 * <p>
 * Eric Theller 3/8/2012
 */
public class Layer extends TimelineContainer implements Named {
	private FilterMode filterMode = FilterMode.NONE;
	private final List<Bitmap> textures = new ArrayList<>();
	private final Map<Integer, BitmapAnimFlag> flipbookTextures = new HashMap<>();
	private int coordId = 0;
	private TextureAnim textureAnim;
	private double emissiveGain = 0;
	private Vec3 fresnelColor = new Vec3(1, 1, 1);
	private double fresnelOpacity = 0;
	private double fresnelTeamColor = 0;
	private double staticAlpha = 1;// Amount of static alpha (opacity)
	private final EnumSet<flag> flags = EnumSet.noneOf(flag.class);


	public Layer() {
	}

	public Layer(Bitmap texture) {
		this(FilterMode.NONE, texture);
	}

	public Layer(FilterMode filterMode, Bitmap texture) {
		this.filterMode = filterMode;
		textures.add(texture);
	}

	public Layer(FilterMode filterMode, Collection<Bitmap> textures) {
		this.filterMode = filterMode;
		this.textures.addAll(textures);
	}

	private Layer(Layer other) {
		textures.addAll(other.textures);
		flags.addAll(other.flags);
		filterMode = other.filterMode;
		coordId = other.coordId;
//		texture = other.texture;
		if (other.textureAnim != null) {
			textureAnim = new TextureAnim(other.textureAnim);
//			textureAnim = other.textureAnim;
		} else {
			textureAnim = null;
		}
		staticAlpha = other.staticAlpha;
		emissiveGain = other.emissiveGain;
		fresnelColor = new Vec3(other.fresnelColor);
		fresnelOpacity = other.fresnelOpacity;
		fresnelTeamColor = other.fresnelTeamColor;
//		unshaded = other.unshaded;
//		sphereEnvMap = other.sphereEnvMap;
//		twoSided = other.twoSided;
//		unfogged = other.unfogged;
//		noDepthTest = other.noDepthTest;
//		noDepthSet = other.noDepthSet;
//		unlit = other.unlit;

		for (AnimFlag<?> animFlag : other.getAnimFlags()) {
			add(animFlag.deepCopy());
		}
		for (Integer slot : other.flipbookTextures.keySet()) {
			BitmapAnimFlag animFlag = flipbookTextures.get(slot);
			flipbookTextures.put(slot, (BitmapAnimFlag) animFlag.deepCopy());
		}

	}

	public void setFlipbookTexture(int slot, BitmapAnimFlag animFlag){
		if(animFlag != null){
			flipbookTextures.put(slot, animFlag);
		} else {
			flipbookTextures.remove(slot);
		}
	}

	public Map<Integer, BitmapAnimFlag> getFlipbookTextures() {
		return flipbookTextures;
	}

	public BitmapAnimFlag getFlipbookTexture(int slot) {
		return flipbookTextures.get(slot);
	}

	public Bitmap getRenderTexture(TimeEnvironmentImpl animatedRenderEnvironment) {
		BitmapAnimFlag textureFlag = (BitmapAnimFlag) find(MdlUtils.TOKEN_TEXTURE_ID);
		if ((textureFlag != null) && (animatedRenderEnvironment != null && animatedRenderEnvironment.getCurrentSequence() != null)) {
			return textureFlag.interpolateAt(animatedRenderEnvironment);
		} else {
			if (textures.size() > 0) {
				return textures.get(0);
			}
			return null;
		}
	}
	public Bitmap getRenderTexture(TimeEnvironmentImpl animatedRenderEnvironment, int slot) {
		BitmapAnimFlag textureFlag = flipbookTextures.get(slot);
		if ((textureFlag != null) && (animatedRenderEnvironment != null && animatedRenderEnvironment.getCurrentSequence() != null)) {
			return textureFlag.interpolateAt(animatedRenderEnvironment);
		} else if(slot < textures.size()){
			return textures.get(slot);
		} else if (textures.size() > 0) {
			return textures.get(0);
		}
		return null;
	}

	public Bitmap firstTexture() {
		if (!textures.isEmpty()) {
			return textures.get(0);
		}
		return null;
	}

	public Bitmap getTexture(int id){
		if (0 <= id && id < textures.size()){
			return textures.get(id);
		}
		return null;
	}

	public List<Bitmap> getTextures() {
		return textures;
	}

	public void setTexture(int i, Bitmap texture) {
		this.textures.set(i, texture);
	}

	@Override
	public float getRenderVisibility(TimeEnvironmentImpl animatedRenderEnvironment) {
		return getRenderVisibility(animatedRenderEnvironment, (float) staticAlpha);
	}

	@Override
	public String getName() {
		if (!textures.isEmpty()) {
			return textures.get(0).getName() + " layer (mode " + filterMode + ") ";
		}
		return "multi-textured layer (mode " + filterMode + ") ";
	}

	@Override
	public void setName(String text) {
	}

	@Override
	public String visFlagName() {
		return MdlUtils.TOKEN_ALPHA;
	}

	public double getStaticAlpha() {
		return staticAlpha;
	}

	public void setStaticAlpha(double staticAlpha) {
		this.staticAlpha = staticAlpha;
	}

	public boolean hasTexAnim() {
		return textureAnim != null;
	}

	public TextureAnim getTextureAnim() {
		return textureAnim;
	}

	public void setTextureAnim(TextureAnim texa) {
		textureAnim = texa;
	}

	public FilterMode getFilterMode() {
		return filterMode;
	}

	public void setFilterMode(FilterMode filterMode) {
		this.filterMode = filterMode;
	}


	public boolean hasCoordId() {
		return coordId != 0;
	}

	public int getCoordId() {
		return coordId;
	}

	public void setCoordId(int coordId) {
		this.coordId = coordId;
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
		return flags.contains(flag.UNSHADED);
	}

	public void setUnshaded(boolean unshaded) {
		boolean ugg = unshaded ? flags.add(flag.UNSHADED) : flags.remove(flag.UNSHADED);
	}

	public boolean getSphereEnvMap() {
		return flags.contains(flag.SPHERE_ENV_MAP);
	}

	public void setSphereEnvMap(boolean sphereEnvMap) {
		boolean ugg = sphereEnvMap ? flags.add(flag.SPHERE_ENV_MAP) : flags.remove(flag.SPHERE_ENV_MAP);
	}

	public boolean getTwoSided() {
		return flags.contains(flag.TWO_SIDED);
	}

	public void setTwoSided(boolean twoSided) {
		boolean ugg = twoSided ? flags.add(flag.TWO_SIDED) : flags.remove(flag.TWO_SIDED);
	}

	public boolean getUnfogged() {
		return flags.contains(flag.UNFOGGED);
	}

	public void setUnfogged(boolean unfogged) {
		boolean ugg = unfogged ? flags.add(flag.UNFOGGED) : flags.remove(flag.UNFOGGED);
	}

	public boolean getNoDepthTest() {
		return flags.contains(flag.NO_DEPTH_TEST);
//		return noDepthTest;
	}

	public void setNoDepthTest(boolean noDepthTest) {
		boolean ugg = noDepthTest ? flags.add(flag.NO_DEPTH_TEST) : flags.remove(flag.NO_DEPTH_TEST);
	}

	public boolean getNoDepthSet() {
		return flags.contains(flag.NO_DEPTH_SET);
	}

	public void setNoDepthSet(boolean noDepthSet) {
		boolean ugg = noDepthSet ? flags.add(flag.NO_DEPTH_SET) : flags.remove(flag.NO_DEPTH_SET);
	}

	public boolean getUnlit() {
		return flags.contains(flag.UNLIT);
	}

	public void setUnlit(boolean unlit) {
		boolean ugg = unlit ? flags.add(flag.UNLIT) : flags.remove(flag.UNLIT);
	}

	public Set<flag> getFlags() {
		return flags;
	}

	public void setFlags(Collection<flag> newFlags){
		flags.clear();
		flags.addAll(newFlags);
	}

	public boolean isFlagSet(flag flag){
		return flags.contains(flag);
	}
	public void setFlag(flag flag){
		flags.add(flag);
	}
	public void setFlag(flag flag, boolean set){
		if(set){
			flags.add(flag);
		} else {
			flags.remove(flag);
		}
	}

	public Layer deepCopy(){
		return new Layer(this);
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + coordId;
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
//		result = (prime * result) + ((texture == null) ? 0 : texture.hashCode());
		result = (prime * result) + ((textureAnim == null) ? 0 : textureAnim.hashCode());
		result = prime * result + textures.hashCode();
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
		if (obj instanceof Layer) {
			Layer other = (Layer) obj;
			if (coordId != other.coordId) {
				return false;
			}
			if (!Objects.equals(animFlags, other.animFlags)) {
				return false;
			}
			if (!Objects.equals(flipbookTextures, other.flipbookTextures)) {
				return false;
			}

//			if (Double.doubleToLongBits(emissiveGain) != Double.doubleToLongBits(other.emissiveGain)) {
//				return false;
//			}
//			if (Double.doubleToLongBits(fresnelOpacity) != Double.doubleToLongBits(other.fresnelOpacity)) {
//				return false;
//			}
//			if (Double.doubleToLongBits(fresnelTeamColor) != Double.doubleToLongBits(other.fresnelTeamColor)) {
//				return false;
//			}
//			if (Double.doubleToLongBits(staticAlpha) != Double.doubleToLongBits(other.staticAlpha)) {
//				return false;
//			}


			if (Float.MIN_VALUE < (staticAlpha - other.staticAlpha) ) {
				return false;
			}
			if (Float.MIN_VALUE < (emissiveGain - other.emissiveGain) ) {
				return false;
			}
			if (Float.MIN_VALUE < (fresnelOpacity - other.fresnelOpacity) ) {
				return false;
			}
			if (Float.MIN_VALUE < (fresnelTeamColor - other.fresnelTeamColor) ) {
				return false;
			}
			if (fresnelColor != other.fresnelColor && fresnelColor != null && !fresnelColor.equalLocs(other.fresnelColor)) {
				return false;
			}
			if (filterMode != other.filterMode) {
				return false;
			}

			if(!flags.equals(other.flags)){
				return false;
			}

			if (!Objects.equals(textureAnim, other.textureAnim)) {
				return false;
			}

			return textures.equals(other.textures);
		}
		return false;
	}


//		if (layer.getTwoSided()) {mdlxLayer.flags |= 0x10;}
//
//		if (layer.getUnfogged()) {mdlxLayer.flags |= 0x20;}
//
//		if (layer.getNoDepthTest()) {mdlxLayer.flags |= 0x40;}
//
//		if (layer.getNoDepthSet()) {mdlxLayer.flags |= 0x80;}
//
//		if (layer.getUnlit()) {mdlxLayer.flags |= 0x100;}
	public enum flag {
		UNSHADED(MdlUtils.TOKEN_UNSHADED, 0x1),
		SPHERE_ENV_MAP(MdlUtils.TOKEN_SPHERE_ENV_MAP, 0x2),
		TWO_SIDED(MdlUtils.TOKEN_TWO_SIDED, 0x10),
		UNFOGGED(MdlUtils.TOKEN_UNFOGGED, 0x20),
		NO_DEPTH_TEST(MdlUtils.TOKEN_NO_DEPTH_TEST, 0x40),
		NO_DEPTH_SET(MdlUtils.TOKEN_NO_DEPTH_SET, 0x80),
		UNLIT(MdlUtils.TOKEN_UNLIT, 0x100);
		final String name;
		final int flagBit;
		flag(String name, int flagBit){
			this.name = name;
			this.flagBit = flagBit;
		}

		public String getName() {
			return name;
		}

	public int getFlagBit() {
		return flagBit;
	}
}
}
