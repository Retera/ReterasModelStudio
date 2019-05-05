package com.hiveworkshop.wc3.gui.animedit;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import com.etheller.collections.ListView;
import com.hiveworkshop.wc3.gui.ProgramPreferences;
import com.hiveworkshop.wc3.gui.modeledit.CoordinateSystem;
import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.cutpaste.CopiedModelData;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.AbstractSelectingEditor;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.ModelEditorActionType;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.AddTimelineAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.RotationKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.ScalingKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.SquatToolKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.animation.TranslationKeyframeAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.editor.StaticMeshMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.MakeNotEditableAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.selection.SetSelectionAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.CloneAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.RigAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.CompoundAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericMoveAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericRotateAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.util.GenericScaleAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.ClonedNodeNamePicker;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.listener.EditabilityToggleHandler;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponent;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectableComponentVisitor;
import com.hiveworkshop.wc3.gui.modeledit.selection.SelectionManager;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.render3d.RenderModel;
import com.hiveworkshop.wc3.mdl.render3d.RenderNode;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelView;
import com.hiveworkshop.wc3.mdl.v2.timelines.InterpolationType;
import com.hiveworkshop.wc3.mdl.v2.visitor.IdObjectVisitor;

public class NodeAnimationModelEditor extends AbstractSelectingEditor<IdObject> {
	private final ProgramPreferences programPreferences;
	private final GenericSelectorVisitor genericSelectorVisitor;
	private final SelectionAtPointTester selectionAtPointTester;
	private final ModelView model;
	private final RenderModel renderModel;
	private final ModelStructureChangeListener structureChangeListener;

	public NodeAnimationModelEditor(final ModelView model, final ProgramPreferences programPreferences,
			final SelectionManager<IdObject> selectionManager, final RenderModel renderModel,
			final ModelStructureChangeListener structureChangeListener) {
		super(selectionManager);
		this.model = model;
		this.programPreferences = programPreferences;
		this.structureChangeListener = structureChangeListener;
		this.genericSelectorVisitor = new GenericSelectorVisitor();
		this.selectionAtPointTester = new SelectionAtPointTester();
		this.renderModel = renderModel;
	}

	@Override
	public UndoAction autoCenterSelectedBones() {
		throw new WrongModeException("Unable to autocenter bones in Animation Editor");
	}

	@Override
	public UndoAction setSelectedBoneName(final String name) {
		throw new WrongModeException("Unable to change bone names in Animation Editor");
	}

	@Override
	public UndoAction addTeamColor() {
		throw new WrongModeException("Unable to add team color in Animation Editor");
	}

	@Override
	public void selectByVertices(final Collection<? extends Vertex> newSelection) {
		final Set<IdObject> newlySelectedObjects = new HashSet<>();
		for (final IdObject object : model.getEditableIdObjects()) {
			if (newSelection.contains(object.getPivotPoint())) {
				newlySelectedObjects.add(object);
			}
			object.apply(new IdObjectVisitor() {
				@Override
				public void ribbonEmitter(final RibbonEmitter particleEmitter) {

				}

				@Override
				public void particleEmitter2(final ParticleEmitter2 particleEmitter) {

				}

				@Override
				public void particleEmitter(final ParticleEmitter particleEmitter) {

				}

				@Override
				public void light(final Light light) {

				}

				@Override
				public void helper(final Helper object) {

				}

				@Override
				public void eventObject(final EventObject eventObject) {

				}

				@Override
				public void collisionShape(final CollisionShape collisionShape) {
					for (final Vertex vertex : collisionShape.getVertices()) {
						if (newSelection.contains(vertex)) {
							newlySelectedObjects.add(collisionShape);
						}
					}
				}

				@Override
				public void camera(final Camera camera) {

				}

				@Override
				public void bone(final Bone object) {

				}

				@Override
				public void attachment(final Attachment attachment) {

				}
			});
		}
		// TODO cameras in a second CameraAnimationEditor
		selectionManager.setSelection(newlySelectedObjects);
	}

	@Override
	public UndoAction expandSelection() {
		throw new WrongModeException("Unable to expand selection in Node Animation Editor");
	}

