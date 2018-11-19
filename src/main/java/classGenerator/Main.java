/**
 * This script allows to convert class files from KB into jsystem
 * and generate workflow from old KB automation system
 * @author Alex Bogdanovich
 * @author Vadim Chiritsa
 */
package classGenerator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import static java.util.Arrays.asList;

public class Main {
	// FIXME: optimize class global variables
	private static final Logger log = Logger.getLogger(Main.class);

	static final String workFlowFileName = "PCRF_Basic.xml";
	static final String workflowPath = "PCRF_Basic";
	static final String packageFileName = "preferences.xml";
	static final String rootXMLFolder = workflowPath;

	static final String rootClassFolder = workflowPath + "_class";
	static final String xmlKBFiles = "d:\\allot\\oldKB\\trunk\\SW\\bin\\res\\xml\\";
	static final String xmlKBActions = "smp_actions.txt";

	static final String smpTestPath = "scenarios/SMP/Quality_Gates/Gate4/SMPTests/";

	ArrayList<String> actionList = new ArrayList<>();
	ArrayList<String> paramList = new ArrayList<>();

	private final HashMap<String, String> pathsToActionsMap = new HashMap<>();
	private final HashMap<String, String> paramListWithValues = new HashMap<>();

	ArrayList<ArrayList<String>> uuidWithActions = new ArrayList<>();
	ArrayList<ArrayList<String>> testCaseStepsList = new ArrayList<>();
	ArrayList<ArrayList<String>> fatherXmlOfTestCases = new ArrayList<>();
	ArrayList<String> loopData = new ArrayList<>();

	UUID uuidForAction = UUID.randomUUID();
	UUID uuidForFatherXML = UUID.randomUUID();

