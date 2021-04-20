package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.filesystem.sources.CompoundDataSource;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.parsers.slk.StandardObjectData;
import com.hiveworkshop.rms.parsers.w3o.WTSFile;
import com.hiveworkshop.rms.parsers.w3o.War3ObjectDataChangeset;
import com.hiveworkshop.rms.ui.application.viewer.AnimationViewer;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
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
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class MenuBarActions {
	static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

	static void updateUIFromProgramPreferences(List<ModelPanel> modelPanels, ProgramPreferences prefs) {
		for (ModelPanel mpanel : modelPanels) {
//            mpanel.getEditorRenderModel().setSpawnParticles(prefs.getRenderParticles());
//            mpanel.getEditorRenderModel().setAllowInanimateParticles(prefs.getRenderStaticPoseParticles());
//            mpanel.getAnimationViewer().setSpawnParticles(prefs.getRenderParticles());
		}
	}

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

	static void openUnitViewer(MainPanel mainPanel) {
		UnitEditorTree unitEditorTree = MainLayoutCreator.createUnitEditorTree(mainPanel);
		mainPanel.rootWindow.setWindow(new SplitWindow(true, 0.75f, mainPanel.rootWindow.getWindow(),
				new View("Unit Browser",
						new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST)),
						new JScrollPane(unitEditorTree))));
	}

	static void openHiveViewer(MainPanel mainPanel) {
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

	static void openPreferences(MainPanel mainPanel) {
		ProgramPreferences programPreferences = new ProgramPreferences();
		programPreferences.loadFrom(mainPanel.prefs);
		List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
		ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences, priorDataSources);

		int ret = JOptionPane.showConfirmDialog(mainPanel, programPreferencesPanel, "Preferences",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (ret == JOptionPane.OK_OPTION) {
			mainPanel.prefs.loadFrom(programPreferences);
			List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
			boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
			if (changedDataSources) {
				SaveProfile.get().setDataSources(dataSources);
			}
			SaveProfile.save();
			if (changedDataSources) {
				dataSourcesChanged(mainPanel.directoryChangeNotifier, mainPanel.modelPanels);
			}
			updateUIFromProgramPreferences(mainPanel.modelPanels, mainPanel.prefs);
		}
	}

	static void createAndShowRtfPanel(String filePath, String title) {
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
		// JOptionPane.showMessageDialog(this,new JScrollPane(textPane));
	}

	static void duplicateSelectionActionRes(MainPanel mainPanel) {
		// final int x = JOptionPane.showConfirmDialog(this,
		// "This is an irreversible process that will split selected
		// vertices into many copies of themself, one for each face, so
		// you can wrap textures and normals in a different
		// way.\n\nContinue?",
		// "Warning"/* : Divide Vertices" */,
		// JOptionPane.OK_CANCEL_OPTION);
		// if (x == JOptionPane.OK_OPTION) {
		ModelPanel currentModelPanel = mainPanel.currentModelPanel();
		if (currentModelPanel != null) {
			currentModelPanel.getUndoManager().pushAction(currentModelPanel.getModelEditorManager()
					.getModelEditor().cloneSelectedComponents(mainPanel.namePicker));
		}
		// }
	}

	static void clearRecent(MainPanel mainPanel) {
		int dialogResult = JOptionPane.showConfirmDialog(mainPanel,
				"Are you sure you want to clear the Recent history?", "Confirm Clear",
				JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			SaveProfile.get().clearRecent();
			MenuBar.updateRecent();
		}
	}

	static void closePanel(MainPanel mainPanel) {
		ModelPanel modelPanel = mainPanel.currentModelPanel();
		int oldIndex = mainPanel.modelPanels.indexOf(modelPanel);
		if (modelPanel != null) {
			if (modelPanel.close(mainPanel)) {
				mainPanel.modelPanels.remove(modelPanel);
//                mainPanel.windowMenu.remove(modelPanel.getMenuItem());
				MenuBar.windowMenu.remove(modelPanel.getMenuItem());
				if (mainPanel.modelPanels.size() > 0) {
					int newIndex = Math.min(mainPanel.modelPanels.size() - 1, oldIndex);
					MPQBrowserView.setCurrentModel(mainPanel, mainPanel.modelPanels.get(newIndex));
				} else {
					// TODO remove from notifiers to fix leaks
					MPQBrowserView.setCurrentModel(mainPanel, null);
				}
			}
		}
	}

	static void newModel(MainPanel mainPanel) {
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
			ModelPanel temp = new ModelPanel(mainPanel, mdl, mainPanel.prefs, mainPanel,
					mainPanel.selectionItemTypeGroup, mainPanel.selectionModeGroup,
					mainPanel.modelStructureChangeListener, mainPanel.coordDisplayListener,
					mainPanel.viewportTransferHandler, mainPanel.viewportListener, RMSIcons.MDLIcon, false);
			MPQBrowserView.loadModel(mainPanel, true, true, temp);
		}

	}

	public static boolean closeOthers(MainPanel mainPanel, ModelPanel panelToKeepOpen) {
		boolean success = true;
		Iterator<ModelPanel> iterator = mainPanel.modelPanels.iterator();
		boolean closedCurrentPanel = false;
		ModelPanel lastUnclosedModelPanel = null;
		while (iterator.hasNext()) {
			ModelPanel panel = iterator.next();
			if (panel == panelToKeepOpen) {
				lastUnclosedModelPanel = panel;
				continue;
			}
			if (success = panel.close(mainPanel)) {
//                mainPanel.windowMenu.remove(panel.getMenuItem());
				MenuBar.windowMenu.remove(panel.getMenuItem());
				iterator.remove();
				if (panel == mainPanel.currentModelPanel) {
					closedCurrentPanel = true;
				}
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (closedCurrentPanel) {
			MPQBrowserView.setCurrentModel(mainPanel, lastUnclosedModelPanel);
		}
		return success;
	}


	public static void refreshController(JScrollPane geoControl, JScrollPane geoControlModelData) {
		if (geoControl != null) {
			geoControl.repaint();
		}
		if (geoControlModelData != null) {
			geoControlModelData.repaint();
		}
	}

	static View testItemResponse(MainPanel mainPanel) {
		JPanel testPanel = new JPanel();

		for (int i = 0; i < 3; i++) {
//					ControlledAnimationViewer animationViewer = new ControlledAnimationViewer(
//							currentModelPanel().getModelViewManager(), prefs);
//					animationViewer.setMinimumSize(new Dimension(400, 400));
//					AnimationController animationController = new AnimationController(
//							currentModelPanel().getModelViewManager(), true, animationViewer);

			AnimationViewer animationViewer2 = new AnimationViewer(
					mainPanel.currentModelPanel().getModelViewManager(), mainPanel.prefs, false);
			animationViewer2.setMinimumSize(new Dimension(400, 400));
			testPanel.add(animationViewer2);
//					testPanel.add(animationController);
		}
		testPanel.setLayout(new GridLayout(1, 4));
		return new View("Test", null, testPanel);
	}

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
}
