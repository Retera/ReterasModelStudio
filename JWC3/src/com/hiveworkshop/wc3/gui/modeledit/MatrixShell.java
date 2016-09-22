package com.hiveworkshop.wc3.gui.modeledit;

import java.util.ArrayList;

import com.hiveworkshop.wc3.mdl.Matrix;

public class MatrixShell {
	Matrix matrix;
	ArrayList<BoneShell> newBones = new ArrayList<BoneShell>();

	public MatrixShell(final Matrix m) {
		matrix = m;
		newBones = new ArrayList<BoneShell>();
	}

	public Matrix getMatrix() {
		return matrix;
	}

	public ArrayList<BoneShell> getNewBones() {
		return newBones;
	}

	@Override
	public String toString() {
		return matrix.getName();
	}
}