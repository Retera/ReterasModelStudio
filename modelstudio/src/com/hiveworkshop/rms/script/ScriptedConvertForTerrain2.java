package com.hiveworkshop.rms.script;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

import de.wc3data.image.TgaFile;

public class ScriptedConvertForTerrain2 {
	static int tileCount = 0;
	static Set<Character> tilesets = new HashSet<Character>();
	static {
		tilesets.add('A');
		tilesets.add('B');
		tilesets.add('K');
		tilesets.add('Y');
		tilesets.add('X');
		tilesets.add('J');
		tilesets.add('D');
		tilesets.add('C');
		tilesets.add('I');
		tilesets.add('F');
		tilesets.add('L');
		tilesets.add('W');
		tilesets.add('N');
		tilesets.add('O');
		tilesets.add('Z');
		tilesets.add('G');
		tilesets.add('V');
		tilesets.add('Q');
		// custom
		tilesets.add('E');
		
	}
	static File sourceDir = new File("C:\\Warsmash\\Scratch\\ThirdPerson\\Tileset\\Elwynn");
	static File destDir = new File("C:\\Warsmash\\Scratch\\ThirdPerson_Tileset\\Tileset\\Elwynn");

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
			if (lowerCaseName.endsWith(".blp") && !lowerCaseName.endsWith("_s.blp")) {
				try {
					BufferedImage bufferedImg = ImageIO.read(file);
					BufferedImage fixed = new BufferedImage(bufferedImg.getWidth() * 4, bufferedImg.getHeight() * 4,
							BufferedImage.TYPE_INT_ARGB);
					Graphics2D graphics = fixed.createGraphics();
					for (int i = 0; i < 4; i++) {
						for (int j = 0; j < 4; j++) {
							graphics.drawImage(bufferedImg, i * bufferedImg.getWidth(), j * bufferedImg.getHeight(),
									null);
						}
					}
					graphics.dispose();
					for (int k = 1; k < 16; k++) {
						boolean bottomRight = (k & 0x1) != 0;
						boolean bottomLeft = (k & 0x2) != 0;
						boolean topRight = (k & 0x4) != 0;
						boolean topleft = (k & 0x8) != 0;
						int cellX = (k) % 4;
						int cellY = (k) / 4;
						for (int pixelX = 0; pixelX < bufferedImg.getWidth(); pixelX++) {
							for (int pixelY = 0; pixelY < bufferedImg.getHeight(); pixelY++) {
								float fX = pixelX / (float) bufferedImg.getWidth();
								float fY = pixelY / (float) bufferedImg.getWidth();
								float alpha = 1;
								if (bottomRight && !bottomLeft && !topRight && !topleft) {
									alpha = Math.max(0, 1f - (float) new Point2D.Float(fX, fY).distance(1, 1));
								} else if (!bottomRight && bottomLeft && !topRight && !topleft) {
									alpha = Math.max(0, 1f - (float) new Point2D.Float(fX, fY).distance(0, 1));
								} else if (bottomRight && bottomLeft && !topRight && !topleft) {
									alpha = fY;
								} else if (!bottomRight && !bottomLeft && topRight && !topleft) {
									alpha = Math.max(0, 1f - (float) new Point2D.Float(fX, fY).distance(1, 0));
								} else if (bottomRight && !bottomLeft && topRight && !topleft) {
									alpha = fX;
								} else if (!bottomRight && bottomLeft && topRight && !topleft) {
									alpha = Math.min(1,
											Math.max(0, 1f - (float) new Point2D.Float(fX, fY).distance(1, 0)) + Math.max(0,1f
													- (float) new Point2D.Float(fX, fY).distance(0, 1)));
								} else if (bottomRight && bottomLeft && topRight && !topleft) {
									alpha = Math.min(1,
											Math.max(0,(float) new Point2D.Float(fX, fY).distance(0, 0)));
								} else if (!bottomRight && !bottomLeft && !topRight && topleft) {
									alpha = Math.max(0, 1f - (float) new Point2D.Float(fX, fY).distance(0, 0));
								} else if (bottomRight && !bottomLeft && !topRight && topleft) {
									alpha = Math.min(1,
											Math.max(0, 1f - (float) new Point2D.Float(fX, fY).distance(1, 1)) + Math.max(0,1f
													- (float) new Point2D.Float(fX, fY).distance(0, 0)));
								} else if (!bottomRight && bottomLeft && !topRight && topleft) {
									alpha = 1 - fX;
								} else if (bottomRight && bottomLeft && !topRight && topleft) {
									alpha = Math.min(1,
											Math.max(0,(float) new Point2D.Float(fX, fY).distance(1, 0)));
								} else if (!bottomRight && !bottomLeft && topRight && topleft) {
									alpha = 1 - fY;
								} else if (bottomRight && !bottomLeft && topRight && topleft) {
									alpha = Math.min(1,
											Math.max(0,(float) new Point2D.Float(fX, fY).distance(0, 1)));
								} else if (!bottomRight && bottomLeft && topRight && topleft) {
									alpha = Math.min(1,
											Math.max(0,(float) new Point2D.Float(fX, fY).distance(1, 1)));
								}
								int alphaB = (int)(alpha*255) << 24;
								int x = pixelX + cellX * bufferedImg.getWidth();
								int y = pixelY + cellY * bufferedImg.getHeight();
								fixed.setRGB(x, y, (fixed.getRGB(x, y) & 0xFFFFFF) | alphaB);
							}
						}
					}

					File destinationFor = destinationFor(file);
					destinationFor.getParentFile().mkdirs();
					ImageIO.write(fixed, "blp", destinationFor);
					TgaFile.writeTGA(fixed, new File(destinationFor.toString().replace(".blp", ".tga")));
					int tileId = tileCount++;
					String tileSTr = String.format("%3s", Integer.toString(tileId)).replace(' ', '0');
					String shorterName = file.getName();
					shorterName= shorterName.substring(0, shorterName.indexOf('.'));
					System.out.println("A"+tileSTr+",-1,Tileset\\Lorladir,"+shorterName+","+shorterName+","+shorterName+",1,0,1,1,0,Fdrt,1,0");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
