package com.hiveworkshop.rms.parsers.simsstuff;

import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Quat;
import com.hiveworkshop.rms.util.Vec3;

public class SimsBone {
	private String name;
	private String parName;
	private SimsBone parent;
	private Vec3 pos = new Vec3();
	private Quat quat = new Quat();
	private float scaleFactor = 20.0F;
	private Quat totQuat = new Quat();
	private int index;
	private int rawVertexIndex;
	private int rawVertexCount;
	private int blendedVertexIndex;
	private int blendedVertexCount;
	private final Mat4 localMat = new Mat4();
	private final Vec3 worldLoc = new Vec3();
	private final Mat4 worldMat = (new Mat4()).setIdentity();

	public Quat getTotQuat() {
		return this.totQuat;
	}

	public SimsBone setTotQuat(Quat totQuat) {
		this.totQuat = totQuat;
		return this;
	}

	public SimsBone(int index, String name) {
		this.name = name;
		this.index = index;
	}

	public SimsBone(String name, String parName, Vec3 pos, Quat quat) {
		this.name = name;
//		this.quat.set(quat.x, -quat.y, -quat.z, -quat.w);
//		this.pos.set(-pos.x, pos.y, pos.z);
		this.quat.set(quat.x, quat.y, quat.z, quat.w);
		this.pos.set(pos.x, pos.y, pos.z);
		this.pos.scale(this.scaleFactor);
		this.parName = parName;
		Mat4 translateMatrix = (new Mat4()).fromRotationTranslationScale(Quat.IDENTITY, this.pos, Vec3.ONE);
		Mat4 rotateMatrix = (new Mat4()).fromRotationTranslationScale(this.quat, Vec3.ZERO, Vec3.ONE);
		this.localMat.set(translateMatrix).mul(rotateMatrix);
	}

	public SimsBone setStuff(int rawVertexIndex, int RawVertexCount, int blendedVertexIndex, int blendedVertexCount) {
		this.name = this.name;
		return this;
	}

	public SimsBone setStuff(String s) {
		String[] split = s.split(" ");
		this.rawVertexIndex = Integer.parseInt(split[1]);
		this.rawVertexCount = Integer.parseInt(split[2]);
		this.blendedVertexIndex = Integer.parseInt(split[3]);
		this.blendedVertexCount = Integer.parseInt(split[4]);
		return this;
	}

	public SimsBone setStuff(LineReaderThingi lineReaderThingi) {
		int[] ints = lineReaderThingi.readInts();
		this.rawVertexIndex = ints[1];
		this.rawVertexCount = ints[2];
		this.blendedVertexIndex = ints[3];
		this.blendedVertexCount = ints[4];
		return this;
	}

	public Quat getQuat() {
		return this.quat;
	}

	public SimsBone setQuat(Quat quat) {
		this.quat = quat;
		return this;
	}

	public SimsBone setParName(String parName) {
		this.parName = parName;
		return this;
	}

	public String getParName() {
		return this.parName;
	}

	public SimsBone getParent() {
		return this.parent;
	}

	public SimsBone setParent(SimsBone parent) {
		this.parent = parent;
		return this;
	}

	public Vec3 getPos() {
		return this.pos;
	}

	public SimsBone setPos(Vec3 pos) {
		this.pos = pos;
		return this;
	}

	public Vec3 getWorldLoc() {
		return this.worldLoc;
	}

	public SimsBone setLocalMat(Mat4 localMat) {
		this.localMat.set(localMat);
		return this;
	}

	public Mat4 getLocalMat() {
		return this.localMat;
	}

	public SimsBone setWorldMat(Mat4 worldMat) {
		this.worldMat.set(worldMat);
		return this;
	}

	public SimsBone calcWorldMat(Mat4 parentWorldMat) {
		this.worldMat.set(parentWorldMat).mul(this.localMat);
		this.worldLoc.set(Vec3.ZERO).transform(this.worldMat);
		return this;
	}

	public Mat4 getWorldMat() {
		return this.worldMat;
	}

	public String getName() {
		return this.name;
	}

	public SimsBone setName(String name) {
		this.name = name;
		return this;
	}

	public int getIndex() {
		return this.index;
	}

	public SimsBone setIndex(int index) {
		this.index = index;
		return this;
	}

	public int getRawVertexIndex() {
		return this.rawVertexIndex;
	}

	public SimsBone setRawVertexIndex(int rawVertexIndex) {
		this.rawVertexIndex = rawVertexIndex;
		return this;
	}

	public int getRawVertexCount() {
		return this.rawVertexCount;
	}

	public SimsBone setRawVertexCount(int rawVertexCount) {
		this.rawVertexCount = rawVertexCount;
		return this;
	}

	public int getBlendedVertexIndex() {
		return this.blendedVertexIndex;
	}

	public SimsBone setBlendedVertexIndex(int blendedVertexIndex) {
		this.blendedVertexIndex = blendedVertexIndex;
		return this;
	}

	public int getBlendedVertexCount() {
		return this.blendedVertexCount;
	}

	public SimsBone setBlendedVertexCount(int blendedVertexCount) {
		this.blendedVertexCount = blendedVertexCount;
		return this;
	}

	private String getParName1() {
		return switch (this.name) {
			case "ROOT" -> "NULL";
			case "PELVIS" -> "ROOT";
			case "L_LEG" -> "PELVIS";
			case "L_LEG1" -> "L_LEG";
			case "L_FOOT" -> "L_LEG1";
			case "L_TOE0" -> "L_FOOT";
			case "L_TOE01" -> "L_TOE0";
			case "L_TOE02" -> "L_TOE01";
			case "R_LEG" -> "PELVIS";
			case "R_LEG1" -> "R_LEG";
			case "R_FOOT" -> "R_LEG1";
			case "R_TOE0" -> "R_FOOT";
			case "R_TOE01" -> "R_TOE0";
			case "R_TOE02" -> "R_TOE01";
			case "SPINE" -> "PELVIS";
			case "SPINE1" -> "SPINE";
			case "SPINE2" -> "SPINE1";
			case "NECK" -> "SPINE2";
			case "HEAD" -> "NECK";
			case "L_ARM" -> "NECK";
			case "L_ARM1" -> "L_ARM";
			case "L_ARM2" -> "L_ARM1";
			case "L_HAND" -> "L_ARM2";
			case "L_FINGER0" -> "L_HAND";
			case "R_ARM" -> "NECK";
			case "R_ARM1" -> "R_ARM";
			case "R_ARM2" -> "R_ARM1";
			case "R_HAND" -> "R_ARM2";
			case "R_FINGER0" -> "R_HAND";
			default -> "";
		};
	}

	public String bindingString() {
		return this.index + " " + this.rawVertexIndex + " " + this.rawVertexCount + " " + this.blendedVertexIndex + " " + this.blendedVertexCount;
	}
}