package edu.guilford;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Scanner;

public class ThirtyOneDriver {
    private static final int INITIAL_CARDS = 3;
    private static final int MAX_TURNS_WITHOUT_KNOCK = 100;
    private static final int MIN_PLAYERS = 2;
    private static final int MAX_PLAYERS = 16;
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int numPlayers;
        
        // Get number of players with validation
        while (true) {
            System.out.println("Enter number of players (" + MIN_PLAYERS + "-" + MAX_PLAYERS + "): ");
            try {
                numPlayers = Integer.parseInt(scanner.nextLine());
                if (numPlayers >= MIN_PLAYERS && numPlayers <= MAX_PLAYERS) {
                    break;
                } else {
                    System.out.println("Please enter a number between " + MIN_PLAYERS + " and " + MAX_PLAYERS);
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number");
            }
        }

        // Initialize game with specified number of players
        Queue<Player> players = new LinkedList<>();
        for (int i = 1; i <= numPlayers; i++) {
            System.out.println("Enter name for Player " + i + ": ");
            String playerName = scanner.nextLine().trim();
            // Use default name if empty input
            if (playerName.isEmpty()) {
                playerName = "Player " + i;
            }
            players.add(new Player(playerName));
        }

        System.out.println("\nStarting game with " + numPlayers + " players:");
        for (Player p : players) {
            System.out.println("- " + p.getName());
        }

        boolean gameOver = false;
        while (!gameOver) {
            playRound(players);
            // Check if game is over (only one player with lives remaining)
            int playersAlive = 0;
            for (Player p : players) {
                if (p.getLives() > 0) playersAlive++;
            }
            gameOver = playersAlive <= 1;
        }

        // Announce winner
        for (Player p : players) {
            if (p.getLives() > 0) {
                System.out.println("\nGame Over! " + p.getName() + " wins!");
                break;
            }
        }
        
        scanner.close();
    }

    private static void playRound(Queue<Player> players) {
        System.out.println("\n--- New Round ---");
        
        // Initialize deck and piles
        Deck deck = new Deck();
        deck.shuffle();
        Stack<Card> stockPile = new Stack<>();
        Stack<Card> discardPile = new Stack<>();

        // Check if we have enough cards for all players
        int activePlayers = 0;
        for (Player p : players) {
            if (p.getLives() > 0) activePlayers++;
        }
        
        if (activePlayers * INITIAL_CARDS > 52) {
            System.out.println("Error: Not enough cards for " + activePlayers + " players!");
            System.out.println("Need " + (activePlayers * INITIAL_CARDS) + " cards but deck only has 52.");
            return;
        }

        // Deal initial cards
        for (Player p : players) {
            if (p.getLives() > 0) {
                for (int i = 0; i < INITIAL_CARDS && deck.size() > 0; i++) {
                    p.getHand().addCard(deck.deal());
                }
            }
        }

        // Move remaining cards to stock pile
        while (deck.size() > 0) {
            stockPile.push(deck.deal());
        }

        // Start discard pile
        if (!stockPile.isEmpty()) {
            discardPile.push(stockPile.pop());
        } else {
            System.out.println("Error: Not enough cards to start the game!");
            return;
        }

        // Play round
        boolean roundOver = false;
        int turnsAfterKnock = -1;
        int turnCount = 0;
        Player knocker = null;

        while (!roundOver) {
            turnCount++;
            
            // Force round end if maximum turns reached
            if (turnCount > MAX_TURNS_WITHOUT_KNOCK && turnsAfterKnock == -1) {
                System.out.println("Maximum turns reached without knock - forcing round end!");
                turnsAfterKnock = players.size() - 1;
            }

            Player currentPlayer = players.peek();
            
            if (currentPlayer.getLives() <= 0) {
                players.add(players.remove());
                continue;
            }

            System.out.println("\n" + currentPlayer.getName() + "'s turn");
            System.out.println("Current hand: \n" + currentPlayer.getHand());
            System.out.println("Top of discard: " + discardPile.peek());

            // Player's turn - check for knock
            if (!currentPlayer.hasKnocked() && turnsAfterKnock == -1 && currentPlayer.decideToKnock()) {
                System.out.println(currentPlayer.getName() + " knocks!");
                currentPlayer.setKnocked(true);
                knocker = currentPlayer;
                turnsAfterKnock = players.size() - 1;
            }

            // Check if both piles are empty
            if (stockPile.isEmpty() && discardPile.isEmpty()) {
                System.out.println("Both piles are empty - ending round!");
                roundOver = true;
                break;
            }

            // Take card
            Card newCard;
            if (currentPlayer.decideToTakeFromDiscard(discardPile.peek()) && !discardPile.isEmpty()) {
                newCard = discardPile.pop();
                System.out.println(currentPlayer.getName() + " takes from discard pile");
            } else if (!stockPile.isEmpty()) {
                newCard = stockPile.pop();
                System.out.println(currentPlayer.getName() + " takes from stock pile");
            } else if (!discardPile.isEmpty()) {
                newCard = discardPile.pop();
                System.out.println(currentPlayer.getName() + " must take from discard pile");
            } else {
                System.out.println("Error: No cards available!");
                roundOver = true;
                break;
            }
            
            currentPlayer.getHand().addCard(newCard);

            // Discard
            int discardIndex = currentPlayer.decideCardToDiscard();
            Card discarded = currentPlayer.getHand().getCard(discardIndex);
            currentPlayer.getHand().removeCard(discarded);
            discardPile.push(discarded);
            System.out.println(currentPlayer.getName() + " discards " + discarded);

            // Check if round is over
            if (turnsAfterKnock >= 0) {
                turnsAfterKnock--;
                if (turnsAfterKnock < 0) {
                    roundOver = true;
                }
            }

            // Rotate to next player
            players.add(players.remove());

            // If stock pile is empty, shuffle discard pile except top card
            if (stockPile.isEmpty() && discardPile.size() > 1) {
                Card topDiscard = discardPile.pop();
                ArrayList<Card> tempList = new ArrayList<>();
                while (!discardPile.isEmpty()) {
                    tempList.add(discardPile.pop());
                }
                java.util.Collections.shuffle(tempList);
                for (Card c : tempList) {
                    stockPile.push(c);
                }
                discardPile.push(topDiscard);
            }
        }

        // Round is over, determine winner and losers
        endRound(players, knocker);
    }