	String actionName = "";
	String actionMeaningful = "";
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
	 * @throws Exception
	 */
	private void getPackageName(Node node) {
		NodeList list = node.getChildNodes();

		for (int i = 0; i < list.getLength(); i++) {
			Node childNode = list.item(i);
			String nodeText = childNode.getTextContent();
			String nodeName = childNode.getNodeName();
			if (childNode.getNodeType() == Node.ELEMENT_NODE) {

				switch (nodeName) {
					case "class":
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

				pathsToActionsMap.put(nodeText, folderPath);
				folderPath = "";
				getPackageName(childNode);
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
		Path xmlFile = Paths.get(fileName);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document xmlDocument = dBuilder.parse(xmlFile.toString());

		return xmlDocument;
	}

	public boolean checkFileExists(String fileName) {
		Path file = Paths.get(fileName);
		if (Files.exists(file)) {
			return true;
		}
		return false;
	}

	public void checkDirectoryExists(String path) throws IOException {
		Path dirPath = Paths.get(path);
		boolean dirExists;
		dirExists = Files.exists(dirPath);
		if (!dirExists) {
			Files.createDirectories(dirPath);
			log.info(String.format("Folder: %s was created successfully", folderPath));
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

			if (actionMeaningful.equals("default")) {
				actionMeaningful = actionName;
			}

			BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName, true));
			// iterate hashmap and write lines like: uuid.paramName=param_value
			for (Map.Entry<String, String> entry : paramListWithValues.entrySet()) {
				String paramName = entry.getKey();
				String paramValue = entry.getValue();

				// rspecial rules should be applied for param values
				paramValue = paramValue.replace("$${SMP1.Host}", "${sut:R_SMP1/connectDetails/iP}");
				paramValue = paramValue.replace("$${NE1.Host}", "${sut:R_NE1/connectDetails/iP}");
				paramValue = paramValue.replace("$${", "${run:");
				if (paramValue.contains("..\\..\\tests\\SMP\\")) {
					// all specific test data files should be under test_suite/data folder
					paramValue = paramValue.replace("..\\..\\tests\\SMP\\", "C:/JAutomationPackage/Actions/target/classes/" + smpTestPath + "data/");
					if (paramValue.contains("/")) {
						paramValue = paramValue.replace("\\", "/");
					}
				}

				String data = String.format("%s.%s=%s", uuid, paramName, paramValue);
				WriteFileBuffer.write(data);
				WriteFileBuffer.write(System.lineSeparator());
			}
			// write specific configuration line
			WriteFileBuffer.write(String.format("%s.jsystem.uisettings=sortSection\\:0;sortHeader\\:0;paramsOrder\\:defaultOrder;activeTab\\:0;headersRatio\\:0.1,0.25,0.05,0.2\n", uuid));
			WriteFileBuffer.write(String.format("%s.meaningfulName=%s\n", uuid, actionMeaningful));

			WriteFileBuffer.close();
		} finally {

			log.info(String.format("File [%s] is updated", fileName));
		}
	}

	/**
	 * save properties for checkboxes
	 * @param filePath
	 * @param fileName
	 * @param uuid
	 * @param isDisabled
	 * @throws IOException
	 */
	public void savePropertiesFile(String filePath, String fileName, UUID uuid, String isDisabled) throws IOException {
		try {
			filePath = rootXMLFolder + "\\" + filePath;
			checkDirectoryExists(filePath);

			BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName, true));
			Boolean checkBox = !Boolean.valueOf(isDisabled);
			// write specific configuration line
			WriteFileBuffer.write(String.format("%s.jsystem.isdisabled=%s\n", uuid, checkBox.toString()));
			WriteFileBuffer.close();
		} finally {
			log.info(String.format("File [%s] is updated", fileName));
		}
	}

	/**
	 * update properties files with appropriate data for specific uuid
	 * @param filePath
	 * @param fileName
	 * @param uuid
	 * @throws IOException
	 */
	public void savePropertiesFile(String filePath, String fileName, UUID uuid) throws IOException {
		try {
			filePath = rootXMLFolder + "\\" + filePath;
			checkDirectoryExists(filePath);

			BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName, true));
			// write specific configuration line

			if (!loopData.isEmpty()) {
				WriteFileBuffer.write(String.format("%s.list=%s\n", uuid, loopData.get(1)));
				WriteFileBuffer.write(String.format("%s.loop\\ value=%s\n", uuid, loopData.get(0)));
				WriteFileBuffer.write(String.format("%s.jsystem.uisettings=sortSection\\:0;sortHeader\\:0;paramsOrder\\:defaultOrder;activeTab\\:0;headersRatio\\:0.1,0.25,0.05,0.2\n", uuid));
			}

			WriteFileBuffer.close();
		} finally {
			log.info(String.format("Properties file [%s] is updated", fileName));
		}
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

		// The list of Actions from @Rabi -> that should be translated to "_withParser" suffix.
		List<String> suffixActionRequired = asList("Acstat", "CheckActiveSubs", "CheckSubSessions",
				"CheckNoOfConnections", "Acmon");

		if (suffixActionRequired.contains(actionName)) {
			testMethodName = actionName + SUFFIX;
		} else {
			testMethodName = actionName;
		}

		//get folder path path for action
		folderPath = pathsToActionsMap.get(actionName);

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
					// jsystem test case
					testCase = childNode.getTextContent();
					testCase = testCase.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
					break;

				// STEP NAME
				case "keyBlockGroupName":
					// jsystem test step
					testStep = childNode.getTextContent();
					testStep = testStep.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
					ArrayList<String> uuidStepName = new ArrayList<String>();

					uuidStepName.add(UUID.randomUUID().toString());
					uuidStepName.add(testStep);
					// save step name
					testCaseStepsList.add(uuidStepName);
					break;

				case "keyBlockDisplayName":
					// jsystem test case meaningful
					actionMeaningful = childNode.getTextContent();
					break;

				// ACTION NAME
				case "keyBlockName":
					// jsystem test case action name
					ArrayList<String> uuidRecord = new ArrayList<String>();
					actionName = childNode.getTextContent();
					uuidForAction = UUID.randomUUID();
					uuidRecord.add(uuidForAction.toString());
					uuidRecord.add(actionName);
					uuidWithActions.add(uuidRecord);
					break;

				// params with values
				case "KeyBlockParam":
					// jsystem test case action name params with values
					Element blockParamElement = (Element) childNode;
					for (int k = 0; k < blockParamElement.getElementsByTagName("paramName").getLength(); k++) {
						// add a new param name
						paramListWithValues.put(blockParamElement.getElementsByTagName("paramName").item(k).getTextContent(),
								blockParamElement.getElementsByTagName("paramValue").item(k).getTextContent());
					}

					savePropertiesFile(testCase + "\\", testStep + ".properties", uuidForAction, paramListWithValues);
					paramListWithValues.clear();
					break;

				case "termAndLoopData":
					// check if we have a loop for action only eg: KeyBlock action name
					if (childNode.getParentNode().getNodeName().equals("KeyBlock")) {
						Element loopElement = (Element) childNode;

						if (loopElement.getElementsByTagName("isEnableLoop").item(0).getTextContent().equals("true")) {

							int startIndex = Integer.parseInt(loopElement.getElementsByTagName("startIndex").item(0).getTextContent());
							int endIndex = Integer.parseInt(loopElement.getElementsByTagName("endIndex").item(0).getTextContent());
							int stepValue = Integer.parseInt(loopElement.getElementsByTagName("stepValue").item(0).getTextContent());
							String loopValues = "";
							// add a new param name
							loopData.add(loopElement.getElementsByTagName("loopVariableName").item(0).getTextContent());

							for (int stepvalue = startIndex; stepvalue <= endIndex; stepvalue += stepValue) {
								loopValues += stepvalue + ";";
							}
							// remove the last ';' symbol
                            loopValues = loopValues.substring(0, loopValues.length() - 1);
							loopData.add(loopValues);
							// in order to workflow - we need to add loop into the list > uuidWithActions
							ArrayList<String> uuidLoopRecord = new ArrayList<>();
							UUID uuidLoop = UUID.randomUUID();
							uuidLoopRecord.add(uuidLoop.toString());
							uuidLoopRecord.add("loop_enabled");
							uuidWithActions.add(uuidLoopRecord);
							// update properties file
							savePropertiesFile(testCase + "\\", testStep + ".properties", uuidLoop);
						}
					}

					break;

				case "runState":
					// we need to look into the last but not least node name to do not miss the last action step

					//if KeyBlock, then we have 'runstate' for test actions and add it to the properties
					if (childNode.getParentNode().getNodeName().equals("KeyBlock")) {
						savePropertiesFile(testCase + "\\", testStep + ".properties", uuidForAction, childNode.getTextContent());
					}
					if (childNode.getParentNode().getNodeName().equals("KeyBlockGroup")) {
						generateJSystemWorkflow(testCase + "\\", testStep, uuidWithActions, true);
						uuidWithActions.clear();
						loopData.clear();
					}
					if (childNode.getParentNode().getNodeName().equals("KeyBlockScenario")) {
						// new scenario - so we can save our prepared xml file
						uuidForFatherXML = UUID.randomUUID();
						ArrayList<String> uuidScenarioName = new ArrayList<String>();
						uuidScenarioName.add(uuidForFatherXML.toString());
						uuidScenarioName.add(testCase);
						// save scenario name
						fatherXmlOfTestCases.add(uuidScenarioName);

						generateJSystemWorkflow(testCase + "\\", testCase, testCaseStepsList, false);
						testCaseStepsList.clear();
					}
					break;
			}
			parseKBWorkflow(childNode);
		}
	}

	/**
	 * method take params and create xml workflow files for test cases and test steps with actions
	 * @param filePath
	 * @param fileName
	 * @param actionsWithUUID
	 * @param scenarioWithActions
	 * @throws Exception
	 */
	public void generateJSystemWorkflow(String filePath, String fileName, ArrayList<ArrayList<String>> actionsWithUUID, boolean scenarioWithActions) throws Exception {
		// xml file header
		String data =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
						"<!--This file was generated by converting script. Developers: Aliaksandr Bahdanovich, Vadim Chiritsa for JSystem runner, " +
						"do not change it manually--><project default=\"execute scenario\" name=\"/" + smpTestPath +
						workflowPath + "/" + testCase + "/" + fileName + "\">\r\n" + XML_HEADER;

		filePath = rootXMLFolder + "\\" + filePath;

		// steps ordering
		for (int i = 0; i < actionsWithUUID.size(); i++) {
			if ((i < actionsWithUUID.size()-1) && (actionsWithUUID.get(i+1).get(1).equals("loop_enabled"))) {
				data += "\t\t<jsystemfor delimiter=\";\" fullUuid=\"${jsystem.parent.uuid}." +
						"${jsystem.uuid}."+ actionsWithUUID.get(i+1).get(0) +"\" list=\"a;b;c;d\" " +
						"param=\"myVar\" parentName=\"${jsystem.parent.name}.${ant.project.name}\">\n"+
						"\t\t\t<!--#Jsystem#-->\n" +
						"\t\t\t<sequential>\n" +
						"\t\t\t\t<echo message=\"Parameter: index=@{"+ loopData.get(0) +"}\"/>\n" +
						"\t\t\t\t\t<var name=\""+ loopData.get(0) +"\" value=\"@{"+loopData.get(0)+"}\"/>\n" +
						"\t\t\t\t\t<jsystemsetantproperties>\n" +
						"\t\t\t\t\t\t<!--Task for updating the ant parameters file - used for reference parameters-->\n" +
						"\t\t\t\t\t</jsystemsetantproperties>\n" +
						"\t\t\t\t\t<antcallback target=\"t" + i + "\"/>\n" +
						"\t\t\t</sequential>\n" +
						"\t\t\t</jsystemfor>\n";
				break;
			}
			else {
				data += String.format("\t\t<antcallback target=\"t%s\"/>\r\n", i);
			}
		}

		data += "\t</target>\r\n";

		// actionsWithUUID[UUID, actionName]
		if (scenarioWithActions) {
			// we generate XML file with references JSystem Actions
			for (int i = 0; i < actionsWithUUID.size(); i++) {

				String packagePath = "";
				if (!actionsWithUUID.get(i).get(1).equals("loop_enabled")) {
					packagePath = pathsToActionsMap.get(actionsWithUUID.get(i).get(1));
					packagePath = packagePath.replace("/", ".");
					packagePath = packagePath.substring(0, packagePath.length() - 1);
				}

				data += String.format(
						"\t<target name=\"t%s\">\r\n" +
								"\t\t<jsystem showoutput=\"true\">\r\n" +
								"\t\t\t<sysproperty key=\"jsystem.uuid\" value=\"%s\"/>\r\n" +
								"\t\t\t<sysproperty key=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\r\n" +
								"\t\t\t<sysproperty key=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\r\n" +
								"\t\t\t<test name=\"automation.allot.com.Actions.KBsystem.jsystemActions." + packagePath + ".%s.%s_KB\"/>\r\n" +
								"\t\t</jsystem>\r\n" +
								"\t</target>\r\n", i, actionsWithUUID.get(i).get(0), actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(1));
			}
		} else {
			// we generate XML file with references to XML files with JSystem Actions
			for (int i = 0; i < actionsWithUUID.size(); i++) {
				data += String.format(
						"\t<target name=\"t%s\">\n" +
								"\t\t<jsystem-ant antfile=\"${scenarios.base}/" + smpTestPath + workflowPath + "/" + testCase + "/%s.xml\">\n" +
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

	public void generateJSystemFatherWorkflow(String filePath, String fileName, ArrayList<ArrayList<String>> actionsWithUUID) throws Exception {
		// xml file header
		String data =
				"<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--This file was auto-generated by Aliaksandr Bahdanovich auto-gen script for JSystem runner, " +
						"do not change it manually--><project default=\"execute scenario\" name=\"/" + smpTestPath + workflowPath  + "/" + fileName + "\">\r\n" +
						XML_HEADER;

		filePath = rootXMLFolder + "\\" + filePath;

		// steps ordering
		for (int i = 0; i < actionsWithUUID.size(); i++) {
			data += String.format("\t\t<antcallback target=\"t%s\"/>\r\n", i);
		}

		data += "\t</target>\r\n";

		// actionsWithUUID[UUID, actionName]
		for (int i = 0; i < actionsWithUUID.size(); i++) {
			data += String.format(
					"\t<target name=\"t%s\">\n" +
							"\t\t<jsystem-ant antfile=\"${scenarios.base}/" + smpTestPath + workflowPath + "/%s/%s.xml\">\n" +
							"\t\t\t<property name=\"jsystem.uuid\" value=\"%s\"/>\n" +
							"\t\t\t<property name=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\n" +
							"\t\t\t<property name=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\n" +
							"\t\t</jsystem-ant>\n" +
							"\t</target>", i, actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(0));
		}
		// xml file last line
		data += XML_FOOTER;

		// save xml header and write data into file. filename = step name
		saveFile(filePath, fileName + ".xml", data);

	}

	public void generatePathsForActionsMap(String fileName) throws Exception {
		Document xmlHierarchyDocument = getParserObject(fileName);
		// take the correct path for KB actions from preferences.xml
		getPackageName(xmlHierarchyDocument);
	}

	public void copyDirectoryWithFilesFromTo(String scrDir,String destinationDir){
		File srcDir = new File(scrDir);
		File destDir = new File(destinationDir);
        //if dir is not exist, this dir will be created
		try {
			FileUtils.copyDirectory(srcDir, destDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception, DirectoryIteratorException {
		Main xmlParser = new Main();

		// leave as 'false' to work with workflow
		boolean buildClass = false;

		log.info("Script converter is started");

		log.info("Cleanup directory: " + workflowPath);
		FileUtils.deleteDirectory(new File(workflowPath));

		log.info("Cleanup directory: C:/JAutomationPackage/");
		FileUtils.deleteDirectory(new File("C:/JAutomationPackage/"));

		log.info("CalL: generatePathsForActionsMap");
		xmlParser.generatePathsForActionsMap(packageFileName);

		if (buildClass) {
			log.info("");
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
			log.info("");
		}

		// please uncomment this section to call class or workflow generator
		log.info("");
		log.info("XML WORKFLOW GENERATOR is started");
		Document xmlWorkFlowDocument = xmlParser.getParserObject(workFlowFileName);
		xmlParser.parseKBWorkflow(xmlWorkFlowDocument);

		log.info("generate father PCRF_basic");
		xmlParser.generateJSystemFatherWorkflow( "", workflowPath, xmlParser.fatherXmlOfTestCases);

		// copy folders
		Thread.sleep(2000);
		xmlParser.copyDirectoryWithFilesFromTo("d:\\allot\\testFRAMEWORK\\oldKB\\trunk\\tests\\SMP\\PCRF_Basic", workflowPath + "\\data\\PCRF_Basic\\");
		Thread.sleep(2000);
		xmlParser.copyDirectoryWithFilesFromTo("d:\\allot\\testFRAMEWORK\\oldKB\\trunk\\tests\\SMP\\OCS\\General", workflowPath + "\\data\\OCS\\General");
		Thread.sleep(2000);
		xmlParser.copyDirectoryWithFilesFromTo(workflowPath, "C:/JAutomationPackage/Actions/target/classes/" + smpTestPath + "PCRF_Basic/"); //"scenarios/SMP/Quality_Gates/Gate4/SMPTests/";
		log.info("Well done!");
		log.info("");
	}
}