package classGenerator;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    static final String xmlKBActions = "smp_actions.txt";
    static final String xmlKBFiles = "d:\\allot\\oldKB\\trunk\\SW\\bin\\res\\xml\\";
    static final String workflowPath = "PCRF_Basic";
    static final String packageFileName = "preferences.xml";
    static final String workFlowFileName = "PCRF_Basic.xml";
    static final String smpTestPath = "scenarios/SMP/Quality_Gates/Gate4/SMPTests/";

    public static void main(String[] args) throws Exception {
        ClassGenerator classGenerator = new ClassGenerator();
        XmlAndPropertiesGenerator xmlAndPropertiesGenerator = new XmlAndPropertiesGenerator();

        // leave as 'false' to work with workflow
        boolean buildClass = false;

        log.info("Script converter is started");

        log.info("Cleanup directory: " + workflowPath);
        FileUtils.deleteDirectory(new File(workflowPath));

        log.info("Cleanup directory: C:/JAutomationPackage/");
        FileUtils.deleteDirectory(new File("C:/JAutomationPackage/"));

        log.info("CalL: generatePathsForActionsMap");
        CommonParseActions.generatePathsForActionsMap(packageFileName);

        if (buildClass) {
            log.info("");
            log.info("CLASS GENERATOR is started");
            //get the list of xml files and pass one by one into cycle
            try (BufferedReader br = new BufferedReader(new FileReader(xmlKBActions))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.info("Input XML file is : " + line);
                    Document xmlActionsDocument = classGenerator.getParserObject(xmlKBFiles + line + ".xml");
                    // recursion node review
                    classGenerator.parseKBActions(xmlActionsDocument);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("");
        }
        // please uncomment this section to call class or workflow generator
        log.info("");
        log.info("XML WORKFLOW GENERATOR is started");
        Document xmlWorkFlowDocument;

        try {
            xmlWorkFlowDocument = xmlAndPropertiesGenerator.getParserObject(workFlowFileName);
        } catch (IOException e) {
            log.info("try to replace special symbols");
            CommonParseActions.removeSpecialSymbols();
            log.info("try to replace special symbols - success");
            xmlWorkFlowDocument = xmlAndPropertiesGenerator.getParserObject(workFlowFileName);
        }

        xmlAndPropertiesGenerator.parseKBWorkflow(xmlWorkFlowDocument);

        log.info("generate father PCRF_basic");
        xmlAndPropertiesGenerator.generateJSystemFatherWorkflow("", workflowPath, xmlAndPropertiesGenerator.fatherXmlOfTestCases);
        xmlAndPropertiesGenerator.savePropertiesFileForFatherXML("", workflowPath, xmlAndPropertiesGenerator.mapTestScenarioMapTestStepsPropertiesFatherXML);

        // copy folders
        Thread.sleep(2000);
        CommonParseActions.copyDirectoryWithFilesFromTo("d:\\allot\\testFRAMEWORK\\oldKB\\trunk\\tests\\SMP\\PCRF_Basic", workflowPath + "\\data\\PCRF_Basic\\");
        Thread.sleep(2000);
        CommonParseActions.copyDirectoryWithFilesFromTo("d:\\allot\\testFRAMEWORK\\oldKB\\trunk\\tests\\SMP\\OCS\\General", workflowPath + "\\data\\OCS\\General");
        Thread.sleep(2000);
        CommonParseActions.copyDirectoryWithFilesFromTo(workflowPath, "C:/JAutomationPackage/Actions/target/classes/" + smpTestPath + "PCRF_Basic/"); //"scenarios/SMP/Quality_Gates/Gate4/SMPTests/";
        log.info("Well done!");
        log.info("");
    }
}
