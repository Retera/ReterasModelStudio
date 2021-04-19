package com.hiveworkshop.rms.ui.application.actions.mesh;

import com.hiveworkshop.rms.editor.model.*;
import com.hiveworkshop.rms.parsers.mdlx.MdlxLayer.FilterMode;
import com.hiveworkshop.rms.ui.application.edit.ModelStructureChangeListener;
import com.hiveworkshop.rms.ui.gui.modeledit.UndoAction;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.rms.ui.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;

public final class TeamColorAddAction<T> implements UndoAction {

	private List<Triangle> trianglesMovedToSeparateGeo;
	private final List<Geoset> geosetsCreated;
	private final EditableModel model;
	private final Collection<Triangle> trisToSeparate;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final SelectionManager<T> selectionManager;
	private final Collection<T> selection;
	private final Collection<Vec3> newVerticesToSelect;
	private final VertexSelectionHelper vertexSelectionHelper;

	public TeamColorAddAction(final Collection<Triangle> trisToSeparate, final EditableModel model,
			final ModelStructureChangeListener modelStructureChangeListener, final SelectionManager<T> selectionManager,
			final VertexSelectionHelper vertexSelectionHelper) {
		this.trisToSeparate = trisToSeparate;
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.selectionManager = selectionManager;
		this.vertexSelectionHelper = vertexSelectionHelper;
		geosetsCreated = new ArrayList<>();
		newVerticesToSelect = new ArrayList<>();
		final Set<GeosetVertex> verticesInTheTriangles = new HashSet<>();
		final Set<Geoset> geosetsToCopy = new HashSet<>();
		for (final Triangle tri : trisToSeparate) {
            verticesInTheTriangles.addAll(Arrays.asList(tri.getVerts()));
			geosetsToCopy.add(tri.getGeoset());
		}
		final Map<Geoset, Geoset> oldGeoToNewGeo = new HashMap<>();
		final Map<GeosetVertex, GeosetVertex> oldVertToNewVert = new HashMap<>();
		for (final Geoset geoset : geosetsToCopy) {
			final Geoset geosetCreated = new Geoset();
			if (geoset.getExtents() != null) {
				geosetCreated.setExtents(new ExtLog(geoset.getExtents()));
			}
			for (final Animation anim : geoset.getAnims()) {
				geosetCreated.add(new Animation(anim));
			}
			geosetCreated.setUnselectable(geoset.getUnselectable());
			geosetCreated.setSelectionGroup(geoset.getSelectionGroup());
			final GeosetAnim geosetAnim = geoset.getGeosetAnim();
			if (geosetAnim != null) {
				geosetCreated.setGeosetAnim(new GeosetAnim(geosetCreated, geosetAnim));
			}
			geosetCreated.setParentModel(model);
			final Material newMaterial = new Material(geoset.getMaterial());
			if (newMaterial.getLayers().get(0).getFilterMode() == FilterMode.NONE) {
				newMaterial.getLayers().get(0).setFilterMode(FilterMode.BLEND);
			}
			final Layer teamColorLayer = new Layer(FilterMode.NONE.toString(), new Bitmap("", 1));
			teamColorLayer.setUnshaded(true);
			if (geoset.getMaterial().firstLayer().getTwoSided()) {
				teamColorLayer.setTwoSided(true);
			}
			newMaterial.getLayers().add(0, teamColorLayer);
			geosetCreated.setMaterial(newMaterial);
			oldGeoToNewGeo.put(geoset, geosetCreated);
			geosetsCreated.add(geosetCreated);
		}
		for (final GeosetVertex vertex : verticesInTheTriangles) {
			final GeosetVertex copy = new GeosetVertex(vertex);
			final Geoset newGeoset = oldGeoToNewGeo.get(vertex.getGeoset());
			copy.setGeoset(newGeoset);
			newGeoset.add(copy);
			oldVertToNewVert.put(vertex, copy);
			newVerticesToSelect.add(copy);
		}
		for (final Triangle tri : trisToSeparate) {
			final GeosetVertex a, b, c;
			a = oldVertToNewVert.get(tri.get(0));
			b = oldVertToNewVert.get(tri.get(1));
			c = oldVertToNewVert.get(tri.get(2));
			final Geoset newGeoset = oldGeoToNewGeo.get(tri.getGeoset());
			final Triangle newTriangle = new Triangle(a, b, c, newGeoset);
			newGeoset.add(newTriangle);
//			a.addTriangle(newTriangle);
//			b.addTriangle(newTriangle);
//			c.addTriangle(newTriangle);
		}
		selection = new ArrayList<>(selectionManager.getSelection());
	}

	@Override
	public void undo() {
		for (final Geoset geoset : geosetsCreated) {
			model.remove(geoset);
		}
		modelStructureChangeListener.geosetsRemoved(geosetsCreated);
		for (final Triangle tri : trisToSeparate) {
			final Geoset geoset = tri.getGeoset();
			for (final GeosetVertex gv : tri.getVerts()) {
				gv.addTriangle(tri);
				if (!geoset.getVertices().contains(gv)) {
					geoset.add(gv);
				}
			}
			geoset.add(tri);
		}
		selectionManager.setSelection(selection);
	}

	@Override
	public void redo() {
		for (final Geoset geoset : geosetsCreated) {
			model.add(geoset);
		}
		for (final Triangle tri : trisToSeparate) {
			final Geoset geoset = tri.getGeoset();
			for (final GeosetVertex gv : tri.getVerts()) {
				gv.removeTriangle(tri);
				if (gv.getTriangles().isEmpty()) {
					geoset.remove(gv);
				}
			}
			geoset.removeTriangle(tri);
		}
		modelStructureChangeListener.geosetsAdded(geosetsCreated);
		selectionManager.removeSelection(selection);
		vertexSelectionHelper.selectVertices(newVerticesToSelect);
	}

	@Override
	public String actionName() {
		return "add team color layer";
	}

}
