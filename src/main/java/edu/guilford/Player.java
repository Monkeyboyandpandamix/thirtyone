package edu.guilford;

public class Player {
    private Hand hand;
    private int lives;
    private String name;
    private boolean hasKnocked;

    public Player(String name) {
        this.name = name;
        this.hand = new Hand();
        this.lives = 3;
        this.hasKnocked = false;
    }

    public Hand getHand() {
        return hand;
    }

    public int getLives() {
        return lives;
    }

    public void loseLife() {
        lives--;
    }

    public String getName() {
        return name;
    }

    public boolean hasKnocked() {
        return hasKnocked;
    }

    public void setKnocked(boolean knocked) {
        this.hasKnocked = knocked;
    }

    // Simple AI decision making
    public boolean decideToKnock() {
        // Knock if hand value is greater than 25
        return hand.getTotalValue() > 25;
    }

    public int decideCardToDiscard() {
        // Simple strategy: discard the card that contributes least to the total
        int lowestValue = Integer.MAX_VALUE;
        int lowestIndex = 0;
        
        for (int i = 0; i < hand.size(); i++) {
            Card currentCard = hand.getCard(i);
            int cardValue = getCardValue(currentCard);
            if (cardValue < lowestValue) {
                lowestValue = cardValue;
                lowestIndex = i;
            }
        }
        return lowestIndex;
    }

    private int getCardValue(Card card) {
        switch (card.getRank()) {
            case ACE: return 11;
            case TWO: return 2;
            case THREE: return 3;
            case FOUR: return 4;
            case FIVE: return 5;
            case SIX: return 6;
            case SEVEN: return 7;
            case EIGHT: return 8;
            case NINE: return 9;
            case TEN:
            case JACK:
            case QUEEN:
            case KING:
                return 10;
            default: return 0;
        }
    }

    public boolean decideToTakeFromDiscard(Card topDiscard) {
        // Simple strategy: take from discard if the card value is higher than our lowest card
        return getCardValue(topDiscard) > getCardValue(hand.getCard(decideCardToDiscard()));
    }

    public String toString() {
        return name + " (Lives: " + lives + ")\n" + hand.toString();
    }
}