package com.hiveworkshop.rms.editor.model.util;

import com.hiveworkshop.rms.editor.model.animflag.Entry;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

public class MotionGenerator {

	public static List<Entry<Quat>> getRotation(int start, int length, double degAngle, Vec3 axis, Quat initialRot) {
		initialRot = initialRot == null ? new Quat() : initialRot;
		int numKF = (int) ((Math.abs(degAngle) + 179.999) / 180);
		List<Entry<Quat>> rotEntries = new ArrayList<>(numKF+1);

		float kfSpaceing = length / (float) numKF;
		double degPerKf = degAngle / numKF;

		Quat rot = new Quat(axis, (float) Math.toRadians(degPerKf));
		Quat currRot = new Quat(initialRot);

		for (float t = start; t < start + length; t += kfSpaceing) {
			rotEntries.add(new Entry<>((int) t, new Quat(currRot)));
			currRot.mul(rot).normalize();
		}

		rotEntries.add(new Entry<>(start + length, new Quat(currRot)));
		return rotEntries;
	}

	public static List<Entry<Vec3>> getMoveAndHold(int start, int length, int holdTime, Vec3 move, Vec3 initialPos, boolean addStartFrame) {
		initialPos = initialPos == null ? new Vec3() : initialPos;

		int numKF = addStartFrame ? 3 : 2;
		List<Entry<Vec3>> moveEntries = new ArrayList<>(numKF);
		if (addStartFrame) {
			moveEntries.add(new Entry<>(start, new Vec3(initialPos)));
		}
		moveEntries.add(new Entry<>(start+length-holdTime, new Vec3(initialPos).add(move)));
		moveEntries.add(new Entry<>(start+length, new Vec3(initialPos).add(move)));

		return moveEntries;
	}
}
