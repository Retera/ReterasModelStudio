package com.hiveworkshop.rms.ui.browsers.jworldedit.triggers;

import com.hiveworkshop.rms.ui.browsers.jworldedit.WorldEditArt;
import com.hiveworkshop.rms.ui.browsers.jworldedit.WorldEditorSettings;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.gui.TriggerElementTreeNode;
import com.hiveworkshop.rms.ui.browsers.jworldedit.triggers.impl.TriggerEnvironment;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.*;

public class TriggerTreeCellEditor extends DefaultTreeCellEditor {
	private final WorldEditorSettings settings;
	private final Color defaultBackgroundSelectionColor = null;
	private final WorldEditArt worldEditArt;
	private JTree tree;
	private final TriggerEnvironment triggerEnvironment;

	public TriggerTreeCellEditor(final JTree tree, final DefaultTreeCellRenderer renderer,
								 final WorldEditorSettings settings, final WorldEditArt worldEditArt,
								 final TriggerEnvironment triggerEnvironment) {
		super(tree, renderer, new TriggerTreeCellSubEditor(triggerEnvironment));
		this.settings = settings;
		this.worldEditArt = worldEditArt;
		this.triggerEnvironment = triggerEnvironment;
	}

	@Override
	public Component getTreeCellEditorComponent(final JTree tree, final Object value, final boolean isSelected,
												final boolean expanded, final boolean leaf, final int row) {
		this.tree = tree;
		return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
	}

	@Override
	public Object getCellEditorValue() {
		if (tree.getSelectionCount() == 1) {
			final TreePath selectionPath = tree.getSelectionPath();
			final Object lastPathComponent = selectionPath.getLastPathComponent();
//			if (lastPathComponent instanceof TriggerTreeNode) {
//				final TriggerTreeNode triggerTreeNode = (TriggerTreeNode) lastPathComponent;
//				triggerTreeNode.getTrigger().setName(super.getCellEditorValue().toString());
//				triggerTreeNode.setUserObject(super.getCellEditorValue().toString());
//				return triggerTreeNode.getTrigger().getName();
//			} else if (lastPathComponent instanceof TriggerCategoryTreeNode) {
//				final TriggerCategoryTreeNode triggerTreeNode = (TriggerCategoryTreeNode) lastPathComponent;
//				triggerTreeNode.getCategory().setName(super.getCellEditorValue().toString());
//				triggerTreeNode.setUserObject(super.getCellEditorValue().toString());
//				return triggerTreeNode.getCategory().getName();
//			} else if (lastPathComponent instanceof TriggerEnvironmentRootNode) {
//				final TriggerEnvironmentRootNode triggerTreeNode = (TriggerEnvironmentRootNode) lastPathComponent;
//				triggerTreeNode.getTriggerEnvironment().setName(super.getCellEditorValue().toString());
//				triggerTreeNode.setUserObject(super.getCellEditorValue().toString());
//				return triggerTreeNode.getTriggerEnvironment().getName();
//			}
			if (lastPathComponent instanceof TriggerElementTreeNode) {
				TriggerElementTreeNode triggerTreeNode = (TriggerElementTreeNode) lastPathComponent;
				triggerTreeNode.setNodeObjectName(super.getCellEditorValue().toString());
				triggerTreeNode.setUserObject(super.getCellEditorValue().toString());
				return triggerTreeNode.getNodeObjectName();
			}
		}
		return super.getCellEditorValue();
	}

	@Override
	protected void determineOffset(final JTree tree, final Object value, final boolean isSelected,
								   final boolean expanded, final boolean leaf, final int row) {
		// TODO override the decisions on "editingIcon"
		if (renderer != null) {
			if (leaf) {
				editingIcon = renderer.getLeafIcon();
			} else if (expanded) {
				editingIcon = renderer.getOpenIcon();
			} else {
				editingIcon = renderer.getClosedIcon();
			}
			if (editingIcon != null) {
				offset = renderer.getIconTextGap() + editingIcon.getIconWidth();
			} else {
				offset = renderer.getIconTextGap();
			}
		} else {
			editingIcon = null;
			offset = 0;
		}
	}

//	@Override
//	public Component getTreeCellEditorComponent2(JTree tree, Object value, boolean isSelected,
//                                          boolean expanded, boolean leaf, int row) {
//		DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
//		if (node instanceof TriggerEnvironmentRootNode) {
//			TriggerEnvironmentRootNode rootNode = (TriggerEnvironmentRootNode) node;
//			TriggerEnvironment triggerEnv = rootNode.getTriggerEnvironment();
//			String displayName = triggerEnv.getName();
//			JComponent treeCellEditorComponent = (JComponent) super.getTreeCellEditorComponent(tree, displayName, isSelected, expanded, leaf, row);
//			try {
//				final BufferedImage img = BLPHandler.get().getGameTex("ReplaceableTextures\\WorldEditUI\\CampaignEditor-Map.blp");
//				treeCellEditorComponent.setIcon(new ImageIcon(img.getScaledInstance(16, 16, Image.SCALE_FAST)));
//			} catch (final Exception exc) {
//				exc.printStackTrace();
//			}
//			return treeCellEditorComponent;
//		} else if (node instanceof TriggerTreeNode) {
//			TriggerTreeNode triggerTreeNode = (TriggerTreeNode) node;
//			Trigger trigger = triggerTreeNode.getTrigger();
//			String displayName = trigger.getName();
//			super.getTreeCellRendererComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
//			try {
//				if (trigger.isComment()) {
//					setForeground(settings.getTriggerCommentColor());
//					setIcon(worldEditArt.getIcon("SEIcon_TriggerComment"));
//				} else {
//					setIcon(worldEditArt.getIcon("SEIcon_Trigger"));
//				}
//			} catch (final Exception exc) {
//				exc.printStackTrace();
//			}
//		} else if (node instanceof TriggerCategoryTreeNode) {
//			TriggerCategoryTreeNode triggerTreeNode = (TriggerCategoryTreeNode) node;
//			TriggerCategory trigger = triggerTreeNode.getCategory();
//			String displayName = trigger.getName();
//			Component component = super.getTreeCellEditorComponent(tree, displayName, selected, expanded, leaf, row, hasFocus);
//			try {
//				if (expanded) {
//					component.setIcon(worldEditArt.getIcon("SEIcon_TriggerCategoryOpen"));
//				} else {
//					component.setIcon(worldEditArt.getIcon("SEIcon_TriggerCategory"));
//				}
//			} catch (final Exception exc) {
//				exc.printStackTrace();
//			}
//			return component;
//		}
//		return null;
//	}
}
