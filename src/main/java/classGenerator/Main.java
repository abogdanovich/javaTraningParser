/**
 * this script allow to extract all actions from old KB system and
 * save them into class files with specific folder structure
 * log4j 2 is used to log actions into <logs/smp.log>
 * @author bogdanovich_a
 */
package classGenerator;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import static java.util.Arrays.asList;

import org.apache.log4j.Logger;
import org.w3c.dom.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author bogdanovich_a
 */
public class Main {
	private static final Logger log = Logger.getLogger(Main.class);
	static final String workFlowFileName = "PCRF_Basic.xml";
	static final String workflowPath = "PCRF_Basic";
	static final String packageFileName = "preferences.xml";
	static final String rootXMLFolder = "out_xml_workflow";
	static final String rootClassFolder = "out_class_files";
	static final String xmlKBFiles = "d:\\allot\\oldKB\\trunk\\SW\\bin\\res\\xml\\";
	static final String xmlKBActions = "smp_actions.txt";
	ArrayList<String> actionList = new ArrayList<String>();
	ArrayList<String> paramList = new ArrayList<String>();

	HashMap<String, String> paramListWithValues = new HashMap<String, String>();

	ArrayList<ArrayList<String>> uuidWithActions = new ArrayList<ArrayList<String>>();

	ArrayList<ArrayList<String>> testCaseStepsList = new ArrayList<ArrayList<String>>();
	UUID uuidForAction = UUID.randomUUID();
	UUID uuidForScenario = UUID.randomUUID();

	String actionName = "";
	String testCase = "";
	String testStep = "";

	String folderPath = "";
	String packagePath = "";
	static final String XML_FOOTER = "</project>";
	static final String XML_HEADER =
					"\t<property name=\"test.parameters.file.name\" value=\".testPropertiesFile_Empty\"/>\r\n" +
					"\t<property name=\"scenario.version\" value=\"JSystem5_1\"/>\r\n" +
					"\t<property name=\"jsystem.uuid\" value=\"\"/>\r\n" +
					"\t<property name=\"jsystem.parent.uuid\" value=\"\"/>\r\n" +
					"\t<property name=\"jsystem.parent.name\" value=\"\"/>\r\n" +
					"\t<loadproperties srcFile=\"${test.parameters.file.name}\"/>\r\n" +
					"\t<taskdef classname=\"com.aqua.anttask.jsystem.JSystemTask\" name=\"jsystem\"/>\r\n" +
					"\t<taskdef classname=\"com.aqua.anttask.jsystem.JSystemAntTask\" name=\"jsystem-ant\"/>\r\n" +
					"\t<taskdef resource=\"net/sf/antcontrib/antlib.xml\"/>\r\n" +
					"\t<typedef classname=\"com.aqua.anttask.jsystem.JSystemScriptCondition\" name=\"jsystemscriptcondition\"/>\r\n" +
					"\t<taskdef classname=\"com.aqua.anttask.jsystem.JSystemSwitch\" name=\"jsystemswitch\"/>\r\n" +
					"\t<taskdef classname=\"com.aqua.anttask.jsystem.JSystemForTask\" name=\"jsystemfor\"/>\r\n" +
					"\t<taskdef classname=\"com.aqua.anttask.jsystem.JSystemDataDrivenTask\" name=\"jsystemdatadriven\"/>\r\n" +
					"\t<taskdef classname=\"com.aqua.anttask.jsystem.JSystemSetAntProperties\" name=\"jsystemsetantproperties\"/>\r\n" +
					"\t<target name=\"execute scenario\">\n";

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
							break;
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
		String testMethodName = "";
		final String SUFFIX = "_withParser";

		/* The list of Actions from @Rabi -> that should be translated to "_withParser" suffix.
		* Feel free to add a new Actions names into that list
		*/

		List<String> suffixActionRequired = asList("Acstat", "CheckActiveSubs", "CheckSubSessions",
				"CheckNoOfConnections", "Acmon");

		if (suffixActionRequired.contains(actionName)) {
			testMethodName = actionName + SUFFIX;
		} else {
			testMethodName = actionName;
		}

