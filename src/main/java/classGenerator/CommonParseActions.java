package classGenerator;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public abstract class CommonParseActions {
    private static final Logger log = Logger.getLogger(CommonParseActions.class);

    protected static final String smpTestPath = "scenarios/SMP/Quality_Gates/Gate4/SMPTests/";
    public static String workflowPath = "";
    public static String rootXMLFolder = "";

    public static final HashMap<String, String> pathsToActionsMap = new HashMap<>();
    public static String rootClassFolder = "";
    public static String folderPath = "";
    public static String packagePath = "";
    public String actionName = "";

    public static void generatePathsForActionsMap(String fileName) throws Exception {
        Document xmlHierarchyDocument = getParserObject(fileName);
        // take the correct path for KB actions from preferences.xml
        getPackageName(xmlHierarchyDocument);
    }

    /**
     * get document object for XML parsing
     * @param fileName
     * @return
     * @throws Exception
     */
    public static Document getParserObject(String fileName) throws Exception {
        Path xmlFile = Paths.get(fileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

        return dBuilder.parse(xmlFile.toString());
    }

    /**
     * Method that takes package path from <preferences.xml> KB file
     * @param node
     * @throws Exception
     */
    private static void getPackageName(Node node) {
        NodeList list = node.getChildNodes();

        for (int i = 0; i < list.getLength(); i++) {
            Node childNode = list.item(i);
            String nodeText = childNode.getTextContent();
            String nodeName = childNode.getNodeName();
            if ((childNode.getNodeType() == Node.ELEMENT_NODE) && (nodeName.equals("class"))) {
                    getParentNodes(childNode);
                    pathsToActionsMap.put(nodeText, folderPath);
                    folderPath = "";
            }
            getPackageName(childNode);
        }
    }

    private static void getParentNodes(Node node){
        while(node!=null){
            node = node.getParentNode();
            if(node.getParentNode().getNodeType()==Node.DOCUMENT_NODE){
               break;
            }
            folderPath=node.getNodeName()+"/"+folderPath;
        }
    }

    private static boolean checkFileExists(String fileName) {
        Path file = Paths.get(fileName);
        return Files.exists(file);
    }

    public static void checkDirectoryExists(String path) throws IOException {
        Path dirPath = Paths.get(path);
        boolean dirExists;
        dirExists = Files.exists(dirPath);
        if (!dirExists) {
            Files.createDirectories(dirPath);
            log.info(String.format("Folder: %s was created successfully", folderPath));
        }
    }

    public static void copyDirectoryWithFilesFromTo(String scrDir,String destinationDir){
        File srcDir = new File(scrDir);
        File destDir = new File(destinationDir);
        //if dir is not exist, this dir will be created
        try {
            FileUtils.copyDirectory(srcDir, destDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * save data into Class file
     * @param fileName
     * @param data
     * @throws IOException
     */
    public static void saveFile(String Folder,  String fileName, String data) throws IOException {
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
     * update input file for special symbols
     * @throws IOException
     */
    public static void removeSpecialSymbols(String workflowPath) throws IOException {

        FileWriter fw = new FileWriter(workflowPath+"_new.xml");
        BufferedWriter WriteFileBuffer = new BufferedWriter(fw);
        String data = "";
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(workflowPath+".xml"), "Cp1252"))) {
            String line;
            while ((line = br.readLine()) != null) {
                data = line.replaceAll("([\u2022\u2013\u2014])","");
                WriteFileBuffer.write(data+"\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        WriteFileBuffer.close();
        FileUtils.deleteQuietly(new File(workflowPath+".xml"));
        new File(workflowPath+"_new.xml").renameTo(new File(workflowPath + ".xml"));
    }
}
