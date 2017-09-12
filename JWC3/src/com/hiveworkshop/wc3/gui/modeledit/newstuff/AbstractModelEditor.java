package com.hiveworkshop.wc3.gui.modeledit.newstuff;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.etheller.collections.HashMap;
import com.etheller.collections.ListView;
import com.etheller.collections.Map;
import com.etheller.util.CollectionUtils;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.DeleteAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.SnapAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.SnapNormalsAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.SpecialDeleteAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.CloneAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.FlipFacesAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.FlipNormalsAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.MirrorModelAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.MoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.RotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.SetMatrixAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Matrix;
import com.hiveworkshop.wc3.mdl.Normal;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;

public abstract class AbstractModelEditor<T> implements ModelEditor {
	protected final SelectionManager<T> selectionManager;
	protected final ModelView model;

	public AbstractModelEditor(final SelectionManager<T> selectionManager, final ModelView model) {
		this.selectionManager = selectionManager;
		this.model = model;
	}

	@Override
	public UndoAction translate(final double x, final double y, final double z) {
		final Vertex delta = new Vertex(x, y, z);
		final MoveAction moveAction = new MoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(final double x, final double y, final double z) {
		final Vertex center = selectionManager.getCenter();
		final Vertex delta = new Vertex(x - center.x, y - center.y, z - center.z);
		final MoveAction moveAction = new MoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction rotate(final double rotateX, final double rotateY, final double rotateZ) {

		final CompoundAction compoundAction = new CompoundAction("rotate",
				ListView.Util.of(new RotateAction(this, selectionManager.getCenter(), rotateX, (byte) 0, (byte) 2),
						new RotateAction(this, selectionManager.getCenter(), rotateY, (byte) 1, (byte) 0),
						new RotateAction(this, selectionManager.getCenter(), rotateZ, (byte) 1, (byte) 2)));
		compoundAction.redo();
		return compoundAction;
	}

	@Override
	public UndoAction setMatrix(final Collection<Bone> bones) {
		final Matrix mx = new Matrix();
		mx.setBones(new ArrayList<Bone>());
		for (final Bone bone : bones) {
			mx.add(bone);
		}
		final Map<GeosetVertex, List<Bone>> vertexToOldBoneReferences = new HashMap<>();
		for (final Vertex vert : selectionManager.getSelectedVertices()) {
			if (vert instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vert;
				vertexToOldBoneReferences.put(gv, new ArrayList<>(gv.getBoneAttachments()));
				gv.clearBoneAttachments();
				gv.addBoneAttachments(mx.getBones());
			}
		}
		return new SetMatrixAction(vertexToOldBoneReferences, bones);
	}

	@Override
	public UndoAction snapNormals() {
		final ArrayList<Vertex> oldLocations = new ArrayList<>();
		final ArrayList<Vertex> selectedNormals = new ArrayList<>();
		final Normal snapped = new Normal(0, 0, 1);
		for (final Vertex vertex : selectionManager.getSelectedVertices()) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vertex;
				if (gv.getNormal() != null) {
					oldLocations.add(new Vertex(gv.getNormal()));
					selectedNormals.add(gv.getNormal());
				} // else no normal to snap!!!
			}
		}
		final SnapNormalsAction temp = new SnapNormalsAction(selectedNormals, oldLocations, snapped);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public UndoAction deleteSelectedComponents(final ModelStructureChangeListener modelStructureChangeListener) {
		// TODO this code is RIPPED FROM MDLDispaly and is not good for general
		// cases
		// TODO this code operates directly on MODEL
		final ArrayList<Geoset> remGeosets = new ArrayList<>();// model.getGeosets()
		final ArrayList<Triangle> deletedTris = new ArrayList<>();
		final Collection<Vertex> selection = selectionManager.getSelectedVertices();
		for (final Vertex vertex : selection) {
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
		if (remGeosets.size() <= 0) {
			final DeleteAction temp = new DeleteAction(selection, deletedTris);
			return temp;
		} else {
			final SpecialDeleteAction temp = new SpecialDeleteAction(selection, deletedTris, remGeosets,
					model.getModel(), modelStructureChangeListener);
			modelStructureChangeListener.geosetsRemoved(remGeosets);
			return temp;
		}
	}

	@Override
	public UndoAction mirror(final byte dim, final boolean flipModel) {
		final MirrorModelAction mirror = new MirrorModelAction(selectionManager.getSelectedVertices(),
				CollectionUtils.toJava(model.getEditableIdObjects()), dim);
		mirror.redo();
		if (flipModel) {
			final UndoAction flipFacesAction = flipSelectedFaces();
			return new CompoundAction(mirror.actionName(), ListView.Util.of(mirror, flipFacesAction));
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
		final Collection<Vertex> selection = selectionManager.getSelectedVertices();
		final ArrayList<Vertex> oldLocations = new ArrayList<>();
		final ArrayList<Vertex> selectedNormals = new ArrayList<>();
		final Normal snapped = new Normal(0, 0, 1);
		for (final Vertex vertex : selection) {
			if (vertex instanceof GeosetVertex) {
				final GeosetVertex gv = (GeosetVertex) vertex;
				if (gv.getNormal() != null) {
					oldLocations.add(new Vertex(gv.getNormal()));
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
		return null;
	}

	@Override
	public UndoAction snapSelectedVertices() {
		final Collection<Vertex> selection = selectionManager.getSelectedVertices();
		final ArrayList<Vertex> oldLocations = new ArrayList<>();
		final Vertex cog = Vertex.centerOfGroup(selection);
		for (final Vertex vertex : selection) {
			oldLocations.add(new Vertex(vertex));
		}
		final SnapAction temp = new SnapAction(selection, oldLocations, cog);
		temp.redo();// a handy way to do the snapping!
		return temp;
	}

	@Override
	public UndoAction cloneSelectedComponents(final ModelStructureChangeListener modelStructureChangeListener,
			final ClonedNodeNamePicker clonedNodeNamePicker) {
		final List<Vertex> source = new ArrayList<>(selectionManager.getSelectedVertices());
		final ArrayList<Triangle> selTris = new ArrayList<>();
		final ArrayList<IdObject> selBones = new ArrayList<>();
		final ArrayList<IdObject> newBones = new ArrayList<>();
		final ArrayList<GeosetVertex> newVertices = new ArrayList<>();
		final ArrayList<Triangle> newTriangles = new ArrayList<>();
		for (int i = 0; i < source.size(); i++) {
			final Vertex vert = source.get(i);
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
			final Vertex vert = source.get(k);
			if (vert.getClass() == GeosetVertex.class) {
				final GeosetVertex gv = (GeosetVertex) vert;
				final ArrayList<Triangle> gvTriangles = new ArrayList<>();// gv.getTriangles());
				for (final Triangle tri : gv.getGeoset().getTriangles()) {
					if (tri.contains(gv)) {
						boolean good = true;
						for (final Vertex vTemp : tri.getAll()) {
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
			newTriangles.add(new Triangle(a, b, c, a.getGeoset()));
		}
		final Set<Vertex> newSelection = new HashSet<>();
		for (final Vertex ver : newVertices) {
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
		final CloneAction cloneAction = new CloneAction(model, source, modelStructureChangeListener,
				new VertexSelectionHelper() {
					@Override
					public void selectVertices(final Collection<Vertex> vertices) {
						selectByVertices(vertices);
					}
				}, selBones, newVerticesWithoutNulls, newTriangles, newBones, newSelection);
		cloneAction.redo();
		return cloneAction;
	}

	protected abstract void selectByVertices(Collection<Vertex> newSelection);
}
