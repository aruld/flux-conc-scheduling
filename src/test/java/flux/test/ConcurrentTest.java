package flux.test;

import flux.*;
import flux.listeners.GenomesProcessor;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ConcurrentTest extends AbstractFluxTest {

    @Override
    public void setUpTest() throws Exception {
        clearEngine = true;
        super.setUpTest();
    }

    @Test
    public void testConcurrentPut() throws Exception {
        FlowChart flowChart = EngineHelper.makeFlowChart(GenomesProcessor.genomesChildWorkflowTemplate);
        NullAction printer = flowChart.makeNullAction("printer");
        printer.setPrescript("System.out.println(\"Processing file ${file_name} of size ${file_size}\");");

        engine.getRepositoryAdministrator().put(flowChart, true);
        System.out.println("Added child template to repository.");

        FlowChart genomesWorkflow = EngineHelper.makeFlowChart(GenomesProcessor.genomesParentWorkflow);
        JavaAction genomesProcessor = genomesWorkflow.makeJavaAction("genomes processor");
        genomesProcessor.setListener(GenomesProcessor.class);

        String name = engine.put(genomesWorkflow);
        System.out.println("Scheduled genomes parent workflow : " + name);
        assertEquals(1000, waitForRuns(GenomesProcessor.joinNamespace, 1000, 10, 120));
    }
}
