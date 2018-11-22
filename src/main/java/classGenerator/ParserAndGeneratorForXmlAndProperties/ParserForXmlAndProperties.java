package classGenerator.ParserAndGeneratorForXmlAndProperties;

import classGenerator.CommonParseActions;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ParserForXmlAndProperties extends CommonParseActions {
    private static final Logger log = Logger.getLogger(ParserForXmlAndProperties.class);


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
    private PropertiesGenerator propertiesGenerator;
    private XmlGenerator xmlGenerator;


    public ParserForXmlAndProperties(String workflowPath) {
        this.workflowPath = workflowPath;
        this.rootXMLFolder = "output\\"+workflowPath;
        this.propertiesGenerator = new PropertiesGenerator();
        this.xmlGenerator = new XmlGenerator();
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

                    propertiesGenerator.savePropertiesFile(testCase + "\\", testStep + ".properties", uuidForAction, paramListWithValues, actionMeaningful);
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
                            propertiesGenerator.savePropertiesFileWithLoop(testCase + "\\", testStep + ".properties", uuidLoop, loopData);
                        }
                    }

                    break;

                case "runState":
                    //if KeyBlock, then we have 'runstate' for test actions and add it to the properties
                    if (childNode.getParentNode().getNodeName().equals("KeyBlock")) {
                        propertiesGenerator.savePropertiesFile(testCase + "\\", testStep + ".properties", uuidForAction, childNode.getTextContent());
                    }
                    if (childNode.getParentNode().getNodeName().equals("KeyBlockGroup")) {
                        xmlGenerator.generateJSystemWorkflow(testCase + "\\", testStep, uuidWithActions, true, testCase, loopData);
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
                            propertiesGenerator.savePropertiesFileForTestScenarios(testCase + "\\",testCase, listOfDisabledTestStepsPropertiesOfTestCase);
                        }
                        listOfDisabledTestStepsPropertiesOfTestCase =new ArrayList<>();

                        //if this test case is disabled, then put it uuid to hashMap
                        if(runStateOfTestCase.equals("false")) {
                            mapTestScenarioMapTestStepsPropertiesFatherXML.put(uuidForFatherXML.toString(), mapUuidStepListUuidActionsPropertiesFatherXML);
                            mapUuidStepListUuidActionsPropertiesFatherXML= new HashMap<>();
                        }

                        xmlGenerator.generateJSystemWorkflow(testCase + "\\", testCase, testCaseStepsList, false, testCase, loopData);
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
                    xmlGenerator.generateJSystemWorkflow("AddAdditionalParamaters\\", "AddAdditionalParamaters", addUUIDParamaters.toString(), testCase);
                    // save properties file for global param
                    propertiesGenerator.savePropertiesFile("AddAdditionalParamaters" + "\\", "AddAdditionalParamaters.properties", addUUIDParamaters, additionalParamsTableData);

                    break;

            }
            parseKBWorkflow(childNode);
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

    public void savePropertiesFileForFatherXML() throws IOException {
        propertiesGenerator.savePropertiesFileForFatherXML("", workflowPath, mapTestScenarioMapTestStepsPropertiesFatherXML);
    }

    public void generateJSystemFatherWorkflow() throws Exception {
        xmlGenerator.generateJSystemFatherWorkflow("", workflowPath, fatherXmlOfTestCases);
    }
}
