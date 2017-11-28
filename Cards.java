import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ryoto on 2017/10/17.
 */
public class Cards extends ArrayList<Card> {
	private static Cards readonlyCards;
	private static boolean readonlyCreated = false;
	
	static Card getReadonlyCard(Card card) {
		return getReadonlyCard(card.rank, card.suit);
	}
	
	static Card getReadonlyCard(int rank, int suit) {
		if (!readonlyCreated) {
			readonlyCreated = true;
			readonlyCards = createCards(65);
		}
		for (Card readonly : Cards.readonlyCards) {
			if (rank == readonly.rank && suit == readonly.suit) {
				return readonly;
			}
		}
		return null;
	}
	
	static Cards getReadonlyCards(Cards cards) {
		Cards readonlyCards = new Cards();
		for (Card card : cards) {
			readonlyCards.add(Cards.getReadonlyCard(card));
		}
		return readonlyCards;
	}
	
	static Cards createCards(int num) {
		Cards cards = new Cards();
		for (int count = 0 ; count < num ; count++) {
			cards.add(Card.createCard(count));
		}
		return cards;
	}
	
	
	boolean containsCard(Card card) {
		return this.containsCard(card.rank, card.suit);
	}
	
	boolean containsCard(int rank, int suit) {
		for (Card card : this) {
			if (card.rank == rank && card.suit == suit) return true;
		}
		return false;
	}
	
	Card getCard(int rank, int suit) {
		for (Card card : this) {
			if (card.rank == rank && card.suit == suit) {
				return card;
			}
		}
		return null;
	}
	
	Card getRankCard(int rank) {
		for (Card card : this) {
			if (card.rank == rank) {
				return card;
			}
		}
		return null;
	}
	
	Card getSuitCard(int suit) {
		for (Card card : this) {
			if (card.suit == suit) {
				return card;
			}
		}
		return null;
	}
	
	Card getSuitRankCard(int suitRank) {
		for (Card card : this) {
			if (card.getSuitRank() == suitRank) {
				return card;
			}
		}
		return null;
	}
	
	Card indexOfCard(Card card) {
		return this.indexOfCard(card.rank, card.suit);
	}
	
	Card indexOfCard(int rank, int suit) {
		int count;
		for (count = 0; count < this.size() ; count++) {
			if (this.get(count).equal(rank, suit)) {
				return this.get(count);
			}
		}
		return null;
	}
	
	void sortRankSuit() {
		Cards sorted = new Cards();
		Card min;
		while (this.size() > 0) {
			min = this.get(0);
			for (Card card : this) {
				if (min.getRankSuit() > card.getRankSuit()) {
					min = card;
				}
			}
			sorted.add(min);
			this.remove(min);
		}
		while (sorted.size() > 0) this.add(sorted.remove(0));
		
		return;
	}
	
	void sortSuitRank() {
		Cards sorted = new Cards();
		Card min;
		while (this.size() > 0) {
			min = this.get(0);
			for (Card card : this) {
				if (min.getSuitRank() > card.getSuitRank()) {
					min = card;
				}
			}
			sorted.add(min);
			this.remove(min);
		}
		while (sorted.size() > 0) this.add(sorted.remove(0));
		
		return;
	}
	
	Cards deepCopy() {
		Cards copy = new Cards();
		for (Card card : this) {
			copy.add(card.deepCopy());
		}
		return copy;
	}
	static ArrayList<Cards> cardsListDeepCopy(ArrayList<Cards> cardsList){
		ArrayList<Cards> cardsListCopy=new ArrayList<>();
		for(Cards cards:cardsList){
			cardsListCopy.add(cards.deepCopy());
		}
		return cardsListCopy;
	}
	
	Cards shuffle() {
		Random rnd = new Random();
		//int ran;
		Cards cards = new Cards();
		while (0 < this.size()) {
			cards.add(this.remove((rnd.nextInt(this.size()))));
		}
		//=cards;
		this.mergeCards(cards);
		return this;
	}
	
	Cards mergeCards(Cards source) {
		while (source.size() > 0) {
			this.add(source.remove(0));
		}
		return this;
	}
	
	void removeCard(int rank, int suit) {
		Card card = this.indexOfCard(rank, suit);
		this.remove(card);
	}
	
	void removeCard(Card target) {
		Card card = this.indexOfCard(target);
		this.remove(card);
	}
	
	Cards getUnion(Cards cards) {
		Cards union = this.deepCopy();
		for (Card card : cards) {
			if (!union.containsCard(card)) {
				union.add(card.deepCopy());
			}
		}
		return union;
	}
	
	Cards getDifferenceSet(Cards cards) {
		Cards differenceSet = new Cards();
		for (Card card : this) {
			if (!cards.containsCard(card)) {
				differenceSet.add(card.deepCopy());
			}
		}
		return differenceSet;
	}
	
	Cards getIntersection(Cards cards) {
		Cards intersectionSet = new Cards();
		for (Card card : this) {
			if (cards.containsCard(card)) {
				intersectionSet.add(card.deepCopy());
			}
		}
		return intersectionSet;
	}
	
	void showCards(int depth) {
		for (Card card : this) {
			MyUtil.dp(card.getInfoStr() + " ", depth);
		}
	}
	
	void showCardsWithSpace(int depth) {
		this.sortRankSuit();
		Card card;
		for (int count = 14 ; count < 66 ; count++) {
			//myUtil.p("YY");
			card = this.getSuitRankCard(count);
			if (card != null) {
				MyUtil.play.dp(card.getInfoStr(), depth);
			} else MyUtil.play.dp("   ", depth);
			//myUtil.p(count%13);
			if (count % 13 == 0) MyUtil.play.dpln("", depth);
		}
	}
	boolean checkDuplication(){
		Cards cards =new Cards();
		boolean check=false;
		for(Card card :this){
			if(cards.containsCard(card)){
				System.out.println(card.getInfoStr());
				check=true;
			}
			cards.add(card);
		}
		return check;
	}
	boolean hasJoker(){
		for(Card card :this){
			if(card.isJoker()){
				return true;
			}
		}
		return false;
	}
	Card getJoker(){
		for(Card card:this){
			if(card.isJoker()){
				return card;
			}
		}
		return null;
	}
	static Cards genDifferenceCardsFromPlayers(ArrayList<Player> players,Cards allCards){
		Cards cards=new Cards();
		for(Player player: players){
			cards=cards.getUnion(player.hand);
		}
		return Cards.getReadonlyCards(allCards.getDifferenceSet(cards));
	}
	static Cards genDifferenceFromCards(ArrayList<Cards> subtrahend,Cards minuend){
		Cards cards=new Cards();
		for(Cards hand: subtrahend){
			cards=cards.getUnion(hand);
		}
		return Cards.getReadonlyCards(minuend.getDifferenceSet(cards));
	}
	static int matchNum(Cards alphas,Cards betas){
		int count=0;
		for(Card alpha:alphas){
			if(betas.containsCard(alpha))count++;
		}
		return count;
	}
}
