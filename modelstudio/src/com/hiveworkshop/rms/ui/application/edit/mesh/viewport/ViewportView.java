package com.hiveworkshop.rms.ui.application.edit.mesh.viewport;

import com.hiveworkshop.rms.ui.application.ProgramGlobals;
import com.hiveworkshop.rms.ui.application.edit.mesh.activity.ViewportActivityManager;
import com.hiveworkshop.rms.ui.application.edit.mesh.viewport.axes.CoordinateSystem;
import com.hiveworkshop.rms.ui.gui.modeledit.ModelHandler;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.util.function.BiConsumer;

public abstract class ViewportView extends JPanel {
	protected final CoordinateSystem coordinateSystem;
	protected Point lastMouseMotion = new Point(0, 0);
	protected ModelHandler modelHandler;
	protected ViewportActivityManager viewportActivityManager;
	protected MouseListenerThing2 mouseListenerThing2;
	private final Timer repaintTimer = new Timer(16, e -> repaintTimer());
	private Color weakLineColor = new Color(80, 80, 80, 100);
	private Color mediumLineColor = new Color(80, 80, 80, 150);
	private Color strongLineColor = new Color(0,0,0, 255);
	private Color xLineColor = new Color(200,20,20, 255);
	private Color yLineColor = new Color(20,200,20, 255);

	public ViewportView(Vec3 right, Vec3 up,
	                    BiConsumer<Double, Double> coordDisplayListener2) {

		coordinateSystem = new CoordinateSystem(right, up, this);

		setBorder(BorderFactory.createBevelBorder(1));
		setBackground(ProgramGlobals.getPrefs().getBackgroundColor());

		mouseListenerThing2 = new MouseListenerThing2(this, coordDisplayListener2, coordinateSystem);
		addMouseListener(mouseListenerThing2);
		addMouseWheelListener(mouseListenerThing2);
		addMouseMotionListener(mouseListenerThing2);
		addFocusListener(getFocusAdapter());

		addComponentListener(getComponentAdapter());
	}

	private FocusAdapter getFocusAdapter() {
		return new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				super.focusGained(e);

				if (!repaintTimer.isRunning()) {
					repaintTimer.start();
				}
				setTimerDelay(16);
			}

			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				if (repaintTimer.isRunning()) {
					setTimerDelay(200);
				}
			}
		};
	}

	private ComponentAdapter getComponentAdapter() {
		return new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				super.componentResized(e);
//				repaint();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				super.componentMoved(e);
//				System.out.println("moved");
			}

			@Override
			public void componentShown(ComponentEvent e) {
				super.componentShown(e);

				if (!repaintTimer.isRunning()) {
					repaintTimer.start();
				}
				System.out.println("Shown!!");
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				super.componentHidden(e);
				if (repaintTimer.isRunning()) {
					repaintTimer.stop();
				}
				System.out.println("hidden!!");
			}
		};
	}

	private boolean repaintTimer() {
		if(isShowing()){
			repaint();
		}
		if(!isShowing() && repaintTimer.isRunning()){
			repaintTimer.stop();
		}
		return false;
	}

	private void setTimerDelay(int delay){
		repaintTimer.setDelay(delay);
	}
	public void setModel(ModelHandler modelHandler, ViewportActivityManager viewportActivityManager) {
		this.modelHandler = modelHandler;
		this.viewportActivityManager = viewportActivityManager;

		mouseListenerThing2.setActivityManager(viewportActivityManager);
	}

	public CoordinateSystem getCoordinateSystem() {
		return coordinateSystem;
	}

	private final Vec2 originInScreenSpace = new Vec2();
	private final Vec2 stepSize = new Vec2();
	public void drawGrid(Graphics g) {
		originInScreenSpace.set(coordinateSystem.viewV(0,0));

		Vec2 stepSize = getFixedStepSize(coordinateSystem.viewV(1,1).sub(originInScreenSpace));

		g.setColor(weakLineColor);
		drawXLines(g, originInScreenSpace.x, stepSize.x/10f);
		drawYLines(g, originInScreenSpace.y, stepSize.y/10f);

		g.setColor(mediumLineColor);
		drawXLines(g, originInScreenSpace.x, stepSize.x);
		drawYLines(g, originInScreenSpace.y, stepSize.y);

		g.setColor(strongLineColor);
		drawXLines(g, originInScreenSpace.x, stepSize.x*10f);
		drawYLines(g, originInScreenSpace.y, stepSize.y*10f);

		g.setColor(xLineColor);
		g.drawLine(0, (int) originInScreenSpace.y, getWidth(), (int) originInScreenSpace.y);
		g.setColor(yLineColor);
		g.drawLine((int) originInScreenSpace.x, 0, (int) originInScreenSpace.x, getHeight());

//		System.out.println("--drawn grid");
	}

	private Vec2 getFixedStepSize(Vec2 halfScreenSize) {
		stepSize.set(Math.abs(halfScreenSize.x), Math.abs(halfScreenSize.y));

		if (stepSize.x < .0001 || stepSize.y < .0001) {
			stepSize.set(100, 100);
		}

		int v = (int)Math.log10(stepSize.x);
		if (v != 2) {
			stepSize.scale((float) Math.pow(10, 2-v));
		}
		return stepSize;
	}

	private void drawXLines(Graphics g, float startX, float distanceX) {
		float startOffsetX = getStart(startX, distanceX);
		for (float x = startOffsetX;  x < getWidth(); x += distanceX) {
			g.drawLine((int)  x, 0, (int) x, getHeight());
		}
	}

	private void drawYLines(Graphics g, float startY, float distanceY) {
		float startOffsetY = getStart(startY, distanceY);
		for (float y = startOffsetY; y < getHeight(); y += distanceY) {
			g.drawLine(0, (int) y, getWidth(), (int) y);
		}
	}

	private float getStart(float orign, float realStep){
		int stepDiff = (int)(orign / realStep)+1;
		return orign - realStep * stepDiff;
	}

	public BufferedImage getBufferedImage() {
		BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
		super.paintComponent(image.getGraphics());
		paintComponent(image.getGraphics(), 5);
		return image;
	}

	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		paintComponent(g, 1);
	}

	public abstract void paintComponent(final Graphics g, final int vertexSize);
}
