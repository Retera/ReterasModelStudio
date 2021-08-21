package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public class ScriptActions {

//    static void exportAnimatedFramePNG(MainPanel mainPanel) {
//        BufferedImage fBufferedImage = mainPanel.currentModelPanel().getAnimationViewer().getBufferedImage();
//
//        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
//            EditableModel current = mainPanel.currentMDL();
//            if ((current != null) && !current.isTemp() && (current.getFile() != null)) {
//                mainPanel.fc.setCurrentDirectory(current.getFile().getParentFile());
//            } else if (mainPanel.profile.getPath() != null) {
//                mainPanel.fc.setCurrentDirectory(new File(mainPanel.profile.getPath()));
//            }
//        }
//        if (mainPanel.exportTextureDialog.getCurrentDirectory() == null) {
//            mainPanel.exportTextureDialog.setSelectedFile(new File(mainPanel.exportTextureDialog.getCurrentDirectory() + File.separator));
//        }
//
//        int x = mainPanel.exportTextureDialog.showSaveDialog(mainPanel);
//        if (x == JFileChooser.APPROVE_OPTION) {
//            File file = mainPanel.exportTextureDialog.getSelectedFile();
//            if (file != null) {
//                try {
//                    if (file.getName().lastIndexOf('.') >= 0) {
//                        BufferedImage bufferedImage = fBufferedImage;
//                        String fileExtension = file.getName().substring(file.getName().lastIndexOf('.') + 1)
//                                .toUpperCase();
//                        if (fileExtension.equals("BMP") || fileExtension.equals("JPG")
//                                || fileExtension.equals("JPEG")) {
//                            JOptionPane.showMessageDialog(mainPanel,
//                                    "Warning: Alpha channel was converted to black. Some data will be lost\nif you convert this texture back to Warcraft BLP.");
//                            bufferedImage = BLPHandler.removeAlphaChannel(bufferedImage);
//                        }
//                        if (fileExtension.equals("BLP")) {
//                            fileExtension = "blp";
//                        }
//                        boolean write = ImageIO.write(bufferedImage, fileExtension, file);
//                        if (!write) {
//                            JOptionPane.showMessageDialog(mainPanel, "File type unknown or unavailable");
//                        }
//                    } else {
//                        JOptionPane.showMessageDialog(mainPanel, "No file type was specified");
//                    }
//                } catch (final Exception e1) {
//                    ExceptionPopup.display(e1);
//                    e1.printStackTrace();
//                }
//            } else {
//                JOptionPane.showMessageDialog(mainPanel, "No output file was specified");
//            }
//        }
//    }

	public static void combineAnimations() {
		EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
		List<Animation> anims = model.getAnims();
		Animation[] array = anims.toArray(new Animation[0]);
		Object choice = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Pick the first animation",
				"Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation = (Animation) choice;

		Object choice2 = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"Pick the second animation",
				"Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation2 = (Animation) choice2;

		String nameChoice = JOptionPane.showInputDialog(ProgramGlobals.getMainPanel(),
				"What should the combined animation be called?");
		if (nameChoice != null) {
			int anim1Length = animation.getEnd() - animation.getStart();
			int anim2Length = animation2.getEnd() - animation2.getStart();
			int totalLength = anim1Length + anim2Length;

			int animTrackEnd = model.animTrackEnd();
			int start = animTrackEnd + 1000;
			animation.copyToInterval(start, start + anim1Length, model.getAllAnimFlags(), model.getEvents());
			animation2.copyToInterval(start + anim1Length, start + totalLength, model.getAllAnimFlags(), model.getEvents());

			Animation newAnimation = new Animation(nameChoice, start, start + totalLength);
			model.add(newAnimation);
			newAnimation.setNonLooping(true);
			newAnimation.setExtents(animation.getExtents().deepCopy());
			JOptionPane.showMessageDialog(ProgramGlobals.getMainPanel(),
					"DONE! Made a combined animation called " + newAnimation.getName(), "Success",
					JOptionPane.PLAIN_MESSAGE);
		}
	}


	public static String incName(String name) {
		String output = name;

//        int depth = 1;
//        boolean continueLoop = name != null && output.length()>0;
//        while (continueLoop) {
//            char c = '0';
//            try {
//                c = output.charAt(output.length() - depth);
//            } catch (final IndexOutOfBoundsException e) {
//                // c remains '0', name is not changed (this should only happen if name is "9" or "99...9")
//                continueLoop = false;
//            }
//            for (char n = '0'; (n < '9') && continueLoop; n++) {
//                if (c == n) {
//                    char x = c;
//                    x++;
//                    output = output.substring(0, output.length() - depth) + x
//                            + output.substring((output.length() - depth) + 1);
//                    continueLoop = false;
//                }
//            }
//            if (c == '9') {
//                output = output.substring(0, output.length() - depth) + 0
//                        + output.substring((output.length() - depth) + 1);
//            } else if (continueLoop) {
//                output = output.substring(0, (output.length() - depth) + 1) + 1
//                        + output.substring((output.length() - depth) + 1);
//                continueLoop = false;
//            }
//            depth++;
//        }
//        if (output == null) {
//            output = "name error";
//        } else if (output.equals(name)) {
//            output = output + "_edit";
//        }

		if (output != null) {
			for (int offsetFromEnd = 1; offsetFromEnd <= output.length(); offsetFromEnd++) {
				char charAt = output.charAt(output.length() - offsetFromEnd);
				if ('0' <= charAt && charAt <= '8') {
					int numberLocation = output.length() - offsetFromEnd;
					output = output.substring(0, numberLocation) + (charAt + 1) + output.substring((numberLocation) + 1);
					break;
				} else if (charAt == '9') {
					int numberLocation = output.length() - offsetFromEnd;
					output = output.substring(0, numberLocation) + "0" + output.substring(numberLocation + 1);
					if (numberLocation == 0) {
						output = 1 + output; // if name == "999...9" -> output = "1000...0" instead of "000...0"
					}
				} else { // charAt is not a digit
					int numberLocation = output.length() - offsetFromEnd;
					output = output.substring(0, numberLocation + 1) + "1" + output.substring(numberLocation + 1);
					break;
				}
			}
		}

		if (output == null) {
			output = "name error";
		}
		if (output.equals(name)) {
			output = output + "_edit";
		}

		return output;
	}

	public static void openImportPanelWithEmpty() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null && modelPanel.getModel() != null) {
			EditableModel model = modelPanel.getModel();
			EditableModel newModel = new EditableModel();
			newModel.copyHeaders(model);
			if (newModel.getFileRef() == null) {
				newModel.setFileRef(
						new File(System.getProperty("java.io.tmpdir") + "MatrixEaterExtract/matrixeater_anonymousMDL",
								"" + (int) (Math.random() * Integer.MAX_VALUE) + ".mdl"));
			}
			while (newModel.getFile().exists()) {
				newModel.setFileRef(new File(model.getFile().getParent() + "/" + incName(newModel.getName()) + ".mdl"));
			}
			ImportPanel importPanel = new ImportPanel(newModel, TempStuffFromEditableModel.deepClone(model, "CurrentModel"));

			final Thread watcher = new Thread(() -> {
				while (importPanel.getParentFrame().isVisible()
						&& (!importPanel.importStarted()
						|| importPanel.importEnded())) {
					trySleep();
				}
				if (importPanel.importStarted()) {
					while (!importPanel.importEnded()) {
						trySleep();
					}

					if (importPanel.importSuccessful()) {
						try {
							MdxUtils.saveMdx(newModel, newModel.getFile());
						} catch (final IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						ModelLoader.loadFile(newModel.getFile());
					}
				}
			});
			watcher.start();
		}
		if (ProgramGlobals.getCurrentModelPanel() != null) {
//			ProgramGlobals.getCurrentModelPanel().repaintModelTrees();
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		}
	}

	private static void trySleep() {
		try {
			Thread.sleep(1);
		} catch (final Exception e) {
			ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
		}
	}

	public static void jokeButtonClickResponse() {
		StringBuilder sb = new StringBuilder();
		for (File file : new File(
				"C:\\Users\\micro\\OneDrive\\Documents\\Warcraft III\\CustomMapData\\LuaFpsMap\\Maps\\MultiplayerFun004")
				.listFiles()) {
			if (!file.getName().toLowerCase().endsWith("_init.txt")) {
				sb.setLength(0);
				try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
					String line;
					while ((line = reader.readLine()) != null) {
						if (line.contains("BlzSetAbilityActivatedIcon")) {
							int startIndex = line.indexOf('"') + 1;
							int endIndex = line.lastIndexOf('"');
							String dataString = line.substring(startIndex, endIndex);
							sb.append(dataString);
						}
					}
				} catch (final IOException e1) {
					e1.printStackTrace();
				}
				String dataString = sb.toString();
				EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
				for (int i = 0; (i + 23) < dataString.length(); i += 24) {
					Geoset geo = new Geoset();
					model.addGeoset(geo);
					geo.setParentModel(model);
					geo.setMaterial(new Material(new Layer("Blend", new Bitmap("textures\\white.blp"))));
					String data = dataString.substring(i, i + 24);
					int x = Integer.parseInt(data.substring(0, 3));
					int y = Integer.parseInt(data.substring(3, 6));
					int z = Integer.parseInt(data.substring(6, 9));
					int sX = Integer.parseInt(data.substring(9, 10));
					int sY = Integer.parseInt(data.substring(10, 11));
					int sZ = Integer.parseInt(data.substring(11, 12));
					int red = Integer.parseInt(data.substring(12, 15));
					int green = Integer.parseInt(data.substring(15, 18));
					int blue = Integer.parseInt(data.substring(18, 21));
					int alpha = Integer.parseInt(data.substring(21, 24));
					GeosetAnim forceGetGeosetAnim = geo.forceGetGeosetAnim();
					forceGetGeosetAnim.setStaticColor(new Vec3(blue / 255.0, green / 255.0, red / 255.0));
					forceGetGeosetAnim.setStaticAlpha(alpha / 255.0);
					System.out.println(x + "," + y + "," + z);

					ModelUtils.Mesh mesh = ModelUtils.createBox(new Vec3(x, y, z).scale(10),
							new Vec3(x + sX, y + sY, z + sZ).scale(10), 1, 1, 1, geo);
					geo.addVerticies(mesh.getVertices());
					geo.addTriangles(mesh.getTriangles());
				}
			}

		}
		ModelStructureChangeListener.changeListener.geosetsUpdated();
	}
}
