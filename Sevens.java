import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ryoto on 2017/10/17.
 */
public class Sevens {
	Player turnPlayer;
	Cards deck, layout;
	int turn;
	ArrayList<Integer> playersOrder;
	ArrayList<Player> players;
	final static int PASS_INDEX = 0;
	final static int END_INDEX = 1;
	final static int RANK_INDEX = 2;
	final static int SCORE_INDEX = 3;
	final static int REMAINING_NUM = 0;
	final static int FINISH_NUM = 1;
	final static int LAST_NUM = 2;
	final static int RETIRE_NUM = 3;
	MyUtil myUtil = new MyUtil();
	int printDepth = 0;
	History history;
	int remainingNum;
	int retireNum;
	int totalTurn = -1;
	
	Sevens() {
	}
	
	Result playSevens(ArrayList<Player> players) {
		MyUtil myUtil = new MyUtil();
		myUtil.pln("setup sevens");
		beginSevens(players);
		myUtil.pln("start sevens");
		Result result = startSevens(1);
		myUtil.pln("close sevens");
		closeSevens(1);
		return result;
	}
	
	void beginSevens(ArrayList<Player> argPlayers) {
		players = argPlayers;
		Random rnd = new Random();
		playersOrder = new ArrayList<Integer>();
		for (int count = 0 ; count < players.size() ; count++) {
			playersOrder.add(count);
		}
		remainingNum = players.size() - 1;
		retireNum = 0;
		int index, tmp;
		for (int size = players.size() - 1 ; 0 < size ; size--) {//プレイヤーシャッフル
			index = rnd.nextInt(size);
			tmp = playersOrder.get(size);
			playersOrder.set(size, playersOrder.get(index));
			playersOrder.set(index, tmp);
		}
		history = new History();
		deck = new Cards();
		layout = new Cards();
		turn = 0;
		deck = Cards.createCards(52);
		
		dealAll();
		
		MyUtil.play.pln("デッキ表示");
		deck.showCardsWithSpace(MyUtil.PLAY);
		
		MyUtil.play.pln("手札表示");
		Player player;
		for (int count = 0 ; count < players.size() ; count++) {
			player = players.get(playersOrder.get(count));
			MyUtil.play.pln(player.name);
			player.hand.showCards(MyUtil.PLAY);
			MyUtil.play.pln();
		}
		
		Cards cards;
		Card card;
		for (Player p : players) {
			if (p.nums == null) p.nums = new ArrayList<Integer>();
			if (p.nums.size() <= PASS_INDEX) p.nums.add(PASS_INDEX, 3);
			else p.nums.set(PASS_INDEX, 3);
			if (p.nums.size() <= END_INDEX) p.nums.add(END_INDEX, REMAINING_NUM);
			else p.nums.set(END_INDEX, REMAINING_NUM);
			if (p.nums.size() <= RANK_INDEX) p.nums.add(RANK_INDEX, 0);
			else p.nums.set(RANK_INDEX, 0);
			if (p.nums.size() <= SCORE_INDEX) p.nums.add(SCORE_INDEX, 0);
			else p.nums.set(SCORE_INDEX, 0);
			for ( ; ; ) {//play sevens
				//showPHand(p);
				cards = p.hand;
				card = cards.getRankCard(7);
				if (card == null) break;
				MyUtil.play.p(card.getInfoStr());
				layout.add(card);
				p.hand.remove(card);
			}
			p.hand.sortSuitRank();
		}
	}
	
