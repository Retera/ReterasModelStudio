package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.animflag.AnimFlag;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.util.TransferActionListener;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;
import de.wc3data.stream.BlizzardDataInputStream;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.*;

public class MenuBarActions {
	static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

	private static void dataSourcesChanged(WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier directoryChangeNotifier, List<ModelPanel> modelPanels) {
		for (ModelPanel modelPanel : modelPanels) {
			PerspDisplayPanel pdp = modelPanel.getPerspArea();
			pdp.reloadAllTextures();
			modelPanel.getAnimationViewer().reloadAllTextures();
		}
		directoryChangeNotifier.dataSourcesChanged();
	}

	public static MutableObjectData getDoodadData() {
		War3ObjectDataChangeset editorData = new War3ObjectDataChangeset('d');
		try {
			CompoundDataSource gameDataFileSystem = GameDataFileSystem.getDefault();
			if (gameDataFileSystem.has("war3map.w3d")) {
				editorData.load(new BlizzardDataInputStream(gameDataFileSystem.getResourceAsStream("war3map.w3d")),
						gameDataFileSystem.has("war3map.wts")
								? new WTSFile(gameDataFileSystem.getResourceAsStream("war3map.wts")) : null, true);
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return new MutableObjectData(MutableObjectData.WorldEditorDataType.DOODADS, StandardObjectData.getStandardDoodads(),
				StandardObjectData.getStandardDoodadMeta(), editorData);
	}

	public static void openUnitViewer(MainPanel mainPanel) {
		UnitEditorTree unitEditorTree = MainLayoutCreator.createUnitEditorTree(mainPanel);
		mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
				new View("Unit Browser",
						new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
						new JScrollPane(unitEditorTree))));
	}

	public static void openHiveViewer(MainPanel mainPanel) {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.BEFORE_FIRST_LINE, new JLabel(POWERED_BY_HIVE));

