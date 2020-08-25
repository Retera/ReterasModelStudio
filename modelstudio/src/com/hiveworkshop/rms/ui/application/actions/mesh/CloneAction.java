package com.hiveworkshop.rms.ui.application.actions.mesh;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.ui.application.actions.VertexActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.util.Vertex;

/**
 * CloneAction -- allowing you to undo clone!
 *
 * Eric Theller 'Retera' 6/11/2012
 */
public class CloneAction implements UndoAction {
	private MoveAction baseMovement;
	private List<Vertex> selection;
	private List<GeosetVertex> addedVerts;
	private List<Triangle> addedTriangles;
	private List<GeosetVertex> copiedGroup;
	boolean type;

	public CloneAction(final List<Vertex> selection, final Vertex moveVector, final List<GeosetVertex> clones,
			final List<Triangle> addedTriangles, final boolean isExtrude) {
		addedVerts = clones;
		this.addedTriangles = addedTriangles;
		this.selection = new ArrayList<Vertex>(selection);
		baseMovement = new MoveAction(this.selection, moveVector, VertexActionType.UNKNOWN);
		type = isExtrude;
	}

	public CloneAction() {

	}

	public void storeSelection(final List<Vertex> selection) {
		this.selection = new ArrayList<Vertex>(selection);
	}

	public void storeBaseMovement(final Vertex moveVector) {
		baseMovement = new MoveAction(this.selection, moveVector, VertexActionType.UNKNOWN);
	}

	@Override
	public void redo() {
		baseMovement.redo();
		for (int i = 0; i < selection.size(); i++) {
			if (selection.get(i).getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) selection.get(i);
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
					final List<Triangle> tris = new ArrayList<Triangle>(gv.getTriangles());
					for (final Triangle t : tris) {
						if (!selection.contains(t.get(0)) || !selection.contains(t.get(1))
								|| !selection.contains(t.get(2))) {
							// System.out.println("SHOULD be one:
							// "+Collections.frequency(tris,t));
							// System.out.println("should be a number:
							// "+t.indexOfRef(gv));
							// System.out.println("should be a negative one
							// number: "+t.indexOfRef(cgv));
							t.set(t.indexOfRef(gv), cgv);
							gv.getTriangles().remove(t);
							cgv.getTriangles().add(t);
						}
					}
				}
				// cgv.geoset.addVertex(cgv);
			}
		}
		for (final Triangle t : addedTriangles) {
			for (final GeosetVertex gv : t.getAll()) {
				if (!gv.getTriangles().contains(t)) {
					gv.getTriangles().add(t);
				}
			}
			if (!t.getGeoset().contains(t)) {
				t.getGeoset().addTriangle(t);
			}
		}
		for (final GeosetVertex cgv : addedVerts) {
			if (cgv != null) {
				boolean inGeoset = false;
				for (final Triangle t : cgv.getGeoset().getTriangles()) {
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
		// for( Triangle t: addedTriangles )
		// {
		// t.m_geoRef.addTriangle(t);
		// }
		int probs = 0;
		for (int k = 0; k < selection.size(); k++) {
			final Vertex vert = selection.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				for (final Triangle t : gv.getTriangles()) {
					System.out.println("SHOULD be one: " + Collections.frequency(gv.getTriangles(), t));
					if (!t.containsRef(gv)) {
						probs++;
					}
				}
			}
		}
		System.out.println("Redo " + actionName() + " finished with " + probs + " inexplicable errors.");
	}

	@Override
	public void undo() {
		baseMovement.undo();
		if (type) {
			for (final Triangle t : addedTriangles) {
				for (final GeosetVertex gv : t.getAll()) {
					gv.getTriangles().remove(t);
				}
				t.getGeoset().removeTriangle(t);
			}
			for (int i = 0; i < addedVerts.size(); i++) {
				final GeosetVertex cgv = addedVerts.get(i);
				if (cgv != null) {
					final GeosetVertex gv = (GeosetVertex) selection.get(addedVerts.indexOf(cgv));
					final List<Triangle> ctris = new ArrayList<Triangle>(cgv.getTriangles());
					for (final Triangle t : ctris) {
						t.set(t.indexOf(cgv), gv);
						cgv.getTriangles().remove(t);
						gv.getTriangles().add(t);
					}
					cgv.getGeoset().remove(cgv);
					if (!gv.getGeoset().contains(gv)) {
						gv.getGeoset().addVertex(gv);
					}
				}
			}
		} else {
			for (final Triangle t : addedTriangles) {
				for (final GeosetVertex gv : t.getAll()) {
					gv.getTriangles().remove(t);
				}
				t.getGeoset().removeTriangle(t);
			}
			for (int i = 0; i < addedVerts.size(); i++) {
				final GeosetVertex cgv = addedVerts.get(i);
				if (cgv != null) {
					final GeosetVertex gv = copiedGroup.get(addedVerts.indexOf(cgv));
					final List<Triangle> ctris = new ArrayList<Triangle>(cgv.getTriangles());
					for (final Triangle t : ctris) {
						t.set(t.indexOf(cgv), gv);
						cgv.getTriangles().remove(t);
						gv.getTriangles().add(t);
					}
					cgv.getGeoset().remove(cgv);
					if (!gv.getGeoset().contains(gv)) {
						gv.getGeoset().addVertex(gv);
					}
				}
			}
		}
		// for( GeosetVertex cgv: addedVerts )
		// {
		// if( cgv != null )
		// {
		// boolean inGeoset = false;
		// for( Triangle t: cgv.geoset.m_triangle )
		// {
		// if( t.containsRef(cgv) )
		// {
		// inGeoset = true;
		// break;
		// }
		// }
		// if( inGeoset )
		// cgv.geoset.remove(cgv);
		// }
		// }
		int probs = 0;
		for (int k = 0; k < selection.size(); k++) {
			final Vertex vert = selection.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				for (final Triangle t : gv.getTriangles()) {
					System.out.println("SHOULD be one: " + Collections.frequency(gv.getTriangles(), t));
					if (!t.containsRef(gv)) {
						probs++;
					}
				}
			}
		}
		System.out.println("Undo " + actionName() + " finished with " + probs + " inexplicable errors.");
	}

	@Override
	public String actionName() {
		if (type) {
			return "extrude";
		} else {
			return "extrude";
		}
	}
}