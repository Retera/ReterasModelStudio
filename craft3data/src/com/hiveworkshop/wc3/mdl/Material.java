package com.hiveworkshop.wc3.mdl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.mdl.v2.MaterialView;
import com.hiveworkshop.wc3.mdx.LayerChunk;
import com.hiveworkshop.wc3.mdx.MaterialChunk;
import com.hiveworkshop.wc3.util.ModelUtils;

/**
 * A class for MDL materials.
 *
 * Eric Theller 11/5/2011
 */
public class Material implements MaterialView {
	public static int teamColor = 00;
	com.etheller.collections.ArrayList<Layer> layers;
	private int priorityPlane = 0;
	// "flags" are my way of dealing with all the stuff that I
	// forget/don't bother with: "Unshaded," "Unfogged,"
	// "TwoSided," "CoordId X," actually CoordId was
	// moved into its own field
	private ArrayList<String> flags = new ArrayList<>();
	private String shaderString;

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
			if (layers.get(layers.size() - 1).texture != null) {
				name = layers.get(layers.size() - 1).texture.getName();
				if (layers.get(layers.size() - 1).getFlag("Alpha") != null) {
					name = name + " (animated Alpha)";
				}
			} else {
				name = "animated texture layers";
			}
			for (int i = layers.size() - 2; i >= 0; i--) {
				try {
					name = name + " over " + layers.get(i).texture.getName();
					if (layers.get(i).getFlag("Alpha") != null) {
						name = name + " (animated Alpha)";
					}
				} catch (final NullPointerException e) {
					name = name + " over " + "animated texture layers (" + layers.get(i).textures.get(0).getName()
							+ ")";
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

	public Material(final Layer lay) {
		layers = new com.etheller.collections.ArrayList<>();
		flags = new ArrayList<>();
		layers.add(lay);
	}

	public Material(final List<Layer> layers) {
		this.layers = new com.etheller.collections.ArrayList<>();
		for (final Layer layer : layers) {
			this.layers.add(layer);
		}
		// this.layers.addAll(layers);
	}

	private Material() {
		layers = new com.etheller.collections.ArrayList<>();
		flags = new ArrayList<>();
	}

	public Material(final Material other) {
		layers = new com.etheller.collections.ArrayList<>();
		flags = new ArrayList<>(other.flags);
		for (final Layer lay : other.layers) {
			layers.add(new Layer(lay));
		}
		priorityPlane = other.priorityPlane;
	}

	public Material(final MaterialChunk.Material mat, final EditableModel mdlObject) {
		this();
		for (final LayerChunk.Layer lay : mat.layerChunk.layer) {
			final Layer layer = new Layer(lay);
			layer.updateRefs(mdlObject);
			layers.add(layer);
		}
		setPriorityPlane(mat.priorityPlane);
		if (EditableModel.hasFlag(mat.flags, 0x1)) {
			add("ConstantColor");
		}
		if (EditableModel.hasFlag(mat.flags, 0x10)) {
			add("SortPrimsFarZ");
		}
		if (EditableModel.hasFlag(mat.flags, 0x20)) {
			add("FullResolution");
		}
		if (ModelUtils.isShaderStringSupported(mdlObject.getFormatVersion()) && EditableModel.hasFlag(mat.flags, 0x02)) {
			add("TwoSided");
		}
		this.shaderString = mat.shader;
	}

	public void add(final String flag) {
		flags.add(flag);
	}

	public String getShaderString() {
		return shaderString;
	}

	public void setShaderString(final String shaderString) {
		this.shaderString = shaderString;
	}

	@Override
	public com.etheller.collections.ArrayList<Layer> getLayers() {
		return layers;
	}

	public void setLayers(final com.etheller.collections.ArrayList<Layer> layers) {
		this.layers = layers;
	}

	@Override
	public int getPriorityPlane() {
		return priorityPlane;
	}

	public void setPriorityPlane(final int priorityPlane) {
		this.priorityPlane = priorityPlane;
	}

	public ArrayList<String> getFlags() {
		return flags;
	}

	public void setFlags(final ArrayList<String> flags) {
		this.flags = flags;
	}

	public void updateTextureAnims(final ArrayList<TextureAnim> list) {
		final int sz = layers.size();
		for (int i = 0; i < sz; i++) {
			final Layer lay = layers.get(i);
			if (lay.hasTexAnim()) {
				lay.setTextureAnim(list);
			}
		}
	}

	public void updateReferenceIds(final EditableModel mdlr) {
		for (final Layer lay : layers) {
			lay.updateIds(mdlr);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((flags == null) ? 0 : flags.hashCode());
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
		if (flags == null) {
			if (other.flags != null) {
				return false;
			}
		} else if (!flags.equals(other.flags)) {
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
			if (other.shaderString != null) {
				return false;
			}
		} else if (!shaderString.equals(other.shaderString)) {
			return false;
		}
		return true;
	}

	public static Material read(final BufferedReader mdl, final EditableModel mdlr) {
		String line = MDLReader.nextLine(mdl);
		if (line.contains("Material")) {
			final Material mat = new Material();
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
				if (line.contains("Layer")) {
					MDLReader.reset(mdl);
					mat.layers.add(Layer.read(mdl, mdlr));
					MDLReader.mark(mdl);
				} else if (line.contains("PriorityPlane")) {
					mat.priorityPlane = MDLReader.readInt(line);
				} else if (line.contains("Shader")) {
					mat.shaderString = MDLReader.readName(line);
				} else {
					mat.flags.add(MDLReader.readFlag(line));
					// JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error
					// parsing Material: Unrecognized statement
					// '"+line[i]+"'.");
				}
				MDLReader.mark(mdl);
			}
			return mat;
		} else {
			JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),
					"Unable to parse Material: Missing or unrecognized open statement.");
		}
		return null;
	}

