package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.UndoActionListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.UVViewportModelRenderer;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoHandler;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import com.hiveworkshop.rms.util.Vec2;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class UVViewport extends ViewportView implements TVertexEditorChangeListener {
	ArrayList<Image> backgrounds = new ArrayList<>();

	JMenuItem placeholderButton;
	UVPanel uvPanel;
	private final UVViewportModelRenderer viewportModelRenderer;
	private TVertexEditor editor;

	public UVViewport(ModelView modelView, UVPanel uvPanel, ProgramPreferences programPreferences, ViewportActivity viewportActivity, UndoActionListener undoListener, UndoHandler undoHandler, CoordDisplayListener coordDisplayListener, TVertexEditor editor) {
		super(modelView, (byte) 0, (byte) 1, new Dimension(400, 400), programPreferences, viewportActivity, new ViewportListener(), undoListener, undoHandler, coordDisplayListener);

		this.editor = editor;
		this.viewportListener = new ViewportListener();
		// Dimension 1 and Dimension 2, these specify which dimensions to display.
		// the d bytes can thus be from 0 to 2, specifying either the X, Y, or Z dimensions


//		coordinateSystem = new BasicCoordinateSystem((byte) 0, (byte) 1, this);
		coordinateSystem = this;
		viewport = null;


		contextMenu = new JPopupMenu();
		placeholderButton = new JMenuItem("Placeholder Button");
		contextMenu.add(placeholderButton);

		this.uvPanel = uvPanel;

		viewportModelRenderer = new UVViewportModelRenderer();
	}

	public void init() {
		zoom = getWidth();
		cameraX = geomX(0);
		cameraY = geomY(0);
	}

	public void paintComponent(Graphics g, int vertexSize) {
//		super.paintComponent(g);
		if (programPreferences.show2dGrid()) {
			drawGrid(g);
		}

		PaintBackgroundImage(g);

		Graphics2D graphics2d = (Graphics2D) g;
//		dispMDL.drawGeosets(g, this, vertexSize);
		viewportModelRenderer.reset(graphics2d, programPreferences, this, coordinateSystem, modelView);
		modelView.visitMesh(viewportModelRenderer);
		activityListener.renderStatic(graphics2d, coordinateSystem);
	}

	private void PaintBackgroundImage(Graphics g) {
		for (Image background : backgrounds) {
			if (uvPanel.wrapImage.isSelected()) {
				Vec2 geomMin = new Vec2(geomX(0), geomY(0));
				Vec2 geomMax = new Vec2(geomX(getWidth()), geomY(getHeight()));

				double geomMinX = geomX(0);
				double geomMinY = geomY(0);
				double geomMaxX = geomX(getWidth());
				double geomMaxY = geomY(getHeight());
				int minX = (int) Math.floor(geomMinX);
				int minY = (int) Math.floor(geomMinY);
				int maxX = (int) Math.ceil(geomMaxX);
				int maxY = (int) Math.ceil(geomMaxY);
				for (int y = minY; y < maxY; y++) {
					for (int x = minX; x < maxX; x++) {
						g.drawImage(background, (int) viewX(x), (int) viewY(y), (int) (viewX(x + 1) - viewX(x)), (int) (viewY(y + 1) - viewY(y)), null);
					}
				}
			} else {
				g.drawImage(background, (int) viewX(0), (int) viewY(0), (int) (viewX(1) - viewX(0)), (int) (viewY(1) - viewY(0)), null);
			}
		}
	}

	public void setAspectRatio(final double ratio) {
		aspectRatio = ratio;
		setMinimumSize(new Dimension((int) (400 * ratio), 400));
		remove(boxX);
		add(boxX = Box.createHorizontalStrut((int) (400 * ratio)));
		uvPanel.packFrame();
	}

	public void addBackgroundImage(final Image i) {
		backgrounds.add(i);
		setAspectRatio(i.getWidth(null) / (double) i.getHeight(null));
	}

	public void clearBackgroundImage() {
		backgrounds.clear();
	}

	@Override
	public void editorChanged(final TVertexEditor newModelEditor) {
		editor = newModelEditor;
	}
}