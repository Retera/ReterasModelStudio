package com.hiveworkshop.rms.ui.application.edit.mesh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hiveworkshop.rms.editor.model.Bitmap;
import com.hiveworkshop.rms.editor.model.Bone;
import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.IdObject;
import com.hiveworkshop.rms.editor.model.Layer;
import com.hiveworkshop.rms.editor.model.Material;
import com.hiveworkshop.rms.editor.model.Matrix;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.wrapper.v2.ModelView;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.actions.mesh.DeleteAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.ExtrudeAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.RecalculateNormalsAction2;
import com.hiveworkshop.rms.ui.application.actions.mesh.SnapAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.SnapNormalsAction;
import com.hiveworkshop.rms.ui.application.actions.mesh.SpecialDeleteAction;
import com.hiveworkshop.rms.ui.application.actions.model.RecalculateExtentsAction;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.application.edit.animation.WrongModeException;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawBoxAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.DrawPlaneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.creator.actions.NewGeosetAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.CompoundMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.SimpleRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.editor.StaticMeshScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.CloneAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.FlipFacesAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.FlipNormalsAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.MirrorModelAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.tools.SetMatrixAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.DoNothingMoveActionAdapter;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.rms.ui.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

public abstract class AbstractModelEditor<T> extends AbstractSelectingEditor<T> {
	protected final ModelView model;
	protected final VertexSelectionHelper vertexSelectionHelper;
	protected final ModelStructureChangeListener structureChangeListener;

