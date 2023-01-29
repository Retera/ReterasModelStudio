package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.editor.actions.mesh.FixFacesAction;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.application.tools.SkinningOptionPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;

public class TwilacsTools extends JMenu {

	public TwilacsTools(){
		super("Twilac's Beta Tools");
		setMnemonic(KeyEvent.VK_I);
		getAccessibleContext().setAccessibleDescription("Where Twilac puts new features during development before they find a permanent home.");
		add(getSkinningMenu());
		add(getFixFacesMenu());

		add(new WeldVerts().getMenuItem());
		add(new SplitVertices().getMenuItem());
		add(new FixUnboundVerts().getMenuItem());
		add(TwilacStuff.getSnapCloseVertsMenuItem());
		add(ShowHideStuff.getHideVertsMenuItem());
		add(ShowHideStuff.getShowVertsMenuItem());
		add(TwilacStuff.getBridgeEdgesMenuItem());

		JMenu shaderEditorMenu = createMenu("Shader Editors", KeyEvent.VK_S);
		add(shaderEditorMenu);
		shaderEditorMenu.add(TwilacStuff.getTestShaderStuffMenuItem());
		shaderEditorMenu.add(TwilacStuff.getTextShaderStuffNodeMenuItem());
		shaderEditorMenu.add(TwilacStuff.getTextShaderStuffColMenuItem());
		shaderEditorMenu.add(TwilacStuff.getTextShaderStuffGridMenuItem());

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

	private JMenuItem getSkinningMenu(){
		JMenuItem menuItem = new JMenuItem("Skinning options");
		menuItem.addActionListener(e -> SkinningOptionPanel.showPanel(null, ProgramGlobals.getCurrentModelPanel().getModelHandler()));
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
}
