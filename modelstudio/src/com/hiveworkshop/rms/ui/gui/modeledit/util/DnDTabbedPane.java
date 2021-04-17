package com.hiveworkshop.rms.ui.gui.modeledit.util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * DnDTabbedPane.java
 * http://java-swing-tips.blogspot.com/2008/04/drag-and-drop-tabs-in-jtabbedpane.html
 * written by Terai Atsuhiro. eed3si9n.
 *
 * Thanks to Jay Warrick for ideas concerning a mod to preserve TabComponents.
 * Also, modified by Retera for MatrixEater to preserve Icons and Tooltips when
 * dragging tabs.
 */

public class DnDTabbedPane extends JTabbedPane {
	private static final int LINEWIDTH = 3;
	private static final String NAME = "test";
	private final GhostGlassPane glassPane = new GhostGlassPane();
	private final Rectangle2D lineRect = new Rectangle2D.Double();
	private final Color lineColor = new Color(0, 100, 255);
	// private final DragSource dragSource = new DragSource();
	// private final DropTarget dropTarget;
	private int dragTabIndex = -1;

	public DnDTabbedPane() {
		super();
		final DragSourceListener dsl = new DragSourceListener() {
			@Override
			public void dragEnter(final DragSourceDragEvent e) {
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
			}

			@Override
			public void dragExit(final DragSourceEvent e) {
				e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				lineRect.setRect(0, 0, 0, 0);
				glassPane.setPoint(new Point(-1000, -1000));
				glassPane.repaint();
			}

			@Override
			public void dragOver(final DragSourceDragEvent e) {
				// e.getLocation()
				// This method returns a Point indicating the cursor location in
				// screen coordinates at the moment
				final Point tabPt = e.getLocation();
				SwingUtilities.convertPointFromScreen(tabPt, DnDTabbedPane.this);
				final Point glassPt = e.getLocation();
				SwingUtilities.convertPointFromScreen(glassPt, glassPane);
				final int targetIdx = getTargetTabIndex(glassPt);
				if (getTabAreaBound().contains(tabPt)
						&& targetIdx >= 0
						&& targetIdx != dragTabIndex
						&& targetIdx != dragTabIndex + 1) {
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveDrop);
				} else {
					e.getDragSourceContext().setCursor(DragSource.DefaultMoveNoDrop);
				}
			}

			@Override
			public void dragDropEnd(final DragSourceDropEvent e) {
				lineRect.setRect(0, 0, 0, 0);
				dragTabIndex = -1;
				if (hasGhost()) {
					glassPane.setVisible(false);
					glassPane.setImage(null);
				}
			}

			@Override
			public void dropActionChanged(final DragSourceDragEvent e) {
			}
		};
		final Transferable t = new Transferable() {
			private final DataFlavor FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, NAME);

			@Override
			public Object getTransferData(final DataFlavor flavor) {
				return DnDTabbedPane.this;
			}

			@Override
			public DataFlavor[] getTransferDataFlavors() {
				final DataFlavor[] f = new DataFlavor[1];
				f[0] = this.FLAVOR;
				return f;
			}

