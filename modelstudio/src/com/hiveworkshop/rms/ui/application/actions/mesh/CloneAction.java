package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * CloneAction -- allowing you to undo clone!
 *
 * Eric Theller 'Retera' 6/11/2012
 */
public class CloneAction implements UndoAction {
	private MoveAction baseMovement;
	private List<Vec3> selection;
	private List<GeosetVertex> addedVerts;
	private List<Triangle> addedTriangles;
	private List<GeosetVertex> copiedGroup;
	boolean type;

	public CloneAction(List<Vec3> selection, Vec3 moveVector, List<GeosetVertex> clones,
	                   List<Triangle> addedTriangles, boolean isExtrude) {
		addedVerts = clones;
		this.addedTriangles = addedTriangles;
		this.selection = new ArrayList<>(selection);
		baseMovement = new MoveAction(this.selection, moveVector, VertexActionType.UNKNOWN);
		type = isExtrude;
	}

	public CloneAction() {

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
		for (int i = 0; i < selection.size(); i++) {
			if (selection.get(i).getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) selection.get(i);
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

							t.set(t.indexOfRef(gv), cgv);
							gv.removeTriangle(t);
							cgv.addTriangle(t);
						}
					}
				}
			}
		}
		addTriangles();
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

		checkForErrors("Redo ");
	}

	private void addTriangles() {
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
	}

	@Override
	public void undo() {
		baseMovement.undo();
		if (type) {
			removeTriangle();
			for (int i = 0; i < addedVerts.size(); i++) {
				GeosetVertex cgv = addedVerts.get(i);
				if (cgv != null) {
					GeosetVertex gv = (GeosetVertex) selection.get(addedVerts.indexOf(cgv));
					List<Triangle> ctris = new ArrayList<>(cgv.getTriangles());
					moveTriangle(cgv, gv, ctris);
					cgv.getGeoset().remove(cgv);
					if (!gv.getGeoset().contains(gv)) {
						gv.getGeoset().addVertex(gv);
					}
				}
			}
		} else {
			removeTriangle();
			for (int i = 0; i < addedVerts.size(); i++) {
				GeosetVertex cgv = addedVerts.get(i);
				if (cgv != null) {
					GeosetVertex gv = copiedGroup.get(addedVerts.indexOf(cgv));
					List<Triangle> ctris = new ArrayList<>(cgv.getTriangles());
					moveTriangle(cgv, gv, ctris);
					cgv.getGeoset().remove(cgv);
					if (!gv.getGeoset().contains(gv)) {
						gv.getGeoset().addVertex(gv);
					}
				}
			}
		}

		checkForErrors("Undo ");
	}

	private void checkForErrors(String s) {
		int probs = 0;
		for (Vec3 vert : selection) {
			if (vert.getClass() == GeosetVertex.class) {
				GeosetVertex gv = (GeosetVertex) vert;
				for (Triangle t : gv.getTriangles()) {
					System.out.println("SHOULD be one: " + Collections.frequency(gv.getTriangles(), t));
					if (!t.containsRef(gv)) {
						probs++;
					}
				}
			}
		}
		System.out.println(s + actionName() + " finished with " + probs + " inexplicable errors.");
	}

	private void moveTriangle(GeosetVertex cgv, GeosetVertex gv, List<Triangle> ctris) {
		for (Triangle t : ctris) {
			t.set(t.indexOf(cgv), gv);
			cgv.removeTriangle(t);
			gv.addTriangle(t);
		}
	}

	private void removeTriangle() {
		for (Triangle t : addedTriangles) {
			for (GeosetVertex gv : t.getAll()) {
				gv.removeTriangle(t);
			}
			t.getGeoset().removeTriangle(t);
		}
	}

	@Override
	public String actionName() {
		return "extrude";
	}
}