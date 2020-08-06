package com.matrixeater.hacks;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.hiveworkshop.wc3.mdx.LayerChunk;
import com.hiveworkshop.wc3.mdx.MaterialChunk;
import com.hiveworkshop.wc3.mdx.MdxModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

import de.wc3data.stream.BlizzardDataInputStream;

public class FixMyProgramSir {
	public static void main(final String[] args) {
		final File mapFolder = new File("D:\\NEEDS_ORGANIZING\\Reforged Beta 13769");
		traverse(mapFolder);
	}

	public static void traverse(final File file) {
		if (file.isDirectory()) {
			for (final File subFile : file.listFiles()) {
				traverse(subFile);
			}
		} else if (file.getName().toLowerCase().endsWith(".mdx")) {
			try (BlizzardDataInputStream is = new BlizzardDataInputStream(new FileInputStream(file))) {
				final MdxModel loadModel = MdxUtils.loadModel(is);
				int materialId = 0;
				if (loadModel.materialChunk != null) {
					for (final MaterialChunk.Material mat : loadModel.materialChunk.material) {
						int layerId = 0;
						for (final LayerChunk.Layer lay : mat.layerChunk.layer) {
//							if (lay.mdx1000UnknownData != null) {
//								if ((lay.mdx1000UnknownData[0] != 1.0f) || (lay.mdx1000UnknownData[1] != 1.0f)
//										|| (lay.mdx1000UnknownData[2] != 1.0f) || (lay.mdx1000UnknownData[3] != 0.0f)
//										|| (lay.mdx1000UnknownData[4] != 0.0f)) {
//									System.out.println("Discovered interesting data: ");
//									System.out.println(" - Path: " + file);
//									System.out.println(" - MaterialID: " + materialId);
//									System.out.println(" - LayerID: " + layerId);
//									System.exit(0);
//								}
//							}
							layerId++;
						}
						materialId++;
					}
				}
			} catch (final FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
