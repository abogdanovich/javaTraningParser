/**
 * this script allow to extract 
 * all actions from old KB system and
 * save them into class files
 * @author bogdanovich_a
 */
package classGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author bogdanovich_a
 *
 */
public class Main {
	String fileName;
	static final Logger log = LogManager.getLogger(Main.class);
	
	Main(String fileName) {
		this.fileName = fileName;
	}
	
	/**
	 * generate action file with appropriate data
	 * @param actionName
	 * @param params
	 * @throws IOException
	 */
	public void generateActionFile(String actionName, ArrayList<String> params) throws IOException {
		String data = new String();
		
		data = 
			"/**\r\n" +
			"* "+actionName+" class with appropriate KB old actions\r\n"  +
			"* @author bogdanovich_a\r\n" +
			"*/\r\n" +
			"package automation.allot.com.Actions.KBsystem.jsystemActions."+actionName+";\r\n" +
			"\r\n" +
			"import static org.junit.Assert.*;\r\n" +
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
			"/**\r\n" +
			"* @author bogdanovich_a\r\n" +
			"*\r\n" +
			"*/\r\n" +
			"public class Configuration extends SystemTestCase4 {\r\n" +
			"\r\n" +
			"\t // class variables for the given Action\r\n";

		// put here all action params !
		for (int i=0; i < params.size(); i++) {
			data += String.format("\t String %s; \r\n ", params.get(i).toLowerCase());
		};

		data +=
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
			"\t @TestProperties(ParamsInclude = {";

		// put here all action params !
		for (int i=0; i < params.size(); i++) {
			if (i == params.size() - 1) {
				// pass comma into params list except the last element
				data += String.format("\"%s\"", params.get(i));
			} else {
				data += String.format("\"%s\", ", params.get(i));
			}
		}
		
		data += 
			"})\r\n" +
			"\t @Test\r\n" +
			"\t public void "+actionName+"() {\r\n" +
			"\r\n" +
			"\t\t // prepare parameters\r\n" +
			"\t\t List<KBParam> params = new ArrayList<>();\r\n";

		// put here all action params !
		for (int i=0; i < params.size(); i++) {
			data += String.format("\t\t params.add(new KBParam(\"%s\", %s, \"text\", \"\", false, false)); \r\n",
					params.get(i), params.get(i).toLowerCase());
		};
		
		data += 
		"\r\n" + 
		"\r\n" + 
		"\t\t // execute KB action\r\n" + 
		"\t\t automation.allot.com.Actions.KBsystem.QaAutomation." + actionName + " " + actionName.toLowerCase() +
				" =  new automation.allot.com.Actions.KBsystem.QaAutomation."
		+actionName+"(\"\", params);\r\n" + 
		"\t\t "+actionName.toLowerCase()+".run();\r\n" + 
		"\r\n" + 
		"\t\t if ("+actionName.toLowerCase()+".success() == true) {\r\n" + 
		"\t\t\t report.report(\"SUCCESS\", report.PASS);\r\n" + 
		"\t\t } else {\r\n" + 
		"\t\t\treport.report(\"FAILES\", report.FAIL);\r\n" + 
		"\t\t }\r\n" + 
		"\t } \r\n" + 
		"\r\n" + 
		"\t // actions getter\r\n";

		// generate all params getters
		for (int i=0; i < params.size(); i++) {
			data += String.format(
					"\r\n" + 
					"\t /**\r\n" + 
					"\t * \r\n" + 
					"\t * @return %s \r\n" + 
					"\t */\r\n" + 
					"\t public String get_%s() {\r\n" + 
					"\t\t return this.%s;\r\n" + 
					"\t }  \r\n" + 
					"\r\n"
					, params.get(i).toLowerCase(), params.get(i).toLowerCase(), params.get(i).toLowerCase());
		}

		data += "\t // actions setter\r\n";
		
		// generate all params setters
		for (int i=0; i < params.size(); i++) {
			data += String.format(
					"\r\n" + 
					"\t /**\r\n" + 
					"\t * \r\n" + 
					"\t * @param %s the %s set\r\n" + 
					"\t */\r\n" + 
					"\t public void set_%s(String %s) {\r\n" + 
					"\t\t this.%s = %s;\r\n" + 
					"\t }		\r\n"
					, params.get(i).toLowerCase(), params.get(i).toLowerCase(), params.get(i).toLowerCase(),
					params.get(i).toLowerCase(), params.get(i).toLowerCase(), params.get(i).toLowerCase());
		}

		data += "\r\n }";
		
		//write data into file
		this.saveClassFile(actionName, data);
	}
	
	
	/**
	 * save data into Class file 
	 * @param fileName
	 * @param data
	 * @throws IOException
	 */
	public void saveClassFile(String fileName, String data) throws IOException {
		try {
			FileWriter fw = new FileWriter("output\\" + fileName + ".java");
			BufferedWriter WriteFileBuffer = new BufferedWriter(fw);
			WriteFileBuffer.write(data);
			WriteFileBuffer.close();
			
		} finally {
			log.info(String.format("Class %s file saved", fileName));
		}
	}
	
