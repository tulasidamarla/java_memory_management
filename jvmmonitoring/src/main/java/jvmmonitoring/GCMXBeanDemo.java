package jvmmonitoring;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;

public class GCMXBeanDemo {

	public static void main(String[] args) {
		List<GarbageCollectorMXBean> list=ManagementFactory.getGarbageCollectorMXBeans();
		for(GarbageCollectorMXBean mxBean:list){
			System.out.println("Name: " + mxBean.getName());
			System.out.println("No of collections: " + mxBean.getCollectionCount());
			System.out.println("Collection Time: " + mxBean.getCollectionTime());
			
			for(String name:mxBean.getMemoryPoolNames()){
				System.out.println("\t" + name);
			}
			
			System.out.println();
		}
		
		

	}

}
