package com.hiveworkshop.rms.editor.actions.uv;

import com.hiveworkshop.rms.editor.actions.UndoAction;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.util.Mat4;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UVRemapAction implements UndoAction {

	private final List<Vec2> tVertices;
	private final List<Vec2> newValueHolders;
	private final List<Vec2> oldValueHolders;
	private final String direction;
	private final Collection<GeosetVertex> selectedVertices;
	private final int uvLayerIndex;
	private final Mat4 transformDim;
	private final Mat4 viewPortMat;
	private final boolean keepSize;

	public UVRemapAction(Collection<GeosetVertex> selectedVertices, int uvLayerIndex, Mat4 transformDim, Mat4 viewPortMat, String direction, boolean keepSize) {
		this.selectedVertices = selectedVertices;
		this.uvLayerIndex = uvLayerIndex;
		this.tVertices = new ArrayList<>();
		this.newValueHolders = new ArrayList<>();
		this.oldValueHolders = new ArrayList<>();
		this.direction = direction;
		this.transformDim = transformDim;
		this.viewPortMat = viewPortMat;
		this.keepSize = keepSize;
		remapKeepSize();
	}

	@Override
	public UndoAction undo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).set(oldValueHolders.get(i));
		}
		return this;
	}

	@Override
	public UndoAction redo() {
		for (int i = 0; i < tVertices.size(); i++) {
			tVertices.get(i).set(newValueHolders.get(i));
		}
		return this;
	}

	@Override
	public String actionName() {
		return "remap TVertices " + direction;
	}

	private void remapKeepSize(){
		Vec2 minOld = new Vec2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		Vec2 maxOld = new Vec2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		Vec2 min = new Vec2(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		Vec2 max = new Vec2(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

		for (Vec3 vertex : selectedVertices) {
			if (vertex instanceof GeosetVertex) {
				GeosetVertex geosetVertex = (GeosetVertex) vertex;
				if (uvLayerIndex < geosetVertex.getTverts().size()) {
					Vec2 modelDataTVertex = geosetVertex.getTVertex(uvLayerIndex);
					maxOld.maximize(modelDataTVertex);
					minOld.minimize(modelDataTVertex);

					tVertices.add(modelDataTVertex);
					oldValueHolders.add(new Vec2(modelDataTVertex));

					Vec2 newCoordValue = getProjected(vertex, transformDim, viewPortMat);
					max.maximize(newCoordValue);
					min.minimize(newCoordValue);
					newValueHolders.add(newCoordValue);
				}
			}
		}

		Vec2 uvIlandSpan = Vec2.getDif(max, min);
		Vec2 uvIlandSpanOld = Vec2.getDif(maxOld, minOld);

		if (uvIlandSpan.x == 0) {
			uvIlandSpan.x = 0.00001f;
		}
		if (uvIlandSpan.y == 0) {
			uvIlandSpan.y = 0.00001f;
		}

		for (Vec2 tv : newValueHolders) {
			tv.sub(min).div(uvIlandSpan);
			if(keepSize){
				tv.mul(uvIlandSpanOld).add(minOld);
			}
		}
	}

	private final Vec3 vec3Heap = new Vec3();
	public Vec2 getProjected(Vec3 vert, Mat4 transformDim) {
		vec3Heap.set(vert).transform(transformDim);
		return new Vec2(vec3Heap.x, vec3Heap.y);
	}
	public Vec2 getProjected(Vec3 vert, Mat4 transformDim, Mat4 viewPortMat) {
		vec3Heap.set(vert).transform(viewPortMat).transform(transformDim);
		return new Vec2(vec3Heap.x, vec3Heap.y);
	}
}