	@Override
	public UndoAction invertSelection() {
		final ArrayList<IdObject> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<IdObject> invertedSelection = new HashSet<>(selectionManager.getSelection());
		for (final IdObject node : model.getEditableIdObjects()) {
			toggleSelection(invertedSelection, node);
		}
		selectionManager.setSelection(invertedSelection);
		return new SetSelectionAction<>(invertedSelection, oldSelection, selectionManager, "invert selection");
	}

	private void toggleSelection(final Set<IdObject> selection, final IdObject position) {
		if (selection.contains(position)) {
			selection.remove(position);
		} else {
			selection.add(position);
		}
	}

	@Override
	public UndoAction selectAll() {
		final ArrayList<IdObject> oldSelection = new ArrayList<>(selectionManager.getSelection());
		final Set<IdObject> allSelection = new HashSet<>();
		for (final IdObject node : model.getEditableIdObjects()) {
			allSelection.add(node);
		}
		selectionManager.setSelection(allSelection);
		return new SetSelectionAction<>(allSelection, oldSelection, selectionManager, "select all");
	}

	@Override
	protected List<IdObject> genericSelect(final Rectangle2D region, final CoordinateSystem coordinateSystem) {
		final List<IdObject> selectedItems = new ArrayList<>();
		final double startingClickX = region.getX();
		final double startingClickY = region.getY();
		final double endingClickX = region.getX() + region.getWidth();
		final double endingClickY = region.getY() + region.getHeight();

		final double minX = Math.min(startingClickX, endingClickX);
		final double minY = Math.min(startingClickY, endingClickY);
		final double maxX = Math.max(startingClickX, endingClickX);
		final double maxY = Math.max(startingClickY, endingClickY);
		final Rectangle2D area = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
		final IdObjectVisitor visitor = genericSelectorVisitor.reset(selectedItems, area, coordinateSystem);
		for (final IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		return selectedItems;
	}

	@Override
	public boolean canSelectAt(final Point point, final CoordinateSystem axes) {
		final IdObjectVisitor visitor = selectionAtPointTester.reset(axes, point);
		for (final IdObject object : model.getEditableIdObjects()) {
			object.apply(visitor);
		}
		return selectionAtPointTester.isMouseOverVertex();
	}

	private static final Vector4f pivotHeap = new Vector4f();

	public static void hitTest(final List<IdObject> selectedItems, final Rectangle2D area, final Vertex geosetVertex,
			final CoordinateSystem coordinateSystem, final double vertexSize, final IdObject object,
			final RenderModel renderModel) {
		final RenderNode renderNode = renderModel.getRenderNode(object);
		pivotHeap.x = (float) geosetVertex.x;
		pivotHeap.y = (float) geosetVertex.y;
		pivotHeap.z = (float) geosetVertex.z;
		pivotHeap.w = 1;
		Matrix4f.transform(renderNode.getWorldMatrix(), pivotHeap, pivotHeap);
		final byte dim1 = coordinateSystem.getPortFirstXYZ();
		final byte dim2 = coordinateSystem.getPortSecondXYZ();
		final double minX = coordinateSystem.convertX(area.getMinX());
		final double minY = coordinateSystem.convertY(area.getMinY());
		final double maxX = coordinateSystem.convertX(area.getMaxX());
		final double maxY = coordinateSystem.convertY(area.getMaxY());
		final double vertexX = Vertex.getCoord(pivotHeap, dim1);
		final double x = coordinateSystem.convertX(vertexX);
		final double vertexY = Vertex.getCoord(pivotHeap, dim2);
		final double y = coordinateSystem.convertY(vertexY);
		if (distance(x, y, minX, minY) <= vertexSize / 2.0 || distance(x, y, maxX, maxY) <= vertexSize / 2.0
				|| area.contains(vertexX, vertexY)) {
			selectedItems.add(object);
		}
	}

	public static boolean hitTest(final Vertex vertex, final Point2D point, final CoordinateSystem coordinateSystem,
			final double vertexSize, final Matrix4f worldMatrix) {
		pivotHeap.x = (float) vertex.x;
		pivotHeap.y = (float) vertex.y;
		pivotHeap.z = (float) vertex.z;
		pivotHeap.w = 1;
		Matrix4f.transform(worldMatrix, pivotHeap, pivotHeap);
		final double x = coordinateSystem.convertX(Vertex.getCoord(pivotHeap, coordinateSystem.getPortFirstXYZ()));
		final double y = coordinateSystem.convertY(Vertex.getCoord(pivotHeap, coordinateSystem.getPortSecondXYZ()));
		final double px = coordinateSystem.convertX(point.getX());
		final double py = coordinateSystem.convertY(point.getY());
		return Point2D.distance(px, py, x, y) <= vertexSize / 2.0;
	}

	public static double distance(final double vertexX, final double vertexY, final double x, final double y) {
		final double dx = x - vertexX;
		final double dy = y - vertexY;
		return Math.sqrt(dx * dx + dy * dy);
	}

	@Override
	protected UndoAction buildHideComponentAction(final ListView<? extends SelectableComponent> selectableComponents,
			final EditabilityToggleHandler editabilityToggleHandler, final Runnable refreshGUIRunnable) {
		final List<IdObject> previousSelection = new ArrayList<>(selectionManager.getSelection());
		final List<IdObject> possibleVerticesToTruncate = new ArrayList<>();
		for (final SelectableComponent component : selectableComponents) {
			component.visit(new SelectableComponentVisitor() {
				@Override
				public void accept(final Camera camera) {
				}

				@Override
				public void accept(final IdObject node) {
					possibleVerticesToTruncate.add(node);
				}

				@Override
				public void accept(final Geoset geoset) {
				}
			});
		}
		final Runnable truncateSelectionRunnable = new Runnable() {

			@Override
			public void run() {
				selectionManager.removeSelection(possibleVerticesToTruncate);
			}
		};

		final Runnable unTruncateSelectionRunnable = new Runnable() {
			@Override
			public void run() {
				selectionManager.setSelection(previousSelection);
			}
		};
		return new MakeNotEditableAction(editabilityToggleHandler, truncateSelectionRunnable,
				unTruncateSelectionRunnable, refreshGUIRunnable);
	}

	private final class SelectionAtPointTester implements IdObjectVisitor {
		private CoordinateSystem axes;
		private Point point;
		private boolean mouseOverVertex;

		private SelectionAtPointTester reset(final CoordinateSystem axes, final Point point) {
			this.axes = axes;
			this.point = point;
			this.mouseOverVertex = false;
			return this;
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		private void handleDefaultNode(final Point point, final CoordinateSystem axes, final IdObject node) {
			final Matrix4f worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes,
					node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes) * 2, worldMatrix)) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			handleDefaultNode(point, axes, particleEmitter);
		}