	public static ArrayList<Material> readAll(final BufferedReader mdl, final EditableModel mdlr) {
		String line = "";
		final ArrayList<Material> outputs = new ArrayList<>();
		MDLReader.mark(mdl);
		if ((line = MDLReader.nextLine(mdl)).contains("Materials")) {
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).startsWith("}")) {
				MDLReader.reset(mdl);
				outputs.add(read(mdl, mdlr));
				MDLReader.mark(mdl);
			}
			return outputs;
		} else {
			MDLReader.reset(mdl);
			// JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Unable
			// to parse Materials: Missing or unrecognized open statement.");
		}
		return outputs;
	}

	public void printTo(final PrintWriter writer, final int tabHeight, final int version) {
		String tabs = "";
		for (int i = 0; i < tabHeight; i++) {
			tabs = tabs + "\t";
		}
		writer.println(tabs + "Material {");
		if ((shaderString != null) && ModelUtils.isShaderStringSupported(version)) {
			writer.println(tabs + "\tShader \"" + shaderString + "\",");
		}
		if (priorityPlane != 0) {
			writer.println(tabs + "\tPriorityPlane " + priorityPlane + ",");
		}
		for (int i = 0; i < flags.size(); i++) {
			writer.println(tabs + "\t" + flags.get(i) + ",");
		}
		boolean useCoords = false;
		for (int i = 0; i < layers.size(); i++) {
			useCoords = layers.get(i).hasCoordId();
			if (useCoords) {
				break;
			}
		}
		for (int i = 0; i < layers.size(); i++) {
			layers.get(i).printTo(writer, tabHeight + 1, useCoords, version);
		}
		writer.println(tabs + "}");
	}

	public BufferedImage getBufferedImage(final DataSource workingDirectory) {
		BufferedImage theImage = null;
		for (int i = 0; i < layers.size(); i++) {
			final Layer lay = layers.get(i);
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
	// public BufferedImage getBufferedImage()
	// {
	// BufferedImage theImage = null;
	// for(int i = 0; i < layers.size(); i++ )
	// {
	// Layer lay = layers.get(i);
	// Bitmap tex = lay.firstTexture();
	// String path = tex.getPath();
	// if( path.length() == 0 )
	// {
	// System.err.println("sup homes");
	// if( tex.getReplaceableId() == 1 )
	// {
	// path = "ReplaceableTextures\\TeamColor\\TeamColor0"+teamColor+".blp";
	// }
	// else if( tex.getReplaceableId() == 2 )
	// {
	// path = "ReplaceableTextures\\TeamGlow\\TeamGlow0"+teamColor+".blp";
	// }
	// }
	// try {
	// BufferedImage newImage = BLPHandler.get().getGameTex(path);
	// if( theImage == null )
	// theImage = newImage;
	// else
	// theImage = mergeImage(theImage, newImage);
	// }
	// catch (Exception exc)
	// {
	// exc.printStackTrace();
	// try {
	// BufferedImage newImage =
	// BLPHandler.get().getCustomTex(MDLReader.getDefaultContainer().currentMDL().getFile().getParent()+"\\"+path);
	// if( theImage == null )
	// theImage = newImage;
	// else
	// theImage = mergeImage(theImage, newImage);
	// }
	// catch (Exception exc2)
	// {
	// exc2.printStackTrace();
	// JOptionPane.showMessageDialog(null, "BLP texture-loader failed.");
	// }
	// }
	// }
	// return theImage;
	// }

	@Override
	public boolean isConstantColor() {
		return flags.contains("ConstantColor");
	}

	@Override
	public boolean isSortPrimsFarZ() {
		return flags.contains("SortPrimsFarZ");
	}

	@Override
	public boolean isFullResolution() {
		return flags.contains("FullResolution");
	}
}
