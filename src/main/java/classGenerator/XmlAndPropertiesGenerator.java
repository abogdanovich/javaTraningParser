package classGenerator;

import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class XmlAndPropertiesGenerator extends CommonParseActions{
    private static final Logger log = Logger.getLogger(XmlAndPropertiesGenerator.class);

    private static final String rootXMLFolder = workflowPath;
    private static final String smpTestPath = "scenarios/SMP/Quality_Gates/Gate4/SMPTests/";
    private final HashMap<String, String> paramListWithValues = new HashMap<>();
    private final ArrayList<ArrayList<String>> uuidWithActions = new ArrayList<>();
    private final ArrayList<ArrayList<String>> testCaseStepsList = new ArrayList<>();
    public final ArrayList<ArrayList<String>> fatherXmlOfTestCases = new ArrayList<>();
    private final ArrayList<String> loopData = new ArrayList<>();



    //variable for creating properties of father xml
    public HashMap<String, HashMap<String,ArrayList<String>>> mapTestScenarioMapTestStepsPropertiesFatherXML = new HashMap<>();
    private ArrayList <String> listUuidOfActionsForPropertiesOfFatherXML = new ArrayList<>();
    private HashMap <String, ArrayList<String>> mapUuidStepListUuidActionsPropertiesFatherXML = new HashMap<>();

    //variable for creating properties of test scenarios
    private ArrayList<HashMap<String,ArrayList<String>>> listOfDisabledTestStepsPropertiesOfTestCase = new ArrayList<HashMap<String,ArrayList<String>>>();
    private ArrayList <String> listUuidOfActionsForPropertiesOfTestCase = new ArrayList<>();
    private HashMap <String, ArrayList<String>> mapUuidStepListUuidActionsPropertiesTestCase = new HashMap<>();

    private UUID uuidForAction = UUID.randomUUID();

    private String uuidTestStep= "";
    private String actionMeaningful = "";
    private String testCase = "";
    private String testStep = "";
    private String runStateOfTestStep ="";
    private String runStateOfTestCase ="";

    private static final String XML_FOOTER = "</project>";
    private static final String XML_HEADER =
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
     * Append properties files with uuid:param={value}
     * @param filePath
     * @param fileName
     * @param uuid
     * @param paramListWithValues
     * @throws IOException
     */
    private void savePropertiesFile(String filePath, String fileName, UUID uuid, HashMap<String, String> paramListWithValues) throws IOException {
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
                //paramValue = paramValue.replace("$${", "${run:");
                paramValue = paramValue.replace("$${", "${");
                if (paramValue.contains("..\\..\\tests\\SMP\\")) {
                    // all specific test data files should be under test_suite/data folder
                    paramValue = paramValue.replace("..\\..\\tests\\SMP\\", "C:/JAutomationPackage/Actions/target/classes/" + smpTestPath  + workflowPath + "/data/");
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
    private void savePropertiesFile(String filePath, String fileName, UUID uuid, String isDisabled) throws IOException {
        try {
            filePath = rootXMLFolder + "\\" + filePath;
            checkDirectoryExists(filePath);

            BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName, true));
            boolean checkBox = !Boolean.valueOf(isDisabled);
            // write specific configuration line
            WriteFileBuffer.write(String.format("%s.jsystem.isdisabled=%s\n", uuid, Boolean.toString(checkBox)));
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
    private void savePropertiesFile(String filePath, String fileName, UUID uuid) throws IOException {
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
     * save properties file for global params
     * @param filePath
     * @param fileName
     * @param uuid
     * @param params
     * @throws IOException
     */
    private void savePropertiesFile(String filePath, String fileName, UUID uuid, ArrayList<String> params) throws IOException {
        try {
            filePath = rootXMLFolder + "\\" + filePath;
            checkDirectoryExists(filePath);

            BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName, true));
            // write specific configuration line

            String data = String.format("%s.AdditionalParamters=automation.allot.com.Products.GlobalVariable;\\#Tue Nov 20 15\\:58\\:11 MSK 2018\\r\\n", uuid);

            for (int k = 0; k < params.size() / 3; k++) {
                for (int i = (k * 3); i < (k * 3) + 3; i += 3) {
                    // name - description - value array
                    data += String.format("%s.ParamterName\\=%s\\r\\n%s.ParamterDescreption\\=%s\\r\\n%s.ParamterValue\\=%s\\r\\n%s.ParamterType\\=String\\r\\n", k, params.get(i), k, params.get(i + 1), k, params.get(i + 2), k);
                }
            }

            WriteFileBuffer.write(data);
            WriteFileBuffer.write(System.lineSeparator());

            // write specific configuration line
            WriteFileBuffer.write(String.format("%s.EvaluateMathExpression=false\n", uuid));
            WriteFileBuffer.write(String.format("%s.jsystem.uisettings=sortSection\\:0;sortHeader\\:0;paramsOrder\\:defaultOrder;activeTab\\:0;headersRatio\\:0.1,0.25,0.05,0.2\n", uuid));
            WriteFileBuffer.write(String.format("%s.meaningfulName=Global Parameters\n", uuid));
            WriteFileBuffer.write(System.lineSeparator());

            WriteFileBuffer.close();
        } finally {
            log.info(String.format("Properties file [%s] is updated", fileName));
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

                    setRunStateOfTestCase(childNode);

                    break;

                // STEP NAME
                case "keyBlockGroupName":
                    // jsystem test step
                    testStep = childNode.getTextContent();
                    testStep = testStep.replaceAll("[^a-zA-Z0-9\\.\\-]", "_");
                    ArrayList<String> uuidStepName = new ArrayList<String>();

                    uuidTestStep = UUID.randomUUID().toString();

                    uuidStepName.add(uuidTestStep);
                    uuidStepName.add(testStep);
                    // save step name
                    testCaseStepsList.add(uuidStepName);

                    //find test step, which is disabled
                    listUuidOfActionsForPropertiesOfTestCase = new ArrayList<>();
                    setRunStateOfTestStep(childNode);

                    //update list of action for properties father xml
                    listUuidOfActionsForPropertiesOfFatherXML = new ArrayList<>();

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

                    //if this test STEP is disabled, fill list of his actions
                    if(runStateOfTestStep.equals("false")) {
                        listUuidOfActionsForPropertiesOfTestCase.add(uuidForAction.toString());
                    }

                    //if this test CASE is disabled, fill list of his actions
                    if(runStateOfTestCase.equals("false")) {
                        listUuidOfActionsForPropertiesOfFatherXML.add(uuidForAction.toString());
                        mapUuidStepListUuidActionsPropertiesFatherXML.put(uuidTestStep, listUuidOfActionsForPropertiesOfFatherXML);
                    }
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
                    //if KeyBlock, then we have 'runstate' for test actions and add it to the properties
                    if (childNode.getParentNode().getNodeName().equals("KeyBlock")) {
                        savePropertiesFile(testCase + "\\", testStep + ".properties", uuidForAction, childNode.getTextContent());
                    }
                    if (childNode.getParentNode().getNodeName().equals("KeyBlockGroup")) {
                        generateJSystemWorkflow(testCase + "\\", testStep, uuidWithActions, true);
                        uuidWithActions.clear();
                        loopData.clear();

                        //fill list: hashMaps: key: uuid of test step; value: list of uuid's test actions
                        if(!listUuidOfActionsForPropertiesOfTestCase.isEmpty()){
                            mapUuidStepListUuidActionsPropertiesTestCase.put(uuidTestStep , listUuidOfActionsForPropertiesOfTestCase);
                            listOfDisabledTestStepsPropertiesOfTestCase.add(mapUuidStepListUuidActionsPropertiesTestCase);
                        }
                        listUuidOfActionsForPropertiesOfTestCase = new ArrayList<>();
                        mapUuidStepListUuidActionsPropertiesTestCase = new HashMap<>();


                    }
                    if (childNode.getParentNode().getNodeName().equals("KeyBlockScenario")) {
                        // new scenario - so we can save our prepared xml file
                        UUID uuidForFatherXML = UUID.randomUUID();
                        ArrayList<String> uuidScenarioName = new ArrayList<String>();
                        uuidScenarioName.add(uuidForFatherXML.toString());
                        uuidScenarioName.add(testCase);
                        // save scenario name
                        fatherXmlOfTestCases.add(uuidScenarioName);

                        //if this test case has steps, which are disabled, then create for it properties
                        if(listOfDisabledTestStepsPropertiesOfTestCase.size()>0){
                            savePropertiesFileForTestScenarios(testCase + "\\",testCase, listOfDisabledTestStepsPropertiesOfTestCase);
                        }
                        listOfDisabledTestStepsPropertiesOfTestCase =new ArrayList<>();

                        //if this test case is disabled, then put it uuid to hashMap
                        if(runStateOfTestCase.equals("false")) {
                            mapTestScenarioMapTestStepsPropertiesFatherXML.put(uuidForFatherXML.toString(), mapUuidStepListUuidActionsPropertiesFatherXML);
                            mapUuidStepListUuidActionsPropertiesFatherXML= new HashMap<>();
                        }

                        generateJSystemWorkflow(testCase + "\\", testCase, testCaseStepsList, false);
                        testCaseStepsList.clear();
                    }
                    break;
                case "additionalParamsTable":
                    // workflow AddAdditionalParameters.xml with Action: AddAdditionalParameters
                    Element additionalParamsTable = (Element) childNode;
                    ArrayList<String> additionalParamsTableData = new ArrayList<>();
                    String paramValue = "";
                    UUID addUUIDParamaters = UUID.randomUUID();
                    ArrayList<String> uuidAdditional = new ArrayList<String>();

                    for (int h = 0; h < additionalParamsTable.getElementsByTagName("string").getLength(); h++) {
                        // param string - param value string
                        paramValue = additionalParamsTable.getElementsByTagName("string").item(h).getTextContent();
                        if (paramValue.contains("###")) {
                            String[] split = paramValue.split("###");
                            // description
                            additionalParamsTableData.add(split[0]);
                            // value
                            additionalParamsTableData.add(split[1]);
                        } else {
                            // name
                            additionalParamsTableData.add(paramValue);
                        }
                    }
                    // save global params as workflow
                    generateJSystemWorkflow("AddAdditionalParamaters\\", "AddAdditionalParamaters", addUUIDParamaters.toString());
                    // save properties file for global param
                    savePropertiesFile("AddAdditionalParamaters" + "\\", "AddAdditionalParamaters.properties", addUUIDParamaters, additionalParamsTableData);

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
    private void generateJSystemWorkflow(String filePath, String fileName, ArrayList<ArrayList<String>> actionsWithUUID, boolean scenarioWithActions) throws Exception {
        // xml file header
        StringBuilder data = new StringBuilder();
                data.append(
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<!--This file was generated by converting script. Developers: Aliaksandr Bahdanovich, Vadim Chiritsa for JSystem runner, " +
                        "do not change it manually--><project default=\"execute scenario\" name=\"/" + smpTestPath +
                        workflowPath + "/" + testCase + "/" + fileName + "\">\r\n" + XML_HEADER);

        filePath = rootXMLFolder + "\\" + filePath;

        // steps ordering
        for (int i = 0; i < actionsWithUUID.size(); i++) {
            if ((i < actionsWithUUID.size()-1) && (actionsWithUUID.get(i+1).get(1).equals("loop_enabled"))) {
                data.append("\t\t<jsystemfor delimiter=\";\" fullUuid=\"${jsystem.parent.uuid}." +
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
                        "\t\t\t</jsystemfor>\n");
                break;
            }
            else {
                data.append(String.format("\t\t<antcallback target=\"t%s\"/>\r\n", i));
            }
        }

        data.append("\t</target>\r\n");

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

                data.append(String.format(
                        "\t<target name=\"t%s\">\r\n" +
                                "\t\t<jsystem showoutput=\"true\">\r\n" +
                                "\t\t\t<sysproperty key=\"jsystem.uuid\" value=\"%s\"/>\r\n" +
                                "\t\t\t<sysproperty key=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\r\n" +
                                "\t\t\t<sysproperty key=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\r\n" +
                                "\t\t\t<test name=\"automation.allot.com.Actions.KBsystem.jsystemActions." + packagePath + ".%s.%s_KB\"/>\r\n" +
                                "\t\t</jsystem>\r\n" +
                                "\t</target>\r\n", i, actionsWithUUID.get(i).get(0), actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(1)));
            }
        } else {
            // we generate XML file with references to XML files with JSystem Actions
            for (int i = 0; i < actionsWithUUID.size(); i++) {
                data.append(String.format(
                        "\t<target name=\"t%s\">\n" +
                                "\t\t<jsystem-ant antfile=\"${scenarios.base}/" + smpTestPath + workflowPath + "/" + testCase + "/%s.xml\">\n" +
                                "\t\t\t<property name=\"jsystem.uuid\" value=\"%s\"/>\n" +
                                "\t\t\t<property name=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\n" +
                                "\t\t\t<property name=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\n" +
                                "\t\t</jsystem-ant>\n" +
                                "\t</target>", i, actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(0)));
            }
        }

        // xml file last line
        data.append(XML_FOOTER);

        // save xml header and write data into file. filename = step name
        saveFile(filePath, fileName + ".xml", data.toString());
    }

    /**
     * xml for AddAdditionalParamaters global workflow
     * @param filePath
     * @param fileName
     * @param uuid
     * @throws Exception
     */
    private void generateJSystemWorkflow(String filePath, String fileName, String uuid) throws Exception {
        // xml file header
        StringBuilder data = new StringBuilder();
        data.append(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<!--This file was generated by converting script. Developers: Aliaksandr Bahdanovich, Vadim Chiritsa for JSystem runner, " +
                        "do not change it manually--><project default=\"execute scenario\" name=\"/" + smpTestPath +
                        workflowPath + "/" + testCase + "/" + fileName + "\">\r\n" + XML_HEADER);

        filePath = rootXMLFolder + "\\" + filePath;

        data.append("\t\t<antcallback target=\"t0\"/>\r\n\t</target>\n");

        // we generate XML file with references JSystem Actions

        data.append(
                "\t<target name=\"t0\">\r\n" +
                "\t\t<jsystem showoutput=\"true\">\r\n" +
                "\t\t\t<sysproperty key=\"jsystem.uuid\" value=\"" + uuid + "\"/>\r\n" +
                "\t\t\t<sysproperty key=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\r\n" +
                "\t\t\t<sysproperty key=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\r\n" +
                "\t\t\t<test name=\"automation.allot.com.Actions.AOS.Others.DeploymentProfile.addAdditionalParameters\"/>\r\n" +
                "\t\t</jsystem>\r\n" +
                "\t</target>\r\n");

        // xml file last line
        data.append(XML_FOOTER);

        // save xml header and write data into file. filename = step name
        saveFile(filePath, fileName + ".xml", data.toString());
    }

    public void generateJSystemFatherWorkflow(String filePath, String fileName, ArrayList<ArrayList<String>> actionsWithUUID) throws Exception {
        // xml file header
        StringBuilder data = new StringBuilder();
              data.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!--This file was auto-generated by Aliaksandr Bahdanovich auto-gen script for JSystem runner, " +
                        "do not change it manually--><project default=\"execute scenario\" name=\"/" + smpTestPath + workflowPath  + "/" + fileName + "\">\r\n" +
                        XML_HEADER);

        filePath = rootXMLFolder + "\\" + filePath;

        // add global params
        data.append("\t\t<antcallback target=\"t0\"/>\r\n");

        // steps ordering with shifted i to 1 because of AddAdditionalParamaters = 0
        for (int i = 0; i < actionsWithUUID.size(); i++) {
            data.append(String.format("\t\t<antcallback target=\"t%s\"/>\r\n", i+1));
        }

        data.append("\t</target>\r\n");

        // add manually AddAdditionalParamaters to father workflow
        data.append(
                "\t<target name=\"t0\">\n" +
                "\t\t<jsystem-ant antfile=\"${scenarios.base}/" + smpTestPath + workflowPath + "/AddAdditionalParamaters/AddAdditionalParamaters.xml\">\n" +
                "\t\t\t<property name=\"jsystem.uuid\" value=\"" + UUID.randomUUID().toString() + "\"/>\n" +
                "\t\t\t<property name=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\n" +
                "\t\t\t<property name=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\n" +
                "\t\t</jsystem-ant>\n" +
                "\t</target>");

        // actionsWithUUID[UUID, actionName] with shifted i to 1 because of AddAdditionalParamaters = 0
        for (int i = 0; i < actionsWithUUID.size(); i++) {
            data.append(String.format(
                    "\t<target name=\"t%s\">\n" +
                    "\t\t<jsystem-ant antfile=\"${scenarios.base}/" + smpTestPath + workflowPath + "/%s/%s.xml\">\n" +
                    "\t\t\t<property name=\"jsystem.uuid\" value=\"%s\"/>\n" +
                    "\t\t\t<property name=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\n" +
                    "\t\t\t<property name=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\n" +
                    "\t\t</jsystem-ant>\n" +
                    "\t</target>", i+1, actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(1), actionsWithUUID.get(i).get(0)));
        }
        // xml file last line
        data.append(XML_FOOTER);

        // save xml header and write data into file. filename = step name
        saveFile(filePath, fileName + ".xml", data.toString());
    }

    /**
     * method take params and create properties for test steps
     * @param filePath
     * @param fileName
     * @param uuidTS
     * @throws Exception
     */
    public void savePropertiesFileForTestScenarios(String filePath, String fileName, ArrayList<HashMap<String,ArrayList<String>>> uuidTS) throws IOException {
        try {
            filePath = rootXMLFolder + "\\" + filePath;
            checkDirectoryExists(filePath);

            BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName+".properties", true));

            // write specific configuration line
            for (HashMap<String,ArrayList<String>> hashMap: uuidTS) {
                hashMap.keySet().stream().forEach(uuidOfStep->{
                    hashMap.get(uuidOfStep).stream().forEach(uuidOfActionOfThisStep->{
                        try {
                            WriteFileBuffer.write(String.format("%s.%s.jsystem.isdisabled=%s\n", uuidOfStep, uuidOfActionOfThisStep, true));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    try {
                        WriteFileBuffer.write(String.format("%s.jsystem.hidden.in.html=%s\n", uuidOfStep, "false"));
                        WriteFileBuffer.write(String.format("%s.jsystem.jsystem.known.issue=%s\n", uuidOfStep, "false"));
                        WriteFileBuffer.write(String.format("%s.jsystem.negative.test=%s\n", uuidOfStep, "false"));
                    }
                        catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                );
            }

            WriteFileBuffer.write(String.format("jsystem.hidden.in.html=false\n" +
                    "jsystem.known.issue=false\n" +
                    "jsystem.negative.test=false"));

            WriteFileBuffer.close();
        } finally {
            log.info(String.format("File [%s] is updated", fileName));
        }
    }

    public void savePropertiesFileForFatherXML(String filePath, String fileName, HashMap<String, HashMap<String,ArrayList<String>>> uuidTS) throws IOException {
        try {
            filePath = rootXMLFolder + "\\" + filePath;
            checkDirectoryExists(filePath);

            BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName+".properties", true));

            // write specific configuration line
            uuidTS.keySet().stream().forEach(uuidOfTestCase->{
                uuidTS.get(uuidOfTestCase).keySet().stream().forEach(uuidOfTestStep->{
                    uuidTS.get(uuidOfTestCase).get(uuidOfTestStep).stream().forEach(uuidOfActions->{
                        try {
                            WriteFileBuffer.write(String.format("%s.%s.%s.jsystem.isdisabled=%s\n", uuidOfTestCase, uuidOfTestStep, uuidOfActions, true));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    try {
                        WriteFileBuffer.write(String.format("%s.%s.jsystem.hidden.in.html=%s\n",uuidOfTestCase, uuidOfTestStep, "false"));
                        WriteFileBuffer.write(String.format("%s.%s.jsystem.known.issue=%s\n",uuidOfTestCase, uuidOfTestStep, "false"));
                        WriteFileBuffer.write(String.format("%s.%s.jsystem.isdisabled=%s\n", uuidOfTestCase,uuidOfTestStep, "false"));
                        WriteFileBuffer.write(String.format("%s.%s.jsystem.negative.test=%s\n", uuidOfTestCase,uuidOfTestStep, "false"));
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                try {
                    WriteFileBuffer.write(String.format("%s.jsystem.hidden.in.html=%s\n", uuidOfTestCase, "false"));
                    WriteFileBuffer.write(String.format("%s.jsystem.known.issue=%s\n", uuidOfTestCase, "false"));
                    WriteFileBuffer.write(String.format("%s.jsystem.isdisabled=%s\n", uuidOfTestCase, "false"));
                    WriteFileBuffer.write(String.format("%s.jsystem.negative.test=%s\n", uuidOfTestCase, "false"));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            });

            WriteFileBuffer.write(String.format("jsystem.hidden.in.html=false\n" +
                    "jsystem.known.issue=false\n" +
                    "jsystem.negative.test=false"));

            WriteFileBuffer.close();
        } finally {
            log.info(String.format("File [%s] is updated", fileName));
        }
    }


    private void setRunStateOfTestStep(Node childNode) {
        runStateOfTestStep ="";
        Node elementKeyBlockGroup = childNode.getParentNode();
        for(int t=0;t<elementKeyBlockGroup.getChildNodes().getLength();t++) {
            if(elementKeyBlockGroup.getChildNodes().item(t).getNodeName().equals("runState")){
                runStateOfTestStep = elementKeyBlockGroup.getChildNodes().item(t).getTextContent();
                break;
            }
        }
    }

    private void setRunStateOfTestCase(Node childNode) {
        runStateOfTestCase ="";
        Node elementKeyBlockScenario = childNode.getParentNode();
        for(int t=0;t<elementKeyBlockScenario.getChildNodes().getLength();t++) {
            if(elementKeyBlockScenario.getChildNodes().item(t).getNodeName().equals("runState")){
                runStateOfTestCase = elementKeyBlockScenario.getChildNodes().item(t).getTextContent();
                break;
            }
        }
    }
}
