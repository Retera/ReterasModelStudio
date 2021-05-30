package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * ExtrudeAction -- something extruded that you can undo!
 *
 * Eric Theller 'Retera' 6/11/2012
 */
public class ExtrudeAction implements UndoAction {
	MoveAction baseMovement;
	List<Vec3> selection;
	List<GeosetVertex> addedVerts;
	List<Triangle> addedTriangles;
	List<GeosetVertex> copiedGroup;
	boolean type;

	public ExtrudeAction(List<Vec3> selection, Vec3 moveVector,
	                     List<GeosetVertex> clones, List<Triangle> addedTriangles,
	                     boolean isExtrude) {
		addedVerts = clones;
		this.addedTriangles = addedTriangles;
		this.selection = new ArrayList<>(selection);
		baseMovement = new MoveAction(this.selection, moveVector, VertexActionType.UNKNOWN);
		type = isExtrude;
	}

	public ExtrudeAction() {

	}

	public void storeSelection(List<Vec3> selection) {
		this.selection = new ArrayList<>(selection);
	}

	public void storeBaseMovement(Vec3 moveVector) {
		baseMovement = new MoveAction(this.selection, moveVector, VertexActionType.UNKNOWN);
	}

	@Override
	public void redo() {
		baseMovement.redo();
		for (Vec3 vec3 : selection) {
			if (vec3.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vec3;
				GeosetVertex cgv = null;
				boolean good = true;
				if (type) {
					cgv = addedVerts.get(selection.indexOf(gv));
				} else {
					if (!copiedGroup.contains(gv)) {
						good = false;
					}
					if (good) {
						cgv = addedVerts.get(copiedGroup.indexOf(gv));
					}
				}
				if (good) {
					List<Triangle> tris = new ArrayList<>(gv.getTriangles());
					for (Triangle t : tris) {
						if (!selection.contains(t.get(0))
								|| !selection.contains(t.get(1))
								|| !selection.contains(t.get(2))) {
							// System.out.println("SHOULD be one:"+Collections.frequency(tris,t));
							// System.out.println("should be a number:"+t.indexOfRef(gv));
							// System.out.println("should be a negative one number: "+t.indexOfRef(cgv));
							t.set(t.indexOfRef(gv), cgv);
							gv.removeTriangle(t);
							cgv.addTriangle(t);
						}
					}
				}
				// cgv.geoset.addVertex(cgv);
			}
		}
		for (Triangle t : addedTriangles) {
			for (GeosetVertex gv : t.getAll()) {
				if (!gv.hasTriangle(t)) {
					gv.addTriangle(t);
				}
			}
			if (!t.getGeoset().contains(t)) {
				t.getGeoset().addTriangle(t);
			}
		}
		for (GeosetVertex cgv : addedVerts) {
			if (cgv != null) {
				boolean inGeoset = false;
				for (Triangle t : cgv.getGeoset().getTriangles()) {
					if (t.containsRef(cgv)) {
						inGeoset = true;
						break;
					}
				}
				if (inGeoset) {
					cgv.getGeoset().addVertex(cgv);
				}
			}
		}
		// for( Triangle t: addedTriangles ){t.m_geoRef.addTriangle(t);}
		int probs = 0;
		for (Vec3 vert : selection) {
			if (vert.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vert;
				for (Triangle t : gv.getTriangles()) {
					if (!t.containsRef(gv)) {
						probs++;
					}
				}
			}
		}
	}

	@Override
	public void undo() {
		baseMovement.undo();
		if (type) {
			for (Triangle t : addedTriangles) {
				for (GeosetVertex gv : t.getAll()) {
					gv.removeTriangle(t);
				}
				t.getGeoset().removeTriangle(t);
			}
			for (GeosetVertex cgv : addedVerts) {
				if (cgv != null) {
					GeosetVertex gv = (GeosetVertex) selection.get(addedVerts.indexOf(cgv));
					List<Triangle> ctris = new ArrayList<>(cgv.getTriangles());
					for (Triangle t : ctris) {
						t.set(t.indexOf(cgv), gv);
						cgv.removeTriangle(t);
						gv.addTriangle(t);
					}
					cgv.getGeoset().remove(cgv);
					if (!gv.getGeoset().contains(gv)) {
						gv.getGeoset().addVertex(gv);
					}
				}
			}
		} else {
			for (Triangle t : addedTriangles) {
				for (GeosetVertex gv : t.getAll()) {
					gv.removeTriangle(t);
				}
				t.getGeoset().removeTriangle(t);
			}
			for (GeosetVertex cgv : addedVerts) {
				if (cgv != null) {
					GeosetVertex gv = copiedGroup.get(addedVerts.indexOf(cgv));
					List<Triangle> ctris = new ArrayList<>(cgv.getTriangles());
					for (Triangle t : ctris) {
						t.set(t.indexOf(cgv), gv);
						cgv.removeTriangle(t);
						gv.addTriangle(t);
					}
					cgv.getGeoset().remove(cgv);
					if (!gv.getGeoset().contains(gv)) {
						gv.getGeoset().addVertex(gv);
					}
				}
			}
		}
		// for( GeosetVertex cgv: addedVerts ){
		// if( cgv != null ){boolean inGeoset = false;
		// for( Triangle t: cgv.geoset.m_triangle ){
		// if( t.containsRef(cgv) ){
		// inGeoset = true;break;}}
		// if( inGeoset )cgv.geoset.remove(cgv);}}
		int probs = 0;
		for (Vec3 vert : selection) {
			if (vert.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vert;
				for (Triangle t : gv.getTriangles()) {
					if (!t.containsRef(gv)) {
						probs++;
					}
				}
			}
		}
	}

	@Override
	public String actionName() {
		return "extrude";
	}

	public MoveAction getBaseMovement() {
		return baseMovement;
	}

	public List<GeosetVertex> getAddedVerts() {
		return addedVerts;
	}

	public void setAddedVerts(List<GeosetVertex> addedVerts) {
		this.addedVerts = addedVerts;
	}

	public List<Triangle> getAddedTriangles() {
		return addedTriangles;
	}

	public void setAddedTriangles(List<Triangle> addedTriangles) {
		this.addedTriangles = addedTriangles;
	}

	public boolean isType() {
		return type;
	}

	public void setType(boolean type) {
		this.type = type;
	}

	public void setCopiedGroup(List<GeosetVertex> copiedGroup) {
		this.copiedGroup = copiedGroup;
	}

}
