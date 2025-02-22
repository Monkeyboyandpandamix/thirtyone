package edu.guilford;

import java.util.ArrayList;
import java.util.Random;

public class Deck {
    private ArrayList<Card> deck = new ArrayList<Card>();
    private Random rand = new Random();

    public Deck() {
        build();
    }

    
    public ArrayList<Card> getDeck() {
        return deck;
    }
    
    public void clear() {
        deck.clear();
    }

    public void build() {
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                deck.add(new Card(suit, rank));
            }
        }
    }

    public void shuffle() {
        ArrayList<Card> tempDeck = new ArrayList<Card>();
        while (deck.size() > 0) {
            int loc = rand.nextInt(deck.size());
            tempDeck.add(deck.get(loc));
            deck.remove(loc);
        }
        deck = tempDeck;
    }

    public Card pick(int i) {
        Card picked = deck.remove(i);
        return picked;
    }

    public Card deal() {
        return deck.remove(0);
    }

    public int size() {
        return deck.size();
    }

    public String toString() {
        String deckString = "";
        for (Card card : deck) {
            deckString += card.toString() + "\n";
        }
        return deckString;
    }
}
