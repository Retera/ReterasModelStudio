package com.hiveworkshop.rms.ui.browsers.mpq;

import com.hiveworkshop.rms.editor.actions.model.bitmap.AddBitmapAction;
import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.ui.application.ExportInternal;
import com.hiveworkshop.rms.ui.application.ModelLoader;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.TwiComboBox;
import com.hiveworkshop.rms.util.TwiTreeStuff.TwiTreeMouseAdapter;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public final class MPQImageMouseAdapter extends TwiTreeMouseAdapter {
	private final MPQFilterableBrowser mpqBrowser;
	private final JPopupMenu contextMenu;
	private TreePath clickedPath;
	JComponent popupParent;

	MPQImageMouseAdapter(MPQFilterableBrowser mpqBrowser, Consumer<Boolean> controlDown, JComponent popupParent) {
		super(controlDown);
		this.mpqBrowser = mpqBrowser;
		this.contextMenu = getContextMenu();
		this.popupParent = popupParent;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
		clickedPath = mpqBrowser.getPathForLocation(e.getX(), e.getY());
		if (SwingUtilities.isRightMouseButton(e)) {
			contextMenu.show(popupParent, e.getX(), e.getY());
		} else {
			TreePath treePath = mpqBrowser.getPathForLocation(e.getX(), e.getY());
			mpqBrowser.openTreePath(treePath);
		}
	}

	public String getClickedPath() {
		return ((MPQTreeNode) clickedPath.getLastPathComponent()).getPath();
	}

	private JPopupMenu getContextMenu() {
		JPopupMenu contextMenu = new JPopupMenu();

//		contextMenu.add(getMenuItem("Open", e -> loadFileByType(getClickedPath())));
		contextMenu.add(getMenuItem("Open", e -> ModelLoader.loadFile(GameDataFileSystem.getDefault().getFile(getClickedPath()), true)));
		contextMenu.add(getMenuItem("Export", e -> ExportInternal.exportInternalFile(getClickedPath(), "Texture", mpqBrowser)));
		contextMenu.addSeparator();
		contextMenu.add(getMenuItem("Copy Path to Clipboard", e -> copyItemPathToClipboard(getClickedPath())));
		contextMenu.add(getMenuItem("Use as Texture", e -> addTextureToCurrentModel(getClickedPath())));
		return contextMenu;
	}

	private JMenuItem getMenuItem(String text, ActionListener actionListener) {
		JMenuItem openItem = new JMenuItem(text);
		openItem.addActionListener(actionListener);
		return openItem;
	}

	private void copyItemPathToClipboard(String filepath) {
		StringSelection selection = new StringSelection(filepath);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(selection, selection);
	}


	private void addTextureToCurrentModel(String path) {
		int modIndex = Math.max(path.lastIndexOf(".w3mod/"), path.lastIndexOf(".w3mod\\"));
		String finalPath;
		if (modIndex == -1) {
			finalPath = path;
		} else {
			finalPath = path.substring(modIndex + ".w3mod/".length());
		}

		JPanel panel = new JPanel(new MigLayout());
		panel.add(new JLabel("Choose model to receive texture"), "wrap");

		Map<Integer, ModelPanel> models = new HashMap<>();
		List<ModelPanel> modelPanels = ProgramGlobals.getModelPanels();
		for (ModelPanel m : modelPanels) {
			models.put(models.size(), m);
		}

		TwiComboBox<String> modelsBox = getModelComboBox(models);

		panel.add(modelsBox);

		int option = JOptionPane.showConfirmDialog(mpqBrowser, panel, "Choose model", JOptionPane.OK_CANCEL_OPTION);

		if (option == JOptionPane.OK_OPTION) {
			ModelPanel modelPanel = models.get(modelsBox.getSelectedIndex());
			if (modelPanel != null) {
				if (modelPanel.getModel().getFormatVersion() > 800) {
					finalPath = finalPath.replace("\\", "/"); // Reforged prefers forward slash
				}
				modelPanel.getModelHandler()
						.getUndoManager()
						.pushAction(new AddBitmapAction(new Bitmap(finalPath), modelPanel.getModel(),
								ModelStructureChangeListener.changeListener).redo());
			}
		}
	}

	private TwiComboBox<String> getModelComboBox(Map<Integer, ModelPanel> models) {
		String[] names = new String[models.size()];
		int currentModelPanel = 0;
		for (Integer i : models.keySet()) {
			ModelPanel m = models.get(i);
			names[i] = m.getModel().getName();
			if (m == ProgramGlobals.getCurrentModelPanel()) {
				currentModelPanel = i;
				names[i] += " (current)";
			}
		}

		TwiComboBox<String> modelsBox = new TwiComboBox<>(names, "Prototype Prototype");
		modelsBox.addMouseWheelListener(e -> modelsBox.incIndex(e.getWheelRotation()));
		if (currentModelPanel < names.length) {
			modelsBox.setSelectedIndex(currentModelPanel);
		}
		return modelsBox;
	}
}
