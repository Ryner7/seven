/**
 * Created by ryoto on 2017/10/17.
 */
public class Card {
	int suit, rank;
	final static String[] SUIT = {"♠", "♥", "♦", "♣", "★"};
	
	final static String[] COLOR = {"\u001b[00;3" + 6 + "m", "\u001b[00;3" + 5 + "m", "\u001b[00;3" + 3 + "m", "\u001b[00;3" + 2 + "m", "\u001b[00;3" + 7 + "m"};
	final static String COLOR_END = "\u001b[00m";
	final static int ERROR = -1;
	final static int SPECIAL_SUIT = 5;
	final static int JOKER_RANK_1 = 1;
	final static int JOKER_RANK_2 = 2;
	final static int END_RANK = 11;
	//	final static int RETIRE_RANK=12;
	final static int PASS_RANK = 13;
	
	static Card createCard(int rank, int suit) {
		Card card = new Card();
		int errorFlag = 1;
		if (1 <= suit && suit <= 5 && 1 <= rank && rank <= 13) {
			card.rank = rank;
			card.suit = suit;
			errorFlag = 0;
		}
		//System.out.println(rank + " " + suit);
		if (errorFlag == 1) {
			card.suit = ERROR;
			card.rank = ERROR;
			for ( ; ; ) {
				System.out.println("ERROR at createCard " + rank + " " + suit);
				System.exit(1);
			}
		}
		return card;
	}
	
	static Card createCard(int num) {
		return createCard(num % 13 + 1, num / 13 + 1);
	}
	
	int getSuitRank() {
		return suit * 13 + rank;
	}
	
	int rank() {
		return this.rank;
	}
	
	int suit() {
		return this.suit();
	}
	
	int getRankSuit() {
		return rank * 5 + suit;
	}
	
	String getInfoStr() {
		return COLOR[suit - 1] + SUIT[suit - 1] + "" + rank + (rank < 10 ? " " : "") + COLOR_END;
	}
	
	Card deepCopy() {
		return createCard(rank, suit);
	}
	
	boolean equal(Card card) {
		return this.equal(card.rank, card.suit);
	}
	
	boolean equal(int rank, int suit) {
		if (this.rank == rank && this.suit == suit) {
			return true;
		}
		return false;
	}
}
