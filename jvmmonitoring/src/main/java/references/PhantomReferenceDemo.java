package references;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

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