		@Override
		public void light(final Light light) {
			handleDefaultNode(point, axes, light);
		}

		@Override
		public void helper(final Helper node) {
			final Matrix4f worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes,
					node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes), worldMatrix)) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			handleDefaultNode(point, axes, eventObject);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			handleDefaultNode(point, axes, collisionShape);
		}

		@Override
		public void camera(final Camera camera) {
			System.err.println("CAMERA processed in NodeAnimationModelEditor!!!");
			// if (hitTest(camera.getPosition(), CoordinateSystem.Util.geom(axes, point),
			// axes,
			// programPreferences.getVertexSize(), worldMatrix)) {
			// mouseOverVertex = true;
			// }
			// if (hitTest(camera.getTargetPosition(), CoordinateSystem.Util.geom(axes,
			// point), axes,
			// programPreferences.getVertexSize(), worldMatrix)) {
			// mouseOverVertex = true;
			// }
		}

		@Override
		public void bone(final Bone node) {
			final Matrix4f worldMatrix = renderModel.getRenderNode(node).getWorldMatrix();
			if (hitTest(node.getPivotPoint(), CoordinateSystem.Util.geom(axes, point), axes,
					node.getClickRadius(axes) * CoordinateSystem.Util.getZoom(axes), worldMatrix)) {
				mouseOverVertex = true;
			}
		}

		@Override
		public void attachment(final Attachment attachment) {
			handleDefaultNode(point, axes, attachment);
		}

		public boolean isMouseOverVertex() {
			return mouseOverVertex;
		}
	}

	private final class GenericSelectorVisitor implements IdObjectVisitor {
		private List<IdObject> selectedItems;
		private Rectangle2D area;
		private CoordinateSystem coordinateSystem;

		private GenericSelectorVisitor reset(final List<IdObject> selectedItems, final Rectangle2D area,
				final CoordinateSystem coordinateSystem) {
			this.selectedItems = selectedItems;
			this.area = area;
			this.coordinateSystem = coordinateSystem;
			return this;
		}

		@Override
		public void ribbonEmitter(final RibbonEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					particleEmitter, renderModel);
		}

		@Override
		public void particleEmitter2(final ParticleEmitter2 particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					particleEmitter, renderModel);
		}

		@Override
		public void particleEmitter(final ParticleEmitter particleEmitter) {
			hitTest(selectedItems, area, particleEmitter.getPivotPoint(), coordinateSystem,
					particleEmitter.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					particleEmitter, renderModel);
		}

		@Override
		public void light(final Light light) {
			hitTest(selectedItems, area, light.getPivotPoint(), coordinateSystem,
					light.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2, light,
					renderModel);
		}

		@Override
		public void helper(final Helper object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem,
					object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object,
					renderModel);
		}

		@Override
		public void eventObject(final EventObject eventObject) {
			hitTest(selectedItems, area, eventObject.getPivotPoint(), coordinateSystem,
					eventObject.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2,
					eventObject, renderModel);
		}

		@Override
		public void collisionShape(final CollisionShape collisionShape) {
			hitTest(selectedItems, area, collisionShape.getPivotPoint(), coordinateSystem,
					collisionShape.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem)
							* 2,
					collisionShape, renderModel);
		}

		@Override
		public void camera(final Camera camera) {
			System.err.println("Attempted to process camera with Node Animation Editor generic selector!!!");
		}

		@Override
		public void bone(final Bone object) {
			hitTest(selectedItems, area, object.getPivotPoint(), coordinateSystem,
					object.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem), object,
					renderModel);
		}

		@Override
		public void attachment(final Attachment attachment) {
			hitTest(selectedItems, area, attachment.getPivotPoint(), coordinateSystem,
					attachment.getClickRadius(coordinateSystem) * CoordinateSystem.Util.getZoom(coordinateSystem) * 2,
					attachment, renderModel);
		}

	}

	@Override
	public CopiedModelData copySelection() {
		throw new WrongModeException("Unable to copy selection in animation editor");
	}

	@Override
	public UndoAction deleteSelectedComponents() {
		throw new WrongModeException("Unable to delete selection in animation editor");
	}

	@Override
	public UndoAction setMatrix(final Collection<Bone> bones) {
		throw new WrongModeException("Unable to set Matrix in Animation Editor");
	}

	@Override
	public UndoAction snapNormals() {
		throw new WrongModeException("Unable to modify normals in Animation Editor");
	}

	@Override
	public UndoAction recalcNormals() {
		throw new WrongModeException("Unable to modify normals in Animation Editor");
	}

	@Override
	public UndoAction mirror(final byte dim, final boolean flipModel, final double centerX, final double centerY,
			final double centerZ) {
		throw new WrongModeException("Mirror has not yet been coded in Animation Editor");
	}

	@Override
	public UndoAction flipSelectedFaces() {
		throw new WrongModeException("Unable to flip faces in Animation Editor");
	}

	@Override
	public UndoAction flipSelectedNormals() {
		throw new WrongModeException("Unable to flip normals in Animation Editor");
	}

	@Override
	public UndoAction snapSelectedVertices() {
		throw new WrongModeException("Unable to snap vertices in Animation Editor");
	}

	@Override
	public UndoAction snapSelectedNormals() {
		throw new WrongModeException("Unable to modify normals in Animation Editor");
	}

	@Override
	public UndoAction beginExtrudingSelection() {
		throw new WrongModeException("Unable to extrude in Animation Editor");
	}

	@Override
	public UndoAction beginExtendingSelection() {
		throw new WrongModeException("Unable to extrude in Animation Editor");
	}

	@Override
	public CloneAction cloneSelectedComponents(final ClonedNodeNamePicker clonedNodeNamePicker) {
		throw new WrongModeException("Unable to clone components in Animation Editor");
	}

	@Override
	public UndoAction addVertex(final double x, final double y, final double z,
			final Vertex preferredNormalFacingVector) {
		throw new WrongModeException("Unable to add vertices in Animation Editor");
	}

	@Override
	public GenericMoveAction addPlane(final double x, final double y, final double x2, final double y2, final byte dim1,
			final byte dim2, final Vertex facingVector, final int numberOfWidthSegments,
			final int numberOfHeightSegments) {
		throw new WrongModeException("Unable to add plane in Animation Editor");
	}

	@Override
	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ) {
		throw new UnsupportedOperationException("Unable to scale directly in animation mode, use other system");
	}

	public void rawScale(final double centerX, final double centerY, final double centerZ, final double scaleX,
			final double scaleY, final double scaleZ, final Map<IdObject, Vector3f> nodeToLocalScale) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateScalingKeyframe(renderModel, scaleX, scaleY, scaleZ, nodeToLocalScale.get(idObject));
		}
	}

	@Override
	public void rawTranslate(final double x, final double y, final double z) {
		// throw new UnsupportedOperationException("Unable to translate directly in
		// animation mode, use other system");
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, new Vector3f());
		}
	}

	public void rawTranslate(final double x, final double y, final double z,
			final Map<IdObject, Vector3f> nodeToLocalTranslation) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateTranslationKeyframe(renderModel, x, y, z, nodeToLocalTranslation.get(idObject));
		}
	}

	@Override
	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ) {
		throw new UnsupportedOperationException("Unable to rotate directly in animation mode, use other system");
	}

	public void rawRotate2d(final double centerX, final double centerY, final double centerZ, final double radians,
			final byte firstXYZ, final byte secondXYZ, final Map<IdObject, Quaternion> nodeToLocalRotation) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ,
					nodeToLocalRotation.get(idObject));
		}
	}

	public void rawSquatToolRotate2d(final double centerX, final double centerY, final double centerZ,
			final double radians, final byte firstXYZ, final byte secondXYZ,
			final Map<IdObject, Quaternion> nodeToLocalRotation) {
		for (final IdObject idObject : selectionManager.getSelection()) {
			idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, radians, firstXYZ, secondXYZ,
					nodeToLocalRotation.get(idObject));
		}
		for (final IdObject idObject : model.getModel().getIdObjects()) {
			if (selectionManager.getSelection().contains(idObject.getParent()) && (idObject.getClass() == Bone.class
					&& idObject.getParent().getClass() == Bone.class
					|| idObject.getClass() == Helper.class && idObject.getParent().getClass() == Helper.class)) {
				idObject.updateRotationKeyframe(renderModel, centerX, centerY, centerZ, -radians, firstXYZ, secondXYZ,
						nodeToLocalRotation.get(idObject));
			}
		}
	}

	@Override
	public void rawRotate3d(final Vertex center, final Vertex axis, final double radians) {
		throw new UnsupportedOperationException("NYI");
	}

	@Override
	public UndoAction translate(final double x, final double y, final double z) {
		final Vertex delta = new Vertex(x, y, z);
		final StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction setPosition(final Vertex center, final double x, final double y, final double z) {
		final Vertex delta = new Vertex(x - center.x, y - center.y, z - center.z);
		final StaticMeshMoveAction moveAction = new StaticMeshMoveAction(this, delta);
		moveAction.redo();
		return moveAction;
	}

	@Override
	public UndoAction rotate(final Vertex center, final double rotateX, final double rotateY, final double rotateZ) {
		throw new UnsupportedOperationException("Not yet implemented for animation editing");
		// final CompoundAction compoundAction = new CompoundAction("rotate",
		// ListView.Util.of(new StaticMeshRotateAction(this, center, rotateX, (byte) 0,
		// (byte) 2),
		// new StaticMeshRotateAction(this, center, rotateY, (byte) 1, (byte) 0),
		// new StaticMeshRotateAction(this, center, rotateZ, (byte) 1, (byte) 2)));
		// compoundAction.redo();
		// return compoundAction;
	}

	@Override
	public Vertex getSelectionCenter() {
		return selectionManager.getCenter();
	}

	@Override
	public boolean editorWantsAnimation() {
		return true;
	}

	@Override
	public GenericMoveAction beginTranslation() {
		final Set<IdObject> selection = selectionManager.getSelection();
		final com.etheller.collections.List<UndoAction> actions = new com.etheller.collections.ArrayList<>();
		// TODO fix cast, meta knowledge: NodeAnimationModelEditor will only be
		// constructed from
		// a TimeEnvironmentImpl render environment, and never from the anim previewer
		// impl
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		for (final IdObject node : selection) {
			AnimFlag translationTimeline = AnimFlag.find(node.getAnimFlags(), "Translation",
					timeEnvironmentImpl.getGlobalSeq());
			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018("Translation", InterpolationType.HERMITE,
						timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);
				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline,
						structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}
			final AddKeyframeAction keyframeAction = node.createTranslationKeyframe(renderModel, translationTimeline,
					structureChangeListener);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime()
				+ renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime
				: timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new TranslationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse,
				timeEnvironmentImpl.getGlobalSeq(), selection, this);
	}

	@Override
	public GenericRotateAction beginRotation(final double centerX, final double centerY, final double centerZ,
			final byte firstXYZ, final byte secondXYZ) {
		final Set<IdObject> selection = selectionManager.getSelection();
		final com.etheller.collections.List<UndoAction> actions = new com.etheller.collections.ArrayList<>();
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		for (final IdObject node : selection) {
			AnimFlag translationTimeline = AnimFlag.find(node.getAnimFlags(), "Rotation",
					timeEnvironmentImpl.getGlobalSeq());
			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018("Rotation", InterpolationType.HERMITE,
						timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);
				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline,
						structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}
			final AddKeyframeAction keyframeAction = node.createRotationKeyframe(renderModel, translationTimeline,
					structureChangeListener);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime()
				+ renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime
				: timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new RotationKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse,
				timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ, firstXYZ, secondXYZ);
	}

	@Override
	public GenericScaleAction beginScaling(final double centerX, final double centerY, final double centerZ) {
		final Set<IdObject> selection = selectionManager.getSelection();
		final com.etheller.collections.List<UndoAction> actions = new com.etheller.collections.ArrayList<>();
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		for (final IdObject node : selection) {
			AnimFlag translationTimeline = AnimFlag.find(node.getAnimFlags(), "Scaling",
					timeEnvironmentImpl.getGlobalSeq());
			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018("Scaling", InterpolationType.HERMITE,
						timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);
				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline,
						structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}
			final AddKeyframeAction keyframeAction = node.createScalingKeyframe(renderModel, translationTimeline,
					structureChangeListener);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime()
				+ renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime
				: timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new ScalingKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse,
				timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ);
	}

	@Override
	public UndoAction createKeyframe(final ModelEditorActionType actionType) {
		String keyframeMdlTypeName;
		switch (actionType) {
		case ROTATION:
			keyframeMdlTypeName = "Rotation";
			break;
		case SCALING:
			keyframeMdlTypeName = "Scaling";
			break;
		case TRANSLATION:
			keyframeMdlTypeName = "Translation";
			break;
		default:
			throw new IllegalArgumentException();
		}
		final Set<IdObject> selection = selectionManager.getSelection();
		final com.etheller.collections.List<UndoAction> actions = new com.etheller.collections.ArrayList<>();
		for (final IdObject node : selection) {
			final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
					.getAnimatedRenderEnvironment();
			AnimFlag translationTimeline = AnimFlag.find(node.getAnimFlags(), keyframeMdlTypeName,
					timeEnvironmentImpl.getGlobalSeq());
			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018(keyframeMdlTypeName, InterpolationType.HERMITE,
						timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);
				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline,
						structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}
			final AddKeyframeAction keyframeAction;
			switch (actionType) {
			case ROTATION:
				keyframeAction = node.createRotationKeyframe(renderModel, translationTimeline, structureChangeListener);
				break;
			case SCALING:
				keyframeAction = node.createScalingKeyframe(renderModel, translationTimeline, structureChangeListener);
				break;
			case TRANSLATION:
				keyframeAction = node.createTranslationKeyframe(renderModel, translationTimeline,
						structureChangeListener);
				break;
			default:
				throw new IllegalArgumentException();
			}
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		return new CompoundAction("create keyframe", actions);
	}

	@Override
	public UndoAction createFaceFromSelection(final Vertex preferredFacingVector) {
		throw new WrongModeException("Unable to create face in animation editor");
	}

	@Override
	public GenericMoveAction addBox(final double x, final double y, final double x2, final double y2, final byte dim1,
			final byte dim2, final Vertex facingVector, final int numberOfLengthSegments,
			final int numberOfWidthSegments, final int numberOfHeightSegments) {
		throw new WrongModeException("Unable to create box in animation editor");
	}

	@Override
	public UndoAction splitGeoset() {
		throw new WrongModeException("Unable to split geoset in animation editor");
	}

	@Override
	public GenericRotateAction beginSquatTool(final double centerX, final double centerY, final double centerZ,
			final byte firstXYZ, final byte secondXYZ) {
		final Set<IdObject> selection = new HashSet<>(selectionManager.getSelection());
		for (final IdObject idObject : model.getModel().getIdObjects()) {
			if (selectionManager.getSelection().contains(idObject.getParent()) && (idObject.getClass() == Bone.class
					&& idObject.getParent().getClass() == Bone.class
					|| idObject.getClass() == Helper.class && idObject.getParent().getClass() == Helper.class)) {
				selection.add(idObject);
			}
		}
		final com.etheller.collections.List<UndoAction> actions = new com.etheller.collections.ArrayList<>();
		final TimeEnvironmentImpl timeEnvironmentImpl = (TimeEnvironmentImpl) renderModel
				.getAnimatedRenderEnvironment();
		for (final IdObject node : selection) {
			AnimFlag translationTimeline = AnimFlag.find(node.getAnimFlags(), "Rotation",
					timeEnvironmentImpl.getGlobalSeq());
			if (translationTimeline == null) {
				translationTimeline = AnimFlag.createEmpty2018("Rotation", InterpolationType.HERMITE,
						timeEnvironmentImpl.getGlobalSeq());
				node.add(translationTimeline);
				final AddTimelineAction addTimelineAction = new AddTimelineAction(node, translationTimeline,
						structureChangeListener);
				structureChangeListener.timelineAdded(node, translationTimeline);
				actions.add(addTimelineAction);
			}
			final AddKeyframeAction keyframeAction = node.createRotationKeyframe(renderModel, translationTimeline,
					structureChangeListener);
			if (keyframeAction != null) {
				actions.add(keyframeAction);
			}
		}

		final int trackTime = renderModel.getAnimatedRenderEnvironment().getAnimationTime()
				+ renderModel.getAnimatedRenderEnvironment().getCurrentAnimation().getStart();
		final int trackTimeToUse = timeEnvironmentImpl.getGlobalSeq() == null ? trackTime
				: timeEnvironmentImpl.getGlobalSeqTime(timeEnvironmentImpl.getGlobalSeq());
		return new SquatToolKeyframeAction(new CompoundAction("setup", actions), trackTimeToUse,
				timeEnvironmentImpl.getGlobalSeq(), selection, this, centerX, centerY, centerZ, firstXYZ, secondXYZ);
	}

	@Override
	public UndoAction setParent(final IdObject node) {
		throw new WrongModeException("Can't set parent in Animation Editor");
	}

	@Override
	public RigAction rig() {
		throw new WrongModeException("Unable to rig in Animation Editor");
	}

	@Override
	public UndoAction addBone(final double x, final double y, final double z) {
		throw new WrongModeException("Unable to add bone in Animation Editor");
	}

}
