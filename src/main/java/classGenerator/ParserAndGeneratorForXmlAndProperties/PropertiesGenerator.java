package classGenerator.ParserAndGeneratorForXmlAndProperties;

import classGenerator.CommonParseActions;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PropertiesGenerator extends CommonParseActions {
    private static final Logger log = Logger.getLogger(PropertiesGenerator.class);


    /**
     * Append properties files with uuid:param={value}
     * @param filePath
     * @param fileName
     * @param uuid
     * @param paramListWithValues
     * @throws IOException
     */
    public void savePropertiesFile(String filePath, String fileName, UUID uuid, HashMap<String, String> paramListWithValues, String actionMeaningful) throws IOException {
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
                paramValue = paramValue.replace("$${EXTERNAL1.Host}", "${sut:R_EXTERNAL1/connectDetails/iP}");


                // variable rule converting
                if (paramValue.contains("$${")) {
                    if (paramValue.contains("SP$${")) {
                        paramValue = paramValue.replace("SP$${", "SP${");
                    } else {
                        paramValue = paramValue.replace("$${", "${run:");
                    }
                }

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
    public void savePropertiesFile(String filePath, String fileName, UUID uuid, String isDisabled) throws IOException {
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
    public void savePropertiesFileWithLoop(String filePath, String fileName, UUID uuid, ArrayList<String> loopData) throws IOException {
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

    public void savePropertiesFileWithDataDriven(String filePath, String fileName, UUID uuid, ArrayList<String> dataDrivenData) throws IOException {
        try {
            filePath = rootXMLFolder + "\\" + filePath;
            checkDirectoryExists(filePath);

            BufferedWriter WriteFileBuffer = new BufferedWriter(new FileWriter(filePath+fileName, true));
            // write specific configuration line

            if (!dataDrivenData.isEmpty()) {
                WriteFileBuffer.write(String.format("%s.File=%s\n", uuid, dataDrivenData.get(0)));
                WriteFileBuffer.write(String.format("%s.Parameter=%s\n", uuid, dataDrivenData.get(1)));
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
    public void savePropertiesFile(String filePath, String fileName, UUID uuid, ArrayList<String> params) throws IOException {
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

}
