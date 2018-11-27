package classGenerator;

import classGenerator.ParserAndGeneratorForClassFiles.ParserForClass;
import classGenerator.ParserAndGeneratorForXmlAndProperties.ParserForXmlAndProperties;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
    private static final Logger log = Logger.getLogger(Main.class);

    // change for every feature run
    static final String workflowPath = "PCRF_Basic";
    static final String kbPath = "d:\\KB\\";
    static final String xmlKBFiles = kbPath + "trunk\\SW\\bin\\res\\xml\\";

    static final String workFlowFileName = workflowPath + ".xml";

    static final String xmlKBActions = "smp_actions.txt";
    static final String packageFileName = "preferences.xml";

    static final String smpTestPath = "scenarios/SMP/Quality_Gates/Gate4/SMPTests/";

    public static void main(String[] args) throws Exception {
        ParserForClass classGenerator = new ParserForClass(workflowPath);
        ParserForXmlAndProperties xmlAndPropertiesGenerator = new ParserForXmlAndProperties(workflowPath);

        // leave as 'false' to work with workflow
        boolean buildClass = false;

        log.info("Script converter is started");

        log.info("Cleanup directory: output\\");
        FileUtils.deleteDirectory(new File("output\\"));

        log.info("Cleanup directory: C:/JAutomationPackage/");
        FileUtils.deleteDirectory(new File("C:/JAutomationPackage/"));

        log.info("Call: generatePathsForActionsMap");
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
        } else {
            log.info("");
            log.info("XML WORKFLOW GENERATOR is started");
            Document xmlWorkFlowDocument;

            try {
                xmlWorkFlowDocument = xmlAndPropertiesGenerator.getParserObject(workFlowFileName);
            } catch (IOException e) {
                log.info("try to replace special symbols");
                CommonParseActions.removeSpecialSymbols(workflowPath);
                log.info("try to replace special symbols - success");
                xmlWorkFlowDocument = xmlAndPropertiesGenerator.getParserObject(workFlowFileName);
            }

            xmlAndPropertiesGenerator.parseKBWorkflow(xmlWorkFlowDocument);

            log.info("generate father PCRF_basic");
            xmlAndPropertiesGenerator.generateJSystemFatherWorkflow();
            xmlAndPropertiesGenerator.savePropertiesFileForFatherXML();

            // copy workflow folders
            Thread.sleep(2000);
            CommonParseActions.copyDirectoryWithFilesFromTo(kbPath + "trunk\\tests\\SMP\\" + workflowPath, "output\\" + workflowPath + "\\data\\" + workflowPath);
            Thread.sleep(2000);
            CommonParseActions.copyDirectoryWithFilesFromTo(kbPath + "trunk\\tests\\SMP\\OCS\\General", workflowPath + "\\data\\OCS\\General");
            Thread.sleep(2000);
            CommonParseActions.copyDirectoryWithFilesFromTo("output\\" + workflowPath, "C:/JAutomationPackage/Actions/target/classes/" + smpTestPath + workflowPath);
            log.info("Well done!");
            log.info("");
        }
    }
}
