package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.actionfunctions.Select;
import com.hiveworkshop.rms.ui.application.actionfunctions.ShowHideStuff;
import com.hiveworkshop.rms.ui.application.actionfunctions.TwilacStuff;
import com.hiveworkshop.rms.ui.application.actionfunctions.WeldVerts;

import javax.swing.*;
import java.awt.event.KeyEvent;

public class TwilacsTools extends JMenu {

	public TwilacsTools(){
		super("Twilac's Beta Tools");
		setMnemonic(KeyEvent.VK_I);
		getAccessibleContext().setAccessibleDescription("Where Twilac puts new features during development before they find a permanent home.");

		add(new WeldVerts().getMenuItem());
		add(TwilacStuff.getSnapCloseVertsMenuItem());
		add(ShowHideStuff.getHideVertsMenuItem());
		add(ShowHideStuff.getShowVertsMenuItem());
		add(new JSeparator());

		add(Select.getSelectNodeGeometryMenuItem());
		add(Select.getSelectLinkedGeometryMenuItem());


		add(new JSeparator());
		add(TwilacStuff.getImportModelPartMenuItem());
		add(TwilacStuff.getImportModelSubAnimMenuItem());
		add(TwilacStuff.getSpliceSubMeshMenuItem());
		add(new JSeparator());
		add(TwilacStuff.getReorderAnimationsMenuItem());
		add(new JSeparator());
		add(TwilacStuff.getLinearizeSelectedMenuItem());
		add(TwilacStuff.getRenameBoneChainMenuItem());
		add(TwilacStuff.getRenameNodesMenuItem());
		add(new JSeparator());
		add(TwilacStuff.getBakeAndRebindToNullMenuItem());
		add(TwilacStuff.getMergeBoneHelpersMenuItem());
	}
}
