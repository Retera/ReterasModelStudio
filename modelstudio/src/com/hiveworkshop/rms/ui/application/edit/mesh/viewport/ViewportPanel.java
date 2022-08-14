package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportCanvas;
import com.hiveworkshop.rms.ui.application.viewer.twiTestRenderMaster.ViewportSettings;
import com.hiveworkshop.rms.ui.gui.modeledit.cutpaste.ViewportTransferHandler;
import com.hiveworkshop.rms.ui.language.TextKey;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ViewportPanel extends JPanel {
	private final ViewportCanvas viewport;
	private final ViewportSettings viewportSettings;
	private final JPanel buttonPanel;
	private ViewportActivityManager activityListener;

	public ViewportPanel(boolean allowButtonPanel, boolean showNodes) {
		this(allowButtonPanel, showNodes, false);
	}
	public ViewportPanel(boolean allowButtonPanel, boolean showNodes, boolean potrait) {
		super(new MigLayout("gap 0, ins 0, hidemode 2", "[grow][]", "[grow]"));
		setupCopyPaste(ProgramGlobals.getViewportTransferHandler());

		JPanel viewHolderPanel = new JPanel(new MigLayout("fill, gap 0, ins 0", "[grow]", "[grow]"));
		setOpaque(true);

		try {
			viewport = new ViewportCanvas(ProgramGlobals.getPrefs(), potrait);
			viewport.setMinimumSize(new Dimension(200, 200));
			viewportSettings = viewport.getViewportSettings();
			viewportSettings.setShowNodes(showNodes);

			viewHolderPanel.add(viewport, "spany, spanx, growy, growx");
			add(viewHolderPanel, "spany, growy, growx");
		} catch (LWJGLException e) {
			throw new RuntimeException(e);
		}

		if (allowButtonPanel) {
			buttonPanel = new VPButtonPanel(viewport.getCameraHandler());
			add(buttonPanel, "gapy 16, top");
		} else {
			buttonPanel = null;
		}
	}
	public ViewportPanel setModel(RenderModel renderModel, ViewportActivityManager activityListener) {
		this.activityListener = activityListener;
		if (renderModel != null) {
			viewport.setModel(renderModel);
			viewport.getMouseAdapter().setActivityManager(activityListener);
		} else {
			viewport.setModel(null);
		}
		return this;
	}

	private void setupCopyPaste(ViewportTransferHandler viewportTransferHandler) {
		setTransferHandler(viewportTransferHandler);
		ActionMap map = getActionMap();
		map.put(TextKey.CUT, TransferHandler.getCutAction());
		map.put(TextKey.COPY, TransferHandler.getCopyAction());
		map.put(TextKey.PASTE, TransferHandler.getPasteAction());
		setFocusable(true);
	}

	protected MouseAdapter getMouseAdapter() {
		return new MouseAdapter() {
			@Override
			public void mouseEntered(final MouseEvent e) {
				if (activityListener != null && !activityListener.isEditing()) {
					activityListener.viewportChanged(ViewportPanel.this::setCursor);
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

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	public ViewportPanel reload() {
//		viewport.reloadTextures();
		return this;
	}

	public ViewportPanel setRenderTextures(boolean renderTextures) {
		viewportSettings.setRenderTextures(renderTextures);
		return this;
	}

	public ViewportPanel setWireFrame(boolean wireFrame) {
		viewportSettings.setWireFrame(wireFrame);
		return this;
	}

	public ViewportPanel setShowNormals(boolean showNormals) {
		viewportSettings.setShowNormals(showNormals);
		return this;
	}

	public ViewportPanel setShow3dVerts(boolean show3dVerts) {
		viewportSettings.setShow3dVerts(show3dVerts);
		return this;
	}

	public ViewportCanvas getViewport() {
		return viewport;
	}
}
