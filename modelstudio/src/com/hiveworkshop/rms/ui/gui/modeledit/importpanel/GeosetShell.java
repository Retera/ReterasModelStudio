package com.hiveworkshop.rms.ui.gui.modeledit.importpanel;

import com.hiveworkshop.rms.editor.model.EditableModel;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.Material;

import java.util.List;

public class GeosetShell {
	private final String modelName;
	private String name;
	private int index;
	private final boolean isFromDonating;
	private final Geoset geoset;
	private boolean doImport = true;
	private Material oldMaterial;
	private Material newMaterial;
	private boolean hasSkinBones = false;
	private List<MatrixShell> matrixShells;

	public GeosetShell(Geoset geoset, EditableModel model, boolean isFromDonating) {
		this.geoset = geoset;
		this.isFromDonating = isFromDonating;
		modelName = model.getName();
		if (geoset != null) {
			name = geoset.getName();
			index = model.getGeosetId(geoset);
			oldMaterial = geoset.getMaterial();
			hasSkinBones = !geoset.getVertices().isEmpty() && geoset.getVertex(0).getSkinBones() != null;
		}
	}

	public GeosetShell(Geoset geoset, String modelName, int index, boolean isFromDonating) {
		this.geoset = geoset;
		this.isFromDonating = isFromDonating;
		this.modelName = modelName;
		if (geoset != null) {
			name = geoset.getName();
			this.index = index;
			oldMaterial = geoset.getMaterial();
		}
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public boolean isDoImport() {
		return doImport;
	}

	public GeosetShell setDoImport(boolean doImport) {
		this.doImport = doImport;
		return this;
	}

	public Material getMaterial() {
		if (newMaterial != null) {
			return newMaterial;
		}
		return oldMaterial;
	}

	public Material getOldMaterial() {
		return oldMaterial;
	}


	@Override
	public String toString() {
		if (geoset != null) {
			return modelName + ": " + geoset.getName();
		}
		return modelName + ": none";
	}

	public String getName() {
		return name;
	}

	public GeosetShell setName(String name) {
		this.name = name;
		return this;
	}

	public int getIndex() {
		return index;
	}

	public GeosetShell setIndex(int index) {
		this.index = index;
		return this;
	}

	public boolean isFromDonating() {
		return isFromDonating;
	}

	public Material getNewMaterial() {
		return newMaterial;
	}

	public GeosetShell setNewMaterial(Material newMaterial) {
		this.newMaterial = newMaterial;
		return this;
	}

	public String getModelName() {
		return modelName;
	}

	public boolean hasSkinBones() {
		return hasSkinBones;
	}

	public List<MatrixShell> getMatrixShells() {
		return matrixShells;
	}

	public GeosetShell setMatrixShells(List<MatrixShell> matrixShells) {
		this.matrixShells = matrixShells;
		return this;
	}


}
