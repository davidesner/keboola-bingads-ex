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
		BingAdsRunner bRunner = new BingAdsRunner(args);
		try {
			bRunner.run();
		} catch (Exception e) {
			System.out.print("Excecution Failed!");
			e.printStackTrace();
			System.exit(1);

		}
	}

}
