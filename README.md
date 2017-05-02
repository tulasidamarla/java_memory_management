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
 





