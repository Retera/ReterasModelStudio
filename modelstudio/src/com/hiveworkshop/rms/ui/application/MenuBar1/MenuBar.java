package com.hiveworkshop.rms.ui.application.MenuBar1;

import com.hiveworkshop.rms.ui.application.MenuBarActions;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;

import javax.swing.*;

public class MenuBar extends JMenuBar {
	private final FileMenu fileMenu;
	private final ToolsMenu toolsMenu;
	private final WindowsMenu windowMenu;
	private final TeamColorMenu teamColorMenu;
	private WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier directoryChangeNotifier = new WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier();

	public MenuBar() {

		fileMenu = new FileMenu();
		EditMenu editMenu = new EditMenu();
		toolsMenu = new ToolsMenu();
		toolsMenu.setEnabled(false);
		ViewMenu viewMenu = new ViewMenu();
		teamColorMenu = new TeamColorMenu();
		windowMenu = new WindowsMenu();
		AddMenu addMenu = new AddMenu();
		ScriptsMenu scriptsMenu = new ScriptsMenu();
		TwilacsTools twilacsTools = new TwilacsTools();
		AboutMenu aboutMenu = new AboutMenu();

		directoryChangeNotifier.subscribe(MenuBarActions::updateDataSource);

		add(fileMenu);
		add(editMenu);
		add(toolsMenu);

		add(viewMenu);
		add(teamColorMenu);
		add(windowMenu);
		add(addMenu);
		add(scriptsMenu);
		add(twilacsTools);
		add(aboutMenu);

		for (int i = 0; i < getMenuCount(); i++) {
			getMenu(i).getPopupMenu().setLightWeightPopupEnabled(false);
		}
		fileMenu.getRecentMenu().updateRecent();
	}

	public void updateRecent() {
		fileMenu.getRecentMenu().updateRecent();
	}

	public void setToolsMenuEnabled(boolean enabled) {
		toolsMenu.setEnabled(enabled);
	}

	public void removeModelPanel(ModelPanel modelPanel) {
		windowMenu.removeModelPanelItem(modelPanel);
	}

	public void addModelPanel(ModelPanel modelPanel) {
		windowMenu.addModelPanelItem(modelPanel);
	}

	public void updateTeamColors() {
		teamColorMenu.updateTeamColors();
	}
}
