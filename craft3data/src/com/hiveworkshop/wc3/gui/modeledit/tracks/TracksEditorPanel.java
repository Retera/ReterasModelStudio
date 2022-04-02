package com.hiveworkshop.wc3.gui.modeledit.tracks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.ModelEditorManager;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

public class TracksEditorPanel extends JPanel {
	private final ModelComponentAnimFlagTree modelComponentAnimFlagTree;

	public TracksEditorPanel(final ModelViewManager modelViewManager, final UndoActionListener undoActionListener,
			final ModelEditorManager modelEditorManager,
			final ModelStructureChangeListener modelStructureChangeListener) {
		modelComponentAnimFlagTree = new ModelComponentAnimFlagTree(modelViewManager, undoActionListener,
				modelEditorManager, modelStructureChangeListener);
		setLayout(new BorderLayout());

		final TracksEditorTimelinePanel timelinePanel = new TracksEditorTimelinePanel(modelComponentAnimFlagTree,
				modelViewManager);
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
		modelComponentAnimFlagTree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(final TreeSelectionEvent e) {
				timelinePanel.repaint();
			}
		});
	}

	private static final class TracksEditorTimelinePanel extends JPanel {
		private final GradientPaint keyframePaint;
		private final GradientPaint keyframePaintBlue;
		private final GradientPaint keyframePaintRed;
		private final ModelComponentAnimFlagTree tree;
		private final ModelView modelView;

		public TracksEditorTimelinePanel(final ModelComponentAnimFlagTree tree, final ModelView modelView) {
			this.tree = tree;
			this.modelView = modelView;
			setPreferredSize(new Dimension(30000, 1));
			keyframePaint = new GradientPaint(new Point(0, 10), new Color(200, 255, 200), new Point(0, getHeight()),
					new Color(100, 255, 100), true);
			keyframePaintBlue = new GradientPaint(new Point(0, 10), new Color(200, 200, 255), new Point(0, getHeight()),
					new Color(100, 100, 255), true);
			keyframePaintRed = new GradientPaint(new Point(0, 10), new Color(255, 200, 200), new Point(0, getHeight()),
					new Color(255, 100, 100), true);
		}

		@Override
		protected void paintComponent(final Graphics g) {
			super.paintComponent(g);
			final int rowHeight = 16;// tree.getLastRendererRowHeight();
			final int rowCount = tree.getRowCount();
			for (int i = 0; i < rowCount; i++) {
				final TreePath pathForRow = tree.getPathForRow(i);
				final DefaultMutableTreeNode node = (DefaultMutableTreeNode) pathForRow.getLastPathComponent();
				final Object userObject = node.getUserObject();
				if (userObject instanceof ModelComponentAnimFlagTree.ChooseableAnimFlagItem) {
					final AnimFlag track = ((ModelComponentAnimFlagTree.ChooseableAnimFlagItem) userObject).getFlag();

					g.setColor(Color.BLACK);
					g.fillRect(0, rowHeight * i, getWidth(), rowHeight - 1);
					g.setColor(Color.GRAY);
					g.drawRect(0, rowHeight * i, getWidth(), rowHeight - 1);
					final ArrayList<Integer> times = track.getTimes();
					for (int j = 0; j < times.size(); j++) {
						final Integer time = times.get(j);

						final int currentTimePixelX = computeXFromTime(time);
						final boolean mouseOver = false;// timeAndKey.getValue() == mouseOverFrame;
						boolean translation = false, rotation = false, scaling = false, other = false;
						final boolean afTranslation = "Translation".equals(track.getName());
						translation |= afTranslation;
						final boolean afRotation = "Rotation".equals(track.getName());
						rotation |= afRotation;
						final boolean afScaling = "Scaling".equals(track.getName());
						scaling |= afScaling;
						other |= !(afTranslation || afRotation || afScaling);
						if (scaling) {
							((Graphics2D) g).setPaint(keyframePaintRed);
						}
						else if (rotation) {
							((Graphics2D) g).setPaint(keyframePaint);
						}
						else if (translation) {
							((Graphics2D) g).setPaint(keyframePaintBlue);
						}
						else {
							((Graphics2D) g).setPaint(keyframePaint);
						}
						g.fillRoundRect(currentTimePixelX - 4, rowHeight * i, 8, rowHeight, 2, 2);
						Color color = Color.GREEN;
						if (scaling) {
							color = Color.ORANGE;
						}
						else if (rotation) {
						}
						else if (translation) {
							color = Color.BLUE;
						}
						g.setColor(mouseOver ? Color.RED : color);
						g.drawRoundRect(currentTimePixelX - 4, rowHeight * i, 8, rowHeight, 2, 2);
					}
				}
				else if (userObject instanceof ModelComponentAnimFlagTree.ChooseableModelRoot) {
					g.setColor(Color.GRAY);
					g.drawRect(0, rowHeight * i, getWidth(), rowHeight - 1);
					for (final Animation anim : modelView.getModel().getAnims()) {
						final int xEnd = computeXFromTime(anim.getStart());
						final int xStart = computeXFromTime(anim.getEnd());
						g.setColor(Color.RED.darker());
						g.drawLine(xStart, 0, xStart, getHeight());
						g.drawLine(xEnd, 0, xEnd, getHeight());
						g.setColor(Color.BLACK);
						final String animName = "\"" + anim.getName() + "\"";
						g.drawString(animName, xStart, (rowHeight * i) + ((rowHeight + g.getFont().getSize()) / 2));
						g.drawString(animName, xEnd, (rowHeight * i) + ((rowHeight + g.getFont().getSize()) / 2));
					}
				}
				else {
					g.setColor(Color.GRAY);
					g.drawRect(0, rowHeight * i, getWidth(), rowHeight - 1);
				}

			}
		}
	}

	private static int computeXFromTime(final int time) {
//		final double timeRatio = (time - start) / (double) (end - start);
//		final int widthMinusOffsets = getWidth() - (SIDE_OFFSETS * 2);
//		return (int) (widthMinusOffsets * timeRatio) + (timeChooserRect.width / 2);
		return (int) (time * 0.01f);
	}

	public void reloadFromModelView() {
		modelComponentAnimFlagTree.reloadFromModelView();
	}
}
