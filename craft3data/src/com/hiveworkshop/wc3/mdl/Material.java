package com.hiveworkshop.wc3.mdl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import com.etheller.warsmash.parsers.mdlx.MdlxLayer;
import com.etheller.warsmash.parsers.mdlx.MdlxMaterial;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.util.ModelUtils;

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
		this.layers.add(layer);
	}

	public Material(final List<Layer> layers) {
		this.layers.addAll(layers);
	}

	public Material(final Material material) {
		this.layers.addAll(material.layers);
		this.priorityPlane = material.priorityPlane;
		this.shaderString = material.shaderString;
		this.constantColor = material.constantColor;
		this.sortPrimsFarZ = material.sortPrimsFarZ;
		this.fullResolution = material.fullResolution;
		this.twoSided = material.twoSided;
	}

	public Material(final MdlxMaterial material, final EditableModel editableModel) {
		this();

		for (final MdlxLayer mdlxLayer : material.layers) {
			final Layer layer = new Layer(mdlxLayer);

			layer.updateRefs(editableModel);

			this.layers.add(layer);
		}

		setPriorityPlane(material.priorityPlane);

		if ((material.flags & 0x1) != 0) {
			this.constantColor = true;
		}

		if ((material.flags & 0x10) != 0) {
			this.sortPrimsFarZ = true;
		}

		if ((material.flags & 0x20) != 0) {
			this.fullResolution = true;
		}

		if (ModelUtils.isShaderStringSupported(editableModel.getFormatVersion()) && ((material.flags & 0x2) != 0)) {
			this.twoSided = true;
		}

		this.shaderString = material.shader;
	}

	public MdlxMaterial toMdlx() {
		final MdlxMaterial material = new MdlxMaterial();

		for (final Layer layer : getLayers()) {
			material.layers.add(layer.toMdlx());
		}

		material.priorityPlane = getPriorityPlane();

		if (this.constantColor) {
			material.flags |= 0x1;
		}

		if (this.sortPrimsFarZ) {
			material.flags |= 0x10;
		}

		if (this.fullResolution) {
			material.flags |= 0x20;
		}

		if (this.twoSided) {
			material.flags |= 0x2;
		}

		material.shader = this.shaderString;

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
		if (this.layers.size() > 0) {
			if (SHADER_HD_DEFAULT_UNIT.equals(this.shaderString)) {
				try {
					name = name + " over " + this.layers.get(0).texture.getName();
					if (this.layers.get(0).find("Alpha") != null) {
						name = name + " (animated Alpha)";
					}
				} catch (final NullPointerException e) {
					name = name + " over " + "animated texture layers (" + this.layers.get(0).textures.get(0).getName()
							+ ")";
				}
			} else {
				if (this.layers.get(this.layers.size() - 1).texture != null) {
					name = this.layers.get(this.layers.size() - 1).texture.getName();
					if (this.layers.get(this.layers.size() - 1).find("Alpha") != null) {
						name = name + " (animated Alpha)";
					}
				} else {
					name = "animated texture layers";
				}
				for (int i = this.layers.size() - 2; i >= 0; i--) {
					try {
						name = name + " over " + this.layers.get(i).texture.getName();
						if (this.layers.get(i).find("Alpha") != null) {
							name = name + " (animated Alpha)";
						}
					} catch (final NullPointerException e) {
						name = name + " over " + "animated texture layers ("
								+ this.layers.get(i).textures.get(0).getName() + ")";
					}
				}
			}
		}
		return name;
	}

	public Layer firstLayer() {
		if (this.layers.size() > 0) {
			return this.layers.get(this.layers.size() - 1);
		}
		return null;
	}

	public String getShaderString() {
		return this.shaderString;
	}

	public void setShaderString(final String shaderString) {
		this.shaderString = shaderString;
	}

	public List<Layer> getLayers() {
		return this.layers;
	}

	public void setLayers(final List<Layer> layers) {
		this.layers = layers;
	}

	public int getPriorityPlane() {
		return this.priorityPlane;
	}

	public void setPriorityPlane(final int priorityPlane) {
		this.priorityPlane = priorityPlane;
	}

	public void updateTextureAnims(final List<TextureAnim> list) {
		final int sz = this.layers.size();
		for (int i = 0; i < sz; i++) {
			final Layer lay = this.layers.get(i);
			if (lay.hasTexAnim()) {
				lay.setTextureAnim(list);
			}
		}
	}

	public void updateReferenceIds(final EditableModel mdlr) {
		for (final Layer lay : this.layers) {
			lay.updateIds(mdlr);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((this.layers == null) ? 0 : this.layers.hashCode());
		result = (prime * result) + this.priorityPlane;
		result = (prime * result) + ((this.shaderString == null) ? 0 : this.shaderString.hashCode());
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

		if ((this.constantColor != other.constantColor) || (this.sortPrimsFarZ != other.sortPrimsFarZ)
				|| (this.fullResolution != other.fullResolution) || (this.twoSided != other.twoSided)) {
			return false;
		}
		if (this.layers == null) {
			if (other.layers != null) {
				return false;
			}
		} else if (!this.layers.equals(other.layers)) {
			return false;
		}
		if (this.priorityPlane != other.priorityPlane) {
			return false;
		}
		if (this.shaderString == null) {
			if (other.shaderString != null) {
				return false;
			}
		} else if (!this.shaderString.equals(other.shaderString)) {
			return false;
		}
		return true;
	}

	public boolean getConstantColor() {
		return this.constantColor;
	}

	public void setConstantColor(final boolean constantColor) {
		this.constantColor = constantColor;
	}

	public boolean getSortPrimsFarZ() {
		return this.sortPrimsFarZ;
	}

	public void setSortPrimsFarZ(final boolean sortPrimsFarZ) {
		this.sortPrimsFarZ = sortPrimsFarZ;
	}

	public boolean getFullResolution() {
		return this.fullResolution;
	}

	public void setFullResolution(final boolean fullResolution) {
		this.fullResolution = fullResolution;
	}

	public boolean getTwoSided() {
		return this.twoSided;
	}

	public void setTwoSided(final boolean twoSided) {
		this.twoSided = twoSided;
	}

	public BufferedImage getBufferedImage(final DataSource workingDirectory) {
		BufferedImage theImage = null;
		if (SHADER_HD_DEFAULT_UNIT.equals(this.shaderString) && (this.layers.size() > 0)) {
			final Layer firstLayer = this.layers.get(0);
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
			for (int i = 0; i < this.layers.size(); i++) {
				final Layer lay = this.layers.get(i);
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
