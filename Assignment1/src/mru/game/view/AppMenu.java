package mru.game.view;

import java.util.Scanner;

/*
  - this class handles showing menus and getting the user inputs
  - main menu, search menu, and prompts
*/
public class AppMenu {

	private Scanner input;

	public AppMenu(Scanner input) {
		// we pass scanner from gamemanager so we don’t make duplicates
		this.input = input;
	}

	// main menu 
	public char showMainMenuGetChoice() {
		System.out.println("\nSelect one of these options:\n");
		System.out.println("        (P) Play Game");
		System.out.println("        (S) Search");
		System.out.println("        (E) Exit\n");
		System.out.print("Enter a choice: ");

		String choice = input.nextLine().trim();
		System.out.println(); // adds space after user input
		if (choice.length() == 0) return ' ';
		return Character.toUpperCase(choice.charAt(0));
	}

	// search submenu - used for searching for players
	public char showSearchMenuGetChoice() {
		System.out.println("\nSelect one of these options:\n");
		System.out.println("        (T) Top player (Most number of wins)");
		System.out.println("        (N) Looking for a Name");
		System.out.println("        (B) Back to Main menu\n");
		System.out.print("Enter a choice: ");

		String choice = input.nextLine().trim();
		System.out.println(); // adds space after user input
		if (choice.length() == 0) return ' ';
		return Character.toUpperCase(choice.charAt(0));
	}

	// asks for player name at the start of a play session
	public String promptName() {
		System.out.print("Enter your name: ");
		String name = input.nextLine().trim();
		System.out.println(); // space after name
		return name;
	}
	
	// prompt for a name when searching (exact wording)
	public String promptSearchName() {
		System.out.print("What is your name: ");
		String name = input.nextLine().trim();
		System.out.println(); // keep spacing consistent
		return name;
	}

	// prints the welcome box for new or returning players
	public void showWelcome(String name, int balance, boolean isNew) {
		System.out.println("******************************************************************");
		if (isNew) {
			// if player is brand new
			System.out.println("***    Welcome " + name + "    ---   Your initial balance is: " + balance + "  $    ***");
		} else {
			// if player already exists in the database
			System.out.println("***    Welcome back " + name + "    ---   Your initial balance is: " + balance + "  $    ***");
		}
		System.out.println("******************************************************************");
		System.out.println(); // blank line after the welcome banner
	}


	// ask for bet amount
	public int promptBet(int maxBalance) {
		while (true) {
			System.out.print("How much do you want to bet this round? ");
			String s = input.nextLine().trim();
			System.out.println(); // extra space after input, to match spacing

			int bet;
			try {
				// converts input to integer (if invalid, it should catch it)
				bet = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				System.out.println("please enter a whole number.\n");
				continue;
			}
			
			// if player wants to go back
			if (bet == 0) return 0;
			
			// bet validation rule (cannot bet less then $2)
			if (bet < 2) { 
				System.out.println("minimum bet is $2.\n"); 
				continue; 
			}
			if (bet > maxBalance) { 
				System.out.println("over your balance ($" + maxBalance + ").\n"); 
				continue; 
			}
			return bet;
		}
	}

	// pause program and waits for the user to hit enter
	// will be used between menus or after showing results
	public void pauseEnter() {
		System.out.println("\nPress Enter to continue...");
		input.nextLine();
		System.out.println();
	}
}
