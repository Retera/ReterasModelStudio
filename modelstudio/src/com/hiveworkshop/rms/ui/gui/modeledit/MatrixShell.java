package com.hiveworkshop.rms.ui.gui.modeledit;

import java.util.ArrayList;

import com.hiveworkshop.rms.editor.model.Matrix;

public class MatrixShell {
	Matrix matrix;
	ArrayList<BoneShell> newBones = new ArrayList<>();

	public MatrixShell(final Matrix m) {
		matrix = m;
		newBones = new ArrayList<>();
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