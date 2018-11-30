package classGenerator.ParserAndGeneratorForXmlAndProperties;

import classGenerator.CommonParseActions;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ParserForXmlAndProperties extends CommonParseActions {
    private static final Logger log = Logger.getLogger(ParserForXmlAndProperties.class);


    private final HashMap<String, String> paramListWithValues = new HashMap<>();
    private final ArrayList<ArrayList<String>> uuidWithActions = new ArrayList<>();
    private final ArrayList<ArrayList<String>> testCaseStepsList = new ArrayList<>();
    public final ArrayList<ArrayList<String>> fatherXmlOfTestCases = new ArrayList<>();
    private ArrayList<String> loopData = new ArrayList<>();
    private final ArrayList<ArrayList<String>> listOfListLoopData = new ArrayList<>();

    //variable for creating properties of father xml
    public HashMap<String, HashMap<String,ArrayList<String>>> mapTestScenarioMapTestStepsPropertiesFatherXML = new HashMap<>();
    private ArrayList <String> listUuidOfActionsForPropertiesOfFatherXML = new ArrayList<>();
    private HashMap <String, ArrayList<String>> mapUuidStepListUuidActionsPropertiesFatherXML = new HashMap<>();

    //variable for creating properties of test scenarios
    private ArrayList<HashMap<String,ArrayList<String>>> listOfDisabledTestStepsPropertiesOfTestCase = new ArrayList<HashMap<String,ArrayList<String>>>();
    private ArrayList <String> listUuidOfActionsForPropertiesOfTestCase = new ArrayList<>();
    private HashMap <String, ArrayList<String>> mapUuidStepListUuidActionsPropertiesTestCase = new HashMap<>();

    private UUID uuidForAction = UUID.randomUUID();

    private String uuidDataDriven = "";

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

                            //FIXME in this case we check string "$${NumberOfGroups}", it is not good, just for working in case Quota
                            if (!loopElement.getElementsByTagName("startIndex").item(0).getTextContent().equals("") &&
                                    !loopElement.getElementsByTagName("endIndex").item(0).getTextContent().contains("$${")) {
                                fillLoopDataForStartAndEndIndexes(loopElement);
                            } else {
                                fillLoopDataForStringArguments(loopElement);
                            }

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

                        //get loop from test step
                        Node nodeLoopKeyBlockGroup = childNode;
                        while(true){
                            nodeLoopKeyBlockGroup = nodeLoopKeyBlockGroup.getNextSibling();
                            if (nodeLoopKeyBlockGroup.getNodeName().equals("termAndLoopData")){
                             getDataFromKeyBlockGroup(nodeLoopKeyBlockGroup);
                             break;
                            }
                        }

                        xmlGenerator.generateJSystemWorkflow(testCase + "\\", testStep, uuidWithActions, true, testCase, listOfListLoopData, uuidDataDriven);
                        listOfListLoopData.clear();
                        uuidWithActions.clear();
                        uuidDataDriven = "";
                        //loopData.clear();

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
                        xmlGenerator.generateJSystemWorkflow(testCase + "\\", testCase, testCaseStepsList, false, testCase, listOfListLoopData, uuidDataDriven);
                        listOfListLoopData.clear();
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

    private void getDataFromKeyBlockGroup(Node childNode) throws IOException {
        Element loopElementTestStep = (Element) childNode;
        // when we have loop with filelocation, loop status is disabled
        if (loopElementTestStep.getElementsByTagName("isEnableLoop").item(0).getTextContent().equals("true")) {
            if (!loopElementTestStep.getElementsByTagName("fileLocation").item(0).getTextContent().equals("")){
                String dataFilePath = loopElementTestStep.getElementsByTagName("fileLocation").item(0).getTextContent();
                String variableName = loopElementTestStep.getElementsByTagName("loopVariableName").item(0).getTextContent();
                // this method create and save CSV file and RETURN path
                String dataDrivenFile = getFileConvertToCsvAndSave(dataFilePath, testCase+ "\\", variableName);

                // create UUID and boolean isDataDriven = true
                ArrayList<String> dataDriven = new ArrayList<>();
                // File and Parameter for data driven
                dataDriven.add(dataDrivenFile);
                dataDriven.add(loopElementTestStep.getElementsByTagName("loopVariableName").item(0).getTextContent());

                UUID uuidLoop = UUID.randomUUID();
                uuidDataDriven = uuidLoop.toString();

                // save properties for this step (in properties should be uuids of actions and them parameters: File and Parameters
                propertiesGenerator.savePropertiesFileWithDataDriven(testCase + "\\", testStep + ".properties", uuidLoop, dataDriven);

            }
            // we have case when we havno no file but we have $${NumberOfGroups} FOR end index
            // TODO: add this case without file
        }
    }

    //read txt file, convert to csv and save to testcase package
    private String getFileConvertToCsvAndSave(String dataFilePathInKB, String filePath, String variableName) throws IOException {
        String KBpath = "D:\\KB\\trunk\\tests\\SMP\\";
        String fileName="";
        //path to file in KB package
        String [] splitPath = dataFilePathInKB.split("SMP");
        KBpath +=splitPath[1];
        //get file name
        splitPath = dataFilePathInKB.split("\\\\");
        fileName=splitPath[splitPath.length-1].replaceAll(".txt","");

        String pathToSaveCsv=rootXMLFolder+"\\"+filePath;
        checkDirectoryExists(pathToSaveCsv);
        BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(pathToSaveCsv+fileName+".csv", true));
        try (BufferedReader br = new BufferedReader(new FileReader(KBpath))) {
            WriteFileBuffer.write(variableName+"\n");
            String line;
            while ((line = br.readLine()) != null) {
                WriteFileBuffer.write(line+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WriteFileBuffer.close();
        String pathForLoopJSystemCsv = pathToSaveCsv+fileName+".csv";
        pathForLoopJSystemCsv = pathForLoopJSystemCsv.replaceAll("output","C:/JAutomationPackage/Actions/target/classes/scenarios/SMP/Quality_Gates/Gate4/SMPTests");
        pathForLoopJSystemCsv = pathForLoopJSystemCsv.replaceAll("\\\\","/");
        log.debug("#### "+ pathForLoopJSystemCsv);
        return pathForLoopJSystemCsv;
    }

    private void fillLoopDataForStartAndEndIndexes(Element loopElement) {
        loopData=new ArrayList<>();
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
        listOfListLoopData.add(loopData);
    }

    private void fillLoopDataForStringArguments(Element loopElement) {
        loopData=new ArrayList<>();
        String loopValues = "";
        // add a new param name
        loopData.add(loopElement.getElementsByTagName("loopVariableName").item(0).getTextContent());

        if(!loopElement.getElementsByTagName("parametersListInput").item(0).getTextContent().equals("")) {
            String parametersListInput = loopElement.getElementsByTagName("parametersListInput").item(0).getTextContent();
            String[] splitValues = parametersListInput.split(",");
            for (String value : splitValues) {
                loopValues += value + ";";
            }
            // remove the last ';' symbol
            loopValues = loopValues.substring(0, loopValues.length() - 1);
        } else {
            //FIXME just for this moment, when we don't know, what we should do with variable in loop
            loopValues = "1;2;3;4;5;6;7;8;9;10;11;12";
        }
        loopData.add(loopValues);
        listOfListLoopData.add(loopData);
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
