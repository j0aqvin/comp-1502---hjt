package mru.game.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

import mru.game.model.Player;
import mru.game.view.AppMenu;

/*
  this class is the main controller of the entire program.
  it connects all parts together: the menu system, the blackjack game logic, 
  and reading/writing player data to a file.
  it manages the player list in memory, controls when games start or stop,
  and handles saving/loading from the text file.
*/
public class GameManager {

	private static final String DB_PATH = "res/CasinoInfo.txt"; 
	// path to the player data file (res/CasinoInfo.txt)
	// each line contains: name,balance,wins

	private ArrayList<Player> players;  // stores all players currently in memory
	private CardDeck sharedDeck;        // shared deck of cards used for all blackjack rounds

	public GameManager() {
		ensureResAndDb();   // ensure the res folder and data file exist
		players = new ArrayList<Player>(); // starts with an empty list
		sharedDeck = new CardDeck();       // create and shuffle the card deck
	}

	// starts the main program loop
	public void launchApplication() {
		loadPlayers();  // load saved players from the file first

		Scanner input = new Scanner(System.in);
		AppMenu menu = new AppMenu(input); // use the same Scanner to avoid input issues

		boolean running = true; // controls whether the program keeps running
		while (running) {
			// display menu and get user input
			char choice = menu.showMainMenuGetChoice();

			switch (choice) {
				case 'P': 
					// start a blackjack game
					playFlow(input, menu); 
					break;

				case 'S': 
					// view or search for players
					searchFlow(input, menu); 
					break;

				case 'E': 
					// exit the program
					running = false; 
					break;

				default: 
					// input validation - if user doesnt pick any of the cases
					System.out.println("invalid choice.\n"); 
			}
		}

		// save all player data before exiting
		savePlayers(); 
		System.out.println("Saving...");
		System.out.println("Done! Please visit us again!");
	}

	// handles the blackjack playing process
	private void playFlow(Scanner input, AppMenu menu) {
		String name = menu.promptName(); // asks for player name

		if (name.length() == 0) { 
			// prevents blank input/empty names
			System.out.println("name cannot be empty.\n"); 
			return; 
		}

		// determine if this is a new or returning player
		boolean isNew = (findByName(name) == null);

		// get existing player or create a new one with $100 starting balance
		Player p = getOrCreatePlayer(name);

		// displays welcome message and shows current balance
		menu.showWelcome(p.getName(), p.getBalance(), isNew);

		// prevent play if the balance is zero
		if (p.getBalance() == 0) { 
			System.out.println("your balance is $0. returning to main menu.\n"); 
			return; 
		}

		// create a blackjack game using the shared deck
		BlackjackGame game = new BlackjackGame(sharedDeck);

		boolean again = true;
		while (again) {
			// ask for the betting amount
			int bet = menu.promptBet(p.getBalance());
			if (bet == 0) break; // return to main menu if user enters 0

			// play one round (this method updates player stats internally)
			game.playRound(input, bet, p);

			// spacing for readability
			System.out.println();

			// ask if the player wants to continue playing
			System.out.print("Do you want to continue(y/n)? ");
			String ans = input.nextLine().trim();
			again = ans.length() > 0 && Character.toUpperCase(ans.charAt(0)) == 'Y';
		}
	}

