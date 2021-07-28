package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.*;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import net.infonode.docking.RootWindow;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenu;
import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.createMenuItem;

public class WindowsMenu extends JMenu {

	public WindowsMenu() {
		super("Window");
		setMnemonic(KeyEvent.VK_W);
		getAccessibleContext().setAccessibleDescription("Allows the user to open various windows containing the program features.");

		JMenuItem resetViewButton = new JMenuItem("Reset Layout");
		resetViewButton.addActionListener(e -> WindowHandler.resetView());
		add(resetViewButton);

		JMenu viewsMenu = getViewsMenu();
		add(viewsMenu);

		JMenu browsersMenu = createMenu("Browsers", KeyEvent.VK_B);
		add(browsersMenu);

		browsersMenu.add(createMenuItem("Data Browser", KeyEvent.VK_A, e -> MPQBrowserView.openMPQViewer()));
		browsersMenu.add(createMenuItem("Unit Browser", KeyEvent.VK_U, e -> MenuBarActions.openUnitViewer()));
		browsersMenu.add(createMenuItem("Doodad Browser", KeyEvent.VK_D, e -> InternalFileLoader.OpenDoodadViewer()));

		JMenuItem hiveViewer = new JMenuItem("Hive Browser");
		hiveViewer.setMnemonic(KeyEvent.VK_H);
		hiveViewer.addActionListener(e -> MenuBarActions.openHiveViewer());

		addSeparator();
	}

	public void addModelPanelItem(ModelPanel modelPanel) {
		add(modelPanel.getMenuItem());
	}

	public void removeModelPanelItem(ModelPanel modelPanel) {
		remove(modelPanel.getMenuItem());
	}

	private JMenu getViewsMenu() {
		MainPanel mainPanel = ProgramGlobals.getMainPanel();
		JMenu viewsMenu = createMenu("Views", KeyEvent.VK_V);

		RootWindow rootWindow = mainPanel.getRootWindow();
		MainLayoutCreator mainLayoutCreator = mainPanel.getMainLayoutCreator();
		viewsMenu.add(createMenuItem("Animation Preview", KeyEvent.VK_A, OpenViewAction.getOpenViewAction(rootWindow, "Animation Preview", mainLayoutCreator.getPreviewView())));
		viewsMenu.add(createMenuItem("Animation Controller", KeyEvent.VK_C, OpenViewAction.getOpenViewAction(rootWindow, "Animation Controller", mainLayoutCreator.getAnimationControllerView())));
		viewsMenu.add(createMenuItem("Modeling", KeyEvent.VK_M, OpenViewAction.getOpenViewAction(rootWindow, "Modeling", mainLayoutCreator.getCreatorView())));
		viewsMenu.add(createMenuItem("Outliner", KeyEvent.VK_O, OpenViewAction.getOpenViewAction(rootWindow, "Outliner", mainLayoutCreator.getModelEditingTreeView())));
		viewsMenu.add(createMenuItem("Perspective", KeyEvent.VK_P, OpenViewAction.getOpenViewAction(rootWindow, "Perspective", mainLayoutCreator.getPerspectiveView())));
		viewsMenu.add(createMenuItem("Front", KeyEvent.VK_F, OpenViewAction.getOpenViewAction(rootWindow, "Front", mainLayoutCreator.getFrontView())));
		viewsMenu.add(createMenuItem("Side", KeyEvent.VK_S, OpenViewAction.getOpenViewAction(rootWindow, "Side", mainLayoutCreator.getLeftView())));
		viewsMenu.add(createMenuItem("Bottom", KeyEvent.VK_B, OpenViewAction.getOpenViewAction(rootWindow, "Bottom", mainLayoutCreator.getBottomView())));
		viewsMenu.add(createMenuItem("Tools", KeyEvent.VK_T, OpenViewAction.getOpenViewAction(rootWindow, "Tools", mainLayoutCreator.getToolView())));
		viewsMenu.add(createMenuItem("Contents", KeyEvent.VK_C, OpenViewAction.getOpenViewAction(rootWindow, "Model", mainLayoutCreator.getModelDataView())));
		viewsMenu.add(createMenuItem("Footer", -1, OpenViewAction.getOpenViewAction(rootWindow, "Footer", mainLayoutCreator.getTimeSliderView())));
		viewsMenu.add(createMenuItem("Matrix Eater Script", KeyEvent.VK_H, KeyStroke.getKeyStroke("control P"), e -> ScriptView.openScriptView()));
		return viewsMenu;
	}
}
