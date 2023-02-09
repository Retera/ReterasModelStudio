package com.hiveworkshop.rms.ui.application.tools.twilacimport;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.addactions.AddGeosetAction;
import com.hiveworkshop.rms.editor.actions.animation.AddTimelineAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelFromFile;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.OpenFromInternal;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpliceHandsByTwilac {
	FileDialog fc;
	JMenu skinSplice;

	public SpliceHandsByTwilac(){
		JMenuItem skinSpliceFromFile = new JMenuItem("From File");
		skinSpliceFromFile.addActionListener(e -> spliceFileModel());

		JMenuItem skinSpliceFromWorkspace = new JMenuItem("From Workspace");
		skinSpliceFromWorkspace.addActionListener(e -> spliceFromWorkspace());

		JMenuItem skinSpliceFromModel = new JMenuItem("From Model");
		skinSpliceFromModel.addActionListener(e -> spliceFromInternalModel());

//		JMenuItem skinSpliceFromUnit = new JMenuItem("From Unit");
//		skinSpliceFromUnit.addActionListener(e -> spliceFromInternalUnit());

		skinSplice = new JMenu("Splice Hands into Current");
		skinSplice.add(skinSpliceFromFile);
		skinSplice.add(skinSpliceFromWorkspace);
		skinSplice.add(skinSpliceFromModel);
//		skinSplice.add(skinSpliceFromUnit);
	}

	public JMenu getMenu(){
		return skinSplice;
	}

	public void ugg(){
		JMenuItem skinSpliceFromFile = new JMenuItem("From File");
		skinSpliceFromFile.addActionListener(e -> spliceFileModel());

		JMenuItem skinSpliceFromWorkspace = new JMenuItem("From Workspace");
		skinSpliceFromWorkspace.addActionListener(e -> spliceFromWorkspace());

		JMenuItem skinSpliceFromModel = new JMenuItem("From Model");
		skinSpliceFromModel.addActionListener(e -> spliceFromInternalModel());

//		JMenuItem skinSpliceFromUnit = new JMenuItem("From Unit");
//		skinSpliceFromUnit.addActionListener(e -> spliceFromInternalUnit());

		JMenu skinSplice = new JMenu("Skin Splice Mesh into Current");
		skinSplice.add(skinSpliceFromFile);
		skinSplice.add(skinSpliceFromWorkspace);
		skinSplice.add(skinSpliceFromModel);
//		skinSplice.add(skinSpliceFromUnit);
	}

//	private void spliceFromInternalUnit() {
//		final GameObject unitFetched = fetchUnit();
//		if (unitFetched != null) {
//			final String filepath = convertPathToMDX(unitFetched.getField("file"));
//			if (filepath != null) {
//				try (BlizzardDataInputStream in = new BlizzardDataInputStream(MpqCodebase.get().getResourceAsStream(filepath))) {
//					final EditableModel mdl = new EditableModel(MdxUtils.loadModel(in));
//					mdl.setFileRef(null);
//					doSkinSpliceUI(mdl);
//				} catch (final FileNotFoundException e) {
//					e.printStackTrace();
//					ExceptionPopup.display(e);
//					throw new RuntimeException("Reading mdx failed");
//				} catch (final IOException e) {
//					e.printStackTrace();
//					ExceptionPopup.display(e);
//					throw new RuntimeException("Reading mdx failed");
//				}
//			}
//		}
//	}

	private void spliceFromInternalModel() {
		EditableModel model = OpenFromInternal.getInternalModel();
		if (model != null) {
//			doSkinSpliceUI(model);

			new TwilacsHandWizard(model, ProgramGlobals.getCurrentModelPanel().getModelHandler());
		}
	}

	private void spliceFromWorkspace() {
		EditableModel mdl = ModelFromFile.getWorkspaceModelCopy(
				"Import from Workspace",
				"Choose a workspace item to import data from:",
				ProgramGlobals.getMainPanel());
		if (mdl != null) {
//			doSkinSpliceUI(mdl);
			new TwilacsHandWizard(mdl, ProgramGlobals.getCurrentModelPanel().getModelHandler());
		}
	}

	private void spliceFileModel() {
		EditableModel mdl = ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, null);
		if (mdl != null) {
			new TwilacsHandWizard(mdl, ProgramGlobals.getCurrentModelPanel().getModelHandler());
//			doSkinSpliceUI(mdl);
		}
	}

	protected void doSkinSpliceUI(EditableModel meshModel) {
		ModelHandler modelHandler = ProgramGlobals.getCurrentModelPanel().getModelHandler();
		EditableModel animationModel = modelHandler.getModel();
		final Map<String, Bone> nameToNode = new HashMap<>();
		for (Bone bone : animationModel.getBones()) {
			nameToNode.put(bone.getName(), bone);
		}

		List<UndoAction> undoActions = new ArrayList<>();
		List<String> warnings = new ArrayList<>();

//		List<Geoset> newGeosets = new ArrayList<>();
		for (final Geoset geo : meshModel.getGeosets()) {
			for (final GeosetVertex gv : geo.getVertices()) {
				if(gv.getSkinBones() != null){
					replaceHDBones(animationModel, nameToNode, warnings, gv);
				} else {
					replaceSDBones(animationModel, nameToNode, warnings, gv);
				}
			}
			undoActions.add(new AddGeosetAction(geo, animationModel, null));
			Geoset geosetAnim = animationModel.getGeoset(0);
			if(geosetAnim != null){
				undoActions.add(new AddTimelineAction<>(geo, geosetAnim.getVisibilityFlag().deepCopy()));
			}
		}
		modelHandler.getUndoManager().pushAction(new CompoundAction("Splice Mesh", undoActions, ModelStructureChangeListener.changeListener::geosetsUpdated).redo());

	}

	private void replaceSDBones(EditableModel animationModel, Map<String, Bone> nameToNode, List<String> warnings, GeosetVertex gv) {
		List<Bone> bones = gv.getMatrix().getBones();
		for (int i = 0; i < bones.size(); i++) {
			IdObject bone = bones.get(i);
			if (bone != null) {
				final String boneName = bone.getName();
				Bone replacement = nameToNode.get(boneName);
				int upwardDepth = 0;
				while ((replacement == null) && (bone != null)) {
					bone = bone.getParent();
					upwardDepth++;
					if (bone != null) {
						replacement = nameToNode.get(bone.getName());
					} else {
						replacement = null;
					}
				}
				if (replacement == null) {
					warnings.add("Failed to replace: " + boneName);
					replacement = animationModel.getBones().get(0);
//							throw new IllegalStateException("failed to replace: " + boneName);
				} else {
					while ((upwardDepth > 0) && (replacement.getChildrenNodes().size() == 1)
							&& (replacement.getChildrenNodes().get(0) instanceof Bone)) {
						replacement = (Bone) replacement.getChildrenNodes().get(0);
						upwardDepth--;
					}
				}
				gv.setBone(i, replacement);

			}
		}
	}

	private void replaceHDBones(EditableModel animationModel, Map<String, Bone> nameToNode, List<String> warnings, GeosetVertex gv) {
		for (int i = 0; i < gv.getSkinBones().length; i++) {
			IdObject bone = gv.getSkinBones()[i].getBone();
			if (bone != null) {
				final String boneName = bone.getName();
				Bone replacement = nameToNode.get(boneName);
				int upwardDepth = 0;
				while ((replacement == null) && (bone != null)) {
					bone = bone.getParent();
					upwardDepth++;
					if (bone != null) {
						replacement = nameToNode.get(bone.getName());
					} else {
						replacement = null;
					}
				}
				if (replacement == null) {
					warnings.add("Failed to replace: " + boneName);
					replacement = animationModel.getBones().get(0);
//							throw new IllegalStateException("failed to replace: " + boneName);
				} else {
					while ((upwardDepth > 0) && (replacement.getChildrenNodes().size() == 1)
							&& (replacement.getChildrenNodes().get(0) instanceof Bone)) {
						replacement = (Bone) replacement.getChildrenNodes().get(0);
						upwardDepth--;
					}
				}
				gv.getSkinBones()[i].setBone(replacement);

			}
		}
	}
}
