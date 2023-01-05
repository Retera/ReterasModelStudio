package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.editor.StaticMeshScaleAction;
import com.hiveworkshop.rms.editor.actions.model.RecalculateExtentsAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.editor.model.util.TwiAiIoSys;
import com.hiveworkshop.rms.editor.model.util.TwiAiSceneParser;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.model.editors.FloatEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.IntEditorJSpinner;
import com.hiveworkshop.rms.ui.application.model.editors.TwiTextField;
import com.hiveworkshop.rms.ui.util.ExceptionPopup;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.uiFactories.Button;
import jassimp.AiPostProcessSteps;
import jassimp.AiProgressHandler;
import jassimp.AiScene;
import jassimp.Jassimp;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ObjBatchConverter extends JPanel {
	FileDialog fileDialog;
	File[] chosenFiles;
	File saveLocation;
	JLabel numFiles = new JLabel("0");
	JLabel convertProgress = new JLabel("");
	JLabel step = new JLabel("");
	JButton convButton;
	String[] ext = {"mdx"};

	public ObjBatchConverter(){
		super(new MigLayout());
		fileDialog = new FileDialog(this);
//		add(new JLabel("Convert OBJ"), "wrap");
		add(Button.create("Choose files", e -> chooseFiles()), "wrap");

		add(new JLabel("destination: "), "split");
		add(new TwiTextField(20, s -> saveLocation = new File(s)), "wrap");

		add(new JLabel("chosen files: "), "split");
		add(numFiles, "wrap");

		add(new JLabel("version: "), "split");
		IntEditorJSpinner versionSpinner = new IntEditorJSpinner(800, 800, 10000, 100, null);
		add(versionSpinner, "wrap");

		add(new JLabel("scale: "), "split");
		FloatEditorJSpinner scaleSpinner = new FloatEditorJSpinner(1, -100000, 100000, 1, null);
		add(scaleSpinner, "wrap");

		SmartButtonGroup smartButtonGroup = new SmartButtonGroup();
		smartButtonGroup.addJRadioButton("mdx", e -> ext[0] = "mdx");
		smartButtonGroup.addJRadioButton("mdl", e -> ext[0] = "mdl");
		smartButtonGroup.setButtonConst("").setSelectedIndex(0);
		add(smartButtonGroup.getButtonPanel(), "wrap");
		add(convertProgress, "wrap");
		add(step, "wrap");
		convButton = Button.create("Convert", e -> doConvert(new Vec3(Vec3.ONE).scale(scaleSpinner.getFloatValue()),
				saveLocation, versionSpinner.getIntValue(), ext[0], handleExisting.ASK));
		add(convButton, "wrap");


	}

	public JMenuItem getMenuItem(){
		JMenuItem menuItem = new JMenuItem("OBJ Batch Convert");
		menuItem.addActionListener(e -> showPopup(ProgramGlobals.getMainPanel()));
		return menuItem;
	}

	public static void showPopup(JComponent parent){
		ObjBatchConverter skinningOptionPanel = new ObjBatchConverter();
		skinningOptionPanel.setPreferredSize(new Dimension(800, 650));
		skinningOptionPanel.revalidate();
//		FramePopup.show(skinningOptionPanel, ProgramGlobals.getMainPanel(), "Edit Textures");
		FramePopup.show(skinningOptionPanel, parent, "Convert OBJ");
	}

	private void chooseSaveLocation(){
		;
		if(chosenFiles == null){
			numFiles.setText("0");
			convButton.setEnabled(false);
		} else {
			numFiles.setText("" + chosenFiles.length);
			convButton.setEnabled(0 < chosenFiles.length);
		}
	}

	private void chooseFiles(){
		chosenFiles = fileDialog.openFiles(FileDialog.OPEN_MODEL);
		if(chosenFiles == null){
			numFiles.setText("0");
			convButton.setEnabled(false);
		} else {
			numFiles.setText("" + chosenFiles.length);
			convButton.setEnabled(0 < chosenFiles.length);
		}
	}

	enum handleExisting {
		OVERWRITE,
		SKIPP,
		ASK
	}

	private void doConvert(Vec3 scale, File saveLocation, int version, String extension, handleExisting existing){
		if(chosenFiles != null){
			int successConv = 0;
			int totFiles = chosenFiles.length;
			for (int i = 0; i<totFiles; i++) {
				convertProgress.setText((i+1) + " / " + totFiles);
				File file = chosenFiles[i];
				if(file.exists()) {
					step.setText("loading file");
					EditableModel model = getAssImpModel(file);
					if(model != null){
						model.setFormatVersion(version);
						File fileRef = model.getFileRef();
						String impExtension = fileDialog.getExtension(fileRef);
						System.out.println("extension: " + impExtension);
						String fileName = fileRef.getName().replaceAll("\\." + impExtension + "$", "");
						model.setName(fileName);
						addRootAndStand(model, version);
						if(scale != null && !scale.equalLocs(Vec3.ONE)){
							step.setText("scaling model");
							scaleModel(model, scale);
						}
						step.setText("recalculating extents");
						new RecalculateExtentsAction(model, model.getGeosets()).redo();
						step.setText("saving model");
						File realSaveLoc = saveLocation != null && !saveLocation.getPath().isBlank() ? saveLocation : fileRef.getParentFile();
						File saveFile = new File(realSaveLoc, fileName + "." + extension);
						boolean proceed = !saveFile.exists()
								|| saveFile.exists() && existing == handleExisting.OVERWRITE
								|| saveFile.exists() && existing == handleExisting.ASK && ask(saveFile.getName())
								;
						if (proceed){
							System.out.println("Saving at: \"" + saveFile.getPath() + "\"");
							try {
								if (extension.equals("mdl")) {
									MdxUtils.saveMdl(model, saveFile);
								} else {
									MdxUtils.saveMdx(model, saveFile);
								}
								successConv++;
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					}
				}
			}
			convertProgress.setText("");
			step.setText("Successfully converted " + successConv + "/" + totFiles + " files");
		}
	}

	private boolean ask(String fileName){
		int foundExisting = JOptionPane.showConfirmDialog(this, "overwrite " + fileName, "Found Existing Model", JOptionPane.YES_NO_OPTION);
		return foundExisting == JOptionPane.YES_OPTION;
	}

	private void scaleModel(EditableModel model, Vec3 scale){
		Set<GeosetVertex> vertices = new HashSet<>();
		for(Geoset geoset : model.getGeosets()){
			vertices.addAll(geoset.getVertices());
		}
		Set<CameraNode> cameraNodes = new HashSet<>();

		for(Camera camera : model.getCameras()){
			cameraNodes.add(camera.getSourceNode());
			cameraNodes.add(camera.getTargetNode());
		}
		new StaticMeshScaleAction(vertices, model.getIdObjects(), cameraNodes, Vec3.ZERO, scale, new Mat4()).redo();
	}

	private void addRootAndStand(EditableModel model, int version){
		if(model.getAnims().size() == 0){
			Animation animation = new Animation("Stand", 0, 3000);
			model.add(animation);
		} else if(model.getAnims().size() == 1 && model.getAnim(0).getName().equals("emptyAnim")){
			model.getAnim(0).setName("Stand");
		}
		if(model.getBones().size() == 0){
			System.out.println("adding bone!");
			Bone root = new Bone("Root");
			model.add(root);
			for(Geoset geoset : model.getGeosets()){
				for(GeosetVertex vertex : geoset.getVertices()){
					System.out.println("adding 'Root' to vertex!");
					if(version < 900){
						vertex.removeSkinBones();
						vertex.addBoneAttachment(root);
					} else {
						vertex.setSkinBone(root, (short) 255, 0);
					}
				}
			}
		}
	}

	private EditableModel getAssImpModel(File f) {
		try {
			System.out.println("importing file \"" + f.getName() + "\" this might take a while...");
			long timeStart = System.currentTimeMillis();
			AiProgressHandler aiProgressHandler = new AiProgressHandler() {
				@Override
				public boolean update(float v) {
//					System.out.println("progress: " + (int)((v+1)*100) + "%  " + (System.currentTimeMillis()-timeStart) + " ms");
					return true;
				}
			};
//			AiClassLoaderIOSystem aiIOSystem = new AiClassLoaderIOSystem();
			TwiAiIoSys twiAiIoSys = new TwiAiIoSys();


			HashSet<AiPostProcessSteps> processSteps = new HashSet<>();
			processSteps.add(AiPostProcessSteps.TRIANGULATE);
			processSteps.add(AiPostProcessSteps.REMOVE_REDUNDANT_MATERIALS);
			AiScene scene = Jassimp.importFile(f.getPath(), processSteps, twiAiIoSys, aiProgressHandler);
			TwiAiSceneParser twiAiSceneParser = new TwiAiSceneParser(scene);
			System.out.println("took " + (System.currentTimeMillis() - timeStart) + " ms to load the model");
			EditableModel model = twiAiSceneParser.getEditableModel();
			model.setFileRef(f);
			return model;
			//
		} catch (final Exception e) {
			ExceptionPopup.display(e);
			e.printStackTrace();
		}
		return null;
	}
}
