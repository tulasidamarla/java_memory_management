Java Reference Classes
----------------------
We know java has strong references and they are not GC'd until they are released.  But, there are other types of references
available in java.lang.ref package. There are three different types of References are available namely SoftReference,
WeakReference and PhantomReference.

Strong reference is the strongest of all, then followed by soft reference, weak reference and phantom reference.

The difference is object's can be GC'd if there is a soft,weak and phantom reference.

Objects referenced through soft reference will be GC'd if there is a memory pressure. i.e. if we get into a situation of 
"out of memory" at that point of time these type of objects will be GC'd.

Objects that are referenced through weak references can be collected by GC when it runs even when there is no memory pressure. 

Phantom references are different than the above two. If there is a soft reference or weak reference, we can call get() on them
to retrieve the object.But, using phantom reference you cannot retrieve the object even if it alive.

WeakReference Demo
------------------
SoftReference can't be demoed because they will be GC'd only when there is memory pressure. Let's illustrate WeakReference.

Using weak reference we can retrieve the object as long as it is in memory. Also, we know that weak references will be garbage
collected when GC is run. Here is the code to demonstrate both the behaviors.


		Person person = new Person();
		WeakReference<Person> wr=new WeakReference<Person>(person);
		
		Person p1=wr.get();
		System.out.println(p1);
		
		person = null;
		p1 = null;
		Person p2 = wr.get();
		System.out.println(p2); // should be able to retrieve the object using weak reference.
		
		p2 = null;
		System.gc(); // GC is run here
		
		Person p3 = wr.get();
		System.out.println(p3); // should be null here.

Usage of References
-------------------
WeakReferences are often used in WeakHashMap. weak references are used to associate meta data with another type. For ex, 
if a class if final, we cannot add additional data/behavior to it. It is possible to do that using WeakHashMap and WeakReference.

SoftReferences can be used for caching. If an object is expensive to create or to get from remote location this can be referenced
through soft reference. The strong reference can be set to null but if the object is needed later it can be loaded from SoftReference.
If there is a memory pressure, then it will be GC'd. 

Note: SoftReference is not a great mechanism for caching. Because it is managed by GC, there is no control over cache. Also, there is
no control over cache ageing, cannot provide caching with LRU list.

PhantomReference is the least used among all. It is used for interaction with GC. It is used to monitor when object is GC'd or may be
used to do some extra work when it is GC'd. This looks similar to finalize() method, but this works better. finalize() can have
side effects. i.e. it may cause gc to not claim the object but PhantomReference will have no side effect.

WeakHashMap
-----------
WeakHashMap is like a HashMap except that key is a weak reference to an object. i.e. It stores weak reference to an object
as a key and value is the object's metadata.

Note: when object has no more strong references, the key is released and the value goes away.

WeakHashMap Demo
----------------
Let's create a fincal class named Person and PersonMetaData class with a date field, which is object creation time.
	
	final class Person{
	}
	
	class PersonMetaData {
		Date date;

		PersonMetaData() {
			date = new Date();
		}

		@Override
		public String toString() {
			return "PersonMetaData {" + "date=" + date + '}';
		}
	}

Let's write code to demo WeakHashMap by adding a person/metadata and remove the person key to check if value is present .

	WeakHashMap<Person, PersonMetaData> weakHashMap = new WeakHashMap<Person, PersonMetaData>();
	Person kevin = new Person();
	weakHashMap.put(kevin, new PersonMetaData());

	PersonMetaData p = weakHashMap.get(kevin);

	System.out.println(p);

	kevin = null;
	System.gc();

	if (weakHashMap.containsValue(p)) {
		System.out.println("Still contains key");
	} else {
		System.out.println("Key gone");
	}

*****	
Note: The above code is removing the entry some times but not on every run of the code.

ReferenceQueues
---------------
ReferenceQueues are like queues, but no objects are added to it manually. If an object is wrapped with a weak reference i.e.
by extending WeakReference and passing ReferenceQueue to the constructor,then when the original object(strong reference) is
cleared, then the weak reference is added to the reference queue. 

	class PersonWeakReference extends WeakReference<Person>{
		PersonWeakReference(Person p, ReferenceQueue<? super Person> q){
			
		}
	}

Note: This is useful, when you want to associate some cleanup mechanism with an object.
*****
ReferenceQueue has two methods poll and remove. poll returns immediately if present, otherwise null. remove method blocks
until there is a reference on the queue and returns once it is available. remove also has timeout.
so, we don't need to wait indefinitely. once the timeout is over, remove will either return an object or
return null if object is not present.

Note: ReferenceQueues are used to attach clean up code instead of extending reference type. Here is the complete code.

	public class ReferenceQueueDemo {

		public static void main(String[] args) {
			Person p = new Person();
			ReferenceQueue<Person> refQueue= new ReferenceQueue<Person>();
			PersonCleaner cleaner = new PersonCleaner();
			PersonWeakReference weakReference = new PersonWeakReference(p, cleaner, refQueue); //just create weak reference
			
			p = null;
			System.gc();
			
			try {
				PersonWeakReference wr = (PersonWeakReference) refQueue.remove();
				wr.clean();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
		}

	}
	class PersonCleaner{
		public void clean(){
			System.out.println("cleaned");
		}
	}

	class PersonWeakReference extends WeakReference<Person>{
		
		PersonCleaner cleaner;
		public PersonWeakReference(Person referent,PersonCleaner cleaner, ReferenceQueue<? super Person> q) {
			super(referent, q);
			this.cleaner = cleaner;
		}
		
		public void clean(){
			cleaner.clean();
		}
		
	}

PhantomReference
----------------
If we wrap a strong reference in a phantom reference and try to get object using phantom reference we will get null always.
It seems like these are useless. But, they have an use case.

PhantomReferences are used inplace of finalizers. we know finalizers are expensive, because these objects survive atleast one GC.

For SoftReference and WeakReference where ReferenceQueue is optional, whereas for PhantomReference it is must.

PhantomReference Example
------------------------
	public class PhantomReferenceDemo {

		public static void main(String[] args) {
			ReferenceQueue<Person> refQueue= new ReferenceQueue<Person>();
			List<FinalizePerson> list=new ArrayList<FinalizePerson>();
			List<Person> people = new ArrayList<Person>();
			
			for(int i=0;i<10;i++){
				Person p = new Person();
				people.add(p);
				list.add(new FinalizePerson(p, refQueue));
			}
			
			people = null;
			System.gc();
			
			for(PhantomReference<Person> ref:list){
				System.out.println(ref.isEnqueued());
			}
			
			Reference<? extends Person> referenceFromQueue;
			while((referenceFromQueue = refQueue.poll()) != null){
				((FinalizePerson)referenceFromQueue).cleanup();
			}
		}

	}

	class FinalizePerson extends PhantomReference<Person>{
		
		public FinalizePerson(Person referent, ReferenceQueue<? super Person> q) {
			super(referent, q);
			
		}
		
		public void cleanup(){
			System.out.println("cleaned....");
		}
		
	}
  
  Note: Using phantom references have more control on cleanup activities than finalize().
