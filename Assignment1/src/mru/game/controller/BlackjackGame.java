package mru.game.controller;

import java.util.ArrayList;
import java.util.Scanner;

import mru.game.model.Player;

/*
  notes:
  - prints and runs one round of blackjack
  - shows only the dealer’s first card until the player stands or busts
*/
public class BlackjackGame {

	private CardDeck deck; // shared deck passed in from game manager

	public BlackjackGame(CardDeck deck) {
		this.deck = deck; // remember the shared deck so we can draw cards from it
	}

	/*
	  plays one full round:
	  - deals 2 to player, 2 to dealer
	  - lets player hit/stand (menu)
	  - if player stands and didn’t bust, dealer draws to 17
	  - prints the final result and updates the player object (wins/balance)
	  returns the net change to the player balance (positive = won, negative = lost, 0 = push)
	*/
	public int playRound(Scanner input, int bet, Player player) {

		// hands for this round
		ArrayList<Card> playerHand = new ArrayList<Card>();
		ArrayList<Card> dealerHand = new ArrayList<Card>();

		// initial deal: player, dealer, player, dealer
		playerHand.add(drawFromDeck());
		dealerHand.add(drawFromDeck());
		playerHand.add(drawFromDeck());
		dealerHand.add(drawFromDeck());

		// show the table once at the beginning (hide dealer’s second card here)
		printBoard(playerHand, dealerHand, true);

		boolean playerBust = false;  // becomes true if player goes over 21
		boolean playerStand = false; // becomes true when player chooses stand

		// player turn loop: keep asking until stand or bust
		while (!playerBust && !playerStand) {
			// simple menu like sample
			System.out.println("Select an option:\n");
			System.out.println("                  1. Hit");
			System.out.println("                  2. Stand\n");
			System.out.print("Your choice: ");

			String ans = input.nextLine().trim();
			System.out.println(); // spacing after user input so it looks clean

			if (ans.equals("1")) {
				// player hits → add one card and show the table again (still hiding dealer hole)
				playerHand.add(drawFromDeck());
				printBoard(playerHand, dealerHand, true);

				// check if player busted after this hit
				if (handValue(playerHand) > 21) {
					playerBust = true;
				}
			} else if (ans.equals("2")) {
				// player stands → exit the loop and let dealer play
				playerStand = true;
			}
			// note: any other input just keeps the loop going and shows menu again
		}

		// if player didn’t bust, now the dealer draws until at least 17
		if (!playerBust) {
			while (handValue(dealerHand) < 17) {
				dealerHand.add(drawFromDeck());
			}
		}

		// final table: reveal all dealer cards now
		printBoard(playerHand, dealerHand, false);

		// figure out who won and by how much
		int playerVal = handValue(playerHand);
		int dealerVal = handValue(dealerHand);
		int delta = 0; // net change to balance (returned to caller)

		// 4 cases: player busts, player beats dealer, dealer beats player, or push
		if (playerBust) {
			// player busted → immediate loss
			player.addToBalance(-bet);
			delta = -bet;
			System.out.println("You lost " + bet + "$");
		} else if (dealerVal > 21 || playerVal > dealerVal) {
			// dealer busts or player total is higher → player wins
			player.addToBalance(bet);
			player.addWin(); // track wins for the “top players” feature
			delta = bet;
			System.out.println("You won " + bet + "$");
		} else if (playerVal < dealerVal) {
			// dealer total is higher → player loses
			player.addToBalance(-bet);
			delta = -bet;
			System.out.println("You lost " + bet + "$");
		} else {
			// same totals → push (no money changes hands)
			System.out.println("Push (tie)");
		}

		System.out.println(); // blank line before the “continue (y/n)” prompt (printed by gamemanager)
		return delta; // tell caller the net change so it could be used if needed
	}


	// printing helpers (just ascii layout and manual spacing)

