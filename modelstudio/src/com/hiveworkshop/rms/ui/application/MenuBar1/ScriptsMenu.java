package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.FileDialog;
import com.hiveworkshop.rms.ui.application.MenuBarActions;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.ScriptActions;
import com.hiveworkshop.rms.ui.application.scripts.AnimationTransfer;
import com.hiveworkshop.rms.ui.application.tools.KeyframeCopyPanel;
import com.hiveworkshop.rms.ui.application.tools.MergeGeosetsPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class ScriptsMenu extends JMenu {

	public ScriptsMenu() {
		super("Scripts");
		setMnemonic(KeyEvent.VK_S);
		getAccessibleContext().setAccessibleDescription("Allows the user to execute model edit scripts.");
		add(createMenuItem("Oinkerwinkle-Style AnimTransfer", KeyEvent.VK_P, KeyStroke.getKeyStroke("control shift S"), e -> new AnimationTransfer().showWindow()));

		FileDialog fileDialog = new FileDialog();

		JMenuItem mergeGeoset = new JMenuItem("Oinkerwinkle-Style Merge Geoset");
		mergeGeoset.setMnemonic(KeyEvent.VK_M);
		mergeGeoset.setAccelerator(KeyStroke.getKeyStroke("control M"));
		mergeGeoset.addActionListener(e -> ScriptActions.mergeGeosetActionRes());
		add(mergeGeoset);

		JMenuItem mergeGeoset2 = new JMenuItem("Twilac-Style Merge Geoset");
		mergeGeoset2.setMnemonic(KeyEvent.VK_T);
		mergeGeoset2.setAccelerator(KeyStroke.getKeyStroke("control T"));
		mergeGeoset2.addActionListener(e -> MergeGeosetsPanel.mergeGeosetActionRes2());
		add(mergeGeoset2);

		JMenuItem smoothVerts = new JMenuItem("Twilac-Style SmoothVerts");
//		smoothVerts.setMnemonic(KeyEvent.VK_M);
//		smoothVerts.setAccelerator(KeyStroke.getKeyStroke("control T"));
		smoothVerts.addActionListener(e -> ScriptActions.smoothSelection());
		add(smoothVerts);

		JMenuItem nullmodelButton = new JMenuItem("Edit/delete model components");
		nullmodelButton.setMnemonic(KeyEvent.VK_E);
		nullmodelButton.setAccelerator(KeyStroke.getKeyStroke("control E"));
		nullmodelButton.addActionListener(e -> ScriptActions.nullmodelButtonActionRes());
		add(nullmodelButton);

		add(createMenuItem("Export Animated to Static Mesh", KeyEvent.VK_E, e -> ScriptActions.exportAnimatedToStaticMesh()));
		add(createMenuItem("Export Animated Frame PNG", KeyEvent.VK_F, e -> fileDialog.exportAnimatedFramePNG()));
		add(createMenuItem("Copy Keyframes Between Animations", KeyEvent.VK_K, e -> KeyframeCopyPanel.showPanel()));
		add(createMenuItem("Create Back2Back Animation", KeyEvent.VK_P, e -> ScriptActions.combineAnimations()));
		add(createMenuItem("Change Animation Lengths by Scaling", KeyEvent.VK_A, e -> ScriptActions.scaleAnimations()));
		add(createMenuItem("Assign FormatVersion 800", KeyEvent.VK_A, e -> ProgramGlobals.getCurrentModelPanel().getModel().setFormatVersion(800)));
		add(createMenuItem("Assign FormatVersion 1000", KeyEvent.VK_A, e -> ProgramGlobals.getCurrentModelPanel().getModel().setFormatVersion(1000)));
		add(createMenuItem("SD -> HD (highly experimental, requires 900 or 1000)", KeyEvent.VK_A, e -> ScriptActions.makeItHD()));
		add(createMenuItem("HD -> SD (highly experimental, becomes 800)", KeyEvent.VK_A, e -> ScriptActions.convertToV800(1)));
		add(createMenuItem("Remove LoDs (highly experimental)", KeyEvent.VK_A, e -> ScriptActions.removeLoDs()));
		add(createMenuItem("Recalculate Tangents (requires 900 or 1000)", KeyEvent.VK_A, e -> MenuBarActions.recalculateTangents()));

		final JMenuItem jokebutton = new JMenuItem("Load Retera Land");
		jokebutton.setMnemonic(KeyEvent.VK_A);
		jokebutton.addActionListener(e -> ScriptActions.jokeButtonClickResponse());
//		scriptsMenu.add(jokebutton);
	}
}
