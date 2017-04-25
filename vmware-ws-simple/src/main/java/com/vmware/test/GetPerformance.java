package com.vmware.test;

import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ccl on 17/3/17.
 */
public class GetPerformance extends ConnectedVimServiceBase {
    /*
    * Map of counter IDs indexed by counter name.
    * The full counter name is the hash key - group.name.ROLLUP-TYPE.
    * */
    private static HashMap<String, Integer> countersIdMap = new HashMap<String, Integer>();
    /*
    * Map of performance counter data (PerfCounterInfo) indexed by counter ID * (PerfCounterInfo.key property).
    */
    private static HashMap<Integer, PerfCounterInfo> countersInfoMap = new HashMap<Integer, PerfCounterInfo>();


    public void perfCountersInfo() throws Exception {
        ManagedObjectReference performanceMgrRef = serviceContent.getPerfManager();
        ManagedObjectReference pCollectorRef = serviceContent.getPropertyCollector();
        /*
        * Create an object spec to define the context to retrieve the PerformanceManager property.
        * */
        ObjectSpec oSpec = new ObjectSpec();
        oSpec.setObj(performanceMgrRef);
        /*
        * Specify the property for retrieval
        * (PerformanceManager.perfCounter is the list of counters of which the vCenter Server is aware.)
        */
        PropertySpec pSpec = new PropertySpec();
        pSpec.setType("PerformanceManager");
        pSpec.getPathSet().add("perfCounter");
        /*
        * Create a PropertyFilterSpec and add the object and property specs to it.
        */
        PropertyFilterSpec fSpec = new PropertyFilterSpec();
        fSpec.getObjectSet().add(oSpec);
        fSpec.getPropSet().add(pSpec);
        /*
        206 / 241
        Breeze.1978@aliyun.com
         * Create a list for the filter and add the spec to it.
        */
        List<PropertyFilterSpec> fSpecList = new ArrayList<PropertyFilterSpec>();
        fSpecList.add(fSpec);
        /*
        * Get the performance counters from the server.
        */
        RetrieveOptions ro = new RetrieveOptions();
        RetrieveResult props = vimPort.retrievePropertiesEx(pCollectorRef, fSpecList, ro);
        /*
        * Turn the retrieved results into an array of PerfCounterInfo.
        */
        List<PerfCounterInfo> perfCounters = new ArrayList<PerfCounterInfo>();
        if (props != null) {
            for (ObjectContent oc : props.getObjects()) {
                List<DynamicProperty> dps = oc.getPropSet();
                if (dps != null) {
                    for (DynamicProperty dp : dps) {
                        /*
                        * DynamicProperty.val is an xsd:anyType value to be cast
                        * to an ArrayOfPerfCounterInfo and List<PerfCounterInfo>.
                        */
                        perfCounters = ((ArrayOfPerfCounterInfo) dp.getVal()).getPerfCounterInfo();
                    }
                }
            }
        }
        /*
        assigned
        to a
        =* Cycle through the PerfCounterInfo objects and load the maps.
        */
        for (PerfCounterInfo perfCounter : perfCounters) {
            Integer counterId = new Integer(perfCounter.getKey());
            /*
            * This map uses the counter ID to index performance counter metadata.
            */
            countersInfoMap.put(counterId, perfCounter);
            /*
            * Obtain the name components and construct the full counter name, * for example – power.power.AVERAGE.
            * This map uses the full counter name to index counter IDs.
            */
            String counterGroup = perfCounter.getGroupInfo().getKey();
            String counterName = perfCounter.getNameInfo().getKey();
            String counterRollupType = perfCounter.getRollupType().toString();
            String fullCounterName = counterGroup + "." + counterName + "." + counterRollupType;
            /*
            * Store the counter ID in a map indexed by the full counter name.
            */
            countersIdMap.put(fullCounterName, counterId);
        }

        //System.out.println(countersIdMap);
        //System.out.println("-----------------");
        //System.out.println(countersInfoMap);

        /*
        * Use <group>.<name>.<ROLLUP-TYPE> path specification to identify counters.
        */
//        String[] counterNames = new String[] {"disk.provisioned.LATEST","cpu.usage.AVERAGE",
//                "mem.usage.AVERAGE", "power.power.AVERAGE","mem.overhead.average","mem.consumed.AVERAGE"};
        String[] counterNames = new String[] {"mem.usage.AVERAGE"};
        /*
        * Create the list of PerfMetricIds, one for each counter.
        * */
        List<PerfMetricId> perfMetricIds = new ArrayList<PerfMetricId>();

        for(int i = 0; i < counterNames.length; i++) {
            /*
            * Create the PerfMetricId object for the counterName.
            * Use an asterisk to select all metrics associated with counterId (instances and rollup).
            * */
            PerfMetricId metricId = new PerfMetricId();
            /* Get the ID for this counter. */
            metricId.setCounterId(countersIdMap.get(counterNames[i]));
            metricId.setInstance("*");
            perfMetricIds.add(metricId);
        }

        //getCSV(pCollectorRef,perfMetricIds,performanceMgrRef);
        System.out.println("==================================================");
        getNormal(pCollectorRef,perfMetricIds,performanceMgrRef);



        //--------------
    }

