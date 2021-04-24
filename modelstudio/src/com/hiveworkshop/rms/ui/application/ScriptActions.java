package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.InterpolationType;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.actions.mesh.DeleteGeosetAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.TimeBoundProvider;
import com.hiveworkshop.rms.ui.application.scripts.ChangeAnimationLengthFrame;
import com.hiveworkshop.rms.ui.application.viewer.AnimatedRenderEnvironment;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.importpanel.ImportPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.Vec4;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ScriptActions {
	static void mergeGeosetActionRes(MainPanel mainPanel) throws IOException {
		FileDialog fileDialog = new FileDialog(mainPanel);
//
		EditableModel current = mainPanel.currentMDL();
		EditableModel geoSource = fileDialog.chooseModelFile(FileDialog.OPEN_WC_MODEL);

		if (geoSource != null) {
			boolean going = true;
			Geoset host = null;
			while (going) {
				String s = JOptionPane.showInputDialog(mainPanel,
						"Geoset into which to Import: (1 to " + current.getGeosetsSize() + ")");
				try {
					int x = Integer.parseInt(s);
					if ((x >= 1) && (x <= current.getGeosetsSize())) {
						host = current.getGeoset(x - 1);
						going = false;
					}
				} catch (final NumberFormatException ignored) {

				}
			}
			Geoset newGeoset = null;
			going = true;
			while (going) {
				String s = JOptionPane.showInputDialog(mainPanel,
						"Geoset to Import: (1 to " + geoSource.getGeosetsSize() + ")");
				try {
					int x = Integer.parseInt(s);
					if ((x >= 1) && x <= geoSource.getGeosetsSize()) {
						newGeoset = geoSource.getGeoset(x - 1);
						going = false;
					}
				} catch (final NumberFormatException ignored) {

				}
			}
			newGeoset.updateToObjects(current);
			System.out.println("putting " + newGeoset.numUVLayers() + " into a nice " + host.numUVLayers());
			for (int i = 0; i < newGeoset.numVerteces(); i++) {
				GeosetVertex ver = newGeoset.getVertex(i);
				host.add(ver);
				ver.setGeoset(host);// geoset = host;
				// for( int z = 0; z < host.n.numUVLayers(); z++ ){
				// host.getUVLayer(z).addTVertex(newGeoset.getVertex(i).getTVertex(z));}
			}
			for (int i = 0; i < newGeoset.numTriangles(); i++) {
				Triangle tri = newGeoset.getTriangle(i);
				host.add(tri);
				tri.setGeoRef(host);
			}
		}
	}

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

	static void exportAnimatedToStaticMesh(MainPanel mainPanel) {
		if (!mainPanel.animationModeState) {
			JOptionPane.showMessageDialog(mainPanel, "You must be in the Animation Editor to use that!",
					"Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		ModelPanel modelContext = mainPanel.currentModelPanel();
		RenderModel editorRenderModel = modelContext.getEditorRenderModel();
		EditableModel model = modelContext.getModel();

		AnimatedRenderEnvironment renderEnv = editorRenderModel.getAnimatedRenderEnvironment();
		TimeBoundProvider currentAnimation = renderEnv.getCurrentAnimation();

		String s = "At" + renderEnv.getAnimationTime();
		System.out.println(currentAnimation);
		if (currentAnimation instanceof Animation) {
			s = ((Animation) currentAnimation).getName() + s;
		}
		EditableModel frozenModel = EditableModel.deepClone(model, model.getHeaderName() + s);
		if (frozenModel.getFileRef() != null) {
			frozenModel.setFileRef(new File(frozenModel.getFileRef().getPath().replaceFirst("(?<=\\w)\\.(?=md[lx])", s + ".")));
		}

		for (int geosetIndex = 0; geosetIndex < frozenModel.getGeosets().size(); geosetIndex++) {
			Geoset geoset = model.getGeoset(geosetIndex);
			Geoset frozenGeoset = frozenModel.getGeoset(geosetIndex);

			for (int vertexIndex = 0; vertexIndex < geoset.getVertices().size(); vertexIndex++) {
				GeosetVertex vertex = geoset.getVertex(vertexIndex);
				GeosetVertex frozenVertex = frozenGeoset.getVertex(vertexIndex);
				Mat4 skinBonesMatrixSumHeap;
				if (vertex.getSkinBones() != null) {
					skinBonesMatrixSumHeap = ModelUtils.processHdBones(editorRenderModel, vertex.getSkinBoneBones(), vertex.getSkinBoneWeights());
				} else {
					skinBonesMatrixSumHeap = ModelUtils.processSdBones(editorRenderModel, vertex.getBones());
				}
				Vec4 vertexSumHeap = Vec4.getTransformed(new Vec4(vertex, 1), skinBonesMatrixSumHeap);
				frozenVertex.set(vertexSumHeap);
				if (vertex.getNormal() != null) {
					Vec4 normalSumHeap = Vec4.getTransformed(new Vec4(vertex.getNormal(), 0), skinBonesMatrixSumHeap);
					normalSumHeap.normalize();
					frozenVertex.getNormal().set(normalSumHeap);
				}
			}
		}
		frozenModel.clearAllIdObjects();
		Bone boneRoot = new Bone("Bone_Root");
		boneRoot.setPivotPoint(new Vec3(0, 0, 0));
		frozenModel.add(boneRoot);

		for (Geoset geoset : frozenModel.getGeosets()) {
			for (GeosetVertex vertex : geoset.getVertices()) {
				if (vertex.getSkinBones() != null) {
					vertex.setSkinBones(new Bone[] {boneRoot, null, null, null}, new short[] {255, 0, 0, 0});
				} else {
					vertex.getBones().clear();
					vertex.getBones().add(boneRoot);
				}
			}
		}

		List<Geoset> geosetsToRemove = new ArrayList<>();
		for (Geoset geoset : frozenModel.getGeosets()) {
			GeosetAnim geosetAnim = geoset.getGeosetAnim();

			if (geosetAnim != null && geosetAnim.getVisibilityFlag() != null) {
				Object visibilityValue = geosetAnim.getVisibilityFlag().interpolateAt(renderEnv);

				if (visibilityValue instanceof Float) {
					double visValue = (Float) visibilityValue;

					if (visValue < 0.01) {
						geosetsToRemove.add(geoset);
						frozenModel.remove(geosetAnim);
					}
				}
			}
		}

		for (Geoset geoset : geosetsToRemove) {
			frozenModel.remove(geoset);
		}

		frozenModel.getAnims().clear();
		frozenModel.add(new Animation("Stand", 333, 1333));
		List<AnimFlag<?>> allAnimFlags = frozenModel.getAllAnimFlags();
		for (AnimFlag flag : allAnimFlags) {
			if (!flag.hasGlobalSeq()) {
				if (flag.size() > 0) {
					Object value = flag.interpolateAt(mainPanel.animatedRenderEnvironment);
					flag.setInterpType(InterpolationType.DONT_INTERP);
					flag.clear();
					flag.addEntry(333, value);
				}
			}
		}

		FileDialog fileDialog = new FileDialog(mainPanel);
		fileDialog.onClickSaveAs(frozenModel, FileDialog.SAVE_MODEL, false);
	}

	static void combineAnimations(MainPanel mainPanel) {
		List<Animation> anims = mainPanel.currentMDL().getAnims();
		Animation[] array = anims.toArray(new Animation[0]);
		Object choice = JOptionPane.showInputDialog(mainPanel, "Pick the first animation",
				"Choose 1st Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation = (Animation) choice;

		Object choice2 = JOptionPane.showInputDialog(mainPanel, "Pick the second animation",
				"Choose 2nd Anim", JOptionPane.PLAIN_MESSAGE, null, array, array[0]);
		Animation animation2 = (Animation) choice2;

		String nameChoice = JOptionPane.showInputDialog(mainPanel,
				"What should the combined animation be called?");
		if (nameChoice != null) {
			int anim1Length = animation.getEnd() - animation.getStart();
			int anim2Length = animation2.getEnd() - animation2.getStart();
			int totalLength = anim1Length + anim2Length;

			EditableModel model = mainPanel.currentMDL();
			int animTrackEnd = model.animTrackEnd();
			int start = animTrackEnd + 1000;
			animation.copyToInterval(start, start + anim1Length, model.getAllAnimFlags(), model.getEvents());
			animation2.copyToInterval(start + anim1Length, start + totalLength, model.getAllAnimFlags(), model.getEvents());

			Animation newAnimation = new Animation(nameChoice, start, start + totalLength);
			model.add(newAnimation);
			newAnimation.setNonLooping(true);
			newAnimation.setExtents(new ExtLog(animation.getExtents()));
			JOptionPane.showMessageDialog(mainPanel,
					"DONE! Made a combined animation called " + newAnimation.getName(), "Success",
					JOptionPane.PLAIN_MESSAGE);
		}
	}

	static void scaleAnimations(MainPanel mainPanel) {
		ChangeAnimationLengthFrame aFrame = new ChangeAnimationLengthFrame(mainPanel.currentModelPanel(), () -> mainPanel.timeSliderPanel.revalidateKeyframeDisplay());
		aFrame.setVisible(true);
	}

	static void nullmodelButtonActionRes(MainPanel mainPanel) {
		nullModelFile(mainPanel);
		MenuBarActions.refreshController(mainPanel.geoControl, mainPanel.geoControlModelData);
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

	public static void nullModelFile(MainPanel mainPanel) {
		EditableModel currentMDL = mainPanel.currentMDL();
		if (currentMDL != null) {
			EditableModel newModel = new EditableModel();
			newModel.copyHeaders(currentMDL);
			if (newModel.getFileRef() == null) {
				newModel.setFileRef(
						new File(System.getProperty("java.io.tmpdir") + "MatrixEaterExtract/matrixeater_anonymousMDL",
								"" + (int) (Math.random() * Integer.MAX_VALUE) + ".mdl"));
			}
			while (newModel.getFile().exists()) {
				newModel.setFileRef(new File(currentMDL.getFile().getParent() + "/" + incName(newModel.getName()) + ".mdl"));
			}
			mainPanel.importPanel = new ImportPanel(newModel, EditableModel.deepClone(currentMDL, "CurrentModel"));

			final Thread watcher = new Thread(() -> {
				while (mainPanel.importPanel.getParentFrame().isVisible()
						&& (!mainPanel.importPanel.importStarted()
						|| mainPanel.importPanel.importEnded())) {
					trySleep();
				}
				// if( !importPanel.getParentFrame().isVisible() &&
				// !importPanel.importEnded() )
				// JOptionPane.showMessageDialog(null,"bad voodoo
				// "+importPanel.importSuccessful());
				// else
				// JOptionPane.showMessageDialog(null,"good voodoo
				// "+importPanel.importSuccessful());
				// if( importPanel.importSuccessful() )
				// {
				// newModel.saveFile();
				// loadFile(newModel.getFile());
				// }

				if (mainPanel.importPanel.importStarted()) {
					while (!mainPanel.importPanel.importEnded()) {
						trySleep();
					}

					if (mainPanel.importPanel.importSuccessful()) {
						try {
							MdxUtils.saveMdx(newModel, newModel.getFile());
						} catch (final IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						MPQBrowserView.loadFile(mainPanel, newModel.getFile());
					}
				}
			});
			watcher.start();
		}
	}

	private static void trySleep() {
		try {
			Thread.sleep(1);
		} catch (final Exception e) {
			ExceptionPopup.display("MatrixEater detected error with Java's wait function", e);
		}
	}

	static void jokeButtonClickResponse(MainPanel mainPanel) {
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
				for (int i = 0; (i + 23) < dataString.length(); i += 24) {
					Geoset geo = new Geoset();
					mainPanel.currentMDL().addGeoset(geo);
					geo.setParentModel(mainPanel.currentMDL());
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
					geo.getVertices().addAll(mesh.getVertices());
					geo.getTriangles().addAll(mesh.getTriangles());
				}
			}

		}
		mainPanel.modelStructureChangeListener.geosetsAdded(new ArrayList<>(mainPanel.currentMDL().getGeosets()));
	}

	/**
	 * Please, for the love of Pete, don't actually do this.
	 */
	public static void convertToV800(int targetLevelOfDetail, EditableModel model) {
		// Things to fix:
		// 1.) format version
		model.setFormatVersion(800);
		// 2.) materials: only diffuse
		for (Bitmap tex : model.getTextures()) {
			String path = tex.getPath();
			if ((path != null) && !path.isEmpty()) {
				int dotIndex = path.lastIndexOf('.');
				if ((dotIndex != -1) && !path.endsWith(".blp")) {
					path = (path.substring(0, dotIndex));
				}
				if (!path.endsWith(".blp")) {
					path += ".blp";
				}
				tex.setPath(path);
			}
		}
		for (Material material : model.getMaterials()) {
			material.makeSD();
		}
		// 3.) geosets:
		// - Convert skin to matrices & vertex groups
		List<Geoset> wrongLOD = new ArrayList<>();
		for (Geoset geo : model.getGeosets()) {
			for (GeosetVertex vertex : geo.getVertices()) {
				vertex.un900Heuristic();
			}
			if (geo.getLevelOfDetail() != targetLevelOfDetail) {
				// wrong lod
				wrongLOD.add(geo);
			}
		}
		// - Probably overwrite normals with tangents, maybe, or maybe not
		// - Eradicate anything that isn't LOD==X
		if (model.getGeosets().size() > wrongLOD.size()) {
			for (Geoset wrongLODGeo : wrongLOD) {
				model.remove(wrongLODGeo);
				GeosetAnim geosetAnim = wrongLODGeo.getGeosetAnim();
				if (geosetAnim != null) {
					model.remove(geosetAnim);
				}
			}
		}
		// 4.) remove popcorn
		// - add hero glow from popcorn if necessary
		List<IdObject> incompatibleObjects = new ArrayList<>();
		for (int idObjIdx = 0; idObjIdx < model.getIdObjectsSize(); idObjIdx++) {
			IdObject idObject = model.getIdObject(idObjIdx);
			if (idObject instanceof ParticleEmitterPopcorn) {
				incompatibleObjects.add(idObject);
				if (((ParticleEmitterPopcorn) idObject).getPath().toLowerCase().contains("hero_glow")) {
					System.out.println("HERO HERO HERO");
					Bone dummyHeroGlowNode = new Bone("hero_reforged");
					// this model needs hero glow
					ModelUtils.Mesh heroGlowPlane = ModelUtils.createPlane((byte) 0, (byte) 1, new Vec3(0, 0, 1), 0, new Vec2(-64, -64), new Vec2(64, 64), 1, 1);

					Geoset heroGlow = new Geoset();
					heroGlow.getVertices().addAll(heroGlowPlane.getVertices());
					for (GeosetVertex gv : heroGlow.getVertices()) {
						gv.setGeoset(heroGlow);
						gv.getBones().clear();
						gv.getBones().add(dummyHeroGlowNode);
					}
					heroGlow.getTriangles().addAll(heroGlowPlane.getTriangles());
					heroGlow.setUnselectable(true);

					Bitmap heroGlowBitmap = new Bitmap("");
					heroGlowBitmap.setReplaceableId(2);
					Layer layer = new Layer("Additive", heroGlowBitmap);
					layer.setUnshaded(true);
					layer.setUnfogged(true);
					heroGlow.setMaterial(new Material(layer));

					model.add(dummyHeroGlowNode);
					model.add(heroGlow);

				}
			}
		}
		for (IdObject incompat : incompatibleObjects) {
			model.remove(incompat);
		}
		// 5.) remove other unsupported stuff
		for (IdObject obj : model.getIdObjects()) {
			obj.setBindPose(null);
		}
		for (Camera camera : model.getCameras()) {
			camera.setBindPose(null);
		}
		// 6.) fix dump bug with paths:
		for (Bitmap tex : model.getTextures()) {
			String path = tex.getPath();
			if (path != null) {
				tex.setPath(path.replace('/', '\\'));
			}
		}
		for (ParticleEmitter emitter : model.getParticleEmitters()) {
			String path = emitter.getPath();
			if (path != null) {
				emitter.setPath(path.replace('/', '\\'));
			}
		}
		for (Attachment emitter : model.getAttachments()) {
			String path = emitter.getPath();
			if (path != null) {
				emitter.setPath(path.replace('/', '\\'));
			}
		}

		model.setBindPoseChunk(null);
		model.getFaceEffects().clear();
	}

	public static void makeItHD2(EditableModel model) {
		for (Geoset geo : model.getGeosets()) {
			List<GeosetVertex> vertices = geo.getVertices();
			for (GeosetVertex gv : vertices) {
				Vec3 normal = gv.getNormal();
				if (normal != null) {
					gv.initV900();
					gv.setTangent(normal, 1);
				}
				int bones = Math.min(4, gv.getBoneAttachments().size());
				short weight = (short) (255 / bones);
				for (int i = 0; i < bones; i++) {
					if (i == 0) {
						gv.setSkinBone(gv.getBoneAttachments().get(i), (short) (weight + (255 % bones)), i);
					} else {
						gv.setSkinBone(gv.getBoneAttachments().get(i), weight, i);

					}
				}
			}
		}
		for (Material m : model.getMaterials()) {
			m.makeHD();
		}
	}


	static void removeLoDs(MainPanel mainPanel) {
		ModelPanel modelPanel = mainPanel.currentModelPanel();
		if (modelPanel != null) {
			JPanel panel = new JPanel(new MigLayout());
			panel.add(new JLabel("LoD to remove"));
			JSpinner spinner = new JSpinner(new SpinnerNumberModel(2, -2, 10, 1));
			panel.add(spinner, "wrap");
			int option = JOptionPane.showConfirmDialog(mainPanel, panel, "Remove LoDs", JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.OK_OPTION) {
				removeLoDGeoset(modelPanel, (int) spinner.getValue(), mainPanel.getModelStructureChangeListener());

//				modelPanel.getUndoManager().pushAction(modelPanel.getModelEditorManager().getModelEditor().recalcNormals(lastNormalMaxAngle, useTris));
			}
		}
		mainPanel.repaint();
	}

	public static void removeLoDGeoset(ModelPanel modelPanel, int lodToRemove, ModelStructureChangeListener changeListener) {
		EditableModel model = modelPanel.getModel();
		List<Geoset> lodGeosToRemove = new ArrayList<>();
		for (Geoset geo : model.getGeosets()) {
			if (geo.getLevelOfDetail() == lodToRemove) {
				lodGeosToRemove.add(geo);
			}
		}
		if (model.getGeosets().size() > lodGeosToRemove.size()) {
			DeleteGeosetAction deleteGeosetAction = new DeleteGeosetAction(lodGeosToRemove, changeListener);
			CompoundAction deletActions = new CompoundAction("Delete LoD=" + lodToRemove + " geosets", deleteGeosetAction);
			modelPanel.getUndoManager().pushAction(deletActions);
			deletActions.redo();
		}
	}

	public static void makeItHD(EditableModel model) {
		for (Geoset geo : model.getGeosets()) {
			geo.makeHd();
		}
		for (Material m : model.getMaterials()) {
			m.makeHD();
		}
	}
}
