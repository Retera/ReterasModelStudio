package com.matrixeater.hacks;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import com.etheller.collections.SetView;
import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;
import com.hiveworkshop.wc3.mpq.MpqCodebase;

import de.wc3data.stream.BlizzardDataInputStream;

public class FtlopMaker4 {

	public static void main(final String[] args) {
		final ExecutorService newFixedThreadPool = Executors.newFixedThreadPool(12);
		final SetView<String> mergedListfile = MpqCodebase.get().getMergedListfile();
		final List<String> betterList = new ArrayList<>();
		for (final String s : mergedListfile) {
			if (!s.startsWith("_hd.w3mod") && !s.startsWith("_locales")) {
				if (s.toLowerCase().endsWith(".mdx")) {// || s.toLowerCase().endsWith(".dds")) {
					betterList.add(s);
				}
			}
		}
		final String outputDump = "E:\\Games\\FtlopModLocalFiles\\";
		final int targetLevelOfDetail = 1;
		System.out.println("Going to attempt to port " + betterList.size() + " items");
		for (int i = 0; i < betterList.size(); i++) {
			if ((i % 100) == 0) {
				System.out.println("Processed 100 items... now at " + i);
			}
			final String item = betterList.get(i);
			newFixedThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					System.out.println(item);
					if (item.toLowerCase().endsWith(".dds")) {
						// it's a texture
						try {
							final BufferedImage gameTex = BLPHandler.get().getGameTex(item);
							final File outputFile = new File(
									outputDump + item.substring(0, item.lastIndexOf('.')) + ".blp");
							outputFile.getParentFile().mkdirs();
							ImageIO.write(gameTex, "blp", outputFile);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					} else if (item.toLowerCase().endsWith(".mdx")) {
						// it's a model
						try {
							final File outputFile = new File(outputDump + item);
							outputFile.getParentFile().mkdirs();
							final EditableModel model = new EditableModel(MdxUtils.loadModel(MpqCodebase.get().getResourceAsStream(item)));
							EditableModel.convertToV800(targetLevelOfDetail, model);
							MdxUtils.saveEditableModel(model, outputFile);
						} catch (final Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
		}
		System.out.println("Done assigning jobs");
	}
}
