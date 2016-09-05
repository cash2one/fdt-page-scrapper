package com.fdt.doorgen.generator.xmlparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.fdt.doorgen.generator.MenuItem;

public class CustomParser {

	public CustomParser(){
		super();
	}

	public List<MenuItem> parseMenu(File menuFile, String expression){
		List<MenuItem> menuItems = new ArrayList<MenuItem>(); 

		try {
			FileInputStream file = new FileInputStream(menuFile);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder =  builderFactory.newDocumentBuilder();

			Document xmlDocument = builder.parse(file);

			XPath xPath =  XPathFactory.newInstance().newXPath();

			System.out.println(expression);
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {

				NodeList childNodes = nodeList.item(i).getChildNodes();

				MenuItem menuItem = new MenuItem(); 

				for(int j = 0; j < childNodes.getLength(); j++)
				{
					if(childNodes.item(j).getNodeName().equals("label")){
						menuItem.setLabel(childNodes.item(j).getTextContent());
					}
					else if(childNodes.item(j).getNodeName().equals("href")){
						menuItem.setHref(childNodes.item(j).getTextContent());
					}
					else if(childNodes.item(j).getNodeName().equals("file")){
						menuItem.setContentFile(childNodes.item(j).getTextContent());
					}
					else if(childNodes.item(j).getNodeName().equals("style_class")){
						menuItem.setStyleClass(childNodes.item(j).getTextContent());
					}
					else if(childNodes.item(j).getNodeName().equals("page_title")){
						menuItem.setPageTitle(childNodes.item(j).getTextContent());
					}
					else if(childNodes.item(j).getNodeName().equals("page_meta_description")){
						menuItem.setPageMetaDescription(childNodes.item(j).getTextContent());
					}
					else if(childNodes.item(j).getNodeName().equals("page_meta_keywords")){
						menuItem.setPageMetaKeywords(childNodes.item(j).getTextContent());
					}
					else if(childNodes.item(j).getNodeName().equals("tmpl_page_label")){
						menuItem.setTmplPageLabel(childNodes.item(j).getTextContent());
					}
				}

				menuItems.add(menuItem);
			}
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
		catch (SAXException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (ParserConfigurationException e) {
			e.printStackTrace();
		} 
		catch (XPathExpressionException e) {
			e.printStackTrace();
		}	


		return menuItems;
	}

	public static void main(String[] args){
		try {
			FileInputStream file = new FileInputStream(new File("./instruction/instruction_1_usainstallmentloans.org/main_menu.xml"));

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();

			DocumentBuilder builder =  builderFactory.newDocumentBuilder();

			Document xmlDocument = builder.parse(file);

			XPath xPath =  XPathFactory.newInstance().newXPath();

			System.out.println("*************************");
			String expression = "/menu/header/menu_item";
			System.out.println(expression);
			NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);

			for (int i = 0; i < nodeList.getLength(); i++) {

				NodeList childNodes = nodeList.item(i).getChildNodes();

				MenuItem menuItem = new MenuItem(); 

				for(int j = 0; j < childNodes.getLength(); j++){
					if(childNodes.item(j).getNodeName().equals("title")){
						menuItem.setLabel(childNodes.item(j).getTextContent());
					}else
						if(childNodes.item(j).getNodeName().equals("href")){
							menuItem.setHref(childNodes.item(j).getTextContent());
						}else
							if(childNodes.item(j).getNodeName().equals("file")){
								menuItem.setContentFile(childNodes.item(j).getTextContent());
							}else
								if(childNodes.item(j).getNodeName().equals("style_class")){
									menuItem.setStyleClass(childNodes.item(j).getTextContent());
								}
				}

				System.out.println(nodeList.item(i).getChildNodes().item(4).getTextContent()); 

				String nodeValue = nodeList.item(i).getChildNodes().item(0).getNodeName().toString();
				System.out.println(nodeValue); 
			}

			/*System.out.println("*************************");
			expression = "/Employees/Employee[@type='admin']/firstname";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
			    System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
			}

			System.out.println("*************************");
			expression = "/Employees/Employee[@emplid='2222']";
			System.out.println(expression);
			Node node = (Node) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODE);
			if(null != node) {
				nodeList = node.getChildNodes();
				for (int i = 0;null!=nodeList && i < nodeList.getLength(); i++) {
					Node nod = nodeList.item(i);
					if(nod.getNodeType() == Node.ELEMENT_NODE)
						System.out.println(nodeList.item(i).getNodeName() + " : " + nod.getFirstChild().getNodeValue()); 
				}
			}

			System.out.println("*************************");

			expression = "/Employees/Employee[age>40]/firstname";
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			System.out.println(expression);
			for (int i = 0; i < nodeList.getLength(); i++) {
			    System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
			}

			System.out.println("*************************");
			expression = "/Employees/Employee[1]/firstname";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
			    System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
			}
			System.out.println("*************************");
			expression = "/Employees/Employee[position() <= 2]/firstname";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
			    System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
			}

			System.out.println("*************************");
			expression = "/Employees/Employee[last()]/firstname";
			System.out.println(expression);
			nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); i++) {
			    System.out.println(nodeList.item(i).getFirstChild().getNodeValue()); 
			}*/

			System.out.println("*************************");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}		
	}
}
