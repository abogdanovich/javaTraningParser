/**
 * this script allow to extract all actions from old KB system and
 * save them into class files with specific folder structure
 * log4j 2 is used to log actions into <logs/smp.log>
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

import static java.util.Arrays.asList;

import org.w3c.dom.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author bogdanovich_a
 */
public class Main {
	static final Logger log = LogManager.getLogger(Main.class);
	static final String packageFileName = "preferences.xml";
	static final String xmlFileName = "PCRF_Basic.xml";
	ArrayList<String> actionList = new ArrayList<String>();
	ArrayList<String> paramList = new ArrayList<String>();
	String actionName = "";
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
	 * get document object for XML parsing
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public Document getParserObject(String fileName) throws Exception {
		File xmlFile = new File(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document xmlDocument = dBuilder.parse(xmlFile);

		return xmlDocument;
	}

	/**
	 * generate action file with appropriate data and folder structure
	 * @param actionName
	 * @param params
	 * @throws IOException
	 */
	public void generateActionFile(String actionName, ArrayList<String> params) throws Exception {
		String data = new String();
		String actionNameOriginal = actionName;
		String packagePath = "";
		final String SUFFIX = "_withParser";

		/* The list of Actions from @Rabi -> that should be translated to "_withParser" suffix.
		* Feel free to add a new Actions names into that list
		*/

		List<String> suffixActionRequired = asList("Acstat", "CheckActiveSub", "CheckSubSessions",
				"CheckNoOfConnections", "Acmon");

		// get node package path (eg: folder structure) -> and put packagePath
		Document xmlHierarchyDocument = getParserObject(packageFileName);
		// take the correct path for KB actions from preferences.xml
		getPackageName(xmlHierarchyDocument, actionName);
		// convert </> folder path into package path
		packagePath = folderPath.replace("/", ".");

		// remove the last "." symbol
		if (!packagePath.equals("")) {
			packagePath = packagePath.substring(0, packagePath.length() - 1);
		}

		if (suffixActionRequired.contains(actionName)) {
			actionName = actionName + SUFFIX;
		}

		data =
			"/**\r\n" +
			"* " + actionName + " class with appropriate KB old actions\r\n"  +
			"* @author bogdanovich_a\r\n" +
			"*/\r\n" +
			"package automation.allot.com.Actions.KBsystem.jsystemActions." + packagePath + ";\r\n" +
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

		//log.debug(String.format("PARAMS >>>> %s ", params));

		// put here all action params !
		for (int i=0; i < params.size(); i++) {
			data += String.format("\t private String %s = \"\"; \r\n ", params.get(i));
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
			"\t @TestProperties(paramsInclude = {";

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
		"\t\t automation.allot.com.Actions.KBsystem.QaAutomation." + packagePath + "." + actionName + " " + actionName.toLowerCase() +
				" =  new automation.allot.com.Actions.KBsystem.QaAutomation."
				+ packagePath + "." + actionName +"(\"\", params);\r\n" +
		"\t\t "+actionName.toLowerCase()+".run();\r\n" + 
		"\r\n" + 
		"\t\t if ("+actionName.toLowerCase()+".success() == true) {\r\n" + 
		"\t\t\t report.report(\"KeyBlock Action SUCCESS\", report.PASS);\r\n" +
		"\t\t } else {\r\n" + 
		"\t\t\treport.report(\"KeyBlock Action FAILED\", report.FAIL);\r\n" +
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
		saveClassFile(actionNameOriginal, data);
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
	public void parseKBActions(Node node) throws Exception {
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
					actionName = childNode.getTextContent();
					// check if that action is not in the list
					if (!actionName.equals("")) {
						actionList.add(actionName);
					}
					break;
				case "KeyBlockParam":
					Element element = (Element) childNode;
					for (int k = 0; k < element.getElementsByTagName("paramName").getLength(); k++) {
						// add a new param name
						paramList.add(element.getElementsByTagName("paramName").item(k).getTextContent());

					}
					break;

				case "KeyBlock":
					// generate class file
					if (!paramList.isEmpty() && !actionName.equals("")) {
						log.info("---------");
						log.info(String.format("paramList >>>>> %s", paramList));
						log.info(String.format("actionName >>>>> %s", actionName));
						generateActionFile(actionName, paramList);
						paramList.clear();
						log.info("---------");
					}

			}


			parseKBActions(childNode);
		}
	}

	/**
	 * main method for ActionClassGenerator
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception {
		Main xmlParser = new Main();
		
		log.info("Converter is started");
		log.info("Input XML file is : " + xmlFileName);
		log.info("Get started with parseActions");

		Document xmlActionsDocument = xmlParser.getParserObject(xmlFileName);

		// recursion node review
		xmlParser.parseKBActions(xmlActionsDocument);
	}
}