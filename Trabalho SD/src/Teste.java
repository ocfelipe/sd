
public class Teste {
	public static void main(String[] args) {
		System.out.println("Available processors (cores): " + Runtime.getRuntime().availableProcessors());

		/* Total amount of free memory available to the JVM */
		System.out.println("Free memory (bytes): " + Runtime.getRuntime().freeMemory());
	}
}
