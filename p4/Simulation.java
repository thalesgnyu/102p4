package project4;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * A simulation of flooding on a map representing a particular world. 
 * The simulation can be performed using different algorithms and the results 
 * can be visualized. 
 * 
 * 
 * @author Joanna Klukowska
 * @author Thales
 */
public class Simulation {
	// the map of the world
	static Map map;
	// the queue and stack used in the simulation
	static Queue<GridPoint> queue = new LinkedList<GridPoint>();
	static Stack<GridPoint> stack = new Stack<GridPoint>();
	// a 2D array to keep track of which grid points are flooded
	static boolean[][] flooded;	
	
	/**
	 * Runs the simulation of flooding on the map using the specified algorithm
	 * @param args command line arguments: map file, algorithm, and visualize flag
	 */
	public static void main(String[] args)  {
		
		// check for the correct number of arguments
		if (args.length < 3) {
			System.err.println("Usage: java Simulation <map file> <algorithm> <visualize>");
			System.err.println("\n Possible arguments: ");
			System.err.println("       algorithms: queue, stack, or recursive ");
			System.err.println("       visualize: true, false");
			System.exit(1);
		}
		
		// load the map from the file specified at args[0]
		File fileMap = new File(args[0]); 
		if (!fileMap.exists()) {
			System.err.println("File not found: " + args[0]);
			System.exit(1);
		}
	    try {
			map = new Map(fileMap);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: File not found " + args[0]);
		}
	    
        // create a 2D array to keep track of which grid poins are flooded
	    flooded = new boolean[map.numOfRows()][map.numOfCols()];
	    
	    
		// determine if progress should be visualized based on the value of args[2]
	    // check if true, anything else treated as false
		boolean visualize = args[2].equalsIgnoreCase("true"); 
	    
		// determine which algorithm to use based on the value of args[1]
		String algorithm = args[1];
		if (!algorithm.equalsIgnoreCase("queue") 
				&& !algorithm.equalsIgnoreCase("stack") 
				&& !algorithm.equalsIgnoreCase("recursive") ) {
			System.err.println("ERROR: Invalid algorithm: " + args[1]);
			System.err.println("Valid algorithms are: queue, stack, ore recursive");
			System.exit(1);
		}
		// show the map before simulation starts
		// this is displayed regardless of the value of the visualize flag 
		visualize();
		
		switch (algorithm.toLowerCase()) {
		case "queue":
			// use the queue-based algorithm
			simulateWithQueue(visualize);
			break;
		case "stack":
			// use the stack-based algorithm
			simulateWithStack(visualize);
			break;
		case "recursive":
			// use the recursive algorithm
			simulateRecursive(visualize);
			break;
		}    		
		
		// show the map after simulation ends 
		// this is displayed regardless of the value of the visualize flag 
		visualize();
	}
	
	/**
	 * Simulates flooding using a recursive algorithm
	 * 
	 * @param visualize true if the progress should be visualized, false otherwise
	 */
	public static void simulateRecursive(boolean visualize) {
		for (GridPoint i : map.waterSources()) {
			sR(i, visualize);
		}
	}

	/**
	 * Helper method to test and flood gridpoints on the map recursively
	 */
	private static void sR(GridPoint p, boolean v) {
		try {
			if (map.belowOrAtHeight(p) && !underWater(p)) {
				flooded[p.row()][p.col()] = true;
				if (v) {
					visualize();
				}
				sR(p.up(), v);
				sR(p.down(), v);
				sR(p.left(), v);
				sR(p.right(), v);
			}
		} catch (NotOnMapException e) {}
	}
	
	/**
	 * Simulates flooding using a stack-based algorithm
	 * 
	 * @param visualize true if the progress should be visualized, false otherwise
	 */
	public static void simulateWithStack(boolean visualize) {
		for (GridPoint i : map.waterSources()) {
			pushGP(stack, i);
		}
		GridPoint p = null;
		while (!stack.empty()){
			p = stack.pop();
			flooded[p.row()][p.col()] = true;
			if (visualize) {
				visualize();
			}
			pushGP(stack, p.up());
			pushGP(stack, p.down());
			pushGP(stack, p.left());
			pushGP(stack, p.right());
		}
	}

	/**
	 * Helper method to try pushing a Gridpoint to stack
	 */
	private static void pushGP(Stack<GridPoint> s, GridPoint p) {
		try {
			if (map.belowOrAtHeight(p) && !underWater(p)) {
				s.push(p);
			}
		} catch (NotOnMapException e) {}
	}

	/**
	 * Simulates flooding using a queue-based algorithm
	 * 
	 * @param visualize true if the progress should be visualized, false otherwise
	 */
	public static void simulateWithQueue(boolean visualize) {
		for (GridPoint i : map.waterSources()) {
			addGP(queue, i);
		}
		GridPoint p = null;
		while (!queue.isEmpty()){
			p = queue.poll();
			flooded[p.row()][p.col()] = true;
			if (visualize) {
				visualize();
			}
			addGP(queue, p.up());
			addGP(queue, p.down());
			addGP(queue, p.left());
			addGP(queue, p.right());
		}
	}
    
