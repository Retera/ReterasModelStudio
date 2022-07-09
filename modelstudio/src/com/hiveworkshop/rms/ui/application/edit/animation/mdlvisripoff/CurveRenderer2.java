package com.hiveworkshop.rms.ui.application.edit.animation.mdlvisripoff;

import com.hiveworkshop.rms.editor.model.animflag.Vec3AnimFlag;
import com.hiveworkshop.rms.ui.application.edit.animation.Sequence;
import com.hiveworkshop.rms.util.GU;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import javax.swing.*;
import java.awt.*;

public class CurveRenderer2 extends JPanel {
	Sequence sequence;
	Vec3AnimFlag animFlag;

	Vec2 renderStartPoint = new Vec2();
	Vec2 renderEndPoint = new Vec2();
	float pixPerUnitX;
	float pixPerUnitY;

	Vec2[] curve = new Vec2[101];
	Vec2[] curveX = new Vec2[101];
	Vec2[] curveY = new Vec2[101];
	Vec2[] curveZ = new Vec2[101];
	Vec3 max = new Vec3();
	Vec3 min = new Vec3();

	float zeroHeight = 0;

	float timeStep = 2;

	public CurveRenderer2() {
		for (int i = 0; i < curve.length; i++) {
			curve[i] = new Vec2(i, 0);
		}
		for (int i = 0; i < curveX.length; i++) {
			curveX[i] = new Vec2(i, 0);
		}
		for (int i = 0; i < curveY.length; i++) {
			curveY[i] = new Vec2(i, 0);
		}
		for (int i = 0; i < curveZ.length; i++) {
			curveZ[i] = new Vec2(i, 0);
		}
		setPreferredSize(new Dimension(100, 70));
	}

	public CurveRenderer2 setAnimFlag(Vec3AnimFlag animFlag) {
		this.animFlag = animFlag;
//		makeSplines();
		max.set(0, 0, 0);
		min.set(0, 0, 0);
		return this;
	}

	public CurveRenderer2 setSequence(Sequence sequence) {
		this.sequence = sequence;
		timeStep = sequence.getLength() / 100f;
		max.set(0, 0, 0);
		min.set(0, 0, 0);
		return this;
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Rectangle rect = getBounds();
		int height = rect.height;
		pixPerUnitX = 0.005f * rect.width;
		pixPerUnitY = height / 130f;
		renderStartPoint.set(rect.x, rect.y + height);

//		g.setColor(Color.BLUE);
////		g.drawRect(rect.x, rect.y, rect.width, height);
//		g.drawRect(0, 0, getWidth(), getHeight());

		g.setColor(Color.BLACK);
		g.drawLine(0, (int) zeroHeight, getWidth(), (int) zeroHeight);
//		g.setColor(Color.MAGENTA);
//		g.drawLine(0, (int)zeroHeight/2, getWidth(), (int)zeroHeight/2);
//		g.setColor(Color.cyan);
//		g.drawLine(0, (int)-zeroHeight, getWidth(), (int)-zeroHeight);
//		g.setColor(Color.YELLOW);
//		g.drawLine(0, (int)-zeroHeight/2, getWidth(), (int)-zeroHeight/2);

		if (animFlag != null && !animFlag.hasGlobalSeq()) {
			g.setColor(Color.RED.darker());
			GU.drawLines(g, curveX);
			g.setColor(Color.GREEN.darker());
			GU.drawLines(g, curveY);
			g.setColor(Color.BLUE.darker());
			GU.drawLines(g, curveZ);
//			drawSplines(g);
		} else {
			// Central line
			g.setColor(Color.RED);

			//		int x1 = getEndX(100);
			int x1 = 50;
			//		g.drawLine(x1, height, x1, height - Math.round(pixPerUnitY * 100));
			g.drawLine(x1, 0, x1, height);
		}

//		// Central line
//		g.setColor(Color.RED);
//
////		int x1 = getEndX(100);
//		int x1 = 50;
////		g.drawLine(x1, height, x1, height - Math.round(pixPerUnitY * 100));
//		g.drawLine(x1, 0, x1, height);
	}

	private void drawSplines(Graphics g) {
		GU.drawLines(g, curve);
	}

	public void makeSplines() {
		if (animFlag != null) {
			for (int i = 0; i <= 100; i++) {
				Vec3 vec3 = animFlag.interpolateAt(sequence, (int) (i * timeStep));
				curveX[i].y = vec3.x;
				curveY[i].y = vec3.y;
				curveZ[i].y = vec3.z;
				max.maximize(vec3);
				min.minimize(vec3);
				System.out.println(vec3);
			}
			float heightAdj = (getHeight() - 5) / (max.length() + min.length());
			zeroHeight = min.length() * heightAdj;
			System.out.println(zeroHeight + ", minLength: " + min.length() + ", heightAdj: " + heightAdj + ", height: " + getHeight());
			for (int i = 0; i <= 100; i++) {
				curveX[i].y *= heightAdj;
				curveY[i].y *= heightAdj;
				curveZ[i].y *= heightAdj;
				curveX[i].y += zeroHeight;
				curveY[i].y += zeroHeight;
				curveZ[i].y += zeroHeight;
			}
		}
	}

	public void makeSplinesAt(int time) {
		if (animFlag != null) {
			int timeStart = time-50;
			for (int i = 0; i <= 100; i++) {
				Vec3 vec3 = animFlag.interpolateAt(sequence, (int) (timeStart + i * timeStep));
				curveX[i].y = vec3.x;
				curveY[i].y = vec3.y;
				curveZ[i].y = vec3.z;
				max.maximize(vec3);
				min.minimize(vec3);
				System.out.println(vec3);
			}
			float heightAdj = (getHeight() - 5) / (max.length() + min.length());
			zeroHeight = min.length() * heightAdj;
			System.out.println(zeroHeight + ", minLength: " + min.length() + ", heightAdj: " + heightAdj + ", height: " + getHeight());
			for (int i = 0; i <= 100; i++) {
				curveX[i].y *= heightAdj;
				curveY[i].y *= heightAdj;
				curveZ[i].y *= heightAdj;
				curveX[i].y += zeroHeight;
				curveY[i].y += zeroHeight;
				curveZ[i].y += zeroHeight;
			}
		}
	}

	public void clearCurve() {
		for (int i = 0; i < curve.length; i++) {
			curve[i].set(i, -1);
		}
	}
}
