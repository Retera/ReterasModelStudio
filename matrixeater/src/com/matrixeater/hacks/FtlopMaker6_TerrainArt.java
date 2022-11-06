package com.matrixeater.hacks;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hiveworkshop.wc3.gui.ExceptionPopup;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.LayerShader;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;
import com.hiveworkshop.wc3.mdl.TVertex;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mpq.Codebase;
import com.hiveworkshop.wc3.mpq.MpqCodebase;
import com.hiveworkshop.wc3.units.DataTable;
import com.hiveworkshop.wc3.units.Element;

public class FtlopMaker6_TerrainArt {
	public static EditableModel genericModel() {
		final EditableModel blankTextureModel = new EditableModel("GenericPlane");
		blankTextureModel.setFormatVersion(800);
		final Geoset newGeoset = new Geoset();
		final Layer layer = new Layer("Blend", new Bitmap(""));
		final Material material = new Material(layer);
		newGeoset.setMaterial(material);
		final float aspectRatio = 1;

		final int displayWidth = 128;
		final int displayHeight = 128;

		final int groundOffset = -64;
		final Normal norm = new Normal(0, 0, 1);
		final GeosetVertex upperLeft = new GeosetVertex(-displayWidth / 2, -displayWidth / 2, 0, norm);
		final float[] defaultTangent = new float[] { 1, 0, 0, 1 };
		upperLeft.setTangent(defaultTangent);
		final TVertex upperLeftTVert = new TVertex(0, 0);
		upperLeft.addTVertex(upperLeftTVert);
		newGeoset.add(upperLeft);
		upperLeft.setGeoset(newGeoset);

		final GeosetVertex upperRight = new GeosetVertex(-displayWidth / 2, displayWidth / 2, 0, norm);
		newGeoset.add(upperRight);
		final TVertex upperRightTVert = new TVertex(1, 0);
		upperRight.setTangent(defaultTangent);
		upperRight.addTVertex(upperRightTVert);
		upperRight.setGeoset(newGeoset);

		final GeosetVertex lowerLeft = new GeosetVertex(displayWidth / 2, -displayWidth / 2, 0, norm);
		newGeoset.add(lowerLeft);
		final TVertex lowerLeftTVert = new TVertex(0, 1);
		lowerLeft.setTangent(defaultTangent);
		lowerLeft.addTVertex(lowerLeftTVert);
		lowerLeft.setGeoset(newGeoset);

		final GeosetVertex lowerRight = new GeosetVertex(displayWidth / 2, displayWidth / 2, 0, norm);
		newGeoset.add(lowerRight);
		final TVertex lowerRightTVert = new TVertex(1, 1);
		lowerRight.setTangent(defaultTangent);
		lowerRight.addTVertex(lowerRightTVert);
		lowerRight.setGeoset(newGeoset);

		newGeoset.add(new Triangle(upperLeft, upperRight, lowerLeft));
		newGeoset.add(new Triangle(upperRight, lowerRight, lowerLeft));
		blankTextureModel.add(newGeoset);
		blankTextureModel.add(new Animation("Stand", 0, 1000));
		final Camera blpCam = new Camera("Camera01", new Vertex(displayWidth * 2, 0, groundOffset + displayHeight / 2),
				new Vertex(0, 0, groundOffset + displayHeight / 2), 0.75f, 1000f, 8f);
		blankTextureModel.add(blpCam);
		blankTextureModel.doSavePreps();

		return blankTextureModel;
	}

	public static void main(final String[] args) {
		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(1);

		final Codebase source = MpqCodebase.get();

		final DataTable terrainData = new DataTable();

		try {
			terrainData.readSLK(source.getResourceAsStream("TerrainArt\\Terrain.slk"));
		} catch (final IOException e) {
			ExceptionPopup.display(e);
		}

		final ThreadLocal<EditableModel> genericModel = new ThreadLocal<>();
		final String outputDump = "C:\\Temp\\HiveForged\\ArchiveT\\";
		final Set<String> keySet = terrainData.keySet();
		final int size = keySet.size();
		int i = 0;
		System.out.println("Going to attempt to port " + size + " items");
		final int targetLevelOfDetail = 0;
		Material.FLIP_ORM_ALPHA = true;
		Material.LIGHTEN_BAKED_DARK_AREAS = false;
		for (final String key : keySet) {
			final Element terrain = terrainData.get(key);
			final String dir = terrain.getField("dir");
			final String file = terrain.getField("file");

			final int fi = i;
			newFixedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(fi + "/" + size + ": " + terrain.getName());
					try {
						final String baseTerrainPath = dir + "\\" + file;
						final String terrainModelPath = baseTerrainPath + ".mdx";

						final EditableModel genericModelForThread = genericModel();// genericModel.get();
//						if (genericModelForThread == null) {
//							genericModelForThread = genericModel();
//							genericModel.set(genericModelForThread);
//						}

						final File outputFile = new File(outputDump + terrainModelPath);
						final File parentFileOfOutput = outputFile.getParentFile();
						parentFileOfOutput.mkdirs();

						final Material materialZero = genericModelForThread.getMaterial(0);
						final Layer layerZero = materialZero.getLayers().get(0);
						materialZero.getLayers().clear();
						materialZero.getLayers().add(layerZero);
						layerZero.getFlags().clear();
						layerZero.setLayerShader(LayerShader.HD);
						layerZero.getShaderTextures().put(ShaderTextureTypeHD.Diffuse,
								new Bitmap(baseTerrainPath + "_diffuse.dds"));
//						layerZero.getShaderTextures().put(ShaderTextureTypeHD.Diffuse,
//								new Bitmap("Textures\\White.dds"));
						layerZero.getShaderTextures().put(ShaderTextureTypeHD.Normal,
								new Bitmap(baseTerrainPath + "_normal.dds"));
						layerZero.getShaderTextures().put(ShaderTextureTypeHD.ORM,
								new Bitmap(baseTerrainPath + "_orm.dds"));
						layerZero.getShaderTextures().put(ShaderTextureTypeHD.Emissive,
								new Bitmap("Textures\\black32.dds"));
						layerZero.getShaderTextures().put(ShaderTextureTypeHD.TeamColor,
								new Bitmap("ReplaceableTextures\\TeamColor\\TeamColor00.dds"));
						layerZero.getShaderTextures().put(ShaderTextureTypeHD.Reflections,
								new Bitmap("ReplaceableTextures\\EnvironmentMap.dds"));

						genericModelForThread.setFormatVersion(1100);
//						genericModelForThread.printTo(new File(outputDump + baseTerrainPath + "_before.mdx"));

						String relativePath = parentFileOfOutput.getAbsolutePath()
								.substring(new File(outputDump).getAbsolutePath().length());
						if (relativePath.startsWith("\\") || relativePath.startsWith("/")) {
							relativePath = relativePath.substring(1);
						}
						EditableModel.convertToV800BakingTextures(targetLevelOfDetail, genericModelForThread,
								new File(outputDump), relativePath);
						genericModelForThread.printTo(outputFile);
						new File(outputDump + baseTerrainPath + "_diffuse.dds_baked.tga")
								.renameTo(new File(outputDump + baseTerrainPath + ".tga"));

					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			});
			i++;
		}

	}
}
