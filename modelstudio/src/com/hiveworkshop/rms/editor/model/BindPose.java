package com.hiveworkshop.rms.editor.model;

import java.util.ArrayList;
import java.util.List;

public class BindPose {
	private final ArrayList<float[]> bindPoses = new ArrayList<>();

	public BindPose() {
	}

	public BindPose(List<float[]> matrices) {
		bindPoses.addAll(matrices);
	}

	public ArrayList<float[]> getBindPoses() {
		return bindPoses;
	}
	public float[] getBindPose(int i) {
		return bindPoses.get(i);
	}
	public BindPose setBindPose(int i, float[] bp) {
		bindPoses.set(i, bp);
		return this;
	}
	public BindPose addBindPose(float[] bp){
		bindPoses.add(bp);
		return this;
	}

	public int getSize(){
		return bindPoses.size();
	}

	public List<float[]> toMdlx() {
		return new ArrayList<>(bindPoses);
	}
}
