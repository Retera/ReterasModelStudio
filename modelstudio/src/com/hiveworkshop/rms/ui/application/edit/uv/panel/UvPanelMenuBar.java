package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.actions.mesh.SplitVertexAction;
import com.hiveworkshop.rms.editor.actions.uv.MirrorTVerticesAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.ui.application.actionfunctions.Select;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.uv.TVertexEditorManager;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelPanel;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UvPanelMenuBar extends JMenuBar {
	private ModelHandler modelHandler;
	private TVertexEditorManager tVertexEditorManager;

	private final UVViewport uvViewport;

	public UvPanelMenuBar(UVViewport uvViewport){
		this.uvViewport = uvViewport;
		add(getEditMenu());
		add(getDisplayMenu());
	}

	public UvPanelMenuBar setModel(ModelHandler modelHandler, TVertexEditorManager tVertexEditorManager) {
		this.modelHandler = modelHandler;
		this.tVertexEditorManager = tVertexEditorManager;

		return this;
	}
	public UvPanelMenuBar setModel(ModelPanel modelPanel) {
		if(modelPanel != null) {
			this.modelHandler = modelPanel.getModelHandler();
			this.tVertexEditorManager = modelPanel.getUvModelEditorManager();
		} else {
			this.modelHandler = null;
			this.tVertexEditorManager = null;
		}

		return this;
	}


	private JMenu getEditMenu() {
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.getAccessibleContext().setAccessibleDescription("Allows the user to use various tools to edit the currently selected model's TVertices.");

		editMenu.add(Select.getSelectAll().getAction());
		editMenu.add(Select.getInvertSelection().getAction());
		editMenu.add(Select.getExpandSelection().getAction());

		editMenu.add(getAsMenuItem("Split Vertex", KeyEvent.VK_V, e -> splitVertex()));

		editMenu.add(new JSeparator());

		JMenu mirrorSubmenu = getMirrorSubMenu();
		editMenu.add(mirrorSubmenu);
		return editMenu;
	}

	private JMenu getDisplayMenu() {
		JMenu displayMenu = new JMenu("View");
		displayMenu.setMnemonic(KeyEvent.VK_V);
		displayMenu.getAccessibleContext().setAccessibleDescription("Control display settings for this Texture Coordinate Editor window.");

		JCheckBoxMenuItem wrapImage = new JCheckBoxMenuItem("Wrap Image", false);
		wrapImage.addActionListener(e -> uvViewport.setWrapImage(wrapImage.isSelected()));
		wrapImage.setToolTipText("Repeat the texture many times in a grid-like display. This feature does not edit the model in any way; only this viewing window.");
		displayMenu.add(wrapImage);

		JMenuItem setAspectRatio = getAsMenuItem("Set Aspect Ratio", KeyEvent.VK_S, e -> setAspectRatio());
		setAspectRatio.setAccelerator(KeyStroke.getKeyStroke("control R"));
		setAspectRatio.setToolTipText("Sets the amount by which the texture display is stretched, for editing textures with non-uniform width and height.");
		displayMenu.add(setAspectRatio);
		return displayMenu;
	}

	private JMenu getMirrorSubMenu() {
		JMenu mirrorSubmenu = new JMenu("Mirror");
		mirrorSubmenu.setMnemonic(KeyEvent.VK_M);
		mirrorSubmenu.getAccessibleContext().setAccessibleDescription("Allows the user to mirror objects.");

		mirrorSubmenu.add(getAsMenuItem("Mirror X (selection center)", KeyEvent.VK_X, e -> mirror(Vec2.X_AXIS, null)));
		mirrorSubmenu.add(getAsMenuItem("Mirror Y (selection center)", KeyEvent.VK_Y, e -> mirror(Vec2.Y_AXIS, null)));

		mirrorSubmenu.add(getAsMenuItem("Mirror X (image center)", KeyEvent.VK_U, e -> mirror(Vec2.X_AXIS, new Vec2(.5,.5))));
		mirrorSubmenu.add(getAsMenuItem("Mirror Y (image center)", KeyEvent.VK_V, e -> mirror(Vec2.Y_AXIS, new Vec2(.5,.5))));
		return mirrorSubmenu;
	}

	private JMenuItem getAsMenuItem(String itemText, int keyEvent, ActionListener actionListener) {
		JMenuItem menuItem = new JMenuItem(itemText);
		menuItem.setMnemonic(keyEvent);
		menuItem.addActionListener(actionListener);
		return menuItem;
	}

	private void splitVertex() {
		if (modelHandler != null) {
			UndoAction action = new SplitVertexAction(modelHandler.getModelView().getSelectedVertices(), ModelStructureChangeListener.changeListener);
			modelHandler.getUndoManager().pushAction(action.redo());
		}
//		repaint();
	}

	private void mirror(Vec2 axis, Vec2 center) {
		if (modelHandler != null) {
			if(center == null){
				center = tVertexEditorManager.getSelectionView().getUVCenter();
			}
			Collection<Vec2> tVertices = getTVertices(modelHandler.getModelView().getSelectedVertices(), 0);
			modelHandler.getUndoManager().pushAction(new MirrorTVerticesAction(tVertices, center, axis, ModelStructureChangeListener.changeListener).redo());
		}
//		repaint();
	}

	public static Collection<Vec2> getTVertices(Collection<GeosetVertex> vertexSelection, int uvLayerIndex) {
		List<Vec2> tVertices = new ArrayList<>();
		for (GeosetVertex vertex : vertexSelection) {
			if (uvLayerIndex < vertex.getTverts().size()) {
				tVertices.add(vertex.getTVertex(uvLayerIndex));
			}
		}
		return tVertices;
	}

	private void setAspectRatio() {
		JPanel panel = new JPanel();
		JSpinner widthVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		JSpinner heightVal = new JSpinner(new SpinnerNumberModel(1, 1, 10000, 1));
		JLabel toLabel = new JLabel(" to ");
		panel.add(widthVal);
		panel.add(toLabel);
		panel.add(heightVal);
		JOptionPane.showMessageDialog(this, panel);
		uvViewport.setAspectRatio((Integer) widthVal.getValue() / (double) (Integer) heightVal.getValue());
	}
}
