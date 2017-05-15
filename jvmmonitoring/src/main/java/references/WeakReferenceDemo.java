package references;

import java.lang.ref.WeakReference;

public class WeakReferenceDemo {

	public static void main(String[] args) {
		Person person = new Person();
		WeakReference<Person> wr=new WeakReference<Person>(person);
		
		Person p1=wr.get();
		System.out.println(p1);
		
		person = null;
		p1 = null;
		Person p2 = wr.get();
		System.out.println(p2);
		
		p2 = null;
		System.gc();
		
		Person p3 = wr.get();
		System.out.println(p3);

	}

}

class Person{
	
}