		JList<String> view = new JList<>(new String[] {"Bongo Bongo (Phantom Shadow Beast)", "Other Model", "Other Model"});
		view.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list, final Object value,
			                                              final int index, final boolean isSelected, final boolean cellHasFocus) {
				Component listCellRendererComponent = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				ImageIcon icon = new ImageIcon(MainPanel.class.getResource("ImageBin/deleteme.png"));
				setIcon(new ImageIcon(icon.getImage().getScaledInstance(48, 32, Image.SCALE_DEFAULT)));
				return listCellRendererComponent;
			}
		});
		panel.add(BorderLayout.BEFORE_LINE_BEGINS, new JScrollPane(view));

		JPanel tags = new JPanel();
		tags.setBorder(BorderFactory.createTitledBorder("Tags"));
		tags.setLayout(new GridLayout(30, 1));
		tags.add(new JCheckBox("Results must include all selected tags"));
		tags.add(new JSeparator());
		tags.add(new JLabel("Types (Models)"));
		tags.add(new JSeparator());
		tags.add(new JCheckBox("Building"));
		tags.add(new JCheckBox("Doodad"));
		tags.add(new JCheckBox("Item"));
		tags.add(new JCheckBox("User Interface"));
		panel.add(BorderLayout.CENTER, tags);

		mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
				new View("Hive Browser",
						new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
						panel)));
	}

	public static void openPreferences(MainPanel mainPanel) {
		ProgramPreferences programPreferences = new ProgramPreferences();
		programPreferences.loadFrom(ProgramGlobals.getPrefs());
		List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
		ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences, priorDataSources);

		int ret = JOptionPane.showConfirmDialog(mainPanel, programPreferencesPanel, "Preferences",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (ret == JOptionPane.OK_OPTION) {
			ProgramGlobals.getPrefs().loadFrom(programPreferences);
			List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
			boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
			if (changedDataSources) {
				SaveProfile.get().setDataSources(dataSources);
			}
			SaveProfile.save();
			if (changedDataSources) {
				com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar.updateDataSource(mainPanel);
//				dataSourcesChanged(MenuBar.directoryChangeNotifier, mainPanel.modelPanels);
			}
		}
	}

	public static void createAndShowRtfPanel(String filePath, String title) {
		DefaultStyledDocument document = new DefaultStyledDocument();
		JTextPane textPane = new JTextPane();
		textPane.setForeground(Color.BLACK);
		textPane.setBackground(Color.WHITE);
		RTFEditorKit rtfk = new RTFEditorKit();
		try {
			rtfk.read(GameDataFileSystem.getDefault().getResourceAsStream(filePath), document, 0);
		} catch (final BadLocationException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		textPane.setDocument(document);
		JFrame frame = new JFrame(title);
		frame.setContentPane(new JScrollPane(textPane));
		frame.setSize(650, 500);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public static void clearRecent(MainPanel mainPanel) {
		int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
				"Are you sure you want to clear the Recent history?", "Confirm Clear",
				JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			SaveProfile.get().clearRecent();
			com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar.updateRecent();
		}
	}

	public static void closeModelPanel(MainPanel mainPanel) {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		int oldIndex = ProgramGlobals.getModelPanels().indexOf(modelPanel);
		if (modelPanel != null) {
			if (modelPanel.close()) {
				ProgramGlobals.getModelPanels().remove(modelPanel);
				com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar.removeModelPanel(modelPanel);
				if (ProgramGlobals.getModelPanels().size() > 0) {
					int newIndex = Math.min(ProgramGlobals.getModelPanels().size() - 1, oldIndex);
					ModelLoader.setCurrentModel(mainPanel, ProgramGlobals.getModelPanels().get(newIndex));
				} else {
					// TODO remove from notifiers to fix leaks
					ModelLoader.setCurrentModel(mainPanel, null);
				}
			}
		}
	}

	public static void newModel(MainPanel mainPanel) {
		JPanel newModelPanel = new JPanel();
		newModelPanel.setLayout(new MigLayout("fill, ins 0"));
		newModelPanel.add(new JLabel("Model Name: "), "");
		JTextField newModelNameField = new JTextField("MrNew", 25);
		newModelPanel.add(newModelNameField, "wrap");

		SmartButtonGroup typeGroup = new SmartButtonGroup();
		typeGroup.addJRadioButton("Create Empty", null);
		typeGroup.addJRadioButton("Create Plane", null);
		typeGroup.addJRadioButton("Create Box", null);
		typeGroup.setSelectedIndex(0);
		newModelPanel.add(typeGroup.getButtonPanel());

		int userDialogResult = JOptionPane.showConfirmDialog(mainPanel, newModelPanel, "New Model", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (userDialogResult == JOptionPane.OK_OPTION) {
			EditableModel mdl = new EditableModel(newModelNameField.getText());
			if (typeGroup.getButton("Create Box").isSelected()) {
				SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
				JSpinner spinner = new JSpinner(sModel);
				int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Box: Choose Segments",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (userChoice != JOptionPane.OK_OPTION) {
					return;
				}
				ModelUtils.createBox(mdl, new Vec3(-64, -64, 0), new Vec3(64, 64, 128), ((Number) spinner.getValue()).intValue());
				mdl.setExtents(new ExtLog(128).setDefault());
			} else if (typeGroup.getButton("Create Plane").isSelected()) {
				SpinnerNumberModel sModel = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
				JSpinner spinner = new JSpinner(sModel);
				int userChoice = JOptionPane.showConfirmDialog(mainPanel, spinner, "Plane: Choose Segments",
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
				if (userChoice != JOptionPane.OK_OPTION) {
					return;
				}
				ModelUtils.createGroundPlane(mdl, new Vec3(64, 64, 0), new Vec3(-64, -64, 0),
						((Number) spinner.getValue()).intValue());
				mdl.setExtents(new ExtLog(128).setDefault());
			}

			ModelHandler modelHandler = new ModelHandler(mdl, mainPanel.getUndoHandler());
			ModelPanel temp = new ModelPanel(mainPanel, modelHandler, ProgramGlobals.getPrefs(),
					mainPanel.selectionItemTypeGroup, mainPanel.selectionModeGroup,
					mainPanel.modelStructureChangeListener, mainPanel.coordDisplayListener,
					mainPanel.viewportTransferHandler, mainPanel.viewportListener, RMSIcons.MDLIcon, false);
			ModelLoader.loadModel(mainPanel, true, true, temp);
		}

	}

	public static boolean closeOthers(MainPanel mainPanel) {
		boolean success = true;
		Iterator<ModelPanel> iterator = ProgramGlobals.getModelPanels().iterator();
		boolean closedCurrentPanel = false;
		ModelPanel lastUnclosedModelPanel = null;
		while (iterator.hasNext()) {
			ModelPanel panel = iterator.next();
			if (panel == ProgramGlobals.getCurrentModelPanel()) {
				lastUnclosedModelPanel = panel;
				continue;
			}
			if (success = panel.close()) {
//                mainPanel.windowMenu.remove(panel.getMenuItem());
				MenuBar.removeModelPanel(panel);
				iterator.remove();
				if (panel == ProgramGlobals.getCurrentModelPanel()) {
					closedCurrentPanel = true;
				}
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (closedCurrentPanel) {
			ModelLoader.setCurrentModel(mainPanel, lastUnclosedModelPanel);
		}
		return success;
	}

//	static View testItemResponse(MainPanel mainPanel) {
//		JPanel testPanel = new JPanel();
//
//		for (int i = 0; i < 3; i++) {
////					ControlledAnimationViewer animationViewer = new ControlledAnimationViewer(
////							currentModelPanel().getModelViewManager(), prefs);
////					animationViewer.setMinimumSize(new Dimension(400, 400));
////					AnimationController animationController = new AnimationController(
////							currentModelPanel().getModelViewManager(), true, animationViewer);
//
//			AnimationViewer animationViewer2 = new AnimationViewer(
//					ProgramGlobals.getCurrentModelPanel().getModelViewManager(), ProgramGlobals.getPrefs(), false);
//			animationViewer2.setMinimumSize(new Dimension(400, 400));
//			testPanel.add(animationViewer2);
////					testPanel.add(animationController);
//		}
//		testPanel.setLayout(new GridLayout(1, 4));
//		return new View("Test", null, testPanel);
//	}

	public static void addNewMaterial(MainPanel mainPanel) {
		EditableModel current = mainPanel.currentMDL();
		if (current != null) {
			Material material = new Material();
			Bitmap white = new Bitmap("Textures\\White.dds").setWrapHeight(true).setWrapWidth(true);
			material.getLayers().add(new Layer("None", white));
			if (current.getFormatVersion() == 1000) {
				material.makeHD();
			}
			current.add(material);
			mainPanel.modelStructureChangeListener.materialsListChanged();
		}
	}

	public static void recalculateTangents(EditableModel currentMDL, Component parent) {
		// copied from
		// https://github.com/TaylorMouse/MaxScripts/blob/master/Warcraft%203%20Reforged/GriffonStudios/GriffonStudios_Warcraft_3_Reforged_Export.ms#L169
		int zeroAreaUVTris = 0;
		currentMDL.doSavePreps(); // I wanted to use VertexId on the triangle
		for (Geoset theMesh : currentMDL.getGeosets()) {
			double[][] tan1 = new double[theMesh.getVertices().size()][];
			double[][] tan2 = new double[theMesh.getVertices().size()][];
			for (int nFace = 0; nFace < theMesh.getTriangles().size(); nFace++) {
				Triangle face = theMesh.getTriangle(nFace);

				GeosetVertex v1 = face.getVerts()[0];
				GeosetVertex v2 = face.getVerts()[1];
				GeosetVertex v3 = face.getVerts()[2];

				Vec3 vv1 = Vec3.getDiff(v2, v1);
				double x1 = v2.x - v1.x;
				double y1 = v2.y - v1.y;
				double z1 = v2.z - v1.z;


				Vec3 vv2 = Vec3.getDiff(v3, v1);
				double x2 = v3.x - v1.x;
				double y2 = v3.y - v1.y;
				double z2 = v3.z - v1.z;

				Vec2 w1 = v1.getTVertex(0);
				Vec2 w2 = v2.getTVertex(0);
				Vec2 w3 = v3.getTVertex(0);

				Vec2 st1 = Vec2.getDif(w2, w1);
				double s1 = w2.x - w1.x;
				double t1 = w2.y - w1.y;

				Vec2 st2 = Vec2.getDif(w3, w1);
				double s2 = w3.x - w1.x;
				double t2 = w3.y - w1.y;


				double tVertWeight = (s1 * t2) - (s2 * t1);
				if (tVertWeight == 0) {
					tVertWeight = 0.00000001;
					zeroAreaUVTris++;
				}

				double r = 1.0 / tVertWeight;

				double[] sdir = {((t2 * x1) - (t1 * x2)) * r, ((t2 * y1) - (t1 * y2)) * r, ((t2 * z1) - (t1 * z2)) * r};
				double[] tdir = {((s1 * x2) - (s2 * x1)) * r, ((s1 * y2) - (s2 * y1)) * r, ((s1 * z2) - (s2 * z1)) * r};

				tan1[face.getId(0)] = sdir;
				tan1[face.getId(1)] = sdir;
				tan1[face.getId(2)] = sdir;

				tan2[face.getId(0)] = tdir;
				tan2[face.getId(1)] = tdir;
				tan2[face.getId(2)] = tdir;
			}
			for (int vertexId = 0; vertexId < theMesh.getVertices().size(); vertexId++) {
				GeosetVertex gv = theMesh.getVertex(vertexId);
				Vec3 n = gv.getNormal();
				Vec3 t = new Vec3(tan1[vertexId]);

//				Vec3 v = new Vec3(t).sub(n).scale(n.dot(t)).normalize();
				Vec3 v = Vec3.getDiff(t, n).normalize();
				Vec3 cross = Vec3.getCross(n, t);

				Vec3 tanAsVert = new Vec3(tan2[vertexId]);

				double w = cross.dot(tanAsVert);

				if (w < 0.0) {
					w = -1.0;
				} else {
					w = 1.0;
				}
				gv.setTangent(v, (float) w);
			}
		}
		int goodTangents = 0;
		int badTangents = 0;
		for (Geoset theMesh : currentMDL.getGeosets()) {
			for (GeosetVertex gv : theMesh.getVertices()) {
				double dotProduct = gv.getNormal().dot(gv.getTang().getVec3());
//				System.out.println("dotProduct: " + dotProduct);
				if (Math.abs(dotProduct) <= 0.000001) {
					goodTangents += 1;
				} else {
					badTangents += 1;
				}
			}
		}
		if (parent != null) {
			JOptionPane.showMessageDialog(parent,
					"Tangent generation completed." +
							"\nGood tangents: " + goodTangents + ", bad tangents: " + badTangents + "" +
							"\nFound " + zeroAreaUVTris + " uv triangles with no area");
		} else {
			System.out.println(
					"Tangent generation completed." +
							"\nGood tangents: " + goodTangents + ", bad tangents: " + badTangents +
							"\nFound " + zeroAreaUVTris + " uv triangles with no area");
		}
	}

	public static void recalculateTangentsOld(EditableModel currentMDL) {
		for (Geoset theMesh : currentMDL.getGeosets()) {
			for (int nFace = 0; nFace < theMesh.getTriangles().size(); nFace++) {
				Triangle face = theMesh.getTriangle(nFace);

				GeosetVertex v1 = face.getVerts()[0];
				GeosetVertex v2 = face.getVerts()[0];
				GeosetVertex v3 = face.getVerts()[0];

				Vec2 uv1 = v1.getTVertex(0);
				Vec2 uv2 = v2.getTVertex(0);
				Vec2 uv3 = v3.getTVertex(0);

				Vec3 dV1 = new Vec3(v1).sub(v2);
				Vec3 dV2 = new Vec3(v1).sub(v3);

				Vec2 dUV1 = new Vec2(uv1).sub(uv2);
				Vec2 dUV2 = new Vec2(uv1).sub(uv3);
				double area = (dUV1.x * dUV2.y) - (dUV1.y * dUV2.x);
				int sign = (area < 0) ? -1 : 1;
				Vec3 tangent = new Vec3(1, 0, 0);

				tangent.x = (dV1.x * dUV2.y) - (dUV1.y * dV2.x);
				tangent.y = (dV1.y * dUV2.y) - (dUV1.y * dV2.y);
				tangent.z = (dV1.z * dUV2.y) - (dUV1.y * dV2.z);

				tangent.normalize();
				tangent.scale(sign);

				Vec3 faceNormal = new Vec3(v1.getNormal());
				faceNormal.add(v2.getNormal());
				faceNormal.add(v3.getNormal());
				faceNormal.normalize();
			}
		}
	}

	public static void copyCutPast(MainPanel mainPanel, TransferActionListener transferActionListener, ActionEvent e) {
		if (!mainPanel.animationModeState) {
			transferActionListener.actionPerformed(e);
		} else {
			if (e.getActionCommand().equals(TransferHandler.getCutAction().getValue(Action.NAME))) {
				mainPanel.timeSliderPanel.cut();
			} else if (e.getActionCommand().equals(TransferHandler.getCopyAction().getValue(Action.NAME))) {
				mainPanel.timeSliderPanel.copy();
			} else if (e.getActionCommand().equals(TransferHandler.getPasteAction().getValue(Action.NAME))) {
				mainPanel.timeSliderPanel.paste();
			}
		}
	}

	public static void sortBones(MainPanel mainPanel) {
		final EditableModel model = mainPanel.currentMDL();
		final List<IdObject> roots = new ArrayList<>();
		final List<IdObject> modelList = model.getIdObjects();
		for (final IdObject object : modelList) {
			if (object.getParent() == null) {
				roots.add(object);
			}
		}
		final Queue<IdObject> bfsQueue = new LinkedList<>(roots);
		final List<IdObject> result = new ArrayList<>();
		while (!bfsQueue.isEmpty()) {
			final IdObject nextItem = bfsQueue.poll();
			bfsQueue.addAll(nextItem.getChildrenNodes());
			result.add(nextItem);
		}
		for (final IdObject node : result) {
			model.remove(node);
		}
		mainPanel.modelStructureChangeListener.nodesRemoved(result);
		for (final IdObject node : result) {
			model.add(node);
		}
		mainPanel.modelStructureChangeListener.nodesAdded(result);
	}

	public static void minimizeGeoset(MainPanel mainPanel) {
		final int confirm = JOptionPane.showConfirmDialog(mainPanel,
				"This is experimental and I did not code the Undo option for it yet. Continue?" +
						"\nMy advice is to click cancel and save once first.",
				"Confirmation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirm != JOptionPane.OK_OPTION) {
			return;
		}

		mainPanel.currentMDL().doSavePreps();

		final Map<Geoset, Geoset> sourceToDestination = new HashMap<>();
		final List<Geoset> retainedGeosets = new ArrayList<>();
		for (final Geoset geoset : mainPanel.currentMDL().getGeosets()) {
			boolean alreadyRetained = false;
			for (final Geoset retainedGeoset : retainedGeosets) {
				if (retainedGeoset.getMaterial().equals(geoset.getMaterial())
						&& (retainedGeoset.getSelectionGroup() == geoset.getSelectionGroup())
						&& (retainedGeoset.getUnselectable() == geoset.getUnselectable())
						&& isGeosetAnimationsMergable(retainedGeoset.getGeosetAnim(), geoset.getGeosetAnim())) {
					alreadyRetained = true;
					for (final GeosetVertex gv : geoset.getVertices()) {
						retainedGeoset.add(gv);
					}
					for (final Triangle t : geoset.getTriangles()) {
						retainedGeoset.add(t);
					}
					break;
				}
			}
			if (!alreadyRetained) {
				retainedGeosets.add(geoset);
			}
		}
		final EditableModel currentMDL = mainPanel.currentMDL();
		final List<Geoset> geosets = currentMDL.getGeosets();
		final List<Geoset> geosetsRemoved = new ArrayList<>();
		final Iterator<Geoset> iterator = geosets.iterator();
		while (iterator.hasNext()) {
			final Geoset geoset = iterator.next();
			if (!retainedGeosets.contains(geoset)) {
				iterator.remove();
				final GeosetAnim geosetAnim = geoset.getGeosetAnim();
				if (geosetAnim != null) {
					currentMDL.remove(geosetAnim);
				}
				geosetsRemoved.add(geoset);
			}
		}
		mainPanel.modelStructureChangeListener.geosetsRemoved(geosetsRemoved);
	}

	private static boolean isGeosetAnimationsMergable(final GeosetAnim first, final GeosetAnim second) {
		if ((first == null) && (second == null)) {
			return true;
		}
		if ((first == null) || (second == null)) {
			return false;
		}
		final AnimFlag<?> firstVisibilityFlag = first.getVisibilityFlag();
		final AnimFlag<?> secondVisibilityFlag = second.getVisibilityFlag();
		if ((firstVisibilityFlag == null) != (secondVisibilityFlag == null)) {
			return false;
		}
		if ((firstVisibilityFlag != null) && !firstVisibilityFlag.equals(secondVisibilityFlag)) {
			return false;
		}
		if (first.isDropShadow() != second.isDropShadow()) {
			return false;
		}
		if (Math.abs(first.getStaticAlpha() - second.getStaticAlpha()) > 0.001) {
			return false;
		}
		if ((first.getStaticColor() == null) != (second.getStaticColor() == null)) {
			return false;
		}
		if ((first.getStaticColor() != null) && !first.getStaticColor().equalLocs(second.getStaticColor())) {
			return false;
		}
		final AnimFlag<?> firstAnimatedColor = first.find("Color");
		final AnimFlag<?> secondAnimatedColor = second.find("Color");
		if ((firstAnimatedColor == null) != (secondAnimatedColor == null)) {
			return false;
		}
		return (firstAnimatedColor == null) || firstAnimatedColor.equals(secondAnimatedColor);
	}

	public static void removeMaterialDuplicates(MainPanel mainPanel) {
		EditableModel model = ProgramGlobals.getCurrentModelPanel().getModel();
		List<Material> materials = model.getMaterials();
		Map<Material, Material> sameMaterialMap = new HashMap<>();
		for (int i = 0; i < materials.size(); i++) {
			Material material1 = materials.get(i);
			for (int j = i + 1; j < materials.size(); j++) {
				Material material2 = materials.get(j);
				System.out.println(material1.getName() + " == " + material2.getName());
				if (material1.equals(material2)) {
					if (!sameMaterialMap.containsKey(material2)) {
						sameMaterialMap.put(material2, material1);
					}
				}
			}
		}

		List<Geoset> geosets = model.getGeosets();
		for (Geoset geoset : geosets) {
			if (sameMaterialMap.containsKey(geoset.getMaterial())) {
				geoset.setMaterial(sameMaterialMap.get(geoset.getMaterial()));
			}
		}

		materials.removeAll(sameMaterialMap.keySet());
		mainPanel.modelStructureChangeListener.materialsListChanged();
	}
}
