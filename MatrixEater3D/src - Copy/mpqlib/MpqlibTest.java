package mpqlib;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.JStormLib.MPQArchive;
import com.JStormLib.MPQArchiveException;

//import com.mundi4.mpq.MpqEntry;
//import com.mundi4.mpq.MpqFile;

public class MpqlibTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		try {
//			MpqFile file = new MpqFile("C:\\Program Files (x86)\\Warcraft III\\war3.mpq");
//			MpqEntry entry = new MpqEntry("Scripts\\Blizzard.j");
//			InputStream is = file.getInputStream(entry);
//			FileOutputStream fos = new FileOutputStream(new File("awesome.txt"));
//			int nextByte;
//			while( (nextByte = (is.read())) > -1 )
//			{
//				fos.write(nextByte);
//			}
//			is.close();
//			fos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		try {
//			String targetFile = "Scripts\\Blizzard.j";
//			File targetFileRef = new File(targetFile);
//			//System.out.println(targetFileRef.getParent());
//			Runtime.getRuntime().exec(new String [] {"MPQEditor.exe","/extract","war3patch.mpq",targetFile,System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\"+targetFileRef.getParent()});
//			File newTempFile = new File(System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\"+targetFile);
//			newTempFile.deleteOnExit();
//			try {
//				Thread.sleep(9000);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
			System.out.println(Arrays.asList(new Long [] {1L, 2L, 3L, 4L}).contains(1L));
            File f = new File("C:\\temp\\awesomesauce\\war3patch.mpq");
			MPQArchive mpq = MPQArchive.openArchive(f);
			try {
				mpq.extractFile("Textures\\HeroLich.blp",new File("C:\\temp\\awesomesauce\\HeroLich.blp"));
			} catch (MPQArchiveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
