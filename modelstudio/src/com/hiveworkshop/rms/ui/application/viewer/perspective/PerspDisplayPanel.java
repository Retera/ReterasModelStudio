package com.hiveworkshop.rms.ui.application.viewer.perspective;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.render3d.RenderModel;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.ui.preferences.ProgramPreferences;
import net.infonode.docking.View;
import org.lwjgl.LWJGLException;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Write a description of class DisplayPanel here.
 *
 * @author (your name)
 * @version (a version number or a date)
 */
public class PerspDisplayPanel extends JPanel implements ActionListener {
	private final ModelView dispMDL;
	private PerspectiveViewport vp;
	private JPanel vpp;
	private String title;
	private final JButton up, down, left, right, plusZoom, minusZoom;
	private final ProgramPreferences programPreferences;
	private final View view;
	private final RenderModel editorRenderModel;

	// private JCheckBox wireframe;
	public PerspDisplayPanel(final String title, final ModelView dispMDL, final ProgramPreferences programPreferences,
			final RenderModel editorRenderModel) {
		super();
		this.programPreferences = programPreferences;
		this.editorRenderModel = editorRenderModel;
		// BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(title),BorderFactory.createBevelBorder(1)),BorderFactory.createEmptyBorder(1,1,1,1)
		// ));
		setOpaque(true);

		// wireframe = new JCheckBox("Wireframe");
		// add(wireframe);
		setViewport(dispMDL);
		getViewport().setMinimumSize(new Dimension(200, 200));
		this.title = title;
		this.dispMDL = dispMDL;

		plusZoom = new JButton("");
		Dimension dim = new Dimension(20, 20);
		plusZoom.setMaximumSize(dim);
		plusZoom.setMinimumSize(dim);
		plusZoom.setPreferredSize(dim);
		plusZoom.addActionListener(this);
		// add(plusZoom);

		minusZoom = new JButton("");
		minusZoom.setMaximumSize(dim);
		minusZoom.setMinimumSize(dim);
		minusZoom.setPreferredSize(dim);
		minusZoom.addActionListener(this);
		// add(minusZoom);

		up = new JButton("");
		dim = new Dimension(32, 16);
		up.setMaximumSize(dim);
		up.setMinimumSize(dim);
		up.setPreferredSize(dim);
		up.addActionListener(this);
		// add(up);

		down = new JButton("");
		down.setMaximumSize(dim);
		down.setMinimumSize(dim);
		down.setPreferredSize(dim);
		down.addActionListener(this);
		// add(down);

		dim = new Dimension(16, 32);
		left = new JButton("");
		left.setMaximumSize(dim);
		left.setMinimumSize(dim);
		left.setPreferredSize(dim);
		left.addActionListener(this);
		// add(left);

		right = new JButton("");
		right.setMaximumSize(dim);
		right.setMinimumSize(dim);
		right.setPreferredSize(dim);
		right.addActionListener(this);
		// add(right);

		final GroupLayout layout = new GroupLayout(this);
		layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(vp));
		// .addComponent(wireframe));
		// .addComponent(vp)
		// .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		// .addComponent(plusZoom)
		// .addComponent(minusZoom)
		// .addGroup(layout.createSequentialGroup()
		// .addComponent(left)
		// .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		// .addComponent(up)
		// .addComponent(down))
		// .addComponent(right)))
		// );
		layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(vp));
		// .addComponent(wireframe));
		// .addComponent(vp)
		// .addGroup(layout.createSequentialGroup()
		// .addComponent(plusZoom)
		// .addGap(16)
		// .addComponent(minusZoom)
		// .addGap(16)
		// .addComponent(up)
		// .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
		// .addComponent(left)
		// .addComponent(right))
		// .addComponent(down)
		// ));
		//
		setLayout(new BorderLayout());
		add(vp);
//		add(Box.createHorizontalStrut(200));
//		add(Box.createVerticalStrut(200));
		// setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
		// setLayout(new GridLayout(1,1));
		view = new View(title, null, this);
	}

	public void setViewportBackground(final Color background) {
		vp.setViewportBackground(background);
	}

	public Color getViewportBackground() {
		return vp.getBackground();
	}

	public View getView() {
		return view;
	}

	public void addGeosets(final List<Geoset> list) {
		vp.addGeosets(list);
	}

	public void reloadTextures() {
		vp.reloadTextures();
	}

	public void reloadAllTextures() {
		vp.reloadAllTextures();
	}

	public void setViewport(final ModelView dispModel) {
		setViewport(dispModel, 200);
	}

	public void setViewport(final ModelView dispModel, final int viewerSize) {
		try {
			if (vp != null) {
				vp.destroy();
			}
			removeAll();
			vp = new PerspectiveViewport(dispModel, programPreferences, editorRenderModel);
			vp.setIgnoreRepaint(false);
			vp.setMinimumSize(new Dimension(viewerSize, viewerSize));
			final GroupLayout layout = new GroupLayout(this);
			layout.setHorizontalGroup(layout.createSequentialGroup().addComponent(vp));
			// .addComponent(wireframe));
			// .addComponent(vp)
			// .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			// .addComponent(plusZoom)
			// .addComponent(minusZoom)
			// .addGroup(layout.createSequentialGroup()
			// .addComponent(left)
			// .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			// .addComponent(up)
			// .addComponent(down))
			// .addComponent(right)))
			// );
			layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(vp));
			// .addComponent(wireframe));
			// .addComponent(vp)
			// .addGroup(layout.createSequentialGroup()
			// .addComponent(plusZoom)
			// .addGap(16)
			// .addComponent(minusZoom)
			// .addGap(16)
			// .addComponent(up)
			// .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			// .addComponent(left)
			// .addComponent(right))
			// .addComponent(down)
			// ));
			//
			setLayout(new BorderLayout());
			// vp.setWireframeHandler(wireframe);
			// vpp = new JPanel();
			// vpp.add(Box.createHorizontalStrut(200));
			// vpp.add(Box.createVerticalStrut(200));
			// vpp.setLayout( new BoxLayout(this,BoxLayout.LINE_AXIS));
			// vpp.add(vp);
			// vp.initGL();
		} catch (final LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		add(vp, BorderLayout.CENTER);
	}

	public void setTitle(final String what) {
		title = what;
		setBorder(BorderFactory.createTitledBorder(title));
	}

	public PerspectiveViewport getViewport() {
		return vp;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		vp.paint(vp.getGraphics());
		// g.drawString(title,3,3);
		// vp.repaint();
	}

	// public void addGeoset(Geoset g)
	// {
	// m_geosets.add(g);
	// }
	// public void setGeosetVisible(int index, boolean flag)
	// {
	// Geoset geo = (Geoset)m_geosets.get(index);
	// geo.setVisible(flag);
	// }
	// public void setGeosetHighlight(int index, boolean flag)
	// {
	// Geoset geo = (Geoset)m_geosets.get(index);
	// geo.setHighlight(flag);
	// }
	// public void clearGeosets()
	// {
	// m_geosets.clear();
	// }
	// public int getGeosetsSize()
	// {
	// return m_geosets.size();
	// }
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
}
