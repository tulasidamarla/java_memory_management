# jvm_memory_management

We need to understand some basics about Java's Garbage collection(GC) before memory management.so, why GC?<br>
Ans:<br>
1)once object is created, no need to remember to delete. whenever an object has no live references to it GC will clear the memory allocated for it. If we don't do this in other languages like c,c++ etc, it will cause memory leak.<br>
2)use and forget, no need to ask should I delete. For ex, an object is returned from a method like this: Account acc=getAccount(). Who should clear the memory allocated for it. Is it the caller or the one who created it. It may end up in a situation where both ignore to free the object, which may cause issues like null pointer exception etc and it is very hard to debug. In java, you don't need to worry about it.<br>
3)Use with confidence. As long as there is a life refernce to an object, there is no way that garbage collector can free up that memory.In other languages like c or c++, a thread in the background may free up the memory allocated for an object, which may cause null pointer issues at runtime.<br>

The GC promise<br>
--------------<br>
1)GC never claims any live objects.<br>
2)GC promises nothing about dead objects. i.e. when it will free up the memory of a dead object is not guaranteed.

Types of Garbage Collection<br>
---------------------------<br>
1)Do Nothing: Never to run, never to do anything, no memory gets released, but still has guarantee of not collecting any live references. [Program should call System.gc() may be].<br>
2)Reference Counting: It tracks, for each object, a count of number of references to it held by other objects. If an object's reference count reaches zero, the object has become inaccessible, and can be destroyed.<br>
3)Mark and Sweep: It works in two phases called Mark and Sweep. In Mark phase it marks all live references as still being alive. Sweep phase removes all un-used memory. It leaves the memory fragmented.<br>
4)Copying:It works hand-in-hand with Mark and Sweep. After Sweep, all the memory that is left is copied from one buffer to other. Once it is copied, it re-arranges memory so that it is no longer fragmented.<br>
5)Generational: Once an object survives a garbage collection, It is likely to be an object that lives longer. In this case, GC may not look at it again for a while. This improves performance.<br>
6)Incremental: Incremental GC is a GC that does not look at all the memory all the times. Infact, Generational GC is one form of Incremental GC.<br>

Reference Counting GC<br>
---------------------<br>
Reference Counting uses AddRef and Release call for objects. when count hits 0 object is freed.<br>
Reference Counted GC has problems with circular references.i.e. If two objects referencing each other then both will have reference count of 1. Eventhough, there are no external references to these, they are not claimed by GC.

Mark and Sweep GC<br>
-----------------<br>
Mark and Sweep contains three phases.<br>
1)Mark phase that identifies live objects.<br>
2)Sweep phase to remove unused objects<br>
3)compact phase to compact the memory.

Mark Phase<br>
----------<br>
Mark phase uses something called as root set to identify all live objects. This root set contains objects(or stack references) from which all objects in memory are created. So, Mark phase identifies all live objects if it is referenced from root set.

Note:Cyclic references are not a problem here, because those are not referenced from root set and hence eligible for GC.

Sweep Phase<br>
-----------<br>
In sweep phase garbage is taken away and that leaves all objects that are referenced still in memory.

Compact phase<br>
-------------<br>
After Sweep phase the memory is fragmented. Compact phase compacts memory. This changes all the physical address of objects. In java objects are accessed through references not with physical address, so this is not a problem.

Copying GC<br>
----------<br>
Copying GC also uses Mark and Sweep phases but it uses different spaces to manage memory instead of one memory. Copying GC uses spaces called fromspace and tospace. Copying GC runs whenever fromspace is full. 

Mark phase with the help of root set identifies live objects. <br>
Sweep phase moves these live objects into tospace, so compacted at the same time. After this fromspace is cleared. Now fromspace and tospace are now swapped. Now any memory that is allocated will be in this new fromspace and if it is full again Mark and Sweep phases run.

Generational GC<br>
---------------<br>
Generational GC maintains different generations(or spaces) for memory. Long lived objects are promoted from one generation(called younger generation) to a different generation(old generation) for a given definition of long.

The Garbage Collector sweeps through younger generation more ofthen than old generation.

Note: Depending on the environment there can be any no of generation. For ex, Java uses two generations, .net uses 3 generations.

