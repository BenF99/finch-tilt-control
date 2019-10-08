import java.awt.Color; // Colour
import java.io.IOException; // Handling IO file exceptions
import java.nio.file.Files; // Directory
import java.nio.file.Path; // Path
import java.nio.file.Paths; // Path
import java.nio.file.StandardOpenOption; // File Opening Format
import java.text.DateFormat; // Date Format
import java.text.SimpleDateFormat; // Simple Date Format
import java.util.ArrayList; // ArrayList
import java.util.Arrays; // Arrays
import java.util.Calendar; // Calender
import java.util.Random; // Random

import edu.cmu.ri.createlab.terk.robot.finch.Finch; // Finch

// Comments referenced with '// *' indicate required or additional functionalities

public class MainApp {
	
	// Class/Field Variables
	Finch f;
	private int seconds;
	
	// Constants

	private static final int STRAIGHT = 1;
	private static final int TURN_LEFT = 2;
	private static final int TURN_RIGHT = 3;
	private static final int BACK = 4;
	private static final int STOP = 5;
	
	// Constructor
	
	public MainApp(Finch f, int seconds) {
		this.f = f;
		this.seconds = seconds;
	}
	
	// Getter
	
	public int getSeconds() {
		return seconds;
	}
	
	/*Checks Finch's Orientation*/
	
	public void checkFinchLevel() throws InterruptedException {
		while (!f.isFinchLevel()) {
			Thread.sleep(2000); // Causes InterruptedException
		}
	}
	
	
	/*Records the orientation of the Finch*/
	
	public ArrayList<Integer> recordOrientation() throws InterruptedException {
		
		ArrayList<Integer> recList = new ArrayList<Integer>(); // Recorded tilts
		
		System.out.println("After three beeps, please begin moving the Finch...");
	    
	    for (int i = 0; i < 3; i ++) { // * BUZZES to notify user of recording stage
	    	f.buzz(600, 500);
	    	Thread.sleep(1000);
	    }
	    
	    System.out.println("\nRecording...\n");
		
		long endTime = System.currentTimeMillis() + getSeconds()*1000; // *1000 to convert S to MS
		while (System.currentTimeMillis() < endTime) {	
			f.setLED(Color.RED); // * Set LED to RED while RECORDING
			try {
				int state = checkState();
				if (!(state == 0)) { // If an orientation has been correctly recorded it gets appended to the arrayList
					recList.add(state);
				}
				else {
					if (recList.size() > 1) { // Checking if recList isn't empty
						recList.add(recList.get(recList.size() - 1)); // If the Finch is incorrectly orientated, the last recorded state is appended
						System.out.println("NOTICE: Please ensure to correnctly tilt the Finch, the last correct orientation has been recorded");
					}
				}
				f.buzz(500, 100);
				Thread.sleep(500); // * Sleeps for 500ms
			} catch (Exception e) {	// Catches any exceptions -> Usually hardware related																																													
				e.printStackTrace();
			} 
		}
		f.setLED(0,0,0); // * Set led to OFF after recording stage
		f.buzz(700, 1000); // * BUZZ to indicate recording has ended
		System.out.println("\nPlease place the Finch on the ground.");
		Thread.sleep(2000); // * Wait 2 seconds for user to put the finch down
		checkFinchLevel(); // * Checks if the Finch is level before execution
		return recList;
	}
	
	/*Returns the orientation of the Finch*/
	
	public int checkState() { 
		int state = 0;
		if (f.isBeakUp())
			state = STRAIGHT; // 1
		else if (f.isLeftWingDown())
			state = TURN_LEFT; // 2
		else if (f.isRightWingDown())
			state = TURN_RIGHT; // 3
		else if (f.isBeakDown())
			state = BACK; // 4
		else if (f.isFinchLevel())
			state = STOP; // 5
		
		return state;
		

	}
	
	/*Generates random velocity to be used in execute()*/
	
	public int ranVelocity() { 
		Random rand = new Random();
		int randomNum = rand.nextInt((255 - 30) + 1) + 30; // Min set to 30 to ensure FINCH doesn't travel too slowly.
		return randomNum;
	}
	
	/*Executes the corrsponding action depending on the recorded tilts.*/
	
	public void execute(int state) {
		int vel = ranVelocity();
		switch (state) {
			case STRAIGHT: // 1 
				f.saySomething("Moving Forward");
				f.setWheelVelocities(vel,vel, 1000);
				break;
			case TURN_LEFT: // 2
				f.saySomething("Turning Left");
				f.setWheelVelocities(0, vel, 1000); // 120 for r/l 90d.
				break;
			case TURN_RIGHT: // 3
				f.saySomething("Turning Right");
				f.setWheelVelocities(vel, 0, 1000);  
				break;
			case BACK: // 4
				f.saySomething("Moving Backwards");
				f.setWheelVelocities(-vel, -vel, 1000);
				break;
			case STOP: // 5
				f.saySomething("Stopping");
				f.setWheelVelocities(0, 0, 1000);
				break;
			default:
				break;
		}
		
	}
	
	/*Writes the date and time, as well as the Order in which the Finch executed the translated orientations*/
	
	public void writeToFile(ArrayList<Integer> order) {
		
		// FalseExistSetup written only if NEW FILE generated
		
		String FalseExistSetup = 
				
		"Key:\n" +	
		"\nSTRAIGHT = 1" +
		"\nTURN_LEFT = 2" +
		"\nTURN_RIGHT = 3" +
		"\nBACK = 4" +
		"\nSTOP = 5\n" +
		"\n_____________________________\n";

		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		String datetime = dateFormat.format(cal.getTime());

		String toAppend = datetime + ": " + order; // yyyy/MM/dd HH:mm:ss + Order of states
		
		try {
		    Path path = Paths.get("./executionLog.txt"); // '.' to reference directory of the JAR/EXE
		    if (!Files.exists(path)) { // IF the path doesn't exist, a new file is created with the KEY for the states included
		    	Files.write(path, Arrays.asList(FalseExistSetup), StandardOpenOption.CREATE);
		    }
		    Files.write(path, Arrays.asList(" ", toAppend), StandardOpenOption.APPEND); // Uses .APPEND while executionLog exists
		} catch (IOException e) { // Catches any FILE related expections
			System.err.println(e);
		}
	}

}