    public void getCSV(ManagedObjectReference pCollectorRef, List<PerfMetricId> perfMetricIds,ManagedObjectReference performanceMgrRef) throws Exception{
        /*
        * Create the query specification for queryPerf().
        * Specify 5 minute rollup interval and CSV output format. */
        int intervalId = 300;
        ManagedObjectReference entityMor = getMOREFs.vmByVMname("v-center",pCollectorRef);
        PerfQuerySpec querySpecification = new PerfQuerySpec(); querySpecification.setEntity(entityMor);
        querySpecification.setIntervalId(intervalId);
        querySpecification.setFormat("csv");
        querySpecification.getMetricId().addAll(perfMetricIds);
        List<PerfQuerySpec> pqsList = new ArrayList<PerfQuerySpec>();
        pqsList.add(querySpecification);
        /*
        * Call queryPerf() *
        * QueryPerf() returns the statistics specified by the provided
        * PerfQuerySpec objects. When specified statistics are unavailable -
        * for example, when the counter doesn't exist on the target
        * ManagedEntity - QueryPerf() returns null for that counter. */
        List<PerfEntityMetricBase> retrievedStats = vimPort.queryPerf(performanceMgrRef, pqsList);

        //System.out.println(retrievedStats);


        /*
        * Cycle through the PerfEntityMetricBase objects. Each object contains * a set of statistics for a single ManagedEntity.
        */
        for (PerfEntityMetricBase singleEntityPerfStats : retrievedStats) {
            /*
            * Cast the base type (PerfEntityMetricBase) to the csv-specific sub-class.
            */
            PerfEntityMetricCSV entityStatsCsv = (PerfEntityMetricCSV) singleEntityPerfStats; /* Retrieve the list of sampled values. */
            List<PerfMetricSeriesCSV> metricsValues = entityStatsCsv.getValue();
            if (metricsValues.isEmpty()) {
                System.out.println("No stats retrieved. " +
                        "Check whether the virtual machine is powered on.");
                throw new Exception();
            }

            /*
            * Retrieve time interval information (PerfEntityMetricCSV.sampleInfoCSV). */
            String csvTimeInfoAboutStats = entityStatsCsv.getSampleInfoCSV();
            /* Print the time and interval information. */
            System.out.println("Collection: interval (seconds),time (yyyy-mm-ddThh:mm:ssZ)");
            System.out.println(csvTimeInfoAboutStats);
            /*
            * Cycle through the PerfMetricSeriesCSV objects. Each object contains * statistics for a single counter on the ManagedEntity.
            */
            for (PerfMetricSeriesCSV csv : metricsValues) {
                /*
                * Use the counterId to obtain the associated PerfCounterInfo object */
                PerfCounterInfo pci = countersInfoMap.get(csv.getId().getCounterId()); /* Print out the metadata for the counter. */
                System.out.println("----------------------------------------");
                System.out.println(pci.getGroupInfo().getKey() + "."
                        + pci.getNameInfo().getKey() + "." + pci.getRollupType() + " - "
                        + pci.getUnitInfo().getKey());
                System.out.println("Instance: " + csv.getId().getInstance());
                System.out.println("Values: " + csv.getValue());

            }
        }
    }
    public void getNormal(ManagedObjectReference pCollectorRef, List<PerfMetricId> perfMetricIds,ManagedObjectReference performanceMgrRef) throws Exception{
        /*
        * Create the query specification for queryPerf().
        * Specify 5 minute rollup interval and CSV output format. */
        int intervalId = 1800;
        ManagedObjectReference entityMor = getMOREFs.vmByVMname("3#l1OhSkf4Du",pCollectorRef);
        int mem = (Integer) getMOREFs.entityProps(entityMor, new String[]{"summary.config.memorySizeMB"}).get("summary.config.memorySizeMB");

        PerfQuerySpec querySpecification = new PerfQuerySpec(); querySpecification.setEntity(entityMor);
        querySpecification.setIntervalId(intervalId);
        querySpecification.setFormat("normal");
        querySpecification.getMetricId().addAll(perfMetricIds);
        List<PerfQuerySpec> pqsList = new ArrayList<PerfQuerySpec>();
        pqsList.add(querySpecification);
        /*
        * Call queryPerf() *
        * QueryPerf() returns the statistics specified by the provided
        * PerfQuerySpec objects. When specified statistics are unavailable -
        * for example, when the counter doesn't exist on the target
        * ManagedEntity - QueryPerf() returns null for that counter. */
        List<PerfEntityMetricBase> retrievedStats = vimPort.queryPerf(performanceMgrRef, pqsList);

        //System.out.println(retrievedStats);


        /*
        * Cycle through the PerfEntityMetricBase objects. Each object contains * a set of statistics for a single ManagedEntity.
        */
        for (PerfEntityMetricBase singleEntityPerfStats : retrievedStats) {
            /*
            * Cast the base type (PerfEntityMetricBase) to the csv-specific sub-class.
            */
            PerfEntityMetric entityMetric = (PerfEntityMetric) singleEntityPerfStats; /* Retrieve the list of sampled values. */
            List<PerfMetricSeries> metricsValues = entityMetric.getValue();
            if (metricsValues.isEmpty()) {
                System.out.println("No stats retrieved. " +
                        "Check whether the virtual machine is powered on.");
                throw new Exception();
            }



            List<PerfSampleInfo> sampleInfos = entityMetric.getSampleInfo();
//            for (PerfSampleInfo info : sampleInfos){
//                System.out.println(info.getTimestamp().toString()+": " + info.getInterval());
//            }
            if (sampleInfos == null || sampleInfos.size() == 0) {
                System.out.println("No Samples available. Continuing.");
                continue;
            }
            List<PerfMetricSeries> series = entityMetric.getValue();
            for (PerfMetricSeries info : series){
                PerfCounterInfo pci = countersInfoMap.get(info.getId().getCounterId());
                System.out.println(pci.getGroupInfo().getKey() + "[      ]" +pci.getNameInfo().getSummary());
                if (info instanceof PerfMetricIntSeries){
                    PerfMetricIntSeries val = (PerfMetricIntSeries) info;
                    List<Long> listlongs = val.getValue();
                    for (int j = 0; j < listlongs.size(); j++) {
                        //long mems = listlongs.get(j) * 10;
                        System.out.println("timestamp: "
                                + sampleInfos.get(j).getTimestamp().toString()
                                + "\tvalue: " + listlongs.get(j));
                    }
                }
            }

     /*       *//* Print the time and interval information. *//*
            System.out.println("Collection: interval (seconds),time (yyyy-mm-ddThh:mm:ssZ)");
            System.out.println(csvTimeInfoAboutStats);
            *//*
            * Cycle through the PerfMetricSeriesCSV objects. Each object contains * statistics for a single counter on the ManagedEntity.
            *//*
            for (PerfMetricSeriesCSV csv : metricsValues) {
                *//*
                * Use the counterId to obtain the associated PerfCounterInfo object *//*
                PerfCounterInfo pci = countersInfoMap.get(csv.getId().getCounterId()); *//* Print out the metadata for the counter. *//*
                System.out.println("----------------------------------------");
                System.out.println(pci.getGroupInfo().getKey() + "."
                        + pci.getNameInfo().getKey() + "." + pci.getRollupType() + " - "
                        + pci.getUnitInfo().getKey());
                System.out.println("Instance: " + csv.getId().getInstance());
                System.out.println("Values: " + csv.getValue());

            }*/
        }
    }


    //
}
