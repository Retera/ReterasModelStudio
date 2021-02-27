package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditor;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ModelEditorChangeNotifier;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Write a description of class DisplayPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class DisplayPanel extends JPanel implements ActionListener {
	private Viewport vp;
	private final String title;
	private final JButton up, down, left, right, plusZoom, minusZoom;
	private final ViewportActivity activityListener;
	private final ModelEditorChangeNotifier modelEditorChangeNotifier;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final View view;
	private final ViewportListener viewportListener;

	public DisplayPanel(final String title, final byte a, final byte b, final ModelView modelView,
						final ModelEditor modelEditor, final ModelStructureChangeListener modelStructureChangeListener,
						final ViewportActivity activityListener, final ProgramPreferences preferences,
						final UndoActionListener undoListener, final CoordDisplayListener coordDisplayListener,
						final UndoHandler undoHandler, final ModelEditorChangeNotifier modelEditorChangeNotifier,
						final ViewportTransferHandler viewportTransferHandler, final RenderModel renderModel,
						final ViewportListener viewportListener) {
		super();
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.activityListener = activityListener;
		this.modelEditorChangeNotifier = modelEditorChangeNotifier;
		this.viewportListener = viewportListener;

		setLayout(new MigLayout("gap 0, hidemode 2", "[grow][][][]", "[][][][][grow]"));

		setOpaque(true);
		vp = getViewport(a, b, modelView, preferences, undoListener, coordDisplayListener, undoHandler, modelEditor, viewportTransferHandler, renderModel);
		modelEditorChangeNotifier.subscribe(vp);
		add(vp, "cell 0 0, w 50%:100%:100%, spany, growy, north, west");
		this.title = title;

		plusZoom = addButton(20, 20, "Plus.png");
		minusZoom = addButton(20, 20, "Minus.png");
		up = addButton(32, 16, "ArrowUp.png");
		left = addButton(16, 32, "ArrowLeft.png");
		right = addButton(16, 32, "ArrowRight.png");
		down = addButton(32, 16, "ArrowDown.png");

		add(plusZoom, "cell 2 0, gapy 16, align center top");
		add(minusZoom, "cell 2 1, gapy 16, align center top");
		add(up, "cell 2 2, gapy 16, align center bottom");
		add(left, "cell 1 3, left");
		add(right, "cell 3 3, right");
		add(down, "cell 2 4, align center top");


		view = new View(title, null, this);
		vp.setView(view);
	}

	private JButton addButton(int width, int height, String iconPath) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton("");
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage(iconPath)));
		button.addActionListener(this);
		add(button);
		return button;
	}

	public View getView() {
		return view;
	}

	public void setControlsVisible(final boolean flag) {
		up.setVisible(flag);
		down.setVisible(flag);
		left.setVisible(flag);
		right.setVisible(flag);
		plusZoom.setVisible(flag);
		minusZoom.setVisible(flag);
	}

	public Viewport getViewport(final byte a, final byte b, final ModelView modelView,
	                            final ProgramPreferences programPreferences, final UndoActionListener undoListener,
	                            final CoordDisplayListener coordDisplayListener, final UndoHandler undoHandler,
	                            final ModelEditor modelEditor, final ViewportTransferHandler viewportTransferHandler,
	                            final RenderModel renderModel) {
		return new Viewport(a, b, modelView, programPreferences, activityListener, modelStructureChangeListener, undoListener, coordDisplayListener, undoHandler, modelEditor, viewportTransferHandler, renderModel, viewportListener);
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		revalidate();
		// g.drawString(title,3,3);
		vp.repaint();
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		if (e.getSource() == up) {
			vp.translate(0, (20 * (1 / vp.getZoomAmount())));
			vp.repaint();
		}
		if (e.getSource() == down) {
			vp.translate(0, (-20 * (1 / vp.getZoomAmount())));
			vp.repaint();
		}
		if (e.getSource() == left) {
			vp.translate((20 * (1 / vp.getZoomAmount())), 0);
			vp.repaint();
		}
		if (e.getSource() == right) {
			vp.translate((-20 * (1 / vp.getZoomAmount())), 0);// *vp.getZoomAmount()
			vp.repaint();
		}
		if (e.getSource() == plusZoom) {
			vp.zoom(.15);
			vp.repaint();
		}
		if (e.getSource() == minusZoom) {
			vp.zoom(-.15);
			vp.repaint();
		}
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(vp.getBufferedImage());
	}

	public BufferedImage getBufferedImage() {
		return vp.getBufferedImage();
	}

	public Viewport getViewport() {
		return vp; // TODO why is this named vp is it the vice president
	}
}
