package com.kms.katalon.core.reporting;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.kms.katalon.core.constants.StringConstants;
import com.kms.katalon.core.logging.TestSuiteXMLLogParser;
import com.kms.katalon.core.logging.XMLLoggerParser;
import com.kms.katalon.core.logging.XMLParserException;
import com.kms.katalon.core.logging.XmlLogRecord;
import com.kms.katalon.core.logging.model.ILogRecord;
import com.kms.katalon.core.logging.model.MessageLogRecord;
import com.kms.katalon.core.logging.model.TestStatus.TestStatusValue;
import com.kms.katalon.core.logging.model.TestSuiteLogRecord;
import com.kms.katalon.core.reporting.template.ResourceLoader;
import com.kms.katalon.core.testdata.reader.CsvWriter;

public class ReportUtil {

    private static StringBuilder generateVars(List<String> strings, TestSuiteLogRecord suiteLogEntity,
            StringBuilder model) throws IOException {
        StringBuilder sb = new StringBuilder();
        List<String> lines = IOUtils
                .readLines(ResourceLoader.class.getResourceAsStream(ResourceLoader.HTML_TEMPLATE_VARS));
        for (String line : lines) {
            if (line.equals(ResourceLoader.HTML_TEMPLATE_SUITE_MODEL_TOKEN)) {
                sb.append(model);
            } else if (line.equals(ResourceLoader.HTML_TEMPLATE_STRINGS_CONSTANT_TOKEN)) {
                StringBuilder stringSb = listToStringArray(strings);
                sb.append(stringSb);
            } else if (line.equals(ResourceLoader.HTML_TEMPLATE_EXEC_ENV_TOKEN)) {
                StringBuilder envInfoSb = new StringBuilder();
                envInfoSb.append("{");
                envInfoSb.append(String.format("\"host\" : \"%s\", ", suiteLogEntity.getHostName()));
                envInfoSb.append(String.format("\"os\" : \"%s\", ", suiteLogEntity.getOs()));
                envInfoSb.append(String.format("\"" + StringConstants.APP_VERSION + "\" : \"%s\", ",
                        suiteLogEntity.getAppVersion()));
                if (suiteLogEntity.getBrowser() != null && !suiteLogEntity.getBrowser().equals("")) {
                    if (suiteLogEntity.getRunData().containsKey("browser")) {
                        envInfoSb.append(
                                String.format("\"browser\" : \"%s\",", suiteLogEntity.getRunData().get("browser")));
                    } else {
                        envInfoSb.append(String.format("\"browser\" : \"%s\",", suiteLogEntity.getBrowser()));
                    }
                }
                if (suiteLogEntity.getDeviceName() != null && !suiteLogEntity.getDeviceName().equals("")) {
                    envInfoSb.append(String.format("\"deviceName\" : \"%s\",", suiteLogEntity.getDeviceName()));
                }
                if (suiteLogEntity.getDeviceName() != null && !suiteLogEntity.getDeviceName().equals("")) {
                    envInfoSb.append(String.format("\"devicePlatform\" : \"%s\",", suiteLogEntity.getDevicePlatform()));
                }
                envInfoSb.append("\"\" : \"\"");

                envInfoSb.append("}");
                sb.append(envInfoSb);
            } else {
                sb.append(line);
                sb.append("\n");
            }
        }
        return sb;
    }

    public static String getOs() {
        return System.getProperty("os.name") + " " + System.getProperty("sun.arch.data.model") + "bit";
    }

    public static String getHostName() {
        String hostName = "Unknown";
        try {
            InetAddress addr;
            addr = InetAddress.getLocalHost();
            hostName = addr.getCanonicalHostName();
        } catch (UnknownHostException ex) {}
        return hostName;
    }

    private static StringBuilder listToStringArray(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (int idx = 0; idx < strings.size(); idx++) {
            if (idx > 0) {
                sb.append(",");
            }
            sb.append("\"" + (strings.get(idx) == null ? ""
                    : strings.get(idx).equals("*") ? strings.get(idx) : ("*" + strings.get(idx))) + "\"");
        }
        return sb;
    }

    private static void collectInfoLines(ILogRecord logRecord, List<ILogRecord> rmvLogs) {
        if (logRecord instanceof MessageLogRecord) {
            if (logRecord.getStatus().getStatusValue() == TestStatusValue.INCOMPLETE
                    || logRecord.getStatus().getStatusValue() == TestStatusValue.INFO) {
                rmvLogs.add(logRecord);
            }
        }
        for (ILogRecord childLogRecord : logRecord.getChildRecords()) {
            collectInfoLines(childLogRecord, rmvLogs);
        }
    }

    public static void writeLogRecordToFiles(String logFolder) throws Exception {
        TestSuiteLogRecord testSuiteLogRecord = generate(logFolder);
        if (testSuiteLogRecord != null) {
            writeLogRecordToFiles(testSuiteLogRecord, new File(logFolder));
        }
    }

