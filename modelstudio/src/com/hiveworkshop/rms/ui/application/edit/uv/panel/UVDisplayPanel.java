package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.viewer.CameraHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.icons.RMSIcons;
import com.hiveworkshop.rms.ui.language.TextKey;
import com.hiveworkshop.rms.util.TwiComboBox;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.function.Consumer;

public class UVDisplayPanel extends JPanel {
	private TextureViewport vp2;
	private final JPanel buttonPanel;
	private ViewportActivityManager activityListener;
	Consumer<Cursor> cursorManager;

	TwiComboBox<Bitmap> modelTextures;

	public UVDisplayPanel() {
		super(new MigLayout("gap 0, ins 0, hidemode 2", "[grow][]", "[grow]"));
		setupCopyPaste(ProgramGlobals.getViewportTransferHandler());

		cursorManager = this::setCursor;

		JPanel viewHolderPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		setOpaque(true);

		try {
			vp2 = new TextureViewport();
			vp2.setMinimumSize(new Dimension(200, 200));
			CameraHandler cameraHandler = vp2.getCameraHandler();
			cameraHandler.toggleOrtho().setAllowToggleOrtho(false);
			cameraHandler.setAllowRotation(false);

			modelTextures = new TwiComboBox<>(new Bitmap("", 1));
			modelTextures.addOnSelectItemListener(b -> vp2.setCurrTexture(b));
			modelTextures.setStringFunctionRender(b -> b instanceof Bitmap ? ((Bitmap)b).getName() : "none");


			viewHolderPanel.add(vp2, "spany, growy, growx");
			add(viewHolderPanel, "spany, growy, growx");
//			add(vp2, "spany, growy, growx");
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		buttonPanel = getButtonPanel();
		add(buttonPanel, "gapy 16, top");
	}

	public UVDisplayPanel setModel(ModelHandler modelHandler, ViewportActivityManager activityListener) {
		this.activityListener = activityListener;
		if (modelHandler != null) {
//			vp2.getCameraHandler().setActivityManager(activityListener);
			vp2.setModel(modelHandler.getModelView(), modelHandler.getRenderModel(), true);
			vp2.getMouseListenerThing().setActivityManager(activityListener);
			vp2.reloadAllTextures();
			modelTextures.setNewLinkedModelOf(modelHandler.getModel().getTextures());
			modelTextures.setSelectedIndex(0);
			vp2.setCurrTexture(modelHandler.getModel().getTextures().get(0));
		} else {
			vp2.setModel(null, null, false);
			modelTextures.setNewLinkedModelOf(new ArrayList<>());
		}
		return this;
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][][][]"));
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton plusZoom = getButton(20, 20, "Plus.png", e -> zoom(.15));
		JButton minusZoom = getButton(20, 20, "Minus.png", e -> zoom(-.15));
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
		buttonPanel.add(arrowPanel, "gapy 16, wrap");

		JPanel rotPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton pos_x = getButton(20, 20, "Plus.png", e -> rot(45, 0, 0));
		JButton neg_x = getButton(20, 20, "Minus.png", e -> rot(-45, 0, 0));
		JButton pos_y = getButton(20, 20, "Plus.png", e -> rot(0, 45, 0));
		JButton neg_y = getButton(20, 20, "Minus.png", e -> rot(0, -45, 0));
		JButton pos_z = getButton(20, 20, "Plus.png", e -> rot(0, 0, 45));
		JButton neg_z = getButton(20, 20, "Minus.png", e -> rot(0, 0, -45));
		rotPanel.add(new JLabel("rotX"), "");
		rotPanel.add(pos_x, "");
		rotPanel.add(neg_x, "wrap");
		rotPanel.add(new JLabel("rotY"), "");
		rotPanel.add(pos_y, "");
		rotPanel.add(neg_y, "wrap");
		rotPanel.add(new JLabel("rotZ"), "");
		rotPanel.add(pos_z, "");
		rotPanel.add(neg_z, "wrap");

		buttonPanel.add(rotPanel, "gapy 16, wrap");


		JPanel arrowPanel2 = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton up2 = getButton(32, 16, "ArrowUp.png", e -> pan2(0, -.5));
		JButton left2 = getButton(16, 32, "ArrowLeft.png", e -> pan2(.5, 0));
		JButton right2 = getButton(16, 32, "ArrowRight.png", e -> pan2(-.5, 0));
		JButton down2 = getButton(32, 16, "ArrowDown.png", e -> pan2(0, .5));

		arrowPanel2.add(up2, "cell 1 0");
		arrowPanel2.add(left2, "cell 0 1");
		arrowPanel2.add(right2, "cell 2 1");
		arrowPanel2.add(down2, "cell 1 2");

		buttonPanel.add(arrowPanel2, "gapy 16, wrap");
		buttonPanel.add(modelTextures, "gapy 16, wrap");


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
		buttonPanel.setVisible(flag);
	}

	public UVDisplayPanel reload() {
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
		ch.translate(x * ch.getZoom(), y * ch.getZoom());
	}

	public void pan2(double x, double y) {
		CameraHandler ch = vp2.getCameraHandler();
//		ch.translate(x * ch.getZoom(),y * ch.getZoom());
//		ch.translate2(0, x, y);
	}

	public void rot(int x, int y, int z) {
		CameraHandler ch = vp2.getCameraHandler();
		ch.rot(x, y, z);
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


//	public UVDisplayPanel setRenderTextures(boolean renderTextures) {
//		vp2.setRenderTextures(renderTextures);
//		return this;
//	}
//
//	public UVDisplayPanel setWireFrame(boolean wireFrame) {
//		vp2.setWireFrame(wireFrame);
//		return this;
//	}
//
//	public UVDisplayPanel setShowNormals(boolean showNormals) {
//		vp2.setShowNormals(showNormals);
//		return this;
//	}
//
//	public UVDisplayPanel setShow3dVerts(boolean show3dVerts) {
//		vp2.setShow3dVerts(show3dVerts);
//		return this;
//	}
}
