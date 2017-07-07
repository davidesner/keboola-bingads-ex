package keboola.bingads.ex;

/**
 * @author David Esner
 */
public class Main {

	public static void main(String[] args) {
		if (args.length == 0) {
			System.out.print("No parameters provided.");
			System.exit(1);
		}
		Runner bRunner = new Runner();
		try {
			bRunner.run(args[0]);
		} catch (Exception e) {
			System.out.print("Excecution Failed!");
			e.printStackTrace();
			System.exit(1);

		}
	}

}
