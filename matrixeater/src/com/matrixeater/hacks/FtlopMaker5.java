package com.matrixeater.hacks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.image.TgaFile;
import de.wc3data.stream.BlizzardDataInputStream;

public class FtlopMaker5 {

	public static void main(final String[] args) {
		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(12);
		final SetView<String> mergedListfile = MpqCodebase.get().getMergedListfile();
		final List<String> betterList = new ArrayList<>();
		for (final String s : mergedListfile) {
			if (!s.startsWith("_hd.w3mod") && !s.startsWith("_locales")) {
				if (s.toLowerCase().endsWith(".mdx") || s.toLowerCase().endsWith(".dds")) {
					if (s.startsWith("war3.w3mod\\_hd.w3mod\\")) {
						betterList.add(s);
					}
				}
			}
		}
		final String outputDump = "C:\\Temp\\HiveForged\\Archive\\";
		final int targetLevelOfDetail = 0;
		final int size = betterList.size();
		System.out.println("Going to attempt to port " + size + " items");
		for (int i = 0; i < size; i++) {
			if (i % 100 == 0) {
				System.out.println("Processed 100 items... now at " + i);
			}
			final String item = betterList.get(i);
			final int fi = i;
			newFixedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(fi + "/" + size + ": " + item);
					if (item.toLowerCase().endsWith(".dds")) {
						// it's a texture
						try {
							final BufferedImage gameTex = BLPHandler.get().getGameTex(item);
							final File outputFile = new File(
									outputDump + item.substring(0, item.lastIndexOf('.')) + ".tga");
							outputFile.getParentFile().mkdirs();
							TgaFile.writeTGA(gameTex, outputFile);
//							ImageIO.write(gameTex, "blp", outputFile);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					} else if (item.toLowerCase().endsWith(".mdx")) {
						// it's a model
						try {
							final File outputFile = new File(outputDump + item);
							final File parentFileOfOutput = outputFile.getParentFile();
							parentFileOfOutput.mkdirs();
							final EditableModel model = new EditableModel(MdxUtils.loadModel(
									new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream(item))));
//							EditableModel.convertToV800(targetLevelOfDetail, model);
							String relativePath = parentFileOfOutput.getAbsolutePath()
									.substring(new File(outputDump).getAbsolutePath().length());
							if (relativePath.startsWith("\\") || relativePath.startsWith("/")) {
								relativePath = relativePath.substring(1);
							}
							EditableModel.convertToV800BakingTextures(targetLevelOfDetail, model, new File(outputDump),
									relativePath);
							model.printTo(outputFile);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
	}
}
