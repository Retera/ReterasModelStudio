package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.util.ModelFactory.TempOpenModelStuff;
import com.hiveworkshop.rms.filesystem.GameDataFileSystem;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.viewer.perspective.PerspDisplayPanel;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.ObjectTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableObjectData;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public abstract class BetterSelector extends JSplitPane {
	protected MutableGameObject currentUnit = null;

	protected EditableModel mdl = new EditableModel();
	protected final PerspDisplayPanel perspDisplayPanel;
	protected DefaultMutableTreeNode defaultSelection = null;
	protected final UnitEditorTree tree;

	public BetterSelector(MutableObjectData unitData,
	                      ObjectTabTreeBrowserBuilder treeBuilder,
	                      UnitEditorSettings unitEditorSettings) {
		tree = new UnitEditorTree(unitData, treeBuilder, unitEditorSettings);

		JScrollPane treePane;
		setLeftComponent(treePane = new JScrollPane(tree));
		perspDisplayPanel = new PerspDisplayPanel("blank");
		JPanel rightPanel = getRightPanel();
		setCurrentGameObject(null);

		setRightComponent(rightPanel);

		tree.addTreeSelectionListener(this::valueChanged);
		treePane.setPreferredSize(new Dimension(350, 600));
		perspDisplayPanel.setPreferredSize(new Dimension(800, 600));
		if (defaultSelection != null) {
			tree.getSelectionModel().setSelectionPath(getPath(defaultSelection));
		}
	}

	protected abstract JPanel getRightPanel();

	public void setCurrentGameObject(MutableGameObject gameObject) {
		if (gameObject == null) {
			tree.selectFirstUnit();
			currentUnit = tree.getSelectedGameObject();
		} else {
			currentUnit = gameObject;
		}
		if (currentUnit != null) {
			loadUnitPreview();
		}

	}

	protected abstract void loadUnitPreview();

	protected void openModel(String filepath, String gameObjectName) {
		try {
			filepath = setEndWithMdx(filepath);
			try (InputStream reader = GameDataFileSystem.getDefault().getResourceAsStream(filepath)) {
				mdl = TempOpenModelStuff.createEditableModel(MdxUtils.loadMdlx(reader));
				ModelHandler modelHandler = new ModelHandler(mdl);
				perspDisplayPanel.setModel(modelHandler);
				perspDisplayPanel.setTitle(gameObjectName);
				System.out.println("Opened \"" + filepath + "\"");
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (final Exception exc) {
			exc.printStackTrace();
			System.err.println("Could not open \"" + filepath + "\"");
			// bad model!
			JOptionPane.showMessageDialog(getParent(),
					"The chosen model could not be used.",
					"Program Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	protected String setEndWithMdx(String filepath) {
		if (filepath.endsWith(".mdl")) {
			filepath = filepath.replace(".mdl", ".mdx");
		} else if (!filepath.endsWith(".mdx")) {
			filepath = filepath.concat(".mdx");
		}
		return filepath;
	}


	public void valueChanged(final TreeSelectionEvent e) {
		TreePath newLeadSelectionPath = e.getNewLeadSelectionPath();
		if(newLeadSelectionPath != null){
			final DefaultMutableTreeNode o = (DefaultMutableTreeNode) newLeadSelectionPath.getLastPathComponent();
			if (o.getUserObject() instanceof MutableGameObject) {
				setCurrentGameObject((MutableGameObject) o.getUserObject());
			}
		}
	}

	public static TreePath getPath(TreeNode treeNode) {
		final List<Object> nodes = new ArrayList<>();
		if (treeNode != null) {
			nodes.add(treeNode);
			treeNode = treeNode.getParent();
			while (treeNode != null) {
				nodes.add(0, treeNode);
				treeNode = treeNode.getParent();
			}
		}

		return nodes.isEmpty() ? null : new TreePath(nodes.toArray());
	}

	public MutableGameObject getSelection() {
		return currentUnit;
	}

	public EditableModel getSelectedModel() {
		return mdl;
	}
}
