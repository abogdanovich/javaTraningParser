package classGenerator.ParserAndGeneratorForXmlAndProperties;

import classGenerator.CommonParseActions;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

public class XmlGenerator extends CommonParseActions {
    private static final Logger log = Logger.getLogger(XmlGenerator.class);


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
     * method take params and create xml workflow files for test cases and test steps with actions
     * @param filePath
     * @param fileName
     * @param actionsWithUUID
     * @param scenarioWithActions
     * @throws Exception
     */
    public void generateJSystemWorkflow(String filePath,
                                        String fileName,
                                        ArrayList<ArrayList<String>> actionsWithUUID,
                                        boolean scenarioWithActions,
                                        String testCase,
                                        ArrayList<ArrayList<String>> loopData,
                                        String dataDriven) throws Exception {
        StringBuilder data = new StringBuilder();

        //create list without 'loop_enabled' lists
        ArrayList<ArrayList<String>> listWithoutLoop = (ArrayList) actionsWithUUID.stream().filter(k -> !k.get(1).equals("loop_enabled")).collect(Collectors.toList());

        data.append(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<!--This file was generated by converting script. Developers: Aliaksandr Bahdanovich, Vadim Chiritsa for JSystem runner, " +
                        "do not change it manually--><project default=\"execute scenario\" name=\"/" + smpTestPath +
                        workflowPath + "/" + testCase + "/" + fileName + "\">\r\n" + XML_HEADER);

        filePath = rootXMLFolder + "\\" + filePath;

        // pass UUID data driven via String param
        if (!dataDriven.equals("")) {
            // data driven case
            data.append("\t\t<jsystemdatadriven delimiter=\";\" fullUuid=\"${jsystem.parent.uuid}.${jsystem.uuid}."+ dataDriven +"\" parentName=\"${jsystem.parent.name}.${ant.project.name}\">\n" +
                    "\t\t\t<!--#Jsystem#-->\n" +
                    "\t\t\t<sequential>\n" +
                    "\t\t\t\t<echo message=\"Data Driven\"/>\n" +
                    "\t\t\t\t\t<jsystemsetantproperties>\n" +
                    "\t\t\t\t\t\t<!--Task for updating the ant parameters file - used for reference parameters-->\n" +
                    "\t\t\t\t\t</jsystemsetantproperties>\n");

            for (int i = 0; i < actionsWithUUID.size(); i++) {
                data.append(String.format("\t\t<antcallback target=\"t%s\"/>\n", i));
            }

            data.append("\t\t\t</sequential>\n" +
                        "\t\t</jsystemdatadriven>\n");

        } else {
            int indexForListWithoutLoop = 0;
            int indexForLoopData = 0;
            for (int i = 0; i < actionsWithUUID.size(); i++) {
                // not a data driven case
                if ((i < actionsWithUUID.size() - 1) && (actionsWithUUID.get(i + 1).get(1).equals("loop_enabled"))) {
                    data.append("\t\t<jsystemfor delimiter=\";\" fullUuid=\"${jsystem.parent.uuid}." +
                            "${jsystem.uuid}." + actionsWithUUID.get(i + 1).get(0) + "\" list=\"a;b;c;d\" " +
                            "param=\"myVar\" parentName=\"${jsystem.parent.name}.${ant.project.name}\">\n" +
                            "\t\t\t<!--#Jsystem#-->\n" +
                            "\t\t\t<sequential>\n" +
                            "\t\t\t\t<echo message=\"Parameter: index=@{" + loopData.get(indexForLoopData).get(0) + "}\"/>\n" +
                            "\t\t\t\t\t<var name=\"" + loopData.get(indexForLoopData).get(0) + "\" value=\"@{" + loopData.get(indexForLoopData).get(0) + "}\"/>\n" +
                            "\t\t\t\t\t<jsystemsetantproperties>\n" +
                            "\t\t\t\t\t\t<!--Task for updating the ant parameters file - used for reference parameters-->\n" +
                            "\t\t\t\t\t</jsystemsetantproperties>\n" +
                            "\t\t\t\t\t<antcallback target=\"t" + indexForListWithoutLoop + "\"/>\n" +
                            "\t\t\t</sequential>\n" +
                            "\t\t\t</jsystemfor>\n");
                    indexForLoopData++;
                    continue;
                } else {
                    if (!actionsWithUUID.get(i).get(1).equals("loop_enabled")) {
                        data.append(String.format("\t\t<antcallback target=\"t%s\"/>\r\n", indexForListWithoutLoop));
                    }
                    indexForListWithoutLoop++;
                }
            }
        }

        data.append("\t</target>\r\n");

        // actionsWithUUID[UUID, actionName]
        if (scenarioWithActions) {
            // we generate XML file with references JSystem Actions
            for (int i = 0; i < listWithoutLoop.size(); i++) {

                String packagePath = "";
                packagePath = pathsToActionsMap.getOrDefault(listWithoutLoop.get(i).get(1), "General");
                packagePath = packagePath.replace("/", ".");
                packagePath = packagePath.substring(0, packagePath.length() - 1);

                data.append(String.format(
                        "\t<target name=\"t%s\">\r\n" +
                                "\t\t<jsystem showoutput=\"true\">\r\n" +
                                "\t\t\t<sysproperty key=\"jsystem.uuid\" value=\"%s\"/>\r\n" +
                                "\t\t\t<sysproperty key=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\r\n" +
                                "\t\t\t<sysproperty key=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\r\n" +
                                "\t\t\t<test name=\"automation.allot.com.Actions.KBsystem.jsystemActions." + packagePath + ".%s.%s_KB\"/>\r\n" +
                                "\t\t</jsystem>\r\n" +
                                "\t</target>\r\n", i, listWithoutLoop.get(i).get(0), listWithoutLoop.get(i).get(1), listWithoutLoop.get(i).get(1)));
            }
        } else {
            // we generate XML file with references to XML files with JSystem Actions
            for (int i = 0; i < listWithoutLoop.size(); i++) {
                data.append(String.format(
                        "\t<target name=\"t%s\">\n" +
                                "\t\t<jsystem-ant antfile=\"${scenarios.base}/" + smpTestPath + workflowPath + "/" + testCase + "/%s.xml\">\n" +
                                "\t\t\t<property name=\"jsystem.uuid\" value=\"%s\"/>\n" +
                                "\t\t\t<property name=\"jsystem.parent.uuid\" value=\"${jsystem.parent.uuid}.${jsystem.uuid}\"/>\n" +
                                "\t\t\t<property name=\"jsystem.parent.name\" value=\"${jsystem.parent.name}.${ant.project.name}\"/>\n" +
                                "\t\t</jsystem-ant>\n" +
                                "\t</target>", i, listWithoutLoop.get(i).get(1), listWithoutLoop.get(i).get(0)));
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
    public void generateJSystemWorkflow(String filePath, String fileName, String uuid, String testCase) throws Exception {
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
}
