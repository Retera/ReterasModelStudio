package com.matrixeater.src;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import com.JStormLib.MPQArchive;
import com.JStormLib.MPQArchiveException;
import com.matrixeater.data.SaveProfile;


public class MPQHandler {
	MPQArchive war3;
	MPQArchive war3x;
	MPQArchive war3xlocal;
	MPQArchive war3patch;
	ArrayList<MPQArchive> mpqList = new ArrayList<MPQArchive>();
	
	public MPQHandler()
	{
		war3 = loadMPQ("war3.mpq");
		war3x = loadMPQ("war3x.mpq");
		war3xlocal = loadMPQ("war3xlocal.mpq");
		war3patch = loadMPQ("war3patch.mpq");
	}
	
	public File getGameFile(String filepath)
	{
		for( int i = mpqList.size()-1; i >= 0; i-- )
		{
			MPQArchive mpq = mpqList.get(i);
			try {
				if( mpq.containsFile(filepath) )
				{
					String tempDir = System.getProperty("java.io.tmpdir")+"MatrixEaterExtract\\";
					File tempProduct = new File(tempDir+filepath);
					tempProduct.getParentFile().mkdirs();
					mpq.extractFile(filepath, tempProduct);
					tempProduct.deleteOnExit();
					return tempProduct;
				}
			} catch (MPQArchiveException e) {
				ExceptionPopup.display("MPQ reader exception from JStormLib:", e);
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public void refresh()
	{
		try {
			war3.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MPQArchiveException e) {
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		try {
			war3x.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MPQArchiveException e) {
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		try {
			war3xlocal.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MPQArchiveException e) {
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		try {
			war3patch.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MPQArchiveException e) {
			e.printStackTrace();
		} catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		mpqList.clear();
		war3 = loadMPQ("war3.mpq");
		war3x = loadMPQ("war3x.mpq");
		war3xlocal = loadMPQ("war3xlocal.mpq");
		war3patch = loadMPQ("war3patch.mpq");
	}
	
	private MPQArchive loadMPQ(String mpq)
	{
		try {
			MPQArchive temp = MPQArchive.openArchive(new File(getWarcraftDirectory()+mpq));
			mpqList.add(temp);
			return temp;
		} catch (FileNotFoundException e) {
			ExceptionPopup.display("Could not find/access Warcraft III archive: "+mpq, e);
			e.printStackTrace();
		} catch (IOException e) {
			ExceptionPopup.display("Could not find/access Warcraft III archive: "+mpq, e);
			e.printStackTrace();
		}
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
