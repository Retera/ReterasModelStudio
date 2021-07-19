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
import net.infonode.docking.View;
import net.miginfocom.swing.MigLayout;
import org.lwjgl.LWJGLException;

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
	private PerspectiveViewport vp2;
	JPanel buttonPanel;
	private final ViewportActivityManager activityListener;
	private final View view;
	private final ViewportListener viewportListener;

	public DisplayPanel(String title, byte a, byte b, ModelHandler modelHandler,
	                    ModelEditorManager modelEditorManager,
	                    ViewportActivityManager activityListener,
	                    CoordDisplayListener coordDisplayListener,
	                    ViewportTransferHandler viewportTransferHandler,
	                    ViewportListener viewportListener) {
		super(new MigLayout("gap 0, ins 0, hidemode 2", "[grow][]", "[grow]"));
		this.activityListener = activityListener;
		this.viewportListener = viewportListener;

		setOpaque(true);
		vp = new Viewport(a, b, modelHandler, activityListener, coordDisplayListener, modelEditorManager, viewportTransferHandler, viewportListener);
//		add(vp, "spany, growy, growx");

		try {
			vp2 = new PerspectiveViewport(modelHandler.getModelView(), modelHandler.getRenderModel(), modelHandler.getEditTimeEnv(), false);
			vp2.setMinimumSize(new Dimension(200, 200));
			CameraHandler cameraHandler = vp2.getCameraHandler();
			cameraHandler.toggleOrtho().setAllowRotation(false).setAllowToggleOrtho(false);
			cameraHandler.setCameraTop(300);
			cameraHandler.setActivityManager(activityListener);
			add(vp2, "spany, growy, growx");
		} catch (LWJGLException e) {
			e.printStackTrace();
			add(vp, "spany, growy, growx");
		}

		buttonPanel = getButtonPanel();
		add(buttonPanel, "gapy 16, top");

		view = new View(title, null, this);
		vp.setView(view);
	}

	private JPanel getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][][][]"));
		JPanel arrowPanel = new JPanel(new MigLayout("gap 0, ins 0, fill", "[][][]", "[][][]"));
		JButton plusZoom = addButton(20, 20, "Plus.png", e -> zoom(1.15));
		JButton minusZoom = addButton(20, 20, "Minus.png", e -> zoom(-1.15));
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

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		revalidate();
		// g.drawString(title,3,3);

//		g.setColor(ProgramGlobals.getPrefs().getSelectColor());

//		try {
//
//				vp2.paint(vp2.getGraphics());
//			if(!vp2.isCurrent()){
//				vp2.makeCurrent();
//			}
//			if(vp2.isCurrent()){
//				System.out.println("Painted square!");
////				vp2.paint(vp2.getGraphics());
////				vp2.paint(vp2.getGraphics());
////				GL11.glDepthMask(false);
//				GL11.glEnable(GL11.GL_BLEND);
//				GL11.glDisable(GL_CULL_FACE);
//				GL11.glDisable(GL_DEPTH_TEST);
//				GL11.glDisable(GL_ALPHA_TEST);
//				GL11.glDisable(GL_TEXTURE_2D);
//				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
//
//				glColor4f(1f, .2f, 1f, .4f);
//				glBegin(GL11.GL_LINES);
//				GL11.glNormal3f(0, 0, 0);
//
//				int ySize = 50;
//				int xSize = 10;
//
//
//				int height = -310;
//
//				GL11.glVertex3f(xSize, height, ySize);
//				GL11.glVertex3f(xSize, height, -ySize);
//
//				GL11.glVertex3f(xSize, height, -ySize);
//				GL11.glVertex3f(-xSize, height, -ySize);
//
//				GL11.glVertex3f(-xSize, height, -ySize);
//				GL11.glVertex3f(-xSize, height, ySize);
//
//				GL11.glVertex3f(-xSize, height, ySize);
//				GL11.glVertex3f(xSize, height, ySize);
//
//
//				glEnd();
//				vp2.releaseContext();
//			}
//		} catch (LWJGLException e) {
//			e.printStackTrace();
//		}
//		vp2.getContext();
//
//		vp2.paint(vp2.getGraphics());
//		GL11.glDepthMask(false);
//		GL11.glDisable(GL_CULL_FACE);
//		GL11.glDisable(GL_DEPTH_TEST);
//		GL11.glDisable(GL_ALPHA_TEST);
//		GL11.glDisable(GL_TEXTURE_2D);
//
//		glColor4f(1f, .2f, 1f, .4f);
//		glBegin(GL11.GL_LINES);
//		GL11.glNormal3f(0, 0, 0);
//
//		int ySize = 50;
//		int xSize = 50;
//
//
//
//		GL11.glVertex3f(xSize, 0, ySize);
//		GL11.glVertex3f(xSize, 0, -ySize);
//
//		GL11.glVertex3f(xSize, 0, -ySize);
//		GL11.glVertex3f(-xSize, 0, -ySize);
//
//		GL11.glVertex3f(-xSize, 0, -ySize);
//		GL11.glVertex3f(-xSize, 0, ySize);
//
//		GL11.glVertex3f(-xSize, 0, ySize);
//		GL11.glVertex3f(xSize, 0, ySize);
//
//
//		glEnd();
		vp.repaint();
//		vp2.getGraphics().setColor(Color.MAGENTA);
//		vp2.getGraphics().fillRect((int) 00, (int) 00, (int) (3000), (int) (3000));
	}

	public void zoom(double v) {
		if (v > 0) {
			vp.getCoordinateSystem().zoomIn(v);
		} else {
			vp.getCoordinateSystem().zoomOut(-v);

		}
		System.out.println("ugg");
		view.setName("ugg");
		view.getViewProperties().setTitle("tugg");
		View frontView = ProgramGlobals.getMainPanel().getMainLayoutCreator().getFrontView();
//		frontView.getViewProperties().setTitle("puss");
		frontView.setName("woop");
//		vp.zoom(v);
		vp.repaint();
	}

	public void panLeftRight(int i) {
		vp.getCoordinateSystem().translateZoomed(i, 0);
//		vp.translate((i / vp.getZoom()), 0);
		vp.repaint();
	}

	public void panUpDown(int i) {
		vp.getCoordinateSystem().translateZoomed(0, i);
//		vp.translate(0, (i / vp.getZoom()));
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