	/**
	 * parse XML file and return all actions with params and values
	 * @param xmlNodeName
	 * @return
	 */
	public void parseActions(String xmlNodeName) {
		try {
			/* parse XML file structure */
			File xmlFile = new File(this.fileName);
			ArrayList<String> actionList = new ArrayList<String>();

			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(xmlFile);
			doc.getDocumentElement().normalize();

			// get KeyBlockScenario
			NodeList scenarios = doc.getElementsByTagName(xmlNodeName); 
			
			for (int i = 0; i < scenarios.getLength(); i++ ) {
				// get the list of child like> scenarioName and etc....
				NodeList testCase = scenarios.item(i).getChildNodes();
				
				// iterate child items
				for (int j = 0; j < testCase.getLength(); j++) {
					Node testCaseName = testCase.item(j);
					
					if (testCaseName.getNodeType() == Node.ELEMENT_NODE) {
						if (testCaseName.getNodeName().equals("scenarioName")) {
							// test case name from old KB system
							//log.debug(" TEST CASE :" + testCaseName.getTextContent());
						}

						// review each child
						NodeList steps = testCaseName.getChildNodes();
						
						for (int k = 0; k < steps.getLength(); k++) {
							if (steps.item(k).getNodeType() == Node.ELEMENT_NODE) {
								//node with elements 
								NodeList step = steps.item(k).getChildNodes();
								
									for (int p = 0; p < step.getLength(); p++) {
										//take here step name keyBlockGroupName
										NodeList stepName = step.item(p).getChildNodes();
										if (step.item(p).getNodeName().equals("keyBlockGroupName")) {
											// test case step
											//log.debug("STEP : " + step.item(p).getTextContent());
										}
										
										for (int st = 0; st < stepName.getLength(); st++) {
											ArrayList<String> paramList = new ArrayList<String>();
											String actionName = new String();
											
											if (stepName.item(st).getNodeType() == Node.ELEMENT_NODE) {
												NodeList params = stepName.item(st).getChildNodes();
												
													for (int pst = 0; pst < params.getLength(); pst++) {
														if (params.item(pst).getNodeName().equals("keyBlockName")) {
															// action from the XML file
															//log.debug("ACTION : " + params.item(pst).getTextContent());
															actionName = params.item(pst).getTextContent();
														} 
														
														if (params.item(pst).getNodeName().equals("keyBlockParams")) {
															Element element = (Element) params.item(pst);
															
															for (int elparam = 0; elparam < element.getElementsByTagName("paramName").getLength(); elparam++) {
																//log.info("PARAM : " + element.getElementsByTagName("paramName").item(elparam).getTextContent()
																//		+ " value: " + element.getElementsByTagName("paramValue").item(elparam).getTextContent());
																paramList.add(element.getElementsByTagName("paramName").item(elparam).getTextContent());
															} //end of action element
														
														} //end for if

														if (!actionName.equals("") && !paramList.isEmpty() && !actionList.contains(actionName)) {
															actionList.add(actionName);
															//log.info(String.format("actionName : %s and paramList %s", actionName, paramList));
															this.generateActionFile(actionName, paramList);
														}
													} //end for if
											} 
										} //end for step
									} // end of for
							} 
						} // end of for steps
					} //end of test case
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * main method for ActionClassGenerator
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String nodeName = "KeyBlockScenario";
		String fileName = "PCRF_Basic.xml";

		Main xmlParser = new Main(fileName);
		
		log.info("Converter is started to look for: " + nodeName + "node name");
		log.info("Input XML file is : " + fileName);
		log.info("Get started with parseActions");
		
		xmlParser.parseActions(nodeName);
	}
}