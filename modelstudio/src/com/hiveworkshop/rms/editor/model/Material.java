package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.animflag.BitmapAnimFlag;
import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.parsers.mdlx.mdl.MdlUtils;

import java.util.*;

/**
 * A class for MDL materials.
 *
 * Eric Theller 11/5/2011
 */
public class Material implements Named {
	public static final String SHADER_HD_DEFAULT_UNIT = "Shader_HD_DefaultUnit";
	public static final String SHADER_HD_CRYSTAL = "Shader_HD_Crystal";
	public static final String SHADER_SD_FIXEDFUNCTION = "Shader_SD_FixedFunction";
	public static final String SHADER_SD_LEGACY = "Shader_SD_Legacy";
	private final List<Layer> layers = new ArrayList<>();
	int priorityPlane = 0;
	String shaderString = "";
	private final EnumSet<flag> flags = EnumSet.noneOf(flag.class);
	private String tempName;

	public Material() {

	}

	public Material(final Layer layer) {
		layers.add(layer);
	}

	public Material(final List<Layer> layers) {
		this.layers.addAll(layers);
	}

	private Material(final Material material) {
		for (Layer layer : material.layers) {
			this.layers.add(layer.deepCopy());
		}
		flags.addAll(material.flags);
		priorityPlane = material.priorityPlane;
		shaderString = material.shaderString;
	}

	public void resetTempName(){
		tempName = null;
	}

	public String getName() {
		if (tempName == null) {
			StringBuilder name = new StringBuilder();

			String alpha = "\ud83c\udd30"; //🄰
			String a_alpha = "\ud83c\udd70"; //🅰
			String tv_anim = "\uD83C\uDD83"; //🆃
			String flip_book = "\uD83C\uDD75"; //🅵

			if (0 < layers.size()) {
				for (int i = layers.size() - 1; 0 <= i; i--) {
					Layer layer = layers.get(i);
					if (layer != null) {
						StringBuilder prefix = new StringBuilder();
						try {
							if(layer.getVisibilityFlag() != null){
								prefix.append(a_alpha);
							} else if (layer.getStaticAlpha() != 1) {
								prefix.append(alpha);
							}
							if (layer.hasTexAnim()) {
								prefix.append(tv_anim);
							}
							layer.getTextureSlots().stream()
									.filter(t -> t.getFlipbookTexture() != null && !t.getFlipbookTexture().getAnimMap().isEmpty())
									.findFirst()
									.flatMap(
											animTexture -> animTexture.getFlipbookTexture().getAnimMap().values().stream()
													.filter(tm -> !tm.isEmpty())
													.findFirst())
									.ifPresent(entryTreeMap -> prefix.append(flip_book));

							BitmapAnimFlag flipbookTexture = layer.getTextureSlot(0).getFlipbookTexture();
							String textureName = layer.getTexture(0).getName();
							if(flipbookTexture != null && !flipbookTexture.getAnimMap().isEmpty()) {
								TreeMap<Integer, Entry<Bitmap>> entryTreeMap = flipbookTexture.getAnimMap().values().stream()
										.filter(tm -> !tm.isEmpty())
										.findFirst().orElse(null);
								if (entryTreeMap != null && entryTreeMap.firstEntry().getValue().value != null) {
									textureName = entryTreeMap.firstEntry().getValue().value.getName();

								}
							}

							if (!name.isEmpty()) name.append(" / ");
//							if (!prefix.isEmpty()) name.append(prefix).append(" ");
							name.append(textureName);
							if (!prefix.isEmpty()) name.append(" ").append(prefix);
						} catch (final NullPointerException e) {
							if (layer.getTextures().get(0) != null){
								name.append("(").append(layer.getTextures().get(0).getName()).append(")");
							} else {
								name.append("No Texture");
							}
						}
					}
				}
			} else {
				name.append("This material got no layers!");
			}

			tempName = name.toString();
		}
		return tempName;
	}


	@Override
	public void setName(String text) {
	}

	public Layer firstLayer() {
		if (layers.size() > 0) {
			return layers.get(layers.size() - 1);
		}
		return null;
	}

	public String getShaderString() {
		return shaderString;
	}

	public void setShaderString(final String shaderString) {
		this.shaderString = shaderString;
	}

	public List<Layer> getLayers() {
		return layers;
	}

	public Layer getLayer(int i){
		if(i<layers.size()){
			return layers.get(i);
		}
		return null;
	}

	public void clearLayers() {
		layers.clear();
	}

