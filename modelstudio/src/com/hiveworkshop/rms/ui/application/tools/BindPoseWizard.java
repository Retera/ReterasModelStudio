package com.hiveworkshop.rms.ui.application.tools;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.util.BoolAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.ModelFromFile;
import com.hiveworkshop.rms.ui.application.actionfunctions.ImportFromUnit;
import com.hiveworkshop.rms.ui.application.actionfunctions.OpenFromInternal;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.util.TwiPopup;
import com.hiveworkshop.rms.util.FramePopup;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.SmartButtonGroup;
import com.hiveworkshop.rms.util.Vec3;
import com.hiveworkshop.rms.util.uiFactories.Button;
import com.hiveworkshop.rms.util.uiFactories.CheckBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.util.*;

public class BindPoseWizard extends JPanel {
	private static boolean onlySBool = false;
	private static boolean locationFromMat = false;
	private ModelHandler modelHandler;


	public BindPoseWizard(ModelHandler modelHandler){
		super(new MigLayout("", "", ""));
		this.modelHandler = modelHandler;
		String selToolTip = "Only edit BindPoses of selected nodes/cameras";
		add(CheckBox.setTooltip(CheckBox.create("Only selected", onlySBool, b -> onlySBool = b), selToolTip), "wrap"); // Should maybe have the options [all], [visible], [editable] and [selected]
		JPanel formSelfPanel = new JPanel(new MigLayout(""));
		formSelfPanel.setBorder(new TitledBorder("Update BindPose"));
		formSelfPanel.add(Button.setTooltip(Button.create("New BindPose from pivots", e -> doUpdateBP(false)), "Clear BindPose Rotations"));
		formSelfPanel.add(Button.setTooltip(Button.create("Update BindPose locations", e -> doUpdateBP(true)), "Preserve BindPose Rotations"));
		add(formSelfPanel, "wrap");


		JPanel formOtherPanel = new JPanel(new MigLayout(""));
		formOtherPanel.setBorder(new TitledBorder("Import BindPose Rotations"));

		SmartButtonGroup smartButtonGroup = new SmartButtonGroup(); // Should maybe have the options [location from new BP] and [location from source pivot]
		smartButtonGroup.addJRadioButton("Location from pivot", null).setToolTipText("Use node location for BindPose location");
		smartButtonGroup.addJRadioButton("Location from original BindPose", null, b->locationFromMat=b).setToolTipText("Keep the existing location in the BindPose");
		formOtherPanel.add(smartButtonGroup.setSelectedIndex(locationFromMat ? 1 : 0), "spanx, wrap");

		formOtherPanel.add(Button.create("From File", e -> doFetchFromOther(ModelFromFile.chooseModelFile(FileDialog.OPEN_WC_MODEL, this), onlySBool, locationFromMat)));
		formOtherPanel.add(Button.create("From Unit", e -> doFetchFromOther(ImportFromUnit.getFileModel(), onlySBool, locationFromMat)));
		formOtherPanel.add(Button.create("From Internal", e -> doFetchFromOther(OpenFromInternal.getInternalModel(), onlySBool, locationFromMat)));
		formOtherPanel.add(Button.create("From Workspace", e -> doFetchFromOther(ModelFromFile.getWorkspaceModelCopy(
				"Import from Workspace",
				"Choose a workspace item to import data from:",
				this), onlySBool, locationFromMat)));
		add(formOtherPanel);
	}

	public static void showPanel(JComponent parent, ModelHandler modelHandler) {
		BindPoseWizard bindPoseWizard = new BindPoseWizard(modelHandler);
		FramePopup.show(bindPoseWizard, parent, "BindPose Wizard");
	}


	private void doUpdateBP(boolean keepRot) {
		Collection<IdObject> idObjects = onlySBool ? modelHandler.getModelView().getSelectedIdObjects() : modelHandler.getModel().getIdObjects();
		Collection<Camera> cameras = onlySBool ? modelHandler.getModelView().getSelectedCameras() : modelHandler.getModel().getCameras();
		if(0 < idObjects.size()) {
			List<UndoAction> bpActions = getBPActions(idObjects, cameras, keepRot);
			int numBpImported = bpActions.size();

			if(!modelHandler.getModel().isUseBindPose()){
				bpActions.add(new BoolAction(modelHandler.getModel()::setUseBindPose, true, "", null));
			}
			modelHandler.getUndoManager().pushAction(new CompoundAction("Recalculate BindPoses", bpActions, null).redo());
			String bpImp = "BindPose" + (numBpImported == 1 ? "" : "s") + (keepRot ? " Updated" : " Created");
			TwiPopup.quickDismissPopup(this, numBpImported + " " + bpImp, bpImp);
		} else {
			if (onlySBool){
				TwiPopup.quickDismissPopup(this, "No nodes selected", "No BindPose Imported");
			} else {
//						TwiPopup.quickDismissPopup(this, "Found no editable nodes", "No BindPose Imported");
				TwiPopup.quickDismissPopup(this, "Found no nodes", "No BindPose Imported");
			}
		}
	}


