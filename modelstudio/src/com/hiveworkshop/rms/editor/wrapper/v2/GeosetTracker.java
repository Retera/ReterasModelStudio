package com.hiveworkshop.rms.editor.wrapper.v2;

import com.hiveworkshop.rms.editor.model.Geoset;
import com.hiveworkshop.rms.editor.model.GeosetVertex;
import com.hiveworkshop.rms.editor.model.Triangle;
import com.hiveworkshop.rms.editor.model.util.ModelUtils;
import com.hiveworkshop.rms.util.Vec2;
import com.hiveworkshop.rms.util.Vec3;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeosetTracker {

	private final Set<GeosetVertex> selectedTVertices = new HashSet<>();
	private final Set<GeosetVertex> selectedVertices = new HashSet<>();

	private final Set<GeosetVertex> selEdTVertices = new HashSet<>();
	private final Set<GeosetVertex> selEdVertices = new HashSet<>();

	private final Set<GeosetVertex> hiddenVertices = new HashSet<>();
	private final Set<GeosetVertex> editableVertices = new HashSet<>();
	private final Set<GeosetVertex> notEditableVertices = new HashSet<>();

	private final Set<Geoset> editableGeosets = new HashSet<>();
	private final Set<Geoset> notEditableGeosets = new HashSet<>();
	private final Set<Geoset> visibleGeosets = new HashSet<>();
	private final Set<Geoset> hiddenGeosets = new HashSet<>();
	private Geoset highlightedGeoset;

	private boolean geosetsVisible = true;
	private boolean geosetsEditable = true;

	public GeosetTracker(Collection<Geoset> geosets, int version){
		for (Geoset geoset : geosets) {
			if (!ModelUtils.isLevelOfDetailSupported(version) || (geoset.getLevelOfDetail() == 0)) {
				editableGeosets.add(geoset);
				visibleGeosets.add(geoset);
				editableVertices.addAll(geoset.getVertices());
			} else {
				hiddenGeosets.add(geoset);
			}
		}
	}


	public Set<Geoset> getVisibleGeosets() {
//		return visibleGeosets;
		if (geosetsVisible) return visibleGeosets;
		return Collections.emptySet();
	}

	public Set<Geoset> getEditableGeosets() {
		if (geosetsVisible && geosetsEditable) return editableGeosets;
		return Collections.emptySet();
	}

	public void setHighlightedGeoset(Geoset highlightedGeoset) {
		this.highlightedGeoset = highlightedGeoset;
	}


	public void unhighlightGeoset(Geoset geoset) {
		if (highlightedGeoset == geoset) {
			highlightedGeoset = null;
		}
	}

	public Geoset getHighlightedGeoset() {
		return highlightedGeoset;
	}
	public Set<GeosetVertex> getHiddenVertices(){
		return hiddenVertices;
	}




	public void makeGeosetEditable(boolean editable, Geoset... geosets){
		if(editable){
			for (Geoset geoset : geosets){
				makeGeosetEditable(geoset);
			}
		} else {
			for (Geoset geoset : geosets){
				makeGeosetNotEditable(geoset);
			}
		}
	}

	public void makeGeosetEditable(boolean editable, Collection<Geoset> geosets){
		if(editable){
			for (Geoset geoset : geosets){
				makeGeosetEditable(geoset);
			}
		} else {
			for (Geoset geoset : geosets){
				makeGeosetNotEditable(geoset);
			}
		}
	}
	public void makeGeosetEditable(Geoset geoset) {
		editableGeosets.add(geoset);
		notEditableGeosets.remove(geoset);
		editableVertices.addAll(geoset.getVertices());
		notEditableVertices.removeAll(geoset.getVertices());
	}

	public void makeGeosetNotEditable(Geoset geoset) {
		editableGeosets.remove(geoset);
		notEditableGeosets.add(geoset);
		editableVertices.removeAll(geoset.getVertices());
		notEditableVertices.addAll(geoset.getVertices());
	}

	public void makeGeosetVisible(boolean visible, Geoset... geosets){
		if(visible){
			for (Geoset geoset : geosets){
				makeGeosetVisible(geoset);
			}
		} else {
			for (Geoset geoset : geosets){
				makeGeosetNotVisible(geoset);
			}
		}
	}
	public void makeGeosetVisible(boolean visible, Collection<Geoset> geosets){
		if(visible){
			for (Geoset geoset : geosets){
				makeGeosetVisible(geoset);
			}
		} else {
			for (Geoset geoset : geosets){
				makeGeosetNotVisible(geoset);
			}
		}
	}

	public void makeGeosetVisible(Geoset geoset) {
		visibleGeosets.add(geoset);
		hiddenGeosets.remove(geoset);
		hiddenVertices.removeAll(geoset.getVertices());
	}

	public void makeGeosetNotVisible(Geoset geoset) {
		visibleGeosets.remove(geoset);
		hiddenGeosets.add(geoset);
		hiddenVertices.addAll(geoset.getVertices());
	}

	public <T> boolean isVisible(T ob) {
		if(ob instanceof Geoset){
			return visibleGeosets.contains(ob) && geosetsVisible;
		} else if (ob instanceof GeosetVertex){
			return !hiddenVertices.contains(ob) && geosetsVisible;
		} else if (ob instanceof Triangle){
			Triangle tri = (Triangle) ob;
			return geosetsVisible
					&& !hiddenVertices.contains(tri.get(0))
					&& !hiddenVertices.contains(tri.get(1))
					&& !hiddenVertices.contains(tri.get(2));
		}
		return false;
	}
	public <T> boolean isEditable(T ob) {
		if(ob instanceof Geoset){
			return geosetsEditable && geosetsVisible && editableGeosets.contains(ob) && visibleGeosets.contains(ob);
		} else if (ob instanceof GeosetVertex){
			return geosetsEditable && geosetsVisible && editableVertices.contains(ob) && !hiddenVertices.contains(ob);
		} else if (ob instanceof Triangle){
			Triangle tri = (Triangle) ob;
			return geosetsEditable
					&& geosetsVisible
					&& editableVertices.containsAll(Arrays.asList(tri.getVerts()))
					&& !hiddenVertices.contains(tri.get(0))
					&& !hiddenVertices.contains(tri.get(1))
					&& !hiddenVertices.contains(tri.get(2));
		}
		return false;
	}

	public boolean isVisible(Geoset ob) {
		return visibleGeosets.contains(ob) && geosetsVisible;
	}

	public boolean isEditable(Geoset ob) {
		return geosetsEditable && geosetsVisible && editableGeosets.contains(ob) && visibleGeosets.contains(ob);
	}

	public boolean isEditable(GeosetVertex ob) {
		return geosetsEditable && geosetsVisible && editableVertices.contains(ob) && !hiddenVertices.contains(ob);
	}

	public boolean isEditable(Triangle ob) {
		return geosetsEditable && geosetsVisible && editableVertices.containsAll(Arrays.asList(ob.getVerts())) && !hiddenVertices.contains(ob.get(0)) && !hiddenVertices.contains(ob.get(1)) && !hiddenVertices.contains(ob.get(2));
	}

	public boolean shouldRender(Geoset ob) {
		return visibleGeosets.contains(ob) && geosetsVisible;
	}
	public boolean canSelect(Geoset ob) {
		return editableGeosets.contains(ob) && geosetsVisible && geosetsEditable;
	}

	public Set<GeosetVertex> getSelectedVertices() {
		// ToDo not editable stuff should not be in the selection (but maybe be added back once editable again)
		if (!geosetsEditable || !geosetsVisible) return Collections.emptySet();
		selEdVertices.clear();
		selEdVertices.addAll(selectedVertices);
		selEdVertices.removeAll(hiddenVertices);
		selEdVertices.removeAll(notEditableVertices);
		return selEdVertices;
	}

	public void setSelectedVertices(Collection<GeosetVertex> geosetVertices) {
		selectedVertices.clear();
		selectedVertices.addAll(geosetVertices);
	}

	public Set<Triangle> getSelectedTriangles() {
		Set<Triangle> selTris = new HashSet<>();
		for (GeosetVertex vertex : selectedVertices) {
			for (Triangle triangle : vertex.getTriangles()) {
				if (selectedVertices.containsAll(Arrays.asList(triangle.getVerts()))) {
					selTris.add(triangle);
				}
			}
		}
		return selTris;
	}

	public void addSelectedTris(Collection<Triangle> triangles) {
		for (Triangle triangle : triangles) {
			selectedVertices.addAll(Arrays.asList(triangle.getVerts()));
		}
	}

	public void setSelectedTris(Collection<Triangle> triangles) {
		selectedVertices.clear();
		for (Triangle triangle : triangles) {
			selectedVertices.addAll(Arrays.asList(triangle.getVerts()));
		}
	}

	public void removeSelectedTris(Collection<Triangle> triangles) {
		for (Triangle triangle : triangles) {
			for (GeosetVertex vertex : triangle.getVerts()) {
				selectedVertices.remove(vertex);
			}
		}
	}

	public void addSelectedVertex(GeosetVertex geosetVertex) {
		selectedVertices.add(geosetVertex);
	}

	public void removeSelectedVertex(GeosetVertex geosetVertex) {
		selectedVertices.remove(geosetVertex);
	}

	public void addSelectedVertices(Collection<GeosetVertex> geosetVertices) {
		selectedVertices.addAll(geosetVertices);
	}

	public void removeSelectedVertices(Collection<GeosetVertex> geosetVertices) {
		selectedVertices.removeAll(geosetVertices);
	}

	public void clearSelectedVertices() {
		selectedVertices.clear();
	}

	public boolean isHidden(GeosetVertex vertex) {
		return hiddenVertices.contains(vertex) || hiddenGeosets.contains(vertex.getGeoset());
	}
	public boolean isSelected(GeosetVertex geosetVertex) {
		return selectedVertices.contains(geosetVertex);
	}

	public void invertVertSelection() {
		Set<GeosetVertex> tempVerts = new HashSet<>();
		for (Geoset geoset : editableGeosets) {
			tempVerts.addAll(geoset.getVertices());
		}
		tempVerts.removeAll(selectedVertices);
		setSelectedVertices(tempVerts);
	}

	public void selectAllVerts() {
		for (Geoset geoset : editableGeosets) {
			selectedVertices.addAll(geoset.getVertices());
		}
	}

	public void selectAll() {
		if (geosetsVisible && geosetsEditable) {
			for (Geoset geoset : editableGeosets) {
				if (isEditable(geoset)) {
					selectedVertices.addAll(geoset.getVertices());
					selectedVertices.removeIf(this::isHidden);
				}
			}
		}
	}

	public void addSelectedTVertex(GeosetVertex geosetVertex) {
		selectedTVertices.add(geosetVertex);
	}

	public void addSelectedTVertices(Collection<GeosetVertex> geosetVertices) {
		selectedTVertices.addAll(geosetVertices);
	}

	public void hideVertices(Collection<GeosetVertex> geosetVertices) {
		hiddenVertices.addAll(geosetVertices);
	}
	public void showVertices(Collection<GeosetVertex> geosetVertices) {
		hiddenVertices.removeAll(geosetVertices);
	}

	public void unHideAllVertices() {
		hiddenVertices.clear();
		for (Geoset geoset : hiddenGeosets) {
			hiddenVertices.addAll(geoset.getVertices());
		}
	}

	public void clearSelectedTVertices() {
		selectedTVertices.clear();
	}

	public void removeSelectedTVertices(Collection<GeosetVertex> geosetVertices) {
		selectedTVertices.removeAll(geosetVertices);
	}

	public boolean isTSelected(GeosetVertex geosetVertex) {
		return selectedTVertices.contains(geosetVertex);
	}

	public void selectAllTVerts() {
		for (Geoset geoset : editableGeosets) {
			selectedTVertices.addAll(geoset.getVertices());
		}
	}

	public Vec2 getTSelectionCenter(){
		Set<Vec2> selectedPoints = new HashSet<>();
		selectedTVertices.stream().filter(editableVertices::contains).forEach(v -> selectedPoints.add(v.getTVertex(0)));

		return Vec2.centerOfGroup(selectedPoints);
	}

	public Set<GeosetVertex> getSelectedTVertices() {
		// ToDo not editable stuff should not be in the selection (but maybe be added back once editable again)
		return selectedTVertices;
	}

	public void setSelectedTVertices(Collection<GeosetVertex> geosetVertices) {
		selectedTVertices.clear();
		selectedTVertices.addAll(geosetVertices);
	}


	public <T> boolean isInEditable(T obj) {
		if (obj instanceof GeosetVertex) {
			return editableVertices.contains(obj);
		} else if (obj instanceof Geoset) {
			return editableGeosets.contains(obj);
		}
//		else if (obj instanceof Triangle) {
//			return .contains(obj);
//		}
		return false;
	}

	public <T> boolean isInVisible(T obj) {
		if (obj instanceof GeosetVertex) {
//			System.out.println("GeosetVertex inVissible:" + hiddenVertices.contains(obj));
			return !hiddenVertices.contains(obj);
		} else if (obj instanceof Geoset) {
//			System.out.println("Geoset inVissible:" + visibleGeosets.contains(obj));
			return visibleGeosets.contains(obj);
		}
		return false;
	}

	public void setGeosetsVisible(boolean visible) {
		geosetsVisible = visible;
	}

	public boolean isGeosetsVisible() {
		return geosetsVisible;
	}

	public void setGeosetsEditable(boolean editable) {
		geosetsEditable = editable;
	}

	public boolean isGeosetsEditable() {
		return geosetsEditable;
	}




	public void updateElements(Collection<Geoset> geosets) {
		Set<Geoset> modelGeosets = new HashSet<>(geosets);

		Set<Geoset> geosetsToRemove = Stream.of(visibleGeosets, hiddenGeosets, notEditableGeosets, editableGeosets)
				.flatMap(Collection::stream)
				.filter(g -> !modelGeosets.contains(g))
				.collect(Collectors.toSet());
		visibleGeosets.removeAll(geosetsToRemove);
		hiddenGeosets.removeAll(geosetsToRemove);
		notEditableGeosets.removeAll(geosetsToRemove);
		editableGeosets.removeAll(geosetsToRemove);

		for (Geoset geoset : geosetsToRemove) {
			geoset.getVertices().forEach(this::removeFromAll);
		}

		for (Geoset geoset : modelGeosets) {
			if (!visibleGeosets.contains(geoset) && !hiddenGeosets.contains(geoset) && !notEditableGeosets.contains(geoset) && !editableGeosets.contains(geoset)){
				visibleGeosets.add(geoset);
				editableGeosets.add(geoset);
				editableVertices.addAll(geoset.getVertices());
			} else {
				geoset.getVertices().stream()
						.filter(v -> !editableVertices.contains(v) || !notEditableVertices.contains(v))
						.forEach(editableVertices::add);
			}
		}

		hiddenVertices.removeIf(g -> !g.getGeoset().contains(g));
		editableVertices.removeIf(g -> !g.getGeoset().contains(g));
		notEditableVertices.removeIf(g -> !g.getGeoset().contains(g));
		selectedVertices.removeIf(g -> !g.getGeoset().contains(g));
	}

	private void removeFromAll(GeosetVertex vertex){
		hiddenVertices.remove(vertex);
		editableVertices.remove(vertex);
		notEditableVertices.remove(vertex);
		selectedVertices.remove(vertex);
	}

	public void collectSelectionCenter(Set<Vec3> selectedPoints){
		selectedVertices.stream().filter(editableVertices::contains).filter(v -> !hiddenVertices.contains(v)).forEach(selectedPoints::add);
	}
}
