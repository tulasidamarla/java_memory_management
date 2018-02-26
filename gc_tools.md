Monitoring GC
-------------
MX Beans
Jstat
VisualVM(Visual GC plugin)

MX Beans
--------
Java provides MX Bean to monitor GC. It provides information like Name of collectors,Number of collections,Time of 
collections and also manages some information about the memory pools those collectors manage.

JVM provides two MX Beans one for young and another for old GC. Here is the program which prints all this information.
		
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

O/p from my machine:
--------------------
Name: PS Scavenge
No of collections: 0
Collection Time: 0
	PS Eden Space
	PS Survivor Space

Name: PS MarkSweep
No of collections: 0
Collection Time: 0
	PS Eden Space
	PS Survivor Space
	PS Old Gen

Note: From the o/p it is clear that both young and old generations have Eden and Survivor spaces.	
Note: You can test the program by changing VM arguments. I ran the program again with vm argument "-XX:+UseConcMarkSweepGC".
Here is the o/p:

Name: ParNew  
No of collections: 0  
Collection Time: 0  
	Par Eden Space  
	Par Survivor Space  

Name: ConcurrentMarkSweep  
No of collections: 0  
Collection Time: 0  
	Par Eden Space  
	Par Survivor Space  
	CMS Old Gen  

Jstat
-----
Jstat is the command line utility provided by oracle.  Jstat command syntax is

	jstat -option <pid> <interval> <count>

jstat is the command name.  
option is one of many arguments like gcutil,gc etc.  
pid is the process id of the java program.  
interval is the time interval for jstat to produce o/p.  
count is the no of times the o/p is produced. If no value is given then o/p prints indefinitely.

Note:Jstat works with remote vm's also.

Here is the sample program which allocates an object into a random location in an array. After some time same memory
locations will be allocated with new objects and replaced objects are eligible for GC. 

		int size = 1000000;
		GCMe[] gcmes=new GCMe[size];
		
		int count= 0;
		Random random=new Random();
		while(true){
			gcmes[random.nextInt(size)] = new GCMe();
			if(count%1000000 == 0){
				System.out.print(".");
			}
			count++;
		}

Note: GCMe is a class like the one in GC demo program section above. 

Lets run the above program and from another command prompt we can run jstat commands to monitor its behavior.

To get the processid of the above program use "jps" command. 

gcutil command
--------------
once you get pid run the command  
	
	jstat -gcutil <pid> 

In my machine it produced the following output.

  S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT  
 99.69   0.00  40.00  35.31  55.61  58.45    686  152.947    52   19.154  172.101
 
Note: The above o/p shows the percentages of spaces that were full at the time of running the command.   
s0 --> survivor space 0  
s1 --> survivor space 1  
E --> Eden space  
O --> old space  
M --> meta space  

YGC --> no of young GC that were run so far.  
YGCT --> average time for the young GC to run.  
FGC --> no of full GC that were run so far.  
FGCT --> average time for the full GC to run.  
GCT --> Total average time taken for both young and old GC to run.

gccause command
---------------
jstat -gccause <pid>

In my machine it produced the following o/p:

  S0     S1     E      O      M     CCS    YGC     YGCT    FGC    FGCT     GCT    LGCC                 GCC  
  0.00  99.73   0.00  98.68  55.61  58.45   2329  498.687   108   38.777  537.464 Allocation Failure   Ergonomics
  
Note: From the above it is understood that LGCC is because of allocation failure and GCC is for Ergonomics(for efficiency reasons).

gccapacity
----------

	jstat -gccapacity <pid> // It gives memory allocated for various regions. These are divided into minimum,maximum and current.

Here is the o/p from my machine.

 NGCMN    NGCMX     NGC     S0C   S1C       EC      OGCMN      OGCMX       OGC         OC       MCMN     MCMX      MC     CCSMN    CCSMX     CCSC    YGC    FGC  
 41984.0 673792.0 673792.0 166400.0 166400.0 340992.0    84992.0  1347584.0  1347584.0  1347584.0      0.0 1056768.0   4864.0      0.0 1048576.0    512.0   5273   187 

NGC meant for new gc  
OGC meant for old gc

Note: NGC memory should be equal to S0C+S1C+EC.

gc command
----------
	
	jstat -gc <pid>

Here is the o/p:

 S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT  
166400.0 166400.0  0.0   165824.0 340992.0 184137.0 1347584.0   861861.0  4864.0 2704.9 512.0  299.3    6199 1341.903  212    78.137 1420.039 

C --> capacity  
U --> Utilization

Let's run the command at an interval of 1 sec for 10 times.
	
	jstat -gc <pid> 1000 10

In the o/p you can notice the no of young and old GC counts will keep increasing.

Lets change the GC type to G1 GC in VM and findout the difference with the default.  
VM argument for G1 GC is "-XX:+UseG1GC". Here is the sample o/p:

S0C    S1C    S0U    S1U      EC       EU        OC         OU       MC     MU    CCSC   CCSU   YGC     YGCT    FGC    FGCT     GCT  
0.0   96256.0  0.0   96256.0 607232.0 291840.0 1317888.0   956226.3  4864.0 2676.3 512.0  299.3      96   10.535   0      0.000   10.535

Points to Consider
------------------
1)From the o/p of the program it is clear that memory allocation is much slower, because it will not use sequential memory
allocation(bump the pointer allocation) algorithm. We know different GC's work in different ways. Some GC's allocate memory
much faster and some GC's collect objects much faster. It depends on the requirement which one to choose.  
2)Memory for survivor and eden spaces is less compared with the default GC settings. Also, memory allocated for old generation is more.  
3)YGC run more no of times over OGC when compared with default jvm settings. This has much higher throughput with YGC
and much slower allocation because it will not use bum the pointer allocation as mentioned above.

Visual VM
---------
It is installed as part of standard jdk installation. To run this, go command prompt and run "jvisualvm". 
jvisualvm has a tab named monitor using which we can monitor cpu,memory,classes,threads etc. 
But, this won't give enough information about GC. To monitor GC we need to install a plugin named "visualgc".