	/**
	 * Helper method to try adding a Gridpoint to queue
	 */
	private static void addGP(Queue<GridPoint> q, GridPoint p) {
		try {
			if (map.belowOrAtHeight(p) && !underWater(p)) {
				q.add(p);
			}
		} catch (NotOnMapException e) {}
	}
	/**
	 * Determines whether a grid point is flooded
	 * 
	 * @param g the grid point
	 * @return true if the grid point is flooded, false otherwise
	 */
	public static boolean underWater ( GridPoint g) {
        return flooded[g.row()][g.col()];
    }
    
	/**
	 * Visualizes the current state of the world. The visualization is done by
	 * printing the map to the console. The map is printed with an indication of
	 * which grid points are flooded and which are not. 
	 * 
	 * WARNING: The visualization does not work well when the elevation
	 * numbers are large or when the map itself is large. 
	 */
    public static void visualize() {
    	visualizeBasic();
    }
    
    /**
     * Visualizes the current state of the world. The visualization is done by
     * printing un-flooded grid points with their with values truncated to integers
     * and flooded grid points are shaded.
     */
    public static void visualizeBasic() {
		clearScreen(); 
		for (int i = 0; i < flooded.length; i++) {
			for (int j = 0; j < flooded[0].length; j++) {
				if (flooded[i][j]) {
					System.out.print("░░ ");
				} else {
					try {
						System.out.print(
								String.format("%2.0f ", map.elevation(new GridPoint(i,j))));
					} catch (NotOnMapException e) {}
				}
			}
			System.out.println();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) { }
	}
    

    /**
     * Visualizes the current state of the world. The visualization is done by
     * printing un-flooded grid points with one decimal place and flooded grid points
     * using "XXX" sequence.
     */	
	public static void visualizeValues()  {
		clearScreen(); 
		for (int i = 0; i < flooded.length; i++) {
			for (int j = 0; j < flooded[0].length; j++) {
				if (flooded[i][j]) {
					System.out.print(" XXX ");
				} else {
					try {
						System.out.print(
								String.format(" %3.1f ", map.elevation(new GridPoint(i,j))));
					} catch (NotOnMapException e) {}
				}
			}
			System.out.println();
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) { }
	}
    
	/**
	 * Visualizes the current state of the world. The visualization is done using 
	 * four different shades. The flooded areas are shown as "  ". The un-flooded
	 * areas are shaded based on their elevation: the lowest 25% of the elevations
	 * are shown as "░░", the next 25% are shown as "▒▒", the next 25% are shown as
	 * "▓▓", and the highest 25% are shown as "██".
	 * 
	 * Disclaimer: This visualization does not work well if your system is not 
	 * set to use UTF-8 encoding. 
	 */
	public static void visualizeShade()  {
		clearScreen(); 
		
		double highPoint = map.maxElevation();
		double lowPoint = map.minElevation();
		double range = highPoint - lowPoint;
		double quarterRange = range / 4; 
		
		//draw the upper border
		System.out.print("╔");
		for (int i = 0; i < flooded[0].length; i++) {
			System.out.print("══");
		}
		System.out.println("╗");
		
		//draw the map with side borders
		for (int i = 0; i < flooded.length; i++) {
			System.out.print("║");
			for (int j = 0; j < flooded[0].length; j++) {
				if (flooded[i][j]) {
					System.out.print("  ");
				} else {
					try {
						System.out.print(
								shade( map.elevation(new GridPoint(i,j)), lowPoint, quarterRange));
					} catch (NotOnMapException e) {}
				}
			}
			System.out.print("║");
			System.out.println();
		}
		//draw the lower border
		System.out.print("╚");
		for (int i = 0; i < flooded[0].length; i++) {
			System.out.print("══");
		}
		System.out.println("╝");
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) { }
	}
	
	/**
	 * Determine the appropriate shade based on the elevation of a grid point.
	 * @param elevation the elevation of the grid point
	 * @param lowPoint the lowest elevation on the map
	 * @param quarterRange the range of elevations that correspond to each shade
	 * @return the appropriate shade for the grid point based on its elevation
	 */
	private static String shade(double elevation, double lowPoint, double quarterRange) {
        // Redirect System.out to use a PrintStream using UTF-8 charset.
        FileOutputStream fos2 = new FileOutputStream(FileDescriptor.out);
        PrintStream ps2 = new PrintStream(fos2, true, StandardCharsets.UTF_8);
        System.setOut(ps2);
        
		if (elevation < lowPoint + quarterRange) {
			return "░░";
		} else if (elevation < lowPoint + 2 * quarterRange) {
			return "▒▒";
		} else if (elevation < lowPoint + 3 * quarterRange) {
			return "▓▓";
		} else {
			return "██";
		}
	}
	
	/**
	 * Clear screen for the purpose of visualization.
	 */
	public static void clearScreen() {
		for (int i = 0; i < 50; i++)
			System.out.println();
	}
}