	public void addLayer(Layer layer) {
		if (layer != null) {
			layers.add(layer);
		}
	}

	public void addLayer(int index, Layer layer) {
		if (layer != null) {
			layers.add(index, layer);
		}
	}

	public void removeLayer(Layer layer) {
		if (layer != null) {
			layers.remove(layer);
		}
	}

	public void setLayers(final List<Layer> layers) {
		this.layers.clear();
		this.layers.addAll(layers);
	}

	public int getPriorityPlane() {
		return priorityPlane;
	}

	public void setPriorityPlane(final int priorityPlane) {
		this.priorityPlane = priorityPlane;
	}

	// @Override
	// public int hashCode() {
	// 	final int prime = 31;
	// 	int result = 1;
	// 	result = (prime * result) + ((layers == null) ? 0 : layers.hashCode());
	// 	result = (prime * result) + priorityPlane;
	// 	result = (prime * result) + ((shaderString == null) ? 0 : shaderString.hashCode());
	// 	return result;
	// }

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof Material) {
			final Material other = (Material) obj;
			if (!Objects.equals(flags, other.flags)) {
				return false;
			}
			if (!Objects.equals(layers, other.getLayers())) {
				return false;
			}
			if (priorityPlane != other.priorityPlane) {
				return false;
			}
			return Objects.equals(shaderString, other.shaderString);
		}
		return false;
	}

	public boolean getConstantColor() {
		return flags.contains(flag.CONSTANT_COLOR);
	}

	public void setConstantColor(final boolean constantColor) {
		setFlag(flag.CONSTANT_COLOR, constantColor);
	}

	public boolean getSortPrimsFarZ() {
		return flags.contains(flag.SORT_PRIMS_FAR_Z);
	}

	public void setSortPrimsFarZ(final boolean sortPrimsFarZ) {
		setFlag(flag.SORT_PRIMS_FAR_Z, sortPrimsFarZ);
	}

	public boolean getSortPrimsNearZ() {
		return flags.contains(flag.SORT_PRIMS_NEAR_Z);
	}
	public void setSortPrimsNearZ(final boolean sortPrimsNearZ) {
		setFlag(flag.SORT_PRIMS_NEAR_Z, sortPrimsNearZ);
	}

	public boolean getFullResolution() {
		return flags.contains(flag.FULL_RESOLUTION);
	}

	public void setFullResolution(final boolean fullResolution) {
		setFlag(flag.FULL_RESOLUTION, fullResolution);
	}

	public boolean getTwoSided() {
		return flags.contains(flag.TWO_SIDED);
	}

	public void setTwoSided(final boolean twoSided) {
		setFlag(flag.TWO_SIDED, twoSided);
	}

	public EnumSet<flag> getFlags() {
		return flags;
	}

	public void setFlag(flag flag, boolean on){
		if (on){
			flags.add(flag);
		} else {
			flags.remove(flag);
		}
	}
	public boolean isFlagSet(flag flag){
		return flags.contains(flag);
	}
	public Material deepCopy(){
		return new Material(this);
	}

	public boolean isHD(){
		return shaderString.equals(SHADER_HD_DEFAULT_UNIT);
	}



//	boolean constantColor = false;
//	boolean sortPrimsFarZ = false;
//	boolean fullResolution = false;
//	boolean twoSided = false;

	public enum flag {
		TWO_SIDED(MdlUtils.TOKEN_TWO_SIDED, 0x2),
		CONSTANT_COLOR(MdlUtils.TOKEN_CONSTANT_COLOR, 0x1),
		SORT_PRIMS_FAR_Z(MdlUtils.TOKEN_SORT_PRIMS_FAR_Z, 0x10),
		SORT_PRIMS_NEAR_Z(MdlUtils.TOKEN_SORT_PRIMS_NEAR_Z, 0x8),
		FULL_RESOLUTION(MdlUtils.TOKEN_FULL_RESOLUTION, 0x20);
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

		public static flag getFromFlagBit(int flagBit){
			for(flag flag : flag.values()){
				if((flagBit&flag.getFlagBit()) == flag.getFlagBit()){
					return flag;
				}
			}
			return null;
		}
		public static Set<flag> getFlags(int flagBits){
			Set<flag> flags = new HashSet<>();
			for(flag flag : flag.values()){
				if((flagBits&flag.getFlagBit()) == flag.getFlagBit()){
					flags.add(flag);
				}
			}
			return flags;
		}
	}
}
