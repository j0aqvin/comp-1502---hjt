package mru.game.application;

// creates the GameManager object and then starts the application

import mru.game.controller.GameManager;

public class AppDriver {

	public static void main(String[] args) {

		// make the game manager - menus, loading/saving players, and running blackjack
		GameManager gm = new GameManager();

		// starts the app loop, this will keep showing menus until the user exits

		gm.launchApplication();
		
	}
}
