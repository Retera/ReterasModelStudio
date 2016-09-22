package com.matrixeater.src;
import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;

import com.matrixeater.data.SaveProfile;

import de.deaod.jstormlib.MPQArchive;
import de.deaod.jstormlib.MPQArchiveOpenFlags;
import de.deaod.jstormlib.MPQArchiveOpenFlags.BaseProvider;
import de.deaod.jstormlib.MPQArchiveOpenFlags.StreamFlag;
import de.deaod.jstormlib.exceptions.MPQFileNotFoundException;
import de.deaod.jstormlib.exceptions.MPQFormatException;
import de.deaod.jstormlib.exceptions.MPQIsAVIException;


public class MPQHandler {
	boolean isDebugMode = false;
	MPQArchive war3;
	MPQArchive war3x;
	MPQArchive war3xlocal;
	MPQArchive war3patch;
	MPQArchive hfmd;
	MPQArchiveOpenFlags flags = new MPQArchiveOpenFlags();
	ArrayList<MPQArchive> mpqList = new ArrayList<MPQArchive>();
	
	public MPQHandler()
	{
		flags.setBaseProvider(BaseProvider.FILE);
		flags.setOpenFlags(StreamFlag.READ_ONLY);
		war3 = loadMPQ("war3.mpq");
		war3x = loadMPQ("war3x.mpq");
		war3xlocal = loadMPQ("war3xlocal.mpq");
		war3patch = loadMPQ("war3patch.mpq");
		if( isDebugMode ) {
			hfmd = loadMPQ("hfmd.exe");
		}
	}
	
	public File getGameFile(String filepath)
	{
		try {
			for( int i = mpqList.size()-1; i >= 0; i-- )
			{
				MPQArchive mpq = mpqList.get(i);
				if( mpq.hasFile(filepath) )
				{
					String tempDir = System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\";
					File tempProduct = new File(tempDir+filepath);
					tempProduct.getParentFile().mkdirs();
					tempProduct.delete();
						mpq.extractFile(filepath, tempProduct);
					tempProduct.deleteOnExit();
					return tempProduct;
				}
			}
		} catch (MPQFileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public void refresh()
	{
		try {
			war3.close();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		try {
			war3x.close();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		try {
			war3xlocal.close();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		try {
			war3patch.close();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		try {
			hfmd.close();
//		} catch (IOException e) {
//			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		mpqList.clear();
		war3 = loadMPQ("war3.mpq");
		war3x = loadMPQ("war3x.mpq");
		war3xlocal = loadMPQ("war3xlocal.mpq");
		war3patch = loadMPQ("war3patch.mpq");
		hfmd = loadMPQ("hfmd.mpq");
	}
	
	private MPQArchive loadMPQ(String mpq)
	{
//		try {
			MPQArchive temp;
			try {
				temp = new MPQArchive(new File(getWarcraftDirectory()+mpq), flags);
				mpqList.add(temp);
				return temp;
			} catch (MPQFormatException e) {
				ExceptionPopup.display("Warcraft installation archive reading error occurred. Check your MPQs."+mpq, e);
				e.printStackTrace();
			} catch (MPQIsAVIException e) {
				ExceptionPopup.display("Warcraft installation archive reading error occurred. Check your MPQs."+mpq, e);
				e.printStackTrace();
			}//MPQArchive.openArchive(new File(getWarcraftDirectory()+mpq));
			catch (MPQFileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//		} catch (FileNotFoundException e) {
//			ExceptionPopup.display("Could not find/access Warcraft III archive: "+mpq, e);
//			e.printStackTrace();
//		} catch (IOException e) {
//			ExceptionPopup.display("Could not find/access Warcraft III archive: "+mpq, e);
//			e.printStackTrace();
//		}
		return null;
	}
	
	private static MPQHandler current;
	public static MPQHandler get()
	{
		if( current == null )
			current = new MPQHandler();
		return current;
	}

	/**
	 * @return The Warcraft directory used by this test.
	 */
	public static String getWarcraftDirectory()
	{
		//return "C:\\temp\\WC3Archives\\";
		return SaveProfile.getWarcraftDirectory();
	}
}
