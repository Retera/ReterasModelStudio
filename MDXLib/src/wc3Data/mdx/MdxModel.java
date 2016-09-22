package wc3Data.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import wc3Data.mdl.AnimFlag;
import wc3Data.mdl.Animation;
import wc3Data.mdl.Bitmap;
import wc3Data.mdl.Bone;
import wc3Data.mdl.ExtLog;
import wc3Data.mdl.Geoset;
import wc3Data.mdl.GeosetVertex;
import wc3Data.mdl.Layer;
import wc3Data.mdl.MDL;
import wc3Data.mdl.Material;
import wc3Data.mdl.Matrix;
import wc3Data.mdl.Normal;
import wc3Data.mdl.QuaternionRotation;
import wc3Data.mdl.TVertex;
import wc3Data.mdl.TextureAnim;
import wc3Data.mdl.Triangle;
import wc3Data.mdl.UVLayer;
import wc3Data.mdl.Vertex;
import wc3Data.mdx.SequenceChunk.Sequence;
import wc3Data.mdx.TextureAnimationChunk.TextureAnimation;
import wc3Data.mdx.TextureChunk.Texture;

public class MdxModel {
	public VersionChunk versionChunk;
	public ModelChunk modelChunk;
	public SequenceChunk sequenceChunk;
	public GlobalSequenceChunk globalSequenceChunk;
	public MaterialChunk materialChunk;
	public TextureChunk textureChunk;
	public TextureAnimationChunk textureAnimationChunk;
	public GeosetChunk geosetChunk;
	public GeosetAnimationChunk geosetAnimationChunk;
	public BoneChunk boneChunk;
	public LightChunk lightChunk;
	public HelperChunk helperChunk;
	public AttachmentChunk attachmentChunk;
	public PivotPointChunk pivotPointChunk;
	public ParticleEmitterChunk particleEmitterChunk;
	public ParticleEmitter2Chunk particleEmitter2Chunk;
	public RibbonEmitterChunk ribbonEmitterChunk;
	public EventObjectChunk eventObjectChunk;
	public CameraChunk cameraChunk;
	public CollisionShapeChunk collisionShapeChunk;

	public static final String key = "MDLX";
	
