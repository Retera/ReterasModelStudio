package com.hiveworkshop.rms.editor.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for MDL materials.
 *
 * Eric Theller 11/5/2011
 */
public class Material {
	public static final String SHADER_HD_DEFAULT_UNIT = "Shader_HD_DefaultUnit";
	public static int teamColor = 0;
	List<Layer> layers = new ArrayList<>();
	int priorityPlane = 0;
	String shaderString = "";
	boolean constantColor = false;
	boolean sortPrimsFarZ = false;
	boolean fullResolution = false;
	boolean twoSided = false;

	public Material() {

	}

	public Material(final Layer layer) {
		layers.add(layer);
	}

	public Material(final List<Layer> layers) {
		this.layers.addAll(layers);
	}

	private Material(final Material material) {
		// copying the layers so the new material don't have to share them with the old one
		for (Layer layer : material.layers) {
			this.layers.add(layer.deepCopy());
		}
		priorityPlane = material.priorityPlane;
		shaderString = material.shaderString;
		constantColor = material.constantColor;
		sortPrimsFarZ = material.sortPrimsFarZ;
		fullResolution = material.fullResolution;
		twoSided = material.twoSided;
	}

	public String getName2() {
		StringBuilder name = new StringBuilder();
		if (layers.size() > 0) {
			if (SHADER_HD_DEFAULT_UNIT.equals(shaderString)) {
				try {
					name.append(" over ").append(layers.get(0).getTextureBitmap().getName());
					if (layers.get(0).find("Alpha") != null) {
						name.append(" (animated Alpha)");
					}
				} catch (final NullPointerException e) {
					name.append(" over ").append("animated texture layers (").append(layers.get(0).getTextures().get(0).getName()).append(")");
				}
			} else {
				if (layers.get(layers.size() - 1).getTextureBitmap() != null) {
					name = new StringBuilder(layers.get(layers.size() - 1).getTextureBitmap().getName());
					if (layers.get(layers.size() - 1).find("Alpha") != null) {
						name.append(" (animated Alpha)");
					}
				} else {
					name = new StringBuilder("animated texture layers");
				}
				for (int i = layers.size() - 2; i >= 0; i--) {
					try {
						name.append(" over ").append(layers.get(i).getTextureBitmap().getName());
						if (layers.get(i).find("Alpha") != null) {
							name.append(" (animated Alpha)");
						}
					} catch (final NullPointerException e) {
						name.append(" over ").append("animated texture layers (").append(layers.get(i).getTextures().get(0).getName()).append(")");
					}
				}
			}
		}
		return name.toString();
	}

	public String getName() {
		StringBuilder name = new StringBuilder();
		String over = " /\u21E9 "; //\u226F ≯,\u21B8 ↸, \u21B8↸, /\u02C5 /˅, /\u21E9 /⇩, /\u23F7 /⏷, /\u25BC /▼, /\u2304 /⌄, \u2215\u2304
//		"\u226F ≯,\u21B8 ↸, \u21B8↸, /\u02C5 /˅, /\u21E9 /⇩, /\u23F7 /⏷, /\u25BC /▼, /\u2304 /⌄, \u2215\u2304 Pessant /↘ Team color"
		String alpha = "\u25A8"; //\u2237 ∷, \u25A8▨
		String animated = " \u23E9"; //\u23EF ⏯, \u21DD⇝, \u23ED ⏭, \u23F5\u23F8⏵⏸, \u25B6\u23F8▶⏸, \u23E9⏩, \u23F2⏲
		String texture = "\u25A3"; //\u22A2 ⊢, 22A1⊡, \u25A3 ▣
		if (layers.size() > 0) {
			if (SHADER_HD_DEFAULT_UNIT.equals(shaderString)) {
				try {
					name.append(over).append(layers.get(0).getTextureBitmap().getName());
					if (layers.get(0).find("Alpha") != null) {
						name.append(animated + alpha);
					}
				} catch (final NullPointerException e) {
//					name.append(over).append(animated + texture).append("animated texture layers (").append(layers.get(0).textures.get(0).getName()).append(")");
					name.append(over).append(animated + texture).append("(").append(layers.get(0).getTextures().get(0).getName()).append(")");
				}
			} else {
				if (layers.get(layers.size() - 1).getTextureBitmap() != null) {
					name = new StringBuilder(layers.get(layers.size() - 1).getTextureBitmap().getName());
					if (layers.get(layers.size() - 1).find("Alpha") != null) {
						name.append(animated + alpha);
					}
				} else {
					name = new StringBuilder(animated + texture);
				}
				for (int i = layers.size() - 2; i >= 0; i--) {
					Layer layer = layers.get(i);
					try {
						Bitmap textureBitmap = layer.getTextureBitmap();
						if(textureBitmap != null){
							name.append(over).append(textureBitmap.getName());
						}
						if (layer.find("Alpha") != null) {
							name.append(animated + alpha);
						}
					} catch (final NullPointerException e) {
//						name.append(over).append(animated + texture).append("animated texture layers (").append(layers.get(i).textures.get(0).getName()).append(")");
						name.append(over).append(animated + texture).append("(").append(layer.getTextures().get(0).getName()).append(")");
					}
				}
			}
		} else {
			name.append("This material got no layers!");
		}
		return name.toString();
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
		this.layers = layers;
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
//		System.out.println("equals");
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Material other = (Material) obj;

		if ((constantColor != other.constantColor) || (sortPrimsFarZ != other.sortPrimsFarZ)
				|| (fullResolution != other.fullResolution) || (twoSided != other.twoSided)) {
			return false;
		}
		if (layers == null) {
			if (other.layers != null) {
				return false;
			}
		} else if (!layers.equals(other.layers)) {
			return false;
		}
		if (priorityPlane != other.priorityPlane) {
			return false;
		}
		if (shaderString == null) {
			return other.shaderString == null;
		} else {
			return shaderString.equals(other.shaderString);
		}
	}

	public boolean getConstantColor() {
		return constantColor;
	}

	public void setConstantColor(final boolean constantColor) {
		this.constantColor = constantColor;
	}

	public boolean getSortPrimsFarZ() {
		return sortPrimsFarZ;
	}

	public void setSortPrimsFarZ(final boolean sortPrimsFarZ) {
		this.sortPrimsFarZ = sortPrimsFarZ;
	}

	public boolean getFullResolution() {
		return fullResolution;
	}

	public void setFullResolution(final boolean fullResolution) {
		this.fullResolution = fullResolution;
	}

	public boolean getTwoSided() {
		return twoSided;
	}

	public void setTwoSided(final boolean twoSided) {
		this.twoSided = twoSided;
	}

	public Material deepCopy(){
		return new Material(this);
	}

	public boolean isHD(){
		return shaderString.equals(SHADER_HD_DEFAULT_UNIT);
	}
}
