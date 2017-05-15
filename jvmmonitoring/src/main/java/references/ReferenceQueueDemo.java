package references;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

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