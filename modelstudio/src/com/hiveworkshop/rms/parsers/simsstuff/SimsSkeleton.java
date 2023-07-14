package com.hiveworkshop.rms.parsers.simsstuff;

import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public class SimsSkeleton {
	Map<String, SimsBone> skinBoneMap = new LinkedHashMap();
	Map<SimsBone, Vec3> newPosMap = new HashMap();
	Map<SimsBone, List<SimsBone>> parentChildsMap = new HashMap();

	public SimsSkeleton() {
		float scaleFactor = 1f;
		float scaleFactor3 = 1f;
		this.skinBoneMap.put("ROOT", new SimsBone("ROOT", "NULL", new Vec3(-5.88894E-5, 3.01362, 0.00807638).scale(scaleFactor3), (Quat)(new Quat(-0.0262998, -0.706899, 0.0262789, -0.706336)).scale(scaleFactor)));
		this.skinBoneMap.put("PELVIS", new SimsBone("PELVIS", "ROOT", new Vec3(-0.0366778, -1.72489E-8, -2.62766E-12).scale(scaleFactor3), (Quat)(new Quat(0.499999, 0.5, 0.5, -0.500001)).scale(scaleFactor)));
		this.skinBoneMap.put("L_LEG", new SimsBone("L_LEG", "PELVIS", new Vec3(-4.26205E-7, 0.241228, 2.06089E-7).scale(scaleFactor3), (Quat)(new Quat(0.0137635, 2.41525E-6, -0.999905, -3.00579E-7)).scale(scaleFactor)));
		this.skinBoneMap.put("L_LEG1", new SimsBone("L_LEG1", "L_LEG", new Vec3(1.44737, -3.40221E-10, -3.65496E-9).scale(scaleFactor3), (Quat)(new Quat(-2.21297E-11, 0.0357504, -4.92061E-13, -0.999361)).scale(scaleFactor)));
		this.skinBoneMap.put("L_FOOT", new SimsBone("L_FOOT", "L_LEG1", new Vec3(1.31579, 6.72219E-10, 1.85087E-9).scale(scaleFactor3), (Quat)(new Quat(1.18358E-6, -0.0116309, 5.24389E-7, -0.999932)).scale(scaleFactor)));
		this.skinBoneMap.put("L_TOE0", new SimsBone("L_TOE0", "L_FOOT", new Vec3(0.253839, -4.82293E-9, 0.339987).scale(scaleFactor3), (Quat)(new Quat(-1.9198E-11, -0.707107, 1.91956E-11, -0.707107)).scale(scaleFactor)));
		this.skinBoneMap.put("L_TOE01", new SimsBone("L_TOE01", "L_TOE0", new Vec3(0.251088, -3.00624E-9, -2.91078E-10).scale(scaleFactor3), (Quat)(new Quat(-2.95136E-15, 1.25923E-14, 1.67305E-11, -1.0)).scale(scaleFactor)));
		this.skinBoneMap.put("L_TOE02", new SimsBone("L_TOE02", "L_TOE01", new Vec3(0.056929, -3.05933E-9, 3.05676E-10).scale(scaleFactor3), (Quat)(new Quat(7.81281E-17, -5.38587E-15, -1.04296E-11, -1.0)).scale(scaleFactor)));
		this.skinBoneMap.put("R_LEG", new SimsBone("R_LEG", "PELVIS", new Vec3(2.44632E-7, -0.241228, -4.62491E-7).scale(scaleFactor3), (Quat)(new Quat(0.0165402, 5.5519E-7, -0.999863, -1.00484E-6)).scale(scaleFactor)));
		this.skinBoneMap.put("R_LEG1", new SimsBone("R_LEG1", "R_LEG", new Vec3(1.44737, 1.03675E-8, 8.03626E-9).scale(scaleFactor3), (Quat)(new Quat(-2.11848E-11, 0.0298356, 8.86403E-13, -0.999555)).scale(scaleFactor)));
		this.skinBoneMap.put("R_FOOT", new SimsBone("R_FOOT", "R_LEG1", new Vec3(1.31579, 1.12555E-8, -6.11749E-9).scale(scaleFactor3), (Quat)(new Quat(-6.65177E-7, -0.00860503, -3.73375E-7, -0.999963)).scale(scaleFactor)));
		this.skinBoneMap.put("R_TOE0", new SimsBone("R_TOE0", "R_FOOT", new Vec3(0.253839, -4.72826E-9, 0.339987).scale(scaleFactor3), (Quat)(new Quat(1.97569E-11, -0.707107, -1.97538E-11, -0.707107)).scale(scaleFactor)));
		this.skinBoneMap.put("R_TOE01", new SimsBone("R_TOE01", "R_TOE0", new Vec3(0.251088, -2.92067E-9, -5.78397E-10).scale(scaleFactor3), (Quat)(new Quat(-4.94409E-15, 1.09617E-14, 5.30648E-12, -1.0)).scale(scaleFactor)));
		this.skinBoneMap.put("R_TOE02", new SimsBone("R_TOE02", "R_TOE01", new Vec3(0.056929, -2.8047E-9, 5.1338E-10).scale(scaleFactor3), (Quat)(new Quat(7.98796E-16, 2.99644E-15, -1.37573E-11, -1.0)).scale(scaleFactor)));
		this.skinBoneMap.put("SPINE", new SimsBone("SPINE", "PELVIS", new Vec3(0.381066, 5.29048E-7, -3.34763E-4).scale(scaleFactor3), (Quat)(new Quat(2.10104E-6, -0.0315231, 7.14932E-7, -0.999503)).scale(scaleFactor)));
		this.skinBoneMap.put("SPINE1", new SimsBone("SPINE1", "SPINE", new Vec3(0.421059, -9.27955E-10, -3.3528E-4).scale(scaleFactor3), (Quat)(new Quat(-3.30452E-12, 7.52235E-11, -8.18583E-15, -1.0)).scale(scaleFactor)));
		this.skinBoneMap.put("SPINE2", new SimsBone("SPINE2", "SPINE1", new Vec3(0.421059, -9.33352E-10, -3.35277E-4).scale(scaleFactor3), (Quat)(new Quat(-3.30452E-12, 7.52235E-11, -8.18583E-15, -1.0)).scale(scaleFactor)));
		this.skinBoneMap.put("NECK", new SimsBone("NECK", "SPINE2", new Vec3(0.421097, -8.79169E-10, -3.15105E-4).scale(scaleFactor3), (Quat)(new Quat(-1.2815E-11, -0.0604161, 1.67573E-7, -0.998173)).scale(scaleFactor)));
		this.skinBoneMap.put("HEAD", new SimsBone("HEAD", "NECK", new Vec3(0.398615, -1.0225E-12, -1.18576E-8).scale(scaleFactor3), (Quat)(new Quat(2.9478E-12, -0.0668782, 1.85489E-7, -0.997761)).scale(scaleFactor)));
		this.skinBoneMap.put("L_ARM", new SimsBone("L_ARM", "NECK", new Vec3(4.56552E-7, 0.130263, 3.17027E-4).scale(scaleFactor3), (Quat)(new Quat(-0.618227, -0.783643, 0.0376674, -0.0477436)).scale(scaleFactor)));
		this.skinBoneMap.put("L_ARM1", new SimsBone("L_ARM1", "L_ARM", new Vec3(0.376316, -5.49453E-8, 1.60809E-9).scale(scaleFactor3), (Quat)(new Quat(-0.00849216, -0.0173039, -0.57938, -0.81483)).scale(scaleFactor)));
		this.skinBoneMap.put("L_ARM2", new SimsBone("L_ARM2", "L_ARM1", new Vec3(0.881536, 2.63531E-8, 9.20282E-9).scale(scaleFactor3), (Quat)(new Quat(-8.57358E-11, 0.0500412, 5.23877E-9, -0.998747)).scale(scaleFactor)));
		this.skinBoneMap.put("L_HAND", new SimsBone("L_HAND", "L_ARM2", new Vec3(0.820474, 1.81877E-8, 3.02261E-9).scale(scaleFactor3), (Quat)(new Quat(0.705821, 0.0376602, 0.0376902, -0.706383)).scale(scaleFactor)));
		this.skinBoneMap.put("L_FINGER0", new SimsBone("L_FINGER0", "L_HAND", new Vec3(0.302072, 8.54501E-9, -1.60412E-8).scale(scaleFactor3), (Quat)(new Quat(3.91826E-4, -0.177342, -7.06052E-5, -0.984149)).scale(scaleFactor)));
		this.skinBoneMap.put("R_ARM", new SimsBone("R_ARM", "NECK", new Vec3(4.57471E-7, -0.130263, 3.17749E-4).scale(scaleFactor3), (Quat)(new Quat(0.618227, -0.783643, -0.0376652, -0.0477453)).scale(scaleFactor)));
		this.skinBoneMap.put("R_ARM1", new SimsBone("R_ARM1", "R_ARM", new Vec3(0.376316, 5.23017E-8, 1.85675E-9).scale(scaleFactor3), (Quat)(new Quat(0.00849222, -0.0173038, 0.57938, -0.81483)).scale(scaleFactor)));
		this.skinBoneMap.put("R_ARM2", new SimsBone("R_ARM2", "R_ARM1", new Vec3(0.881536, -1.63993E-8, 5.11895E-9).scale(scaleFactor3), (Quat)(new Quat(1.00755E-10, 0.0500412, -3.82142E-9, -0.998747)).scale(scaleFactor)));
		this.skinBoneMap.put("R_HAND", new SimsBone("R_HAND", "R_ARM2", new Vec3(0.820474, -4.87935E-8, -3.3423E-9).scale(scaleFactor3), (Quat)(new Quat(-0.706605, 0.0176475, -0.0176615, -0.707168)).scale(scaleFactor)));
		this.skinBoneMap.put("R_FINGER0", new SimsBone("R_FINGER0", "R_HAND", new Vec3(0.302072, 1.54253E-10, 6.47549E-8).scale(scaleFactor3), (Quat)(new Quat(-3.94579E-4, -0.133423, 5.31174E-5, -0.991059)).scale(scaleFactor)));
		new Mat4();
		new Mat4();
		new Quat();

		for (SimsBone bone : this.skinBoneMap.values()) {
			if (this.skinBoneMap.containsKey(bone.getParName())) {
				SimsBone parent = this.skinBoneMap.get(bone.getParName());
				this.parentChildsMap.computeIfAbsent(parent, (k) -> new ArrayList<>()).add(bone);
				bone.setParent(parent);
			}
		}

		for (SimsBone bone : this.skinBoneMap.values()) {
			this.newPosMap.computeIfAbsent(bone, (k) -> this.getTotPos(k, new Vec3()));
		}

		for (SimsBone bone : this.skinBoneMap.values()) {
			bone.setTotQuat(this.getTotQuat(bone, new Quat(0.0, 0.0, 0.0, 1.0)));
		}

		this.computeLoc(this.skinBoneMap.get("ROOT"), this.skinBoneMap.get("ROOT").getWorldMat());
	}

	public void computeLoc(SimsBone bone, Mat4 worldMat) {
		bone.calcWorldMat(worldMat);
		if (this.parentChildsMap.containsKey(bone)) {

			for (SimsBone o : this.parentChildsMap.get(bone)) {
				this.computeLoc(o, bone.getWorldMat());
			}
		}

	}

	private Vec3 getTotPos(SimsBone bone, Vec3 totPos) {
		totPos.add(bone.getPos());
		if (bone.getParent() != null) {
			this.getTotPos(bone.getParent(), totPos);
		}

		return totPos;
	}

	private Quat getTotQuat(SimsBone bone, Quat totQuat) {
		if (bone.getParent() != null) {
			this.getTotQuat(bone.getParent(), totQuat);
		}

		totQuat.mul(bone.getQuat());
		return totQuat;
	}

	public SimsBone getBone(String name) {
		return this.skinBoneMap.get(name);
	}

	public Map<String, SimsBone> getSkinBoneMap() {
		return this.skinBoneMap;
	}
}
