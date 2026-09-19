package com.hiveworkshop.wc3.gui.modeledit.componenttree;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import com.hiveworkshop.wc3.gui.modeledit.UndoAction;
import com.hiveworkshop.wc3.gui.modeledit.activity.UndoActionListener;
import com.hiveworkshop.wc3.gui.modeledit.actions.newsys.ModelStructureChangeListener;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.ModelComponentCopier.CopyResult;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.actions.AddComponentsAction;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.actions.RemoveBitmapAction;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.actions.RemoveComponentAction;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.actions.RemoveGlobalSequenceAction;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.actions.RemoveNodesAction;
import com.hiveworkshop.wc3.gui.modeledit.componenttree.actions.RemoveTextureAnimAction;
import com.hiveworkshop.wc3.gui.modeledit.newstuff.actions.tools.SetParentAction;
import com.hiveworkshop.wc3.mdl.AnimFlag;
import com.hiveworkshop.wc3.mdl.Animation;
import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bitmap;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EditableModel;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.Geoset;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.IdObject;
import com.hiveworkshop.wc3.mdl.Layer;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.Material;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.ParticleEmitterPopcorn;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.TextureAnim;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdl.v2.ModelViewManager;

/**
 * The operations behind the Model tab's popup menu, hotkeys and drag and drop:
 * new, cut, copy, paste, delete, reparent, move left and move right. Every
 * mutation is an {@link UndoAction} pushed on the undo stack.
 */
public final class ModelComponentTreeController {
	public interface SelectionCallback {
		void componentCreated(Object component);
	}

	private static final String WHITE_TEXTURE = "Textures\\white.blp";

	private final ModelViewManager modelViewManager;
	private final UndoActionListener undoListener;
	private final ModelStructureChangeListener structureListener;
	private final Component dialogParent;
	private SelectionCallback selectionCallback;

	public ModelComponentTreeController(final ModelViewManager modelViewManager,
			final UndoActionListener undoListener, final ModelStructureChangeListener structureListener,
			final Component dialogParent) {
		this.modelViewManager = modelViewManager;
		this.undoListener = undoListener;
		this.structureListener = structureListener;
		this.dialogParent = dialogParent;
	}

	public void setSelectionCallback(final SelectionCallback selectionCallback) {
		this.selectionCallback = selectionCallback;
	}

	private EditableModel model() {
		return modelViewManager.getModel();
	}

	private void push(final UndoAction action) {
		action.redo();
		undoListener.pushAction(action);
	}

	private void created(final Object component) {
		if (selectionCallback != null) {
			selectionCallback.componentCreated(component);
		}
	}

	// ---- clipboard ----

	public boolean canCopy(final ComponentRef ref) {
		return ref.isComponent() && ModelComponentCopier.canCopy(ref.getItem());
	}

	public void copy(final ComponentRef ref) {
		if (canCopy(ref)) {
			ModelComponentClipboard.set(ref.getItem(), model());
		}
	}

	public void cut(final ComponentRef ref) {
		if (canCopy(ref)) {
			copy(ref);
			delete(ref, false);
		}
	}

	public boolean canPaste() {
		return !ModelComponentClipboard.isEmpty();
	}

	/**
	 * @param target the row the paste was invoked on; a node row becomes the
	 *               parent of a pasted node
	 */
	public void paste(final ComponentRef target) {
		if (!canPaste()) {
			return;
		}
		final Object original = ModelComponentClipboard.getItem();
		final EditableModel source = ModelComponentClipboard.getSourceModel();
		final CopyResult copy = ModelComponentCopier.copy(original, source, model());
		if (copy == null) {
			JOptionPane.showMessageDialog(dialogParent, "This component cannot be pasted.");
			return;
		}
		if ((copy.getItem() instanceof IdObject) && (target != null) && target.isNode()) {
			((IdObject) copy.getItem()).setParent(target.asNode());
		}
		final List<Object> components = new ArrayList<>(copy.getExtras());
		components.add(copy.getItem());
		final AddComponentsAction action = new AddComponentsAction(model(), components, structureListener,
				"paste " + ComponentKind.of(copy.getItem()).getDisplayName().toLowerCase());
		if (copy.getKeyframeSource() != null) {
			action.withKeyframesFrom(copy.getKeyframeSource(), (Animation) copy.getItem());
		}
		push(action);
		created(copy.getItem());
	}

