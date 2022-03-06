package com.hiveworkshop.rms.ui.application;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.model.material.AddMaterialAction;
import com.hiveworkshop.rms.editor.actions.model.material.SetMaterialShaderStringAction;
import com.hiveworkshop.rms.editor.actions.nodes.AddNodeAction;
import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.filesystem.sources.DataSourceDescriptor;
import com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.ui.gui.modeledit.ProgramPreferencesPanel;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.ui.preferences.SaveProfile;
import com.hiveworkshop.rms.ui.preferences.listeners.WarcraftDataSourceChangeListener;
import com.hiveworkshop.rms.util.FramePopup;
import net.infonode.docking.SplitWindow;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MenuBarActions {
	static final ImageIcon POWERED_BY_HIVE = RMSIcons.loadHiveBrowserImageIcon("powered_by_hive.png");

	private static void dataSourcesChanged(WarcraftDataSourceChangeListener.WarcraftDataSourceChangeNotifier directoryChangeNotifier, List<ModelPanel> modelPanels) {
//		for (ModelPanel modelPanel : modelPanels) {
//			PerspDisplayPanel pdp = modelPanel.getPerspArea();
//			pdp.reloadAllTextures();
//			modelPanel.getAnimationViewer().reloadAllTextures();
//		}
		ProgramGlobals.getRootWindowUgg().getWindowHandler2().reloadThings();
		directoryChangeNotifier.dataSourcesChanged();
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
				ImageIcon icon = new ImageIcon(MainPanel.class.getResource("ImageBin/deleteme.png"));
				ImageIcon scaledIcon = new ImageIcon(icon.getImage().getScaledInstance(48, 32, Image.SCALE_DEFAULT));
				setIcon(scaledIcon);
				return cellRendererComp;
			}
		};
	}

	public static void openPreferences() {
		ProgramPreferences programPreferences = new ProgramPreferences();
		programPreferences.loadFrom(ProgramGlobals.getPrefs());
		List<DataSourceDescriptor> priorDataSources = SaveProfile.get().getDataSources();
		ProgramPreferencesPanel programPreferencesPanel = new ProgramPreferencesPanel(programPreferences, priorDataSources);
		JPanel prefPanel = new JPanel(new MigLayout("fill"));
		prefPanel.add(programPreferencesPanel, "growx, growy, spanx, wrap");


		JButton okButton = new JButton("OK");
		prefPanel.add(okButton);

		JButton cancelButton = new JButton("Cancel");
		prefPanel.add(cancelButton);

		JFrame frame = FramePopup.get(prefPanel, ProgramGlobals.getMainPanel(), "Preferences");
		okButton.addActionListener(e -> {
			saveSettings(programPreferences, priorDataSources, programPreferencesPanel);
			frame.setVisible(false);
			frame.dispose();
		});
		cancelButton.addActionListener(e -> {frame.setVisible(false);frame.dispose();});

		frame.setVisible(true);

//		int ret = JOptionPane.showConfirmDialog(
//				ProgramGlobals.getMainPanel(),
//				programPreferencesPanel,
//				"Preferences",
//				JOptionPane.OK_CANCEL_OPTION,
//				JOptionPane.PLAIN_MESSAGE);
//
//		if (ret == JOptionPane.OK_OPTION) {
//			saveSettings(programPreferences, priorDataSources, programPreferencesPanel);
//		}
	}

	private static void saveSettings(ProgramPreferences programPreferences, List<DataSourceDescriptor> priorDataSources, ProgramPreferencesPanel programPreferencesPanel) {
		ProgramGlobals.getEditorColorPrefs().setFrom(programPreferencesPanel.getColorPrefs());
		programPreferences.setEditorColors(ProgramGlobals.getEditorColorPrefs());
		ProgramGlobals.getPrefs().loadFrom(programPreferences);
		List<DataSourceDescriptor> dataSources = programPreferencesPanel.getDataSources();
		boolean changedDataSources = (dataSources != null) && !dataSources.equals(priorDataSources);
		if (changedDataSources) {
			SaveProfile.get().setDataSources(dataSources);
		}
		SaveProfile.save();
		if (changedDataSources) {
			MenuBar.updateDataSource();
			ProgramGlobals.getRootWindowUgg().getWindowHandler2().dataSourcesChanged();
//				dataSourcesChanged(MenuBar.directoryChangeNotifier, mainPanel.modelPanels);
		}
	}


	public static void clearRecent() {
		int dialogResult = JOptionPane.showConfirmDialog(ProgramGlobals.getMainPanel(),
				"Are you sure you want to clear the Recent history?", "Confirm Clear",
				JOptionPane.YES_NO_OPTION);
		if (dialogResult == JOptionPane.YES_OPTION) {
			SaveProfile.get().clearRecent();
			com.hiveworkshop.rms.ui.application.MenuBar1.MenuBar.updateRecent();
		}
	}

	public static boolean closeOthers() {
		boolean success = true;
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		ModelPanel lastUnclosedModelPanel = null;
		for (int i = modelPanels.size() - 1; i > 0; i--) {
			ModelPanel panel = modelPanels.get(i);
			if (panel.close()) {
				MenuBar.removeModelPanel(panel);
				ProgramGlobals.removeModelPanel(panel);
			} else {
				lastUnclosedModelPanel = panel;
				break;
			}
		}
		if (ProgramGlobals.getCurrentModelPanel() == null && lastUnclosedModelPanel != null) {
			ModelLoader.setCurrentModel(lastUnclosedModelPanel);
		}
		return success;
	}

	public static void addAttachment() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			Attachment attachment = new Attachment("New Attatchment");
			modelPanel.getModelHandler().getUndoManager().pushAction(new AddNodeAction(current, attachment, ModelStructureChangeListener.changeListener).redo());
		}
	}

	public static void addLight() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			Light light = new Light("New Light");
			modelPanel.getModelHandler().getUndoManager().pushAction(new AddNodeAction(current, light, ModelStructureChangeListener.changeListener).redo());
		}
	}

	public static void addCollision() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel current = modelPanel.getModel();
			CollisionShape collisionShape = new CollisionShape();
			collisionShape.setName("New CollisionShape");
			modelPanel.getModelHandler().getUndoManager().pushAction(new AddNodeAction(current, collisionShape, ModelStructureChangeListener.changeListener).redo());
		}
	}

	public static void addNewMaterial() {
		ModelPanel modelPanel = ProgramGlobals.getCurrentModelPanel();
		if (modelPanel != null) {
			EditableModel model = modelPanel.getModel();
			Bitmap texture;
			if (model.getTextures().isEmpty()) {
				String path = model.getFormatVersion() == 1000 ? "Textures\\White.dds" : "Textures\\White.blp";
				texture = new Bitmap(path);
			} else {
				texture = model.getTexture(0);
			}

			Material material = new Material(new Layer(texture));

			if (model.getFormatVersion() == 1000) {
				new SetMaterialShaderStringAction(model, material, "Shader_HD_DefaultUnit", null).redo();
			}
			UndoAction action = new AddMaterialAction(material, model, ModelStructureChangeListener.changeListener);
			modelPanel.getModelHandler().getUndoManager().pushAction(action.redo());
		}
	}
}
