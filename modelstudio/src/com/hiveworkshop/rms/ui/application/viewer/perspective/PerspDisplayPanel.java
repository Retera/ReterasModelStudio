package com.hiveworkshop.rms.ui.application.viewer.perspective;

import com.hiveworkshop.rms.ui.application.viewer.PerspectiveViewport;
import com.hiveworkshop.rms.ui.application.viewer.ViewportRenderExporter;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
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
public class PerspDisplayPanel extends JPanel {
	private PerspectiveViewport vp;
	private String title;

	public PerspDisplayPanel(String title) {
		super(new BorderLayout());
		setOpaque(true);

		try {
			vp = new PerspectiveViewport();
			vp.setIgnoreRepaint(false);
			vp.setMinimumSize(new Dimension(200, 200));
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		this.title = title;

//		getButtonPanel();

		add(vp);
	}

	public void reloadTextures() {
		vp.reloadTextures();
	}

	public void reloadAllTextures() {
		vp.reloadAllTextures();
	}

	public PerspDisplayPanel setModel(ModelHandler modelHandler) {
		vp.setModel(modelHandler.getModelView(), modelHandler.getRenderModel(), false);
//		setModel(modelHandler, 200);
		return this;
	}

//	public void setModel(ModelHandler modelHandler, int viewerSize) {
//		vp.setModel(modelHandler.getModelView(), modelHandler.getRenderModel(), false);
////		try {
////			if (vp != null) {
////				vp.destroy();
////			}
////			removeAll();
////			vp = new PerspectiveViewport();
////			vp.setModel(modelHandler.getModelView(), modelHandler.getRenderModel(), false);
//////			vp.setIgnoreRepaint(false);
//////			vp.setMinimumSize(new Dimension(viewerSize, viewerSize));
////		} catch (LWJGLException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
//	}

	public void setTitle(String what) {
		title = what;
		setBorder(BorderFactory.createTitledBorder(title));
	}

	public PerspectiveViewport getViewport() {
		return vp;
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		vp.paint(vp.getGraphics());
		// g.drawString(title,3,3);
		// vp.repaint();
	}


	private void getButtonPanel() {
		JPanel buttonPanel = new JPanel(new MigLayout(""));
		JButton plusZoom = getButton(e -> zoom(.15), 20, 20);
		buttonPanel.add(plusZoom, "wrap");
		JButton minusZoom = getButton(e -> zoom(-.15), 20, 20);
		buttonPanel.add(minusZoom, "wrap");
		JButton up = getButton(e -> translateViewUpDown(20), 32, 16);
		buttonPanel.add(up, "wrap");
		JButton down = getButton(e -> translateViewUpDown(-20), 32, 16);
		buttonPanel.add(down, "wrap");
		JButton left = getButton(e -> translateViewLeftRight(20), 16, 32);
		buttonPanel.add(left, "wrap");
		JButton right = getButton(e -> translateViewLeftRight(-20), 16, 32);
		buttonPanel.add(right, "wrap");
	}

	private static JButton getButton(ActionListener actionListener, int width, int height) {
		Dimension dim = new Dimension(width, height);
		JButton button = new JButton("");
		button.setMaximumSize(dim);
		button.setMinimumSize(dim);
		button.setPreferredSize(dim);
		button.addActionListener(actionListener);
		return button;
	}
	public void zoom(double v) {
		vp.zoom(v);
		vp.repaint();
	}

	public void translateViewLeftRight(int i) {
		vp.translate((i * (1 / vp.getZoomAmount())), 0);
		vp.repaint();
	}

	public void translateViewUpDown(int i) {
		vp.translate(0, (i * (1 / vp.getZoomAmount())));
		vp.repaint();
	}

	public ImageIcon getImageIcon() {
		return new ImageIcon(ViewportRenderExporter.getBufferedImage(vp));
	}

	public BufferedImage getBufferedImage() {
		return ViewportRenderExporter.getBufferedImage(vp);
	}
}
