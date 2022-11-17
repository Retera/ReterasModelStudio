package com.hiveworkshop.rms.ui.gui.modeledit.selection;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Matrix;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class VertexGroupBundle {
	private final Geoset geoset;
	private final Matrix matrix;

	public VertexGroupBundle(Geoset geoset, Matrix vertexMatrix) {
		this.geoset = geoset;
		this.matrix = vertexMatrix;
	}

	public Geoset getGeoset() {
		return geoset;
	}


	public Matrix getMatrix() {
		return matrix;
	}

	public boolean sameMatrix(Matrix vertexMatrix){
		return Objects.equals(matrix, vertexMatrix);
	}

	public Set<GeosetVertex> getBundleVerts(){
		Set<GeosetVertex> verticesSelected = new HashSet<>();
		for (GeosetVertex geosetVertex : geoset.getVertices()) {
			if (Objects.equals(geosetVertex.getMatrix(), matrix)) {
				verticesSelected.add(geosetVertex);
			}
		}
		return verticesSelected;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + ((geoset == null) ? 0 : geoset.hashCode());
		result = (prime * result) + matrix.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof VertexGroupBundle) {
			VertexGroupBundle other = (VertexGroupBundle) obj;
			return Objects.equals(geoset, other.geoset) && Objects.equals(matrix, other.matrix);
		}
		return false;
	}

}