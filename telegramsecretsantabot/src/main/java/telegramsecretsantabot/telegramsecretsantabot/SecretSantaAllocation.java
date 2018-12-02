package telegramsecretsantabot.telegramsecretsantabot;

import java.util.ArrayList;
import java.util.Random;

public class SecretSantaAllocation {

	private int[][] adjMatrix;
	private int totalPlayers;

	public SecretSantaAllocation(int size) {
		totalPlayers = size;
		adjMatrix = new int[totalPlayers][totalPlayers];

	}

	public static void main(String args[]) {
		int size = 6;
		//int size = Integer.parseInt(args[0]);

		SecretSantaAllocation SecretSanta = new SecretSantaAllocation(size);
		SecretSanta.allocateSecretSanta(size);
		
		// assign names to numbers
		// send msg to each person 

	}

	public int[][] allocateSecretSanta(int totalPlayers) {
		System.out.println("------------------ SecretSantaAllocation ----------------");

		ArrayList<String> peopleList = new ArrayList<String>(totalPlayers);
		boolean allAssigned = false;
		int personReceiving;
		int personGiving;
		
		// init adjMatrix
		for (int i = 0; i < totalPlayers; i++) {
			peopleList.add(i + "");
			for (int j = 0; j < totalPlayers; j++) {
				if (i != j) {
					adjMatrix[i][j] = 1;
				} else {
					adjMatrix[i][j] = 0;
				}
			}
		}
		
		System.out.println("peopleList:" + peopleList);

		//printAdjMatrix();

		personGiving = 0;
		while (!allAssigned) {
			personReceiving = getRandomNext(peopleList);
			if (adjMatrix[personGiving][personReceiving] == 1) {
				match(personGiving, personReceiving);
				personGiving++;
			} else {
				personReceiving = getRandomNext(peopleList);
			}

			if (personGiving == totalPlayers) {
				allAssigned = true;
			}

		}
		printResults();
		//printAdjMatrix();
		System.out.println("done");
		return adjMatrix;

	}

	private void match(int personGiving, int personReceiving) {
		for (int i = 0; i < totalPlayers; i++) {

			if (i != personGiving) {
				// removes all other edges to the person receiving
				adjMatrix[i][personReceiving] = 0;
				// removes all other edges from the person giving
				adjMatrix[personGiving][i] = 0;
			}
			// set edge from person giving to person receiving
			adjMatrix[personGiving][personReceiving] = 2;
		}
	}

	private void printAdjMatrix() {
		for (int i = 0; i < totalPlayers; i++) {
			for (int j = 0; j < totalPlayers; j++) {
				System.out.print(adjMatrix[i][j]);
			}
			System.out.println("\n");
		}

	}
	
	private void printResults() {
		for (int i = 0; i < totalPlayers; i++) {
			for (int j = 0; j < totalPlayers; j++) {
				if (adjMatrix[i][j] == 2) {
					System.out.println(i + "gives" + j);
				}
			}
		}
	}

	private int getRandomNext(ArrayList<String> peopleList) {
		int chosenOne = new Random().nextInt(peopleList.size());
		//System.out.println("chosenOne: " + chosenOne);
		return Integer.parseInt(peopleList.get(chosenOne));
	}
}
