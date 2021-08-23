package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.ModelEditorManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.application.viewer.PerspectiveViewport;
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
public class DisplayPanel extends JPanel {
	private PerspectiveViewport vp2;
	private final JPanel buttonPanel;
	private ViewportActivityManager activityListener;
	private ViewportListener viewportListener;
	Consumer<Cursor> cursorManager;

	public DisplayPanel(String title, byte a, byte b, ModelHandler modelHandler,
	                    ModelEditorManager modelEditorManager,
	                    ViewportActivityManager activityListener,
	                    CoordDisplayListener coordDisplayListener,
	                    ViewportTransferHandler viewportTransferHandler,
	                    ViewportListener viewportListener) {
		super(new MigLayout("gap 0, ins 0, hidemode 2", "[grow][]", "[grow]"));
		this.activityListener = activityListener;
//		this.viewportListener = viewportListener;
		setupCopyPaste(viewportTransferHandler);

//		addMouseMotionListener(getMouseAdapter());

		cursorManager = this::setCursor;

		JPanel viewHolderPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		setOpaque(true);

		try {
			vp2 = new PerspectiveViewport();
			vp2.setMinimumSize(new Dimension(200, 200));
			CameraHandler cameraHandler = vp2.getCameraHandler();
			cameraHandler.toggleOrtho().setAllowToggleOrtho(false);
			cameraHandler.setCameraTop(300);

			setModel(modelHandler, activityListener);
			viewHolderPanel.add(vp2, "spany, growy, growx");
			add(viewHolderPanel, "spany, growy, growx");
//			add(vp2, "spany, growy, growx");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		buttonPanel = getButtonPanel();
		add(buttonPanel, "gapy 16, top");
	}
	public DisplayPanel() {
		super(new MigLayout("gap 0, ins 0, hidemode 2", "[grow][]", "[grow]"));
//		this.viewportListener = windowHandler2.getViewportListener();
		setupCopyPaste(ProgramGlobals.getViewportTransferHandler());
//		addMouseMotionListener(getMouseAdapter());

		cursorManager = this::setCursor;

		JPanel viewHolderPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		setOpaque(true);

		try {
			vp2 = new PerspectiveViewport();
			vp2.setMinimumSize(new Dimension(200, 200));
			CameraHandler cameraHandler = vp2.getCameraHandler();
			cameraHandler.toggleOrtho().setAllowToggleOrtho(false);
			cameraHandler.setCameraTop(300);

			viewHolderPanel.add(vp2, "spany, growy, growx");
			add(viewHolderPanel, "spany, growy, growx");
//			add(vp2, "spany, growy, growx");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		buttonPanel = getButtonPanel();
		add(buttonPanel, "gapy 16, top");
	}

	public DisplayPanel setModel(ModelHandler modelHandler, ViewportActivityManager activityListener) {
		this.activityListener = activityListener;
		if(modelHandler != null){
			vp2.getCameraHandler().setActivityManager(activityListener);
			vp2.setModel(modelHandler.getModelView(), modelHandler.getRenderModel(), true);
			vp2.getMouseListenerThing().setActivityManager(activityListener);
		} else {
			vp2.setModel(null, null, false);
		}
		return this;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][][][]"));
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton plusZoom = addButton(20, 20, "Plus.png", e -> zoom(.15));
		JButton minusZoom = addButton(20, 20, "Minus.png", e -> zoom(-.15));
		JButton up = addButton(32, 16, "ArrowUp.png", e -> pan(0,-20));
		JButton left = addButton(16, 32, "ArrowLeft.png", e -> pan(20,0));
		JButton right = addButton(16, 32, "ArrowRight.png", e -> pan(-20,0));
		JButton down = addButton(32, 16, "ArrowDown.png", e -> pan(0,20));


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
		buttonPanel.setVisible(flag);
	}

	public DisplayPanel reload() {
		vp2.reloadTextures();
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
		CameraHandler ch = vp2.getCameraHandler();
		ch.translate(x * ch.getZoom(),y * ch.getZoom());
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
}