	// ---- delete ----

	public boolean canDelete(final ComponentRef ref) {
		return ref.isComponent();
	}

	public void delete(final ComponentRef ref, final boolean deleteSubtree) {
		if (!canDelete(ref)) {
			return;
		}
		final Object item = ref.getItem();
		final ComponentKind kind = ref.getKind();
		UndoAction action = null;
		switch (kind) {
		case TEXTURE: {
			final Bitmap bitmap = (Bitmap) item;
			final int references = RemoveBitmapAction.countReferences(model(), bitmap);
			Bitmap replacement = null;
			if (references > 0) {
				replacement = chooseReplacementTexture(bitmap, references);
				if (replacement == null) {
					return;
				}
			}
			action = new RemoveBitmapAction(model(), bitmap, replacement, structureListener);
			break;
		}
		case MATERIAL: {
			final int users = countMaterialUsers((Material) item);
			if (users > 0) {
				JOptionPane.showMessageDialog(dialogParent,
						"This material is used by " + users + " geoset(s) or ribbon emitter(s).\n"
								+ "Assign them another material first, or delete them.",
						"Material in use", JOptionPane.WARNING_MESSAGE);
				return;
			}
			action = new RemoveComponentAction(model(), item, structureListener);
			break;
		}
		case TEXTURE_ANIM: {
			final int references = RemoveTextureAnimAction.countReferences(model(), (TextureAnim) item);
			if (references > 0) {
				final int choice = JOptionPane.showConfirmDialog(dialogParent,
						"This texture animation is used by " + references
								+ " layer(s). Those layers will lose their texture animation.\nDelete anyway?",
						"Texture anim in use", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (choice != JOptionPane.OK_OPTION) {
					return;
				}
			}
			action = new RemoveTextureAnimAction(model(), (TextureAnim) item, structureListener);
			break;
		}
		case GLOBAL_SEQUENCE:
			action = new RemoveGlobalSequenceAction(model(), (Integer) item, structureListener);
			break;
		case SEQUENCE:
		case GEOSET:
		case GEOSET_ANIM:
		case CAMERA:
			action = new RemoveComponentAction(model(), item, structureListener);
			break;
		default: {
			final List<IdObject> nodes = new ArrayList<>();
			nodes.add((IdObject) item);
			action = new RemoveNodesAction(model(), nodes, deleteSubtree, structureListener);
			break;
		}
		}
		push(action);
		ModelComponentClipboard.forgetIfSame(item);
		if (item instanceof IdObject) {
			// a cut node stays pasteable: keep it on the clipboard
			ModelComponentClipboard.forgetIfSame(null);
		}
	}

	private int countMaterialUsers(final Material material) {
		int count = 0;
		for (final Geoset geoset : model().getGeosets()) {
			if (geoset.getMaterial() == material) {
				count++;
			}
		}
		for (final IdObject node : model().getIdObjects()) {
			if ((node instanceof RibbonEmitter) && (((RibbonEmitter) node).getMaterial() == material)) {
				count++;
			}
		}
		return count;
	}

	private Bitmap chooseReplacementTexture(final Bitmap bitmap, final int references) {
		final List<Bitmap> candidates = new ArrayList<>();
		for (final Bitmap other : model().getTextures()) {
			if (other != bitmap) {
				candidates.add(other);
			}
		}
		final Bitmap white = new Bitmap(WHITE_TEXTURE);
		final List<Object> options = new ArrayList<>();
		options.add(white);
		options.addAll(candidates);
		final JComboBox<Object> combo = new JComboBox<>(options.toArray());
		combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final javax.swing.JList<?> list, final Object value,
					final int index, final boolean isSelected, final boolean cellHasFocus) {
				final Object label = value == white ? "New texture: " + WHITE_TEXTURE
						: ((Bitmap) value).getName();
				return super.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
			}
		});
		final Object[] message = { "\"" + bitmap.getName() + "\" is used in " + references + " place(s).",
				"Replace those references with:", combo };
		final int choice = JOptionPane.showConfirmDialog(dialogParent, message, "Texture in use",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return null;
		}
		return (Bitmap) combo.getSelectedItem();
	}

	// ---- new ----

	public void createNew(final ComponentKind kind, final ComponentRef context) {
		final EditableModel model = model();
		final List<Object> components = new ArrayList<>();
		Object created;
		switch (kind) {
		case SEQUENCE: {
			final int start = ModelComponentCopier.nextFreeSequenceStart(model);
			created = new Animation("New Sequence", start, start + 1000);
			break;
		}
		case GLOBAL_SEQUENCE:
			created = ModelComponentCopier.newGlobalSequence(1000);
			break;
		case TEXTURE:
			created = new Bitmap(WHITE_TEXTURE);
			break;
		case MATERIAL:
			created = new Material(new Layer("None", anyTexture(model, components)));
			break;
		case TEXTURE_ANIM:
			created = new TextureAnim(new ArrayList<AnimFlag>());
			break;
		case GEOSET: {
			final Geoset geoset = new Geoset();
			geoset.setMaterial(anyMaterial(model, components));
			geoset.setParentModel(model);
			created = geoset;
			break;
		}
		case GEOSET_ANIM: {
			final Geoset geoset = chooseGeosetWithoutAnim(context);
			if (geoset == null) {
				return;
			}
			final GeosetAnim geosetAnim = new GeosetAnim(geoset);
			created = geosetAnim;
			break;
		}
		case CAMERA:
			created = new Camera("New Camera", new Vertex(0, -400, 200), new Vertex(0, 0, 80), 0.7853982, 10000, 8);
			break;
		default:
			created = createNode(kind, context, model, components);
			break;
		}
		components.add(created);
		push(new AddComponentsAction(model, components, structureListener,
				"new " + kind.getDisplayName().toLowerCase()));
		created(created);
	}

	private IdObject createNode(final ComponentKind kind, final ComponentRef context, final EditableModel model,
			final List<Object> extras) {
		final String name = "New " + kind.getDisplayName();
		IdObject node;
		switch (kind) {
		case BONE:
			node = new Bone(name);
			break;
		case HELPER:
			node = new Helper(name);
			break;
		case LIGHT:
			node = new Light(name);
			break;
		case ATTACHMENT:
			node = new Attachment(name);
			break;
		case PARTICLE_EMITTER: {
			final ParticleEmitter emitter = new ParticleEmitter(name);
			emitter.setPath("");
			node = emitter;
			break;
		}
		case PARTICLE_EMITTER2: {
			final ParticleEmitter2 emitter = new ParticleEmitter2(name);
			emitter.setTexture(anyTexture(model, extras));
			node = emitter;
			break;
		}
		case POPCORN: {
			final ParticleEmitterPopcorn emitter = new ParticleEmitterPopcorn(name);
			emitter.setPath("");
			node = emitter;
			break;
		}
		case RIBBON: {
			final RibbonEmitter emitter = new RibbonEmitter(name);
			emitter.setMaterial(anyMaterial(model, extras));
			node = emitter;
			break;
		}
		case EVENT_OBJECT:
			node = new EventObject(name);
			break;
		case COLLISION_SHAPE:
			node = new CollisionShape(name);
			break;
		default:
			throw new IllegalArgumentException(kind.toString());
		}
		final IdObject parent = (context != null) ? context.asNode() : null;
		if (parent != null) {
			node.setParent(parent);
			node.setPivotPoint(new Vertex(parent.getPivotPoint()));
		} else if (node.getPivotPoint() == null) {
			node.setPivotPoint(new Vertex(0, 0, 0));
		}
		return node;
	}

	/** The model's first texture, or a new white one registered in extras. */
	private static Bitmap anyTexture(final EditableModel model, final List<Object> extras) {
		if (!model.getTextures().isEmpty()) {
			return model.getTextures().get(0);
		}
		for (final Object extra : extras) {
			if (extra instanceof Bitmap) {
				return (Bitmap) extra;
			}
		}
		final Bitmap white = new Bitmap(WHITE_TEXTURE);
		extras.add(white);
		return white;
	}

	/** The model's first material, or a new one registered in extras. */
	private static Material anyMaterial(final EditableModel model, final List<Object> extras) {
		if (!model.getMaterials().isEmpty()) {
			return model.getMaterials().get(0);
		}
		for (final Object extra : extras) {
			if (extra instanceof Material) {
				return (Material) extra;
			}
		}
		final Material material = new Material(new Layer("None", anyTexture(model, extras)));
		extras.add(material);
		return material;
	}

	private Geoset chooseGeosetWithoutAnim(final ComponentRef context) {
		if ((context != null) && (context.getItem() instanceof Geoset)
				&& (((Geoset) context.getItem()).getGeosetAnim() == null)) {
			return (Geoset) context.getItem();
		}
		final List<Geoset> candidates = new ArrayList<>();
		for (final Geoset geoset : model().getGeosets()) {
			if (geoset.getGeosetAnim() == null) {
				candidates.add(geoset);
			}
		}
		if (candidates.isEmpty()) {
			JOptionPane.showMessageDialog(dialogParent, "Every geoset already has a geoset animation.");
			return null;
		}
		final JComboBox<Object> combo = new JComboBox<>(candidates.toArray());
		combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(final javax.swing.JList<?> list, final Object value,
					final int index, final boolean isSelected, final boolean cellHasFocus) {
				return super.getListCellRendererComponent(list, ((Geoset) value).getUIName(model()), index,
						isSelected, cellHasFocus);
			}
		});
		final int choice = JOptionPane.showConfirmDialog(dialogParent,
				new Object[] { "Geoset animation for which geoset?", combo }, "New Geoset Anim",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return null;
		}
		return (Geoset) combo.getSelectedItem();
	}

	// ---- node structure ----

	public boolean canReparent(final IdObject node, final IdObject newParent) {
		if ((node == null) || (node == newParent) || (node.getParent() == newParent)) {
			return false;
		}
		for (IdObject ancestor = newParent; ancestor != null; ancestor = ancestor.getParent()) {
			if (ancestor == node) {
				return false;
			}
		}
		return true;
	}

	public void reparent(final IdObject node, final IdObject newParent) {
		if (!canReparent(node, newParent)) {
			return;
		}
		final com.etheller.collections.HashMap<IdObject, IdObject> nodeToOldParent = new com.etheller.collections.HashMap<>();
		nodeToOldParent.put(node, node.getParent());
		push(new SetParentAction(nodeToOldParent, newParent, structureListener));
	}

	/** Becomes a child of its grandparent (a sibling of its former parent). */
	public boolean canMoveLeft(final IdObject node) {
		return (node != null) && (node.getParent() != null);
	}

	public void moveLeft(final IdObject node) {
		if (canMoveLeft(node)) {
			reparent(node, node.getParent().getParent());
		}
	}

	/** Becomes a child of the sibling listed just above it. */
	public IdObject siblingAbove(final IdObject node) {
		if (node == null) {
			return null;
		}
		IdObject previous = null;
		for (final IdObject candidate : model().getIdObjects()) {
			if (candidate == node) {
				return previous;
			}
			if ((candidate.getParent() == node.getParent()) && (candidate != node)) {
				previous = candidate;
			}
		}
		return null;
	}

	public boolean canMoveRight(final IdObject node) {
		return siblingAbove(node) != null;
	}

	public void moveRight(final IdObject node) {
		final IdObject sibling = siblingAbove(node);
		if (sibling != null) {
			reparent(node, sibling);
		}
	}
}
