package references;

import java.util.Date;
import java.util.WeakHashMap;

public class WeakHashMapDemo {

	public static void main(String[] args) {

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
	}

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