		// get node package path (eg: folder structure) -> and put packagePath
		Document xmlHierarchyDocument = getParserObject(packageFileName);
		// take the correct path for KB actions from preferences.xml
		getPackageName(xmlHierarchyDocument, actionName);
		// convert </> folder path into package path

		// remove the last "." symbol
		if (!folderPath.equals("")) {
			packagePath = folderPath.replace("/", ".");
			packagePath = packagePath.substring(0, packagePath.length() - 1);
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
			"\t public void " + actionName + "_KB() {\r\n" +
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
		"\t\t automation.allot.com.Actions.KBsystem.QaAutomation." + packagePath + "." + testMethodName + " " + actionName.toLowerCase() +
				" =  new automation.allot.com.Actions.KBsystem.QaAutomation."
				+ packagePath + "." + testMethodName +"(\"\", params);\r\n" +
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
		saveFile(rootClassFolder + folderPath, actionNameOriginal + ".java", data);
	}

	public void checkDirectoryExists(String path) throws IOException {
		Path dirPath = Paths.get(path);
		boolean dirExists;

		dirExists = Files.exists(dirPath);
		if (!dirExists) {
			Files.createDirectories(dirPath);
			log.debug(String.format("Folder: %s was created successfully", folderPath));
		}
	}

	/**
	 * save data into Class file 
	 * @param fileName
	 * @param data
	 * @throws IOException
	 */
	public void saveFile(String Folder,  String fileName, String data) throws IOException {
		try {
			boolean checkFileExists;
			// create a directory if not exists
			checkDirectoryExists(Folder);
			// check if that file is already exists
			checkFileExists = checkFileExists(Folder+fileName);

			// next is to write file into this directory
			if (checkFileExists) {
				fileName = "_" + fileName;
			}
			FileWriter fw = new FileWriter(Folder + "\\" + fileName);
			BufferedWriter WriteFileBuffer = new BufferedWriter(fw);
			WriteFileBuffer.write(data);
			WriteFileBuffer.close();
		} finally {
			// make cleanup global variables
			folderPath = "";
			packagePath = "";
			log.info(String.format("File %s saved successfully", fileName));
		}
	}

	public boolean checkFileExists(String fileName) {
		Path file = Paths.get(fileName);
		if (Files.exists(file)) {
			return true;
		}
		return false;
	}

