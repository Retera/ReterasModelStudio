package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.*;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;

import javax.swing.*;
import java.awt.event.KeyEvent;

import static com.hiveworkshop.rms.ui.application.MenuCreationUtils.*;

public class WindowsMenu extends JMenu {

	public WindowsMenu(MainPanel mainPanel) {
		super("Window");
		setMnemonic(KeyEvent.VK_W);
		getAccessibleContext().setAccessibleDescription("Allows the user to open various windows containing the program features.");

		JMenuItem resetViewButton = new JMenuItem("Reset Layout");
		resetViewButton.addActionListener(e -> WindowHandler.resetView(mainPanel));
		add(resetViewButton);

		JMenu viewsMenu = getViewsMenu(mainPanel);
		add(viewsMenu);

		JMenu browsersMenu = createMenu("Browsers", KeyEvent.VK_B);
		add(browsersMenu);

		createAndAddMenuItem("Data Browser", browsersMenu, KeyEvent.VK_A, e -> MPQBrowserView.openMPQViewer(mainPanel));
		createAndAddMenuItem("Unit Browser", browsersMenu, KeyEvent.VK_U, e -> MenuBarActions.openUnitViewer(mainPanel));
		createAndAddMenuItem("Doodad Browser", browsersMenu, KeyEvent.VK_D, e -> InternalFileLoader.OpenDoodadViewer(mainPanel));

		JMenuItem hiveViewer = new JMenuItem("Hive Browser");
		hiveViewer.setMnemonic(KeyEvent.VK_H);
		hiveViewer.addActionListener(e -> MenuBarActions.openHiveViewer(mainPanel));

		addSeparator();
	}

	public void addModelPanelItem(ModelPanel modelPanel) {
		add(modelPanel.getMenuItem());
	}

	public void removeModelPanelItem(ModelPanel modelPanel) {
		remove(modelPanel.getMenuItem());
	}

	private JMenu getViewsMenu(MainPanel mainPanel) {
		JMenu viewsMenu = createMenu("Views", KeyEvent.VK_V);

		viewsMenu.add(createMenuItem("Animation Preview", KeyEvent.VK_A, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Animation Preview", mainPanel.previewView)));
		viewsMenu.add(createMenuItem("Animation Controller", KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Animation Controller", mainPanel.animationControllerView)));
		viewsMenu.add(createMenuItem("Modeling", KeyEvent.VK_M, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Modeling", mainPanel.creatorView)));
		viewsMenu.add(createMenuItem("Outliner", KeyEvent.VK_O, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Outliner", mainPanel.viewportControllerWindowView)));
		viewsMenu.add(createMenuItem("Perspective", KeyEvent.VK_P, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Perspective", mainPanel.perspectiveView)));
		viewsMenu.add(createMenuItem("Front", KeyEvent.VK_F, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Front", mainPanel.frontView)));
		viewsMenu.add(createMenuItem("Side", KeyEvent.VK_S, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Side", mainPanel.leftView)));
		viewsMenu.add(createMenuItem("Bottom", KeyEvent.VK_B, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Bottom", mainPanel.bottomView)));
		viewsMenu.add(createMenuItem("Tools", KeyEvent.VK_T, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Tools", mainPanel.toolView)));
		viewsMenu.add(createMenuItem("Contents", KeyEvent.VK_C, OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Model", mainPanel.modelDataView)));
		viewsMenu.add(createMenuItem("Footer", OpenViewAction.getOpenViewAction(mainPanel.getRootWindow(), "Footer", mainPanel.timeSliderView)));
		viewsMenu.add(createMenuItem("Matrix Eater Script", KeyEvent.VK_H, KeyStroke.getKeyStroke("control P"), e -> ScriptView.openScriptView(mainPanel)));
		return viewsMenu;
	}
}