GC demo program
---------------
write a program that allocates memory and prints the addresses of those objects. The idea behind this is, 
as we print these addresses we will see the memory address raises while allocating more and more objects 
and eventually GC will run and the next allocation of addresses will be close to the previous address. 
Here is the sample code. This class uses Unsafe class to print address depends on either 32-bit or 64-bit env.

	import java.lang.reflect.Field;

	import sun.misc.Unsafe;

	public class Sawtooth {
		private static Unsafe unsafe;

		static {
			try {
				Field field = Unsafe.class.getDeclaredField("theUnsafe");
				field.setAccessible(true);
				unsafe = (Unsafe) field.get(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public static long addressOf(Object o) throws Exception {
			Object[] array = new Object[]{o};

			long baseOffset = unsafe.arrayBaseOffset(Object[].class);
			int addressSize = unsafe.addressSize();
			long objectAddress;
			switch (addressSize) {
				case 4:
					objectAddress = unsafe.getInt(array, baseOffset);
					break;
				case 8:
					objectAddress = unsafe.getLong(array, baseOffset);
					break;
				default:
					throw new Error("unsupported address size: " + addressSize);
			}

			return (objectAddress);
		}

	public static void main(String... args) throws Exception {
		for (int i = 0; i < 132000; i++) {
			Object mine = new GCMe();
			long address = addressOf(mine);
			System.out.println(address);
		}
		//Verify address works - should see the characters in the array in the output
		//printBytes(address, 31);

	}

		public static void printBytes(long objectAddress, int num) {
			for (long i = 0; i < num; i++) {
				int cur = unsafe.getByte(objectAddress + i);
				System.out.print((char) cur);
			}
			System.out.println();
		}
	}
	//Create GCMe with more fields so that each object size is more and less no of objects will be enough
	class GCMe {
		long a;
		long aa;
		long aaa;
	}
	
GC in Oracle JVM
----------------
We know there are various kinds of GC's available. While choosing one kind over other needs to 
consider many things like this.<br>
1)Stop the world events: It means GC will force the entire application to stop and collects garbage. 
 we need to minimize these events.<br>
2)Memory fragmentation: When GC runs it may defragment memory all at once, or leaves memory for later stage 
 or leaves the memory fragmented on the basis of whether leaving memory fragmented is cheaper than defragmentation.<br>
3)Throughput: How quickly GC collects garbage and how does it effect the performance of the application.

Note: We know there are various GC's available like Generational, copying,Mark and Sweep etc.<br>
We need to choose carefully one of them, depends on defragmentation needs, throughput etc.

JVM memory spaces
-----------------
In JVM the memory spaces are divided into two spaces (or generations) called 'young generation' and 'old generation'.

young generation
----------------
Young generation is divided into 'Eden space' and 'survivor space'. It contains one 'Eden space' and two 'Survivor spaces'.<br>
-->All new object allocations typically go to 'Eden space'.<br>
-->Objects that survive GC are moved into one of these two survivor spaces.<br>
-->Only one survivor space is used at a time.<br>
-->Objects copied between survivor spaces.<br>
Note: Objects that survive GC will be moved to new survivor space. Any objects that exist in original survivor space 
are also copied to new survivor space.

Old generation
--------------
Once an object survives a no of GC's then they are moved to old generation.

In a nutshell Memory spaces are categorized like this.

Young generation
----------------
Eden space 
S0(survivor space 0)
S1(survivor space 1)

Old generation
--------------
Tenured

Permanent Generation
--------------------
permgen space

Note: permgen space is the space used by Java runtime, for ex class information etc. This will never be garbage collected.

*****
why different spaces?<br>
Ans:The idea behind this is two run GC on younger generation more frequently than old generation, because most objects 
live for a short while. i.e. typically objects die young or live forever. By running GC on old generation less frequently 
and young generation more often will improve the performance of the application.

GC is categorized into two categories called as Minor GC and Major GC.

Minor GC
--------
we know new objects are allocated into Eden space. When GC runs objects that are survived will be copied to empty(or new 
survivor space or to survivor space) survivor space. Objects from older survior space(or from survivor space) are also 
copied to this new survivor space. Next time, when GC runs these spaces are swapped. i.e. new survivor space becomes old 
and old becomes new. So Minor GC runs when Eden space(or young generation) is full.

Major GC
--------
Major GC is triggered when tenured space(or old generation) is full.
 
With Major GC, memory gets copied from young generation to old generation. This is called promotion. 
This promotion happens in two scenarios.

1)If survivor space is full because objects that survive Minor GC from Eden space cannot be moved to survivor space.
2)If objects in survivor space are survived certain no of GC's then they will be promoted to old generation.

Major GC is slower than Minor GC because it runs on large section of the heap. It should run less frequently.

Note: In Oracle jvm Major gc collects both old and young generation.

There is no direct way of allocating objects in old space. However, we can set if an object is larger than <n> bytes, 
should be allocated directly to old space using the Option:
	-XX:PretenureSizeThreshold=<n> , where n is no of bytes

Note: If object fits in TLAB, JVM will allocate memory in TLAB. so, we should also limit TLAB size.

Memory allocation in JVM
-------------------------
The other side of collecting(GC) memory is allocating memory. Ideally allocating Memory should be as quick as possible. 
The best way for this is to simply increment the pointer for each new object that comes in. This is cheap and very fast. Infact, young generation does this.