			@Override
			public boolean isDataFlavorSupported(final DataFlavor flavor) {
				return flavor.getHumanPresentableName().equals(NAME);
			}
		};
		final DragGestureListener dgl = e -> {
            final Point tabPt = e.getDragOrigin();
            dragTabIndex = indexAtLocation(tabPt.x, tabPt.y);
            if (dragTabIndex < 0) {
                return;
            }
            initGlassPane(e.getComponent(), e.getDragOrigin());
            try {
                e.startDrag(DragSource.DefaultMoveDrop, t, dsl);
            } catch (final InvalidDnDOperationException idoe) {
                idoe.printStackTrace();
            }
        };
		// dropTarget =
		new DropTarget(glassPane, DnDConstants.ACTION_COPY_OR_MOVE, new CDropTargetListener(), true);
		new DragSource().createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, dgl);
	}

	class CDropTargetListener implements DropTargetListener {
		@Override
		public void dragEnter(final DropTargetDragEvent e) {
			if (isDragAcceptable(e)) {
				e.acceptDrag(e.getDropAction());
			} else {
				e.rejectDrag();
			}
		}

		@Override
		public void dragExit(final DropTargetEvent e) {
		}

		@Override
		public void dropActionChanged(final DropTargetDragEvent e) {
		}

		@Override
		public void dragOver(final DropTargetDragEvent e) {
			if (getTabPlacement() == SwingConstants.TOP || getTabPlacement() == SwingConstants.BOTTOM) {
				initTargetLeftRightLine(getTargetTabIndex(e.getLocation()));
			} else {
				initTargetTopBottomLine(getTargetTabIndex(e.getLocation()));
			}
			repaint();
			if (hasGhost()) {
				glassPane.setPoint(e.getLocation());
				glassPane.repaint();
			}
		}

		@Override
		public void drop(final DropTargetDropEvent e) {
			if (isDropAcceptable(e)) {
				convertTab(dragTabIndex, getTargetTabIndex(e.getLocation()));
				e.dropComplete(true);
			} else {
				e.dropComplete(false);
			}
			repaint();
		}

		public boolean isDragAcceptable(final DropTargetDragEvent e) {
			final Transferable t = e.getTransferable();
			if (t == null) {
				return false;
			}
			final DataFlavor[] f = e.getCurrentDataFlavors();
			return t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0;
		}

		public boolean isDropAcceptable(final DropTargetDropEvent e) {
			final Transferable t = e.getTransferable();
			if (t == null) {
				return false;
			}
			final DataFlavor[] f = t.getTransferDataFlavors();
			return t.isDataFlavorSupported(f[0]) && dragTabIndex >= 0;
		}
	}

	private boolean hasGhost = true;

	public void setPaintGhost(final boolean flag) {
		hasGhost = flag;
	}

	public boolean hasGhost() {
		return hasGhost;
	}

	private int getTargetTabIndex(final Point glassPt) {
		final Point tabPt = SwingUtilities.convertPoint(glassPane, glassPt, DnDTabbedPane.this);
		final boolean isTB = getTabPlacement() == SwingConstants.TOP || getTabPlacement() == SwingConstants.BOTTOM;
		for (int i = 0; i < getTabCount(); i++) {
			final Rectangle r = getBoundsAt(i);
			setTBRect(isTB, r, r.x - r.width / 2.0, r.y - r.height / 2.0);
			if (r.contains(tabPt)) {
				return i;
			}
		}
		final Rectangle r = getBoundsAt(getTabCount() - 1);
		setTBRect(isTB, r, r.x + r.width / 2.0, r.y + r.height / 2.0);
		return r.contains(tabPt) ? getTabCount() : -1;
	}

	private void setTBRect(boolean isTB, Rectangle r, double x, double y) {
		if (isTB) {
			r.setRect(x, r.y, r.width, r.height);
		} else {
			r.setRect(r.x, y, r.width, r.height);
		}
	}

	private void convertTab(final int prev, final int next) {
		if (next < 0 || prev == next) {
			// System.out.println("press="+prev+" next="+next);
			return;
		}
		final Component cmp = getComponentAt(prev);
		final Component tcmp = getTabComponentAt(prev);
		final Icon icon = getIconAt(prev);
		final String ttp = getToolTipTextAt(prev);

		final String str = getTitleAt(prev);
		if (next == getTabCount()) {
			// System.out.println("last: press="+prev+" next="+next);
			remove(prev);
			addTab(str, cmp);
			setTabComponentAt(getTabCount() - 1, tcmp);
			setIconAt(getTabCount() - 1, icon);
			setToolTipTextAt(getTabCount() - 1, ttp);
			setSelectedIndex(getTabCount() - 1);
		} else if (prev > next) {
			// System.out.println(" >: press="+prev+" next="+next);
			remove(prev);
			insertTab(str, null, cmp, null, next);
			setTabComponentAt(next, tcmp);
			setIconAt(next, icon);
			setToolTipTextAt(next, ttp);
			setSelectedIndex(next);
		} else {
			// System.out.println(" <: press="+prev+" next="+next);
			remove(prev);
			insertTab(str, null, cmp, null, next - 1);
			setTabComponentAt(next - 1, tcmp);
			setIconAt(next - 1, icon);
			setSelectedIndex(next - 1);
		}
	}

	private void initTargetLeftRightLine(final int next) {
		if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1) {
			lineRect.setRect(0, 0, 0, 0);
		} else if (next == getTabCount()) {
			final Rectangle rect = getBoundsAt(getTabCount() - 1);
			lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2.0, rect.y, LINEWIDTH, rect.height);
		} else if (next == 0) {
			final Rectangle rect = getBoundsAt(0);
			lineRect.setRect(-LINEWIDTH / 2.0, rect.y, LINEWIDTH, rect.height);
		} else {
			final Rectangle rect = getBoundsAt(next - 1);
			lineRect.setRect(rect.x + rect.width - LINEWIDTH / 2.0, rect.y, LINEWIDTH, rect.height);
		}
	}

	private void initTargetTopBottomLine(final int next) {
		if (next < 0 || dragTabIndex == next || next - dragTabIndex == 1) {
			lineRect.setRect(0, 0, 0, 0);
		} else if (next == getTabCount()) {
			final Rectangle rect = getBoundsAt(getTabCount() - 1);
			lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2.0, rect.width, LINEWIDTH);
		} else if (next == 0) {
			final Rectangle rect = getBoundsAt(0);
			lineRect.setRect(rect.x, -LINEWIDTH / 2.0, rect.width, LINEWIDTH);
		} else {
			final Rectangle rect = getBoundsAt(next - 1);
			lineRect.setRect(rect.x, rect.y + rect.height - LINEWIDTH / 2.0, rect.width, LINEWIDTH);
		}
	}

	private void initGlassPane(final Component c, final Point tabPt) {
		// Point p = (Point) pt.clone();
		getRootPane().setGlassPane(glassPane);
		if (hasGhost()) {
			final Rectangle rect = getBoundsAt(dragTabIndex);
			BufferedImage image = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_ARGB);
			final Graphics g = image.getGraphics();
			c.paint(g);
			image = image.getSubimage(rect.x, rect.y, rect.width, rect.height);
			glassPane.setImage(image);
		}
		final Point glassPt = SwingUtilities.convertPoint(c, tabPt, glassPane);
		glassPane.setPoint(glassPt);
		glassPane.setVisible(true);
	}

	private Rectangle getTabAreaBound() {
		final Rectangle lastTab = getUI().getTabBounds(this, getTabCount() - 1);
		return new Rectangle(0, 0, getWidth(), lastTab.y + lastTab.height);
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		if (dragTabIndex >= 0) {
			final Graphics2D g2 = (Graphics2D) g;
			g2.setPaint(lineColor);
			g2.fill(lineRect);
		}
	}
}

class GhostGlassPane extends JPanel {
	private final AlphaComposite composite;
	private Point location = new Point(0, 0);
	private BufferedImage draggingGhost = null;

	public GhostGlassPane() {
		setOpaque(false);
		composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
	}

	public void setImage(final BufferedImage draggingGhost) {
		this.draggingGhost = draggingGhost;
	}

	public void setPoint(final Point location) {
		this.location = location;
	}

	@Override
	public void paintComponent(final Graphics g) {
		if (draggingGhost == null) {
			return;
		}
		final Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(composite);
		final double xx = location.getX() - (draggingGhost.getWidth(this) / 2d);
		final double yy = location.getY() - (draggingGhost.getHeight(this) / 2d);
		g2.drawImage(draggingGhost, (int) xx, (int) yy, null);
	}
}