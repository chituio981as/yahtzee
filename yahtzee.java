/*
 * File: Yahtzee.java
 * ------------------
 * This program plays the CS106A - Yahtzee game.
 */

import acm.io.*;
import acm.program.*;
import acm.util.*;

import java.util.ArrayList;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	/* Private instance variables */
	private int nPlayers;
	private int[][] score;
	private int[][] marked;
	private int[] diceRoll = new int[N_DICE];
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();

	public void run() {
		setupPlayers();
		initDisplay();
		playGame();
	}

	/**
	 * Prompts the user for information about the number of players, then sets up the
	 * players array and number of players.
	 */
	private void setupPlayers() {
		nPlayers = chooseNumberOfPlayers();

      /* Set up the players array by reading names for each player. */
		playerNames = new String[nPlayers];
		for (int i = 0; i < nPlayers; i++) {
         /* IODialog is a class that allows us to prompt the user for information as a
          * series of dialog boxes.  We will use this here to read player names.
          */
			IODialog dialog = getDialog();
			playerNames[i] = dialog.readLine("Enter name for player " + (i + 1));
		}
	}

	/**
	 * Prompts the user for a number of players in this game, reprompting until the user
	 * enters a valid number.
	 *
	 * @return The number of players in this game.
	 */
	private int chooseNumberOfPlayers() {
      /* See setupPlayers() for more details on how IODialog works. */
		IODialog dialog = getDialog();

		while (true) {
         /* Prompt the user for a number of players. */
			int result = dialog.readInt("Enter number of players");

         /* If the result is valid, return it. */
			if (result > 0 && result <= MAX_PLAYERS)
				return result;

			dialog.println("Please enter a valid number of players.");
		}
	}

	/**
	 * Sets up the YahtzeeDisplay associated with this game.
	 */
	private void initDisplay() {
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
	}

	/**
	 * Actually plays a game of Yahtzee.  This is where you should begin writing your
	 * implementation.
	 */
	private void playGame() {
		// Initialized the score array in order to hold all the score values;
		score  = new int[N_CATEGORIES][nPlayers];

		// Initialized an array to keep track of categories that have been scored with 0;
		marked = new int[N_CATEGORIES][nPlayers];

		/**
		 * Each player takes a turn to roll the dice 3 times. After the player rolls 3 times, he can select
		 * a category to place the score. Game goes on for N_SCORING_CATEGORIES * nPlayers. At the end
		 * each player score is updated and a winner is decided.
		 */
		for (int category = 0; category < N_SCORING_CATEGORIES; category++){
			for (int player = 0; player < nPlayers; player++){
				firstRoll(player);
				secondRoll();
				thirdRoll();
				addCategoryScore(player);
			}
		}

		// Updating UPPER_SCORE and LOWER_SCORE for each player
		for (int player = 0; player < nPlayers; player++){
			updateScores(player);
		}
		showWinner();
	}

	/** Rolls the dice and assigns values to the five dice */
	private void firstRoll(int player){
		display.printMessage(playerNames[player] + "'s turn! Click \"Roll Dice\" button to roll the dice.");
		display.waitForPlayerToClickRoll(player);

		for (int dice = 0; dice < N_DICE; dice++){
			diceRoll[dice] = rgen.nextInt(1, 6);
		}
		display.displayDice(diceRoll);
	}

	/** Rolls the dice a second time. User can select which dice need to be rolled for new values.*/
	private void secondRoll(){
		display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\"");
		display.waitForPlayerToSelectDice();
		for (int dice = 0; dice < N_DICE; dice++){
			if (display.isDieSelected(dice)){
				diceRoll[dice] = rgen.nextInt(1, 6);
			}
		}
		display.displayDice(diceRoll);
	}

	/** Rolls the dice a third time. User can select which dice need to be rolled for new values.*/
	private void thirdRoll(){
		display.printMessage("Select the dice you wish to re-roll and click \"Roll Again\"");
		display.waitForPlayerToSelectDice();
		for (int dice = 0; dice < N_DICE; dice++){
			if (display.isDieSelected(dice)){
				diceRoll[dice] = rgen.nextInt(1, 6);
			}
		}
		display.displayDice(diceRoll);
	}

	/**
	 * Waits for the player to select a category, checks if it's valid and assigns the value
	 * If the value is not valid category, it assigns the value 0. Updates the TOTAL after each score entry.
	 * Marks a category as already scored in order to block the user to enter another value.
	 */
	private void addCategoryScore(int player){
		display.printMessage("Select a category for this roll.");
		while (true) {
			int category = display.waitForPlayerToSelectCategory();
			if (markScored(category, player)){
				if (checkCategory(diceRoll, category)){
					score[category][player] = calculateScore(category);
					display.updateScorecard(category, player, score[category][player]);
				} else {
					display.updateScorecard(category, player, 0);
				}
				marked[category][player] = 1;
				updateTotal(player);
				break;
			}
			display.printMessage("You already scored this category. Try another.");
		}
	}

	/**
	 * Checks if the category is a valid one and returns true if it is.
	 * Stores the dice values in ArrayLists in order to validate the different categories
	 */
	private boolean checkCategory(int[] dice, int category){
		if (category >= ONES && category <= SIXES || category == CHANCE){
			return true;
		} else {
			ArrayList<Integer> ones   = new ArrayList<>();
			ArrayList<Integer> twos   = new ArrayList<>();
			ArrayList<Integer> threes = new ArrayList<>();
			ArrayList<Integer> fours  = new ArrayList<>();
			ArrayList<Integer> fives  = new ArrayList<>();
			ArrayList<Integer> sixes  = new ArrayList<>();

			// Increasing the size of the corresponding ArrayList by one
			for (int num = 0; num < N_DICE; num++){
				if (dice[num] == 1) ones.add(1);
				else if (dice[num] == 2) twos.add(1);
				else if (dice[num] == 3) threes.add(1);
				else if (dice[num] == 4) fours.add(1);
				else if (dice[num] == 5) fives.add(1);
				else if (dice[num] == 6) sixes.add(1);
			}

			if (category == THREE_OF_A_KIND) {
				if (ones.size() >= 3 || twos.size() >= 3 || threes.size() >= 3 ||
						fours.size() >= 3 || fives.size() >= 3 || sixes.size() >= 3) {
					return true;
				}
			}
			else if (category == FOUR_OF_A_KIND) {
				if (ones.size() >= 4 || twos.size() >= 4 || threes.size() >= 4 ||
						fours.size() >= 4 || fives.size() >= 4 || sixes.size() >= 4) {
					return true;
				}
			}
			else if (category == YAHTZEE) {
				if (ones.size() == 5 || twos.size() == 5 || threes.size() == 5 ||
						fours.size() == 5 || fives.size() == 5 || sixes.size() == 5) {
					return true;
				}
			}
			else if (category == FULL_HOUSE) {
				if (ones.size() == 3 || twos.size() == 3 || threes.size() == 3 ||
						fours.size() == 3 || fives.size() == 3 || sixes.size() == 3) {
					if (ones.size() == 2 || twos.size() == 2 || threes.size() == 2 ||
							fours.size() == 2 || fives.size() == 2 || sixes.size() == 2) {
						return true;
					}
				}
			}
			else if (category == LARGE_STRAIGHT) {
				if (ones.size() == 1 && twos.size() == 1 && threes.size() == 1 &&
						fours.size() == 1 && fives.size() == 1){
					return true;
				}
				else if (twos.size() == 1 && threes.size() == 1 && fours.size() == 1
						&& fives.size() == 1 && sixes.size() == 1) {
					return true;
				}
			}
			else if (category == SMALL_STRAIGHT) {
				if (ones.size() >= 1 && twos.size() >= 1 && threes.size() >= 1 &&
						fours.size() >= 1) {
					return true;
				}
				else if (twos.size() >= 1 && threes.size() >= 1 && fours.size() >= 1 &&
						fives.size() >= 1) {
					return true;
				}
				else if (threes.size() >= 1 && fours.size() >= 1 && fives.size() >= 1 &&
						sixes.size() >= 1) {
					return true;
				}
			}
		}
		return false;
	}

	// Checks if a category has already been scored
	private boolean markScored(int category, int player){
		return (marked[category][player] == 0);
	}

	// Updating the total each time the user enters a value
	private void updateTotal(int player){
		int totalScore = 0;
		for (int cat = 0; cat < N_CATEGORIES - 1; cat++){
			totalScore += score[cat][player];
		}
		display.updateScorecard(TOTAL, player, totalScore + score[UPPER_BONUS][player]);
		score[TOTAL][player] = totalScore;

		System.out.println(score[TOTAL][player]);
	}

	/**
	 * Returns the sum of all the dice with the same value
	 * Example: if the you have 3 TWOS at the end of the rolls, clicking the TWO category
	 * will add the score 3 * 2 = 6.
	 */
	private int calculateScore(int category){
		int result = 0;

		switch (category){
			case ONES: return diceValue(1);
			case TWOS: return diceValue(2);
			case THREES: return diceValue(3);
			case FOURS: return diceValue(4);
			case FIVES: return diceValue(5);
			case SIXES: return diceValue(6);

			case THREE_OF_A_KIND: case FOUR_OF_A_KIND: case CHANCE:
				for (int i = 0; i < N_DICE; i++){
					result += diceRoll[i];
				}
				return result;

			case FULL_HOUSE: return 25;
			case SMALL_STRAIGHT: return 30;
			case LARGE_STRAIGHT: return 40;
			case YAHTZEE: return 50;
		}
		return 0;
	}

	// Sums up the dice with the same values
	private int diceValue(int dice){
		int result = 0;
		for (int i = 0; i < N_DICE; i++){
			if (diceRoll[i] == dice){
				result += dice;
			}
		}
		return result;
	}

	/**
	 * Updates the UPPER_SCORE, LOWER_SCORE and awards the BONUS.
	 * Loops through the upper categories and sums them up, resulting in the UPPER_SCORE.
	 * If the score is above 63, the bonus score is awarded and added.
	 * The last part loops through the lower scores and adds them up
     */
	private void updateScores(int player){
		int upperScore = 0;
		int lowerScore = 0;
		int bonusScore = 35;

		for (int upperCat = 0; upperCat <= SIXES; upperCat++){
			upperScore += score[upperCat][player];
			score[UPPER_SCORE][player] = upperScore;
			if (upperScore >= 63){
				score[UPPER_BONUS][player] = bonusScore;
				display.updateScorecard(UPPER_BONUS, player, bonusScore);
				score[TOTAL][player] += bonusScore;
				display.updateScorecard(TOTAL, player, score[TOTAL][player]);
			}
		}
		display.updateScorecard(UPPER_SCORE, player, upperScore);


		for (int lowerCat = THREE_OF_A_KIND; lowerCat <= CHANCE; lowerCat++){
			lowerScore += score[lowerCat][player];
			score[LOWER_SCORE][player] = lowerScore;
		}
		display.updateScorecard(LOWER_SCORE, player, lowerScore);
	}

	// Congratulates the winner!
	private void showWinner(){
		int winner = 0;
		int winnerScore = 0;

		for (int player = 0; player < nPlayers; player++){
			int totalScore = score[TOTAL][player];
			if (totalScore > winnerScore){
				winnerScore = totalScore;
				winner = player;
			}
		}
		display.printMessage("Congratulations, " + playerNames[winner] + ", you're the winner with a total score of " + winnerScore);
	}
}
