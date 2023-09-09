package com.hiveworkshop.rms.script;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;

import javax.sql.RowSetInternal;
import javax.sql.rowset.WebRowSet;
import javax.sql.rowset.spi.XmlReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hiveworkshop.rms.filesystem.GameDataFileSystem;

public class XmlTest {
	public static void main(String[] args) {
		DocumentBuilderFactory newInstance = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder docBuilder = newInstance.newDocumentBuilder();
			Document root = docBuilder
					.parse(GameDataFileSystem.getDefault().getResourceAsStream("Interface\\FrameXML\\MainMenuBar.xml"));
			NodeList elementsByTagName = root.getElementsByTagName("TexCoords");
			for (int i = 0; i < elementsByTagName.getLength(); i++) {
				Node item = elementsByTagName.item(i);
				System.out.println(item + "," + item.getAttributes().getNamedItem("left") + ","
						+ item.getAttributes().getNamedItem("right") + "," + item.getAttributes().getNamedItem("top")
						+ "," + item.getAttributes().getNamedItem("bottom"));
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
