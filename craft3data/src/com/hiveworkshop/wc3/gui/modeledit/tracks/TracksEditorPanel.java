package com.hiveworkshop.wc3.gui.modeledit.tracks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class TracksEditorPanel extends JPanel {
	private final ModelComponentAnimFlagTree modelComponentAnimFlagTree;

	public TracksEditorPanel(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelEditorManager modelEditorManager,
			final ModelStructureChangeListener modelStructureChangeListener) {
		modelComponentAnimFlagTree = new ModelComponentAnimFlagTree(modelViewManager, undoActionListener,
				modelEditorManager, modelStructureChangeListener);
		setLayout(new BorderLayout());

		final TracksEditorTimelinePanel timelinePanel = new TracksEditorTimelinePanel(modelComponentAnimFlagTree);
		final JScrollPane pane = new JScrollPane(timelinePanel);
		pane.setRowHeaderView(modelComponentAnimFlagTree);
		add(pane, BorderLayout.CENTER);
//		JTable table = new JTable();
//		new TreeList<>(null, null, null)
//		TreeTableSupport support = TreeTableSupport.install(table, null, 0);
//		support.
		modelComponentAnimFlagTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(final TreeExpansionEvent event) {
				timelinePanel
						.setPreferredSize(new Dimension(30000, modelComponentAnimFlagTree.getPreferredSize().height));
			}

			@Override
			public void treeCollapsed(final TreeExpansionEvent event) {
				timelinePanel
						.setPreferredSize(new Dimension(30000, modelComponentAnimFlagTree.getPreferredSize().height));
			}
		});
	}

	private static final class TracksEditorTimelinePanel extends JPanel {
		private final ModelComponentAnimFlagTree tree;

		public TracksEditorTimelinePanel(final ModelComponentAnimFlagTree tree) {
			this.tree = tree;
			setPreferredSize(new Dimension(30000, 1));
		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
		}
	}

	public void reloadFromModelView() {
		modelComponentAnimFlagTree.reloadFromModelView();
	}
}