    public static void writeLogRecordToCSVFile(TestSuiteLogRecord suiteLogEntity, File destFile,
            List<ILogRecord> filteredTestCases) throws IOException {
        CsvWriter.writeCsvReport(suiteLogEntity, destFile, filteredTestCases);
    }

    public static void writeLogRecordToFiles(TestSuiteLogRecord suiteLogEntity, File logFolder) throws Exception {
        List<String> strings = new LinkedList<String>();

        JsSuiteModel jsSuiteModel = new JsSuiteModel(suiteLogEntity, strings);
        StringBuilder sbModel = jsSuiteModel.toArrayString();

        StringBuilder htmlSb = new StringBuilder();
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_FILE, htmlSb);
        htmlSb.append(generateVars(strings, suiteLogEntity, sbModel));
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_CONTENT, htmlSb);

        // Write main HTML Report
        FileUtils.writeStringToFile(new File(logFolder, logFolder.getName() + ".html"), htmlSb.toString());

        // Write CSV file
        CsvWriter.writeCsvReport(suiteLogEntity, new File(logFolder, logFolder.getName() + ".csv"),
                Arrays.asList(suiteLogEntity.getChildRecords()));

        List<ILogRecord> infoLogs = new ArrayList<ILogRecord>();
        collectInfoLines(suiteLogEntity, infoLogs);
        for (ILogRecord infoLog : infoLogs) {
            infoLog.getParentLogRecord().removeChildRecord(infoLog);
        }
        strings = new LinkedList<String>();
        jsSuiteModel = new JsSuiteModel(suiteLogEntity, strings);
        sbModel = jsSuiteModel.toArrayString();
        htmlSb = new StringBuilder();
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_FILE, htmlSb);
        htmlSb.append(generateVars(strings, suiteLogEntity, sbModel));
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_CONTENT, htmlSb);
        FileUtils.writeStringToFile(new File(logFolder, "Report.html"), htmlSb.toString());

    }

    public static void writeLogRecordToHTMLFile(TestSuiteLogRecord suiteLogEntity, File destFile,
            List<ILogRecord> filteredTestCases) throws IOException, URISyntaxException {

        List<String> strings = new LinkedList<String>();

        JsSuiteModel jsSuiteModel = new JsSuiteModel(suiteLogEntity, strings);
        StringBuilder sbModel = jsSuiteModel.toArrayString();

        StringBuilder htmlSb = new StringBuilder();
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_FILE, htmlSb);
        htmlSb.append(generateVars(strings, suiteLogEntity, sbModel));
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_CONTENT, htmlSb);

        List<ILogRecord> infoLogs = new ArrayList<ILogRecord>();
        collectInfoLines(suiteLogEntity, infoLogs);
        for (ILogRecord infoLog : infoLogs) {
            infoLog.getParentLogRecord().removeChildRecord(infoLog);
        }
        strings = new LinkedList<String>();
        jsSuiteModel = new JsSuiteModel(suiteLogEntity, strings, filteredTestCases);
        sbModel = jsSuiteModel.toArrayString();
        htmlSb = new StringBuilder();
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_FILE, htmlSb);
        htmlSb.append(generateVars(strings, suiteLogEntity, sbModel));
        readFileToStringBuilder(ResourceLoader.HTML_TEMPLATE_CONTENT, htmlSb);
        FileUtils.writeStringToFile(destFile, htmlSb.toString());
    }

    public static List<XmlLogRecord> getAllLogRecords(String logFolder)
            throws XMLParserException, IOException, XMLStreamException {
        return XMLLoggerParser.readFromLogFolder(logFolder);
    }

    public static TestSuiteLogRecord generate(String logFolder, IProgressMonitor progressMonitor)
            throws XMLParserException, IOException, XMLStreamException {
        return new TestSuiteXMLLogParser().readTestSuiteLogFromXMLFiles(logFolder, progressMonitor);
    }

    public static TestSuiteLogRecord generate(String logFolder)
            throws XMLParserException, IOException, XMLStreamException {
        return generate(logFolder, new NullProgressMonitor());
    }

    private static void readFileToStringBuilder(String fileName, StringBuilder sb)
            throws IOException, URISyntaxException {
        String path = ResourceLoader.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path = URLDecoder.decode(path, "utf-8");
        File jarFile = new File(path);
        if (jarFile.isFile()) {
            JarFile jar = new JarFile(jarFile);
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String name = jarEntry.getName();
                if (name.endsWith(fileName)) {
                    StringBuilderWriter sbWriter = new StringBuilderWriter(new StringBuilder());
                    IOUtils.copy(jar.getInputStream(jarEntry), sbWriter);
                    sbWriter.flush();
                    sbWriter.close();
                    sb.append(sbWriter.getBuilder());
                    break;
                }
            }
            jar.close();
        } else { // Run with IDE
                 // sb.append(FileUtils.readFileToString(new
                 // File(ResourceLoader.class.getResource(fileName).getContent()
                 // )));
            InputStream is = (InputStream) ResourceLoader.class.getResource(fileName).getContent();
            sb.append(IOUtils.toString(is, "UTF-8"));
        }
    }
}
