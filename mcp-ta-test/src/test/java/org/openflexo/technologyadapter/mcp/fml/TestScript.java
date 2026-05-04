

package org.openflexo.technologyadapter.mcp.fml;

import org.junit.BeforeClass;
import org.junit.Test;
import org.openflexo.foundation.fml.cli.ParseException;
import org.openflexo.foundation.fml.cli.command.FMLCommandExecutionException;
import org.openflexo.pamela.exceptions.ModelDefinitionException;

import java.io.IOException;
import java.util.logging.Logger;

import static org.junit.Assume.assumeTrue;


public class TestScript {

    private static AutomatedTests automatedTests;
    private static boolean            npxAvailable;
    private static final Logger logger =
            Logger.getLogger(TestScript.class.getPackage().getName());
    
    @BeforeClass
    public static void setUp() throws Exception {
        System.out.println("========================================");
        System.out.println("Initializing Test Infrastructure");
        System.out.println("========================================");

        
        
        automatedTests = new AutomatedTests(null, "SpecificScriptRunner");

        System.out.println("Available scripts:");
        System.out.println(AutomatedTests.listAvailableScripts());
         npxAvailable = isNpxRunnable();
        logger.info("npx available as subprocess: " + npxAvailable);
        System.out.println("========================================\n");
    }


    
    @Test
    public void testModelLogic() throws Exception {
        System.out.println("\n>>> Running TestModelLogicTest.fmlscript\n");
        assumeTrue(
                "Skipping TestMCPFilesystem.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestMCPFilesystem.fmlscript");
    }
    @Test
    public void testBraveAssitant() throws Exception {
        System.out.println("\n>>> Running TestModelLogicTest.fmlscript\n");
        assumeTrue(
                "Skipping TestMCPBrave.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestMCPBrave.fmlscript");
    }
    @Test
    public void testClaudeAssitant() throws Exception {
        System.out.println("\n>>> Running TestModelLogicTest.fmlscript\n");
        assumeTrue(
                "Skipping TestMCPClaude.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestMCPClaude.fmlscript");
    }
    @Test
    public void testGemeniAssitant() throws Exception {
        System.out.println("\n>>> Running TestMCPGemini.fmlscript\n");
        assumeTrue(
                "Skipping TestMCPGemini.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestMCPGemini.fmlscript");
    }

    @Test
    public void testCodeReviewer() throws Exception {
        System.out.println("\n>>> Running TestCodeReviewer.fmlscript\n");
        assumeTrue(
                "Skipping TestCodeReviewer.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestCodeReviewer.fmlscript");
    }

    @Test
    public void testEditionActions() throws Exception {
        System.out.println("\n>>> Running TestMCPEditionActions.fmlscript\n");
        assumeTrue(
                "Skipping TestMCPEditionActions.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestMCPEditionActions.fmlscript");
    }
    @Test
    public void testPrinterModel() throws Exception {
        System.out.println("\n>>> Running TestPrintMonitor.fmlscript\n");
        assumeTrue(
                "Skipping TestPrintMonitor.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestPrintMonitor.fmlscript");
    }
    @Test
    public void testMcpHttpTransport() throws Exception {
        System.out.println("\n>>> Running TestMCPHttpTransport.fmlscript\n");
        assumeTrue(
                "Skipping TestMCPHttpTransport.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestMCPHttpTransport.fmlscript");
    }
    @Test
    public void testInventoryVision() throws Exception {
        System.out.println("\n>>> Running TestInventoryVision.fmlscript\n");
        assumeTrue(
                "Skipping TestInventoryVision.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestInventoryVision.fmlscript");
    }

    @Test
    public void testImageCapture() throws Exception {
        System.out.println("\n>>> Running TestImageModel.fmlscript\n");
        assumeTrue(
                "Skipping TestImageModel.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestImageModel.fmlscript");
    }    @Test
    public void testAudio() throws Exception {
        System.out.println("\n>>> Running TestAudio.fmlscript\n");
        assumeTrue(
                "Skipping TestAudio.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestAudio.fmlscript");
    }
    @Test
    public void testAudioandVideo() throws Exception {
        System.out.println("\n>>> Running TestAudioAndImage.fmlscript\n");
        assumeTrue(
                "Skipping TestAudioAndImage.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestAudioAndImage.fmlscript");
    }    @Test
    public void testPureMCP() throws Exception {
        System.out.println("\n>>> Running TestPureMCP.fmlscript\n");
        assumeTrue(
                "Skipping TestPureMCP.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestPureMCP.fmlscript");
    }
    @Test
    public void testSenario2() throws Exception {
        System.out.println("\n>>> Running TestPureMCPS2.fmlscript\n");
        assumeTrue(
                "Skipping TestPureMCPS2.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestPureMCPS2.fmlscript");
    }

    @Test
    public void testHandMesurementFinal() throws Exception {
        System.out.println("\n>>> Running TestHandMeasurementProfessional.fmlscript\n");
        assumeTrue(
                "Skipping TestHandMeasurementProfessional.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestHandMeasurementProfessional.fmlscript");
    }

    @Test
    public void testStatelliteSync() throws Exception {
        System.out.println("\n>>> Running TestSatelliteSync.fmlscript\n");
        assumeTrue(
                "Skipping TestSatelliteSync.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestSatelliteSync.fmlscript");
    }
    @Test
    public void testMcpFMI() throws Exception {
        System.out.println("\n>>> Running TestFMIConnection.fmlscript\n");
        assumeTrue(
                "Skipping TestFMIConnection.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestFMIConnection.fmlscript");
    }
    @Test
    public void testMcpFMU() throws Exception {
        System.out.println("\n>>> Running TestFMUSimulation.fmlscript\n");
        assumeTrue(
                "Skipping TestFMUSimulation.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestFMUSimulation.fmlscript");
    }
    @Test
    public void testDemoSim() throws Exception {
        System.out.println("\n>>> Running RunDemo.fmlscript\n");
        assumeTrue(
                "Skipping RunDemo.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("RunDemo.fmlscript");
    }

    @Test
    public void testCastModel() throws Exception {
        System.out.println("\n>>> Running TestCastModel.fmlscript\n");
        assumeTrue(
                "Skipping TestCastModel.fmlscript — npx not runnable. " +
                        "Install Node.js to execute live MCP server tests.",
                npxAvailable
        );

        automatedTests.runSpecificScript("TestCastModel.fmlscript");
    }


    
    @Test
    public void testListScripts() {
        System.out.println("\n>>> Available FML Scripts:\n");
        System.out.println(AutomatedTests.listAvailableScripts());
    }
    private static boolean isNpxRunnable() {
        try {
            String[] cmd = System.getProperty("os.name", "").toLowerCase().contains("win")
                    ? new String[]{"cmd", "/c", "npx", "--version"}
                    : new String[]{"npx", "--version"};
            Process p = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            // Drain stdout (Java 8 compatible)
            byte[] buf = new byte[512];
            while (p.getInputStream().read(buf) != -1) { /* discard */ }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

}