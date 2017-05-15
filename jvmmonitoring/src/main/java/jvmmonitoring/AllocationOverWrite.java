package jvmmonitoring;

import java.util.Random;

public class AllocationOverWrite {

	public static void main(String[] args) {
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
	}

}

class GCMe {
	long a;
	long a1;
	long a2;
	long a3;
	long a4;
	long a5;
	long a6;
	long a7;
	long a8;
	long a9;
	long a10;
	long a11;
	long a12;
	long a13;
	long a14;
	long a15;
	long a16;
	long a17;
	long a18;
	long a19;
	long a20;
	long a21;
	long a22;
	long a23;
}