    private static void endRound(Queue<Player> players, Player knocker) {
        System.out.println("\nRound Over!");
        int highestScore = 0;
        ArrayList<Player> losers = new ArrayList<>();

        // Find highest score among players with lives
        for (Player p : players) {
            if (p.getLives() > 0) {
                int score = p.getHand().getTotalValue();
                System.out.println(p.getName() + "'s score: " + score);
                if (score > highestScore) {
                    highestScore = score;
                }
            }
        }

        // Handle knocker rules
        if (knocker != null) {
            boolean knockerBeatsAnyone = false;
            int knockerScore = knocker.getHand().getTotalValue();
            
            for (Player p : players) {
                if (p != knocker && p.getLives() > 0) {
                    int score = p.getHand().getTotalValue();
                    if (knockerScore > score) {
                        knockerBeatsAnyone = true;
                    }
                }
            }
            
            if (!knockerBeatsAnyone) {
                // Knocker loses two lives
                knocker.loseLife();
                knocker.loseLife();
                System.out.println(knocker.getName() + " (knocker) loses two lives for not beating anyone!");
                return;  // Round ends here
            }
        }

        // Determine losers (lowest score)
        int lowestScore = highestScore;
        for (Player p : players) {
            if (p.getLives() > 0) {
                int score = p.getHand().getTotalValue();
                if (score < lowestScore) {
                    lowestScore = score;
                }
            }
        }

        // Add all players with lowest score to losers (unless they're the knocker)
        for (Player p : players) {
            if (p.getLives() > 0 && p.getHand().getTotalValue() == lowestScore && p != knocker) {
                losers.add(p);
            }
        }

        // Apply penalties
        for (Player p : losers) {
            p.loseLife();
            System.out.println(p.getName() + " loses a life! Remaining lives: " + p.getLives());
        }

        // Reset hands and knocked status
        for (Player p : players) {
            p.getHand().reset();
            p.setKnocked(false);
        }
    }
}