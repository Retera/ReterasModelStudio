package com.matrixeater.src;

public class MDLScript {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		File skyVaultFile = new File("C:\\Users\\Eric\\Documents\\Warcraft\\Models\\Requests\\Spellbound\\Platform\\SkyVault15.mdl");
//		
//		MDL skyVault = MDL.read(skyVaultFile);
//		
//		for( Object o: skyVault.sortedIdObjects(Bone.class) ) {
//			Bone b = (Bone)o;
//			for( AnimFlag af: b.getAnimFlags() ) {
//				Integer globSeq = af.globalSeq;
//				if( af.hasGlobalSeq && b.getName().contains("copy") ) {
//					af.timeScale(0, globSeq, globSeq * 2, 0);
//					af.globalSeq = globSeq * 2;
//				}
//				if( af.hasGlobalSeq ) {
//					globSeq = af.globalSeq;
//					Collections.shuffle(af.values);
//					Collections.shuffle(af.inTans);
//					Collections.shuffle(af.outTans);
//					af.timeScale(0, af.globalSeq, 0, (int)(af.globalSeq * 1.5));
//					af.globalSeq = (int)(af.globalSeq * 1.5);
//				}
//				for( int i = af.times.size() - 2; i >= 0; i-- ) {
//					if( af.times.get(i).equals(af.times.get(i+1)) ) {
//						af.times.remove(i);
//						af.values.remove(i);
//						if( af.tans() ) {
//							af.inTans.remove(i);
//							af.outTans.remove(i);
//						}
//					}
//				}
//			}
//		}
//
//		skyVaultFile = new File("C:\\Users\\Eric\\Documents\\Warcraft\\Models\\Requests\\Spellbound\\Platform\\SkyVault15_ms.mdl");
//		
//		skyVault.printTo(skyVaultFile);
	}

}
