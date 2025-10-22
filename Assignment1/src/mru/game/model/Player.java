package mru.game.model;
	
/**
 * this class represent each player record in the Database
 * a model class for each record in the txt file
 * player object keeps name, balance, and number of wins
 * gamemanager uses these to load/save data and update the wins/money
*/

public class Player {

	private String name;
	private int balance;
	private int wins;

	// constructor for new or loaded players
	public Player(String name, int balance, int wins) {
		this.name = name;
		this.balance = balance;
		this.wins = wins;
	}

	// getter for player name
	public String getName() {
		return name;
	}

	// getter for balance (money)
	public int getBalance() {
		return balance;
	}

	// getter for total wins
	public int getWins() {
		return wins;
	}

	// setter for player name
	public void setName(String name) {
		this.name = name;
	}

	// setter for balance
	public void setBalance(int balance) {
		this.balance = balance;
	}

	// setter for wins
	public void setWins(int wins) {
		this.wins = wins;
	}

	// used when player wins a round
	public void addWin() {
		this.wins++;
	}

	// used when changing balance (win/loss)
	public void addToBalance(int amount) {
		this.balance += amount;

		// make sure balance never goes below 0
		if (this.balance < 0) {
			this.balance = 0;
		}
	}
}
