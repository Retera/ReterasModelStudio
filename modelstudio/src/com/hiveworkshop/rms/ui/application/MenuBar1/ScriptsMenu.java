package com.hiveworkshop.rms.ui.application.MenuBar1;


import com.hiveworkshop.rms.ui.application.actionfunctions.*;
import com.hiveworkshop.rms.ui.application.scripts.AnimationTransfer;
import com.hiveworkshop.rms.ui.application.tools.KeyframeCopyPanel;
import com.hiveworkshop.rms.util.ActionMapActions;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.getMenuItem;

public class ScriptsMenu extends JMenu {

	public ScriptsMenu() {
		super("Scripts");
		setMnemonic(KeyEvent.VK_S);
		getAccessibleContext().setAccessibleDescription("Allows the user to execute model edit scripts.");


		add(AnimationTransfer.getMenuItem()); //KeyEvent.VK_I
		add(new ImportGeoset().setMenuItemMnemonic(KeyEvent.VK_G).getMenuItem());
		add(new MergeGeosets().setMenuItemMnemonic(KeyEvent.VK_M).getMenuItem());
		add(new SmoothSelection().setMenuItemMnemonic(KeyEvent.VK_S).getMenuItem());


		add(getMenuItem(ActionMapActions.EDIT_MODEL_COMPS, KeyEvent.VK_C));


		add(new ExportStaticMesh().setMenuItemMnemonic(KeyEvent.VK_X).getMenuItem());
		add(new ExportViewportFrame().setMenuItemMnemonic(KeyEvent.VK_P).getMenuItem());
		add(KeyframeCopyPanel.getMenuItem());//KeyEvent.VK_K
//		add(getMenuItem(ActionMapActions.BACK2BACK_ANIMATION, KeyEvent.VK_B));
		add(new CombineAnimations().setMenuItemMnemonic(KeyEvent.VK_B).getMenuItem());
		add(new ScaleAnimationLength().setMenuItemMnemonic(KeyEvent.VK_A).getMenuItem());
		add(getMenuItem(ActionMapActions.ASSIGN_800, KeyEvent.VK_G));
		add(getMenuItem(ActionMapActions.ASSIGN_1000, KeyEvent.VK_N));
		add(new MakeModelHD().setMenuItemMnemonic(KeyEvent.VK_H).getMenuItem());
		add(new MakeModelSD().setMenuItemMnemonic(KeyEvent.VK_D).getMenuItem());
		add(new RemoveLODs().setMenuItemMnemonic(KeyEvent.VK_L).getMenuItem());
		add(new RecalculateTangents().setMenuItemMnemonic(KeyEvent.VK_T).getMenuItem());


	}

//	public ScriptsMenu() {
//		super("Scripts");
//		setMnemonic(KeyEvent.VK_S);
//		getAccessibleContext().setAccessibleDescription("Allows the user to execute model edit scripts.");
//
////		add(createMenuItem("Oinkerwinkle-Style AnimTransfer", KeyEvent.VK_P, KeyStroke.getKeyStroke("control shift S"), e -> new AnimationTransfer().showWindow()));
////		add(createMenuItem("Import Animation", KeyEvent.VK_P, KeyStroke.getKeyStroke("control shift S"), e -> new AnimationTransfer().showWindow()));
//		add(getMenuItem(ActionMapActions.IMPORT_ANIM, KeyEvent.VK_I));
//
////		FileDialog fileDialog = new FileDialog();
//
////		JMenuItem mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
////		JMenuItem mergeGeoset = new JMenuItem("Import Geoset into Existing Geoset");
////		mergeGeoset.setMnemonic(KeyEvent.VK_M);
////		mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
////		mergeGeoset.addActionListener(e -> ScriptActions.mergeGeosetActionRes());
////		add(mergeGeoset);
//		add(getMenuItem(ActionMapActions.IMP_GEOSET_INTO_GEOSET, KeyEvent.VK_G));
//
////		JMenuItem mergeGeoset2 = new JMenuItem("Twilac-Style Merge Geoset");
////		mergeGeoset2.setMnemonic(KeyEvent.VK_T);
////		mergeGeoset2.setAccelerator(KeyStroke.getKeyStroke("control T"));
////		mergeGeoset2.addActionListener(e -> MergeGeosetsPanel.mergeGeosetActionRes2());
////		mergeGeoset2.addActionListener(ActionMapActions.MERGE_GEOSETS.getAction());
////		add(mergeGeoset2);
//		add(getMenuItem(ActionMapActions.MERGE_GEOSETS, KeyEvent.VK_M));
//
////		JMenuItem smoothVerts = new JMenuItem("Twilac-Style SmoothVerts");
//////		smoothVerts.setMnemonic(KeyEvent.VK_M);
//////		smoothVerts.setAccelerator(KeyStroke.getKeyStroke("control T"));
////		smoothVerts.addActionListener(e -> ScriptActions.smoothSelection());
////		add(smoothVerts);
//		add(getMenuItem(ActionMapActions.SMOOTH_VERTS, KeyEvent.VK_S));
//
////		JMenuItem editModelCompsButton = new JMenuItem("Edit/delete model components");
////		editModelCompsButton.setMnemonic(KeyEvent.VK_E);
////		editModelCompsButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
////		editModelCompsButton.addActionListener(e -> ScriptActions.openImportPanelWithEmpty());
////		add(editModelCompsButton);
//		add(getMenuItem(ActionMapActions.EDIT_MODEL_COMPS, KeyEvent.VK_C));
//
////		add(createMenuItem("Export Animated to Static Mesh", KeyEvent.VK_E, e -> ScriptActions.exportAnimatedToStaticMesh()));
////		add(createMenuItem("Export Animated Frame PNG", KeyEvent.VK_F, e -> fileDialog.exportAnimatedFramePNG()));
////		add(createMenuItem("Copy Keyframes Between Animations", KeyEvent.VK_K, e -> KeyframeCopyPanel.showPanel()));
////		add(createMenuItem("Create Back2Back Animation", KeyEvent.VK_P, e -> ScriptActions.combineAnimations()));
////		add(createMenuItem("Change Animation Lengths by Scaling", KeyEvent.VK_A, e -> ScriptActions.scaleAnimations()));
////		add(createMenuItem("Assign FormatVersion 800", KeyEvent.VK_A, e -> ProgramGlobals.getCurrentModelPanel().getModel().setFormatVersion(800)));
////		add(createMenuItem("Assign FormatVersion 1000", KeyEvent.VK_A, e -> ProgramGlobals.getCurrentModelPanel().getModel().setFormatVersion(1000)));
////		add(createMenuItem("SD -> HD (highly experimental, requires 900 or 1000)", KeyEvent.VK_A, e -> ScriptActions.makeItHD()));
////		add(createMenuItem("HD -> SD (highly experimental, becomes 800)", KeyEvent.VK_A, e -> ScriptActions.convertToV800(1)));
////		add(createMenuItem("Remove LoDs (highly experimental)", KeyEvent.VK_A, e -> ScriptActions.removeLoDs()));
////		add(createMenuItem("Recalculate Tangents (requires 900 or 1000)", KeyEvent.VK_A, e -> MenuBarActions.recalculateTangents()));
//		add(getMenuItem(ActionMapActions.EXPORT_STATIC_MESH, KeyEvent.VK_X));
//		add(getMenuItem(ActionMapActions.EXPORT_ANIMATED_PNG, KeyEvent.VK_P));
//		add(getMenuItem(ActionMapActions.COPY_KFS_BETWEEN_ANIMS, KeyEvent.VK_K));
//		add(getMenuItem(ActionMapActions.BACK2BACK_ANIMATION, KeyEvent.VK_B));
//		add(getMenuItem(ActionMapActions.SCALING_ANIM_LENGTHS, KeyEvent.VK_A));
//		add(getMenuItem(ActionMapActions.ASSIGN_800, KeyEvent.VK_G));
//		add(getMenuItem(ActionMapActions.ASSIGN_1000, KeyEvent.VK_N));
//		add(getMenuItem(ActionMapActions.SD_TO_HD, KeyEvent.VK_D));
//		add(getMenuItem(ActionMapActions.HD_TO_SD, KeyEvent.VK_H));
//		add(getMenuItem(ActionMapActions.REMOVE_LODS, KeyEvent.VK_L));
//		add(getMenuItem(ActionMapActions.RECALC_TANGENTS, KeyEvent.VK_T));
//
////		final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
////		jokebutton.setMnemonic(KeyEvent.VK_A);
////		jokebutton.addActionListener(e -> ScriptActions.jokeButtonClickResponse());
////		scriptsMenu.add(jokebutton);
//	}
}