	public AbstractModelEditor(final SelectionManager<T> selectionManager, final ModelView model,
							   final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager);
		this.model = model;
		this.structureChangeListener = structureChangeListener;
		vertexSelectionHelper = vertices -> selectByVertices(vertices);
	}

	@Override
	public UndoAction setMatrix(final Collection<Bone> bones) {
		final Matrix mx = new Matrix();
		mx.setBones(new ArrayList<>());
		for (final Bone bone : bones) {
			mx.add(bone);
		}
		final Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences = new HashMap<>();
		final Map<GeosetVertex, Bone[]> vertexToOldSkinBoneReferences = new HashMap<>();
		final Map<GeosetVertex, short[]> vertexToOldSkinBoneWeightReferences = new HashMap<>();
		for (final Vec3 vert : selectionManager.getSelectedVertices()) {
			if (vert instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vert;
				if (gv.getSkinBones() != null) {
					vertexToOldSkinBoneReferences.put(gv, gv.getSkinBones().clone());
					vertexToOldSkinBoneWeightReferences.put(gv, gv.getSkinBoneWeights().clone());
					Arrays.fill(gv.getSkinBones(), null);
					Arrays.fill(gv.getSkinBoneWeights(), (short) 0);
					final int basicWeighting = 255 / bones.size();
					final int offset = 255 - (basicWeighting * bones.size());
					for (int i = 0; (i < bones.size()) && (i < 4); i++) {
						gv.getSkinBones()[i] = mx.getBones().get(i);
						gv.getSkinBoneWeights()[i] = (short) basicWeighting;
						if (i == 0) {
							gv.getSkinBoneWeights()[i] += offset;
						}
					}
				} else {
					vertexToOldBoneReferences.put(gv, new ArrayList<>(gv.getBoneAttachments()));
					gv.clearBoneAttachments();
					gv.addBoneAttachments(mx.getBones());
				}
			}
		}
		return new SetMatrixAction(vertexToOldBoneReferences, vertexToOldSkinBoneReferences,
				vertexToOldSkinBoneWeightReferences, bones);
	}

	@Override
	public UndoAction snapNormals() {
		final List<Vec3> oldLocations = new ArrayList<>();
		final List<Vec3> selectedNormals = new ArrayList<>();
		final Vec3 snapped = new Vec3(0, 0, 1);
		for (final Vec3 vertex : selectionManager.getSelectedVertices()) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vertex;
				if (gv.getNormal() != null) {
					oldLocations.add(new Vec3(gv.getNormal()));
					selectedNormals.add(gv.getNormal());
				} // else no normal to snap!!!
			}
		}
		final SnapNormalsAction temp = new SnapNormalsAction(selectedNormals, oldLocations, snapped);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public UndoAction recalcNormals() {
		final List<Vec3> oldLocations = new ArrayList<>();
		final List<GeosetVertex> selectedVertices = new ArrayList<>();
		final Vec3 snapped = new Vec3(0, 0, 1);
		for (final Vec3 vertex : selectionManager.getSelectedVertices()) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vertex;
				if (gv.getNormal() != null) {
					oldLocations.add(new Vec3(gv.getNormal()));
					selectedVertices.add(gv);
				} // else no normal to snap!!!
			}
		}
		final RecalculateNormalsAction2 temp = new RecalculateNormalsAction2(selectedVertices, oldLocations, snapped);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public UndoAction recalcExtents(final boolean onlyIncludeEditableGeosets) {
		final List<Geoset> geosetsToIncorporate = new ArrayList<>();
		if (onlyIncludeEditableGeosets) {
			geosetsToIncorporate.addAll(model.getEditableGeosets());
		} else {
			geosetsToIncorporate.addAll(model.getModel().getGeosets());
		}
		final RecalculateExtentsAction recalculateExtentsAction = new RecalculateExtentsAction(model,
				geosetsToIncorporate);
		recalculateExtentsAction.redo();
		return recalculateExtentsAction;
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		// TODO this code is RIPPED FROM MDLDispaly and is not good for general
		// cases
		// TODO this code operates directly on MODEL
		final List<Geoset> remGeosets = new ArrayList<>();// model.getGeosets()
		final List<Triangle> deletedTris = new ArrayList<>();
		final Collection<? extends Vec3> selection = new ArrayList<>(selectionManager.getSelectedVertices());
		for (final Vec3 vertex : selection) {
			if (vertex.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vertex;
				for (final Triangle t : gv.getTriangles()) {
					t.getGeoset().removeTriangle(t);
					if (!deletedTris.contains(t)) {
						deletedTris.add(t);
					}
				}
				gv.getGeoset().remove(gv);
			}
		}
		for (final Triangle t : deletedTris) {
			for (final GeosetVertex vertex : t.getAll()) {
				vertex.getTriangles().remove(t);
			}
		}
		for (int i = model.getModel().getGeosets().size() - 1; i >= 0; i--) {
			if (model.getModel().getGeosets().get(i).isEmpty()) {
				final Geoset g = model.getModel().getGeoset(i);
				remGeosets.add(g);
				model.getModel().remove(g);
				if (g.getGeosetAnim() != null) {
					model.getModel().remove(g.getGeosetAnim());
				}
			}
		}
		selectByVertices(new ArrayList<>());
		if (remGeosets.size() <= 0) {
			final DeleteAction temp = new DeleteAction(selection, deletedTris, vertexSelectionHelper);
			return temp;
		} else {
			final SpecialDeleteAction temp = new SpecialDeleteAction(selection, deletedTris, vertexSelectionHelper,
					remGeosets, model.getModel(), structureChangeListener);
			structureChangeListener.geosetsRemoved(remGeosets);
			return temp;
		}
	}

	@Override
	public UndoAction mirror(final byte dim, final boolean flipModel, final double centerX, final double centerY,
							 final double centerZ) {
		final MirrorModelAction mirror = new MirrorModelAction(selectionManager.getSelectedVertices(),
				model.getEditableIdObjects(), dim, centerX, centerY, centerZ);
		// super weird passing of currently editable id Objects, works because
		// mirror action checks selected vertices against pivot points from this
		// list
		mirror.redo();
		if (flipModel) {
			final UndoAction flipFacesAction = flipSelectedFaces();
			return new CompoundAction(mirror.actionName(), Arrays.asList(mirror, flipFacesAction));
		}
		return mirror;
	}

	@Override
	public UndoAction flipSelectedFaces() {
		// TODO implement using faces for FaceModelEditor... probably?
		final FlipFacesAction flipFacesAction = new FlipFacesAction(selectionManager.getSelectedVertices());
		flipFacesAction.redo();
		return flipFacesAction;
	}

	@Override
	public UndoAction flipSelectedNormals() {
		final FlipNormalsAction flipNormalsAction = new FlipNormalsAction(selectionManager.getSelectedVertices());
		flipNormalsAction.redo();
		return flipNormalsAction;
	}

	@Override
	public UndoAction snapSelectedNormals() {
		final Collection<? extends Vec3> selection = selectionManager.getSelectedVertices();
		final List<Vec3> oldLocations = new ArrayList<>();
		final List<Vec3> selectedNormals = new ArrayList<>();
		final Vec3 snapped = new Vec3(0, 0, 1);
		for (final Vec3 vertex : selection) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vertex;
				if (gv.getNormal() != null) {
					oldLocations.add(new Vec3(gv.getNormal()));
					selectedNormals.add(gv.getNormal());
				} // else no normal to snap!!!
			}
		}
		final SnapNormalsAction temp = new SnapNormalsAction(selectedNormals, oldLocations, snapped);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public UndoAction beginExtrudingSelection() {
		final List<Vec3> selection = new ArrayList<>(selectionManager.getSelectedVertices());
		final List<GeosetVertex> copies = new ArrayList<>();
		final List<Triangle> selTris = new ArrayList<>();
		for (final Vec3 vert : selection) {
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				copies.add(new GeosetVertex(gv));

				for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
					final Triangle temptr = gv.getTriangles().get(ti);
					if (!selTris.contains(temptr)) {
						selTris.add(temptr);
					}
				}
			} else {
				copies.add(null);
				// System.out.println("GeosetVertex " + i + " was not found.");
			}
		}
		for (final Triangle tri : selTris) {
			if (!selection.contains(tri.get(0)) || !selection.contains(tri.get(1)) || !selection.contains(tri.get(2))) {
				for (int i = 0; i < 3; i++) {
					final GeosetVertex a = tri.get(i);
					if (selection.contains(a)) {
						final GeosetVertex b = copies.get(selection.indexOf(a));
						tri.set(i, b);
						a.getTriangles().remove(tri);
						// if (a.getTriangles().contains(tri)) {
						// System.out.println("It's a bloody war!");
						// }
						b.getTriangles().add(tri);
					}
				}
			}
		}
		// System.out.println(selection.size() + " verteces cloned into " +
		// copies.size() + " more.");
		final List<Triangle> newTriangles = new ArrayList<>();
		for (int k = 0; k < selection.size(); k++) {
			final Vec3 vert = selection.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final List<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				// WHY IS GV.TRIANGLES WRONG????
				for (final Triangle tri : gv.getGeoset().getTriangles()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (final Vec3 vTemp : tri.getAll()) {
							if (!selection.contains(vTemp)) {
								good = false;
								break;
							}
						}
						if (good) {
							gvTriangles.add(tri);
						}
					}
				}
				for (final Triangle tri : gvTriangles) {
					// for (final GeosetVertex copyVer : copies) {
					// if (copyVer != null) {
					// if (tri.containsRef(copyVer)) {
					// System.out.println("holy brejeezers!");
					// }
					// }
					// }
					for (int gvI = 0; gvI < tri.getAll().length; gvI++) {
						final GeosetVertex gvTemp = tri.get(gvI);
						if (!gvTemp.equalLocs(gv) && (gvTemp.getGeoset() == gv.getGeoset())) {
							int ctCount = 0;
							Triangle temptr = null;
							boolean okay = false;
							for (final Triangle triTest : gvTriangles) {
								if (triTest.contains(gvTemp)) {
									ctCount++;
									temptr = triTest;
									if (temptr.containsRef(gvTemp) && temptr.containsRef(gv)) {
										okay = true;
									}
								}
							}
							if (okay && (ctCount == 1) && selection.contains(gvTemp)) {
								final GeosetVertex gvCopy = copies.get(selection.indexOf(gv));
								final GeosetVertex gvTempCopy = copies.get(selection.indexOf(gvTemp));
								// if (gvCopy == null) {
								// System.out.println("Vertex (gvCopy) copy found as null!");
								// }
								// if (gvTempCopy == null) {
								// System.out.println("Vertex (gvTempCopy) copy found as null!");
								// }
								Triangle newFace = new Triangle(null, null, null, gv.getGeoset());

								final int indexA = temptr.indexOf(gv);
								final int indexB = temptr.indexOf(gvTemp);
								int indexC = -1;

								for (int i = 0; (i < 3) && (indexC == -1); i++) {
									if ((i != indexA) && (i != indexB)) {
										indexC = i;
									}
								}

								// System.out.println(" Indeces: " + indexA + "," + indexB + "," + indexC);

								newFace.set(indexA, gv);
								newFace.set(indexB, gvTemp);
								newFace.set(indexC, gvCopy);
								// Make sure it's included later
								// gvTemp.triangles.add(newFace);
								// gv.getTriangles().add(newFace);
								// gvCopy.triangles.add(newFace);
								// gv.getGeoset().addTriangle(newFace);
								boolean bad = false;
								for (final Triangle t : newTriangles) {
									// if( t.equals(newFace) )
									// {
									// bad = true;
									// break;
									// }
									if (t.contains(gv) && t.contains(gvTemp)) {
										bad = true;
										break;
									}
								}
								if (!bad) {
									newTriangles.add(newFace);

									// System.out.println("New Face: ");
									// System.out.println(newFace.get(0));
									// System.out.println(newFace.get(1));
									// System.out.println(newFace.get(2));

									newFace = new Triangle(null, null, null, gv.getGeoset());

									newFace.set(indexA, gvCopy);
									newFace.set(indexB, gvTemp);
									newFace.set(indexC, gvTempCopy);
									// Make sure it's included later
									newTriangles.add(newFace);

									// System.out.println("New Alternate Face: ");
									// System.out.println(newFace.get(0));
									// System.out.println(newFace.get(1));
									// System.out.println(newFace.get(2));

								}
							}
						}
					}
				}
			}
		}

		for (final Triangle t : newTriangles) {
			for (final GeosetVertex gv : t.getAll()) {
				if (!gv.getTriangles().contains(t)) {
					gv.getTriangles().add(t);
				}
				if (!gv.getGeoset().contains(t)) {
					gv.getGeoset().addTriangle(t);
				}
			}
		}
		for (final GeosetVertex cgv : copies) {
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
		int probs = 0;
		for (final Vec3 vert : selection) {
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				for (final Triangle t : gv.getTriangles()) {
					// System.out.println("SHOULD be one: " +
					// Collections.frequency(gv.getTriangles(), t));
					if (!t.containsRef(gv)) {
						probs++;
					}
				}
			}
		}
		// System.out.println("Extrude finished with " + probs + " inexplicable
		// errors.");
		final ExtrudeAction tempe = new ExtrudeAction(); // TODO better code
		tempe.storeSelection(selection);
		tempe.setType(true);
		tempe.storeBaseMovement(new Vec3(0, 0, 0));
		tempe.setAddedTriangles(newTriangles);
		tempe.setAddedVerts(copies);
		return tempe;
	}

	@Override
	public UndoAction beginExtendingSelection() {
		final List<Vec3> selection = new ArrayList<>(selectionManager.getSelectedVertices());
		final List<GeosetVertex> copies = new ArrayList<>();
		final List<Triangle> selTris = new ArrayList<>();
		final List<Triangle> newTriangles = new ArrayList<>();

		final List<Triangle> edges = new ArrayList<>();
		final List<Triangle> brokenFaces = new ArrayList<>();

		for (final Vec3 vert : selection) {
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				// copies.add(new GeosetVertex(gv));

				// selTris.addAll(gv.getTriangles());
				for (int ti = 0; ti < gv.getTriangles().size(); ti++) {
					final Triangle temptr = gv.getTriangles().get(ti);
					if (!selTris.contains(temptr)) {
						selTris.add(temptr);
					}
				}
			} else {
				// copies.add(null);
				// System.out.println("GeosetVertex " + i + " was not found.");
			}
		}
		System.out.println(selection.size() + " verteces cloned into " + copies.size() + " more.");
		final List<GeosetVertex> copiedGroup = new ArrayList<>();
		for (final Triangle tri : selTris) {
			if (!selection.contains(tri.get(0)) || !selection.contains(tri.get(1)) || !selection.contains(tri.get(2))) {
				int selVerts = 0;
				GeosetVertex gv = null;
				GeosetVertex gvTemp = null;
				GeosetVertex gvCopy = null;// copies.get(selection.indexOf(gv));
				GeosetVertex gvTempCopy = null;// copies.get(selection.indexOf(gvTemp));
				for (int i = 0; i < 3; i++) {
					final GeosetVertex a = tri.get(i);
					if (selection.contains(a)) {
						selVerts++;
						final GeosetVertex b = new GeosetVertex(a);
						copies.add(b);
						copiedGroup.add(a);
						tri.set(i, b);
						a.getTriangles().remove(tri);
						b.getTriangles().add(tri);
						if (gv == null) {
							gv = a;
							gvCopy = b;
						} else if (gvTemp == null) {
							gvTemp = a;
							gvTempCopy = b;
						}
					}
				}
				if (selVerts == 2) {
					// if (gvCopy == null) {
					// System.out.println("Vertex (gvCopy) copy found as null!");
					// }
					// if (gvTempCopy == null) {
					// System.out.println("Vertex (gvTempCopy) copy found as null!");
					// }
					Triangle newFace = new Triangle(null, null, null, gv.getGeoset());

					final int indexA = tri.indexOf(gvCopy);
					final int indexB = tri.indexOf(gvTempCopy);
					int indexC = -1;

					for (int i = 0; (i < 3) && (indexC == -1); i++) {
						if ((i != indexA) && (i != indexB)) {
							indexC = i;
						}
					}

					// System.out.println(" Indeces: " + indexA + "," + indexB + "," + indexC);

					newFace.set(indexA, gv);
					newFace.set(indexB, gvTemp);
					newFace.set(indexC, gvCopy);
					// Make sure it's included later
					gvTemp.getTriangles().add(newFace);
					gv.getTriangles().add(newFace);
					gvCopy.getTriangles().add(newFace);
					gv.getGeoset().addTriangle(newFace);
					newTriangles.add(newFace);

					// System.out.println("New Face: ");
					// System.out.println(newFace.get(0));
					// System.out.println(newFace.get(1));
					// System.out.println(newFace.get(2));

					newFace = new Triangle(null, null, null, gv.getGeoset());

					newFace.set(indexA, gvCopy);
					newFace.set(indexB, gvTemp);
					newFace.set(indexC, gvTempCopy);
					// Make sure it's included later
					gvCopy.getTriangles().add(newFace);
					gvTemp.getTriangles().add(newFace);
					gvTempCopy.getTriangles().add(newFace);
					gv.getGeoset().addTriangle(newFace);
					newTriangles.add(newFace);

					// System.out.println("New Alternate Face: ");
					// System.out.println(newFace.get(0));
					// System.out.println(newFace.get(1));
					// System.out.println(newFace.get(2));
				}
			}
		}

		for (final GeosetVertex cgv : copies) {
			if (cgv != null) {
				cgv.getGeoset().addVertex(cgv);
			}
		}

		final ExtrudeAction tempe = new ExtrudeAction();
		tempe.storeSelection(selection);
		tempe.setType(false);
		tempe.storeBaseMovement(new Vec3(0, 0, 0));
		tempe.setAddedTriangles(newTriangles);
		tempe.setAddedVerts(copies);
		tempe.setCopiedGroup(copiedGroup);
		return tempe;
	}

	@Override
	public UndoAction snapSelectedVertices() {
		final Collection<? extends Vec3> selection = selectionManager.getSelectedVertices();
		final List<Vec3> oldLocations = new ArrayList<>();
		final Vec3 cog = Vec3.centerOfGroup(selection);
		for (final Vec3 vertex : selection) {
			oldLocations.add(new Vec3(vertex));
		}
		final SnapAction temp = new SnapAction(selection, oldLocations, cog);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public CloneAction cloneSelectedComponents(final ClonedNodeNamePicker clonedNodeNamePicker) {
		final List<Vec3> source = new ArrayList<>(selectionManager.getSelectedVertices());
		final List<Triangle> selTris = new ArrayList<>();
		final List<IdObject> selBones = new ArrayList<>();
		final List<IdObject> newBones = new ArrayList<>();
		final List<GeosetVertex> newVertices = new ArrayList<>();
		final List<Triangle> newTriangles = new ArrayList<>();
		for (final Vec3 vert : source) {
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				newVertices.add(new GeosetVertex(gv));
			} else {
				newVertices.add(null);
			}
		}
		for (final IdObject b : model.getEditableIdObjects()) {
			if (source.contains(b.getPivotPoint()) && !selBones.contains(b)) {
				selBones.add(b);
				newBones.add(b.copy());
			}
		}
		if (newBones.size() > 0) {
			final java.util.Map<IdObject, String> nodeToNamePicked = clonedNodeNamePicker.pickNames(newBones);
			if (nodeToNamePicked == null) {
				throw new RuntimeException(
						"user does not wish to continue so we put in an error to interrupt clone so model is OK");
			}
			for (final IdObject node : nodeToNamePicked.keySet()) {
				node.setName(nodeToNamePicked.get(node));
			}
		}
		for (int k = 0; k < source.size(); k++) {
			final Vec3 vert = source.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final List<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				for (final Triangle tri : gv.getGeoset().getTriangles()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (final Vec3 vTemp : tri.getAll()) {
							if (!source.contains(vTemp)) {
								good = false;
								break;
							}
						}
						if (good) {
							gvTriangles.add(tri);
							if (!selTris.contains(tri)) {
								selTris.add(tri);
							}
						}
					}
				}
			}
		}
		for (final Triangle tri : selTris) {
			final GeosetVertex a = newVertices.get(source.indexOf(tri.get(0)));
			final GeosetVertex b = newVertices.get(source.indexOf(tri.get(1)));
			final GeosetVertex c = newVertices.get(source.indexOf(tri.get(2)));
			final Triangle newTriangle = new Triangle(a, b, c, a.getGeoset());
			newTriangles.add(newTriangle);
			a.getTriangles().add(newTriangle);
			b.getTriangles().add(newTriangle);
			c.getTriangles().add(newTriangle);
		}
		final Set<Vec3> newSelection = new HashSet<>();
		for (final Vec3 ver : newVertices) {
			if (ver != null) {
				newSelection.add(ver);
				if (ver.getClass() == GeosetVertex.class) {
					final GeosetVertex gv = (GeosetVertex) ver;
					for (int i = 0; i < gv.getBones().size(); i++) {
						final Bone b = gv.getBones().get(i);
						if (selBones.contains(b)) {
							gv.getBones().set(i, (Bone) newBones.get(selBones.indexOf(b)));
						}
					}
				}
			}
		}
		for (final IdObject b : newBones) {
			newSelection.add(b.getPivotPoint());
			if (selBones.contains(b.getParent())) {
				b.setParent(newBones.get(selBones.indexOf(b.getParent())));
			}
		}
		final List<GeosetVertex> newVerticesWithoutNulls = new ArrayList<>();
		for (final GeosetVertex vertex : newVertices) {
			if (vertex != null) {
				newVerticesWithoutNulls.add(vertex);
			}
		}
		// TODO cameras
		final CloneAction cloneAction = new CloneAction(model, source, structureChangeListener, vertexSelectionHelper,
				selBones, newVerticesWithoutNulls, newTriangles, newBones, newSelection);
		cloneAction.redo();
		return cloneAction;
	}

	@Override
	public void rawTranslate(final double x, final double y, final double z) {
		for (final Vec3 vertex : selectionManager.getSelectedVertices()) {
			vertex.translate(x, y, z);
		}
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX,
						 final double scaleY, final double scaleZ) {
		for (final Vec3 vertex : selectionManager.getSelectedVertices()) {
			vertex.scale(centerX, centerY, centerZ, scaleX, scaleY, scaleZ);
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
							final byte firstXYZ, final byte secondXYZ) {
		for (final Vec3 vertex : selectionManager.getSelectedVertices()) {
			vertex.rotate(centerX, centerY, centerZ, radians, firstXYZ, secondXYZ);
		}
	}

	@Override
	public void rawRotate3d(final Vec3 center, final Vec3 axis, final double radians) {
		for (final Vec3 vertex : selectionManager.getSelectedVertices()) {
			Vec3.rotateVertex(center, axis, radians, vertex);
		}
	}

	@Override
	public UndoAction translate(final double x, final double y, final double z) {
		final Vec3 delta = new Vec3(x, y, z);
		final StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(final Vec3 center, final double x, final double y, final double z) {
		final Vec3 delta = new Vec3(x - center.x, y - center.y, z - center.z);
		final StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction rotate(final Vec3 center, final double rotateX, final double rotateY, final double rotateZ) {

		final CompoundAction compoundAction = new CompoundAction("rotate", Arrays.asList(
				new SimpleRotateAction(this, center, rotateX, (byte) 2, (byte) 1),
				new SimpleRotateAction(this, center, rotateY, (byte) 0, (byte) 2),
				new SimpleRotateAction(this, center, rotateZ, (byte) 1, (byte) 0)));
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public Vec3 getSelectionCenter() {
		return selectionManager.getCenter();
	}

	@Override
	public boolean editorWantsAnimation() {
		return false;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		return new StaticMeshMoveAction(this, Vec3.ORIGIN);
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY, final double centerZ,
											 final byte dim1, final byte dim2) {
		return new StaticMeshRotateAction(this, new Vec3(centerX, centerY, centerZ), dim1, dim2);
	}

	@Override
	public GenericRotateAction beginSquatTool(final double centerX, final double centerY, final double centerZ,
											  final byte firstXYZ, final byte secondXYZ) {
		throw new WrongModeException("Unable to use squat tool outside animation editor mode");
	}

	@Override
	public GenericScaleAction beginScaling(final double centerX, final double centerY, final double centerZ) {
		return new StaticMeshScaleAction(this, centerX, centerY, centerZ);
	}

	@Override
	public UndoAction createKeyframe(final ModelEditorActionType actionType) {
		throw new UnsupportedOperationException("Cannot create keyframe outside of animation mode");
	}

	@Override
	public GenericMoveAction addPlane(final double x, final double y, final double x2, final double y2, final byte dim1,
									  final byte dim2, final Vec3 facingVector, final int numberOfWidthSegments,
									  final int numberOfHeightSegments) {
		final List<Geoset> geosets = model.getModel().getGeosets();
		Geoset solidWhiteGeoset = null;
		for (final Geoset geoset : geosets) {
			final Layer firstLayer = geoset.getMaterial().firstLayer();
			if ((geoset.getMaterial() != null) && (firstLayer != null)
					&& (firstLayer.getFilterMode() == FilterMode.NONE)
					&& "Textures\\white.blp".equalsIgnoreCase(firstLayer.getTextureBitmap().getPath())) {
				solidWhiteGeoset = geoset;
			}
		}
		boolean needsGeosetAction = false;
		if (solidWhiteGeoset == null) {
			solidWhiteGeoset = new Geoset();
			solidWhiteGeoset.setMaterial(new Material(new Layer("None", new Bitmap("Textures\\white.blp"))));
			needsGeosetAction = true;
		}
		final GenericMoveAction action;
		final DrawPlaneAction drawVertexAction = new DrawPlaneAction(x, y, x2, y2, dim1, dim2, facingVector,
				numberOfWidthSegments, numberOfHeightSegments, solidWhiteGeoset);
		if (needsGeosetAction) {
			final NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, model.getModel(),
					structureChangeListener);
			action = new CompoundMoveAction("create plane",
					Arrays.asList(new DoNothingMoveActionAdapter(newGeosetAction), drawVertexAction));
		} else {
			action = drawVertexAction;
		}
		action.redo();
		return action;

	}

	@Override
	public GenericMoveAction addBox(final double x, final double y, final double x2, final double y2, final byte dim1,
									final byte dim2, final Vec3 facingVector, final int numberOfLengthSegments,
									final int numberOfWidthSegments, final int numberOfHeightSegments) {
		final List<Geoset> geosets = model.getModel().getGeosets();
		Geoset solidWhiteGeoset = null;
		for (final Geoset geoset : geosets) {
			final Layer firstLayer = geoset.getMaterial().firstLayer();
			if ((geoset.getMaterial() != null) && (firstLayer != null)
					&& (firstLayer.getFilterMode() == FilterMode.NONE)
					&& "Textures\\white.blp".equalsIgnoreCase(firstLayer.getTextureBitmap().getPath())) {
				solidWhiteGeoset = geoset;
			}
		}
		boolean needsGeosetAction = false;
		if (solidWhiteGeoset == null) {
			solidWhiteGeoset = new Geoset();
			solidWhiteGeoset.setMaterial(new Material(new Layer("None", new Bitmap("Textures\\white.blp"))));
			needsGeosetAction = true;
		}
		final GenericMoveAction action;
		final DrawBoxAction drawVertexAction = new DrawBoxAction(x, y, x2, y2, dim1, dim2, facingVector,
				numberOfLengthSegments, numberOfWidthSegments, numberOfHeightSegments, solidWhiteGeoset);
		if (needsGeosetAction) {
			final NewGeosetAction newGeosetAction = new NewGeosetAction(solidWhiteGeoset, model.getModel(),
					structureChangeListener);
			action = new CompoundMoveAction("create plane",
					Arrays.asList(new DoNothingMoveActionAdapter(newGeosetAction), drawVertexAction));
		} else {
			action = drawVertexAction;
		}
		action.redo();
		return action;

	}

	@Override
	public RigAction rig() {
		return new RigAction(selectionManager.getSelectedVertices(), Collections.emptyList());
	}

	@Override
	public UndoAction addBone(final double x, final double y, final double z) {
		throw new WrongModeException("Unable to add bone outside of pivot point editor");
	}
}