	void setupSevens(ArrayList<Player> argPlayers, Cards argDeck, Cards argLayout, int argTurn, int argTotalTurn, int argPrintDepth, ArrayList<Integer> argPlayerOrder, History argHistory ,ArrayList<SevensAgent> agents) {
		players = new ArrayList<Player>();
		retireNum = 0;
		remainingNum = argPlayers.size();
		for (Player player : argPlayers) {
			players.add(player.deepCopy());
			switch (player.nums.get(END_INDEX)) {
				case FINISH_NUM:
					remainingNum--;
					break;
				case RETIRE_NUM:
					retireNum++;
					break;
			}
		}
		for(int index=0;index<players.size();index++){
			Player player=players.get(index);
			player.agent=agents.get(index);
		}
		deck = argDeck.deepCopy();
		layout = argLayout.deepCopy();
		turn = argTurn;
		totalTurn = argTotalTurn;
		printDepth = argPrintDepth;
		playersOrder = new ArrayList<Integer>();
		for (int order : argPlayerOrder) {
			playersOrder.add(order);
		}
		history = argHistory.deepCopy();
	}
	
	
	Result startSevens(int printDepth) {
		MyUtil.dpln("PLAY", MyUtil.PLAY + printDepth);
		int alone, count, tmp;
		ArrayList<Cards> hands;
		Card playCard;
		//	Player p;
	   PLAY:
		while (true) {
		   TURN:
			for (turn = 0; turn < players.size() ; turn++) {
				turnPlayer = players.get(playersOrder.get(turn));
				if (turnPlayer.nums.get(END_INDEX) != REMAINING_NUM) continue;
				totalTurn++;
//				alone=2;
//				for(Player p:players){
//					if (p.nums.get(END_INDEX) == 1) ;//一人以外あがり
//					else alone--;//まだ二人以上あがってない
//				}
				if (remainingNum - retireNum == 0) {//他の皆が上がっている．プレイヤーが残り一人
					while (turnPlayer.hand.size() > 0) {//手札を場に並べるq
						layout.add(turnPlayer.hand.remove(0));
					}
					myUtil.dpln(turnPlayer.name + "  : " + turnPlayer.nums.get(PASS_INDEX) + " passes left", MyUtil.PLAY + printDepth);//パス回数表示
					myUtil.pln("last\n");//パス回数表示
					turnPlayer.nums.set(SCORE_INDEX, remainingNum--);
					turnPlayer.nums.set(END_INDEX, LAST_NUM);
					hands = new ArrayList<Cards>();
					for (Player p : players) {
						hands.add(Cards.getReadonlyCards(p.hand));
					}
					history.addPage(turn, totalTurn, Cards.getReadonlyCard(Card.END_RANK, Card.SPECIAL_SUIT), "alone_and_finish", players);
					break PLAY;//終了
				}
				MyUtil.dpln("___", MyUtil.PLAY + printDepth);
				layout.showCardsWithSpace(MyUtil.PLAY + printDepth);
				for (count = 0; count < players.size() ; count++) {
					Player p = players.get(playersOrder.get(count));
					myUtil.dp("(" + p.key + ") " + p.hand.size() + ", " + p.nums.get(PASS_INDEX) + "  ", MyUtil.PLAY + printDepth);
				}
				MyUtil.dpln("", MyUtil.PLAY + printDepth);
				myUtil.dpln(turnPlayer.name + "  : " + turnPlayer.nums.get(PASS_INDEX) + " passes left", MyUtil.PLAY + printDepth);//パス回数表示
				MyUtil.dp("hands: ", MyUtil.PLAY + printDepth);
				turnPlayer.hand.showCards(printDepth + MyUtil.PLAY);
				MyUtil.dpln("pass", MyUtil.PLAY + printDepth);
				Cards playableCards = playableCards();
				Cards holdAndPlayable = playableCards.getIntersection(turnPlayer.hand);
				
				if (holdAndPlayable.size() <= 0) playCard = null;
				else {
					playCard = turnPlayer.agent.strategy(this, printDepth);
				}
				MyUtil.dp("action: ", MyUtil.PLAY + printDepth);
				if (playCard == null || (playCard.suit == Card.SPECIAL_SUIT && playCard.rank == Card.PASS_RANK)) {
					//パス回数を減らす
					tmp = turnPlayer.nums.get(PASS_INDEX) - 1;
					turnPlayer.nums.set(PASS_INDEX, tmp);
					MyUtil.dpln("pass", MyUtil.PLAY + printDepth);
					
					if (tmp < 0) {//パス回数がない
						while (turnPlayer.hand.size() > 0) {//手札を場に並べる
							layout.add(turnPlayer.hand.remove(0));
						}
						turnPlayer.nums.set(SCORE_INDEX, retireNum);
						retireNum++;
						turnPlayer.nums.set(END_INDEX, RETIRE_NUM);
						myUtil.p(" retire");
						history.addPage(turn, totalTurn, Cards.getReadonlyCard(Card.PASS_RANK, Card.SPECIAL_SUIT), "retire", players);
					} else {
						history.addPage(turn, totalTurn, Cards.getReadonlyCard(Card.PASS_RANK, Card.SPECIAL_SUIT), "pass", players);
					}
				} else {
					MyUtil.dpln("play " + playCard.getInfoStr(), MyUtil.PLAY + printDepth);
					
					layout.add(playCard);
					turnPlayer.hand.removeCard(playCard);
					if (turnPlayer.hand.size() == 0) {//手札がなくなった
						MyUtil.dpln("done", MyUtil.PLAY + printDepth);
						turnPlayer.nums.set(END_INDEX, FINISH_NUM);
						turnPlayer.nums.set(SCORE_INDEX, remainingNum--);
						history.addPage(turn, totalTurn, playCard, "play and finish", players);
					} else {
						history.addPage(turn, totalTurn, playCard, "play", players);
					}
				}
			}
		}
		Result result = new Result();
		for (Player p : players) {
			result.scores.add(p.nums.get(SCORE_INDEX));
		}
		for (int order : playersOrder) {
			result.playerOrder.add(order);
		}
		result.history = history;
		return result;
	}
	
	void closeSevens(int printDepth) {
		int count;
		for (Player player : players) {//順序表示
			count = players.size() - player.nums.get(SCORE_INDEX);
			switch (count) {
				case 1:
					MyUtil.dp("1st: ",printDepth);
					break;
				case 2:
					MyUtil.dp("2nd: ",printDepth);
					break;
				case 3:
					MyUtil.dp("3rd: ",printDepth);
					break;
				default:
					MyUtil.dp(count + "th: ",printDepth);
					break;
			}
			MyUtil.dpln(player.name,printDepth);
		}
	}
	
	boolean canPlay(Card card) {
		int suit = card.suit, pos = 7, direction = 1;
		if (card.rank < 7) direction = -1;
		while (pos != card.rank) {
			boolean bool = layout.containsCard(pos, suit);
			if (!bool) return false;
			pos += direction;
		}
		return true;
	}
	
	Cards playableCards() {
		Cards playable = new Cards();
		int pos;
		for (int suit = 1 ; suit < 5 ; suit++) {
			for (int direction = -1 ; direction < 3 ; direction += 2) {
				pos = 7;
				while (pos != 0 && pos != 14) {
					
					//myUtil.p(bool+" "+pos+" "+suit);
					
					if (!layout.containsCard(pos, suit)) {
						playable.add(Card.createCard(pos, suit));
						//myUtil.pln("added"+playable.size());
						break;
					}
					//myUtil.pln();
					pos += direction;
				}
			}
		}
		return playable;
	}
	
	void dealAll() {
		deck.shuffle();
		int count = 0;
		for (Card card : deck) {
			players.get((count++) % players.size()).hand.add(card);
		}
		deck = new Cards();
	}
}
