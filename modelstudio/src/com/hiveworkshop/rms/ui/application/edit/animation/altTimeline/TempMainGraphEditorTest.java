package com.hiveworkshop.rms.ui.application.edit.animation.altTimeline;

import com.hiveworkshop.rms.editor.model.animflag.Entry;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TempMainGraphEditorTest extends JPanel{
	private final JFrame parentFrame;
	TempGraphHolder graphHolder;

	public TempMainGraphEditorTest(final JFrame parentFrame) {
		setLayout(new MigLayout("fill, gap 0", "[700]", "[500]"));
		this.parentFrame = parentFrame;
		TempGraphBGPanel bgPanel = new TempGraphBGPanel();
		add(bgPanel, "grow");
		graphHolder = new TempGraphHolder();
		graphHolder.addGraph(new TempFloatGraph(this::valueThing, new int[] {0, 50, 100}));
		MouseAdapter ma = getMA(graphHolder);
		this.addMouseListener(ma);
		this.addMouseMotionListener(ma);
	}

	private float valueThing(int t){
		return t*t/200f + 1.0f;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		graphHolder.paintGraphs(g);
	}

	private MouseAdapter getMA(TempGraphHolder graphHolder){
		return new MouseAdapter() {
			TempFloatGraph mousePointGraph;
			Entry<Float> entry;
			Integer startX;
			Integer startY;
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				super.mousePressed(e);
				TempFloatGraph oldPointGraph = mousePointGraph;
				mousePointGraph = graphHolder.getMousePointGraph(e);
				if(mousePointGraph != null){
					entry = mousePointGraph.pointIsOnValue(e);
					mousePointGraph.setSelected(entry);
					startX = e.getX();
					startY = e.getY();
					graphHolder.setDragPoint(e.getX(), (float) e.getY());
				}
				if(oldPointGraph != null && oldPointGraph != mousePointGraph){
					oldPointGraph.setSelected(null);
					repaint();
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				System.out.println("mouse released: [" + e.getX() + ", " + e.getY() + "]");
				if(mousePointGraph != null){
					System.out.println("on point!");
					mousePointGraph.shiftPoint(startX, e.getX(), e.getY());
					startX = null;
					startY = null;
					graphHolder.setDragPoint(null, null);
					entry = null;
					repaint();
				}
				super.mouseReleased(e);
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				super.mouseDragged(e);
				if(mousePointGraph != null && entry != null){
					System.out.println("dragging!");
					graphHolder.setDragPoint(e.getX(), (float) e.getY());
					repaint();
				}
			}
		};
	}

	public static void main(final String[] args) {
		try {
			// Set cross-platform Java L&F (also called "Metal")
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final UnsupportedLookAndFeelException
		               | ClassNotFoundException
		               | InstantiationException
		               | IllegalAccessException e) {
			// handle exception
		}

		final JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		final TempMainGraphEditorTest transfer = new TempMainGraphEditorTest(frame);
		frame.setContentPane(transfer);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}