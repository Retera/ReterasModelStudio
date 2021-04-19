package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
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
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * Write a description of class DisplayPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class DisplayPanel extends JPanel {
	private Viewport vp;
	JPanel buttonPanel;
	private final ViewportActivity activityListener;
	private final ModelEditorChangeNotifier modelEditorChangeNotifier;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final View view;
	private final ViewportListener viewportListener;

	public DisplayPanel(String title, byte a, byte b, ModelView modelView,
	                    ModelEditorManager modelEditorManager, ModelStructureChangeListener modelStructureChangeListener,
	                    ViewportActivity activityListener, ProgramPreferences preferences,
	                    UndoActionListener undoListener, CoordDisplayListener coordDisplayListener,
	                    UndoHandler undoHandler, ModelEditorChangeNotifier modelEditorChangeNotifier,
	                    ViewportTransferHandler viewportTransferHandler, RenderModel renderModel,
	                    ViewportListener viewportListener) {
		super();
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.activityListener = activityListener;
		this.modelEditorChangeNotifier = modelEditorChangeNotifier;
		this.viewportListener = viewportListener;

		setLayout(new MigLayout("gap 0, ins 0, hidemode 2", "[grow][]", "[grow]"));

		setOpaque(true);
		vp = getViewport(a, b, modelView, preferences, undoListener, coordDisplayListener, undoHandler, modelEditorManager, viewportTransferHandler, renderModel);
		modelEditorChangeNotifier.subscribe(vp);
		add(vp, "spany, growy, growx");

		buttonPanel = getButtonPanel();
		add(buttonPanel, "gapy 16, top");

		view = new View(title, null, this);
		vp.setView(view);
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][][][]"));
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton plusZoom = addButton(20, 20, "Plus.png", e -> zoom(.15));
		JButton minusZoom = addButton(20, 20, "Minus.png", e -> zoom(-.15));
		JButton up = addButton(32, 16, "ArrowUp.png", e -> panUpDown(20));
		JButton left = addButton(16, 32, "ArrowLeft.png", e -> panLeftRight(20));
		JButton right = addButton(16, 32, "ArrowRight.png", e -> panLeftRight(-20));
		JButton down = addButton(32, 16, "ArrowDown.png", e -> panUpDown(-20));


		buttonPanel.add(plusZoom, "align center, wrap");
		buttonPanel.add(minusZoom, "gapy 16, align center, wrap");
		arrowPanel.add(up, "cell 1 0");
		arrowPanel.add(left, "cell 0 1");
		arrowPanel.add(right, "cell 2 1");
		arrowPanel.add(down, "cell 1 2");
		buttonPanel.add(arrowPanel, "gapy 16");
		return buttonPanel;
	}

	private JButton addButton(int width, int height, String iconPath, ActionListener actionListener) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton("");
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage(iconPath)));
		button.addActionListener(actionListener);
		return button;
	}

	public View getView() {
		return view;
	}

	public void setControlsVisible(boolean flag) {
		buttonPanel.setVisible(flag);
	}

	public Viewport getViewport(byte a, byte b, ModelView modelView,
	                            ProgramPreferences programPreferences, UndoActionListener undoListener,
	                            CoordDisplayListener coordDisplayListener, UndoHandler undoHandler,
	                            ModelEditorManager modelEditorManager, ViewportTransferHandler viewportTransferHandler,
	                            RenderModel renderModel) {
		return new Viewport(a, b, modelView, programPreferences, activityListener, modelStructureChangeListener, undoListener, coordDisplayListener, undoHandler, modelEditorManager, viewportTransferHandler, renderModel, viewportListener);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		revalidate();
		// g.drawString(title,3,3);
		vp.repaint();
	}

	public void zoom(double v) {
		vp.zoom(v);
		vp.repaint();
	}

	public void panLeftRight(int i) {
		vp.translate((i * (1 / vp.getZoomAmount())), 0);
		vp.repaint();
	}

	public void panUpDown(int i) {
		vp.translate(0, (i * (1 / vp.getZoomAmount())));
		vp.repaint();
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
