package com.hiveworkshop.rms.script;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.parsers.mdlx.MdlxModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;

public class ScriptedConvert {
	static File sourceDir = new File("C:\\Warsmash\\Scratch\\ThirdPerson");
	static File destDir = new File("C:\\Warsmash\\Scratch\\ThirdPerson_800");

	public static void main(String[] args) {
		processFile(sourceDir);
	}

	private static final File destinationFor(File sourceFile) {
		return new File(destDir.getPath() + sourceFile.getPath().substring(sourceDir.getPath().length()));
	}

	private static final void processFile(File file) {
		if (file.isDirectory()) {
			for (File subFile : file.listFiles()) {
				processFile(subFile);
			}
		} else {
			String lowerCaseName = file.getName().toLowerCase();
			if (lowerCaseName.endsWith(".mdx")) {
				File destinationFor = destinationFor(file);
				destinationFor.getParentFile().mkdirs();
				try (FileInputStream stream = new FileInputStream(file);
						FileOutputStream outputStream = new FileOutputStream(destinationFor)) {
					EditableModel model = MdxUtils.loadEditable(stream);
					model.setFormatVersion(1000);
					EditableModel.convertToV800(-1, model);
					System.out.println(destinationFor);
					MdxUtils.saveMdx(model, outputStream);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if(lowerCaseName.endsWith(".blp")) {
				try {
					BufferedImage bufferedImg = ImageIO.read(file);
					BufferedImage fixed = new BufferedImage(bufferedImg.getWidth(), bufferedImg.getHeight(), BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = fixed.createGraphics();
					graphics.drawImage(bufferedImg, 0, 0, null);
					graphics.dispose();
					File destinationFor = destinationFor(file);
					destinationFor.getParentFile().mkdirs();
					ImageIO.write(fixed, "blp", destinationFor);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
