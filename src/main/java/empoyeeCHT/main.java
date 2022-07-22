package empoyeeCHT;

public class main {
	public static void main(String args[]) {
		Employee m = new Manager(1,24000);
		Employee s = new Staff(2,24000);
		System.out.println(m.calculateSalary(24000,30000));
		System.out.println(s.calculateSalary(24000,10000));
	}
}