	// handles searching or viewing player information
	private void searchFlow(Scanner input, AppMenu menu) {
		boolean back = false;

		while (!back) {
			char c = menu.showSearchMenuGetChoice();

			switch (c) {
				case 'T': { 
					// display top players by win count
					ArrayList<Player> tops = getTopPlayers();

					if (tops.isEmpty()) {
						System.out.println("no players in database.");
					} else {
						System.out.println("              - TOP PLAYERS -");
						System.out.println("+====================+=================+");
						System.out.println("| NAME               | # WINS          |");
						System.out.println("+====================+=================+");

						for (int i = 0; i < tops.size(); i++) {
							Player tp = tops.get(i);

							String nameCol = tp.getName();
							String winsCol = String.valueOf(tp.getWins());

							// table alignment
							while (nameCol.length() < 18) nameCol += " ";
							while (winsCol.length() < 7) winsCol += " ";

							System.out.println("| " + nameCol + " | " + winsCol + "         |");
							System.out.println("+--------------------------------------+");
						}
					}

					menu.pauseEnter(); // pauses before going back
					break;
				}

				case 'N': { 
					// search for player by name
					String name = menu.promptSearchName();
					Player p = findByName(name);

					if (p == null) {
						System.out.println("player not found.");
					} else {
						// inline table for one player
						System.out.println("                       - PLAYER INFO -");
						System.out.println("+====================+=================+=================+");
						System.out.println("| NAME               | # WINS          | BALANCE         |");
						System.out.println("+====================+=================+=================+");

						String nameCol = p.getName();
						String winsCol = String.valueOf(p.getWins());
						String balCol = p.getBalance() + "  $";

						// add spaces to align columns
						while (nameCol.length() < 18) nameCol += " ";
						while (winsCol.length() < 7) winsCol += " ";
						while (balCol.length() < 13) balCol += " ";

						System.out.println("| " + nameCol + " | " + winsCol + "         | " + balCol + "   |");
						System.out.println("+--------------------------------------------------------+");
						System.out.println();
					}

					menu.pauseEnter();
					break;
				}

				case 'B': 
					// return to main menu
					back = true; 
					break;

				default: 
					System.out.println("invalid choice.\n");
			}
		}
	}

	// ensures both the folder and data file actually exist
	private void ensureResAndDb() {
		try {
			File resDir = new File("res");
			if (!resDir.exists()) resDir.mkdirs();

			File db = new File(DB_PATH);
			if (!db.exists()) db.createNewFile();
		} catch (IOException e) {
			System.out.println("[error] can't init resources: " + e.getMessage());
		}
	}

	// loads players from text file into the ArrayList
	public void loadPlayers() {
		players.clear(); // clear any old data first
		File f = new File(DB_PATH);
		if (!f.exists()) return;

		Scanner sc = null;
		try {
			sc = new Scanner(f);
			while (sc.hasNextLine()) {
				String line = sc.nextLine().trim();
				if (line.length() == 0) continue; // skip empty lines

				String[] parts = line.split(",");
				if (parts.length == 3) {
					String name = parts[0].trim();
					int bal = safeInt(parts[1].trim(), 0);
					int wins = safeInt(parts[2].trim(), 0);
					players.add(new Player(name, bal, wins));
				}
			}
		} catch (Exception e) {
			System.out.println("[warn] load failed: " + e.getMessage());
		} finally {
			if (sc != null) sc.close();
		}
	}

	// saves all player data to the text file
	public void savePlayers() {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(DB_PATH, false)); // overwrite existing file
			for (int i = 0; i < players.size(); i++) {
				Player p = players.get(i);
				out.println(p.getName() + "," + p.getBalance() + "," + p.getWins());
			}
		} catch (IOException e) {
			System.out.println("[error] save failed: " + e.getMessage());
		} finally {
			if (out != null) out.close();
		}
	}

	// finds a player by name or creates a new one if not found
	public Player getOrCreatePlayer(String name) {
		Player p = findByName(name);
		if (p != null) {
			return p;
		}

		Player np = new Player(name, 100, 0); // default new player with $100 and 0 wins
		players.add(np);
		return np;
	}

	// searches the player list for a name (case-insensitive)
	public Player findByName(String name) {
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.getName().equalsIgnoreCase(name)) return p;
		}
		return null;
	}

	// finds the player(s) with the most wins
	public ArrayList<Player> getTopPlayers() {
		ArrayList<Player> out = new ArrayList<Player>();
		int maxWins = -1;

		// find the highest win count
		for (int i = 0; i < players.size(); i++) {
			int w = players.get(i).getWins();
			if (w > maxWins) maxWins = w;
		}

		if (maxWins < 0) return out; // no players found

		// ddd all players who match that win count
		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			if (p.getWins() == maxWins) out.add(p);
		}
		return out;
	}

	// converts string to integer safely (avoids crashes)
	private int safeInt(String s, int def) {
		try { 
			return Integer.parseInt(s); 
		} catch (NumberFormatException e) { 
			return def; 
		}
	}
}