Incrementing the pointer is fine for single threaded environment, what about multi threaded environment where-in multiple threads compete for the same memory location?
Ans: Locking is an expensive mechanism to use. Java uses something called as Thread Local Allocation Buffer(or TLAB). 
With TLAB, each thread gets its own buffer in Eden space and threads can only allocate into that buffer.

Live roots
----------
A live root is a reference to an object from one of the following
1)stack frame : Stack represents the live running of an application. This is the stack across all threads. 
Any references from variables on stack frame represent objects must be live references.
2)static variables: Any static variables ref to an object and they are kept live.
3)Others like JNI or synchronization monitors: Objects that reference Java native interface or 
if we use synchronization monitors for locking they are all considered as live references.

Note: Any object that is referenced from live root is kept alive during GC. If these objects references to 
any other objects, they are also kept alive.

*****
What about references from old generation to young generation?
Ans: When there is a refernce from old generation to young generation that object should not be garbage collected. 
Young GC has to look at only young generation for performance. In this case, it has to look for reference in old space. 
It defeats the purpose. To fix this issue, we have something called as Cardtable.

Cardtable
---------
When a write to a reference to an object in young generation happens these will go through a write barrier. 
This write barrier updates the Cardtable entry. Each entry in cardtable owns 512 bytes of memory. 
i.e. If any change happens in the 512 bytes of memory, then cardtable is updated.

Minor GC stpes
--------------
1)scan for unused objects
2)scan for references from rootset
3)Instead of scanning old generation for references old to young, cardtable is scanned, looks for any changed data 
and that piece of memory is loaded and any references in that memory are followed and marked as in use.

Java GC's
---------
Java has different GC's. We can choose one of these by passing jvm arguments like below.
-XX:+UseSerialGC --> Serial generational collector.
-XX:+UseParallelGC --> Parallel for young space, serial for old space generational collector.
-XX:+UseParallelOldGC --> parallel for both young and old spaces.
-XX:+UseConcMarkSweepGC(-XX:-UseParNewGC) --> Concurrent Mark sweep with serial young space collector.
-XX:+UseConcMarkSweepGC(+XX:-UseParNewGC) --> Concurrent Mark sweep with parallel young space collector.
-XX:+UseG1GC --> G1 garbage collector.

Serial GC
---------
Serial means it is single threaded. It is also called "stop the world GC" because it stops everything and runs GC. 
It does mark and sweep. It does copying also. That means it uses Eden space, survior spaces and old space. It is suitable for small client applications, not suitable for big enterprise server applications.

Parallel GC
-----------
It uses multiple threads for minor collection and single thread(stop the world) for major collection. It uses same spaces
as in serial GC. This GC is suitable for use in servers.

Parallel Old GC
---------------
It is similar to parallel GC but it uses multiple threads for both minor and major collections.
It is preferred over Parallel GC.

Concurrent Mark and Sweep GC
----------------------------
This will run only during major GC. That means it only collects old space(That's why we need to pass two arguments for 
both young and old GC types). It will not use the sequential pointer memory allocation. It fragments the heap and it 
manages sets of free lists for each of these fragments. It allocates memory into these fragments. That means something 
has to be there to look at these and update the free lists. In some circumstances this will cause the collector to run
very slowly. Because this a low latency collector, through put should be much higher than Parallel GC or parallel Old GC.

CMS(Concurrent Mark Sweep) collector goes through different phases. 

Initial Mark --> Stop the world process. It marks the objects in the old generation reachable  from root references. 
This can happen quickly because it just marks the live references.

Concurrent Mark --> Concurrent process. Traverses object graph looking for live objects for the objects that are marked
in "Inital Mark" phase. Any allocations made during this phase goes through a write barrier and are automatically
marked as live.

Remark --> Stop the world phase. It finds objects created after "Concurrent Mark" phase is stopped and Re-mark them.

Concurrent Sweep --> This is concurrent process. It collects all the objects that were not marked in the previous phase.

Resetting --> Resets everything ready for the next GC run.

G1 Collector
------------
It is introduced in java 6, officially supported in java 7. It is a compacting collector. It was planned as a replacement for CMS.

G1 collector is meant for server applications, typically running on multiprocessor machines with large memories.

G1 Collector splits heap into regions. Instead of splitting memory into Eden, tenure and old spaces it breaks memory into regions. It still has the concept of all regions but managed differently.

G1 collector moves(or copies) objects between one region to another based on minor/major GC. This is called evacuation. 
when a minor GC runs, it collects moves objects that are survived into Survivor region spaces and discards all region 
spaces belong to Eden. Similary, when major GC runs, it discards the unused old memory region spaces.

 