	private void doFetchFromOther(EditableModel model, boolean onlySBool, boolean locationFromMat){
		if (model != null) {
			String modelName = "\"" + model.getName() + "\"";
			String filePath = model.getFile() == null ? "" : " (\"" + model.getFile() + "\")";
			String modelIdentifier = modelName + filePath;
			if (model.isUseBindPose()) {
				Collection<IdObject> idObjects = onlySBool ? modelHandler.getModelView().getSelectedIdObjects() : modelHandler.getModel().getIdObjects();
				if(0 < idObjects.size()) {
					List<UndoAction> bpActions = getBPActions(model, idObjects, locationFromMat);
					int numBpImported = bpActions.size();
					if(0 < numBpImported){
						if(!modelHandler.getModel().isUseBindPose()){
							bpActions.add(new BoolAction(modelHandler.getModel()::setUseBindPose, true, "", null));
						}
						modelHandler.getUndoManager().pushAction(new CompoundAction("Import BindPose Rotations", bpActions, null).redo());
//					    String bpImp = "BindPose" + (numBpImported == 1 ? "" : "s") + " Imported";
						String bpImp = "BindPose Imported";
						TwiPopup.quickDismissPopup(this, numBpImported + " " + bpImp + " from " + modelIdentifier, bpImp);
					} else {
						TwiPopup.quickDismissPopup(this, "Could not match and nodes from " + modelIdentifier, "No BindPose Imported");
					}
				} else {
					if (onlySBool){
						TwiPopup.quickDismissPopup(this, "No nodes selected", "No BindPose Imported");
					} else {
//						TwiPopup.quickDismissPopup(this, "Found no editable nodes", "No BindPose Imported");
						TwiPopup.quickDismissPopup(this, "Found no nodes", "No BindPose Imported");
					}
				}
			} else {
				TwiPopup.quickDismissPopup(this, "The " + modelIdentifier + " has no BindPose", "No BindPose Imported");
			}
		}
	}


	private List<UndoAction> getBPActions(EditableModel sourceModel, Collection<IdObject> idObjects, boolean locationFromMat) {
		Map<String, IdObject> nameToNode = new HashMap<>();
		for (IdObject idObject : sourceModel.getIdObjects()){
			nameToNode.put(idObject.getName(), idObject);
		}

		List<UndoAction> actions = new ArrayList<>();
		Vec3 tempLoc = new Vec3();
		for (IdObject idObject : idObjects){
			IdObject source = nameToNode.get(idObject.getName());

			if (source != null) {
				if(locationFromMat){
					tempLoc.setAsLocationFromMat(idObject.getBindPoseM4());
				} else {
					tempLoc.set(idObject.getPivotPoint());
				}
				Mat4 newBP = new Mat4(source.getBindPoseM4()).setLocation(tempLoc);
				UndoAction a = new ConsumerAction<>(idObject::setBindPoseM4, newBP, new Mat4(idObject.getBindPoseM4()), "");
				actions.add(a);
			}
		}
		return actions;

	}


	private List<UndoAction> getBPActions(Collection<IdObject> idObjects, Collection<Camera> cameras, boolean keepRot) {
		List<UndoAction> actions = new ArrayList<>();
		for(IdObject idObject : idObjects){
			Mat4 newBP = (keepRot ? new Mat4(idObject.getBindPoseM4()) : new Mat4()).setLocation(idObject.getPivotPoint());
			UndoAction a = new ConsumerAction<>(idObject::setBindPoseM4, newBP, new Mat4(idObject.getBindPoseM4()), "");
			actions.add(a);
		}
		for (Camera camera : cameras){
			Mat4 newBP = (keepRot ? new Mat4(camera.getBindPoseM4()) : new Mat4()).setLocation(camera.getPosition());
			UndoAction a = new ConsumerAction<>(camera::setBindPoseM4, newBP, new Mat4(camera.getBindPoseM4()), "");
			actions.add(a);
		}
		return actions;
	}


}
