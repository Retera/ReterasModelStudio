package com.hiveworkshop.rms.ui.browsers.jworldedit.models;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.parsers.mdlx.util.MdxUtils;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportPanel;
import com.hiveworkshop.rms.ui.application.model.nodepanels.AnimationChooser;
import com.hiveworkshop.rms.ui.application.viewer.ViewportHelpers;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.ObjectTabTreeBrowserBuilder;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.UnitEditorTree;
import com.hiveworkshop.rms.ui.browsers.jworldedit.objects.datamodel.MutableGameObject;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.War3ID;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public abstract class BetterSelector extends JSplitPane {
	protected MutableGameObject currentUnit = null;

	protected EditableModel mdl = new EditableModel();
	protected final ViewportPanel viewportPanel;
	protected DefaultMutableTreeNode defaultSelection = null;
	protected final UnitEditorTree tree;

	protected War3ID field;
	protected War3ID variationField;
	protected Consumer<String> setTitle;
	protected AnimationChooser animationChooser;

	public BetterSelector(ObjectTabTreeBrowserBuilder treeBuilder,
	                      UnitEditorSettings unitEditorSettings,
	                      War3ID field,
	                      War3ID variationField) {
		tree = new UnitEditorTree(treeBuilder, unitEditorSettings);

		this.variationField = variationField;
		this.field = field;

		JScrollPane treePane;
		setLeftComponent(treePane = new JScrollPane(tree));
		viewportPanel = new ViewportPanel(false, false);
		animationChooser = new AnimationChooser(true, false, true);
		JPanel rightPanel = getRightPanel();
		setTitle = title -> rightPanel.setBorder(BorderFactory.createTitledBorder(title));
		rightPanel.setBorder(BorderFactory.createTitledBorder(""));
		setCurrentGameObject(null);

		setRightComponent(rightPanel);

		tree.addTreeSelectionListener(this::valueChanged);
		treePane.setPreferredSize(new Dimension(350, 600));
		viewportPanel.setPreferredSize(new Dimension(800, 600));
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

	boolean tempLoadDefault = true;
	protected void openModel(String filepath, String gameObjectName) {
		try {
			filepath = setEndWithMdx(filepath);
			mdl = MdxUtils.loadEditable(filepath, null);
			RenderModel previewRenderModel = new ModelHandler(mdl).getPreviewRenderModel();
			animationChooser.setModel(mdl, previewRenderModel).chooseSequence(ViewportHelpers.findDefaultAnimation(mdl));
			viewportPanel.setModel(previewRenderModel, null, tempLoadDefault);
			tempLoadDefault = false;
			setTitle.accept(gameObjectName);
			System.out.println("Opened \"" + filepath + "\"");
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
		if (newLeadSelectionPath != null
				&& newLeadSelectionPath.getLastPathComponent() instanceof DefaultMutableTreeNode treeNode
				&& treeNode.getUserObject() instanceof MutableGameObject gameObject) {
			setCurrentGameObject(gameObject);
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

	public abstract String getCurrentFilePath();

	protected String getFilePath(MutableGameObject obj, int variant) {
		int numberOfVariations;
		if (variationField == null) {
			numberOfVariations = 0;
		} else {
			numberOfVariations = obj.getFieldAsInteger(variationField, 0);
		}


		if (1 < numberOfVariations) {
			return obj.getFieldAsString(field, 0) + variant + ".mdl";
		} else {
			return obj.getFieldAsString(field, 0);
		}
	}
}
