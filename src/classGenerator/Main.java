/**
 * this script allow to extract 
 * all actions from old KB system and
 * save them into class files
 * @author bogdanovich_a
 */
package classGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import static java.util.Arrays.asList;

import org.w3c.dom.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author bogdanovich_a
 *
 */
public class Main {
	static final Logger log = LogManager.getLogger(Main.class);
	static final String packageFileName = "preferences.xml";
	static final String xmlFileName = "PCRF_Basic.xml";

	String folderPath = "";

	/**
	 * Method that takes package path from <preferences.xml> KB file
	 * @param node
	 * @param actionName
	 * @throws Exception
	 */
	public void getPackageName(Node node, String actionName) throws Exception {

		NodeList list = node.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			String nodeText = childNode.getTextContent();
			String nodeName = childNode.getNodeName();
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {

				switch (nodeName) {
					case "class":
						if (nodeText.equals(actionName)) {
							// FIXME: make algorithm more reliable without hardcode for xml levels
							if (!childNode.getParentNode().getParentNode().getParentNode().getNodeName().equals("layout")) {
								folderPath += childNode.getParentNode().getParentNode().getParentNode().getNodeName() + "/";
							}

							if (!childNode.getParentNode().getParentNode().getNodeName().equals("layout")) {
								folderPath += childNode.getParentNode().getParentNode().getNodeName() + "/";
							}

							if (!childNode.getParentNode().getNodeName().equals("layout")) {
								folderPath += childNode.getParentNode().getNodeName() + "/";
							}
							folderPath = folderPath.replace("#document/", "");
						}
				}
				getPackageName(childNode, actionName);
			}
		}
	}

	/**
	 * generate action file with appropriate data
	 * @param actionName
	 * @param params
	 * @throws IOException
	 */
	public void generateActionFile(String actionName, ArrayList<String> params) throws Exception {
		String data = new String();
		String packagePath = "";
		final String SUFFIX = "_withParser";

		// the list of Actions from @Rabi -> that should be translated to "_withParser" suffix.
		List<String> suffixActionRequired = asList("Acstat", "CheckActiveSub", "CheckSubSessions",
				"CheckNoOfConnections", "Acmon");
		if (suffixActionRequired.contains(actionName)) {
			actionName = actionName + SUFFIX;
		}

		// get node package path (eg: folder structure) -> and put packagePath
		File xmlFile = new File(packageFileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);

		// take the correct path for KB actions from preferences.xml
		getPackageName(doc, actionName);

		// convert </> folder path into package path
		packagePath = folderPath.replace("/", ".");

		// remove the last "." symbol
		packagePath = packagePath.substring(0, packagePath.length() - 1);

		data =
			"/**\r\n" +
			"* " + actionName + " class with appropriate KB old actions\r\n"  +
			"* @author bogdanovich_a\r\n" +
			"*/\r\n" +
			"package automation.allot.com.Actions.KBsystem.jsystemActions." + packagePath + ";\r\n" +
			"\r\n" +
			"import static org.junit.Assert.*;\r\n" +
			"\r\n" +
			"import java.util.ArrayList;\r\n" +
			"import java.util.List;\r\n" +
			"\r\n" +
			"import org.junit.After;\r\n" +
			"import org.junit.Before;\r\n" +
			"import org.junit.Test;\r\n" +
			"\r\n" +
			"import automation.allot.com.Actions.KBsystem.QaAutomation.internal.utils.KBParam;\r\n" +
			"import jsystem.framework.TestProperties;\r\n" +
			"import jsystem.framework.report.Reporter;\r\n" +
			"import junit.framework.SystemTestCase4;\r\n" +
			"\r\n" +
			"public class " + actionName + " extends SystemTestCase4 {\r\n" +
			"\r\n" +
			"\t // class variables for the given Action\r\n";

		// put here all action params !
		for (int i=0; i < params.size(); i++) {
			data += String.format("\t private String %s = \"\"; \r\n ", params.get(i).toLowerCase());
		};

		data +=
			"\r\n" +
			"\t /**\r\n" +
			"\t * @throws java.lang.Exception\r\n" +
			"\t */\r\n" +
			"\t @Before\r\n" +
			"\t public void setUp() throws Exception {\r\n" +
			"\t }\r\n" +
			"\r\n" +
			"\t /**\r\n" +
			"\t * @throws java.lang.Exception\r\n" +
			"\t */\r\n" +
			"\t @After\r\n" +
			"\t public void tearDown() throws Exception {\r\n" +
			"\t }\r\n" +
			"\r\n" +
			"\t /**\r\n" +
			"\t * KB System action: "+actionName+" \r\n" +
			"\t */\r\n" +
			"\t @TestProperties(ParamsInclude = {";

		// put here all action params !
		for (int i=0; i < params.size(); i++) {
			if (i == params.size() - 1) {
				// pass comma into params list except the last element
				data += String.format("\"%s\"", params.get(i));
			} else {
				data += String.format("\"%s\", ", params.get(i));
			}
		}
		
		data += 
			"})\r\n" +
			"\t @Test\r\n" +
			"\t public void "+actionName+"_KB() {\r\n" +
			"\r\n" +
			"\t\t // prepare parameters\r\n" +
			"\t\t List<KBParam> params = new ArrayList<>();\r\n";

		// put here all action params !
		for (int i=0; i < params.size(); i++) {
			data += String.format("\t\t params.add(new KBParam(\"%s\", %s, \"text\", \"\", false, false)); \r\n",
					params.get(i), params.get(i));
		};
		
		data += 
		"\r\n" + 
		"\t\t // execute KB action\r\n" +
		"\t\t automation.allot.com.Actions.KBsystem.QaAutomation." + packagePath + actionName + " " + actionName.toLowerCase() +
				" =  new automation.allot.com.Actions.KBsystem.QaAutomation."
				+ packagePath + actionName +"(\"\", params);\r\n" +
		"\t\t "+actionName.toLowerCase()+".run();\r\n" + 
		"\r\n" + 
		"\t\t if ("+actionName.toLowerCase()+".success() == true) {\r\n" + 
		"\t\t\t report.report(\"SUCCESS\", report.PASS);\r\n" + 
		"\t\t } else {\r\n" + 
		"\t\t\treport.report(\"FAILES\", report.FAIL);\r\n" + 
		"\t\t }\r\n" + 
		"\t } \r\n" + 
		"\r\n" + 
		"\t // actions getter\r\n";

		// generate all params getters
		for (int i=0; i < params.size(); i++) {
			data += String.format(
					"\r\n" + 
					"\t /**\r\n" + 
					"\t * \r\n" + 
					"\t * @return %s \r\n" + 
					"\t */\r\n" + 
					"\t public String get%s() {\r\n" +
					"\t\t return this.%s;\r\n" + 
					"\t } \r\n"
					, params.get(i), params.get(i), params.get(i));
		}

		data += "\t // actions setter\r\n";
		
		// generate all params setters
		for (int i=0; i < params.size(); i++) {
			data += String.format(
					"\r\n" + 
					"\t /**\r\n" + 
					"\t * \r\n" + 
					"\t * @param %s the %s set\r\n" + 
					"\t */\r\n" + 
					"\t public void set%s(String %s) {\r\n" +
					"\t\t this.%s = %s;\r\n" + 
					"\t } \r\n"
					, params.get(i), params.get(i), params.get(i),
					params.get(i), params.get(i), params.get(i));
		}

		data += "\r\n }";
		
		//write data into file
		saveClassFile(actionName, data);
	}

	/**
	 * save data into Class file 
	 * @param fileName
	 * @param data
	 * @throws IOException
	 */
	public void saveClassFile(String fileName, String data) throws IOException {
		try {

			// save into folder packagePath NIO
			folderPath = "output\\" + folderPath;
			Path dirPath = Paths.get(folderPath);
			boolean dirExists = Files.exists(dirPath);
			if (!dirExists) {
				Files.createDirectories(dirPath);
				log.debug(String.format("Folder: %s was created successfully", folderPath));
			}

			// next is to write file into this directory
			FileWriter fw = new FileWriter(folderPath + "\\" + fileName + ".java");
			BufferedWriter WriteFileBuffer = new BufferedWriter(fw);
			WriteFileBuffer.write(data);
			WriteFileBuffer.close();

			
		} finally {
			folderPath = "";
			log.info(String.format("Class %s file saved", fileName));
		}
	}

	/**
	 * xml recursion parser
	 * @param node
	 * @param actionList
	 * @param paramList
	 * @throws Exception
	 */
	public void parseKBActions(Node node, ArrayList<String> actionList, ArrayList<String> paramList) throws Exception {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			String nodeText = childNode.getTextContent();
			String nodeName = childNode.getNodeName();

			switch (nodeName) {
				case "scenarioName":
					log.info(String.format("Test Case: [%s]", nodeText));
					break;
				case "keyBlockGroupName":
					log.info(String.format("Test Case STEP: [%s]", nodeText));
					if (!actionList.isEmpty()) {
						log.info(String.format("actionList = %s", actionList));
						actionList.clear();
					}
					break;
				case "keyBlockName":
					// cleanup params list
					log.info(String.format("STEP ACTION: [%s]", nodeText));

					// check if that action is not in the list
					if (!nodeText.equals("")) {
						actionList.add(nodeText);
						generateActionFile(nodeText, paramList);
						// just to be sure that params are not copied
						paramList.clear();
					}
					break;
				case "keyBlockParams":
					Element element = (Element) childNode;

					for (int k = 0; k < element.getElementsByTagName("paramName").getLength(); k++) {
						// add a new param name
						paramList.add(element.getElementsByTagName("paramName").item(k).getTextContent());
					}
					if (!paramList.isEmpty()) {
						log.info(String.format("paramList = %s", paramList));
					}
					break;
			}
			parseKBActions(childNode, actionList, paramList);
		}
	}

	/**
	 * main method for ActionClassGenerator
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {

		ArrayList<String> actionList = new ArrayList<String>();
		ArrayList<String> paramList = new ArrayList<String>();

		Main xmlParser = new Main();
		
		log.info("Converter is started");
		log.info("Input XML file is : " + xmlFileName);
		log.info("Get started with parseActions");

		File xmlFile = new File(xmlFileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(xmlFile);

		// recursion node review
		xmlParser.parseKBActions(doc, actionList, paramList);
	}
}