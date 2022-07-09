package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportSettings;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Write a description of class DisplayPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class DisplayPanelCanvas extends JPanel {
	private ViewportCanvas vp2;
	private ViewportSettings viewportSettings;
	private final JPanel buttonPanel;
	private ViewportActivityManager activityListener;
	private final Consumer<Cursor> cursorManager;

	public DisplayPanelCanvas(boolean allowButtonPanel) {
		super(new MigLayout("gap 0, ins 0, hidemode 2", "[grow][]", "[grow]"));
//		this.viewportListener = windowHandler2.getViewportListener();
		setupCopyPaste(ProgramGlobals.getViewportTransferHandler());

		cursorManager = this::setCursor;

		JPanel viewHolderPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		setOpaque(true);

		try {
			vp2 = new ViewportCanvas(ProgramGlobals.getPrefs());
			vp2.setMinimumSize(new Dimension(200, 200));

			vp2.getCameraHandler().toggleOrtho();
			viewportSettings = vp2.getViewportSettings();
			viewportSettings.setShowNodes(true);

			viewHolderPanel.add(vp2, "spany, spanx, growy, growx");
			add(viewHolderPanel, "spany, growy, growx");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		if(allowButtonPanel){
			buttonPanel = getButtonPanel();
			add(buttonPanel, "gapy 16, top");
		} else {
			buttonPanel = null;
		}
	}

	public DisplayPanelCanvas setModel(ModelHandler modelHandler, ViewportActivityManager activityListener) {
		this.activityListener = activityListener;
		if(modelHandler != null){
			vp2.setModel(modelHandler.getModelView(), modelHandler.getRenderModel(), true);
//			vp2.getCameraHandler().setOrtho(true);
			vp2.getMouseAdapter().setActivityManager(activityListener);
		} else {
			vp2.setModel(null, null, false);
		}
		return this;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][][][]"));
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton plusZoom = getButton(20, 20, "Plus.png", e -> zoom(1.15));
		JButton minusZoom = getButton(20, 20, "Minus.png", e -> zoom(1/1.15));
		JButton up = getButton(32, 16, "ArrowUp.png", e -> pan(0, -20));
		JButton left = getButton(16, 32, "ArrowLeft.png", e -> pan(20, 0));
		JButton right = getButton(16, 32, "ArrowRight.png", e -> pan(-20, 0));
		JButton down = getButton(32, 16, "ArrowDown.png", e -> pan(0, 20));


		buttonPanel.add(plusZoom, "align center, wrap");
		buttonPanel.add(minusZoom, "gapy 16, align center, wrap");
		arrowPanel.add(up, "cell 1 0");
		arrowPanel.add(left, "cell 0 1");
		arrowPanel.add(right, "cell 2 1");
		arrowPanel.add(down, "cell 1 2");
		buttonPanel.add(arrowPanel, "gapy 16");
		return buttonPanel;
	}

	private JButton getButton(int width, int height, String iconPath, ActionListener actionListener) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton("");
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.setIcon(new ImageIcon(RMSIcons.loadDeprecatedImage(iconPath)));
		button.addActionListener(actionListener);
		return button;
	}

	protected MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				if (activityListener != null && !activityListener.isEditing()) {
					activityListener.viewportChanged(cursorManager);
					requestFocus();
				}
			}
		};
	}

	public void setControlsVisible(boolean flag) {
		if(buttonPanel != null){
			buttonPanel.setVisible(flag);
		}
	}

	public DisplayPanelCanvas reload() {
//		vp2.reloadTextures();
		return this;
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public void zoom(double v) {
		vp2.getCameraHandler().zoom(v);
	}

	public void pan(int x, int y) {
		double zoom = vp2.getCameraHandler().getZoom();
		vp2.getCameraHandler().translate(x/100.0 * zoom,y/100.0 * zoom);
	}

	private void setupCopyPaste(ViewportTransferHandler viewportTransferHandler) {
		setTransferHandler(viewportTransferHandler);
		ActionMap map = getActionMap();
		map.put(TextKey.CUT, TransferHandler.getCutAction());
		map.put(TextKey.COPY, TransferHandler.getCopyAction());
		map.put(TextKey.PASTE, TransferHandler.getPasteAction());
//		map.put(TransferHandler.getCutAction().getValue(Action.NAME), TransferHandler.getCutAction());
//		map.put(TransferHandler.getCopyAction().getValue(Action.NAME), TransferHandler.getCopyAction());
//		map.put(TransferHandler.getPasteAction().getValue(Action.NAME), TransferHandler.getPasteAction());
		setFocusable(true);
	}

	public void setFrontView() {
		vp2.getCameraHandler().setCameraRotation(0, 0);
	}

	public void setLeftView() {
		vp2.getCameraHandler().setCameraRotation(90, 0);
	}

	public void setTopView() {
		vp2.getCameraHandler().setCameraRotation(0, 90);
	}


	public DisplayPanelCanvas setRenderTextures(boolean renderTextures) {
		viewportSettings.setRenderTextures(renderTextures);
		return this;
	}

	public DisplayPanelCanvas setWireFrame(boolean wireFrame) {
		viewportSettings.setWireFrame(wireFrame);
		return this;
	}

	public DisplayPanelCanvas setShowNormals(boolean showNormals) {
		viewportSettings.setShowNormals(showNormals);
		return this;
	}

	public DisplayPanelCanvas setShow3dVerts(boolean show3dVerts) {
		viewportSettings.setShow3dVerts(show3dVerts);
		return this;
	}

	public DisplayPanelCanvas setOrtho(boolean ortho){
		vp2.getCameraManager().setOrtho(ortho);
		return this;
	}

//	public AnimatedPerspectiveViewport getVp2() {
//		return null;
//	}
	public ViewportCanvas getVp2() {
		return vp2;
	}
}