	private void printBoard(ArrayList<Card> player, ArrayList<Card> dealer, boolean hideDealerHole) {
	    // header lines (copied shape to match the sample on assignment guidelines)
	    System.out.println("               -- BLACK JACK --");
	    System.out.println("+======================+=====================+");
	    System.out.println("|| PLAYER              | DEALER             ||");
	    System.out.println("+======================+=====================+");

	    // number of rows we need to print equals the larger hand size
	    int rows = Math.max(player.size(), dealer.size());

	    // print each row: left = player card, right = dealer card (or blank)
	    for (int i = 0; i < rows; i++) {
	        String left = "";   // player card text for this row
	        String right = "";  // dealer card text for this row

	        // fill in left side if player has a card at this row
	        if (i < player.size()) {
	            left = player.get(i).toString(); // e.g., "7 of Diamond"
	        }

	        // fill in right side if dealer has a card at this row
	        if (i < dealer.size()) {
	            if (hideDealerHole && i >= 1) {
	                // if we are still hiding, only show dealer’s first card (index 0)
	                right = ""; 
	            } else {
	                right = dealer.get(i).toString();
	            }
	        }

	        // pad the two columns with spaces so things line up under the headers
	        // (numbers chosen by eyeballing the header widths)
	        while (left.length() < 20) {
	            left += " ";
	        }
	        while (right.length() < 19) {
	            right += " ";
	        }

	        // print the row (two columns) and a separator line below it
	        System.out.println("| " + left + "|| " + right + " |");
	        System.out.println("+----------------------+---------------------+");
	    }

	    System.out.println(); // blank line after the table to separate it from the next text
	}

	// small string helper (not used right now, but left here in case we want to pad something quickly)
	private String padRight(String s, int width) {
		StringBuilder sb = new StringBuilder();
		if (s == null) s = "";
		sb.append(s);
		while (sb.length() < width) sb.append(' ');
		return sb.toString();
	}

	// drawing and scoring

	/*
	  draws the top card from the shared deck:
	  - if the deck is empty, rebuilds a fresh 52-card deck and shuffles it
	  - returns one Card and removes it from the end of the arraylist
	*/
	private Card drawFromDeck() {
		// use only the exposed list from CardDeck (we are not changing CardDeck.java)
		ArrayList<Card> d = deck.getDeck();

		// if empty, make a new full deck and shuffle it
		if (d.isEmpty()) {
			// same suits and ranks as the provided Card / CardDeck setup
			String[] suits = { "Spades", "Diamond", "Clubs", "Hearts" };
			for (int i = 0; i < 4; i++) {
				for (int r = 1; r <= 13; r++) {
					d.add(new Card(r, suits[i]));
				}
			}

			// very simple shuffle: swap each position with a random earlier one (or itself)
			for (int i = 0; i < d.size(); i++) {
				int j = (int)(Math.random() * (i + 1));
				Card tmp = d.get(i);
				d.set(i, d.get(j));
				d.set(j, tmp);
			}
		}

		// remove from the end (treat it like the “top” of the deck)
		return d.remove(d.size() - 1);
	}

	/*
	  calculates the blackjack total for a hand:
	  - number cards are their face value
	  - 10/jack/queen/king count as 10
	  - aces count as 11 first, then are reduced to 1 if we bust
	*/
	private int handValue(ArrayList<Card> hand) {
		int total = 0;
		int aces = 0; // count how many aces we treated as 11 so we can reduce if needed

		// add up the raw values
		for (int i = 0; i < hand.size(); i++) {
			int r = hand.get(i).getRank();
			if (r == 1) {         // ace
				total += 11;
				aces++;
			} else if (r >= 10) { // 10, jack(11), queen(12), king(13)
				total += 10;
			} else {
				total += r;       // 2..9
			}
		}

		// if we busted and we counted aces as 11, reduce them to 1 until we’re <= 21 or we run out of aces
		while (total > 21 && aces > 0) {
			total -= 10; // turning an 11 into a 1 is -10
			aces--;
		}
		return total;
	}
}
