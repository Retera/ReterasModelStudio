package com.hiveworkshop.wc3.mdl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.swing.JOptionPane;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.gui.datachooser.DataSource;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.v2.MaterialView;
import com.hiveworkshop.wc3.mdx.LayerChunk;
import com.hiveworkshop.wc3.mdx.MaterialChunk;
import com.hiveworkshop.wc3.util.MathUtils;
import com.hiveworkshop.wc3.util.ModelUtils;

import de.wc3data.image.TgaFile;

/**
 * A class for MDL materials.
 *
 * Eric Theller 11/5/2011
 */
public class Material implements MaterialView {
	public static final String SHADER_HD_DEFAULT_UNIT = "Shader_HD_DefaultUnit";
	public static final String SHADER_SD_FIXED_FUNCTION = "Shader_SD_FixedFunction";
	public static int teamColor = 00;
	com.etheller.collections.ArrayList<Layer> layers;
	private int priorityPlane = 0;
	// "flags" are my way of dealing with all the stuff that I
	// forget/don't bother with: "Unshaded," "Unfogged,"
	// "TwoSided," "CoordId X," actually CoordId was
	// moved into its own field
	private ArrayList<String> flags = new ArrayList<>();

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
			final Bitmap layer1DiffuseTexture = layers.get(layers.size() - 1).getShaderTextures()
					.get(ShaderTextureTypeHD.Diffuse);
			if (layer1DiffuseTexture != null) {
				name = layer1DiffuseTexture.getName();
				if (layers.get(layers.size() - 1).getFlag("Alpha") != null) {
					name = name + " (animated Alpha)";
				}
			}
			else {
				name = "animated texture layers";
			}
			for (int i = layers.size() - 2; i >= 0; i--) {
				try {
					name = name + " over "
							+ layers.get(i).getShaderTextures().get(ShaderTextureTypeHD.Diffuse).getName();
					if (layers.get(i).getFlag("Alpha") != null) {
						name = name + " (animated Alpha)";
					}
				}
				catch (final NullPointerException e) {
					name = name + " over " + "animated texture layers (" + layers.get(i).textures.get(0).getName()
							+ ")";
				}
			}
		}
		else {
			name = "(Material with no layers)";
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
		if (ModelUtils.isShaderStringSupported(mdlObject.getFormatVersion())) {
			if (SHADER_HD_DEFAULT_UNIT.equals(mat.shader)) {
				// condense layer(s) to single layer
				final LayerChunk.Layer firstLayer = mat.layerChunk.layer[0];

				final Layer condensedLayer = new Layer(firstLayer);
				condensedLayer.setLayerShader(LayerShader.HD);
				for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
					final LayerChunk.Layer mdx1000Layer = mat.layerChunk
							.getLayerIfAvailable(shaderTextureTypeHD.ordinal());
					if (mdx1000Layer != null) {
						condensedLayer.getShaderTextures().put(shaderTextureTypeHD,
								mdlObject.getTexture(mdx1000Layer.textureId));

						if (shaderTextureTypeHD != ShaderTextureTypeHD.Diffuse) {
							if (mdx1000Layer.materialTextureId != null) {
								final AnimFlag flag = new AnimFlag(mdx1000Layer.materialTextureId);
								flag.setName(shaderTextureTypeHD.name() + flag.getName());
								condensedLayer.add(flag);
							}
						}
					}
				}
				condensedLayer.updateTextureListIfApplicable(mdlObject);
				layers.add(condensedLayer);
			}
			else {
				for (final LayerChunk.Layer lay : mat.layerChunk.layer) {
					final Layer layer = new Layer(lay);
					layer.updateRefs(mdlObject);
					layer.setLayerShader(LayerShader.SD);
					layers.add(layer);
				}
			}
		}
		else {
			for (final LayerChunk.Layer lay : mat.layerChunk.layer) {
				final Layer layer = new Layer(lay);
				layer.updateRefs(mdlObject);
				layers.add(layer);
			}
		}
		for (final Layer layer : layers) {
			if (layer.getLayerShader() == null) {
				throw new IllegalStateException("Null layer shader");
			}
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
		if (ModelUtils.isShaderStringSupported(mdlObject.getFormatVersion())
				&& EditableModel.hasFlag(mat.flags, 0x02)) {
			add("TwoSided");
		}
	}

	public void add(final String flag) {
		flags.add(flag);
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
				lay.updateTextureAnim(list);
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
		result = (prime * result) + (flags == null ? 0 : flags.hashCode());
		result = (prime * result) + (layers == null ? 0 : layers.hashCode());
		result = (prime * result) + priorityPlane;
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
		}
		else if (!flags.equals(other.flags)) {
			return false;
		}
		if (layers == null) {
			if (other.layers != null) {
				return false;
			}
		}
		else if (!ListView.Util.equalContents(layers, other.layers)) {
			return false;
		}
		if (priorityPlane != other.priorityPlane) {
			return false;
		}
		return true;
	}

	public static Material read(final BufferedReader mdl, final EditableModel mdlr) {
		String line = MDLReader.nextLine(mdl);
		String shaderString = null;
		if (line.contains("Material")) {
			final Material mat = new Material();
			MDLReader.mark(mdl);
			while (!(line = MDLReader.nextLine(mdl)).contains("\t}")) {
				if (line.contains("Layer")) {
					MDLReader.reset(mdl);
					mat.layers.add(Layer.read(mdl, mdlr));
					MDLReader.mark(mdl);
				}
				else if (line.contains("PriorityPlane")) {
					mat.priorityPlane = MDLReader.readInt(line);
				}
				else if (line.contains("Shader")) {
					shaderString = MDLReader.readName(line);
				}
				else {
					mat.flags.add(MDLReader.readFlag(line));
					// JOptionPane.showMessageDialog(MDLReader.getDefaultContainer(),"Error
					// parsing Material: Unrecognized statement
					// '"+line[i]+"'.");
				}
				MDLReader.mark(mdl);
			}
			if (SHADER_HD_DEFAULT_UNIT.equals(shaderString) && (mat.layers.size() > 0)) {
				// condense layer(s) to single layer
				final Layer firstLayer = mat.layers.get(0);

				firstLayer.setLayerShader(LayerShader.HD);
				for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
					final int ordinal = shaderTextureTypeHD.ordinal();
					if (ordinal < mat.layers.size()) {
						final Layer mdx1000Layer = mat.layers.get(ordinal);
						if (mdx1000Layer != null) {
							firstLayer.getShaderTextures().put(shaderTextureTypeHD,
									mdx1000Layer.getShaderTextures().get(ShaderTextureTypeHD.Diffuse));
							if (shaderTextureTypeHD != ShaderTextureTypeHD.Diffuse) {
								final AnimFlag textureIdFlag = mdx1000Layer.getFlag("TextureID");
								if (textureIdFlag != null) {
									textureIdFlag.setName(shaderTextureTypeHD.name() + textureIdFlag.getName());
									firstLayer.add(textureIdFlag);
								}
							}
						}
					}
				}
				mat.layers.clear();
				mat.layers.add(firstLayer);
				firstLayer.updateTextureListIfApplicable(mdlr);
			}
			return mat;
		}
		else {
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
		}
		else {
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
		if (ModelUtils.isShaderStringSupported(version)) {
			String shaderString;
			if ((layers.size() > 0) && (layers.get(0).getLayerShader() == LayerShader.HD)) {
				shaderString = SHADER_HD_DEFAULT_UNIT;
			}
			else {
				shaderString = "";
			}
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
			final Layer layer = layers.get(i);
			layer.printTo(writer, tabHeight + 1, useCoords, version);
			if (ModelUtils.isShaderStringSupported(version) && (layer.getLayerShader() == LayerShader.HD)) {
				for (final ShaderTextureTypeHD shaderTextureTypeHD : ShaderTextureTypeHD.VALUES) {
					if (shaderTextureTypeHD != ShaderTextureTypeHD.Diffuse) {
						final Integer textureId = layer.getShaderTextureIds().get(shaderTextureTypeHD);
						if (textureId != null) {
							final Layer outputLayer = new Layer(FilterMode.NONE.getMdlText(), textureId);
							final AnimFlag specialTextureIDFlag = layer
									.getFlag(shaderTextureTypeHD.name() + "TextureID");
							if (specialTextureIDFlag != null) {
								final AnimFlag genericTextureIDFlag = new AnimFlag(specialTextureIDFlag);
								genericTextureIDFlag.setName("TextureID");
								outputLayer.add(genericTextureIDFlag);
							}
							outputLayer.printTo(writer, tabHeight + 1, useCoords, version);
						}
					}
				}
			}
		}
		writer.println(tabs + "}");
	}

	public BufferedImage getBufferedImage(final DataSource workingDirectory) {
		BufferedImage theImage = null;
		for (int i = 0; i < layers.size(); i++) {
			final Layer lay = layers.get(i);
			final Bitmap tex = lay.firstTexture();

			final BufferedImage newImage = tex.getBufferedImage(workingDirectory);
			if (theImage == null) {
				theImage = newImage;
			}
			else if (newImage != null) {
				theImage = mergeImage(theImage, newImage);
			}
		}

		return theImage;
	}

	/**
	 * Creates and saves the file for the baked texture corresponding to this
	 * material, then returns the texture path name of the created file to be used
	 * in the model Bitmap in the future.
	 *
	 * @param workingDirectory
	 * @param outputDirectory
	 * @param model
	 * @param lod
	 * @return
	 */
	public String getBakedHDNonEmissiveBufferedImage(final DataSource workingDirectory, final File outputDirectory,
			final EditableModel model, final int lod, final Map<Triangle, Integer> triangleToTeamColorPixelCount) {
		if ((layers.size() == 1) && (layers.get(0).getLayerShader() == LayerShader.HD)) {
			final Layer zeroLayer = layers.get(0);

			class BakingCell {
				int diffuseRGB;
				int normalRGB;
				int ormRGB;
				public Vertex barycentricNormal;
				public Vertex barycentricPosition;
				public double[] barycentricTangent;
				public Vector3f tangentLightPos;
				public Vector3f tangentViewPos;
				public Vector3f tangentFragPos;
				public int outputARGB;
			}

			final String diffuseTextureDataRenderableFilePath = getRenderableTexturePath(
					zeroLayer.getShaderTextures().get(ShaderTextureTypeHD.Diffuse));
			final BufferedImage diffuseTextureData = BLPHandler.get().getTexture(workingDirectory,
					diffuseTextureDataRenderableFilePath);
			final BufferedImage normalTextureData = BLPHandler.get().getTexture(workingDirectory,
					getRenderableTexturePath(zeroLayer.getShaderTextures().get(ShaderTextureTypeHD.Normal)));
			final BufferedImage ormTextureData = BLPHandler.get().getTexture(workingDirectory,
					getRenderableTexturePath(zeroLayer.getShaderTextures().get(ShaderTextureTypeHD.ORM)));
			final BufferedImage reflectionsTextureData = BLPHandler.get().getTexture(workingDirectory,
					getRenderableTexturePath(zeroLayer.getShaderTextures().get(ShaderTextureTypeHD.Reflections)));
			final int diffuseTextureDataWidth = diffuseTextureData.getWidth();
			final int diffuseTextureDataHeight = diffuseTextureData.getHeight();
			System.out.println("Diffuse: " + diffuseTextureDataWidth + " x " + diffuseTextureDataHeight);
			final int normalTextureDataWidth = normalTextureData.getWidth();
			final int normalTextureDataHeight = normalTextureData.getHeight();
			System.out.println("Normal: " + normalTextureDataWidth + " x " + normalTextureDataHeight);
			final int ormTextureDataWidth = ormTextureData.getWidth();
			final int ormTextureDataHeight = ormTextureData.getHeight();
			System.out.println("Orm: " + ormTextureDataWidth + " x " + ormTextureDataHeight);
			System.out.println(
					"Reflections: " + reflectionsTextureData.getWidth() + " x " + reflectionsTextureData.getHeight());
//			if (diffuseTextureDataWidth != normalTextureDataWidth || normalTextureDataWidth != ormTextureDataWidth) {
//				new IllegalStateException(
//						"Baking failed because of differing texture widths; maybe we should update the algorithm?")
//								.printStackTrace();
//				return null;
//			}
//			if (diffuseTextureDataHeight != normalTextureDataHeight
//					|| normalTextureDataHeight != ormTextureDataHeight) {
//				new IllegalStateException(
//						"Baking failed because of differing texture heights; maybe we should update the algorithm?")
//								.printStackTrace();
//				return null;
//			}
			final BakingCell[][] bakingCells = new BakingCell[diffuseTextureDataHeight][diffuseTextureDataWidth];
			for (int i = 0; i < bakingCells.length; i++) {
				for (int j = 0; j < bakingCells[i].length; j++) {
					bakingCells[i][j] = new BakingCell();
					bakingCells[i][j].diffuseRGB = diffuseTextureData.getRGB(j % diffuseTextureDataWidth,
							i % diffuseTextureDataHeight);
					bakingCells[i][j].normalRGB = normalTextureData.getRGB(j % normalTextureDataWidth,
							i % normalTextureDataHeight);
					bakingCells[i][j].ormRGB = ormTextureData.getRGB(j % ormTextureDataWidth, i % ormTextureDataHeight);
				}
			}
			final Vector3f viewDirection = new Vector3f(32f, 0, 128f);
			final Vector3f lightDirection = new Vector3f(-24.1937f, 30.4879f, 444.411f);
			if ((model.getCameras().size() > 0)
					&& diffuseTextureDataRenderableFilePath.toLowerCase(Locale.US).contains("portrait")) {
				final Vertex position = model.getCameras().get(0).getPosition();
				viewDirection.set((float) position.x, (float) position.y, (float) position.z);
			}
			class VertexData {

				private final Vector3f tangentLightPos;
				private final Vector3f tangentViewPos;
				private final Vector3f tangentFragPos;

				public VertexData(final Vector3f tangentLightPos, final Vector3f tangentViewPos,
						final Vector3f tangentFragPos) {
					this.tangentLightPos = tangentLightPos;
					this.tangentViewPos = tangentViewPos;
					this.tangentFragPos = tangentFragPos;
				}

			}
			final Map<Vertex, VertexData> vertexToData = new HashMap<>();
			final Vector3f temp = new Vector3f();
			for (final Geoset geo : model.getGeosets()) {
				if ((geo.getLevelOfDetail() != lod) && (geo.getLevelOfDetail() != -1)) {
					continue;
				}
				if ((geo.getMaterial() == this) || geo.getMaterial().equals(this) || geo.getMaterial().getLayers()
						.get(0).firstTexture().getPath().equals(zeroLayer.firstTexture().getPath())) {
					for (final GeosetVertex vertex : geo.getVertices()) {
						// hacky fake vertex shader-like thing (should closely match with code in vertex
						// shader for HD previewing)
						final Vector3f tangent = new Vector3f(vertex.getTangent()[0], vertex.getTangent()[1],
								vertex.getTangent()[2]);
						final Vector3f normal = new Vector3f((float) vertex.getNormal().x, (float) vertex.getNormal().y,
								(float) vertex.getNormal().z);
						temp.set(normal).scale(Vector3f.dot(tangent, normal));
						if (tangent.equals(temp)) {
							Vector3f.sub(tangent, temp, tangent);
							if (tangent.lengthSquared() != 0) {
								tangent.normalise();
							}
						}
						final Vector3f binormal = new Vector3f();
						Vector3f.cross(normal, tangent, binormal);
						binormal.scale(vertex.getTangent()[3]);
						if (binormal.length() != 0) {
							binormal.normalise();
						}
						final Matrix3f tbn = new Matrix3f();
						tbn.m00 = tangent.x;
						tbn.m10 = tangent.y;
						tbn.m20 = tangent.z;
						tbn.m01 = binormal.x;
						tbn.m11 = binormal.y;
						tbn.m21 = binormal.z;
						tbn.m02 = normal.x;
						tbn.m12 = normal.y;
						tbn.m22 = normal.z;

						// light position in transformed TBN space
						final Vector3f tangentLightPos = new Vector3f();
						Matrix3f.transform(tbn, lightDirection, tangentLightPos);

						// view position in transformed TBN space
						final Vector3f tangentViewPos = new Vector3f();
						Matrix3f.transform(tbn, viewDirection, tangentViewPos);

						// frag pos in transformed tbn space
						final Vector3f tangentFragPos = new Vector3f((float) vertex.x, (float) vertex.y,
								(float) vertex.z);
						Matrix3f.transform(tbn, tangentFragPos, tangentFragPos);
						vertexToData.put(vertex, new VertexData(tangentLightPos, tangentViewPos, tangentFragPos));
					}
				}
			}
			for (final Geoset geo : model.getGeosets()) {
				if ((geo.getLevelOfDetail() != lod) && (geo.getLevelOfDetail() != -1)) {
					continue;
				}
				if ((geo.getMaterial() == this) || geo.getMaterial().equals(this) || geo.getMaterial().getLayers()
						.get(0).firstTexture().getPath().equals(zeroLayer.firstTexture().getPath())) {
					// find geosets bound to this material, needed for baking
					for (final Triangle tri : geo.getTriangles()) {
						// find the triangles using this material, since we need to eval them in 3d
						// space to bake
						final GeosetVertex[] verts = tri.getVerts();
						final GeosetVertex g0 = verts[0];
						final GeosetVertex g1 = verts[1];
						final GeosetVertex g2 = verts[2];
						final TVertex tv0 = g0.getTVertex(0);
						final TVertex tv1 = g1.getTVertex(0);
						final TVertex tv2 = g2.getTVertex(0);
						final double minX = Math.min(tv0.getX(), Math.min(tv1.getX(), tv2.getX()));
						final double minY = Math.min(tv0.getY(), Math.min(tv1.getY(), tv2.getY()));
						final double maxX = Math.max(tv0.getX(), Math.max(tv1.getX(), tv2.getX()));
						final double maxY = Math.max(tv0.getY(), Math.max(tv1.getY(), tv2.getY()));
						final int iminX = (int) Math.floor(minX * bakingCells[0].length);
						final int iminY = (int) Math.floor(minY * bakingCells.length);
						final int imaxX = (int) Math.ceil(maxX * bakingCells[0].length);
						final int imaxY = (int) Math.ceil(maxY * bakingCells.length);
						final int[] polygonXPoints = { (int) Math.round(tv0.getX() * bakingCells[0].length),
								(int) Math.round(tv1.getX() * bakingCells[0].length),
								(int) Math.round(tv2.getX() * bakingCells[0].length) };
						final int[] polygonYPoints = { (int) Math.round(tv0.getY() * bakingCells.length),
								(int) Math.round(tv1.getY() * bakingCells.length),
								(int) Math.round(tv2.getY() * bakingCells.length) };
						final Polygon polygon = new Polygon(polygonXPoints, polygonYPoints, 3);
						final VertexData vertexData0 = vertexToData.get(g0);
						final VertexData vertexData1 = vertexToData.get(g1);
						final VertexData vertexData2 = vertexToData.get(g2);
						int teamColorPixels = 0;
						for (int i = iminY; i <= imaxY; i++) {
							for (int j = iminX; j <= imaxX; j++) {
								if (polygon.contains(j, i)) {
									final int jToUse = ((j % bakingCells[0].length) + bakingCells[0].length)
											% bakingCells[0].length;
									final int iToUse = ((i % bakingCells.length) + bakingCells.length)
											% bakingCells.length;
									final double unitSpaceX = (double) jToUse / (double) bakingCells[0].length;
									final double unitSpaceY = (double) iToUse / (double) bakingCells.length;

									// barycentric
									final double denom = MathUtils.areaOfTriangle(tv0.x, tv0.y, tv1.x, tv1.y, tv2.x,
											tv2.y);
									final double b0 = MathUtils.areaOfTriangle(unitSpaceX, unitSpaceY, tv1.x, tv1.y,
											tv2.x, tv2.y) / denom;
									final double b1 = MathUtils.areaOfTriangle(tv0.x, tv0.y, unitSpaceX, unitSpaceY,
											tv2.x, tv2.y) / denom;
									final double b2 = MathUtils.areaOfTriangle(tv0.x, tv0.y, tv1.x, tv1.y, unitSpaceX,
											unitSpaceY) / denom;

									bakingCells[iToUse][jToUse].barycentricNormal = new Vertex(
											(g0.getNormal().x * b0) + (g1.getNormal().x * b1) + (g2.getNormal().x * b2),
											(g0.getNormal().y * b0) + (g1.getNormal().y * b1) + (g2.getNormal().y * b2),
											(g0.getNormal().z * b0) + (g1.getNormal().z * b1)
													+ (g2.getNormal().z * b2));

									bakingCells[iToUse][jToUse].barycentricPosition = new Vertex(
											(g0.x * b0) + (g1.x * b1) + (g2.x * b2),
											(g0.y * b0) + (g1.y * b1) + (g2.y * b2),
											(g0.z * b0) + (g1.z * b1) + (g2.z * b2));

									bakingCells[iToUse][jToUse].barycentricTangent = new double[] {
											(g0.getTangent()[0] * b0) + (g1.getTangent()[0] * b1)
													+ (g2.getTangent()[0] * b2),
											(g0.getTangent()[1] * b0) + (g1.getTangent()[1] * b1)
													+ (g2.getTangent()[1] * b2),
											(g0.getTangent()[2] * b0) + (g1.getTangent()[2] * b1)
													+ (g2.getTangent()[2] * b2),
											g0.getTangent()[3] };

									bakingCells[iToUse][jToUse].tangentLightPos = new Vector3f(
											(float) ((vertexData0.tangentLightPos.x * b0)
													+ (vertexData1.tangentLightPos.x * b1)
													+ (vertexData2.tangentLightPos.x * b2)),
											(float) ((vertexData0.tangentLightPos.y * b0)
													+ (vertexData1.tangentLightPos.y * b1)
													+ (vertexData2.tangentLightPos.y * b2)),
											(float) ((vertexData0.tangentLightPos.z * b0)
													+ (vertexData1.tangentLightPos.z * b1)
													+ (vertexData2.tangentLightPos.z * b2)));

									bakingCells[iToUse][jToUse].tangentViewPos = new Vector3f(
											(float) ((vertexData0.tangentViewPos.x * b0)
													+ (vertexData1.tangentViewPos.x * b1)
													+ (vertexData2.tangentViewPos.x * b2)),
											(float) ((vertexData0.tangentViewPos.y * b0)
													+ (vertexData1.tangentViewPos.y * b1)
													+ (vertexData2.tangentViewPos.y * b2)),
											(float) ((vertexData0.tangentViewPos.z * b0)
													+ (vertexData1.tangentViewPos.z * b1)
													+ (vertexData2.tangentViewPos.z * b2)));

									bakingCells[iToUse][jToUse].tangentFragPos = new Vector3f(
											(float) ((vertexData0.tangentFragPos.x * b0)
													+ (vertexData1.tangentFragPos.x * b1)
													+ (vertexData2.tangentFragPos.x * b2)),
											(float) ((vertexData0.tangentFragPos.y * b0)
													+ (vertexData1.tangentFragPos.y * b1)
													+ (vertexData2.tangentFragPos.y * b2)),
											(float) ((vertexData0.tangentFragPos.z * b0)
													+ (vertexData1.tangentFragPos.z * b1)
													+ (vertexData2.tangentFragPos.z * b2)));

									if (((bakingCells[iToUse][jToUse].ormRGB >>> 24) & 0xFF) > 0) {
										teamColorPixels++;
									}
								}
							}
						}

						triangleToTeamColorPixelCount.put(tri, teamColorPixels);
					}
				}
			}
			int nShadedPixels = 0;
			int nDiffusePixels = 0;
			final BufferedImage bakedImg = new BufferedImage(bakingCells[0].length, bakingCells.length,
					BufferedImage.TYPE_INT_ARGB);
			for (int i = 0; i < bakingCells.length; i++) {
				for (int j = 0; j < bakingCells[i].length; j++) {
					// fake fragment shader thing, obviously will be very wrong in some cases if the
					// texture is used on multiple triangles in different places

					final Vector3f fragColorRGB = new Vector3f();

					final BakingCell bakingCell = bakingCells[i][j];

					final float teamColorNess = ((bakingCell.ormRGB >> 24) & 0xFF) / 255.0f;
					final float nonTeamColorNess = 1.0f - teamColorNess;

					final float baseRed = ((bakingCell.diffuseRGB >> 16) & 0xFF) / 255.0f;
					final float baseGreen = ((bakingCell.diffuseRGB >> 8) & 0xFF) / 255.0f;
					final float baseBlue = ((bakingCell.diffuseRGB >> 0) & 0xFF) / 255.0f;
					final Vector3f diffuse = new Vector3f(baseRed, baseGreen, baseBlue);
					diffuse.scale(nonTeamColorNess);
					if (bakingCell.tangentFragPos != null) {
						final float normalX = ((((bakingCell.normalRGB >> 16) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
						final float normalY = ((((bakingCell.normalRGB >> 8) & 0xFF) / 255.0f) * 2.0f) - 1.0f;
						final Vector3f normal = new Vector3f(normalY, normalX,
								(float) Math.sqrt(1.0 - ((normalX * normalX) + (normalY * normalY))));
						final Vector3f lightDir = new Vector3f(0, 0, 1);// bakingCell.tangentViewPos;
						lightDir.set(bakingCell.tangentLightPos);
						lightDir.normalise();
						{
							final float cosTheta = (Vector3f.dot(lightDir, normal) * 0.5f) + 0.5f;
							final float lambertFactor = (float) Math.max(0.0, Math.min(1.0, cosTheta));

							diffuse.scale((float) Math.max(0.0, Math.min(1.0, lambertFactor)));
						}
						final Vector3f viewDir = new Vector3f();
						Vector3f.sub(bakingCell.tangentViewPos, bakingCell.tangentFragPos, viewDir);
						viewDir.normalise();
						final Vector3f reflectDir = new Vector3f();
						lightDir.scale(-1);
						MathUtils.reflect(lightDir, normal, reflectDir);
						lightDir.scale(-1);
						final Vector3f halfwayDir = new Vector3f();
						Vector3f.add(lightDir, viewDir, halfwayDir);
						halfwayDir.normalise();
						final float spec = (float) Math.pow(Math.max(Vector3f.dot(normal, halfwayDir), 0.0f), 32.0f);
						final float metalness = ((bakingCell.ormRGB >> 0) & 0xFF) / 255.0f;
						final float roughness = ((bakingCell.ormRGB >> 8) & 0xFF) / 255.0f;
//						"			vec3 specular = vec3(max(-ormTexel.g+0.5, 0.0)+ormTexel.b) * spec * (reflectionsTexel.xyz * (1.0 - ormTexel.g) + ormTexel.g * color.xyz);\r\n"
						final float specularX = (float) ((Math.max(-roughness + 0.5, 0.0) + metalness) * spec);
						final Vector3f specular = new Vector3f(specularX, specularX, specularX);
						// TODO maybe fresnel here
						Vector3f.add(specular, diffuse, fragColorRGB);
						nShadedPixels++;
					}
					else {
						fragColorRGB.set(diffuse);
						nDiffusePixels++;
					}

					final int red = Math.round(Math.min(255, fragColorRGB.x * 255f)) & 0xFF;
					final int green = Math.round(Math.min(255, fragColorRGB.y * 255f)) & 0xFF;
					final int blue = Math.round(Math.min(255, fragColorRGB.z * 255f)) & 0xFF;

					float alpha = ((bakingCell.diffuseRGB >> 24) & 0xFF) / 255.0f;
					alpha *= 1.0f - (teamColorNess * Math.max(baseRed, Math.max(baseGreen, baseBlue)));
					final int alphaI = Math.round(alpha * 255f) & 0xFF;

					bakingCell.outputARGB = (alphaI << 24) | (red << 16) | (green << 8) | (blue << 0);
					bakedImg.setRGB(j, i, bakingCell.outputARGB);
				}
			}
			System.out.println("baked texture with " + nShadedPixels + " pixels loading shader data and "
					+ nDiffusePixels + " pixels defaulting back to diffuse data");

			try {
				final String diffusePath = zeroLayer.firstTexture().getPath();
				final String diffuseName = diffusePath
						.substring(Math.max(diffusePath.lastIndexOf('/'), diffusePath.lastIndexOf('\\')) + 1);
//				ImageIO.write(diffuseTextureData, "png",
//						);
				final String newTexturePath = diffuseName + "_baked.tga";
				TgaFile.writeTGA(bakedImg, new File(outputDirectory.getPath() + "/" + newTexturePath));
				return newTexturePath;
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
//			diffuseLayer.get

		}
		else {
			throw new RuntimeException("Failed to begin baking HD -> SD texture, did not find 6 layers!");
		}
	}

	/**
	 * Intended to handle resolving ReplaceableIds into paths
	 *
	 * @param tex
	 * @return
	 */
	public static String getRenderableTexturePath(final Bitmap tex) {
		if (tex == null) {
			return "Textures\\white.blp";
		}
		String path = tex.getPath();
		if (path.length() == 0) {
			if (tex.getReplaceableId() == 1) {
				path = "ReplaceableTextures\\TeamColor\\TeamColor0" + teamColor + ".blp";
			}
			else if (tex.getReplaceableId() == 2) {
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
