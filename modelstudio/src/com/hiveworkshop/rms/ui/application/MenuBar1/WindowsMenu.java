package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.*;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.DisplayViewCanvas;
import com.hiveworkshop.rms.ui.application.viewer.CameraPreviewView;
import com.hiveworkshop.rms.ui.application.viewer.EditUVsView;
import com.hiveworkshop.rms.ui.application.viewer.PreviewViewCanv;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.DoodadBrowserView;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.ObjectEditorFrame;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitBrowserView;
import com.hiveworkshop.rms.ui.browsers.mpq.MPQImageBrowser;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.ModelingCreatorToolsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelcomponenttree.ModelComponentsView;
import com.hiveworkshop.rms.ui.gui.modeledit.modelviewtree.ModelViewManagingView;
import com.hiveworkshop.rms.util.ModelDependentView;
import net.infonode.docking.RootWindow;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class WindowsMenu extends JMenu {

	private final Map<ModelPanel, JMenuItem> modelMenuItemMap = new HashMap<>();

	public WindowsMenu() {
		super("Window");
		setMnemonic(KeyEvent.VK_W);
		getAccessibleContext().setAccessibleDescription("Allows the user to open various windows containing the program features.");

		JMenuItem resetViewButton = new JMenuItem("Reset Layout");
		resetViewButton.addActionListener(e -> ProgramGlobals.getRootWindowUgg().resetView());
		add(resetViewButton);

//		JMenuItem saveViewButton = new JMenuItem("Save Layout");
////		resetViewButton.addActionListener(e -> WindowHandler.resetView());
//		saveViewButton.addActionListener(e -> ProgramGlobals.getPrefs().saveViewMap());
//		add(saveViewButton);
		add(createMenuItem("Preview of current model", KeyEvent.VK_P, e -> FloatingWindowFactory.openNewWindowWithKB(new PreviewViewCanv().setModelPanel(ProgramGlobals.getCurrentModelPanel()), ProgramGlobals.getRootWindowUgg())));

		JMenu viewsMenu = getViewsMenu();
		add(viewsMenu);

		JMenu browsersMenu = createMenu("Browsers", KeyEvent.VK_B);
		add(browsersMenu);

		browsersMenu.add(createMenuItem("Data Browser", KeyEvent.VK_A, e -> openViewer(new MPQBrowserView())));
		browsersMenu.add(createMenuItem("Unit Browser", KeyEvent.VK_U, e -> openViewer(new UnitBrowserView())));
		browsersMenu.add(createMenuItem("Doodad Browser", KeyEvent.VK_D, e -> openViewer(new DoodadBrowserView())));
		browsersMenu.add(createMenuItem("Image Browser", KeyEvent.VK_I, e -> MPQImageBrowser.showPanel()));

		JMenuItem hiveViewer = new JMenuItem("Hive Browser");
		hiveViewer.setMnemonic(KeyEvent.VK_H);
		hiveViewer.addActionListener(e -> MenuBarActions.openHiveViewer());


		add(createMenuItem("Object Editor", KeyEvent.VK_D, e -> ObjectEditorFrame.showObjectEditor()));
		addSeparator();
	}

	public void addModelPanelItem(ModelPanel modelPanel) {
		JMenuItem menuItem = new JMenuItem(modelPanel.getModel().getName());
		if(modelPanel.getModel().getFile() != null){
			menuItem.setToolTipText(modelPanel.getModel().getFile().getPath());
		}
		menuItem.setIcon(modelPanel.getIcon());
		menuItem.addActionListener(e -> ModelLoader.setCurrentModel(modelPanel));
		modelPanel.setJMenuItem(menuItem);
		modelMenuItemMap.put(modelPanel, menuItem);
		add(menuItem);
	}

	public void removeModelPanelItem(ModelPanel modelPanel) {
		remove(modelMenuItemMap.get(modelPanel));
		modelMenuItemMap.remove(modelPanel);
	}

	public static void openViewer(View view) {
//		RootWindow rootWindow = ProgramGlobals.getMainPanel().getRootWindow();
		RootWindow rootWindow = ProgramGlobals.getRootWindowUgg();
		rootWindow.setWindow(new SplitWindow(true, 0.75f, rootWindow.getWindow(), view));
	}

	private JMenu getViewsMenu() {
		JMenu viewsMenu = createMenu("Views", KeyEvent.VK_V);

		viewsMenu.add(createMenuItem("Animation Preview", KeyEvent.VK_A, e -> openModelDependentView(new PreviewViewCanv())));
		viewsMenu.add(createMenuItem("Camera Preview", KeyEvent.VK_C, e -> openModelDependentView(new CameraPreviewView())));
		viewsMenu.add(createMenuItem("Modeling", KeyEvent.VK_M, e -> openModelDependentView(new ModelingCreatorToolsView())));
		viewsMenu.add(createMenuItem("Outliner", KeyEvent.VK_O, e -> openModelDependentView(new ModelViewManagingView())));
		viewsMenu.add(createMenuItem("Perspective", KeyEvent.VK_P, e -> openModelDependentView(new DisplayViewCanvas("Perspective", false, false))));
		viewsMenu.add(createMenuItem("Front", KeyEvent.VK_F, e -> openModelDependentView(new DisplayViewCanvas("Front", true, true))));
		viewsMenu.add(createMenuItem("Side", KeyEvent.VK_S, e -> openModelDependentView(new DisplayViewCanvas("Side", true, true))));
		viewsMenu.add(createMenuItem("Bottom", KeyEvent.VK_B, e -> openModelDependentView(new DisplayViewCanvas("Bottom", true, true))));

		viewsMenu.add(createMenuItem("Edit UV's", KeyEvent.VK_U, e -> openEditUVsView(ProgramGlobals.getRootWindowUgg())));

		viewsMenu.add(createMenuItem("Contents", KeyEvent.VK_T, e -> ProgramGlobals.getRootWindowUgg().newWindow(new ModelComponentsView().setModelPanel(ProgramGlobals.getCurrentModelPanel()))));
		viewsMenu.add(createMenuItem("Footer", KeyEvent.VK_F, e -> FloatingWindowFactory.openNewWindowWithKB(ProgramGlobals.getRootWindowUgg().getWindowHandler2().getTimeSliderView(), ProgramGlobals.getRootWindowUgg())));

		viewsMenu.add(createMenuItem("Matrix Eater Script", KeyEvent.VK_H, KeyStroke.getKeyStroke("control P"), e -> ScriptView.openScriptView()));
		return viewsMenu;
	}

	private void openModelDependentView(ModelDependentView mdv){
		RootWindowUgg rootWindow = ProgramGlobals.getRootWindowUgg();

		rootWindow.newWindow(mdv.setModelPanel(ProgramGlobals.getCurrentModelPanel()));
	}


	private void openEditUVsView(RootWindowUgg rootWindow) {
		ModelDependentView view = new EditUVsView();
		System.out.println("sat model panel, opening window");
		rootWindow.newWindow(view);
		view.setModelPanel(ProgramGlobals.getCurrentModelPanel());
		System.out.println("done opening window");
	}
}
