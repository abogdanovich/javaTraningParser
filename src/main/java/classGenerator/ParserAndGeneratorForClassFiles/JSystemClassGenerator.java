package classGenerator.ParserAndGeneratorForClassFiles;

import classGenerator.CommonParseActions;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

public class JSystemClassGenerator extends CommonParseActions {
    private static final Logger log = Logger.getLogger(JSystemClassGenerator.class);


    /**
     * generate action file with appropriate data and folder structure
     * @param actionName
     * @param params
     * @throws IOException
     */
    public void generateActionClass(String actionName, ArrayList<String> params) throws Exception {
        StringBuilder data = new StringBuilder();
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

        data.append(
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
                        "\t // class variables for the given Action\r\n");

        // put here all action params !
        for (String param : params) {
            data.append( String.format("\t private String %s = \"\"; \r\n ", param));
        }

        data.append(
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
                        "\t @TestProperties(paramsInclude = {");

        // put here all action params !
        for (int i=0; i < params.size(); i++) {
            if (i == params.size() - 1) {
                // pass comma into params list except the last element
                data.append(String.format("\"%s\"", params.get(i)));
            } else {
                data.append(String.format("\"%s\", ", params.get(i)));
            }
        }

        data.append(
                "})\r\n" +
                        "\t @Test\r\n" +
                        "\t public void " + actionName + "_KB() {\r\n" +
                        "\r\n" +
                        "\t\t // prepare parameters\r\n" +
                        "\t\t List<KBParam> params = new ArrayList<>();\r\n");

        // put here all action params !
        for (String param : params) {
            data.append(String.format("\t\t params.add(new KBParam(\"%s\", %s, \"text\", \"\", false, false)); \r\n",
                    param, param));
        }

        data.append(
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
                        "\t // actions getter\r\n");

        // generate all params getters
        for (String param1 : params) {
            data.append(String.format(
                    "\r\n" +
                            "\t /**\r\n" +
                            "\t * \r\n" +
                            "\t * @return %s \r\n" +
                            "\t */\r\n" +
                            "\t public String get%s() {\r\n" +
                            "\t\t return this.%s;\r\n" +
                            "\t } \r\n"
                    , param1, param1, param1));
        }

        data.append("\t // actions setter\r\n");

        // generate all params setters
        for (String param : params) {
            data.append(String.format(
                    "\r\n" +
                            "\t /**\r\n" +
                            "\t * \r\n" +
                            "\t * @param %s the %s set\r\n" +
                            "\t */\r\n" +
                            "\t public void set%s(String %s) {\r\n" +
                            "\t\t this.%s = %s;\r\n" +
                            "\t } \r\n"
                    , param, param, param,
                    param, param, param));
        }

        data.append("\r\n }");

        //write data into file
        saveFile(rootClassFolder + folderPath, actionNameOriginal + ".java", data.toString());
    }
}
