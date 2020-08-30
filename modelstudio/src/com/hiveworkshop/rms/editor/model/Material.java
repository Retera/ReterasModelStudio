package com.hiveworkshop.rms.editor.model;

import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.sources.DataSource;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer;
import com.hiveworkshop.rms.parsers.mdlx.MdlxMaterial;
import com.hiveworkshop.rms.parsers.blp.BLPHandler;

import java.awt.*;
import java.awt.image.BufferedImage;
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

	public Material(final Material material) {
		layers.addAll(material.layers);
		priorityPlane = material.priorityPlane;
		shaderString = material.shaderString;
		constantColor = material.constantColor;
		sortPrimsFarZ = material.sortPrimsFarZ;
		fullResolution = material.fullResolution;
		twoSided = material.twoSided;
	}

	public Material(final MdlxMaterial material, final EditableModel editableModel) {
		this();

		for (final MdlxLayer mdlxLayer : material.layers) {
			final Layer layer = new Layer(mdlxLayer);

			layer.updateRefs(editableModel);

			layers.add(layer);
		}

		setPriorityPlane(material.priorityPlane);

		if ((material.flags & 0x1) != 0) {
			constantColor = true;
		}

		if ((material.flags & 0x10) != 0) {
			sortPrimsFarZ = true;
		}

		if ((material.flags & 0x20) != 0) {
			fullResolution = true;
		}

		if (ModelUtils.isShaderStringSupported(editableModel.getFormatVersion()) && ((material.flags & 0x2) != 0)) {
			twoSided = true;
		}

		shaderString = material.shader;
	}

	public MdlxMaterial toMdlx() {
		final MdlxMaterial material = new MdlxMaterial();

		for (final Layer layer : getLayers()) {
			material.layers.add(layer.toMdlx());
		}

		material.priorityPlane = getPriorityPlane();

		if (constantColor) {
			material.flags |= 0x1;
		}

		if (sortPrimsFarZ) {
			material.flags |= 0x10;
		}

		if (fullResolution) {
			material.flags |= 0x20;
		}

		if (twoSided) {
			material.flags |= 0x2;
		}

		material.shader = shaderString;

		return material;
	}

	public static String getTeamColorNumberString() {
		final String string = Integer.toString(teamColor);
		if (string.length() < 2) {
			return '0' + string;
		}
		return string;
	}

	public String getName() {
		String name = "";
		if (layers.size() > 0) {
			if (SHADER_HD_DEFAULT_UNIT.equals(shaderString)) {
				try {
					name = name + " over " + layers.get(0).texture.getName();
					if (layers.get(0).find("Alpha") != null) {
						name = name + " (animated Alpha)";
					}
				} catch (final NullPointerException e) {
					name = name + " over " + "animated texture layers (" + layers.get(0).textures.get(0).getName()
							+ ")";
				}
			} else {
				if (layers.get(layers.size() - 1).texture != null) {
					name = layers.get(layers.size() - 1).texture.getName();
					if (layers.get(layers.size() - 1).find("Alpha") != null) {
						name = name + " (animated Alpha)";
					}
				} else {
					name = "animated texture layers";
				}
				for (int i = layers.size() - 2; i >= 0; i--) {
					try {
						name = name + " over " + layers.get(i).texture.getName();
						if (layers.get(i).find("Alpha") != null) {
							name = name + " (animated Alpha)";
						}
					} catch (final NullPointerException e) {
						name = name + " over " + "animated texture layers ("
								+ layers.get(i).textures.get(0).getName() + ")";
					}
				}
			}
		}
		return name;
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

	public void setLayers(final List<Layer> layers) {
		this.layers = layers;
	}

	public int getPriorityPlane() {
		return priorityPlane;
	}

	public void setPriorityPlane(final int priorityPlane) {
		this.priorityPlane = priorityPlane;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((layers == null) ? 0 : layers.hashCode());
		result = (prime * result) + priorityPlane;
		result = (prime * result) + ((shaderString == null) ? 0 : shaderString.hashCode());
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

	public BufferedImage getBufferedImage(final DataSource workingDirectory) {
		BufferedImage theImage = null;
		if (SHADER_HD_DEFAULT_UNIT.equals(shaderString) && (layers.size() > 0)) {
			final Layer firstLayer = layers.get(0);
			final Bitmap tex = firstLayer.firstTexture();
			final String path = getRenderableTexturePath(tex);
			BufferedImage newImage;
			try {
				newImage = BLPHandler.get().getTexture(workingDirectory, path);
			} catch (final Exception exc) {
				// newImage = null;
				newImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
			}
			return newImage;
		} else {
            for (final Layer lay : layers) {
                final Bitmap tex = lay.firstTexture();
                final String path = getRenderableTexturePath(tex);
                BufferedImage newImage;
                try {
                    newImage = BLPHandler.get().getTexture(workingDirectory, path);
                } catch (final Exception exc) {
                    // newImage = null;
                    newImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                }
                if (theImage == null) {
                    theImage = newImage;
                } else {
                    if (newImage != null) {
                        theImage = mergeImage(theImage, newImage);
                    }
                }
            }
		}

		return theImage;
	}

	/**
	 * Intended to handle resolving ReplaceableIds into paths
	 *
	 * @param tex
	 * @return
	 */
	private String getRenderableTexturePath(final Bitmap tex) {
		if (tex == null) {
			return "Textures\\white.blp";
		}
		String path = tex.getPath();
		if (path.length() == 0) {
			if (tex.getReplaceableId() == 1) {
				path = "ReplaceableTextures\\TeamColor\\TeamColor0" + teamColor + ".blp";
			} else if (tex.getReplaceableId() == 2) {
				path = "ReplaceableTextures\\TeamGlow\\TeamGlow0" + teamColor + ".blp";
			}
		}
		return path;
	}

	public static BufferedImage mergeImage(final BufferedImage source, final BufferedImage overlay) {
		final int w = Math.max(source.getWidth(), overlay.getWidth());
		final int h = Math.max(source.getHeight(), overlay.getHeight());
		final BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		final Graphics g = combined.getGraphics();
		g.drawImage(source, 0, 0, w, h, null);
		g.drawImage(overlay, 0, 0, w, h, null);

		return combined;
	}

	public static BufferedImage mergeImageScaled(final Image source, final Image overlay, final int w1, final int h1,
			final int w2, final int h2) {
		final int w = Math.max(w1, w2);
		final int h = Math.max(h1, h2);
		final BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

		final Graphics g = combined.getGraphics();
		g.drawImage(source, 0, 0, w1, h1, null);
		g.drawImage(overlay, (w1 - w2) / 2, (h1 - h2) / 2, w2, h2, null);

		return combined;
	}
}
