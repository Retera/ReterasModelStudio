package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.FixFacesAction;
import com.hiveworkshop.rms.editor.actions.tools.MergeSkinWeightsAction;
import com.hiveworkshop.rms.editor.actions.util.BoolAction;
import com.hiveworkshop.rms.editor.actions.util.CompoundAction;
import com.hiveworkshop.rms.editor.actions.util.ConsumerAction;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Camera;
import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.application.tools.SkinningOptionPanel;
import com.hiveworkshop.rms.ui.application.tools.shadereditors.ShaderEditorType;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Mat4;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;

public class TwilacsTools extends JMenu {

	public TwilacsTools(){
		super("Twilac's Beta Tools");
		setMnemonic(KeyEvent.VK_I);
		getAccessibleContext().setAccessibleDescription("Where Twilac puts new features during development before they find a permanent home.");
//		add(getBakeMenu());
		add(getSkinningMenu());
		add(getFixFacesMenu());
		add(getBindPoseMenu());
		add(getMergeWeightsMenu());

		add(new WeldVerts().getMenuItem());
		add(new SplitVertices().getMenuItem());
		add(new FixUnboundVerts().getMenuItem());
		add(TwilacStuff.getSnapCloseVertsMenuItem());
		add(ShowHideStuff.getHideVertsMenuItem());
		add(ShowHideStuff.getShowVertsMenuItem());
		add(TwilacStuff.getBridgeEdgesMenuItem());
		add(TwilacStuff.getTPoseStuffMenuItem());

		JMenu shaderEditorMenu = createMenu("Shader Editors", KeyEvent.VK_S);
		add(shaderEditorMenu);
		shaderEditorMenu.add(TwilacStuff.getShaderEditorMenuItem(ShaderEditorType.MESH));
		shaderEditorMenu.add(TwilacStuff.getShaderEditorMenuItem(ShaderEditorType.BONE));
		shaderEditorMenu.add(TwilacStuff.getShaderEditorMenuItem(ShaderEditorType.PARTICLE2));
		shaderEditorMenu.add(TwilacStuff.getShaderEditorMenuItem(ShaderEditorType.COLLISION));
		shaderEditorMenu.add(TwilacStuff.getShaderEditorMenuItem(ShaderEditorType.GRID));

//		add(TwilacStuff.getSelectEdgeMenuItem());
//		add(new JSeparator());
//
//		add(Select.getSelectNodeGeometryMenuItem());
//		add(Select.getSelectLinkedGeometryMenuItem());


		add(new JSeparator());
		add(TwilacStuff.getImportModelPartMenuItem());
		add(TwilacStuff.getImportModelSubAnimMenuItem());
		add(TwilacStuff.getSpliceSubMeshMenuItem());
		add(TwilacStuff.getSpliceGeosetMenuItem());
		add(new JSeparator());
		add(new ReorderAnimations().getMenuItem());
		add(new JSeparator());
		add(TwilacStuff.getLinearizeSelectedMenuItem());
		add(TwilacStuff.getRenameBoneChainMenuItem());
		add(TwilacStuff.getRenameNodesMenuItem());
		add(TwilacStuff.getRenameAnimationsMenuItem());
		add(new JSeparator());
		add(TwilacStuff.getDupeForAnimStuffMenuItem());
		add(TwilacStuff.getGlobalTransfStuffMenuItem());
		add(TwilacStuff.getBakeAndRebindToNullMenuItem());
		add(new MergeBonesWithHelpers().getMenuItem());
		add(TwilacStuff.getAddNewAttatchment());
		add(TwilacStuff.getExportUVMaskMenuItem());
		add(TwilacStuff.getTextureCompositionMenuItem());
	}



	private JMenuItem getBindPoseMenu(){
		JMenuItem menuItem = new JMenuItem("add BindPoses");
		menuItem.addActionListener(e -> {
			ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
			if(currentModelPanel != null){
				ModelHandler modelHandler = currentModelPanel.getModelHandler();
				EditableModel model = modelHandler.getModel();
				List<UndoAction> actions = new ArrayList<>();
				for(IdObject idObject : model.getIdObjects()){
					UndoAction a = new ConsumerAction<>(m -> idObject.setBindPoseM4(m), new Mat4().translate(idObject.getPivotPoint()), idObject.getBindPoseM4(), "");
					actions.add(a);
				}
				for (Camera camera : model.getCameras()){
					UndoAction a = new ConsumerAction<>(m -> camera.setBindPoseM4(m), new Mat4().translate(camera.getPosition()), camera.getBindPoseM4(), "");
					actions.add(a);
				}
				if(!model.isUseBindPose()){
					actions.add(new BoolAction(b -> model.setUseBindPose(b), true, "", null));
				}
				modelHandler.getUndoManager().pushAction(new CompoundAction("Add BindPoses", actions, null).redo());
			}
		});
		return menuItem;
	}
	private JMenuItem getSkinningMenu(){
		JMenuItem menuItem = new JMenuItem("Skinning options");
		menuItem.addActionListener(e -> SkinningOptionPanel.showPanel(null, ProgramGlobals.getCurrentModelPanel().getModelHandler()));
		return menuItem;
	}
	private JMenuItem getBakeMenu(){
		JMenuItem menuItem = new JMenuItem("Bake texture test");
//		menuItem.addActionListener(e -> ReteraBakeMaterial.drawStuff(ProgramGlobals.getCurrentModelPanel().getModelHandler()));
		return menuItem;
	}

	private JMenuItem getFixFacesMenu(){
		JMenuItem menuItem = new JMenuItem("Fix Faces");
		menuItem.addActionListener(e -> {
			ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
			ModelView modelView = currentModelPanel.getModelView();
			if(!modelView.isEmpty()){
				currentModelPanel.getModelHandler().getUndoManager().pushAction(new FixFacesAction(modelView.getSelectedTriangles()).redo());
			}
		});
		return menuItem;
	}

	private JMenuItem getMergeWeightsMenu(){
		JMenuItem menuItem = new JMenuItem("Merge Weights");
		menuItem.addActionListener(e -> {
			ModelPanel currentModelPanel = ProgramGlobals.getCurrentModelPanel();
			ModelView modelView = currentModelPanel.getModelView();
			if(!modelView.isEmpty() && !modelView.getModel().getBones().isEmpty()){
				Bone boneForZero = modelView.getModel().getBones().get(0);
				currentModelPanel.getModelHandler().getUndoManager().pushAction(new MergeSkinWeightsAction(modelView.getSelectedVertices(), boneForZero, true).redo());
			}
		});
		return menuItem;
	}
}
