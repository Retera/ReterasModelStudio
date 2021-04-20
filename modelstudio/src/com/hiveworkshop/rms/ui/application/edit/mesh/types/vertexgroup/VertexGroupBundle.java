package com.hiveworkshop.rms.ui.application.edit.mesh.types.vertexgroup;

import com.hiveworkshop.rms.editor.model.Geoset;

public class VertexGroupBundle {
	private Geoset geoset;
	private int vertexGroupId;

	public VertexGroupBundle(Geoset geoset, int vertexGroupId) {
		this.geoset = geoset;
		this.vertexGroupId = vertexGroupId;
	}

	public Geoset getGeoset() {
		return geoset;
	}

	public int getVertexGroupId() {
		return vertexGroupId;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = (prime * result) + ((geoset == null) ? 0 : geoset.hashCode());
		result = (prime * result) + vertexGroupId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		VertexGroupBundle other = (VertexGroupBundle) obj;
		if (geoset == null) {
			if (other.geoset != null) {
				return false;
			}
		} else if (!geoset.equals(other.geoset)) {
			return false;
		}
		return vertexGroupId == other.vertexGroupId;
	}

}