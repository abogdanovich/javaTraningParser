package classGenerator.ParserAndGeneratorForClassFiles;

import classGenerator.CommonParseActions;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class ParserForClass extends CommonParseActions {
    private static final Logger log = Logger.getLogger(ParserForClass.class);
    private static final ArrayList<String> actionList = new ArrayList<>();
    private static final ArrayList<ArrayList<String>> paramList = new ArrayList<>();

    private JSystemClassGenerator jSystemClassGenerator;

    public ParserForClass(String rootClassFolder) {
//        this.rootClassFolder = rootClassFolder + "_class";
        this.rootClassFolder = rootClassFolder;
        this.jSystemClassGenerator = new JSystemClassGenerator();
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
           // String nodeText = childNode.getTextContent();
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
                    ArrayList<String> paramListElement = new ArrayList<>();
                    for (int k = 0; k < element.getElementsByTagName("paramName").getLength(); k++) {
                        // add a new param name
                        paramListElement.add(element.getElementsByTagName("paramName").item(k).getTextContent());

                        switch (element.getElementsByTagName("paramValueType").item(k).getTextContent()) {
                            case "file":
                                // import java.io.File;
                                paramListElement.add("File");
                                break;
//                            case "list":
//                                paramListElement.add("enum");
//                                break;
                            default:
                                paramListElement.add("String");
                                break;
                        }
                        // add param-type into array
                        paramList.add(paramListElement);
                    }
                    break;
                case "keyBlockRepeatCount":
                    // generate class file
                    if (!paramList.isEmpty() && !actionName.equals("")) {
                        jSystemClassGenerator.generateActionClass(actionName, paramList);
                        paramList.clear();
                    }
                    break;
            }
            parseKBActions(childNode);
        }
    }
}
