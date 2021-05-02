package com.hiveworkshop.rms.ui.application.edit.uv.panel;

import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivity;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportListener;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.ViewportView;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordDisplayListener;
import com.hiveworkshop.rms.ui.application.edit.uv.UVViewportModelRenderer;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditor;
import com.hiveworkshop.rms.ui.application.edit.uv.types.TVertexEditorChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
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

	public UVViewport(ModelHandler modelHandler, UVPanel uvPanel, ProgramPreferences programPreferences, ViewportActivity viewportActivity, CoordDisplayListener coordDisplayListener, TVertexEditor editor) {
		super(modelHandler, (byte) 0, (byte) 1, new Dimension(400, 400), programPreferences, viewportActivity, new ViewportListener(), coordDisplayListener);

		this.editor = editor;
		this.viewportListener = new ViewportListener();
		coordinateSystem.setYFlip(1);

		viewport = null;


		contextMenu = new JPopupMenu();
		placeholderButton = new JMenuItem("Placeholder Button");
		contextMenu.add(placeholderButton);

		this.uvPanel = uvPanel;

		viewportModelRenderer = new UVViewportModelRenderer();
	}

	public void init() {
		coordinateSystem.setZoom(getWidth());
		coordinateSystem.setGeomPosition(0,0);
	}

	public void paintComponent(Graphics g, int vertexSize) {
//		super.paintComponent(g);
		if (programPreferences.show2dGrid()) {
			drawGrid(g);
		}

		PaintBackgroundImage(g);

		Graphics2D graphics2d = (Graphics2D) g;
		viewportModelRenderer.reset(graphics2d, programPreferences, coordinateSystem, modelHandler);

		activityListener.render(graphics2d, coordinateSystem, modelHandler.getRenderModel(), false);
	}

	private void PaintBackgroundImage(Graphics g) {
		for (Image background : backgrounds) {
			if (uvPanel.wrapImage.isSelected()) {
				Vec2 geomMin = new Vec2(coordinateSystem.geomX(0), coordinateSystem.geomY(0));
				Vec2 geomMax = new Vec2(coordinateSystem.geomX(getWidth()), coordinateSystem.geomY(getHeight()));

				double geomMinX = coordinateSystem.geomX(0);
				double geomMinY = coordinateSystem.geomY(0);
				double geomMaxX = coordinateSystem.geomX(getWidth());
				double geomMaxY = coordinateSystem.geomY(getHeight());
				int minX = (int) Math.floor(geomMinX);
				int minY = (int) Math.floor(geomMinY);
				int maxX = (int) Math.ceil(geomMaxX);
				int maxY = (int) Math.ceil(geomMaxY);
				for (int y = minY; y < maxY; y++) {
					for (int x = minX; x < maxX; x++) {
						g.drawImage(background, (int) coordinateSystem.viewX(x), (int) coordinateSystem.viewY(y), (int) (coordinateSystem.viewX(x + 1) - coordinateSystem.viewX(x)), (int) (coordinateSystem.viewY(y + 1) - coordinateSystem.viewY(y)), null);
					}
				}
			} else {
				g.drawImage(background, (int) coordinateSystem.viewX(0), (int) coordinateSystem.viewY(0), (int) (coordinateSystem.viewX(1) - coordinateSystem.viewX(0)), (int) (coordinateSystem.viewY(1) - coordinateSystem.viewY(0)), null);
			}
		}
	}

	public void setAspectRatio(final double ratio) {
		coordinateSystem.setAspectRatio(ratio);
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