	// Conversion helpers:
	private static Vertex vertex3f(float[] array) {
		if( array == null || array.length < 3 ) {
			return null;
		}
		return new Vertex(array[0], array[1], array[2]);
	}
	private static QuaternionRotation vertex4f(float[] array) {
		if( array == null || array.length < 3 ) {
			return null;
		}
		return new QuaternionRotation(array[0], array[1], array[2], array[3]);
	}
	private static ExtLog extents(float[] minExt, float[] maxExt, float boundsRadius) {
		return new ExtLog(
				vertex3f(minExt),
				vertex3f(maxExt),
				boundsRadius);
	}
	public static boolean hasFlag(int flags, int mask) {
		return (flags & mask) != 0;
	}
	// end conversion helpers
	public MDL toMDL() {
		MDL mdlObject = new MDL();
		
		// Step 1: Convert the Model Chunk
		// For MDL api, this is currently embedded right inside the
		// MDL class
		mdlObject.setName(modelChunk.name);
		mdlObject.addToHeader("//This model was converted from MDX by ogre-lord's Java MDX API and Retera's Java MDL API");
		mdlObject.setBlendTime(modelChunk.blendTime);
		mdlObject.setExtents(extents(
				modelChunk.minimumExtent,
				modelChunk.maximumExtent,
				modelChunk.boundsRadius));
		mdlObject.setFormatVersion(versionChunk.version);
		
		// Step 2: Convert the Sequences
		if( sequenceChunk != null )
			for( Sequence seq: sequenceChunk.sequence) {
				Animation anim = new Animation(seq.name, seq.intervalStart, seq.intervalEnd);
				anim.setExtents(extents(seq.minimumExtent, seq.maximumExtent, seq.boundsRadius));
				if( seq.moveSpeed != 0 ) {
					anim.addTag("MoveSpeed " + seq.moveSpeed);
				}
				if( seq.nonLooping == 1 ) {
					anim.addTag("NonLooping");
				}
				if( seq.rarity > 0 ) {
					anim.addTag("Rarity " + seq.rarity);
				}
				mdlObject.add(anim);
			}
		
		// Step 3: Convert any global sequences
		if( globalSequenceChunk != null )
			for( int i = 0; i < globalSequenceChunk.globalSequences.length; i++ ) {
				mdlObject.add(new Integer(globalSequenceChunk.globalSequences[i]));
			}
		
		// Step 4: Convert Texture refs
		if( textureChunk != null )
			for( Texture tex: textureChunk.texture ) {
				Bitmap bmp = new Bitmap(tex.fileName, tex.replaceableId);
				bmp.setWrapStyle(tex.flags);
				mdlObject.add(bmp);
			}
		
		// Step 6: Convert TVertexAnims
		if( textureAnimationChunk != null )
			for( TextureAnimation txa: textureAnimationChunk.textureAnimation ) {
				ArrayList<AnimFlag> flags = new ArrayList<AnimFlag>();
				if( txa.textureRotation != null ) {
					AnimFlag flag = new AnimFlag(txa.textureRotation);
					flags.add(flag);
				}
				if( txa.textureScaling != null ) {
					AnimFlag flag = new AnimFlag(txa.textureScaling);
					flags.add(flag);
				}
				if( txa.textureTranslation != null ) {
					AnimFlag flag = new AnimFlag(txa.textureTranslation);
					flags.add(flag);
				}
				TextureAnim anim = new TextureAnim(flags);
				mdlObject.add(anim);
			}
		
		// Step 5: Convert Material refs
		if( materialChunk != null )
			for( MaterialChunk.Material mat: materialChunk.material ) {
				List<Layer> layers = new ArrayList<Layer>();
				for( LayerChunk.Layer lay: mat.layerChunk.layer ) {
					Layer layer = new Layer(Layer.FilterMode.fromId(lay.filterMode).getMdlText(), lay.textureId);
					layer.setStaticAlpha(lay.alpha);
					int shadingFlags = lay.shadingFlags;
					// 0x1: unshaded
	                // 0x2: sphere environment map
	                // 0x4: ?
	                // 0x8: ?
	                // 0x10: two sided
	                // 0x20: unfogged
	                // 0x30: no depth test
	                // 0x40: no depth set
					if( hasFlag(shadingFlags, 0x1) ) {
						layer.addFlag("Unshaded");
					}
					if( hasFlag(shadingFlags, 0x2) ) {
						layer.addFlag("SphereEnvironmentMap");
					}
					if( hasFlag(shadingFlags, 0x10) ) {
						layer.addFlag("TwoSided");
					}
					if( hasFlag(shadingFlags, 0x20) ) {
						layer.addFlag("Unfogged");
					}
					if( hasFlag(shadingFlags, 0x40) ) {
						layer.addFlag("NoDepthTest");
					}
					if( hasFlag(shadingFlags, 0x80) ) {
						layer.addFlag("NoDepthSet");
					}
					layer.setTVertexAnimId(lay.textureAnimationId);
					layer.setCoordId(lay.unknownNull_CoordID); // this isn't an unknown field! it's coordID! don't be like Magos and forget!
					//(breaks the volcano model, this is why War3ModelEditor can't open volcano!)
					if( lay.materialAlpha != null ) {
						AnimFlag flag = new AnimFlag(lay.materialAlpha);
						layer.getAnims().add(flag);
					}
					if( lay.materialTextureId != null ) {
						AnimFlag flag = new AnimFlag(lay.materialTextureId);
						layer.getAnims().add(flag);
					}
					layer.updateRefs(mdlObject);
					layers.add(layer);
				}
				Material material = new Material(layers);
				material.setPriorityPlane(mat.priorityPlane);
				if( hasFlag(mat.flags, 0x1) ) {
					material.addFlag("ConstantColor");
				}
				if( hasFlag(mat.flags, 0x10) ) {
					material.addFlag("SortPrimsFarZ");
				}
				if( hasFlag(mat.flags, 0x20) ) {
					material.addFlag("FullResolution");
				}
				mdlObject.add(material);
			}
		
		// Step 7: Geoset
		if( geosetChunk != null )
			for( GeosetChunk.Geoset mdxGeo : geosetChunk.geoset ) {
				Geoset mdlGeo = new Geoset();
				mdlGeo.setExtLog(new ExtLog(vertex3f(mdxGeo.minimumExtent), vertex3f(mdxGeo.maximumExtent), mdxGeo.boundsRadius));
				for( GeosetChunk.Geoset.Extent ext: mdxGeo.extent ) {
					ExtLog extents = new ExtLog(ext);
					Animation anim = new Animation(extents);
					mdlGeo.add(anim);
				}
				
				mdlGeo.setMaterialID(mdxGeo.materialId);
				ArrayList<UVLayer> uv = new ArrayList<UVLayer>();
				for( int i = 0; i < mdxGeo.nrOfTextureVertexGroups; i++ ) {
					UVLayer layer = new UVLayer();
					uv.add(layer);
					mdlGeo.addUVLayer(layer);
				}
				
				int nVertices = mdxGeo.vertexPositions.length / 3;
				for( int k = 0; k < nVertices; k++ ) {
					int i = k * 3;
					int j = k * 2;
					GeosetVertex gv;
					mdlGeo.add(gv = new GeosetVertex(
							mdxGeo.vertexPositions[i],
							mdxGeo.vertexPositions[i+1],
							mdxGeo.vertexPositions[i+2]));
					gv.setVertexGroup(mdxGeo.vertexGroups[k]);
					mdlGeo.addNormal(new Normal(
							mdxGeo.vertexNormals[i],
							mdxGeo.vertexNormals[i+1],
							mdxGeo.vertexNormals[i+2]));
					
					for( int uvId = 0; uvId < uv.size(); uvId++ ) {
						uv.get(uvId).addTVertex(new TVertex(
								mdxGeo.vertexTexturePositions[uvId * nVertices + j],
								mdxGeo.vertexTexturePositions[uvId * nVertices + j + 1]));
					}
				}
				// guys I didn't code this to allow experimental
				// non-triangle faces that were suggested to exist
				// on the web (i.e. quads).
				// if you wanted to fix that, you'd want to do it below
				for( int i = 0; i < mdxGeo.faces.length; i+=3 ) {
					Triangle triangle = new Triangle(mdxGeo.faces[i+0], mdxGeo.faces[i+1], mdxGeo.faces[i+2], mdlGeo);
					mdlGeo.add(triangle);
				}
				if( mdxGeo.selectionType == 4 ) {
					mdlGeo.addFlag("Unselectable");
				}
				mdlGeo.setSelectionGroup(mdxGeo.selectionGroup);
				int index = 0;
				for( int size: mdxGeo.matrixGroups ) {
					Matrix m = new Matrix();
					for( int i = 0; i < size; i++ ) {
						m.addId(0);//mdxGeo.matrixIndexs[index++]);
					}
					mdlGeo.addMatrix(m);
				}
				mdlObject.add(mdlGeo);
			}
		
		mdlObject.add(new Bone("sucky test"));
		
		mdlObject.doPostRead();
		return mdlObject;
	}

