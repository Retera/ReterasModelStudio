package com.hiveworkshop.wc3.gui.modeledit.actions.newsys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.gui.modeledit.selection.VertexSelectionHelper;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.GeosetVertex;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Layer.FilterMode;
import com.hiveworkshop.wc3.mdl.LayerShader;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ShaderTextureTypeHD;
import com.hiveworkshop.wc3.mdl.Triangle;
import com.hiveworkshop.wc3.mdl.Vertex;

public final class TeamColorAddAction<T> implements UndoAction {

	private List<Triangle> trianglesMovedToSeparateGeo;
	private final List<Geoset> geosetsCreated;
	private final EditableModel model;
	private final Collection<Triangle> trisToSeparate;
	private final ModelStructureChangeListener modelStructureChangeListener;
	private final SelectionManager<T> selectionManager;
	private final Collection<T> selection;
	private final Collection<Vertex> newVerticesToSelect;
	private final VertexSelectionHelper vertexSelectionHelper;

	public TeamColorAddAction(final Collection<Triangle> trisToSeparate, final EditableModel model,
			final ModelStructureChangeListener modelStructureChangeListener, final SelectionManager<T> selectionManager,
			final VertexSelectionHelper vertexSelectionHelper) {
		this.trisToSeparate = trisToSeparate;
		this.model = model;
		this.modelStructureChangeListener = modelStructureChangeListener;
		this.selectionManager = selectionManager;
		this.vertexSelectionHelper = vertexSelectionHelper;
		this.geosetsCreated = new ArrayList<>();
		this.newVerticesToSelect = new ArrayList<>();
		final Set<GeosetVertex> verticesInTheTriangles = new HashSet<>();
		final Set<Geoset> geosetsToCopy = new HashSet<>();
		for (final Triangle tri : trisToSeparate) {
			for (final GeosetVertex gv : tri.getVerts()) {
				verticesInTheTriangles.add(gv);
			}
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
			for (final String flag : geoset.getFlags()) {
				geosetCreated.addFlag(flag);
			}
			geosetCreated.setSelectionGroup(geoset.getSelectionGroup());
			final GeosetAnim geosetAnim = geoset.getGeosetAnim();
			if (geosetAnim != null) {
				geosetCreated.setGeosetAnim(new GeosetAnim(geosetCreated, geosetAnim));
			}
			geosetCreated.setParentModel(model);
			final Material newMaterial = new Material(geoset.getMaterial());
			if (newMaterial.getLayers().size() == 1 && newMaterial.firstLayer().getLayerShader() == LayerShader.HD) {
				newMaterial.firstLayer().setLayerShader(LayerShader.SD);
				final EnumMap<ShaderTextureTypeHD, Bitmap> shaderTextures = newMaterial.firstLayer()
						.getShaderTextures();
				final Bitmap diffuseTexture = shaderTextures.get(ShaderTextureTypeHD.Diffuse);
				shaderTextures.clear();
				shaderTextures.put(ShaderTextureTypeHD.Diffuse, diffuseTexture);
			}

			if (newMaterial.getLayers().get(0).getFilterMode() == FilterMode.NONE) {
				newMaterial.getLayers().get(0).setFilterMode(FilterMode.BLEND);
			}
			final Layer teamColorLayer = new Layer(FilterMode.NONE.getMdlText(), new Bitmap("", 1));
			teamColorLayer.add("Unshaded");
			if (geoset.getMaterial().firstLayer().getFlags().contains("TwoSided")) {
				teamColorLayer.add("TwoSided");
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
			a.getTriangles().add(newTriangle);
			b.getTriangles().add(newTriangle);
			c.getTriangles().add(newTriangle);
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
				gv.getTriangles().add(tri);
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
				gv.getTriangles().remove(tri);
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
