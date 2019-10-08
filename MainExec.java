import java.awt.Color; // Colour
import java.awt.Desktop; // Open application
import java.io.File; // File
import java.util.ArrayList; // AL
import java.util.Scanner; // User Input

import edu.cmu.ri.createlab.terk.robot.finch.Finch;

//Comments referenced with '// *' indicate required or additional functionalities

public class MainExec {
	
	// Class/Field Variables

	Finch myFinch;
	MainApp app;
	
	/*Constructor*/

	public MainExec() throws InterruptedException {

		myFinch = new Finch();
		int sec = userInput();

		app = new MainApp(myFinch, sec);

	}
	
	/*Gets the number of seconds for recording as an input from the user*/

	public int userInput() {

		Scanner console = new Scanner(System.in);
		int number = 0;
		boolean isValid = false;

		System.out.println("\nPlease enter a number between 1 and 20: ");

		do { // Do-While Loop until input is valid
			try {
				number = console.nextInt();
				if (number < 1 || number > 20) {
					System.out.println("Invalid, Please ONLY enter a number between 1 and 20: "); // * Error message given when invalid input
				} else {
					isValid = true;
				}
			} catch (Exception e) {
				isValid = false; // Set to false if number invalid
				System.out.println("Invalid Input, Please only use numbers: ");
				console.next();
			}
		} while (!(isValid));

		return number;
	}
	
	/*Runs the program*/

	public void run() throws InterruptedException {

		ArrayList<Integer> playbackOrder = app.recordOrientation();

		myFinch.setLED(Color.GREEN); // * Set COLOUR to Green during execution
		
		System.out.println("\nRunning...\n");
		
		if (!(playbackOrder.isEmpty())) {
			for (int x : playbackOrder) {
				app.execute(x);
				Thread.sleep(500); // * After each corresponding execution, the finch should wait 500ms.
			}
		} else {
			System.out.println("ERROR: No orientations were recorded, please ensure to correctly tilt the Finch");
		}

		for (int i = 0; i < 2; i++) { // * Beep twice to end the game.
			myFinch.buzz(600, 500);
			Thread.sleep(1000);
		}
		
		app.writeToFile(playbackOrder);

		myFinch.quit();
	}
	
	/* Insturctions */
	
	public static void displayInstructions() {
		
		System.out.println("Welcome to \"Tilt Control\" (Task 7)");
		System.out.println("\nInstructions:");
		System.out.println("\n1) Enter the number of seconds you would like the program to record the Finch’s orientation");
		System.out.println("2) Press ENTER to confirm your choice");
		System.out.println("3) You will be provided with a 3 second warning before being required to move the Finch");
		System.out.println("4) Tilt the Finch as you like, attempting to remain accurate with each orientation");
		System.out.println("5) Once the Finch is on the ground, it will begin executing the corresponding movements of the tilts recorded");
		System.out.println("\nNOTE: If a tilt can't be detected, the last detected tilt is recorded\n");
	}

	public static void main(String[] args) throws Exception {
		
		displayInstructions();
		
		Thread.sleep(2000);
		
		boolean end = false;

		while (!end) {

			MainExec exec = new MainExec(); // Creating an object of main execution class
			exec.run();

			Scanner scan = new Scanner(System.in);

			System.out.println("Would you like to play again? Please enter the either 'Yes' or 'No': ");
			String choiceInp = scan.nextLine();

			while (!(choiceInp.equalsIgnoreCase("yes") || choiceInp.equalsIgnoreCase("no"))) {
				System.out.println("This isn't a valid input. Please enter Yes or No: ");
				choiceInp = scan.nextLine();
			}

			if (choiceInp.equals("no")) {
				System.out.println("\nGoodbye!");
				Desktop.getDesktop().open(new File("executionLog.txt")); // Auto-opens log with OS default application
				end = true;
			}
		}

	}

}