	/**
	 * xml recursion parser
	 * @param node
	 * @throws Exception
	 */
	public void parseKBActions(Node node) throws Exception {
		NodeList list = node.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			String nodeText = childNode.getTextContent();
			String nodeName = childNode.getNodeName();

			switch (nodeName) {
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
				case "keyBlockRepeatCount":
					// generate class file
					if (!paramList.isEmpty() && !actionName.equals("")) {
						generateActionFile(actionName, paramList);
						paramList.clear();
					}
					break;
			}
			parseKBActions(childNode);
		}
	}

	/**
	 * generate XML workflow from KB xml file
	 * @param node
	 * @throws Exception
	 */
	public void parseKBWorkflow(Node node) throws Exception {
		NodeList list = node.getChildNodes();
		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			String nodeText = childNode.getTextContent();
			String nodeName = childNode.getNodeName();

			switch (nodeName) {
				// TEST CASE
				case "scenarioName":
					// JSYSTEM SCENARIO
//					log.debug("test case >> [" + childNode.getTextContent() + "]");
					testCase = childNode.getTextContent();
					testCase = testCase.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

					break;

				// STEP NAME
				case "keyBlockGroupName":
					// JSYSTEM xml workflow
//					log.debug("step >> " + childNode.getTextContent());


					testStep = childNode.getTextContent();
					testStep = testStep.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

					uuidForScenario = UUID.randomUUID();
					ArrayList<String> uuidStepName = new ArrayList<String>();

					uuidStepName.add(uuidForScenario.toString());
					uuidStepName.add(testStep);
					// save step name
					testCaseStepsList.add(uuidStepName);

					break;

				// ACTION NAME
				case "keyBlockName":

//					log.debug("step action >> " + childNode.getTextContent());
					actionName = childNode.getTextContent();
					uuidForAction = UUID.randomUUID();
					ArrayList<String> uuidRecord = new ArrayList<String>();

					// save actionName + uuid
					uuidRecord.add(uuidForAction.toString());
					uuidRecord.add(actionName);
					uuidWithActions.add(uuidRecord);

					break;
				// params with values
				case "KeyBlockParam":
					Element element = (Element) childNode;
					for (int k = 0; k < element.getElementsByTagName("paramName").getLength(); k++) {
						// add a new param name
						paramListWithValues.put(element.getElementsByTagName("paramName").item(k).getTextContent(),
								element.getElementsByTagName("paramValue").item(k).getTextContent());
					}

					savePropertiesFile(testCase + "\\", testStep + ".properties", uuidForAction, paramListWithValues);
					paramListWithValues.clear();

					break;

				// we need to look into the last but not least node name to do not miss the last action step
				case "runState":

					log.debug("PARENT >>> " + childNode.getParentNode().getNodeName());

					if (childNode.getParentNode().getNodeName().equals("KeyBlockGroup")) {
//						log.debug("uuidWithActions > " + uuidWithActions);
						generateJSystemScenario(testCase + "\\", testStep, uuidWithActions, true);
						uuidWithActions.clear();
					}

					if (childNode.getParentNode().getNodeName().equals("KeyBlockScenario")) {
//						// new scenario - so we can save our prepared xml file
//						log.debug("testCaseStepsList > " + testCaseStepsList);
						generateJSystemScenario(testCase + "\\", testCase, testCaseStepsList, false);
						testCaseStepsList.clear();
					}

					break;
			}
			parseKBWorkflow(childNode);
		}
	}

	public void generateJSystemScenario(String filePath, String fileName, ArrayList<ArrayList<String>> actionsWithUUID, boolean scenarioWithActions) throws Exception {
		// xml file header
		log.debug(">>>>>>>>>>>>>>>>>>>> " + workflowPath + " / " + fileName);
		String data =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--This file was auto-generated by Aliaksandr Bahdanovich auto-gen script for JSystem runner, " +
						"do not change it manually--><project default=\"execute scenario\" name=\"scenarios/SMP/Quality_Gates/Gate4/" + workflowPath + "/" + testCase + "/" + fileName + "\">\r\n" +
						XML_HEADER;

		filePath = rootXMLFolder + "\\" + filePath;
		log.debug(">>>" + actionsWithUUID);

		// get node package path (eg: folder structure) -> and put packagePath
		Document xmlHierarchyDocument = getParserObject(packageFileName);
		// take the correct path for KB actions from preferences.xml
		getPackageName(xmlHierarchyDocument, actionName);
		// convert </> folder path into package path

		// remove the last "." symbol
		if (!folderPath.equals("")) {
			packagePath = folderPath.replace("/", ".");
			packagePath = packagePath.substring(0, packagePath.length() - 1);
		}

		// steps ordering
		for (int i = 0; i < actionsWithUUID.size(); i++) {
			data += String.format("\t\t<antcallback target=\"t%s\"/>\r\n", i);
		}

		data += "\t</target>\r\n";

		/*
		 * step description:
		 * actionsWithUUID.get(i).get(0) = UUID
		 * actionsWithUUID.get(i).get(1) = Action name or <Step name> from old KB system
		 */
		if (scenarioWithActions) {
			// example automation.allot.com.Actions.KBsystem.jsystemActions.NX.Catalogs.ChargingApplication.AddChargingApplication_NX.AddChargingApplication_NX_KB
			// we generate XML file with references JSystem Actions
			for (int i = 0; i < actionsWithUUID.size(); i++) {
				data += String.format(
						"\t<target name=\"t%s\">\r\n" +
								"\t\t<jsystem showoutput=\"true\">\r\n" +
								"\t\t\t<sysproperty key=\"jsystem.uuid\" value=\"%s\"/>\r\n" +
								"\t\t\t<sysproperty key=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\r\n" +
								"\t\t\t<sysproperty key=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\r\n" +
								"\t\t\t<test name=\"automation.allot.com.Actions.KBsystem.jsystemActions." + packagePath + ".%s.%s\"/>\r\n" +
								"\t\t</jsystem>\r\n" +
								"\t</target>\r\n", i, actionsWithUUID.get(i).get(0), actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(1) + "_KB");
			}
		} else {
			// we generate XML file with references to XML files with JSystem Actions
			for (int i = 0; i < actionsWithUUID.size(); i++) {
				data += String.format(
						"\t<target name=\"t%s\">\n" +
								"\t\t<jsystem-ant antfile=\"${scenarios.base}/scenarios/SMP/Quality_Gates/Gate4/" + workflowPath + "/" + testCase + "/%s.xml\">\n" +
								"\t\t\t<property name=\"jsystem.uuid\" value=\"%s\"/>\n" +
								"\t\t\t<property name=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\n" +
								"\t\t\t<property name=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\n" +
								"\t\t</jsystem-ant>\n" +
								"\t</target>", i, actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(0));
			}
		}

		// xml file last line
		data += XML_FOOTER;

		// save xml header and write data into file. filename = step name
		saveFile(filePath, fileName + ".xml", data);

	}

	/**
	 * Append properties files with uuid:param={value}
	 * @param filePath
	 * @param fileName
	 * @param uuid
	 * @param paramListWithValues
	 * @throws IOException
	 */
	public void savePropertiesFile(String filePath, String fileName, UUID uuid, HashMap<String, String> paramListWithValues) throws IOException {
		try {
			filePath = rootXMLFolder + "\\" + filePath;
			checkDirectoryExists(filePath);

			BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName, true));
			// iterate hashmap and write lines like:
			// uuid.paramName=param_value
			for (Map.Entry<String, String> entry : paramListWithValues.entrySet()) {
				String paramName = entry.getKey();
				String paramValue = entry.getValue();

				// rspecial rules should be applied for param values
				paramValue = paramValue.replace("$${SMP1.Host}", "${sut:R_SMP1/connectDetails/iP}");
				paramValue = paramValue.replace("$${NE1.Host}", "${sut:R_NE1/connectDetails/iP}");
				paramValue = paramValue.replace("$${", "${run:");

				String data = String.format("%s.%s=%s", uuid, paramName, paramValue);
				WriteFileBuffer.write(data);
				WriteFileBuffer.write(System.lineSeparator());
			}
			// write specific configuration line
			WriteFileBuffer.write(String.format("%s.jsystem.uisettings=sortSection\\:0;sortHeader\\:0;paramsOrder\\:defaultOrder;activeTab\\:0;headersRatio\\:0.1,0.25,0.05,0.2\n", uuid));

			WriteFileBuffer.close();
		} finally {
			log.info(String.format("File [%s] is updated", fileName));
		}
	}

	/**
	 * main method for ActionClassGenerator
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception, DirectoryIteratorException {
		Main xmlParser = new Main();
		boolean buildClass = false;
		
		log.info("Script converter is started");

		if (buildClass) {
			log.info("--------------------------");
			log.info("CLASS GENERATOR is started");
			//get the list of xml files and pass one by one into cycle
			try (BufferedReader br = new BufferedReader(new FileReader(xmlKBActions))) {
				String line;
				while ((line = br.readLine()) != null) {
					log.info("Input XML file is : " + line);
					Document xmlActionsDocument = xmlParser.getParserObject(xmlKBFiles+line+".xml");
					// recursion node review
					xmlParser.parseKBActions(xmlActionsDocument);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// please uncomment this section to call class or workflow generator
		log.info("--------------------------");
		log.info("XML WORKFLOW GENERATOR is started");
		Document xmlWorkFlowDocument = xmlParser.getParserObject(workFlowFileName);
		xmlParser.parseKBWorkflow(xmlWorkFlowDocument);
		log.info("Well done!");
	}
}