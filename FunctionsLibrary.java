package com.webapp.baseLibrary;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.UnreachableBrowserException;
import org.openqa.selenium.support.Color;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Function;
import com.jacob.com.LibraryLoader;
import com.thoughtworks.selenium.SeleniumException;
import com.webapp.executioner.ExecutionerClass;
import com.webapp.stepDefinition.CommonStepDefinitions;
import com.webapp.utilities.ApplicationPropertiesInitializer;
import com.webapp.utilities.GridReporter;

import autoitx4java.AutoItX;

public class FunctionsLibrary 
{
	public String tc_id= null;
	public String scenarioName=null;

	public GridReporter reporter = CommonStepDefinitions.getReporter();
	public String strReportFilename=CommonStepDefinitions.strReportFilename;
	public static WebDriver driver;
	public static ApplicationPropertiesInitializer applicationProperties = null; 
	static Properties object,browserLoad = null;
	static WebDriverWait wait = LaunchLogin.wait;
	static WebDriverWait wait1 = LaunchLogin.wait1;
	private static Map<String, String> envVariableMap = new HashMap();
	static String sysPropFromFile = "Config/Sys.properties";
	int getWaitTime = 0;
	int elementCheckCount = 3;

	public FunctionsLibrary()
	{
		driver =new  LaunchLogin().getDriver();
		//driver.manage().window().maximize();
		tc_id=CommonStepDefinitions.getTCId();
		scenarioName=CommonStepDefinitions.scenarioName;

		applicationProperties = new ApplicationPropertiesInitializer();
		String objectFileName = "src/test/resources/config/object.properties";
		object = loadPropertiesFile(objectFileName);
		getWaitTime = getFluentWaitTime();

		wait = new WebDriverWait(driver, getWaitTime);

	}
	/**
	 * To load properties from application.properties file
	 * @param propFilePath
	 * @return
	 */
	public static Properties loadPropertiesFile(String propFilePath) {
		Properties properties = null;
		try {
			properties = new Properties();
			InputStream fis = new FileInputStream(propFilePath);
			properties.load(fis);
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return properties;
	}

	public String[] readProperties(String locatorKey) {
		String [] locatorMethodName = null;
		try {
			locatorKey=locatorKey.replace(" ", "").replace(":", "");
			String objectValue = object.getProperty(locatorKey);
			locatorMethodName = objectValue.split("#");
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Read Property File and Fetch Locator Key "+locatorKey, locatorKey, e.getMessage(), "Fail", strReportFilename);
		}
		return locatorMethodName;
	}
	
	public void loadjacobdll()
	{
		String jacobDllVersionToUse;
		if(System.getProperty("sun.arch.data.model").contains("32"))	
		{
			jacobDllVersionToUse = "jacob-1.14.3-x86.dll";
		}
		else {
			jacobDllVersionToUse = "jacob-1.14.3-x64.dll";
		}
		File file = new File("jacobdll", jacobDllVersionToUse);
		System.setProperty(LibraryLoader.JACOB_DLL_PATH, file.getAbsolutePath());
	}

	public WebElement getElementFluentWait(String elementAddress) {
		if(this.driver==null)
			return null;
		String locatorMethod = null;
		String locatorValue = null;
		try {
			String[] locatorMethodName = readProperties(elementAddress);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			reporter.writeStepResult(tc_id, scenarioName, "Fetch LocatorMethod and LocatorValue for " + elementAddress,
					"LocatorMethod: " + locatorMethod + ";" + "LocatorValue: " + locatorValue, e.getMessage(), "Fail",
					strReportFilename);
		}
		final String eleAddress = locatorValue;
		final WebDriver driver = this.driver;
		final String locator = locatorMethod;
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver);
		WebElement returnEle = null;

		wait.withTimeout(getWaitTime, TimeUnit.SECONDS)
		.pollingEvery(5, TimeUnit.SECONDS)
		.ignoring(NoSuchElementException.class);
		WebElement element = null;
		int elementCheck = 1;
		while (element == null && elementCheck <=elementCheckCount) {
			try {
				element = wait.until(new Function<WebDriver, WebElement>() {

					public WebElement apply(WebDriver driver) {

						WebElement ele = null;

						switch (locator) {
						case "id":
							ele = driver.findElement(By.id((eleAddress)));
							break;
						case "name":
							ele = driver.findElement(By.name((eleAddress)));
							break;
						case "class":
							ele = driver.findElement(By.className((eleAddress)));
							break;
						case "linkText":
							ele = driver.findElement(By.linkText((eleAddress)));
							break;
						case "partiallinkText":
							ele = driver.findElement(By.partialLinkText((eleAddress)));
							break;
						case "tagname":
							ele = driver.findElement(By.tagName((eleAddress)));
							break;
						case "css":
							ele = driver.findElement(By.cssSelector((eleAddress)));
							break;
						case "xpath":
							ele = driver.findElement(By.xpath((eleAddress)));
							break;

						default:
							break;
						}
						if (!elementAddress.toLowerCase().contains("frame")) 							
							CommonStepDefinitions.exceptioncounter=0;
						highlightElement(ele);
						if (ele != null) {
							//System.out.println("Element located in DOM successfully.. with FluentWait");
							return ele;

						} else {
							/*System.out.println("Element not located in DOM for " + eleAddress
									+ " with FluentWait, Returning null value");*/

							return null;
						}
					}
				});
				//System.out.println("returning " + element + " for " + eleAddress);

			} catch (UnreachableBrowserException e1) {
				CommonStepDefinitions.executeScenario = false;
				this.driver.quit();
				this.driver=null;
				LaunchLogin.driver=null;
				LaunchLogin.browserLaunched=false;
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"Driver instance closed successfully ...!!!");
				reporter.writeStepResult(tc_id, scenarioName,
						"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
						"The Locator Method and Value should be available in the page", "The Element is not available and Execution is Not Completed for this TestCase", "Fail",
						strReportFilename);
				try {
						Runtime.getRuntime().exec("taskKill /IM IEDriverServer.exe /F");						
						Runtime.getRuntime().exec("taskKill /IM WerFault.exe /F");
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"IEDriverServer killed successfully ...!!!");
					} catch (org.apache.commons.exec.ExecuteException e) {				
						e.printStackTrace();
					} catch (IOException e) {					
						e.printStackTrace();
					}
			}
			catch (TimeoutException te) {
				ExceptionUtils.getFullStackTrace(te);
				String[] locatorMethodNameSE = readProperties("ServerError");
				if (driver.findElements(By.xpath(locatorMethodNameSE[1])).size()>0) {
					reporter.writeStepResult(tc_id, scenarioName,
							"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
							"The Locator Method and Value should be available in the page", "The Element is not available and Execution is Not Completed for this TestCase", "Fail",
							strReportFilename);
					CommonStepDefinitions.executeScenario = false;
					driver.quit();
					this.driver=null;
					LaunchLogin.driver=null;
					LaunchLogin.browserLaunched=false;
				}
				else { 
					if (elementCheck == elementCheckCount) {
						CommonStepDefinitions.exceptioncounter++;
						reporter.writeStepResult(tc_id, scenarioName,
								"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
								"The Locator Method and Value should be available in the page", te.getMessage(), "Fail",
								strReportFilename);
					}
					if (CommonStepDefinitions.exceptioncounter==3) {
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
						System.out.println(java.time.LocalDateTime.now()+"-:: "+ExceptionUtils.getFullStackTrace(te));
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
						
						reporter.writeStepResult(tc_id, scenarioName,
								"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
								"The Locator Method and Value should be available in the page", "The Element is not available and Execution is Not Completed for this TestCase", "Fail",
								strReportFilename);
						CommonStepDefinitions.executeScenario = false;
						driver.quit();
						this.driver=null;
						LaunchLogin.driver=null;
						LaunchLogin.browserLaunched=false;
						
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"Driver instance closed successfully ...!!!");
						
						try {
							Runtime.getRuntime().exec("taskKill /IM IEDriverServer.exe /F");
							
							Runtime.getRuntime().exec("taskKill /IM WerFault.exe /F");
							
							System.out.println(java.time.LocalDateTime.now()+"-:: "+"IEDriverServer killed successfully ...!!!");
							return element;
						} catch (org.apache.commons.exec.ExecuteException e) {
							e.printStackTrace();
						} catch (IOException e) {							
							e.printStackTrace();
						}
					}
				} 
			} catch (Exception e) {
				if (elementCheck == elementCheckCount) {
					System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
					System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: ============================================ :: ==========================");
					System.out.println(java.time.LocalDateTime.now()+"-:: "+ExceptionUtils.getFullStackTrace(e));
					e.printStackTrace();
					reporter.writeStepResult(tc_id, scenarioName,
							"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
							"The Locator Method and Value should be available in the page", e.getMessage(), "Fail",
							strReportFilename);
				}
				globalWait();
			}
			elementCheck++;
		}
		return element;
	}
	public WebElement getElementFluentWait(String locatorMethod, String elementAddress) {

		final String eleAddress = elementAddress;
		final WebDriver driver = this.driver;
		final String locator = locatorMethod;
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver);
		wait.withTimeout(getWaitTime, TimeUnit.SECONDS)
		.pollingEvery(5, TimeUnit.SECONDS)
		.ignoring(NoSuchElementException.class);
		WebElement element = null;
		int elementCheck = 1;

		while (element == null && elementCheck <= elementCheckCount) {
			try {
				element = wait.until(new Function<WebDriver, WebElement>() {
					public WebElement apply(WebDriver driver) {
						WebElement ele = null;
						switch (locator) {
						case "id":
							ele = driver.findElement(By.id((eleAddress)));
							break;
						case "name":
							ele = driver.findElement(By.name((eleAddress)));
							break;
						case "class":
							ele = driver.findElement(By.className((eleAddress)));
							break;
						case "linkText":
							ele = driver.findElement(By.linkText((eleAddress)));
							break;
						case "partiallinkText":
							ele = driver.findElement(By.partialLinkText((eleAddress)));
							break;
						case "tagname":
							ele = driver.findElement(By.tagName((eleAddress)));
							break;
						case "css":
							ele = driver.findElement(By.cssSelector((eleAddress)));
							break;
						case "xpath":
							ele = driver.findElement(By.xpath((eleAddress)));
							break;

						default:
							break;
						}
						CommonStepDefinitions.exceptioncounter=0;
						highlightElement(ele);
						if (ele != null) {
							//System.out.println("Element located in DOM successfully.. with FluentWait");
							return ele;

						} else {
							/*System.out.println("Element not located in DOM for " + eleAddress
									+ " with FluentWait, Returning null value");*/
							return null;
						}
					}
				});
			} catch (UnreachableBrowserException e1) {
				CommonStepDefinitions.executeScenario = false;
				this.driver.quit();
				this.driver=null;
				LaunchLogin.driver=null;
				LaunchLogin.browserLaunched=false;
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"Driver instance closed successfully ...!!!");
				reporter.writeStepResult(tc_id, scenarioName,
						"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
						"The Locator Method and Value should be available in the page", "The Element is not available and Execution is Not Completed for this TestCase", "Fail",
						strReportFilename);
				try {
						Runtime.getRuntime().exec("taskKill /IM IEDriverServer.exe /F");						
						Runtime.getRuntime().exec("taskKill /IM WerFault.exe /F");
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"IEDriverServer killed successfully ...!!!");
					} catch (org.apache.commons.exec.ExecuteException e) {				
						e.printStackTrace();
					} catch (IOException e) {					
						e.printStackTrace();
					}
			}
			catch (TimeoutException te) {
				ExceptionUtils.getFullStackTrace(te);
				String[] locatorMethodNameSE = readProperties("ServerError");
				if (driver.findElements(By.xpath(locatorMethodNameSE[1])).size()>0) {
					reporter.writeStepResult(tc_id, scenarioName,
							"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
							"The Locator Method and Value should be available in the page", "The Element is not available and Execution is Not Completed for this TestCase", "Fail",
							strReportFilename);
					CommonStepDefinitions.executeScenario = false;
					driver.quit();
					this.driver=null;
					LaunchLogin.driver=null;
					LaunchLogin.browserLaunched=false;
				}
				else { 
					if (elementCheck == elementCheckCount) {
						CommonStepDefinitions.exceptioncounter++;
						reporter.writeStepResult(tc_id, scenarioName,
								"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
								"The Locator Method and Value should be available in the page", te.getMessage(), "Fail",
								strReportFilename);
					}
					if (CommonStepDefinitions.exceptioncounter==3) {
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
						System.out.println(java.time.LocalDateTime.now()+"-:: "+ExceptionUtils.getFullStackTrace(te));
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
						
						reporter.writeStepResult(tc_id, scenarioName,
								"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
								"The Locator Method and Value should be available in the page", "The Element is not available and Execution is Not Completed for this TestCase", "Fail",
								strReportFilename);
						CommonStepDefinitions.executeScenario = false;
						driver.quit();
						this.driver=null;
						LaunchLogin.driver=null;
						LaunchLogin.browserLaunched=false;
						
						System.out.println(java.time.LocalDateTime.now()+"-:: "+"Driver instance closed successfully ...!!!");
						
						try {
							Runtime.getRuntime().exec("taskKill /IM IEDriverServer.exe /F");
							
							Runtime.getRuntime().exec("taskKill /IM WerFault.exe /F");
							
							System.out.println(java.time.LocalDateTime.now()+"-:: "+"IEDriverServer killed successfully ...!!!");
						} catch (org.apache.commons.exec.ExecuteException e) {
							e.printStackTrace();
						} catch (IOException e) {							
							e.printStackTrace();
						}
					}
				} 
			} catch (Exception e) {
				if (elementCheck == elementCheckCount) {
					System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
					System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: ============================================ :: ==========================");
					System.out.println(java.time.LocalDateTime.now()+"-:: "+ExceptionUtils.getFullStackTrace(e));
					e.printStackTrace();
					reporter.writeStepResult(tc_id, scenarioName,
							"Find Element By " + locatorMethod + " with Value " + eleAddress + " in the webpage",
							"The Locator Method and Value should be available in the page", e.getMessage(), "Fail",
							strReportFilename);
				}
				globalWait();
			}
			elementCheck++;
		}
		return element;
	}
	
	public WebElement getWebElementWithQuickWait(String elementAddress) throws InterruptedException {
		if(this.driver==null)
			return null;
		
		int defaultTimeOut=5;		
		String locatorMethod = null;
		String locatorValue = null;
		try {
			String[] locatorMethodName = readProperties(elementAddress);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			reporter.writeStepResult(tc_id, scenarioName, "Fetch LocatorMethod and LocatorValue for " + elementAddress,
					"LocatorMethod: " + locatorMethod + ";" + "LocatorValue: " + locatorValue, e.getMessage(), "Fail",
					strReportFilename);
		}

		final String eleAddress = locatorValue;
		final WebDriver driver = this.driver;
		final String locator = locatorMethod;
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver);
		WebElement returnEle = null;
		wait.withTimeout(defaultTimeOut, TimeUnit.SECONDS);
		wait.pollingEvery(2, TimeUnit.SECONDS);
		wait.ignoring(NoSuchElementException.class);
		WebElement element = null;
		int elementCheck = 1;

		while (element == null && elementCheck <= elementCheckCount) {
			try {
				element = wait.until(new Function<WebDriver, WebElement>() {

					public WebElement apply(WebDriver driver) {

						WebElement ele = null;

						switch (locator) {
						case "id":
							ele = driver.findElement(By.id((eleAddress)));

							break;
						case "name":
							ele = driver.findElement(By.name((eleAddress)));
							break;
						case "class":
							ele = driver.findElement(By.className((eleAddress)));
							break;
						case "linkText":
							ele = driver.findElement(By.linkText((eleAddress)));
							break;
						case "partiallinkText":
							ele = driver.findElement(By.partialLinkText((eleAddress)));
							break;
						case "tagname":
							ele = driver.findElement(By.tagName((eleAddress)));
							break;
						case "css":
							ele = driver.findElement(By.cssSelector((eleAddress)));
							break;
						case "xpath":
							ele = driver.findElement(By.xpath((eleAddress)));
							break;

						default:
							break;
						}
						CommonStepDefinitions.exceptioncounter = 0;
						// highlightElement(ele);
						if (ele != null) {					
							return ele;
						} else {					
							return null;
						}
					}
				});
		} catch (TimeoutException te) {
			ExceptionUtils.getFullStackTrace(te);
			if (elementCheck == elementCheckCount) {
				CommonStepDefinitions.exceptioncounter++;
			}
			if (elementCheck == elementCheckCount) {
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
				System.out.println(java.time.LocalDateTime.now()+"-:: "+ExceptionUtils.getFullStackTrace(te));
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");		
			}

		} catch (Exception e) {
			if (elementCheck == elementCheckCount) {
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
				
				ExceptionUtils.getFullStackTrace(e);
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: ============================================ :: ==========================");
				System.out.println(java.time.LocalDateTime.now()+"-:: "+e.getStackTrace());
			}
		}

		elementCheck++;
	}
	return element;
	}

	public WebElement getWebElementWithCustomWaitAndIteration(String elementAddress, int waitTime, int iteration) throws InterruptedException {
		if(this.driver==null)
			return null;
		
		int defaultTimeOut=waitTime;		
		String locatorMethod = null;
		String locatorValue = null;
		try {
			String[] locatorMethodName = readProperties(elementAddress);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			reporter.writeStepResult(tc_id, scenarioName, "Fetch LocatorMethod and LocatorValue for " + elementAddress,
					"LocatorMethod: " + locatorMethod + ";" + "LocatorValue: " + locatorValue, e.getMessage(), "Fail",
					strReportFilename);
		}

		final String eleAddress = locatorValue;
		final WebDriver driver = this.driver;
		final String locator = locatorMethod;
		FluentWait<WebDriver> wait = new FluentWait<WebDriver>(driver);
		WebElement returnEle = null;
		wait.withTimeout(defaultTimeOut, TimeUnit.SECONDS);
		wait.pollingEvery(2, TimeUnit.SECONDS);
		wait.ignoring(NoSuchElementException.class);
		WebElement element = null;
		int elementCheck = 1;

		while (element == null && elementCheck <= iteration) {
			try {
				element = wait.until(new Function<WebDriver, WebElement>() {

					public WebElement apply(WebDriver driver) {

						WebElement ele = null;

						switch (locator) {
						case "id":
							ele = driver.findElement(By.id((eleAddress)));

							break;
						case "name":
							ele = driver.findElement(By.name((eleAddress)));
							break;
						case "class":
							ele = driver.findElement(By.className((eleAddress)));
							break;
						case "linkText":
							ele = driver.findElement(By.linkText((eleAddress)));
							break;
						case "partiallinkText":
							ele = driver.findElement(By.partialLinkText((eleAddress)));
							break;
						case "tagname":
							ele = driver.findElement(By.tagName((eleAddress)));
							break;
						case "css":
							ele = driver.findElement(By.cssSelector((eleAddress)));
							break;
						case "xpath":
							ele = driver.findElement(By.xpath((eleAddress)));
							break;

						default:
							break;
						}
						CommonStepDefinitions.exceptioncounter = 0;
						// highlightElement(ele);
						if (ele != null) {
							return ele;
						} else {
							return null;

						}
					}
				});
		} catch (TimeoutException te) {
			ExceptionUtils.getFullStackTrace(te);
			if (elementCheck == elementCheckCount) {	
				CommonStepDefinitions.exceptioncounter++;				
			}
			if (elementCheck == elementCheckCount) {
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
				System.out.println(java.time.LocalDateTime.now()+"-:: "+ExceptionUtils.getFullStackTrace(te));
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");				
			}
		} catch (Exception e) {
			if (elementCheck == elementCheckCount) {
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: Handled Full Stack Trace Information :: ==========================");
				
				ExceptionUtils.getFullStackTrace(e);
				System.out.println(java.time.LocalDateTime.now()+"-:: "+"====================== :: ============================================ :: ==========================");
				System.out.println(java.time.LocalDateTime.now()+"-:: "+e.getStackTrace());
			}
		}
		elementCheck++;
	}
	return element;
	}
	
	public static int getFluentWaitTime() {
		String selectedEnvironment = loadPropertiesFile(sysPropFromFile).getProperty("Execution_Environment");
		String fluentWaitTime = "30";
		switch (selectedEnvironment) {
		case "SandBox":
			fluentWaitTime = loadPropertiesFile(sysPropFromFile).getProperty("SandBoxEnvWaitTime");
			break;
		case "QA":
			fluentWaitTime = loadPropertiesFile(sysPropFromFile).getProperty("QAEnvWaitTime");
			break;
		case "Dev":
			fluentWaitTime = loadPropertiesFile(sysPropFromFile).getProperty("DevEnvWaitTime");
			break;
		case "Production":
			fluentWaitTime = loadPropertiesFile(sysPropFromFile).getProperty("ProductionEnvWaitTime");
			break;
		case "INT":
			fluentWaitTime = loadPropertiesFile(sysPropFromFile).getProperty("INTEnvWaitTime");
			break;
		case "Staging":
			fluentWaitTime = loadPropertiesFile(sysPropFromFile).getProperty("StagingEnvWaitTime");
			break;
		default:
			break;
		}
		//System.out.println("Selected Env is : " + selectedEnvironment + " and wait time is: " + fluentWaitTime);
		// String fluentWaitTime =
		// loadPropertiesFile(sysPropFromFile).getProperty("FluentWaitTime");
		// System.out.println("Global Fluent Wait.......................");
		return Integer.parseInt(fluentWaitTime);
	}

	public void globalWait(String... waitTime) {

		String globalWaitTime = "1";
		if (waitTime.length > 0) {
			globalWaitTime = waitTime[0];
		}
		globalWaitTime = loadPropertiesFile(sysPropFromFile).getProperty("GlobalWaitTime");
		int inputTime = Integer.parseInt(globalWaitTime) * 1000;
		try {
			Thread.sleep(inputTime);
			// System.out.println("Global Wait.......................");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// return inputTime;
	}

	public WebElement getWebElement(String locatorKey){
		WebElement element=null;
		String locatorMethod=null;
		String locatorValue=null;
		try {
			String [] locatorMethodName = readProperties(locatorKey);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Fetch LocatorMethod and LocatorValue for "+locatorKey, "LocatorMethod: "+locatorMethod+";"+"LocatorValue: "+locatorValue, e.getMessage(), "Fail", strReportFilename);
		}
		try {
			switch(locatorMethod) {
			case "id":
				element = driver.findElement(By.id((locatorValue)));
				break;
			case "name":
				element = driver.findElement(By.name((locatorValue)));
				break;
			case "class": 
				element = driver.findElement(By.className((locatorValue)));
				break;
			case "linkText":
				element = driver.findElement(By.linkText((locatorValue)));
				break;
			case "partiallinkText":
				element = driver.findElement(By.partialLinkText((locatorValue)));
				break;
			case "tagname":
				element = driver.findElement(By.tagName((locatorValue)));
				break;
			case "css":
				element = driver.findElement(By.cssSelector((locatorValue)));
				break;
			case "xpath": 
				element = driver.findElement(By.xpath((locatorValue)));
				break;

			default: break;
			}
			highlightElement(element);
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Find Element By "+locatorMethod+" with Value "+locatorValue+ " in the webpage", "The Locator Method and Value should be available in the page", e.getMessage() ,"Fail", strReportFilename);
		}
		return element; 
	}

	public WebElement getWebElementWithWait(String locatorKey){
		WebElement element=null;
		String locatorMethod=null;
		String locatorValue=null;
		try {
			String [] locatorMethodName = readProperties(locatorKey);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Fetch LocatorMethod and LocatorValue for "+locatorKey, "LocatorMethod: "+locatorMethod+";"+"LocatorValue: "+locatorValue, e.getMessage(), "Fail", strReportFilename);
		}
		try {
			switch(locatorMethod) {
			case "id":
				wait.until(ExpectedConditions.presenceOfElementLocated(By.id(locatorValue)));
				element = driver.findElement(By.id((locatorValue)));
				break;
			case "name":
				wait.until(ExpectedConditions.presenceOfElementLocated(By.name(locatorValue)));
				element = driver.findElement(By.name((locatorValue)));
				break;
			case "class": 
				wait.until(ExpectedConditions.presenceOfElementLocated(By.className(locatorValue)));
				element = driver.findElement(By.className((locatorValue)));
				break;
			case "linkText":
				wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText(locatorValue)));
				element = driver.findElement(By.linkText((locatorValue)));
				break;
			case "partiallinkText":
				wait.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText(locatorValue)));
				element = driver.findElement(By.partialLinkText((locatorValue)));
				break;
			case "tagname":
				wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName(locatorValue)));
				element = driver.findElement(By.tagName((locatorValue)));
				break;
			case "css":
				wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(locatorValue)));
				element = driver.findElement(By.cssSelector((locatorValue)));
				break;
			case "xpath": 
				wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locatorValue)));
				element = driver.findElement(By.xpath((locatorValue)));
				break;

			default: break;
			}
			highlightElement(element);
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Find Element By "+locatorMethod+" with Value "+locatorValue+ " in the webpage", "The Locator Method and Value should be available in the page", e.getMessage() ,"Fail", strReportFilename);
		}
		return element; 
	}

	public List<WebElement> getWebElements(String locatorKey){
		List<WebElement> element=null;
		String locatorMethod=null;
		String locatorValue=null;
		try {
			String [] locatorMethodName = readProperties(locatorKey);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Fetch LocatorMethod and LocatorValue for "+locatorKey, "LocatorMethod: "+locatorMethod+";"+"LocatorValue: "+locatorValue, e.getMessage(), "Fail", strReportFilename);
		}
		try {
			switch(locatorMethod) {
			case "id":
				element = driver.findElements(By.id((locatorValue)));
				break;
			case "name":
				element = driver.findElements(By.name((locatorValue)));
				break;
			case "class": 
				element = driver.findElements(By.className((locatorValue)));
				break;
			case "linkText":
				element = driver.findElements(By.linkText((locatorValue)));
				break;
			case "partiallinkText":
				element = driver.findElements(By.partialLinkText((locatorValue)));
				break;
			case "tagname":
				element = driver.findElements(By.tagName((locatorValue)));
				break;
			case "css":
				element = driver.findElements(By.cssSelector((locatorValue)));
				break;
			case "xpath": 
				element = driver.findElements(By.xpath((locatorValue)));
				break;

			default: break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Find Element By "+locatorMethod+" with Value "+locatorValue+ " in the webpage", "The Locator Method and Value should be available in the page", e.getMessage() ,"Fail", strReportFilename);
		}
		if (element.size()==0) {
			reporter.writeStepResult(tc_id,scenarioName, "Find Elements By '"+locatorMethod+"' with value '"+locatorValue+"' for the \""+locatorKey+"\"", "The "+locatorKey+ " should be available in the page", "The "+locatorKey+" is not available in the page","Fail", strReportFilename);	
		}
		return element; 
	}

	public void waitForElementUsingVisibility(String locatorKey)
	{
		String locatorMethod=null;
		String locatorValue=null;
		try {
			String [] locatorMethodName = readProperties(locatorKey);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Fetch LocatorMethod and LocatorValue for "+locatorKey, "LocatorMethod: "+locatorMethod+";"+"LocatorValue: "+locatorValue, e.getMessage(), "Fail", strReportFilename);
		}
		try {
			switch(locatorMethod) {
			case "id":
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.id(locatorValue)));
				break;
			case "name":
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.name(locatorValue)));
				break;
			case "class": 
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.className(locatorValue)));
				break;
			case "linkText":
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(locatorValue)));
				break;
			case "partiallinkText":
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.partialLinkText(locatorValue)));
				break;
			case "tagname":
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.tagName(locatorValue)));
				break;
			case "css":
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(locatorValue)));
				break;
			case "xpath": 
				wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locatorValue)));
				break;

			default: break;
			}
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Find Element By "+locatorMethod+" with Value "+locatorValue+ " in the webpage", "The Locator Method and Value should be available in the page", e.getMessage() ,"Fail", strReportFilename);
		}
	}

	public void waitForElementUsingPresence(String locatorKey){
		String locatorMethod=null;
		String locatorValue=null;
		try {
			String [] locatorMethodName = readProperties(locatorKey);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Fetch LocatorMethod and LocatorValue for "+locatorKey, "LocatorMethod: "+locatorMethod+";"+"LocatorValue: "+locatorValue, e.getMessage(), "Fail", strReportFilename);
		}
		try {
			switch(locatorMethod) {
			case "id":
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.id(locatorValue)));
				break;
			case "name":
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.name(locatorValue)));
				break;
			case "class": 
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.className(locatorValue)));
				break;
			case "linkText":
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.linkText(locatorValue)));
				break;
			case "partiallinkText":
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.partialLinkText(locatorValue)));
				break;
			case "tagname":
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.tagName(locatorValue)));
				break;
			case "css":
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(locatorValue)));
				break;
			case "xpath": 
				wait1.until(ExpectedConditions.presenceOfElementLocated(By.xpath(locatorValue)));
				break;

			default: break;
			}
		} 
		catch(Exception e)
		{
			e.printStackTrace();
			//reporter.writeStepResult(tc_id,scenarioName, "Find Element By "+locatorMethod+" with Value "+locatorValue+ " in the webpage", "The Locator Method and Value should be available in the page", e.getMessage() ,"Fail", strReportFilename);
		}
	}

	public void waitForTextToBePresentInElement(String locatorKey, String strExpectedText){
		try {
			WebElement element = getElementFluentWait(locatorKey);
			wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
		} catch(Exception e)
		{
			reporter.writeStepResult(tc_id,scenarioName, "Wait for the Text to be Present in the element", strExpectedText, e.getMessage() ,"Fail", strReportFilename);
		}
	}

	/**
	 * Parse the test data
	 */
	public Map<String, String> parse(String testCaseID, String filename) {
		String Value = "";
		Map<String, String> keyVal = new HashMap<String, String>();
		try {
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Map<String, String>> map = mapper.readValue(new File("Data/" + filename + ".json"),
					new TypeReference<Map<String, Map<String, String>>>() {
			});

			//System.out.println("Map is " + map);

			String key = "";
			String val = "";
			for (int i = 1; i <= map.size(); i++) {
				Map<String, String> str = map.get(testCaseID);

				//System.out.println(str);

				for (Entry<String, String> data : str.entrySet()) {
					//System.out.println("Key is " + data.getKey() + " Value is " + data.getValue());
					key = data.getKey();
					val = data.getValue();
					keyVal.put(key, val);
				}
			}
			// Value=keyVal.get(value);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return keyVal;
	} 

	/**
	 * Enter the text
	 */
	public void clearText(String locatorKey)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			element.clear();		
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Clear the text value in text field", "Clear the text value", e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Enter the text with Tab
	 */
	public void enterTextwithTab(String locatorKey, String data)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			element.sendKeys(data);	
			element.sendKeys(Keys.TAB);
			reporter.writeStepResult(tc_id,scenarioName, "Enter Value in text field", data, "Value "+data+" entered successfully", "Pass", strReportFilename);		
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Enter Value in text field", data, "Unable to enter value "+data, "Fail", strReportFilename);
		}
	}

	/**
	 * Enter the text using JavaScript
	 */
	public void enterTextwithJS(String locatorKey, String data) {
		try {
			WebElement element = getElementFluentWait(locatorKey);
			JavascriptExecutor js=(JavascriptExecutor) driver;
			js.executeScript("arguments[0].setAttribute('value', '" + data +"')", element);
			reporter.writeStepResult(tc_id,scenarioName, "Enter Value in text field", data, "Value "+data+" entered successfully", "Pass", strReportFilename);		
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Enter Value in text field", data, "Unable to enter value "+data, "Fail", strReportFilename);
		}
	}

	/**
	 * Enter the text in the input field
	 */
	public void enterText(String locatorKey, String data)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			element.sendKeys(data);	
			reporter.writeStepResult(tc_id,scenarioName, "Enter Value in text field", data, "Value "+data+" entered successfully", "Pass", strReportFilename);		
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Enter Value in text field", data, "Unable to enter value "+data, "Fail", strReportFilename);
		}
	}

	/**
	 * Click an element
	 */
	public void clickAnElement(String locatorKey, String strButtonLabel)
	{
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			if(element==null)
				return;
			wait1.until(ExpectedConditions.elementToBeClickable(element));
			element.click();
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" button successfully", "Pass", strReportFilename);
		} catch (Exception e) {
			try {
				if(element==null)
					return;
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].click();", element);

			} catch (Exception e1) {
				reporter.writeStepResult(tc_id, scenarioName, "Click on " + strButtonLabel, strButtonLabel,
						"Not able to click on  button " + strButtonLabel, "Fail", strReportFilename);

			}
		}
	}

	/**
	 * Click an element and Press Enter
	 */
	public void clickAndEnterAnElement(String locatorKey, String strButtonLabel)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			wait1.until(ExpectedConditions.elementToBeClickable(element));
			element.click();
			element.sendKeys(Keys.ENTER);
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" button successfully", "Pass", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Not able to click on  button "+strButtonLabel, "Fail", strReportFilename);
		}
	}

	/**
	 * Click an element using JavascriptExector with Handling Alert
	 */
	public void clickAnElementAndHandleAlert(String condition, String locatorKey, String strButtonLabel)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			wait1.until(ExpectedConditions.elementToBeClickable(element));
			element.click();

			switchTOAlert(condition);
			//reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" button successfully", "Pass", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Not able to click on  button "+strButtonLabel, "Fail", strReportFilename);
		}
	}

	/**
	 * Click an element and Verify Alert
	 */	
	public void clickAnElementAndVerifyAlert(String locatorKey, String strButtonLabel, String expectedText, String condition)
	{
		try {
			boolean flag = false;
			String actualText=null;
			WebElement element = getElementFluentWait(locatorKey);
			wait1.until(ExpectedConditions.elementToBeClickable(element));
			element.click();
			wait.until(ExpectedConditions.alertIsPresent());
		
			Alert alert=driver.switchTo().alert();
			if(!expectedText.equals("")) {
				actualText = alert.getText();
				System.out.println("***"+expectedText+"***");
				System.out.println("***"+actualText+"***");
				if (expectedText.equals(actualText)) 
					flag = true;
				else
					flag=false;
					
			}
			switchTOAlert(condition);
			if (flag) 
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the alert", expectedText, actualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the alert", expectedText, actualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Click an element using JavascriptExector
	 */
	public void clickAnElementUsingJavaScript(String locatorKey, String strButtonLabel)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			if(element==null)
				return;
			wait1.until(ExpectedConditions.elementToBeClickable(element));

			JavascriptExecutor jse = (JavascriptExecutor) driver;
			jse.executeScript("arguments[0].click();", element); 

			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" button successfully", "Pass", strReportFilename);
		} catch (Exception e) {
			e.printStackTrace();
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Not able to click on  button "+strButtonLabel, "Fail", strReportFilename);
		}
	}

	/**
	 * Set Attribute Value using JavascriptExector
	 */
	public void setAttributeValueUsingJavaScript(String locatorKey, String attName, String attValue)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			JavascriptExecutor js = (JavascriptExecutor) driver;
			js.executeScript("arguments[0].setAttribute(arguments[1], arguments[2]);", element, attName, attValue);				
			reporter.writeStepResult(tc_id,scenarioName, "Enter "+attName+" in text field", attValue, "Value "+attValue+" entered successfully", "Pass", strReportFilename);		
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Enter "+attName+" in text field", attValue, "Unable to enter value "+attValue, "Fail", strReportFilename);
		}
	}


	/**
	 * Scroll to an Element using JavascriptExector
	 */
	public void scrollToAnElementUsingJavaScript(String locatorKey)
	{
		try {
			WebElement element = getWebElementWithQuickWait(locatorKey);			
			((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", element);
		} catch (Exception e) {
			e.printStackTrace();
			reporter.writeStepResult(tc_id,scenarioName, "Scroll to an Element", locatorKey,"Not able to Scroll to an element"+e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void scrollToAnElementUsingJavaScript(String locatorKey, String strExpectedText)
	{
		try {
			WebElement element = getWebElement(locatorKey);
			waitForTextToBePresentInElement(locatorKey, strExpectedText);
			((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", element);
		} catch (Exception e) {
			e.printStackTrace();
			reporter.writeStepResult(tc_id,scenarioName, "Scroll to an Element", locatorKey,"Not able to Scroll to an element"+e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Highlight to an Element using JavascriptExector
	 */
	public static void highlightElement(WebElement element) {
		JavascriptExecutor js = (JavascriptExecutor)driver;
		js.executeScript("arguments[0].style.border='2px solid blue'", element);	    
	}

	/**
	 * Press Key
	 */
	public void pressKey(String Key, String locatorKey) {
		Actions act = new Actions(driver);
		WebElement element = getWebElement(locatorKey);
		
		switch(Key)
		{
		case "Enter":
			element.sendKeys(Keys.ENTER);
			break;
		case "Down":
			element.sendKeys(Keys.DOWN);
			break;
		case "ENTER":
			act.moveToElement(element).build().perform();
			act.sendKeys(Keys.ENTER).build().perform();;
			break;
		}
		
			
	}

	/**
	 * Switch to the frame
	 */
	public void switchToFrame(String locatorKey)
	{	
		WebElement frame = getElementFluentWait(locatorKey);
		try {
			waitForElementUsingPresence(locatorKey);
			driver.switchTo().frame(frame);
			reporter.writeStepResult(tc_id,scenarioName, "Select Frame : " + locatorKey, locatorKey, "Frame "+locatorKey+" selected successfully", "Pass", strReportFilename);
		}catch(WebDriverException web1){
			reporter.writeStepResult(tc_id,scenarioName, "Select Frame : " + locatorKey, ""+locatorKey, "Unable to select frame due to Webdriver exception", "Fail", strReportFilename);
		}catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Select Frame : " + locatorKey, ""+locatorKey, "Not able to select frame "+locatorKey, "Fail", strReportFilename);
		}
	}

	/**
	 * Switch to default frame
	 */
	public void deSelectFrame(){
		try{
			driver.switchTo().defaultContent();
			reporter.writeStepResult(tc_id,scenarioName, "DeSelect Frame", "Frame should be deselected", "Frame deselected successfully", "Pass", strReportFilename);
		}catch(SeleniumException web1){
			reporter.writeStepResult(tc_id,scenarioName, "Select Main page", "Frame should be deselected", web1.getMessage(), "Fail", strReportFilename);
		}catch(Exception e1){
			reporter.writeStepResult(tc_id,scenarioName, "Select Main page", "Frame should be deselected", "Not able to select Main page", "Fail", strReportFilename);
		}
	}

	/**
	 * Switch to default frame
	 */
	public String fetchText(String locatorKey) {
		String Text = null;
		try {
			WebElement element = getElementFluentWait(locatorKey);
			Text = element.getText();
			reporter.writeStepResult(tc_id,scenarioName, "Fetch the text from the field", "Text should be fetched Successfully "+Text+"", "Text fetched Successfully "+Text+"", "Pass", strReportFilename);
		} catch(Exception e){
			e.printStackTrace();
			reporter.writeStepResult(tc_id,scenarioName, "Fetch the text from the field", "Text should be fetched Successfully "+Text+"", e.getMessage(), "Fail", strReportFilename);
		}
		return Text;
	}

	/**
	 * Verify element is present
	 */
	public boolean verifyElementPresent(String locatorKey)
	{
		WebElement element = getElementFluentWait(locatorKey);
		if(element.isDisplayed())
			return true;
		else
			return false;
	}

	/**
	 * Verify element is present using element
	 */
	public boolean verifyElementPresent(WebElement element)
	{
		if(element.isDisplayed())
			return true;
		else
			return false;
	}

	/**
	 * Verify element is present
	 */
	public void verifyElementPresentWithReport(String locatorKey) {
		String text="";
		try {
			WebElement element = getElementFluentWait(locatorKey);
			boolean exists;
			if(element.isDisplayed()) {
				exists = true;
				if(locatorKey.contains(" Text"))
				{
					text=element.getText();
				}
				else if(locatorKey.contains(" Value"))
				{
					text=element.getAttribute("value").trim();
				}
			}
			else
				exists = false;	
			

			if (exists) 
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is present on the page with text "+text+" ", locatorKey, "The element "+locatorKey+" is present on the page with text "+text+" ", "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is present on the page with text "+text+" ", locatorKey, "The element "+locatorKey+" is not present on the page", "Fail", strReportFilename);
		}catch(Exception e){
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is present on the page with text "+text+" ", locatorKey, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify value is disabled
	 */
	public void verifyValueIsDisabled(String Label, String locatorKey)
	{
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			//waitForElementUsingVisibility(locatorKey);
			if (element.getAttribute("disabled").equals("true") && !element.isEnabled())
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value is disabled", Label, "The "+Label+" is disabled on the page", "Pass", strReportFilename);	
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value is disabled", Label, "The "+Label+" is not disabled on the page", "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value is disabled", Label, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify value is enabled
	 */
	public void verifyValueIsEnabled(String Label, String locatorKey)
	{
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			//waitForElementUsingVisibility(locatorKey);
			if (element.isEnabled())
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value is enabled", Label, "The "+Label+" is enabled on the page", "Pass", strReportFilename);	
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value is enabled", Label, "The "+Label+" is not enabled on the page", "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value is enabled", Label, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify User Name value
	 */
	public void verifyusername(String locatorKey)
	{
		String Actualusername,Expectedusername=null;
		try {
			WebElement element = getElementFluentWait(locatorKey);
			Actualusername=element.getText().replace("(Ext:)","").replace(" ","");
			Expectedusername=ExecutionerClass.config.getProperty("Username"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")).replace("."," ").replace(" ", "");
			if(Actualusername.equals(Expectedusername))
				reporter.writeStepResult(tc_id,scenarioName, "Verify username",  Expectedusername,Actualusername , "Pass", strReportFilename);  
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify username",  Expectedusername, Actualusername, "Fail", strReportFilename);
		}
		catch(Exception e)
		{
			reporter.writeStepResult(tc_id,scenarioName, "Verify username",  Expectedusername, e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}

	}

	/**
	 * Verify element text is present in the TextBox
	 */
	public void verifyValuePresentInTheTextBox(String Label, String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			if(verifyElementPresent(element)) 
				strActualText = element.getAttribute("value").trim();
		} catch (NullPointerException e) {
			strActualText="";
		}
		try {
			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void verifyValuePresentInTheTextBoxIgnorecase(String Label, String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			if(verifyElementPresent(element)) 
				strActualText = element.getAttribute("value").trim();
		} catch (NullPointerException e) {
			strActualText="";
		}
		try {
			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.equalsIgnoreCase(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present in the TextBox contains
	 */
	public void verifyValuePresentInTheTextBoxContains(String Label, String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			//waitForElementUsingVisibility(locatorKey);

			if(verifyElementPresent(element)) 
				strActualText = element.getAttribute("value").trim();	

			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.contains(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present in the TextBox
	 */
	public void verifyValuePresentInTheTextBox(String Label, WebElement element, String strExpectedText)
	{
		String strActualText = null;
		try {		
			if(verifyElementPresent(element)) 
				strActualText = "";
			highlightElement(element);
			strActualText = element.getAttribute("value").trim().replace("\n", " ");		

			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element is not present
	 */
	public void verifyElementNotPresent(String locatorKey)
	{
		try {
			WebElement element = getWebElement(locatorKey);
			boolean exists;
			if(element.isDisplayed())
				exists = true;
			else
				exists = false;	

			if (!exists) 
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is not present on the page", locatorKey, "The element "+locatorKey+" is not present on the page", "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is not present on the page", locatorKey, "The element "+locatorKey+" is present on the page", "Fail", strReportFilename);
		}catch(Exception e){
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is not present on the page", locatorKey, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element is not present
	 */
	public void verifyElementNotPresentUsingException(String locatorKey)
	{
		WebElement element=null;
		String locatorMethod=null;
		String locatorValue=null;
		try {
			String [] locatorMethodName = readProperties(locatorKey);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Fetch LocatorMethod and LocatorValue for "+locatorKey, "LocatorMethod: "+locatorMethod+";"+"LocatorValue: "+locatorValue, e.getMessage(), "Fail", strReportFilename);
		}
		try {
			switch(locatorMethod) {
			case "id":
				element = driver.findElement(By.id((locatorValue)));
				break;
			case "name":
				element = driver.findElement(By.name((locatorValue)));
				break;
			case "class": 
				element = driver.findElement(By.className((locatorValue)));
				break;
			case "linkText":
				element = driver.findElement(By.linkText((locatorValue)));
				break;
			case "partiallinkText":
				element = driver.findElement(By.partialLinkText((locatorValue)));
				break;
			case "tagname":
				element = driver.findElement(By.tagName((locatorValue)));
				break;
			case "css":
				element = driver.findElement(By.cssSelector((locatorValue)));
				break;
			case "xpath": 
				element = driver.findElement(By.xpath((locatorValue)));
				break;

			default: break;
			}
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is not present on the page", locatorKey, "The element "+locatorKey+" is present on the page", "Fail", strReportFilename);
		}catch(NoSuchElementException e){
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is not present on the page", locatorKey, "The element "+locatorKey+" is not present on the page", "Pass", strReportFilename);
		}catch(Exception e){
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+locatorKey+" is not present on the page", locatorKey, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present
	 */
	public void verifyElementTextPresent(String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);

			if(verifyElementPresent(element)) 
				wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
			else
				waitForElementUsingVisibility(locatorKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {	
			strActualText = element.getText().trim();	
			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void verifyElementTextPresentignorecase(String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {	
			strActualText = element.getText().trim();	
			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.equalsIgnoreCase(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}


	/**
	 * Verify element text is present using element
	 */
	public void verifyElementTextPresent(WebElement element, String strExpectedText)
	{
		String strActualText = null;
		try {		
			if(verifyElementPresent(element)) 
				wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {	
			strActualText = element.getText().trim();	

			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present
	 */
	public void verifyElementTextPresentUsingPattern(String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		int actualtextlength=0,expectdtextlength=0;
		try {
			element = getElementFluentWait(locatorKey);

			if(verifyElementPresent(element)) 
				wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
			else
				waitForElementUsingVisibility(locatorKey);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {	
			strActualText = element.getText().trim().replace("\n", " ");
			actualtextlength=strActualText.length();
			if(expectdtextlength<actualtextlength)
			{				
				Pattern p=Pattern.compile(strExpectedText);
				Matcher m=p.matcher(strActualText);
				while(m.find())
				{
					strActualText=m.group();
				}
			}
			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element using Pattern", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element using Pattern", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present using element for label
	 */
	public void verifyElementTextPresent(String Label, WebElement element, String strExpectedText)
	{
		String strActualText = null;
		try {		
			if(verifyElementPresent(element)) 
				wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {	
			strActualText = element.getText().trim();	
			
			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for"+Label, strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present using contains
	 */
	public void verifyElementTextPresentContains(String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		try {	
			//waitForElementUsingVisibility(locatorKey);
			element = getElementFluentWait(locatorKey);
			if(verifyElementPresent(element))
				wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		try {
			strActualText = element.getText().trim();		
			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");	

			if (strActualText.contains(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}
	/**
	 * Verify element text contains using webelement
	 */
	public void verifyElementTextPresentcontains(WebElement element, String strExpectedText)
	{
		String strActualText = null;
		try {		
			if(verifyElementPresent(element))
				wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
		} catch (Exception e) {
			e.printStackTrace();
		} 
		try {
			strActualText = element.getText().trim();		
			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");	

			if (strActualText.contains(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present for multiple lines
	 */
	public void verifyElementTextPresentContainsMultipleLines(String Label, WebElement element, String strExpectedText)
	{
		String strActualText = null;
		
		try {		
			if(verifyElementPresent(element)) 
				strActualText = element.getText().trim().replace("\n", " ");		

			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");
			if (strActualText.contains(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present for multiple lines using Loop
	 */
	public int counter=0;
	public void verifyElementTextPresentContainsMultipleLines(String Label, WebElement element, String strExpectedText, int dataLoop, int dataSize)
	{
		String strActualText = null;
		try {
			wait.until(ExpectedConditions.visibilityOf(element));
			if(verifyElementPresent(element)) 
				strActualText = element.getText().trim().replace("\n", " ");	

			highlightElement(element);
			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");
			if (strActualText.contains(strExpectedText)) {
				counter=counter+1;
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Pass", strReportFilename);	
			}
			if(dataLoop==(dataSize-1)) {
				if(counter==0) {
					reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Fail", strReportFilename);
				} else {
					counter=0;
				}
			}
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present for multiple lines
	 */
	public void verifyElementTextPresentMultipleLines(String locatorKey, String strExpectedText)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			//waitForElementUsingVisibility(locatorKey);

			if(verifyElementPresent(element)) 
				wait1.until(ExpectedConditions.textToBePresentInElement(element, strExpectedText));
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			strActualText = element.getText().trim().replace("\n", " ");	

			System.out.println("***"+strExpectedText+"****");
			System.out.println("***"+strActualText+"****");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present for multiple lines
	 */
	public void verifyElementTextPresentMultipleLines(String Label, WebElement element, String strExpectedText)
	{
		String strActualText = null;
		try {		
			if(verifyElementPresent(element)) 
				strActualText = "";
			highlightElement(element);
			strActualText = element.getText().trim().replace("\n", " ");		

			System.out.println("****"+strExpectedText+"****");
			System.out.println("****"+strActualText+"****");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label, strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify element text is present is not null
	 */
	public void verifyElementTextPresentIsNotNull(String Label, String locatorKey)
	{
		String strActualText = null;
		try {
			WebElement element = getElementFluentWait(locatorKey);
			if(verifyElementPresent(element)) 
				strActualText = "";

			strActualText = element.getText().trim().replace("\n", " ");		

			System.out.println("****"+strActualText+"****");
			if (!strActualText.equals(""))
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label+" is not Blank", "The Actual Value should not be NULL or Blank", "Actual Value: "+strActualText, "Pass", strReportFilename);
			else if (!element.getAttribute("value").equals("")) {
				System.out.println("****"+element.getAttribute("value")+"****");
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label+" is not Blank", "The Actual Value should not be NULL or Blank", "Actual Value: "+element.getAttribute("value"), "Pass", strReportFilename);			
			}
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the element for "+Label+" is not Blank", "The Actual Value should not be NULL or Blank", "Actual Value: "+strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify element is present on the page", "The Actual Value should not be NULL", e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Match Pattern
	 */
	public void matchPattern(String locaterkey, String ExpectedPattern) {
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locaterkey);
			strActualText = element.getText().trim().replace("\n","");	
			if (strActualText.matches("^"+ExpectedPattern+"$")) 
				reporter.writeStepResult(tc_id, scenarioName, "Verify pattern text",ExpectedPattern , strActualText, "Pass", strReportFilename);
			else 
				reporter.writeStepResult(tc_id, scenarioName, "Verify pattern text",ExpectedPattern , strActualText, "Fail", strReportFilename);
		}
		catch (Exception e) {
			reporter.writeStepResult(tc_id, scenarioName, "Verify pattern text",ExpectedPattern , e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}
	}

	/**
	 * Verify color of an element
	 */
	public void verifyColorOfAnElement(String Label, String ExpectedColor, String locatorKey)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			String ActualColor = element.getCssValue("background-color");
			String hex = Color.fromString(ActualColor).asHex();

			switch (hex) {
			case "#ff0000":
				ActualColor = "RED";
				break;
			case "#008000":
				ActualColor = "GREEN";
				break;
			case "#0000FF":
				ActualColor = "BLUE";
				break;
			case "#ffe4c4":	
				ActualColor = "BISQUE";
				break;
			case "#ffc0cb":	
				ActualColor = "PINK";
				break;
			case "#87cefa":
				ActualColor = "LIGHT SKY BLUE";
				break;
			case "#b0e0e6":
				ActualColor = "POWDER BLUE";
				break;
			case "#d3d3d3":
				ActualColor = "LIGHT GREY";
				break;
			case "#ffffff":
				ActualColor = "WHITE";
				break;
			default:
				break;
			}
			if (ExpectedColor.equalsIgnoreCase(ActualColor))
				reporter.writeStepResult(tc_id,scenarioName, "Verify the color of the element for "+Label, ExpectedColor, ActualColor, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify the color of the element for "+Label, ExpectedColor, ActualColor, "Fail", strReportFilename);

		}catch(Exception e){
			reporter.writeStepResult(tc_id,scenarioName, "Verify the color of the element for" +Label, ExpectedColor, e.getMessage(), "Pass", strReportFilename);
		}
	}

	/**
	 * Select RadioButton or CheckBox
	 */
	public void clickRadioButtonOrCheckBox(String locatorKey, String strButtonLabel)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			wait1.until(ExpectedConditions.elementToBeClickable(element));
			if (!element.isSelected()) {
				element.click();
				reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" successfully", "Pass", strReportFilename);
			} else 
				reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "The "+strButtonLabel+" is already selected", "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * DeSelect RadioButton or CheckBox
	 */
	public void deselectRadioButtonOrCheckBox(String locatorKey, String strButtonLabel)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			wait1.until(ExpectedConditions.elementToBeClickable(element));
			if (element.isSelected()) {
				JavascriptExecutor jse = (JavascriptExecutor) driver;
				jse.executeScript("arguments[0].click();", element); 
				reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" successfully", "Pass", strReportFilename);
			} else 
				reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "The "+strButtonLabel+" is already not selected", "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify RadioButton or CheckBox is selected
	 */
	public void verifyRadioButtonOrCheckBoxIsSelected(String Status, String locatorKey, String strButtonLabel)
	{
		try {
			boolean statusFlag = false;
			WebElement element = getElementFluentWait(locatorKey);
			//waitForElementUsingPresence(locatorKey);
			if (Status.equalsIgnoreCase("selected")) {
				if (element.isSelected())
					statusFlag = true;
			} else if(Status.equalsIgnoreCase("deselected")) {
				if (!element.isSelected())
					statusFlag = true;
			}
			if (statusFlag) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+strButtonLabel+ " is "+Status, strButtonLabel, "The "+strButtonLabel+" is "+Status+" on the page", "Pass", strReportFilename);
			} else 
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+strButtonLabel+ " is "+Status, strButtonLabel, "The "+strButtonLabel+" is not "+Status+" on the page", "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+strButtonLabel+ " is "+Status, strButtonLabel, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Tab is selected
	 */
	public void verifyTabIsSelected(String locatorKey, String strButtonLabel)
	{
		try {
			WebElement element = getElementFluentWait(locatorKey);
			//waitForElementUsingPresence(locatorKey);
			if (element.getAttribute("class").contains("selectedTab")) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+strButtonLabel+ " is Selected", strButtonLabel, "The "+strButtonLabel+" is selected on the page", "Pass", strReportFilename);
			} else 
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+strButtonLabel+ " is Selected", strButtonLabel, "Not able to select the "+strButtonLabel, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+strButtonLabel+ " is Selected", strButtonLabel, e.getMessage(), "Fail", strReportFilename);
		}
	}


	/**
	 * Select drop down value
	 */
	public void selectDropdown(String locatorKey, String Option, String data)
	{
		WebElement element = getElementFluentWait(locatorKey);
		Select sel=new Select(element);
		try {
			if (Option.equalsIgnoreCase("VisibleText")) {	
				sel.selectByVisibleText(data);
			} else if (Option.equalsIgnoreCase("Value")) {
				sel.selectByValue(data);	
			} else if (Option.equalsIgnoreCase("Index")) {
				int index = Integer.parseInt(data);
				sel.selectByIndex(index);
			}
			reporter.writeStepResult(tc_id,scenarioName, "Select value from "+locatorKey+" Listbox",  data, "Expected value "+data+" is selected in the listbox", "Pass", strReportFilename);
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Select value from "+locatorKey+" Listbox",  data, "Expected value "+data+" is not present in the listbox", "Fail", strReportFilename);
		}
	}

	/**
	 * fetch Selected Drop Down Value
	 */
	public String fetchSelectedDropDownValue(String locatorKey) {
		String defaultValue = null;
		try {
			WebElement element = getElementFluentWait(locatorKey);
			Select sel=new Select(element);
			defaultValue = sel.getFirstSelectedOption().getText();
		}catch(NoSuchElementException e) {
			e.printStackTrace();
		}catch(Exception e) {	
			reporter.writeStepResult(tc_id,scenarioName, "fetch default DropDown value from "+locatorKey+" Listbox",  defaultValue, e.getMessage(), "Fail", strReportFilename);
		}
		return defaultValue;
	}

	/**
	 * Verify Selected Drop Down Value
	 */
	public void verifySelectedDropDownValue(String locatorKey, String ExpectedValue) {
		try {
			WebElement element = getElementFluentWait(locatorKey);
			Select sel=new Select(element);
			String ActualValue = sel.getFirstSelectedOption().getText();
			System.out.println("***"+ExpectedValue+"***");
			System.out.println("***"+ActualValue+"***");
			if (ExpectedValue.equals(ActualValue)) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify default DropDown value from "+locatorKey+" Listbox",  ExpectedValue, ActualValue, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify default DropDown value from "+locatorKey+" Listbox",  ExpectedValue, ActualValue, "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify default DropDown value from "+locatorKey+" Listbox",  ExpectedValue, e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void verifyValueRange(String locatorKey, int lowerValue, int higherValue) {
		try {
			WebElement element = getElementFluentWait(locatorKey); 
			String ActualValue = element.getText().replace("$", "").replace(",", "");
			double actValue = Double.parseDouble(ActualValue);
			int actVal = (int) actValue;

			if (actVal>lowerValue && actVal<higherValue) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Value ranges between "+lowerValue+ " to "+higherValue,  "The Value should be between "+lowerValue+ " to "+higherValue, ""+actVal, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Value ranges between "+lowerValue+ " to "+higherValue,  "The Value should be between "+lowerValue+ " to "+higherValue, ""+actVal, "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify Value ranges between "+lowerValue+ " to "+higherValue, "The Value should be between "+lowerValue+ " to "+higherValue, e.getMessage(), "Fail", strReportFilename);
		}		
	}

	/**
	 * Verify Drop Down Value not present in the list
	 */
	public void verifyDropDownValueNotPresent(String locatorKey, String ExpectedValue) {
		try {
			boolean flag = false;
			WebElement element = getElementFluentWait(locatorKey);
			Select sel=new Select(element);
			List<WebElement> ActualValue = sel.getOptions();
			for (int i = 0; i < ActualValue.size(); i++) {
				if (ExpectedValue.equals(ActualValue.get(i).getText())) {
					flag = false;
					break;
				} else {
					flag = true;
				}
			}
			if (flag) 
				reporter.writeStepResult(tc_id,scenarioName, "Verify default DropDown value from "+locatorKey+" Listbox",  ExpectedValue, "The Value "+ExpectedValue+ "is not present in the "+locatorKey+" Listbox", "Pass", strReportFilename);
			else 
				reporter.writeStepResult(tc_id,scenarioName, "Verify default DropDown value from "+locatorKey+" Listbox",  ExpectedValue, "The Value "+ExpectedValue+ "is  present in the "+locatorKey+" Listbox", "Fail", strReportFilename);
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify default DropDown value from "+locatorKey+" Listbox",  ExpectedValue, e.getMessage(), "Fail", strReportFilename);
		}
	}


	/**
	 * fetch Drop Down Values
	 */
	public List<String> fetchDropDownValues(String locatorKey)
	{
		List<String> ActualValues = null;
		try {
			WebElement element = getElementFluentWait(locatorKey);
			Select sel=new Select(element);
			ActualValues = new ArrayList<String>();

			List<WebElement> options = sel.getOptions();
			for (WebElement option : options) {
				ActualValues.add(option.getText());
				System.out.println("***"+option.getText()+"***");
			}	
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Fetch DropDown values for "+locatorKey,  "The dropdown values should be available", e.getMessage(), "Fail", strReportFilename);
		}
		return ActualValues;
	}

	/**
	 * fetch Drop Down Values using list
	 */
	public List<String> fetchDropDownValuesUsingList(String locatorKey)
	{
		List<String> ActualValues = null;
		try {
			List<WebElement> elements = getWebElements(locatorKey);
			ActualValues = new ArrayList<String>();

			for (WebElement element : elements) {
				ActualValues.add(element.getText());
				System.out.println("***"+element.getText()+"***");
			}	
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Fetch DropDown values for "+locatorKey,  "The dropdown values should be available", e.getMessage(), "Fail", strReportFilename);
		}
		return ActualValues;
	}

	/**
	 * Verify Drop Down Values
	 */
	public void VerifyDropDownValues(String Label, List<String> ExpectedValues, List<String>ActualValues)
	{
		try {
			if (ExpectedValues.size()==ActualValues.size()) {
				for (int i = 0; i < ExpectedValues.size(); i++) {
					if (ExpectedValues.get(i).equals(ActualValues.get(i))) 
						reporter.writeStepResult(tc_id,scenarioName, "Verify DropDown value from "+Label+" Listbox",  ExpectedValues.get(i), ActualValues.get(i), "Pass", strReportFilename);
					else
						reporter.writeStepResult(tc_id,scenarioName, "Verify DropDown value from "+Label+" Listbox",  ExpectedValues.get(i), ActualValues.get(i), "Fail", strReportFilename);
				}
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify DropDown value from "+Label+" Listbox",  "The Expected Dropdown Values Size "+ExpectedValues.size(), "The Actual Dropdown Values Size "+ActualValues.size(), "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify DropDown value from "+Label+" Listbox",  ""+ExpectedValues, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Row Label for Column Label with Value
	 */
	public void verifyRowLabelforColumnLabelWithValue(String RowLabel, String ColumnLabel, String ExpectedValue, String ActualValue) {
		try {
			if (ExpectedValue.equals(ActualValue)) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+RowLabel+" for "+ColumnLabel+" with Expected Value", ExpectedValue, ActualValue, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+RowLabel+" for "+ColumnLabel+" with Expected Value", ExpectedValue, ActualValue, "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+RowLabel+" for "+ColumnLabel+" with Expected Value", ExpectedValue, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Table Header Values
	 */
	public void verifyTableHeaderValues(String TableName, String ExpectedValue, String ActualValue) {
		try {
			if (ExpectedValue.equals(ActualValue)) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Header "+ExpectedValue+" is present in the Table "+TableName, ExpectedValue, ActualValue, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Header "+ExpectedValue+" is present in the Table "+TableName, ExpectedValue, ActualValue, "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify Header "+ExpectedValue+" is present in the Table "+TableName, ExpectedValue, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Number of Rows
	 */
	public void verifyNumberOfRows(String locatorKey, int ExpectedNumberofRows) {
		try {
			List<WebElement> element = getWebElements(locatorKey);
			int ActualNumberOfRows = element.size();
			if (ExpectedNumberofRows==ActualNumberOfRows) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Number of Rows or lists Present in the Table", ""+ExpectedNumberofRows, ""+ActualNumberOfRows, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Number of Rows or lists Present in the Table", ""+ExpectedNumberofRows, ""+ActualNumberOfRows, "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify Number of Rows or lists Present in the Table", ""+ExpectedNumberofRows, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Number of Rows
	 */
	public void verifyNumberOfRowsGreaterThan(String locatorKey, int ExpectedNumberofRows) {
		try {
			List<WebElement> element = getWebElements(locatorKey);
			int ActualNumberOfRows = element.size();
			if (ActualNumberOfRows>=ExpectedNumberofRows) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Number of Rows or lists greater than "+ExpectedNumberofRows+" in the Table", "The Number of Rows or lists in the Table should be greater than "+ExpectedNumberofRows, "Total "+ActualNumberOfRows+" number of rows or lists are present in the Table", "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify Number of Rows or lists greater than "+ExpectedNumberofRows+" in the Table", "The Number of Rows or lists in the Table should be greater than "+ExpectedNumberofRows, "Total "+ActualNumberOfRows+" number of rows or lists are present in the Table", "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify Number of Rows or lists Present in the Table", ""+ExpectedNumberofRows, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Alert Text
	 */
	public void verifyAlertText(String expectedText) {
		try {
			wait.until(ExpectedConditions.alertIsPresent());
			Alert alert = driver.switchTo().alert();
			String actualText = alert.getText();
			System.out.println("***"+expectedText+"***");
			System.out.println("***"+actualText+"***");
			if (expectedText.equals(actualText)) 
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the alert", expectedText, actualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify text is present in the alert", expectedText, actualText, "Pass", strReportFilename);
		}
		catch (Exception e)	{
			reporter.writeStepResult(tc_id,scenarioName, "Verify the Alert Text Present on the page", expectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Switch to Alert
	 */
	public void switchTOAlert(String condition) {
		try {
			wait.until(ExpectedConditions.alertIsPresent());
			Alert alert = driver.switchTo().alert();
			if (condition.equalsIgnoreCase("Accept")) 
				alert.accept();
			else
				alert.dismiss();
		}
		catch (Exception e)	{
			reporter.writeStepResult(tc_id,scenarioName, "Handle the Alert Present on the page", condition, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * To get system date
	 */
	public String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");

		Date date = new Date();
		String date1 = dateFormat.format(date);
		return date1;
	}

	/**
	 * Navigate Page
	 */
	public void navigatepage(String option)
	{	
		try {
			if(option.equalsIgnoreCase("back"))
				driver.navigate().back();
			else if(option.equalsIgnoreCase("forward"))
				driver.navigate().forward();

			reporter.writeStepResult(tc_id,scenarioName,"Navigation", "User should navigate"+option,"User navigated" +option+ " successfully", "Pass", strReportFilename);
		}catch(WebDriverException web1){
			reporter.writeStepResult(tc_id,scenarioName, "Navigation","User should navigate"+option, "User not able to navigate"+option+" due to Webdriver exception", "Fail", strReportFilename);
		}catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName,"Navigation", "User should navigate"+option, "User not able to navigate"+option+" due to Webdriver exception", "Fail", strReportFilename);
		}
	}

	/**
	 * To get system date
	 */
	public String getDate(String currentformat,String Requiredformat,String datestring)
	{
		try {
			if(currentformat.equals("")&&datestring.equals("")) {
				Date d =new Date();
				SimpleDateFormat sf=new SimpleDateFormat(Requiredformat);
				String formatteddate=sf.format(d);
				System.out.println(formatteddate);
				return formatteddate ;
			}
			else
			{

				SimpleDateFormat sf=new SimpleDateFormat(currentformat);
				Date d=sf.parse(datestring);
				SimpleDateFormat sf1=new SimpleDateFormat(Requiredformat);
				String formatteddate=sf1.format(d);
				System.out.println(formatteddate);
				return formatteddate ;
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}


	/**
	 * fetch column names from DB query
	 */
	public String[] fetchColumnNamesFromDBQuery(String query) {
		String[] ColumnNames = null;
		int selectIndex = query.toUpperCase().lastIndexOf("SELECT");
		int fromIndex = query.toUpperCase().lastIndexOf("FROM");
		String columnNameValue = query.toUpperCase().substring(selectIndex+ 6, fromIndex - 1)
				.replaceAll("(\\r|\\n|\\r\\n|\\s+)+", "").trim();
		ColumnNames = columnNameValue.split(",");
		for (int j = 0; j < ColumnNames.length; j++) {
			if(ColumnNames[j].contains("SUBSTRING ")){
				if(ColumnNames[j].contains("AS ")){
					int rawColName =ColumnNames[j].indexOf("AS");
					String rawColNameVal = ColumnNames[j].substring(rawColName+2);
				}
			}
			if(ColumnNames[j].contains(".")){
				ColumnNames[j]=ColumnNames[j].replaceAll("^[a-zA-Z0-9]+[.]", "").trim();
			}
		}
		return ColumnNames;
	}

	/**
	 * fetch DataBase Values
	 */
	public List<Map<String, String>> fetchDatabaseValuesInMap(String query) {
		Properties config = null;
		try {
			config = new ExecutionerClass().setEnv();			
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			final String Driver= config.getProperty("DB_DriverClass"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment"));
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName(Driver);
		} catch (ClassNotFoundException e1) {
			System.out.println("Unable to load the driver class! "+e1);
			reporter.writeStepResult(tc_id,scenarioName, "Fetch the Values from Database", query, e1.getMessage(), "Fail", strReportFilename);
			e1.printStackTrace();
		} 
		Connection dbConnection = null;
		try {
			final String DBUrl = config.getProperty("DB_Url"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment"));
			final String DBName = config.getProperty("DB_DBName"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment"));
			/*String connectionUrl = "jdbc:sqlserver://JVLDBUAT11:1433;" +
					"databaseName=HCSdb;integratedSecurity=true;";*/
			String connectionUrl =DBUrl+DBName;

			dbConnection=DriverManager.getConnection(connectionUrl);
			System.out.println("Database Connection Successfully established");
		} catch (SQLException e) {						
			System.out.println("Couldnâ€™t get connection! "+e);
			reporter.writeStepResult(tc_id,scenarioName, "Fetch the Values from Database", query, e.getMessage(), "Fail", strReportFilename);
		}
		int selectIndex = query.toUpperCase().lastIndexOf("SELECT");
		int fromIndex = query.toUpperCase().lastIndexOf("FROM");
		String columnNames = query.toUpperCase().substring(selectIndex+ 6, fromIndex - 1)
				.replaceAll("(\\r|\\n|\\r\\n|\\s+)+", "").trim();

		String[] rawColumnNames=null;
		rawColumnNames = columnNames.split(",");
		for (int j = 0; j < rawColumnNames.length; j++) {
			if(rawColumnNames[j].contains("SUBSTRING ")){
				if(rawColumnNames[j].contains("AS ")){
					int rawColName =rawColumnNames[j].indexOf("AS");
					String rawColNameVal = rawColumnNames[j].substring(rawColName+2);
				}
			}
			if(rawColumnNames[j].contains(".")){
				rawColumnNames[j]=rawColumnNames[j].replaceAll("^[a-zA-Z0-9]+[.]", "").trim();
			}
		}
		String strActualValue = null;
		List<Map<String, String>> dbResultSet = new ArrayList<>();
		if (dbConnection != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = dbConnection.createStatement();
				rs = stmt.executeQuery(query);

				while (rs.next()) {
					Map<String, String> tableData = new HashMap<String, String>();
					for (int p = 0; p < rawColumnNames.length; p++) {
						try {
							if (rs.getString(rawColumnNames[p]).equals(null)) 	
								System.out.println("The DB Value for "+rawColumnNames[p]+" is null");
							else
								strActualValue = rs.getString(rawColumnNames[p]).trim();

						} catch (NullPointerException e) {
							strActualValue = "";
						}
						System.out.println(rawColumnNames[p]+"===> "+strActualValue);
						tableData.put(rawColumnNames[p], strActualValue);
					}
					dbResultSet.add(tableData);
				}
				return dbResultSet;
			} catch (Exception e) {
				reporter.writeStepResult(tc_id,scenarioName, "Fetch the Values from Database", query, e.getMessage(), "Fail", strReportFilename);
			} finally {
				try {
					rs.close();
					dbConnection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			reporter.writeStepResult(tc_id,scenarioName, "Fetch the Values from Database", query, ""+dbConnection, "Fail", strReportFilename);
		}
		return dbResultSet;
	}

	/**
	 * fetch DataBase Values
	 */
	public void VerifyDatabaseValues(String Label, String Field, String ExpectedValue, String ActualValue) {
		try {
			if (ExpectedValue.equals(ActualValue)) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Field+" Database Value for "+Label, ExpectedValue, ActualValue, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Field+ " Database Value for"+Label, ExpectedValue, ActualValue, "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Field+ " Database Value for "+Label, ExpectedValue, e.getMessage(), "Fail", strReportFilename);
		}
	}

	public static String getEnvironmentVariable(String strVariableName) {
		return envVariableMap.get(strVariableName);
	}

	public static void setEnvironmentVariable(String strVariableName, String strValue) {
		envVariableMap.put(strVariableName, strValue);
	}

	/**
	 * Verify Checkbox using symbols
	 */
	public void VerifyCheckBoxUsingSymbols(String locatorKey, String ExpectedValue) {
		try {			
			WebElement element = getElementFluentWait(locatorKey);
			String symbol = element.getText();
			char[] a = symbol.toCharArray();
			String actualHex = "";
			for (int i = 0; i < a.length; i++) {
				String hexSymbol = Integer.toHexString((int) a[i]);
				actualHex = actualHex+hexSymbol;
			}
			String ActualValue = null;

			switch (actualHex) {
			case "2610":
				ActualValue = "UnChecked";
				break;
			case "2611":
				ActualValue = "Checked";
				break;
			case "2612":
				ActualValue = "Crossed";
				break;
			default:
				break;
			}

			if (ExpectedValue.equals(ActualValue)) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify CheckBox Using Symbols",  ExpectedValue, ActualValue, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify CheckBox Using Symbols",  ExpectedValue, ActualValue, "Fail", strReportFilename);
			}
		}catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify CheckBox Using Symbols",  ExpectedValue, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Page Title
	 */
	public void verifyPageTitle(String ClosedPage, String ExpectedTitle) {
		try {
			String ActualTitle = driver.getTitle();
			if (driver.getWindowHandles().size()==1) {
				if (ExpectedTitle.equals(ActualTitle)) {
					reporter.writeStepResult(tc_id,scenarioName, "Verify page Title after "+ClosedPage+" is closed",  ExpectedTitle, ActualTitle, "Pass", strReportFilename);
				} else {
					reporter.writeStepResult(tc_id,scenarioName, "Verify page Title after "+ClosedPage+" is closed",  ExpectedTitle, ActualTitle, "Fail", strReportFilename);
				}	
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify page Title after "+ClosedPage+" is closed",  ExpectedTitle, "Not able to close the " +ClosedPage, "Fail", strReportFilename);
			}
		} catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify page Title after "+ClosedPage+" is closed",  ExpectedTitle, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Page Title
	 */
	public void verifyPageTitle(String ExpectedTitle) {
		String ActualTitle = null;
		try {
			wait.until(ExpectedConditions.titleIs(ExpectedTitle));
			ActualTitle = driver.getTitle();
			if (ExpectedTitle.equals(ActualTitle)) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify page Title",  ExpectedTitle, ActualTitle, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify page Title",  ExpectedTitle, ActualTitle, "Fail", strReportFilename);
			}	
		} catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify page Title",  ExpectedTitle, e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Verify Page Url
	 */
	public void verifyPageURL(String PageName, String ExpectedURL) {
		try {
			String ActualURL = driver.getCurrentUrl();
			if (ExpectedURL.equals(ActualURL)) {
				reporter.writeStepResult(tc_id,scenarioName, "Verify the URL for the page "+PageName,  ExpectedURL, ActualURL, "Pass", strReportFilename);
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify the URL for the page "+PageName,  ExpectedURL, ActualURL, "Fail", strReportFilename);
			}	
		} catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify the URL for the page "+PageName,  ExpectedURL, e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void verifypageurl_for_diff_env(String pagename) {
		String Actualcurrentpageurl=null;
		String Expctedcurrentpageurl=null;
		try {
			Actualcurrentpageurl=driver.getCurrentUrl();
			Expctedcurrentpageurl=ExecutionerClass.config.getProperty(pagename+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment"));
			if(Actualcurrentpageurl.equalsIgnoreCase(Expctedcurrentpageurl))
				reporter.writeStepResult(tc_id, scenarioName, "Verify Pageurl", Expctedcurrentpageurl, Actualcurrentpageurl, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id, scenarioName, "Verify Pageurl", Expctedcurrentpageurl, Actualcurrentpageurl, "Fail", strReportFilename);
		}
		catch(Exception e)
		{
			reporter.writeStepResult(tc_id, scenarioName, "Verify Pageurl", Expctedcurrentpageurl, e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}
	}			  

	/**
	 * Close the Window
	 */
	public void closeTheWindow() {
		try {
			driver.close();
			reporter.writeStepResult(tc_id,scenarioName, "Close the Curent Window", "The Current Window should be closed", "The Current Window is closed", "Pass", strReportFilename);
		} catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Close the Curent Window", "The Current Window should be closed", e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Switch to New window
	 */
	public void switchToNewWindow() {
		try {
			Thread.sleep(3000);
			String parentWindowHandle= driver.getWindowHandle();
			setEnvironmentVariable("ParentWindow", parentWindowHandle);
			Set<String> allHandles = driver.getWindowHandles();			
			System.out.println("***********"+allHandles.size());
			for (String currHandle : allHandles) {
				if (!currHandle.equals(parentWindowHandle)) {
					driver.switchTo().window(currHandle);
					driver.manage().window().maximize();
					break;
				}
			} 
		} catch(Exception e) {
			e.printStackTrace();
			reporter.writeStepResult(tc_id,scenarioName, "Switch To New Window",  "The driver should be switched to New Window", e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * Switch to parent window
	 */
	public void switchToParentWindow() {
		try {
			String parentWindowHandle=getEnvironmentVariable("ParentWindow");
			Set<String> allHandles = driver.getWindowHandles();
			//String parentWindowHandle=allHandles.iterator().next();

			for (String currHandle : allHandles) {
				if (currHandle.equals(parentWindowHandle)) {
					driver.switchTo().window(currHandle);
					System.out.println(driver.getTitle());
					reporter.writeStepResult(tc_id,scenarioName, "Switch To Parent Window",  "The driver should be switched to Parent Window", "The driver switched to Parent Window", "Pass", strReportFilename);
				}
			}
		} catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Switch To Parent Window",  "The driver should be switched to Parent Window", e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void switchBetweenWindows(){
		Set<String> handles = driver.getWindowHandles();
		String firstWinHandle = driver.getWindowHandle();

		handles.remove(firstWinHandle);
		String winHandle=handles.iterator().next();
		if (winHandle!=firstWinHandle) {
			String secondWinHandle=winHandle;

			driver.switchTo().window(secondWinHandle);
		}
	}

	public void switchToTheParentWindow() {
		Set<String> handles = driver.getWindowHandles();

		String firstWinHandle = driver.getWindowHandle(); 
		handles.remove(firstWinHandle);
		driver.close();
		String winHandle=handles.iterator().next();

		if (winHandle!=firstWinHandle)
		{
			String secondWinHandle=winHandle;

			driver.switchTo().window(secondWinHandle);
		}
	}

	public void switchToNewWindowwithtitle(String ExpectedTitle) {
		String Actualtitle ="";
		try {
			Thread.sleep(3000);
			Set<String> allHandles = driver.getWindowHandles();			
			System.out.println("***********"+allHandles.size());
			String Expectedtitlecrt=ExpectedTitle.replaceAll(" ","");
			for (String currHandle : allHandles) {			
				Actualtitle=driver.switchTo().window(currHandle).getTitle().replaceAll(" ", "");
				if(Actualtitle.equalsIgnoreCase(Expectedtitlecrt)) {
					System.out.println(driver.getTitle());
					driver.manage().window().maximize();
					break;
				}
			}
			reporter.writeStepResult(tc_id, scenarioName, "Verify Pageurl", ExpectedTitle, Actualtitle, "Pass", strReportFilename);
		} catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Switch To New Window",  "The driver should be switched to New Window", e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void switchToNewWindowwithindex(String Expectedindex) {
		try {
			Set<String> allHandles = driver.getWindowHandles();
			System.out.println("***********"+allHandles.size());
			List indexes= new ArrayList(allHandles);
			driver.switchTo().window(indexes.get(Integer.parseInt(Expectedindex)-1).toString());
			driver.manage().window().maximize();

		} catch(Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Switch To New Window",  "The driver should be switched to New Window", e.getMessage(), "Fail", strReportFilename);
		}
	}

	/**
	 * To Save Excel
	 */
	public void saveExcel(String Actualfilename) {
		String ExcelName=null;
		loadjacobdll();
		AutoItX x = new AutoItX();
		x.winWait("Save As");
		if(x.winExists("Save As"))
		{
			x.controlFocus("Save As", "", "Edit1");
			String ProjectPath = new File("").getAbsolutePath();
			String ExcelFolder = ProjectPath+"\\Files\\Excel\\";
			int randomnumber = (int)(Math.random() * 999999)+1126;
			ExcelName = Actualfilename+randomnumber+".xls";
			String ExcelPath = ExcelFolder+ExcelName;
			setEnvironmentVariable("ExcelURL", ExcelPath);
			System.out.println("ExcelName"+ExcelName);
			System.out.println("ExcelPath"+ExcelPath);
			x.controlSend("Save As","", "Edit1",ExcelPath);
			x.sleep(2000);
			x.controlClick("Save As", "", "Button1");
			reporter.writeStepResult(tc_id,scenarioName, "Verify the file "+ExcelName+" downloaded successfully ",  ExcelName, "File "+ExcelName+" downloaded successfully ", "Pass", strReportFilename);
		}
		else
			reporter.writeStepResult(tc_id,scenarioName, "Verify the file "+ExcelName+" downloaded successfully ",  ExcelName,"Not able to download the file "+ExcelName+"" , "Fail", strReportFilename);
	}

	/**
	 * To verify file exist
	 */
	public void verifyFileExist(String FileType) {
		String FilePath = "";
		try {
			Thread.sleep(3000);	
			if (FileType.equals("PDF")) {
				FilePath = getEnvironmentVariable("PDFURL");
			} else if(FileType.equals("Excel")) {
				FilePath = getEnvironmentVariable("ExcelURL");
			}
			File f = new File(FilePath);
			if(f.exists() && !f.isDirectory())  
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+FileType+" file exist in the file path",  FilePath, "The "+FileType+" file is available in the path: "+"\""+FilePath+"\"", "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+FileType+" file exist in the file path",  FilePath, "The "+FileType+" file is not available in the path: "+"\""+FilePath+"\"", "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+FileType+" file exist in the file path",  FilePath, e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}
	}

	/**
	 * To Save PDF
	 */

	public void savePDF() {
		Robot robot;
		try {
			robot = new Robot();
			Thread.sleep(4000);

			robot.mouseMove(936, 208);
			robot.mousePress(InputEvent.BUTTON1_MASK);
			robot.mouseRelease(InputEvent.BUTTON1_MASK);

			//Shortcut to save the PDF
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyPress(KeyEvent.VK_S);
			robot.keyRelease(KeyEvent.VK_S);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_CONTROL);

			String ProjectPath = new File("").getAbsolutePath();
			String PDFFolder = ProjectPath+"\\Files\\PDF\\";
			int x = (int)(Math.random() * 999999)+1126;
			String PDFName = "hpstExhibitE"+x+".pdf";
			String PDFPath = PDFFolder+PDFName;
			setEnvironmentVariable("PDFURL", PDFPath);

			//Store the path of PDF to clipboard
			StringSelection selection = new StringSelection(PDFPath);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(selection, selection);

			Thread.sleep(1000);
			//Paste the path of PDF from clipboard and save the PDF
			robot.keyPress(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_V);
			robot.keyRelease(KeyEvent.VK_CONTROL);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);

		} catch (AWTException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	/**
	 * To Validate PDF
	 */
	public void verifyPDFContent(String DocumentName, String ExpectedTextInPDF) {	
		String parsedText = null;
		File pdfFile = null;
		String PDFPath = null;
		try {
			savePDF();

			PDFPath = getEnvironmentVariable("PDFURL");
			Thread.sleep(1000);
			pdfFile = new File(PDFPath);
			FileInputStream fis = new FileInputStream(PDFPath);
			BufferedInputStream file = new BufferedInputStream(fis);

			PDDocument doc = PDDocument.load(file);
			PDFTextStripper pdfStripper = new PDFTextStripper();
			parsedText = pdfStripper.getText(doc);

			if (doc!= null)
				doc.close();
			if (fis!= null)
				fis.close();
			if (file!= null)
				file.close();

		} catch (MalformedURLException e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify Text In "+DocumentName+ " PDF",  ExpectedTextInPDF, e.getMessage(), "Fail", strReportFilename);
			System.err.println("URL string could not be parsed "+e.getMessage());
			pdfFile.delete();
		} catch (IOException e1) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify Text In "+DocumentName+" PDF",  ExpectedTextInPDF, e1.getMessage(), "Fail", strReportFilename);
			System.err.println("Unable to open PDF Parser. " + e1.getMessage());
			pdfFile.delete();
		} catch (Exception e2) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify Text In "+DocumentName+" PDF",  ExpectedTextInPDF, e2.getMessage(), "Fail", strReportFilename);
			e2.printStackTrace();
			pdfFile.delete();
		}
		System.out.println("+++++++++++++++++");
		System.out.println(parsedText);
		System.out.println("+++++++++++++++++");

		if(parsedText.contains(ExpectedTextInPDF)) 
			reporter.writeStepResult(tc_id,scenarioName, "Verify Text In "+DocumentName+ " PDF",  ExpectedTextInPDF, "The Text "+"\""+ExpectedTextInPDF+"\""+" is present in the pdf available in the path: "+"\""+PDFPath+"\"", "Pass", strReportFilename);
		else
			reporter.writeStepResult(tc_id,scenarioName, "Verify Text In "+DocumentName+ " PDF",  ExpectedTextInPDF, "The Text "+"\""+ExpectedTextInPDF+"\""+" is present in pdf available in the path: "+"\""+PDFPath+"\"", "Fail", strReportFilename);		
	}


	public void getandverifyfilename(String pagecenterlocator,String Expectedfilename)
	{
		Robot robot;
		String Actualfilename=null;
		try {
			robot = new Robot();
			Thread.sleep(3000);
						
			WebElement element=getElementFluentWait(pagecenterlocator);
			element.sendKeys("");
			
			Thread.sleep(3000);
			//Shortcut to saveas the Excel	
			robot.keyPress(KeyEvent.VK_F6);
			robot.keyRelease(KeyEvent.VK_F6);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);	
			robot.keyPress(KeyEvent.VK_DOWN);
			robot.keyRelease(KeyEvent.VK_DOWN);
			Thread.sleep(2000);
			robot.keyPress(KeyEvent.VK_DOWN);
			robot.keyRelease(KeyEvent.VK_DOWN);
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(3000);
			loadjacobdll();
			AutoItX x = new AutoItX();
			x.winWait("Save As");
			if(x.winExists("Save As"))
			{
				x.controlFocus("Save As", "", "Edit1");
				Actualfilename=x.controlGetText("Save As", "", "Edit1");
				System.out.println("Fetched filename"+Actualfilename);
				if(Actualfilename.contains(Expectedfilename))
					reporter.writeStepResult(tc_id,scenarioName, "Verify the filename in the download popup as "+Expectedfilename+" ",  Expectedfilename, "Filename in the download popup is "+Expectedfilename+"", "Pass", strReportFilename);
				else
					reporter.writeStepResult(tc_id,scenarioName, "Verify the filename in the download popup as "+Expectedfilename+" ",  Expectedfilename, "Filename in the download popup is "+Expectedfilename+"", "Fail", strReportFilename);
			}
			else
				reporter.writeStepResult(tc_id,scenarioName,"Verify the filename in the download popup as "+Expectedfilename+" " ,Expectedfilename , "Not able to access the popup", "Fail", strReportFilename);
		}
		catch(Exception e)
		{
			reporter.writeStepResult(tc_id,scenarioName, "Verify the filename in the download popup as "+Expectedfilename+" ",  Expectedfilename, e.getMessage(), "Pass", strReportFilename);
			e.printStackTrace();
		}
	}
	public void canceldownloadpopup(String strButtonLabel)
	{
		Robot robot;
		try {
			loadjacobdll();
			AutoItX x = new AutoItX();
			x.winActivate("Save As");
			Boolean Flag=x.controlCommandIsEnabled("Save As", "", "Button2");
			if(Flag)
			{
				x.controlClick("Save As", "", "Button2");
				System.out.println("Clicked cancel button");
				Thread.sleep(3000);
				robot = new Robot();
				robot.mouseMove(936, 208);
				robot.mousePress(InputEvent.BUTTON1_MASK);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				Thread.sleep(3000);
				//Shortcut to saveas the Excel	
				robot.keyPress(KeyEvent.VK_F6);
				robot.keyRelease(KeyEvent.VK_F6);
				robot.keyPress(KeyEvent.VK_TAB);
				robot.keyRelease(KeyEvent.VK_TAB);	
				robot.keyPress(KeyEvent.VK_TAB);
				robot.keyRelease(KeyEvent.VK_TAB);
				robot.keyPress(KeyEvent.VK_ENTER);
				robot.keyRelease(KeyEvent.VK_ENTER);
				Thread.sleep(3000);			
				reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" button successfully", "Pass", strReportFilename);
			}
			else
				reporter.writeStepResult(tc_id, scenarioName, "Click on " + strButtonLabel, strButtonLabel,
						"Not able to click on  button " + strButtonLabel, "Fail", strReportFilename);

		}
		catch(Exception e)
		{
			reporter.writeStepResult(tc_id, scenarioName, "Click on " + strButtonLabel, strButtonLabel,e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}
	}

	public void closedownloadpopup(String pagecenterlocator,String buttonlabel) {
		Robot robot;
		try {
			robot = new Robot();
			Thread.sleep(10000);
						
			WebElement element=getElementFluentWait(pagecenterlocator);
			element.sendKeys("");
			
			Thread.sleep(3000);
		
			robot.keyPress(KeyEvent.VK_F6);
			robot.keyRelease(KeyEvent.VK_F6);
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);	
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);	
			robot.keyPress(KeyEvent.VK_TAB);
			robot.keyRelease(KeyEvent.VK_TAB);	
			robot.keyPress(KeyEvent.VK_ENTER);
			robot.keyRelease(KeyEvent.VK_ENTER);
			Thread.sleep(3000);			
			reporter.writeStepResult(tc_id,scenarioName, "Download popup should be closed", ""+buttonlabel+"", "Download popup is closed", "Pass", strReportFilename);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			reporter.writeStepResult(tc_id,scenarioName, "Download popup should be closed", "Close Button", e.getMessage(), "Fail", strReportFilename);
		}
	}
	public void newWindowClickSave(String strButtonLabel)
	{
		try {
			loadjacobdll();
			AutoItX x = new AutoItX();
			String title = "Internet Explorer";
			x.winWaitActive(title,"", 10);
			if (x.winExists(title)) {			
				x.controlClick(title, "", "Button2");	
				reporter.writeStepResult(tc_id,scenarioName, "Click on "+strButtonLabel, strButtonLabel, "Clicked "+strButtonLabel+" button successfully", "Pass", strReportFilename);
			}
			else
				reporter.writeStepResult(tc_id, scenarioName, "Click on " + strButtonLabel, strButtonLabel,
						"Not able to click on  button " + strButtonLabel, "Fail", strReportFilename);
		}
		catch(Exception e)
		{
			reporter.writeStepResult(tc_id, scenarioName, "Click on " + strButtonLabel, strButtonLabel,e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}
	}

	public void verifyPopupExist(String popupName)
	{
		try {
			loadjacobdll();
			AutoItX x = new AutoItX();
			if (popupName.equalsIgnoreCase("download completed")) {
				String title = "Merchant Servicing - Internet Explorer";
				String className1 = "[CLASS:IEFrame]";
				x.winWaitActive(title,"", 10);
				if (x.winExists(title) && x.winExists(className1)) {			
					reporter.writeStepResult(tc_id,scenarioName, "Verify "+popupName+" is exist", popupName, "The "+popupName+" is displayed on the page", "Pass", strReportFilename);
				} else {
					reporter.writeStepResult(tc_id,scenarioName, "Verify "+popupName+" is exist", popupName, "The "+popupName+" is not displayed on the page", "Fail", strReportFilename);
				}				
			} else {
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+popupName+" is exist", popupName, "The "+popupName+" is not exists on the page", "Fail", strReportFilename);
			}
		}
		catch(Exception e)
		{
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+popupName+" is exist", popupName, e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}
	}

	/**
	 * Run AutoIT
	 */
	public void RunAutoITFile(String fileName) {
		String file = null;
		try {
			String ProjectPath = new File("").getAbsolutePath();
			String AutoITFolder = ProjectPath+"\\Files\\AutoIT\\";
			file = AutoITFolder+fileName+".exe" ;
			Thread.sleep(2500);
			Runtime.getRuntime().exec(file);
			Thread.sleep(2500);
			reporter.writeStepResult(tc_id,scenarioName, "Execute the "+fileName+" AutoIT file",  "File Path: "+file, "The "+fileName+" is available in the path and Successfully executed", "Pass", strReportFilename);
		} catch (IOException e) {
			e.printStackTrace();
			reporter.writeStepResult(tc_id,scenarioName, "Execute the "+fileName+" AutoIT file",  "File Path: "+file, "The "+fileName+" is not available in the path", "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Execute the "+fileName+" AutoIT file",  "File Path: "+file, "The "+fileName+" is not executed", "Fail", strReportFilename);
		}
	}

	/**
	 * Press tab
	 */
	public void pressTab()
	{
		try {
			Robot rb=new Robot();
			rb.keyPress(KeyEvent.VK_SHIFT);
			rb.keyPress(KeyEvent.VK_TAB);
			rb.keyRelease(KeyEvent.VK_TAB);
			rb.keyRelease(KeyEvent.VK_SHIFT);
		}
		catch (AWTException e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Select drop down by visible text
	 */
	public void selectDropdownByVisibleText(WebElement element,String data)
	{
		Select sel=new Select(element);
		sel.selectByVisibleText(data);
	}

	/**
	 * Select drop down by index
	 */
	public void selectDropdownByIndex(WebElement element,int index)
	{
		Select sel=new Select(element);
		sel.selectByIndex(index);
	}

	/**
	 * Select drop down by value
	 */
	public void selectDropdownByValue(WebElement element,String data)
	{
		Select sel=new Select(element);
		sel.selectByValue(data);
	}

	/**
	 * Verify drop down
	 */
	public boolean verifyDropdownvalues(WebElement element,String expectedvalue)
	{
		//WebElement ele=driver.findElement(By.id("ddl_salesChannel"));//div[@id='div_SalesChannel']/div/select[@id='ddl_salesChannel']
		//wait.until(ExpectedConditions.elementToBeSelected(element));
		boolean exists=false;
		Select select = new Select(element);  
		List<WebElement> options = select.getOptions();  
		for(WebElement we:options)
		{
			if(we.getText().equals(expectedvalue))
			{
				we.click();
				exists=true;
			}
		}
		if(exists)
		{
			//reporter.writeStepResult(testCaseName, scenarioName, "Verify DropDown Values with actual value", expectedvalue, "Value", "Pass", reportFileName);
			System.out.println("Pass");
			return true;
		}
		else
		{
			// reporter.writeStepResult(testCaseName, scenarioName, "Verify DropDown Values with actual value", expectedvalue, "Value", "Fail", reportFileName);
			System.out.println("Fail");
			return false;
		}
	}

	/**
	 * Verify drop down values which are not present
	 */
	public boolean verifyDropDownValNotPresent(WebElement element,String expectedvalue)
	{
		boolean notExist=false;
		Select select = new Select(element);  
		List<WebElement> options = select.getOptions();  
		for(WebElement we:options)
		{
			if(!we.getText().equals(expectedvalue))
			{
				notExist=true;
			}
		}
		if(notExist)
		{
			//reporter.writeStepResult("testCaseName", "strScenarioName", "Verify DropDown Values with actual value", expectedvalue, "we.getText()", "Pass", "strReportFile");
			System.out.println("Pass");
			return true;
		}
		else
		{
			//reporter.writeStepResult("testCaseName", "strScenarioName", "Verify DropDown Values with actual value", expectedvalue, "we.getText()", "Fail", "strReportFile");
			System.out.println("Fail");
			return false;
		}
	}

	/**
	 * parse the json input
	 */
	public ArrayList<String> parseJson(String data, String testCaseID,String filename)
	{
		ArrayList<String> value = new ArrayList<String>();
		JSONParser parser = new JSONParser(); 
		try {     
			Object obj = parser.parse(new FileReader("Data/"+filename+".json"));

			JSONObject lev1 = (JSONObject) obj;
			Object jObj = lev1.get(testCaseID);
			if (jObj instanceof Map) 
			{
				HashMap<String, ArrayList<String>> map = (HashMap<String, ArrayList<String>>) jObj;
				Object in=map.get("Input");
				HashMap<String, ArrayList<String>> map1= (HashMap<String, ArrayList<String>>) in;
				//Object input=map1.get(data);
				//HashMap<String, ArrayList<String>> parseData= (HashMap<String, ArrayList<String>>) input;
				for (Entry<String, ArrayList<String>> entry : map1.entrySet())
				{
					for (int i = 0; i < entry.getValue().size(); i++) 
					{
						String val=entry.getValue().get(i);
						value.add(val);
					}
				}
				System.out.println(" "+value);
			}

		} catch (FileNotFoundException e) {
			System.out.println("Json file not present");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * Verify Element in sales channel summary report in Reporting page
	 */
	public boolean verifyAlertPresent()
	{
		WebDriverWait wait=new WebDriverWait(driver, 60);
		if(wait.until(ExpectedConditions.alertIsPresent())==null)
			return false;
		else
		{
			Alert alert=driver.switchTo().alert();
			String text=alert.getText();
			return true;
		}
	}

	/**
	 * To wait for a page to load until it gets in ready state
	 */
	public void waitForPageLoad() {
	
		String locatorMethod=null;
		String locatorValue=null;
		boolean display=false;
		
		new WebDriverWait(driver, 50).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver input) {
				return ((JavascriptExecutor) driver).executeScript("return document.readyState").equals("complete");				
			}
		});
		
			String [] locatorMethodName = readProperties("LoadingImage");
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
			
		while(!display)
			{
			try {
			display=!driver.findElement(By.xpath(locatorValue)).isDisplayed();
			wait1.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(locatorValue)));
			}
			catch(Exception e)
			{
				System.out.println("please wait Loading");
				continue;
			}
				}
	}

	/**
	 * To wait for specific element on the page for defined period[explicit wait]
	 * @param element
	 * @param seconds
	 * @return
	 */
	public boolean waitForElement(WebElement element, int seconds) {

		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
				.withTimeout(seconds, TimeUnit.SECONDS)
				.pollingEvery((seconds/5), TimeUnit.SECONDS);	
		return wait.until(ExpectedConditions.visibilityOf(element)) !=null;
	}

	public void verifyvaluepresentinthedropdown(String locatorkey,String Expectedoption)
	{
		boolean flag=false;
		try
		{
			WebElement element=getElementFluentWait(locatorkey);
			Select s = new Select(element);
			List<WebElement>Actualoptions=s.getOptions();
			for(WebElement el:Actualoptions)
			{
				if(Expectedoption.equalsIgnoreCase(el.getText())) {
					flag=true;
					break;
				}
			}
			if(flag)
				reporter.writeStepResult(tc_id,scenarioName, "Verify value present in the "+locatorkey+" Multiple selection field ", Expectedoption, "The Value "+Expectedoption+ "is  present in the "+locatorkey+" Multiple selection field", "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify value present in the "+locatorkey+" Multiple selection field ",Expectedoption,"The value "+Expectedoption+" is not present in the "+locatorkey+" Multiple selection field" , "Fail", strReportFilename);
		}
		catch(Exception e)
		{
			reporter.writeStepResult(tc_id,scenarioName, "Verify value present in the "+locatorkey+" Multiple selection field ",Expectedoption,e.getMessage(), "Fail", strReportFilename);
			e.printStackTrace();
		}
	}
	//To verify no.of.dropdowns

	public void verifynoofdropdownvalues(String locator,String expectedNumber)
	{
		int expectednumber=0;
		List<String>Actualoptions=null;
		boolean flag=false;
		try {
			Actualoptions=fetchDropDownValues(locator);
			if(expectedNumber.contains("greater than")) {
				expectedNumber=expectedNumber.replace("greater than ","");
				expectednumber=Integer.parseInt(expectedNumber);
				if(Actualoptions.size()>expectednumber)
				{
					flag=true;
				}
			}
			else
			{
				expectednumber=Integer.parseInt(expectedNumber);
				if(Actualoptions.size()==expectednumber)
				{
					flag=true;
				}
			}
			if(flag)
				reporter.writeStepResult(tc_id,scenarioName,"Dropdown values should be available","The Dropdown values of "+locator+" should be greater than "+expectednumber+" ","The Dropdown values of "+locator+" is greater than "+expectednumber+" ","Pass",strReportFilename);	
			else
				reporter.writeStepResult(tc_id,scenarioName,"Dropdown values should be available","The Dropdown values of "+locator+" should be greater than "+expectednumber+" ","The Dropdown values of "+locator+" is not greater than "+expectednumber+" ","Fail",strReportFilename);
		}catch(Exception e)
		{
			reporter.writeStepResult(tc_id,scenarioName,"Dropdown values should be available","The Dropdown values of "+locator+" should be greater than "+expectednumber+" ",e.getMessage(),"Fail",strReportFilename);
			e.printStackTrace();
		}
	}

	public void verifyValuePresentInTheTextBox(String Label, String locatorKey, String strExpectedText,String attributename)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			if(verifyElementPresent(element)) 
				strActualText = element.getAttribute(attributename).trim();
		} catch (NullPointerException e) {
			strActualText="";
		}
		try {
			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.equals(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}}

	public void verifyValuePresentInTheTextBoxContains(String Label, String locatorKey, String strExpectedText,String attributename)
	{
		String strActualText = null;
		WebElement element = null;
		try {
			element = getElementFluentWait(locatorKey);
			//waitForElementUsingVisibility(locatorKey);

			if(verifyElementPresent(element)) 
				strActualText = element.getAttribute(attributename).trim();	

			System.out.println("***"+strExpectedText+"***");
			System.out.println("***"+strActualText+"***");
			if (strActualText.contains(strExpectedText))
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Pass", strReportFilename);
			else
				reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, strActualText, "Fail", strReportFilename);
		} catch (Exception e) {
			reporter.writeStepResult(tc_id,scenarioName, "Verify "+Label+" value present in the TextBox", strExpectedText, e.getMessage(), "Fail", strReportFilename);
		}
	}

	public void refresh()
	{
		driver.navigate().refresh();
	}
	

	/**
	 * Insert into DataBase 
	 */
	public void insertIntoDatabase(String querystr) {
		Properties config = null,DBconfig=null;
		int affectedrows=0;
		String query=null;
		try {
			config = new ExecutionerClass().setEnv();	
			DBconfig=loadPropertiesFile(System.getProperty("user.dir") + "//src//test//resources//config//DBQueries.properties");
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			final String Driver= config.getProperty("DB_DriverClass"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName(Driver);
		} catch (ClassNotFoundException e1) {
			System.out.println("Unable to load the driver class! "+e1);
			reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, e1.getMessage(), "Fail", strReportFilename);
			e1.printStackTrace();
		} 
		Connection dbConnection = null;
		try {
			final String DBUrl = config.getProperty("DB_Url"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			final String DBName = config.getProperty("DB_DBName"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			/*String connectionUrl = "jdbc:sqlserver://JVLDBUAT11:1433;" +
					"databaseName=HCSdb;integratedSecurity=true;";*/
			String connectionUrl =DBUrl+DBName;

			dbConnection=DriverManager.getConnection(connectionUrl);
			System.out.println("Database Connection Successfully established");
		} catch (SQLException e) {						
			System.out.println("Couldnâ€™t get connection! "+e);
			reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, e.getMessage(), "Fail", strReportFilename);
		}
		
		if (dbConnection != null) {
			Statement stmt = null;
			
			try {
				stmt = dbConnection.createStatement();
				 query=DBconfig.getProperty(querystr);
				affectedrows=stmt.executeUpdate(query);
				if(affectedrows!=0)
				{
					reporter.writeStepResult(tc_id,scenarioName, "Values should be inserted into Database", querystr,"Inserted the values into DB and affected rows are "+affectedrows+"" , "Pass", strReportFilename);
				}
				else
					reporter.writeStepResult(tc_id,scenarioName, "Values should be inserted into Database", querystr,"Values are not inserted into DB and no rows are affected" , "Fail", strReportFilename);
				
			} catch (Exception e) {
				reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, e.getMessage(), "Fail", strReportFilename);
			} finally {
				try {
					stmt.close();
					dbConnection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, ""+dbConnection, "Fail", strReportFilename);
		}
		
	}
	
	/**
	 * Delete from DataBase 
	 */
	public void deleteFromDatabase(String querystr) {
		Properties config = null,DBconfig=null;
		int affectedrows=0;
		String query=null;
		try {
			config = new ExecutionerClass().setEnv();	
			DBconfig=loadPropertiesFile(System.getProperty("user.dir") + "//src//test//resources//config//DBQueries.properties");
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			final String Driver= config.getProperty("DB_DriverClass"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName(Driver);
		} catch (ClassNotFoundException e1) {
			System.out.println("Unable to load the driver class! "+e1);
			reporter.writeStepResult(tc_id,scenarioName, "Delete the values From Database", querystr, e1.getMessage(), "Fail", strReportFilename);
			e1.printStackTrace();
		} 
		Connection dbConnection = null;
		try {
			final String DBUrl = config.getProperty("DB_Url"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			final String DBName = config.getProperty("DB_DBName"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			/*String connectionUrl = "jdbc:sqlserver://JVLDBUAT11:1433;" +
					"databaseName=HCSdb;integratedSecurity=true;";*/
			String connectionUrl =DBUrl+DBName;

			dbConnection=DriverManager.getConnection(connectionUrl);
			System.out.println("Database Connection Successfully established");
		} catch (SQLException e) {						
			System.out.println("Couldnâ€™t get connection! "+e);
			reporter.writeStepResult(tc_id,scenarioName, "Delete the values From Database", querystr, e.getMessage(), "Fail", strReportFilename);
		}
		
		if (dbConnection != null) {
			Statement stmt = null;
			
				try {
					stmt = dbConnection.createStatement();
					query=DBconfig.getProperty(querystr);
					affectedrows=stmt.executeUpdate(query);
					if(affectedrows!=0)
					{
						reporter.writeStepResult(tc_id,scenarioName, "Values should be Deleted from Database", querystr,"Deleted the values from DB and affected rows are "+affectedrows+"" , "Pass", strReportFilename);
					}
					else
						reporter.writeStepResult(tc_id,scenarioName, "Values should be Deleted from Database", querystr,"Values are not Deleted from DB and no rows are affected" , "Fail", strReportFilename);
					
				
			} catch (Exception e) {
				reporter.writeStepResult(tc_id,scenarioName, "Delete the values From Database", querystr, e.getMessage(), "Fail", strReportFilename);
			} finally {
				try {
					stmt.close();
					dbConnection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			reporter.writeStepResult(tc_id,scenarioName, "Delete the values From Database", querystr, ""+dbConnection, "Fail", strReportFilename);
		}
		
	}
	/**
	 * 
	Fetch No.of.rows
	 */
	public int fetchNumberOfRows(String locatorKey) {
		int ActualNumberOfRows=0;
		try {
			List<WebElement> element = getWebElements(locatorKey);
			 ActualNumberOfRows = element.size();
		}catch(Exception e) {
			e.printStackTrace();
		}
		return ActualNumberOfRows;
	}
	/**
	 * 
	 * checkDatabaseValueEmpty
	 */
	public void checkDatabaseValueEmpty(String querystr) {
		Properties config = null,DBconfig=null;
		String query=null;
		try {
			config = new ExecutionerClass().setEnv();	
			DBconfig=loadPropertiesFile(System.getProperty("user.dir") + "//src//test//resources//config//DBQueries.properties");
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			final String Driver= config.getProperty("DB_DriverClass"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName(Driver);
		} catch (ClassNotFoundException e1) {
			System.out.println("Unable to load the driver class! "+e1);
			reporter.writeStepResult(tc_id,scenarioName, "Fetch the Values from Database", querystr, e1.getMessage(), "Fail", strReportFilename);
			e1.printStackTrace();
		} 
		Connection dbConnection = null;
		try {
			final String DBUrl = config.getProperty("DB_Url"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			final String DBName = config.getProperty("DB_DBName"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			/*String connectionUrl = "jdbc:sqlserver://JVLDBUAT11:1433;" +
					"databaseName=HCSdb;integratedSecurity=true;";*/
			String connectionUrl =DBUrl+DBName;

			dbConnection=DriverManager.getConnection(connectionUrl);
			System.out.println("Database Connection Successfully established");
		} catch (SQLException e) {						
			System.out.println("Couldnâ€™t get connection! "+e);
			reporter.writeStepResult(tc_id,scenarioName, "Fetch the Values from Database", querystr, e.getMessage(), "Fail", strReportFilename);
		}
		
		if (dbConnection != null) {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				stmt = dbConnection.createStatement();
				query=DBconfig.getProperty(querystr);
				rs = stmt.executeQuery(query);

				if (!rs.next()) {
					reporter.writeStepResult(tc_id,scenarioName, "Check values are not available in Database", querystr,"Values are not available in DB" , "Pass", strReportFilename);
				}
				else
					reporter.writeStepResult(tc_id,scenarioName, "Check values are not available in Database", querystr,"Values are available in DB" , "Fail", strReportFilename);
			}
			catch(Exception e)
			{
				reporter.writeStepResult(tc_id,scenarioName, "Check values are not available in Database", querystr,e.getMessage(),"Fail", strReportFilename);
			}
		}
				}
	
	/**
	 * Move to element using actions
	 */
	
	public void hovering(String locatorkey)
	{
		WebElement element=getElementFluentWait(locatorkey);
		Actions hover=new Actions(driver);
		hover.moveToElement(element).build().perform();
	}
	
	/**
	 * wait for element to appear
	 */
	public void waitForElementToAppear(String locatorkey) {
		
		String locatorMethod=null;
		String locatorValue=null;
		boolean display=false;
		
			String [] locatorMethodName = readProperties(locatorkey);
			locatorMethod = locatorMethodName[0];
			locatorValue = locatorMethodName[1];
			
		while(!display)
			{
			try {
			display=driver.findElement(By.xpath(locatorValue)).isDisplayed();
			wait1.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(locatorValue)));
			}
			catch(Exception e)
			{
				System.out.println("please wait Loading");
				continue;
			}
				}
	}

	/**
	 * Fetch value from DataBase 
	 */
	public void FetchvaluefromDatabase(String querystr) {
		Properties config = null,DBconfig=null;
		ResultSet rs = null;
		String query=null;
		try {
			config = new ExecutionerClass().setEnv();	
			DBconfig=loadPropertiesFile(System.getProperty("user.dir") + "//src//test//resources//config//DBQueries.properties");
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			final String Driver= config.getProperty("DB_DriverClass"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName(Driver);
		} catch (ClassNotFoundException e1) {
			System.out.println("Unable to load the driver class! "+e1);
			reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, e1.getMessage(), "Fail", strReportFilename);
			e1.printStackTrace();
		} 
		Connection dbConnection = null;
		try {
			final String DBUrl = config.getProperty("DB_Url"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			final String DBName = config.getProperty("DB_DBName"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"11");
			/*String connectionUrl = "jdbc:sqlserver://JVLDBUAT11:1433;" +
					"databaseName=HCSdb;integratedSecurity=true;";*/
			String connectionUrl =DBUrl+DBName;

			dbConnection=DriverManager.getConnection(connectionUrl);
			System.out.println("Database Connection Successfully established");
		} catch (SQLException e) {						
			System.out.println("Couldnâ€™t get connection! "+e);
			reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, e.getMessage(), "Fail", strReportFilename);
		}
		
		if (dbConnection != null) {
			Statement stmt = null;
			
			try {
				stmt = dbConnection.createStatement();
				query=DBconfig.getProperty(querystr);
				rs=stmt.executeQuery(query);
				while(rs.next()) {
					rs.getString(1);
				
					rs.getRow();
					rs.getString("EquipAgreeOID");
					rs.getInt("EquipAgreeOID");
					reporter.writeStepResult(tc_id,scenarioName, "Values should be inserted into Database", querystr,"Inserted the values into DB and affected rows are " , "Pass", strReportFilename);
				}
				
					reporter.writeStepResult(tc_id,scenarioName, "Values should be inserted into Database", querystr,"Values are not inserted into DB and no rows are affected" , "Fail", strReportFilename);
				
			} catch (Exception e) {
				reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, e.getMessage(), "Fail", strReportFilename);
			} finally {
				try {
					stmt.close();
					dbConnection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			reporter.writeStepResult(tc_id,scenarioName, "Insert the values into Database", querystr, ""+dbConnection, "Fail", strReportFilename);
		}
		
	}
	
	/**
	 * Update DataBase 
	 */
	public void updateDatabase(String querystr) {
		Properties config = null,DBconfig=null;
		int affectedrows=0;
		String query=null;
		try {
			config = new ExecutionerClass().setEnv();	
			DBconfig=loadPropertiesFile(System.getProperty("user.dir") + "//src//test//resources//config//DBQueries.properties");
		} catch (Exception e2) {
			e2.printStackTrace();
		}

		try {
			final String Driver= config.getProperty("DB_DriverClass"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"06");
			//Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
			Class.forName(Driver);
		} catch (ClassNotFoundException e1) {
			System.out.println("Unable to load the driver class! "+e1);
			reporter.writeStepResult(tc_id,scenarioName, "Update the values in Database", querystr, e1.getMessage(), "Fail", strReportFilename);
			e1.printStackTrace();
		} 
		Connection dbConnection = null;
		try {
			final String DBUrl = config.getProperty("DB_Url"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"06");
			final String DBName = config.getProperty("DB_DBName"+"_"+loadPropertiesFile("Config/Sys.properties").getProperty("Execution_Environment")+"_"+"06");
			/*String connectionUrl = "jdbc:sqlserver://JVLDBUAT11:1433;" +
					"databaseName=INVDB;integratedSecurity=true;";*/
			String connectionUrl =DBUrl+DBName;

			dbConnection=DriverManager.getConnection(connectionUrl);
			System.out.println("Database Connection Successfully established");
		} catch (SQLException e) {						
			System.out.println("Couldnâ€™t get connection! "+e);
			reporter.writeStepResult(tc_id,scenarioName, "Update the values in Database", querystr, e.getMessage(), "Fail", strReportFilename);
		}
		
		if (dbConnection != null) {
			Statement stmt = null;
			
				try {
					stmt = dbConnection.createStatement();
					query=DBconfig.getProperty(querystr);
					affectedrows=stmt.executeUpdate(query);
					if(affectedrows!=0)
					{
						reporter.writeStepResult(tc_id,scenarioName, "Values should be updated in Database", querystr,"Updated the values in DB and affected rows are "+affectedrows+"" , "Pass", strReportFilename);
					}
					else
						reporter.writeStepResult(tc_id,scenarioName, "Values should be updated in Database", querystr,"Values are not Updated in DB and no rows are affected" , "Fail", strReportFilename);
					
				
			} catch (Exception e) {
				reporter.writeStepResult(tc_id,scenarioName, "Update the values in Database", querystr, e.getMessage(), "Fail", strReportFilename);
			} finally {
				try {
					stmt.close();
					dbConnection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		} else {
			reporter.writeStepResult(tc_id,scenarioName, "Update the values in Database", querystr, ""+dbConnection, "Fail", strReportFilename);
		}
		
	}

	/**
	 * Paste the data using action class
	 */
	
	public void Paste_data_using_actions(String locatorkey)
	{
		WebElement element=getElementFluentWait(locatorkey);
		Actions hover=new Actions(driver);
		hover.sendKeys(element, Keys.chord(Keys.CONTROL,"v")).perform();
	}
	
	
	/**
	 * parse the json input
	 */
	public void getReleaseName(String filename)
	{/*
		String value ="";
		JSONParser parser = new JSONParser(); 
	    try {     
	        Object obj = parser.parse(new FileReader("Data/"+filename+".json"));

	        JSONObject lev1 = (JSONObject) obj;
	        Object jObj = lev1.get("ReleaseDetails");

	        if (jObj instanceof Map) 
	        {
	        	//HashMap<String, ArrayList<String>> map = (HashMap<String, ArrayList<String>>) jObj;
	            //Object in=map.get("Input");
	            HashMap<String, ArrayList<String>> map1= (HashMap<String, ArrayList<String>>) jObj;
	            //Object input=map1.get(data);
	            //HashMap<String, ArrayList<String>> parseData= (HashMap<String, ArrayList<String>>) input;
	        	 for (Entry<String, ArrayList<String>> entry : map1.entrySet())
	        	 {
	        		 for (int i = 0; i < entry.getValue().size(); i++) 
	        		{
	        			value=entry.getValue().get(i);
	        			//value.add(val);
					}
	        	 }
	            System.out.println(" "+value);
	        }

	    } catch (FileNotFoundException e) {
	        e.printStackTrace();
	    } catch (IOException e) {
	        e.printStackTrace();
	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    return value;
	 */}
}