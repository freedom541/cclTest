/*
 * ******************************************************
 * Copyright VMware, Inc. 2010-2012.  All Rights Reserved.
 * ******************************************************
 *
 * DISCLAIMER. THIS PROGRAM IS PROVIDED TO YOU "AS IS" WITHOUT
 * WARRANTIES OR CONDITIONS # OF ANY KIND, WHETHER ORAL OR WRITTEN,
 * EXPRESS OR IMPLIED. THE AUTHOR SPECIFICALLY # DISCLAIMS ANY IMPLIED
 * WARRANTIES OR CONDITIONS OF MERCHANTABILITY, SATISFACTORY # QUALITY,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE.
 */

package com.vmware.performance;

import com.vmware.common.annotations.Action;
import com.vmware.common.annotations.Option;
import com.vmware.common.annotations.Sample;
import com.vmware.connection.ConnectedVimServiceBase;
import com.vmware.vim25.*;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

/**
 * <pre>
 * History
 * 
 * This sample reads performance measurements from the current time.
 * 
 * <b>Parameters:</b>
 * url            [required]: url of the web service.
 * username       [required]: username for the authentication
 * Password       [required]: password for the authentication
 * hostname       [required]: name of the host
 * interval       [required]: sampling interval in seconds.
 * starttime:     [optional]: In minutes, to specify what's start time from which samples needs to be collected
 * duration       [optional]: Duration for which samples needs to be  taken
 * groupname      [required]: cpu, mem
 * countername    [required]: usage (for cpu and mem), overhead (for mem)
 * 
 * <b>Command Line:</b>
 * Display performance measurements of extra CPU usage
 * run.bat com.vmware.performance.History --url [webserviceurl]
 * --username [username]  --password [password] --hostname [name of the
 * history server]  --groupname cpu --countername usage --interval 300
 * 
 * Display performance measurements from the past
 * should be displayed as per the counter and the group specified.
 * run.bat com.vmware.performance.History --url [webserviceurl]
 * --username [username]  --password [password]--hostname [name of the
 * history server]  --groupname mem --countername overhead --interval 300
 * </pre>
 */
@Sample(name = "history", description = "reads performance measurements from the current time")
public class History extends ConnectedVimServiceBase {
	private ManagedObjectReference rootFolder;
	private ManagedObjectReference perfManager;
	private ManagedObjectReference propCollector;

	Map<String, Map<String, ArrayList<PerfCounterInfo>>> pci = new HashMap<String, Map<String, ArrayList<PerfCounterInfo>>>();

	String hostname = null;
	String interval = null;
	int starttime = 0;
	int duration = 0;
	String groupname = null;
	String countername = null;

	@Option(name = "hostname", description = "name of the host")
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	@Option(name = "interval", description = "sampling interval in seconds.[Past Day=300,Past Week=1800,Past Month=7200,Past Year=86400]")
	public void setInterval(String interval) {
		this.interval = interval;
	}

	@Option(name = "starttime", required = false, description = "In minutes, to specify what's start time from which samples needs to be collected")
	public void setStarttime(int starttime) {
		this.starttime = starttime;
	}

	@Option(name = "duration", required = false, description = "Duration for which samples needs to be  taken")
	public void setDuration(int duration) {
		this.duration = duration;
	}

	@Option(name = "groupname", description = "[cpu|mem]")
	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	@Option(name = "countername", description = "usage (for cpu and mem), overhead (for mem)")
	public void setCountername(String countername) {
		this.countername = countername;
	}

