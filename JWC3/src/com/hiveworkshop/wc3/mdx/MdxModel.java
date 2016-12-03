package com.hiveworkshop.wc3.mdx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.hiveworkshop.wc3.mdl.Attachment;
import com.hiveworkshop.wc3.mdl.Bone;
import com.hiveworkshop.wc3.mdl.Camera;
import com.hiveworkshop.wc3.mdl.CollisionShape;
import com.hiveworkshop.wc3.mdl.EventObject;
import com.hiveworkshop.wc3.mdl.ExtLog;
import com.hiveworkshop.wc3.mdl.GeosetAnim;
import com.hiveworkshop.wc3.mdl.Helper;
import com.hiveworkshop.wc3.mdl.Light;
import com.hiveworkshop.wc3.mdl.MDL;
import com.hiveworkshop.wc3.mdl.ParticleEmitter;
import com.hiveworkshop.wc3.mdl.ParticleEmitter2;
import com.hiveworkshop.wc3.mdl.QuaternionRotation;
import com.hiveworkshop.wc3.mdl.RibbonEmitter;
import com.hiveworkshop.wc3.mdl.Vertex;
import com.hiveworkshop.wc3.mdx.SequenceChunk.Sequence;

import de.wc3data.stream.BlizzardDataInputStream;
import de.wc3data.stream.BlizzardDataOutputStream;

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
	private static Vertex vertex3f(final float[] array) {
		if( array == null || array.length < 3 ) {
			return null;
		}
		return new Vertex(array[0], array[1], array[2]);
	}
	private static Vertex vertex3f(final byte[] array) {
		if( array == null || array.length < 3 ) {
			return null;
		}
		return new Vertex((256+array[0])%256, (256+array[1])%256, (256+array[2])%256);
	}
	private static QuaternionRotation vertex4f(final float[] array) {
		if( array == null || array.length < 3 ) {
			return null;
		}
		return new QuaternionRotation(array[0], array[1], array[2], array[3]);
	}
	private static ExtLog extents(final float[] minExt, final float[] maxExt, final float boundsRadius) {
		return new ExtLog(
				vertex3f(minExt),
				vertex3f(maxExt),
				boundsRadius);
	}
	public static boolean hasFlag(final int flags, final int mask) {
		return (flags & mask) != 0;
	}
	// end conversion helpers
	public MDL toMDL() {
//		MDL mdlObject = new MDL();
//
//		// Step 1: Convert the Model Chunk
//		// For MDL api, this is currently embedded right inside the
//		// MDL class
//		mdlObject.setName(modelChunk.name);
//		mdlObject.addToHeader("//This model was converted from MDX by ogre-lord's Java MDX API and Retera's Java MDL API");
//		mdlObject.setBlendTime(modelChunk.blendTime);
//		mdlObject.setExtents(extents(
//				modelChunk.minimumExtent,
//				modelChunk.maximumExtent,
//				modelChunk.boundsRadius));
//		mdlObject.setFormatVersion(versionChunk.version);
//
//		// Step 2: Convert the Sequences
//		if( sequenceChunk != null )
//			for( Sequence seq: sequenceChunk.sequence) {
//				Animation anim = new Animation(seq.name, seq.intervalStart, seq.intervalEnd);
//				anim.setExtents(extents(seq.minimumExtent, seq.maximumExtent, seq.boundsRadius));
//				if( seq.moveSpeed != 0 ) {
//					anim.addTag("MoveSpeed " + seq.moveSpeed);
//				}
//				if( seq.nonLooping == 1 ) {
//					anim.addTag("NonLooping");
//				}
//				if( seq.rarity > 0 ) {
//					anim.addTag("Rarity " + seq.rarity);
//				}
//				mdlObject.add(anim);
//			}
//
//		// Step 3: Convert any global sequences
//		if( globalSequenceChunk != null )
//			for( int i = 0; i < globalSequenceChunk.globalSequences.length; i++ ) {
//				mdlObject.add(new Integer(globalSequenceChunk.globalSequences[i]));
//			}
//
//		// Step 4: Convert Texture refs
//		if( textureChunk != null )
//			for( Texture tex: textureChunk.texture ) {
//				int replaceableId = tex.replaceableId;
//				if( replaceableId == 0 && !tex.fileName.equals("") ) {
//					replaceableId = -1; // nice and tidy it up for the MDL code
//				}
//				Bitmap bmp = new Bitmap(tex.fileName, replaceableId);
//				bmp.setWrapStyle(tex.flags);
//				mdlObject.add(bmp);
//			}
//
//		// Step 6: Convert TVertexAnims
//		if( textureAnimationChunk != null )
//			for( TextureAnimation txa: textureAnimationChunk.textureAnimation ) {
//				ArrayList<AnimFlag> flags = new ArrayList<AnimFlag>();
//				if( txa.textureRotation != null ) {
//					AnimFlag flag = new AnimFlag(txa.textureRotation);
//					flags.add(flag);
//				}
//				if( txa.textureScaling != null ) {
//					AnimFlag flag = new AnimFlag(txa.textureScaling);
//					flags.add(flag);
//				}
//				if( txa.textureTranslation != null ) {
//					AnimFlag flag = new AnimFlag(txa.textureTranslation);
//					flags.add(flag);
//				}
//				TextureAnim anim = new TextureAnim(flags);
//				mdlObject.add(anim);
//			}
//
//		// Step 5: Convert Material refs
//		if( materialChunk != null )
//			for( MaterialChunk.Material mat: materialChunk.material ) {
//				List<Layer> layers = new ArrayList<Layer>();
//				for( LayerChunk.Layer lay: mat.layerChunk.layer ) {
//					Layer layer = new Layer(Layer.FilterMode.fromId(lay.filterMode).getMdlText(), lay.textureId);
//					int shadingFlags = lay.shadingFlags;
//					// 0x1: unshaded
//	                // 0x2: sphere environment map
//	                // 0x4: ?
//	                // 0x8: ?
//	                // 0x10: two sided
//	                // 0x20: unfogged
//	                // 0x30: no depth test
//	                // 0x40: no depth set
//					if( hasFlag(shadingFlags, 0x1) ) {
//						layer.add("Unshaded");
//					}
//					if( hasFlag(shadingFlags, 0x2) ) {
//						layer.add("SphereEnvironmentMap");
//					}
//					if( hasFlag(shadingFlags, 0x10) ) {
//						layer.add("TwoSided");
//					}
//					if( hasFlag(shadingFlags, 0x20) ) {
//						layer.add("Unfogged");
//					}
//					if( hasFlag(shadingFlags, 0x40) ) {
//						layer.add("NoDepthTest");
//					}
//					if( hasFlag(shadingFlags, 0x80) ) {
//						layer.add("NoDepthSet");
//					}
//					layer.setTVertexAnimId(lay.textureAnimationId);
//					layer.setCoordId(lay.unknownNull_CoordID); // this isn't an unknown field! it's coordID! don't be like Magos and forget!
//					//(breaks the volcano model, this is why War3ModelEditor can't open volcano!)
//					if( lay.materialAlpha != null ) {
//						AnimFlag flag = new AnimFlag(lay.materialAlpha);
//						layer.getAnims().add(flag);
//					} else if (lay.alpha != 1.0f) {
//						layer.setStaticAlpha(lay.alpha);
//					}
//					if( lay.materialTextureId != null ) {
//						AnimFlag flag = new AnimFlag(lay.materialTextureId);
//						layer.getAnims().add(flag);
//					}
//					layer.updateRefs(mdlObject);
//					layers.add(layer);
//				}
//				Material material = new Material(layers);
//				material.setPriorityPlane(mat.priorityPlane);
//				if( hasFlag(mat.flags, 0x1) ) {
//					material.add("ConstantColor");
//				}
//				if( hasFlag(mat.flags, 0x10) ) {
//					material.add("SortPrimsFarZ");
//				}
//				if( hasFlag(mat.flags, 0x20) ) {
//					material.add("FullResolution");
//				}
//				mdlObject.add(material);
//			}
//
//		// Step 7: Geoset
//		if( geosetChunk != null )
//			for( GeosetChunk.Geoset mdxGeo : geosetChunk.geoset ) {
//				Geoset mdlGeo = new Geoset();
//				mdlGeo.setExtLog(new ExtLog(vertex3f(mdxGeo.minimumExtent), vertex3f(mdxGeo.maximumExtent), mdxGeo.boundsRadius));
//				for( GeosetChunk.Geoset.Extent ext: mdxGeo.extent ) {
//					ExtLog extents = new ExtLog(ext);
//					Animation anim = new Animation(extents);
//					mdlGeo.add(anim);
//				}
//
//				mdlGeo.setMaterialID(mdxGeo.materialId);
//				ArrayList<UVLayer> uv = new ArrayList<UVLayer>();
//				for( int i = 0; i < mdxGeo.nrOfTextureVertexGroups; i++ ) {
//					UVLayer layer = new UVLayer();
//					uv.add(layer);
//					mdlGeo.addUVLayer(layer);
//				}
//
//				int nVertices = mdxGeo.vertexPositions.length / 3;
//				for( int k = 0; k < nVertices; k++ ) {
//					int i = k * 3;
//					int j = k * 2;
//					GeosetVertex gv;
//					mdlGeo.add(gv = new GeosetVertex(
//							mdxGeo.vertexPositions[i],
//							mdxGeo.vertexPositions[i+1],
//							mdxGeo.vertexPositions[i+2]));
//					gv.setVertexGroup(mdxGeo.vertexGroups[k]);
//					mdlGeo.addNormal(new Normal(
//							mdxGeo.vertexNormals[i],
//							mdxGeo.vertexNormals[i+1],
//							mdxGeo.vertexNormals[i+2]));
//
//					for( int uvId = 0; uvId < uv.size(); uvId++ ) {
//						uv.get(uvId).addTVertex(new TVertex(
//								mdxGeo.vertexTexturePositions[uvId * nVertices + j],
//								mdxGeo.vertexTexturePositions[uvId * nVertices + j + 1]));
//					}
//				}
//				// guys I didn't code this to allow experimental
//				// non-triangle faces that were suggested to exist
//				// on the web (i.e. quads).
//				// if you wanted to fix that, you'd want to do it below
//				for( int i = 0; i < mdxGeo.faces.length; i+=3 ) {
//					Triangle triangle = new Triangle(mdxGeo.faces[i+0], mdxGeo.faces[i+1], mdxGeo.faces[i+2], mdlGeo);
//					mdlGeo.add(triangle);
//				}
//				if( mdxGeo.selectionType == 4 ) {
//					mdlGeo.addFlag("Unselectable");
//				}
//				mdlGeo.setSelectionGroup(mdxGeo.selectionGroup);
//				int index = 0;
//				for( int size: mdxGeo.matrixGroups ) {
//					Matrix m = new Matrix();
//					for( int i = 0; i < size; i++ ) {
//						m.addId(mdxGeo.matrixIndexs[index++]);
//					}
//					mdlGeo.addMatrix(m);
//				}
//				mdlObject.add(mdlGeo);
//			}
//
//		// Step 8: GeosetAnims
//		if( geosetAnimationChunk != null )
//			for( GeosetAnimationChunk.GeosetAnimation geosetAnim: geosetAnimationChunk.geosetAnimation ) {
//				GeosetAnim mdlGeoAnim = new GeosetAnim(geosetAnim.geosetId);
//				if( geosetAnim.geosetAlpha == null ) {
//					mdlGeoAnim.setStaticAlpha(geosetAnim.alpha);
//				} else {
//					mdlGeoAnim.addAnimFlag(new AnimFlag(geosetAnim.geosetAlpha));
//				}
//				mdlGeoAnim.setDropShadow((geosetAnim.flags & 1) == 1);
//				if ( (geosetAnim.flags & 2) == 2 ) {
//					if( geosetAnim.geosetColor == null ) {
//						mdlGeoAnim.setStaticColor(vertex3f(geosetAnim.color));
//					} else {
//						mdlGeoAnim.addAnimFlag(new AnimFlag(geosetAnim.geosetColor));
//					}
//				}
//				mdlObject.add(mdlGeoAnim);
//			}
//
//		// Step 9:
//		// convert "IdObjects" as I called them in my high school mdl code (nodes)
//
//		// Bones
//		if( boneChunk != null )
//		for( BoneChunk.Bone bone: boneChunk.bone ) {
//			Bone mdlBone = new Bone(bone.node.name);
//			mdlBone.setPivotPoint(null);
//			// debug print:
////			System.out.println(mdlBone.getName() + ": " + Integer.toBinaryString(bone.node.flags));
//			if( (bone.node.flags & 256) != 256 ) {
//				System.err.println("MDX -> MDL error: A bone '"+bone.node.name+"' not flagged as bone in MDX!");
//			}
//			// ----- Convert Base NODE to "IDOBJECT" -----
//			mdlBone.setParentId(bone.node.parentId);
//			mdlBone.setObjectId(bone.node.objectId);
//			int shift = 1;
//			Node node = bone.node;
//			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
//				if( ((node.flags >> shift) & 1) == 1 ) {
//					mdlBone.add(flag.getMdlText());
//				}
//			}
//			// translations next
//			if( node.geosetTranslation != null ) {
//				mdlBone.add(new AnimFlag(node.geosetTranslation));
//			}
//			if( node.geosetScaling != null ) {
//				mdlBone.add(new AnimFlag(node.geosetScaling));
//			}
//			if( node.geosetRotation != null ) {
//				mdlBone.add(new AnimFlag(node.geosetRotation));
//			}
//			//   ----- End Base NODE to "IDOBJECT" -----
//
//			mdlBone.setGeosetId(bone.geosetId);
//			mdlBone.setGeosetAnimId(bone.geosetAnimationId);
//
//			mdlObject.add(mdlBone);
//		}
//		// Lights
//		if( lightChunk != null )
//		for( LightChunk.Light light: lightChunk.light ) {
//			Light mdlLight = new Light(light.node.name);
//			// debug print:
////			System.out.println(mdlBone.getName() + ": " + Integer.toBinaryString(bone.node.flags));
//			if( (light.node.flags & 512) != 512 ) {
//				System.err.println("MDX -> MDL error: A light '"+light.node.name+"' not flagged as light in MDX!");
//			}
//			// ----- Convert Base NODE to "IDOBJECT" -----
//			mdlLight.setParentId(light.node.parentId);
//			mdlLight.setObjectId(light.node.objectId);
//			int shift = 1;
//			Node node = light.node;
//			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
//				if( ((node.flags >> shift) & 1) == 1 ) {
//					mdlLight.add(flag.getMdlText());
//				}
//			}
//			// translations next
//			if( node.geosetTranslation != null ) {
//				mdlLight.add(new AnimFlag(node.geosetTranslation));
//			}
//			if( node.geosetScaling != null ) {
//				mdlLight.add(new AnimFlag(node.geosetScaling));
//			}
//			if( node.geosetRotation != null ) {
//				mdlLight.add(new AnimFlag(node.geosetRotation));
//			}
//			//   ----- End Base NODE to "IDOBJECT" -----
//			//System.out.println(mdlLight.getName() + ": " + Integer.toBinaryString(light.type));
//			switch (light.type) {
//			case 0:
//				mdlLight.add("Omnidirectional");
//				break;
//			case 1:
//				mdlLight.add("Directional");
//				break;
//			case 2:
//				mdlLight.add("Ambient"); // I'm not 100% that Ambient is supposed to be a possible flag type
//				break;					 //  ---  Is it for Ambient only? All lights have the Amb values
//			default:
//				mdlLight.add("Omnidirectional");
//				break;
//			}
//			mdlLight.setAttenuationStart(light.attenuationStart);
//			mdlLight.setAttenuationEnd(light.attenuationEnd);
//			if( light.lightVisibility != null ) {
//				mdlLight.add(new AnimFlag(light.lightVisibility));
//			}
//			if( light.lightColor != null ) {
//				mdlLight.add(new AnimFlag(light.lightColor));
//			} else {
//				mdlLight.setStaticColor(vertex3f(light.color));
//			}
//			if( light.lightIntensity != null ) {
//				mdlLight.add(new AnimFlag(light.lightIntensity));
//			} else {
//				mdlLight.setIntensity(light.intensity);
//			}
//			if( light.lightAmbientColor != null ) {
//				mdlLight.add(new AnimFlag(light.lightAmbientColor));
//			} else {
//				mdlLight.setStaticAmbColor(vertex3f(light.ambientColor));
//			}
//			if( light.lightAmbientIntensity != null ) {
//				mdlLight.add(new AnimFlag(light.lightAmbientIntensity));
//			} else {
//				mdlLight.setAmbIntensity(light.ambientIntensity);
//			}
//
//			mdlObject.add(mdlLight);
//		}
//		// Helpers
//		if( helperChunk != null )
//		for( HelperChunk.Helper helper: helperChunk.helper ) {
//			Helper mdlBone = new Helper(helper.node.name);
//			mdlBone.setPivotPoint(null);
//			// debug print:
////			System.out.println(mdlBone.getName() + ": " + Integer.toBinaryString(bone.node.flags));
//			if( (helper.node.flags & 1) != 0 ) {
//				System.err.println("MDX -> MDL error: A helper '"+helper.node.name+"' not flagged as helper in MDX!");
//			}
//			// ----- Convert Base NODE to "IDOBJECT" -----
//			mdlBone.setParentId(helper.node.parentId);
//			mdlBone.setObjectId(helper.node.objectId);
//			int shift = 1;
//			Node node = helper.node;
//			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
//				if( ((node.flags >> shift) & 1) == 1 ) {
//					mdlBone.add(flag.getMdlText());
//				}
//			}
//			// translations next
//			if( node.geosetTranslation != null ) {
//				mdlBone.add(new AnimFlag(node.geosetTranslation));
//			}
//			if( node.geosetScaling != null ) {
//				mdlBone.add(new AnimFlag(node.geosetScaling));
//			}
//			if( node.geosetRotation != null ) {
//				mdlBone.add(new AnimFlag(node.geosetRotation));
//			}
//			//   ----- End Base NODE to "IDOBJECT" -----
//
//			mdlObject.add(mdlBone);
//		}
//		// Attachment
//		if( attachmentChunk != null )
//		for( AttachmentChunk.Attachment attachment: attachmentChunk.attachment ) {
//			Attachment mdlAttachment = new Attachment(attachment.node.name);
//			// debug print:
////			System.out.println(mdlBone.getName() + ": " + Integer.toBinaryString(bone.node.flags));
//			if( (attachment.node.flags & 2048) != 2048 ) {
//				System.err.println("MDX -> MDL error: A light '"+attachment.node.name+"' not flagged as light in MDX!");
//			}
//			// ----- Convert Base NODE to "IDOBJECT" -----
//			mdlAttachment.setParentId(attachment.node.parentId);
//			mdlAttachment.setObjectId(attachment.node.objectId);
//			int shift = 1;
//			Node node = attachment.node;
//			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
//				if( ((node.flags >> shift) & 1) == 1 ) {
//					mdlAttachment.add(flag.getMdlText());
//				}
//			}
//			// translations next
//			if( node.geosetTranslation != null ) {
//				mdlAttachment.add(new AnimFlag(node.geosetTranslation));
//			}
//			if( node.geosetScaling != null ) {
//				mdlAttachment.add(new AnimFlag(node.geosetScaling));
//			}
//			if( node.geosetRotation != null ) {
//				mdlAttachment.add(new AnimFlag(node.geosetRotation));
//			}
//			//   ----- End Base NODE to "IDOBJECT" -----
//
//			if( attachment.unknownNull != 0 ) {
//				System.err.println("Surprise! This model has a special attachment data point worthy of documenting! " + modelChunk.name);
//			}
//			//System.out.println(attachment.node.name + ": " + Integer.toBinaryString(attachment.unknownNull));
//
//			if( attachment.attachmentVisibility != null ) {
//				mdlAttachment.add(new AnimFlag(attachment.attachmentVisibility));
//			}
//
//			mdlAttachment.setAttachmentID(attachment.attachmentId);
//			mdlAttachment.setPath(attachment.unknownName_modelPath);
//
//			mdlObject.add(mdlAttachment);
//		}
//		// ParticleEmitter (number 1 kind)
//		if( particleEmitterChunk != null )
//		for( ParticleEmitterChunk.ParticleEmitter emitter: particleEmitterChunk.particleEmitter ) {
//			ParticleEmitter mdlEmitter = new ParticleEmitter(emitter.node.name);
//			// debug print:
//			if( (emitter.node.flags & 4096) != 4096 ) {
//				System.err.println("MDX -> MDL error: A particle emitter '"+emitter.node.name+"' not flagged as particle emitter in MDX!");
//			}
//			System.out.println(mdlEmitter.getName() + ": " + Integer.toBinaryString(emitter.node.flags));
////			System.out.println(emitter.node.name + ": " + Integer.toBinaryString(emitter.node.flags));
//			// ----- Convert Base NODE to "IDOBJECT" -----
//			mdlEmitter.setParentId(emitter.node.parentId);
//			mdlEmitter.setObjectId(emitter.node.objectId);
//			int shift = 1;
//			Node node = emitter.node;
//			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
//				if( ((node.flags >> shift) & 1) == 1 ) {
//					mdlEmitter.add(flag.getMdlText());
//				}
//			}
//			// translations next
//			if( node.geosetTranslation != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetTranslation));
//			}
//			if( node.geosetScaling != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetScaling));
//			}
//			if( node.geosetRotation != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetRotation));
//			}
//			//   ----- End Base NODE to "IDOBJECT" -----
//
//			if( emitter.unknownNull != 0 ) {
//				System.err.println("Surprise! This model has a special emitter data point worthy of documenting! " + modelChunk.name);
//			}
//			//System.out.println(attachment.node.name + ": " + Integer.toBinaryString(attachment.unknownNull));
//
//			if( emitter.particleEmitterVisibility != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.particleEmitterVisibility));
//			}
//			mdlEmitter.setEmissionRate(emitter.emissionRate);
//			mdlEmitter.setGravity(emitter.gravity);
//			mdlEmitter.setInitVelocity(emitter.initialVelocity);
//			mdlEmitter.setLatitude(emitter.latitude);
//			mdlEmitter.setLifeSpan(emitter.lifeSpan);
//			mdlEmitter.setLongitude(emitter.longitude);
//			mdlEmitter.setMDLEmitter(((emitter.node.flags >> 15) & 1) == 1);
//			if( !mdlEmitter.isMDLEmitter() && ((emitter.node.flags >> 8) & 1) == 1) {
//				System.err.println("WARNING in MDX -> MDL: ParticleEmitter of unknown type! Defaults to EmitterUsesTGA in my MDL code!");
//			}
//			mdlEmitter.setPath(emitter.spawnModelFileName);
////			if( emitter. != null ) {
////				mdlEmitter.add(new AnimFlag(emitter.attachmentVisibility));
////			}
//
//			mdlObject.add(mdlEmitter);
//		}
//		// ParticleEmitter2
//		if( particleEmitter2Chunk != null )
//		for( ParticleEmitter2Chunk.ParticleEmitter2 emitter: particleEmitter2Chunk.particleEmitter2 ) {
//			ParticleEmitter2 mdlEmitter = new ParticleEmitter2(emitter.node.name);
//			// debug print:
//			if( (emitter.node.flags & 4096) != 4096 ) {
//				System.err.println("MDX -> MDL error: A particle emitter '"+emitter.node.name+"' not flagged as particle emitter in MDX!");
//			}
//			System.out.println(mdlEmitter.getName() + ": " + String.format("%32s",Integer.toBinaryString(emitter.node.flags)).replace(' ', '0') + ", " + emitter.node.flags);
////			System.out.println(emitter.node.name + ": " + Integer.toBinaryString(emitter.node.flags));
//			// ----- Convert Base NODE to "IDOBJECT" -----
//			mdlEmitter.setParentId(emitter.node.parentId);
//			mdlEmitter.setObjectId(emitter.node.objectId);
//			int shift = 1;
//			Node node = emitter.node;
//			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
//				if( ((node.flags >> shift) & 1) == 1 ) {
//					mdlEmitter.add(flag.getMdlText());
//				}
//			}
//			// translations next
//			if( node.geosetTranslation != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetTranslation));
//			}
//			if( node.geosetScaling != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetScaling));
//			}
//			if( node.geosetRotation != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetRotation));
//			}
//			//   ----- End Base NODE to "IDOBJECT" -----
//			//System.out.println(attachment.node.name + ": " + Integer.toBinaryString(attachment.unknownNull));
//
//			if( emitter.particleEmitter2Visibility != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.particleEmitter2Visibility));
//			}
//			if( ((node.flags >> 15) & 1) == 1 ) {
//				mdlEmitter.add("Unshaded");
//			}
//			if( ((node.flags >> 16) & 1) == 1 ) {
//				mdlEmitter.add("SortPrimsFarZ");
//			}
//			if( ((node.flags >> 17) & 1) == 1 ) {
//				mdlEmitter.add("LineEmitter");
//			}
//			if( ((node.flags >> 18) & 1) == 1 ) {
//				mdlEmitter.add("Unfogged");
//			}
//			if( ((node.flags >> 19) & 1) == 1 ) {
//				mdlEmitter.add("ModelSpace");
//			}
//			if( ((node.flags >> 20) & 1) == 1 ) {
//				mdlEmitter.add("XYQuad");
//			}
//			if( ((node.flags >> shift) & 26) == 1 ) {
//				mdlEmitter.add(IdObject.NodeFlags.DONTINHERIT_ROTATION.getMdlText());
//			}
//			if( emitter.particleEmitter2Speed != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.particleEmitter2Speed));
//			} else
//				mdlEmitter.setSpeed(emitter.speed);
//			mdlEmitter.setVariation(emitter.variation);
//			if( emitter.particleEmitter2Latitude != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.particleEmitter2Latitude));
//			} else
//				mdlEmitter.setLatitude(emitter.latitude);
//			mdlEmitter.setGravity(emitter.gravity);
//			mdlEmitter.setLifeSpan(emitter.lifespan);
//			if( emitter.particleEmitter2EmissionRate != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.particleEmitter2EmissionRate));
//			} else
//				mdlEmitter.setEmissionRate(emitter.emissionRate);
//			if( emitter.particleEmitter2Length != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.particleEmitter2Length));
//			} else
//				mdlEmitter.setLength(emitter.length);
//			if( emitter.particleEmitter2Width != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.particleEmitter2Width));
//			} else
//				mdlEmitter.setWidth(emitter.width);
//			switch (emitter.filterMode) {
//			case 0:
//				mdlEmitter.add("Blend");
//				break;
//			case 1:
//				mdlEmitter.add("Additive");
//				break;
//			case 2:
//				mdlEmitter.add("Modulate");
//				break;
//			case 3:
//				mdlEmitter.add("Modulate2x");
//				break;
//			case 4:
//				mdlEmitter.add("AlphaKey");
//				break;
//			default:
//				System.err.println("Unkown filter mode error");
//				mdlEmitter.add("UnknownFilterMode");
//				break;
//			}
//			mdlEmitter.setRows(emitter.rows);
//			mdlEmitter.setColumns(emitter.columns);
//			switch (emitter.headOrTail) {
//			case 0:
//				mdlEmitter.add("Head");
//				break;
//			case 1:
//				mdlEmitter.add("Tail");
//				break;
//			case 2:
//				mdlEmitter.add("Both");
//				break;
//			default:
//				System.err.println("Unkown head or tail error");
//				mdlEmitter.add("UnknownHeadOrTail");
//				break;
//			}
//			mdlEmitter.setTailLength(emitter.tailLength);
//			mdlEmitter.setTime(emitter.time);
//			// SegmentColor - Inverse order for MDL!
//			for( int i = 0; i < 3; i++ )
//				mdlEmitter.setSegmentColor(i, new Vertex(emitter.segmentColor[i*3 + 2], emitter.segmentColor[i*3 + 1], emitter.segmentColor[i*3 + 0]));
//			mdlEmitter.setAlpha(vertex3f(emitter.segmentAlpha));
//			mdlEmitter.setParticleScaling(vertex3f(emitter.segmentScaling));
//			mdlEmitter.setLifeSpanUVAnim(new Vertex(emitter.headIntervalStart, emitter.headIntervalEnd, emitter.headIntervalRepeat));
//			mdlEmitter.setDecayUVAnim(new Vertex(emitter.headDecayIntervalStart, emitter.headDecayIntervalEnd, emitter.headDecayIntervalRepeat));
//			mdlEmitter.setTailUVAnim(new Vertex(emitter.tailIntervalStart, emitter.tailIntervalEnd, emitter.tailIntervalRepeat));
//			mdlEmitter.setTailDecayUVAnim(new Vertex(emitter.tailDecayIntervalStart, emitter.tailDecayIntervalEnd, emitter.tailDecayIntervalRepeat));
//			mdlEmitter.setTextureID(emitter.textureId);
//			if( emitter.squirt == 1 ) {
//				mdlEmitter.add("Squirt");
//			}
//			mdlEmitter.setPriorityPlane(emitter.priorityPlane);
//			mdlEmitter.setReplaceableId(emitter.replaceableId);
//
//			mdlObject.add(mdlEmitter);
//		}
//		// RibbonEmitter
//		if( ribbonEmitterChunk != null )
//		for( RibbonEmitterChunk.RibbonEmitter emitter: ribbonEmitterChunk.ribbonEmitter ) {
//			RibbonEmitter mdlEmitter = new RibbonEmitter(emitter.node.name);
//			// debug print:
//			if( (emitter.node.flags & 16384) != 16384 ) {
//				System.err.println("MDX -> MDL error: A ribbon emitter '"+emitter.node.name+"' not flagged as ribbon emitter in MDX!");
//			}
//			System.out.println(mdlEmitter.getName() + ": " + Integer.toBinaryString(emitter.node.flags));
////			System.out.println(emitter.node.name + ": " + Integer.toBinaryString(emitter.node.flags));
//			// ----- Convert Base NODE to "IDOBJECT" -----
//			mdlEmitter.setParentId(emitter.node.parentId);
//			mdlEmitter.setObjectId(emitter.node.objectId);
//			int shift = 1;
//			Node node = emitter.node;
////			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
////				if( ((node.flags >> shift) & 1) == 1 ) {
////					mdlEmitter.add(flag.getMdlText());
////				}
////			}
//			// translations next
//			if( node.geosetTranslation != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetTranslation));
//			}
//			if( node.geosetScaling != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetScaling));
//			}
//			if( node.geosetRotation != null ) {
//				mdlEmitter.add(new AnimFlag(node.geosetRotation));
//			}
//			//   ----- End Base NODE to "IDOBJECT" -----
//
//			if( emitter.unknownNull != 0 ) {
//				System.err.println("Surprise! This model has a special emitter data point worthy of documenting! " + modelChunk.name);
//			}
//			//System.out.println(attachment.node.name + ": " + Integer.toBinaryString(attachment.unknownNull));
//
//			if( emitter.ribbonEmitterVisibility != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.ribbonEmitterVisibility));
//			}
//			if( emitter.ribbonEmitterHeightAbove != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.ribbonEmitterHeightAbove));
//			}
//			if( emitter.ribbonEmitterHeightBelow != null ) {
//				mdlEmitter.add(new AnimFlag(emitter.ribbonEmitterHeightBelow));
//			}
//			mdlEmitter.setAlpha(emitter.alpha);
//			mdlEmitter.setStaticColor(vertex3f(emitter.color));
//			mdlEmitter.setLifeSpan(emitter.lifeSpan);
//			mdlEmitter.setEmissionRate(emitter.emissionRate);
//			mdlEmitter.setRows(emitter.rows);
//			mdlEmitter.setColumns(emitter.columns);
//			mdlEmitter.setMaterialId(emitter.materialId);
//			mdlEmitter.setGravity(emitter.gravity);
//
//			mdlObject.add(mdlEmitter);
//		}
//		// EventObject
//		if( eventObjectChunk != null )
//		for( EventObjectChunk.EventObject evtobj: eventObjectChunk.eventObject ) {
//			EventObject mdlEvtObj = new EventObject(evtobj);
////			// debug print:
////			System.out.println(mdlEvtObj.getName() + ": " + Integer.toBinaryString(evtobj.node.flags));
////			if( (evtobj.node.flags & 1024) != 1024 ) {
////				System.err.println("MDX -> MDL error: An eventobject '"+evtobj.node.name+"' not flagged as eventobject in MDX!");
////			}
//////			System.out.println(emitter.node.name + ": " + Integer.toBinaryString(emitter.node.flags));
////			// ----- Convert Base NODE to "IDOBJECT" -----
////			mdlEvtObj.setParentId(evtobj.node.parentId);
////			mdlEvtObj.setObjectId(evtobj.node.objectId);
////			int shift = 1;
////			Node node = evtobj.node;
//////			for( IdObject.NodeFlags flag: IdObject.NodeFlags.values() ) {
//////				if( ((node.flags >> shift) & 1) == 1 ) {
//////					mdlEmitter.add(flag.getMdlText());
//////				}
//////			}
////			// translations next
////			if( node.geosetTranslation != null ) {
////				mdlEvtObj.add(new AnimFlag(node.geosetTranslation));
////			}
////			if( node.geosetScaling != null ) {
////				mdlEvtObj.add(new AnimFlag(node.geosetScaling));
////			}
////			if( node.geosetRotation != null ) {
////				mdlEvtObj.add(new AnimFlag(node.geosetRotation));
////			}
////			//   ----- End Base NODE to "IDOBJECT" -----
////
////			//System.out.println(attachment.node.name + ": " + Integer.toBinaryString(attachment.unknownNull));
////
////			if( evtobj.ribbonEmitterVisibility != null ) {
////				mdlEvtObj.add(new AnimFlag(evtobj.ribbonEmitterVisibility));
////			}
////			if( evtobj.ribbonEmitterHeightAbove != null ) {
////				mdlEvtObj.add(new AnimFlag(evtobj.ribbonEmitterHeightAbove));
////			}
////			if( evtobj.ribbonEmitterHeightBelow != null ) {
////				mdlEvtObj.add(new AnimFlag(evtobj.ribbonEmitterHeightBelow));
////			}
////			mdlEvtObj.setAlpha(evtobj.alpha);
////			mdlEvtObj.setStaticColor(vertex3f(evtobj.color));
////			mdlEvtObj.setLifeSpan(evtobj.lifeSpan);
////			mdlEvtObj.setEmissionRate(evtobj.emissionRate);
////			mdlEvtObj.setRows(evtobj.rows);
////			mdlEvtObj.setColumns(evtobj.columns);
////			mdlEvtObj.setMaterialId(evtobj.materialId);
////			mdlEvtObj.setGravity(evtobj.gravity);
//
//			mdlObject.add(mdlEvtObj);
//		}
//		// Camera
//		if( cameraChunk != null )
//		for( CameraChunk.Camera cam: cameraChunk.camera ) {
//			Camera mdlEvtObj = new Camera(cam);
//			mdlObject.add(mdlEvtObj);
//		}
//		// CollisionShape
//		if( collisionShapeChunk != null )
//		for( CollisionShapeChunk.CollisionShape cam: collisionShapeChunk.collisionShape ) {
//			CollisionShape mdlEvtObj = new CollisionShape(cam);
//			mdlObject.add(mdlEvtObj);
//		}
//
//		if( pivotPointChunk != null ) {
//			for( int objId = 0; objId < pivotPointChunk.pivotPoints.length / 3; objId ++ ) {
//				System.err.println(new Vertex(
//						pivotPointChunk.pivotPoints[objId*3 + 0],
//						pivotPointChunk.pivotPoints[objId*3 + 1],
//						pivotPointChunk.pivotPoints[objId*3 + 2]
//						));
//				mdlObject.addPivotPoint(new Vertex(
//						pivotPointChunk.pivotPoints[objId*3 + 0],
//						pivotPointChunk.pivotPoints[objId*3 + 1],
//						pivotPointChunk.pivotPoints[objId*3 + 2]
//						));
//			}
//		}
//
//		mdlObject.doPostRead(); //fixes all the things
		return new MDL(this);
	}

	public MdxModel() {

	}

	public MdxModel(final MDL mdl) {
		mdl.doSavePreps(); // restores all GeosetID, ObjectID, TextureID, MaterialID stuff all based on object references in the Java
		//   						(this is so that you can write a program that does something like "mdl.add(new Bone())" without
		// 							a problem, or even "mdl.add(otherMdl.getGeoset(5))" and have the geoset's textures and materials
		//							all be carried over with it via object references in java

		//							also this re-creates all matrices, which are consumed by the MatrixEater at runtime in doPostRead()
		//							in favor of each vertex having its own attachments list, no vertex groups)

		versionChunk = new VersionChunk();
		versionChunk.version = mdl.getFormatVersion();
		modelChunk = new ModelChunk();
		modelChunk.blendTime = mdl.getBlendTime();
		modelChunk.name = mdl.getHeaderName();
		if( mdl.getExtents() != null ) {
			modelChunk.boundsRadius = (float)mdl.getExtents().getBoundsRadius();
			if( mdl.getExtents().getMaximumExtent() != null ) {
				modelChunk.maximumExtent = mdl.getExtents().getMaximumExtent().toFloatArray();
			}
			if( mdl.getExtents().getMinimumExtent() != null ) {
				modelChunk.minimumExtent = mdl.getExtents().getMinimumExtent().toFloatArray();
			}
		}
		if( mdl.getAnims().size() > 0 ) {
			sequenceChunk = new SequenceChunk();
			sequenceChunk.sequence = new Sequence[mdl.getAnims().size()];
			for( int i = 0; i < mdl.getAnims().size(); i++ ) {
				final SequenceChunk.Sequence seq = sequenceChunk.new Sequence(mdl.getAnim(i));
				sequenceChunk.sequence[i] = seq;
			}
		}
		if( mdl.getGlobalSeqs().size() > 0 ) {
			globalSequenceChunk = new GlobalSequenceChunk();
			globalSequenceChunk.globalSequences = new int[mdl.getGlobalSeqs().size()];
			for(int i = 0; i < mdl.getGlobalSeqs().size(); i++ ) {
				globalSequenceChunk.globalSequences[i] = mdl.getGlobalSeq(i).intValue();
			}
		}
		if( mdl.getMaterials().size() > 0 ) {
			materialChunk = new MaterialChunk();
			materialChunk.material = new MaterialChunk.Material[mdl.getMaterials().size()];
			for( int i = 0; i < mdl.getMaterials().size(); i++ ) {
				materialChunk.material[i] = materialChunk.new Material(mdl.getMaterial(i));
			}
		}
		if( mdl.getTextures().size() > 0 ) {
			textureChunk = new TextureChunk();
			textureChunk.texture = new TextureChunk.Texture[mdl.getTextures().size()];
			for( int i = 0; i < mdl.getTextures().size(); i++ ) {
				textureChunk.texture[i] = textureChunk.new Texture(mdl.getTexture(i));
			}
		}
		if( mdl.getTexAnims().size() > 0 ) {
			textureAnimationChunk = new TextureAnimationChunk();
			textureAnimationChunk.textureAnimation = new TextureAnimationChunk.TextureAnimation[mdl.getTexAnims().size()];
			for( int i = 0; i < mdl.getTexAnims().size(); i++ ) {
				textureAnimationChunk.textureAnimation[i] = textureAnimationChunk.new TextureAnimation(mdl.getTexAnims().get(i));
			}
		}
		if( mdl.getGeosets().size() > 0 ) {
			geosetChunk = new GeosetChunk();
			geosetChunk.geoset = new GeosetChunk.Geoset[mdl.getGeosets().size()];
			for( int i = 0; i < mdl.getGeosets().size(); i++ ) {
				geosetChunk.geoset[i] = geosetChunk.new Geoset(mdl.getGeoset(i));
			}
		}
		if( mdl.getGeosetAnims().size() > 0 ) {
			geosetAnimationChunk = new GeosetAnimationChunk();
			// Shave off GeosetAnims that are just totally empty and stuff
			final List<GeosetAnim> nonEmptyGeosetAnimations = new ArrayList<GeosetAnim>();
			for( int i = 0; i < mdl.getGeosetAnims().size(); i++ ) {
				final GeosetAnim geosetAnim = mdl.getGeosetAnim(i);
				if( !geosetAnim.getAnimFlags().isEmpty() || !geosetAnim.isDropShadow() || Math.abs(geosetAnim.getStaticAlpha()-1.0f) > 0.0001f || geosetAnim.getStaticColor() != null ) {
					nonEmptyGeosetAnimations.add(geosetAnim);
				}
			}
			geosetAnimationChunk.geosetAnimation = new GeosetAnimationChunk.GeosetAnimation[nonEmptyGeosetAnimations.size()];
			for( int i = 0; i < nonEmptyGeosetAnimations.size(); i++ ) {
				geosetAnimationChunk.geosetAnimation[i] = geosetAnimationChunk.new GeosetAnimation(nonEmptyGeosetAnimations.get(i));
			}
		}
		if( mdl.sortedIdObjects(Bone.class).size() > 0 ) {
			boneChunk = new BoneChunk();
			final List<Bone> nodes = mdl.sortedIdObjects(Bone.class);
			boneChunk.bone = new BoneChunk.Bone[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				boneChunk.bone[i] = boneChunk.new Bone(nodes.get(i));
			}
		}
		if( mdl.sortedIdObjects(Light.class).size() > 0 ) {
			lightChunk = new LightChunk();
			final List<Light> nodes = mdl.sortedIdObjects(Light.class);
			lightChunk.light = new LightChunk.Light[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				lightChunk.light[i] = lightChunk.new Light(nodes.get(i));
			}
		}
		if( mdl.sortedIdObjects(Helper.class).size() > 0 ) {
			helperChunk = new HelperChunk();
			final List<Helper> nodes = mdl.sortedIdObjects(Helper.class);
			helperChunk.helper = new HelperChunk.Helper[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				helperChunk.helper[i] = helperChunk.new Helper(nodes.get(i));
			}
		}
		if( mdl.sortedIdObjects(Attachment.class).size() > 0 ) {
			attachmentChunk = new AttachmentChunk();
			final List<Attachment> nodes = mdl.sortedIdObjects(Attachment.class);
			attachmentChunk.attachment = new AttachmentChunk.Attachment[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				attachmentChunk.attachment[i] = attachmentChunk.new Attachment(nodes.get(i));
			}
		}
		if( mdl.getPivots().size() > 0 ) {
			pivotPointChunk = new PivotPointChunk();
			pivotPointChunk.pivotPoints = new float[mdl.getPivots().size() * 3];
			int i = 0;
			for( final Vertex pivot: mdl.getPivots() ) {
				pivotPointChunk.pivotPoints[i++] = (float)pivot.getX();
				pivotPointChunk.pivotPoints[i++] = (float)pivot.getY();
				pivotPointChunk.pivotPoints[i++] = (float)pivot.getZ();
			}
		}
		if( mdl.sortedIdObjects(ParticleEmitter.class).size() > 0 ) {
			particleEmitterChunk = new ParticleEmitterChunk();
			final List<ParticleEmitter> nodes = mdl.sortedIdObjects(ParticleEmitter.class);
			particleEmitterChunk.particleEmitter = new ParticleEmitterChunk.ParticleEmitter[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				particleEmitterChunk.particleEmitter[i] = particleEmitterChunk.new ParticleEmitter(nodes.get(i));
			}
		}
		if( mdl.sortedIdObjects(ParticleEmitter2.class).size() > 0 ) {
			particleEmitter2Chunk = new ParticleEmitter2Chunk();
			final List<ParticleEmitter2> nodes = mdl.sortedIdObjects(ParticleEmitter2.class);
			particleEmitter2Chunk.particleEmitter2 = new ParticleEmitter2Chunk.ParticleEmitter2[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				particleEmitter2Chunk.particleEmitter2[i] = particleEmitter2Chunk.new ParticleEmitter2(nodes.get(i));
			}
		}
		if( mdl.sortedIdObjects(RibbonEmitter.class).size() > 0 ) {
			ribbonEmitterChunk = new RibbonEmitterChunk();
			final List<RibbonEmitter> nodes = mdl.sortedIdObjects(RibbonEmitter.class);
			ribbonEmitterChunk.ribbonEmitter = new RibbonEmitterChunk.RibbonEmitter[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				ribbonEmitterChunk.ribbonEmitter[i] = ribbonEmitterChunk.new RibbonEmitter(nodes.get(i));
			}
		}
		if( mdl.sortedIdObjects(EventObject.class).size() > 0 ) {
			eventObjectChunk = new EventObjectChunk();
			final List<EventObject> nodes = mdl.sortedIdObjects(EventObject.class);
			eventObjectChunk.eventObject = new EventObjectChunk.EventObject[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				eventObjectChunk.eventObject[i] = eventObjectChunk.new EventObject(nodes.get(i));
			}
		}
		if( mdl.getCameras().size() > 0 ) {
			cameraChunk = new CameraChunk();
			final List<Camera> nodes = mdl.getCameras();
			cameraChunk.camera = new CameraChunk.Camera[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				cameraChunk.camera[i] = cameraChunk.new Camera(nodes.get(i));
			}
		}
		if( mdl.sortedIdObjects(CollisionShape.class).size() > 0 ) {
			collisionShapeChunk = new CollisionShapeChunk();
			final List<CollisionShape> nodes = mdl.sortedIdObjects(CollisionShape.class);
			collisionShapeChunk.collisionShape = new CollisionShapeChunk.CollisionShape[nodes.size()];
			for( int i = 0; i < nodes.size(); i++ ) {
				collisionShapeChunk.collisionShape[i] = collisionShapeChunk.new CollisionShape(nodes.get(i));
			}
		}
	}

	public void load(final BlizzardDataInputStream in) throws IOException {
		MdxUtils.checkId(in, "MDLX");
		for (int i = 0; i < 20; i++) {
			if (MdxUtils.checkOptionalId(in, VersionChunk.key)) {
				versionChunk = new VersionChunk();
				versionChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, ModelChunk.key)) {
				modelChunk = new ModelChunk();
				modelChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, SequenceChunk.key)) {
				sequenceChunk = new SequenceChunk();
				sequenceChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, GlobalSequenceChunk.key)) {
				globalSequenceChunk = new GlobalSequenceChunk();
				globalSequenceChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, MaterialChunk.key)) {
				materialChunk = new MaterialChunk();
				materialChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, TextureChunk.key)) {
				textureChunk = new TextureChunk();
				textureChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, TextureAnimationChunk.key)) {
				textureAnimationChunk = new TextureAnimationChunk();
				textureAnimationChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, GeosetChunk.key)) {
				geosetChunk = new GeosetChunk();
				geosetChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, GeosetAnimationChunk.key)) {
				geosetAnimationChunk = new GeosetAnimationChunk();
				geosetAnimationChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, BoneChunk.key)) {
				boneChunk = new BoneChunk();
				boneChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, LightChunk.key)) {
				lightChunk = new LightChunk();
				lightChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, HelperChunk.key)) {
				helperChunk = new HelperChunk();
				helperChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, AttachmentChunk.key)) {
				attachmentChunk = new AttachmentChunk();
				attachmentChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, PivotPointChunk.key)) {
				pivotPointChunk = new PivotPointChunk();
				pivotPointChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, ParticleEmitterChunk.key)) {
				particleEmitterChunk = new ParticleEmitterChunk();
				particleEmitterChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, ParticleEmitter2Chunk.key)) {
				particleEmitter2Chunk = new ParticleEmitter2Chunk();
				particleEmitter2Chunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, RibbonEmitterChunk.key)) {
				ribbonEmitterChunk = new RibbonEmitterChunk();
				ribbonEmitterChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, EventObjectChunk.key)) {
				eventObjectChunk = new EventObjectChunk();
				eventObjectChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, CameraChunk.key)) {
				cameraChunk = new CameraChunk();
				cameraChunk.load(in);
			} else if (MdxUtils.checkOptionalId(in, CollisionShapeChunk.key)) {
				collisionShapeChunk = new CollisionShapeChunk();
				collisionShapeChunk.load(in);
			}

		}
	}

	public void save(final BlizzardDataOutputStream out) throws IOException {
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