	public void load(BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "MDLX");
		for (int i = 0; i < 20; i++) {
			if (MdxUtils.checkOptionalId(in, versionChunk.key)) {
				versionChunk = new VersionChunk();
				versionChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, modelChunk.key)) {
				modelChunk = new ModelChunk();
				modelChunk.load(in); 
			} else if (MdxUtils.checkOptionalId(in, sequenceChunk.key)) {
				sequenceChunk = new SequenceChunk();
				sequenceChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, globalSequenceChunk.key)) {
				globalSequenceChunk = new GlobalSequenceChunk();
				globalSequenceChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, materialChunk.key)) {
				materialChunk = new MaterialChunk();
				materialChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, textureChunk.key)) {
				textureChunk = new TextureChunk();
				textureChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, textureAnimationChunk.key)) {
				textureAnimationChunk = new TextureAnimationChunk();
				textureAnimationChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, geosetChunk.key)) {
				geosetChunk = new GeosetChunk();
				geosetChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, geosetAnimationChunk.key)) {
				geosetAnimationChunk = new GeosetAnimationChunk();
				geosetAnimationChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, boneChunk.key)) {
				boneChunk = new BoneChunk();
				boneChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, lightChunk.key)) {
				lightChunk = new LightChunk();
				lightChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, helperChunk.key)) {
				helperChunk = new HelperChunk();
				helperChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, attachmentChunk.key)) {
				attachmentChunk = new AttachmentChunk();
				attachmentChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, pivotPointChunk.key)) {
				pivotPointChunk = new PivotPointChunk();
				pivotPointChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, particleEmitterChunk.key)) {
				particleEmitterChunk = new ParticleEmitterChunk();
				particleEmitterChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, particleEmitter2Chunk.key)) {
				particleEmitter2Chunk = new ParticleEmitter2Chunk();
				particleEmitter2Chunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, ribbonEmitterChunk.key)) {
				ribbonEmitterChunk = new RibbonEmitterChunk();
				ribbonEmitterChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, eventObjectChunk.key)) {
				eventObjectChunk = new EventObjectChunk();
				eventObjectChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, cameraChunk.key)) {
				cameraChunk = new CameraChunk();
				cameraChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, collisionShapeChunk.key)) {
				collisionShapeChunk = new CollisionShapeChunk();
				collisionShapeChunk.load(in);
			}

		}
	}

	public void save(BlizzardDataOutputStream out) throws IOException {
		out.writeNByteString("MDLX", 4);
		if (versionChunk != null) {
			versionChunk.save(out);
		}
		if (modelChunk != null) {
			modelChunk.save(out);
		}
		if (sequenceChunk != null) {
			sequenceChunk.save(out);
		}
		if (globalSequenceChunk != null) {
			globalSequenceChunk.save(out);
		}
		if (materialChunk != null) {
			materialChunk.save(out);
		}
		if (textureChunk != null) {
			textureChunk.save(out);
		}
		if (textureAnimationChunk != null) {
			textureAnimationChunk.save(out);
		}
		if (geosetChunk != null) {
			geosetChunk.save(out);
		}
		if (geosetAnimationChunk != null) {
			geosetAnimationChunk.save(out);
		}
		if (boneChunk != null) {
			boneChunk.save(out);
		}
		if (lightChunk != null) {
			lightChunk.save(out);
		}
		if (helperChunk != null) {
			helperChunk.save(out);
		}
		if (attachmentChunk != null) {
			attachmentChunk.save(out);
		}
		if (pivotPointChunk != null) {
			pivotPointChunk.save(out);
		}
		if (particleEmitterChunk != null) {
			particleEmitterChunk.save(out);
		}
		if (particleEmitter2Chunk != null) {
			particleEmitter2Chunk.save(out);
		}
		if (ribbonEmitterChunk != null) {
			ribbonEmitterChunk.save(out);
		}
		if (eventObjectChunk != null) {
			eventObjectChunk.save(out);
		}
		if (cameraChunk != null) {
			cameraChunk.save(out);
		}
		if (collisionShapeChunk != null) {
			collisionShapeChunk.save(out);
		}

	}

	public int getSize() {
		int a = 0;
		a += 4;
		if (versionChunk != null) {
			a += versionChunk.getSize();
		}
		if (modelChunk != null) {
			a += modelChunk.getSize();
		}
		if (sequenceChunk != null) {
			a += sequenceChunk.getSize();
		}
		if (globalSequenceChunk != null) {
			a += globalSequenceChunk.getSize();
		}
		if (materialChunk != null) {
			a += materialChunk.getSize();
		}
		if (textureChunk != null) {
			a += textureChunk.getSize();
		}
		if (textureAnimationChunk != null) {
			a += textureAnimationChunk.getSize();
		}
		if (geosetChunk != null) {
			a += geosetChunk.getSize();
		}
		if (geosetAnimationChunk != null) {
			a += geosetAnimationChunk.getSize();
		}
		if (boneChunk != null) {
			a += boneChunk.getSize();
		}
		if (lightChunk != null) {
			a += lightChunk.getSize();
		}
		if (helperChunk != null) {
			a += helperChunk.getSize();
		}
		if (attachmentChunk != null) {
			a += attachmentChunk.getSize();
		}
		if (pivotPointChunk != null) {
			a += pivotPointChunk.getSize();
		}
		if (particleEmitterChunk != null) {
			a += particleEmitterChunk.getSize();
		}
		if (particleEmitter2Chunk != null) {
			a += particleEmitter2Chunk.getSize();
		}
		if (ribbonEmitterChunk != null) {
			a += ribbonEmitterChunk.getSize();
		}
		if (eventObjectChunk != null) {
			a += eventObjectChunk.getSize();
		}
		if (cameraChunk != null) {
			a += cameraChunk.getSize();
		}
		if (collisionShapeChunk != null) {
			a += collisionShapeChunk.getSize();
		}

		return a;
	}
}