	/**
	 * Uses the new RetrievePropertiesEx method to emulate the now deprecated
	 * RetrieveProperties method.
	 * 
	 * @param listpfs
	 * @return list of object content
	 * @throws Exception
	 */
	List<ObjectContent> retrievePropertiesAllObjects(
			List<PropertyFilterSpec> listpfs) throws Exception {

		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();

		List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

		RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollector,
				listpfs, propObjectRetrieveOpts);
		if (rslts != null && rslts.getObjects() != null
				&& !rslts.getObjects().isEmpty()) {
			listobjcontent.addAll(rslts.getObjects());
		}
		String token = null;
		if (rslts != null && rslts.getToken() != null) {
			token = rslts.getToken();
		}
		while (token != null && !token.isEmpty()) {
			rslts = vimPort.continueRetrievePropertiesEx(propCollector, token);
			token = null;
			if (rslts != null) {
				token = rslts.getToken();
				if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
					listobjcontent.addAll(rslts.getObjects());
				}
			}
		}
		return listobjcontent;
	}

	void setStartTimeAndDuration(int dura, int sttime) throws Exception {
		if (dura == 0 || sttime == 0) {
			duration = 20;
			starttime = 20;
		} else {
			duration = (dura <= 0) ? 20 : dura;
			starttime = (sttime <= 0) ? 20 : sttime;
		}
		if (duration > starttime) {
			System.out.println("Duration must be less than startime");
		}
	}

	/**
	 * This method initializes all the performance counters available on the
	 * system it is connected to. The performance counters are stored in the
	 * hashmap counters with group.counter.rolluptype being the key and id being
	 * the value.
	 */
	List<PerfInterval> getPerfInterval() throws Exception {

		List<PerfInterval> pciArr = null;
		// Create Property Spec
		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.getPathSet().add("historicalInterval");
		propertySpec.setType("PerformanceManager");
		List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
		propertySpecs.add(propertySpec);

		// Now create Object Spec
		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(perfManager);

		List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
		objectSpecs.add(objectSpec);

		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
		// created above.
		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);

		List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
		propertyFilterSpecs.add(propertyFilterSpec);

		List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
		listpfs.add(propertyFilterSpec);
		List<ObjectContent> listocont = retrievePropertiesAllObjects(listpfs);

		if (listocont != null) {
			for (ObjectContent oc : listocont) {
				List<DynamicProperty> dps = oc.getPropSet();
				if (dps != null) {
					for (DynamicProperty dp : dps) {
						List<PerfInterval> perintlist = ((ArrayOfPerfInterval) dp
								.getVal()).getPerfInterval();
						pciArr = perintlist;
					}
				}
			}
		}
		return pciArr;
	}

	/**
	 * @return TraversalSpec specification to get to the HostSystem managed
	 *         object.
	 */
	TraversalSpec getHostSystemTraversalSpec() {
		// Create a traversal spec that starts from the 'root' objects
		// and traverses the inventory tree to get to the Host system.
		// Build the traversal specs bottoms up
		SelectionSpec ss = new SelectionSpec();
		ss.setName("VisitFolders");

		// Traversal to get to the host from ComputeResource
		TraversalSpec computeResourceToHostSystem = new TraversalSpec();
		computeResourceToHostSystem.setName("computeResourceToHostSystem");
		computeResourceToHostSystem.setType("ComputeResource");
		computeResourceToHostSystem.setPath("host");
		computeResourceToHostSystem.setSkip(false);
		computeResourceToHostSystem.getSelectSet().add(ss);

		// Traversal to get to the ComputeResource from hostFolder
		TraversalSpec hostFolderToComputeResource = new TraversalSpec();
		hostFolderToComputeResource.setName("hostFolderToComputeResource");
		hostFolderToComputeResource.setType("Folder");
		hostFolderToComputeResource.setPath("childEntity");
		hostFolderToComputeResource.setSkip(false);
		hostFolderToComputeResource.getSelectSet().add(ss);

		// Traversal to get to the hostFolder from DataCenter
		TraversalSpec dataCenterToHostFolder = new TraversalSpec();
		dataCenterToHostFolder.setName("DataCenterToHostFolder");
		dataCenterToHostFolder.setType("Datacenter");
		dataCenterToHostFolder.setPath("hostFolder");
		dataCenterToHostFolder.setSkip(false);
		dataCenterToHostFolder.getSelectSet().add(ss);

		// TraversalSpec to get to the DataCenter from rootFolder
		TraversalSpec traversalSpec = new TraversalSpec();
		traversalSpec.setName("VisitFolders");
		traversalSpec.setType("Folder");
		traversalSpec.setPath("childEntity");
		traversalSpec.setSkip(false);

		List<SelectionSpec> sSpecArr = new ArrayList<SelectionSpec>();
		sSpecArr.add(ss);
		sSpecArr.add(dataCenterToHostFolder);
		sSpecArr.add(hostFolderToComputeResource);
		sSpecArr.add(computeResourceToHostSystem);
		traversalSpec.getSelectSet().addAll(sSpecArr);
		return traversalSpec;
	}

	/**
	 * Retrieves the MOREF of the host.
	 * 
	 * @param hostName
	 *            :
	 * @return
	 */
	ManagedObjectReference getHostByHostName(String hostName) throws Exception {
		ManagedObjectReference retVal = null;
		TraversalSpec tSpec = getHostSystemTraversalSpec();
		// Create Property Spec
		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.getPathSet().add("name");
		propertySpec.setType("HostSystem");

		// Now create Object Spec
		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(rootFolder);
		objectSpec.setSkip(Boolean.TRUE);
		objectSpec.getSelectSet().add(tSpec);

		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
		// created above.
		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);
		List<PropertyFilterSpec> listPfs = new ArrayList<PropertyFilterSpec>(1);
		listPfs.add(propertyFilterSpec);
		List<ObjectContent> oContList = retrievePropertiesAllObjects(listPfs);

		if (oContList != null) {
			for (ObjectContent oc : oContList) {
				ManagedObjectReference mr = oc.getObj();
				String hostnm = null;
				List<DynamicProperty> listDynamicProps = oc.getPropSet();
				DynamicProperty[] dps = listDynamicProps
						.toArray(new DynamicProperty[listDynamicProps.size()]);
				if (dps != null) {
					for (DynamicProperty dp : dps) {
						hostnm = (String) dp.getVal();
					}
				}
				if (hostnm != null && hostnm.equals(hostName)) {
					retVal = mr;
					break;
				}
			}
		} else {
			System.out.println("The Object Content is Null");
		}
		return retVal;
	}

	void displayHistory() throws Exception {
		ManagedObjectReference hostmor = getHostByHostName(hostname);
		if (hostmor == null) {
			System.out.println("Host " + hostname + " not found");
			return;
		}
		counterInfo(perfManager);
		List<PerfInterval> intervals = getPerfInterval();

		// Integer interval = new Integer(Integer.parseInt(interval));
		boolean valid = checkInterval(intervals, Integer.valueOf(interval));
		if (!valid) {
			System.out.println("Invalid interval, Specify one from above");
			return;
		}

		PerfCounterInfo pci = getCounterInfo(groupname, countername, PerfSummaryType.AVERAGE, null);
		if (pci == null) {
			System.out.println("Incorrect Group Name and Countername specified");
			return;
		}

		PerfQuerySpec qSpec = new PerfQuerySpec();
		qSpec.setEntity(hostmor);
		qSpec.setMaxSample(new Integer(10));
		List<PerfQuerySpec> qSpecs = new ArrayList<PerfQuerySpec>();
		qSpecs.add(qSpec);
		XMLGregorianCalendar serverstarttime = vimPort.currentTime(getServiceInstanceReference());
		XMLGregorianCalendar serverendtime = vimPort.currentTime(getServiceInstanceReference());

		int minsToaddEnd = duration - (2 * starttime);
		int minsToaddStart = duration - ((2 * starttime) + duration);

		int setTime;
		if (minsToaddStart < 0) {
			setTime = serverstarttime.getMinute() + (60 + minsToaddStart);
			if (setTime >= 60) {
				setTime = setTime - 60;
				serverstarttime.setMinute(setTime);
			} else {
				serverstarttime.setHour(serverstarttime.getHour() - 1);
				serverstarttime.setMinute(setTime);
			}
		} else {
			serverstarttime.setMinute(serverstarttime.getMinute()
					+ (duration - ((2 * starttime) + duration)));
		}
		if (minsToaddEnd < 0) {
			setTime = serverendtime.getMinute() + (60 + minsToaddEnd);
			if (setTime >= 60) {
				setTime = setTime - 60;
				serverendtime.setMinute(setTime);
			} else {
				serverendtime.setHour(serverendtime.getHour() - 1);
				serverendtime.setMinute(setTime);
			}
		} else {
			serverendtime.setMinute(serverendtime.getMinute()
					+ (duration - (2 * starttime)));
		}
		serverstarttime.setTime(0,0,0);
		serverendtime.setTime(23,59,59);

		System.out.println("Start Time "
				+ serverstarttime.toGregorianCalendar().getTime().toString());
		System.out.println("End Time   "
				+ serverendtime.toGregorianCalendar().getTime().toString());

		System.out.println();


		List<PerfMetricId> listprfmetid = vimPort.queryAvailablePerfMetric(
				perfManager, hostmor, serverstarttime, serverendtime,
				Integer.valueOf(interval));

		PerfMetricId ourCounter = null;

		for (int index = 0; index < listprfmetid.size(); ++index) {
			if (listprfmetid.get(index).getCounterId() == pci.getKey()) {
				ourCounter = listprfmetid.get(index);
				break;
			}
		}
		if (ourCounter == null) {
			System.out.println("No data on Host to collect. "
					+ "Has it been running for at least " + duration
					+ " minutes");
		} else {
			qSpec = new PerfQuerySpec();
			qSpec.setEntity(hostmor);
			qSpec.setStartTime(serverstarttime);
			qSpec.setEndTime(serverendtime);
			qSpec.getMetricId().addAll(
					Arrays.asList(new PerfMetricId[] { ourCounter }));
			qSpec.setIntervalId(Integer.valueOf(interval));
			qSpecs.add(qSpec);

			List<PerfQuerySpec> alpqs = new ArrayList<PerfQuerySpec>(1);
			alpqs.add(qSpec);
			List<PerfEntityMetricBase> listpemb = vimPort.queryPerf(perfManager, alpqs);

			if (listpemb != null) {
				displayValues(listpemb, pci, ourCounter, Integer.valueOf(interval));
			} else {
				System.out.println("No Samples Found");
			}
		}
	}

	boolean checkInterval(List<PerfInterval> intervals, Integer interv)
			throws Exception {
		boolean flag = false;
		for (int i = 0; i < intervals.size(); ++i) {
			PerfInterval pi = intervals.get(i);
			if (pi.getSamplingPeriod() == interv) {
				flag = true;
				break;
			}
		}
		if (!flag) {
			System.out.println("Available summary collection intervals");
			System.out.println("Period\tLength\tName");
			for (int i = 0; i < intervals.size(); ++i) {
				PerfInterval pi = intervals.get(i);
				System.out.println(pi.getSamplingPeriod() + "\t"
						+ pi.getLength() + "\t" + pi.getName());
			}
			System.out.println();
		}
		return flag;
	}

	/**
	 * This method initializes all the performance counters available on the
	 * system it is connected to. The performance counters are stored in the
	 * hashmap counters with group.counter.rolluptype being the key and id being
	 * the value.
	 */
	List<PerfCounterInfo> getPerfCounters() throws Exception {

		List<PerfCounterInfo> pciArr = null;
		// Create Property Spec
		PropertySpec propertySpec = new PropertySpec();
		propertySpec.setAll(Boolean.FALSE);
		propertySpec.getPathSet().add("perfCounter");
		propertySpec.setType("PerformanceManager");
		List<PropertySpec> propertySpecs = new ArrayList<PropertySpec>();
		propertySpecs.add(propertySpec);

		// Now create Object Spec
		ObjectSpec objectSpec = new ObjectSpec();
		objectSpec.setObj(perfManager);

		List<ObjectSpec> objectSpecs = new ArrayList<ObjectSpec>();
		objectSpecs.add(objectSpec);

		// Create PropertyFilterSpec using the PropertySpec and ObjectPec
		// created above.
		PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
		propertyFilterSpec.getPropSet().add(propertySpec);
		propertyFilterSpec.getObjectSet().add(objectSpec);

		List<PropertyFilterSpec> propertyFilterSpecs = new ArrayList<PropertyFilterSpec>();
		propertyFilterSpecs.add(propertyFilterSpec);

		List<PropertyFilterSpec> listpfs = new ArrayList<PropertyFilterSpec>(1);
		listpfs.add(propertyFilterSpec);
		List<ObjectContent> listocont = retrievePropertiesAllObjects(listpfs);

		if (listocont != null) {
			for (ObjectContent oc : listocont) {
				List<DynamicProperty> dps = oc.getPropSet();
				if (dps != null) {
					for (DynamicProperty dp : dps) {
						List<PerfCounterInfo> pcinfolist = ((ArrayOfPerfCounterInfo) dp
								.getVal()).getPerfCounterInfo();
						pciArr = pcinfolist;
					}
				}
			}
		}
		return pciArr;
	}

	void counterInfo(ManagedObjectReference pmRef) throws Exception {
		List<PerfCounterInfo> cInfos = getPerfCounters();
		for (int i = 0; i < cInfos.size(); ++i) {
			PerfCounterInfo cInfo = cInfos.get(i);
			String group = cInfo.getGroupInfo().getKey();
			Map<String, ArrayList<PerfCounterInfo>> nameMap = null;
			if (!pci.containsKey(group)) {
				nameMap = new HashMap<String, ArrayList<PerfCounterInfo>>();
				pci.put(group, nameMap);
			} else {
				nameMap = pci.get(group);
			}
			String name = cInfo.getNameInfo().getKey();
			ArrayList<PerfCounterInfo> counters = null;
			if (!nameMap.containsKey(name)) {
				counters = new ArrayList<PerfCounterInfo>();
				nameMap.put(name, counters);
			} else {
				counters = nameMap.get(name);
			}
			counters.add(cInfo);
		}

	}

	ArrayList<PerfCounterInfo> getCounterInfos(String groupName,
			String counterName) {
		Map<String, ArrayList<PerfCounterInfo>> nameMap = pci.get(groupName);
		if (nameMap != null) {
			ArrayList<PerfCounterInfo> ret = nameMap.get(counterName);
			if (ret != null) {
				return ret;
			}
		}
		return null;
	}

	PerfCounterInfo getCounterInfo(String groupName, String counterName,
			PerfSummaryType rollupType, PerfStatsType statsType) {
		ArrayList<PerfCounterInfo> counters = getCounterInfos(groupName,counterName);
		if (counters != null) {
			for (Iterator<?> i = counters.iterator(); i.hasNext();) {
				PerfCounterInfo pci = (PerfCounterInfo) i.next();
				if ((statsType == null || statsType.equals(pci.getStatsType()))
						&& (rollupType == null || rollupType.equals(pci.getRollupType()))) {
					return pci;
				}
			}
		}
		return null;
	}

	void displayValues(List<PerfEntityMetricBase> values, PerfCounterInfo pci,
			PerfMetricId pmid, Integer inter) {
		for (int i = 0; i < values.size(); ++i) {
			List<PerfMetricSeries> listperfmetser = ((PerfEntityMetric) values
					.get(i)).getValue();
			List<PerfSampleInfo> listperfsinfo = ((PerfEntityMetric) values
					.get(i)).getSampleInfo();
			if (listperfsinfo == null || listperfsinfo.size() == 0) {
				System.out.println("No Samples available. Continuing.");
				continue;
			}
			System.out.println("Sample time range: "
					+ listperfsinfo.get(0).getTimestamp().toGregorianCalendar()
							.getTime().toString()
					+ " - "
					+ (listperfsinfo.get(listperfsinfo.size() - 1))
							.getTimestamp().toGregorianCalendar().getTime()
							.toString() + ", read every " + inter + " seconds");
			for (int vi = 0; vi < listperfmetser.size(); ++vi) {
				if (pci != null) {
					if (pci.getKey() != listperfmetser.get(vi).getId()
							.getCounterId()) {
						continue;
					}
					System.out.println(pci.getNameInfo().getSummary()
							+ " - Instance: " + pmid.getInstance());
				}
				if (listperfmetser.get(vi) instanceof PerfMetricIntSeries) {
					PerfMetricIntSeries val = (PerfMetricIntSeries) listperfmetser
							.get(vi);
					List<Long> listlongs = val.getValue();
					for (int j = 0; j < listlongs.size(); j++) {
						System.out.println("timestamp: "
								+ listperfsinfo.get(j).getTimestamp().toString()
								+ "\t\t\tvalue: " + listlongs.get(j));
					}
				}
			}
		}
	}

	@Action
	public void run() throws Exception {
		rootFolder = serviceContent.getRootFolder();
		perfManager = serviceContent.getPerfManager();
		propCollector = serviceContent.getPropertyCollector();
		setStartTimeAndDuration(duration, starttime);
		displayHistory();
	}
}
