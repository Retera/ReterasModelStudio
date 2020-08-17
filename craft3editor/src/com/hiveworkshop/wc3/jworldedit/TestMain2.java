package com.hiveworkshop.wc3.jworldedit;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.imageio.ImageIO;

import com.hiveworkshop.wc3.gui.BLPHandler;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdx.MdxUtils;

public class TestMain2 {
	static File root = new File("F:\\NEEDS_ORGANIZING\\WarCraft_III_Beta\\WarCraft_III_Beta\\War3beta");
	static File dest = new File("C:\\Program Files (x86)\\Warcraft III\\War3mod.mpq");

	public static void main(final String[] args) {
		traverse(root);
	}

	public static void traverse(final File file) {
		if (file.isDirectory()) {
			for (final File sub : file.listFiles()) {
				traverse(sub);
			}
		} else {
//			if (!file.getPath().contains("ToolTipManaIcon")) {
//				return;
//			}
			final File destFile = new File(
					dest.getAbsolutePath() + "\\" + file.getAbsolutePath().substring(root.getAbsolutePath().length()));
			if (file.getPath().toLowerCase().endsWith(".wav") || file.getPath().toLowerCase().endsWith(".mp3")) {
				if (true) {
					return;
				}
				try {
					destFile.getParentFile().mkdirs();
					Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
			if (file.getPath().toLowerCase().endsWith(".mdx")) {
				System.out.println(file.getPath());
				// it's a model
				try {
					final EditableModel model = MdxUtils.loadEditableModel(file);
					destFile.getParentFile().mkdirs();
					MdxUtils.saveEditableModel(model, destFile);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			} else if (file.getPath().toLowerCase().endsWith(".blp")) {
				System.out.println(file.getPath());
				try {
					final BufferedImage image = BLPHandler.readCustom(file);
					destFile.getParentFile().mkdirs();
					ImageIO.write(image, "blp", destFile);
				} catch (final Exception e) {
					try {
						final BufferedImage image = BLPHandler.get().getCustomTex(file.getAbsolutePath());
						destFile.getParentFile().mkdirs();
						ImageIO.write(image, "blp", destFile);
					} catch (final Exception e2) {
						e2.printStackTrace();
					}
					e.printStackTrace();
				}
			}
		}
	}

}
