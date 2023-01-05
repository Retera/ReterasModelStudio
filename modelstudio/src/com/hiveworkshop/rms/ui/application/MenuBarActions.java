package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuBarActions {
	static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

	private static void dataSourcesChanged(WarcraftDataSourceChangeListener directoryChangeNotifier, List<ModelPanel> modelPanels) {
//		for (ModelPanel modelPanel : modelPanels) {
//			PerspDisplayPanel pdp = modelPanel.getPerspArea();
//			pdp.reloadAllTextures();
//			modelPanel.getAnimationViewer().reloadAllTextures();
//		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		directoryChangeNotifier.runListeners();
	}

	public static void openHiveViewer() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(BorderLayout.BEFORE_FIRST_LINE, new JLabel(POWERED_BY_HIVE));

		JList<String> view = new JList<>(new String[] {"Bongo Bongo (Phantom Shadow Beast)", "Other Model", "Other Model"});
		view.setCellRenderer(getCellRenderer());
		panel.add(BorderLayout.BEFORE_LINE_BEGINS, new JScrollPane(view));

		JPanel tags = new JPanel();
		tags.setBorder(BorderFactory.createTitledBorder("Tags"));
		tags.setLayout(new GridLayout(30, 1));
		tags.add(new JCheckBox("Results must include all selected tags"));
		tags.add(new JSeparator());
		tags.add(new JLabel("Types (Models)"));
		tags.add(new JSeparator());
		tags.add(new JCheckBox("Building"));
		tags.add(new JCheckBox("Doodad"));
		tags.add(new JCheckBox("Item"));
		tags.add(new JCheckBox("User Interface"));
		panel.add(BorderLayout.CENTER, tags);


		RootWindowUgg rootWindowUgg = ProgramGlobals.getRootWindowUgg();

		ImageIcon icon = new ImageIcon(MainFrame.frame.getIconImage().getScaledInstance(16, 16, Image.SCALE_FAST));
		View hive_browser = new View("Hive Browser", icon, panel);

		rootWindowUgg.setWindow(new SplitWindow(true, 0.75f, rootWindowUgg.getWindow(), hive_browser));
	}

	private static DefaultListCellRenderer getCellRenderer() {
		return new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final JList<?> list,
			                                              final Object value,
			                                              final int index,
			                                              final boolean isSelected,
			                                              final boolean cellHasFocus) {
				Component cellRendererComp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				ImageIcon icon = RMSIcons.loadHiveBrowserImageIcon("deleteme.png");
				ImageIcon scaledIcon = new ImageIcon(icon.getImage().getScaledInstance(48, 32, Image.SCALE_DEFAULT));
				setIcon(scaledIcon);
				return cellRendererComp;
			}
		};
	}
}
