package com.hiveworkshop.rms.editor.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BindPose {
	public float[][] bindPose = null;

	public BindPose(int count) {
		this.bindPose = new float[count][];
	}

	public BindPose(List<float[]> matrices) {
		this.bindPose = new float[matrices.size()][];

		for (int i = 0, l = matrices.size(); i < l; i++) {
			bindPose[i] = matrices.get(i);
		}
	}

	public List<float[]> toMdlx() {

		return new ArrayList<>(Arrays.asList(bindPose));
	}
}
