import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ryoto on 2017/10/17.
 */
public class Sevens {
	final static int PASS_INDEX = 0;
	final static int END_INDEX = 1;
	final static int RANK_INDEX = 2;
	final static int SCORE_INDEX = 3;
	final static int REMAINING_NUM = 0;
	final static int FINISH_NUM = 1;
	final static int LAST_NUM = 2;
	final static int RETIRE_NUM = 3;
	final static String ALONE_STR = "alone and finish";
	final static String PLAY_AND_FINISH_STR = "play and finish";
	final static String PLAY_STR = "play";
	final static String PASS_STR = "pass";
	final static String RETIRE_STR = "retire";
	Player turnPlayer;
	Cards deck, layout,allCards;
	int fakeTurn;
	ArrayList<Integer> playersOrder;
	ArrayList<Player> players;
	MyUtil myUtil = new MyUtil();
	int printDepth = 0;
	History history;
	int remainingNum;
	int retireNum;
	int totalTurn = -1;
	int initTurn = 0;
	
	Sevens() {
	}
	
	Result playSevens(ArrayList<Player> players) {
		MyUtil myUtil = new MyUtil();
		myUtil.pln("setup sevens");
		beginSevens(players);
		myUtil.pln("start sevens");
		Result result = startSevens(0);
		myUtil.pln("close sevens");
		closeSevens(0);
		return result;
	}
	
	void beginSevens(ArrayList<Player> argPlayers) {
		players = argPlayers;
		Random rnd = new Random();
		playersOrder = new ArrayList<Integer>();
		for (int count = 0 ; count < players.size() ; count++) {
			playersOrder.add(count);
			players.get(count).agent.simHistories = new ArrayList<>();//履歴削除
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
		history.playersOrder = playersOrder;
		deck = new Cards();
		layout = new Cards();
		fakeTurn = 0;
		deck = Cards.createCards(Test.cardSize);
		allCards=deck.deepCopy();
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
	
	void setupSevens(ArrayList<Player> argPlayers, Cards argDeck, Cards argLayout,Cards argAllCards, int argFakeTurn, int argTotalTurn, int argPrintDepth, ArrayList<Integer> argPlayerOrder, History argHistory, ArrayList<AgentSevens> agents) {
		players = new ArrayList<Player>();
		retireNum = 0;
		remainingNum = argPlayers.size() - 1;
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
		for (int index = 0 ; index < players.size() ; index++) {
			Player player = players.get(index);
			player.agent = agents.get(index);
		}
		deck = argDeck.deepCopy();
		layout = argLayout.deepCopy();
		allCards=argAllCards.deepCopy();
		initTurn = argFakeTurn;
		totalTurn = argTotalTurn;
		printDepth = argPrintDepth;
		playersOrder = new ArrayList<Integer>();
		for (int order : argPlayerOrder) {
			playersOrder.add(order);
		}
		history = argHistory.deepCopy();
		turnPlayer = players.get(playersOrder.get((initTurn + players.size() - 1) % players.size()));
	}
	
	
	Result startSevens(int printDepth) {
		MyUtil.dpln("PLAY", MyUtil.PLAY + printDepth);
		int alone, count, tmp;
		ArrayList<Cards> hands;
		Card playCard;
		//	Player p;
		fakeTurn = initTurn;
	   PLAY:
		while (true) {
		   TURN:
			for ( ; fakeTurn < players.size() ; fakeTurn++) {
				turnPlayer = players.get(playersOrder.get(fakeTurn));
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
					history.addPage(fakeTurn, totalTurn, Cards.getReadonlyCard(Card.END_RANK, Card.SPECIAL_SUIT), ALONE_STR, players);
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
						history.addPage(fakeTurn, totalTurn, Cards.getReadonlyCard(Card.PASS_RANK, Card.SPECIAL_SUIT), RETIRE_STR, players);
					} else {
						history.addPage(fakeTurn, totalTurn, Cards.getReadonlyCard(Card.PASS_RANK, Card.SPECIAL_SUIT), PASS_STR, players);
					}
				} else {
					MyUtil.dpln("play " + playCard.getInfoStr(), MyUtil.PLAY + printDepth);
					
					layout.add(playCard);
					turnPlayer.hand.removeCard(playCard);
					if (turnPlayer.hand.size() == 0) {//手札がなくなった
						MyUtil.dpln("done", MyUtil.PLAY + printDepth);
						turnPlayer.nums.set(END_INDEX, FINISH_NUM);
						turnPlayer.nums.set(SCORE_INDEX, remainingNum--);
						history.addPage(fakeTurn, totalTurn, Cards.getReadonlyCard(playCard), PLAY_AND_FINISH_STR, players);
					} else {
						history.addPage(fakeTurn, totalTurn, Cards.getReadonlyCard(playCard), PLAY_STR, players);
					}
				}
			}
			fakeTurn = 0;
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
		MyUtil.dpln("", MyUtil.PLAY + printDepth);
		int count;
		for (Player player : players) {//順序表示
			count = players.size() - player.nums.get(SCORE_INDEX);
			switch (count) {
				case 1:
					MyUtil.dp("1st: ", MyUtil.PLAY + printDepth);
					break;
				case 2:
					MyUtil.dp("2nd: ", MyUtil.PLAY + printDepth);
					break;
				case 3:
					MyUtil.dp("3rd: ", MyUtil.PLAY + printDepth);
					break;
				default:
					MyUtil.dp(count + "th: ", MyUtil.PLAY + printDepth);
					break;
			}
			MyUtil.dpln(player.name, MyUtil.PLAY + printDepth);
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
		if (!Test.connected1And14) {
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
					if (Test.connected1And14) {
					
					}
				}
			}
			return playable;
		} else {
			boolean[] reachEnd = {false, false, false, false, false, false, false, false};
			int flag = 0;
			Cards cards;
			for (int suit = 1 ; suit < 5 ; suit++) {
				cards = new Cards();
				for (int direction = -1 ; direction < 3 ; direction += 2) {
					pos = 7;
					while (pos != 0 && pos != 14) {
						
						//myUtil.p(bool+" "+pos+" "+suit);
						
						if (!layout.containsCard(pos, suit)) {
							cards.add(Card.createCard(pos, suit));
							//myUtil.pln("added"+playable.size());
							flag = 1;
							break;
						}
						//myUtil.pln();
						pos += direction;
					}
					if (flag == 1) {
						flag = 0;
					} else {
						reachEnd[2 * (suit - 1) + (direction == -1 ? 0 : 1)] = true;
						if (pos == 0) pos = 13;
						else if (pos == 14) pos = 1;
						else for ( ; ; ) System.out.println("ERROR at playable");
						while (pos != 0 && pos != 14 && pos != 7) {
							
							//myUtil.p(bool+" "+pos+" "+suit);
							
							if (!layout.containsCard(pos, suit)) {
								cards.add(Card.createCard(pos, suit));
								//myUtil.pln("added"+playable.size());
								flag = 1;
								break;
							}
							//myUtil.pln();
							pos += direction;
						}
					}
					
				}
				if (cards.size() == 0) ;
				else if (!reachEnd[2 * (suit - 1)] && reachEnd[2 * (suit - 1) + 1]) playable.add(cards.get(1));
				else if (reachEnd[2 * (suit - 1)] && !reachEnd[2 * (suit - 1) + 1]) playable.add(cards.get(0));
				else if (!reachEnd[2 * (suit - 1)] && !reachEnd[2 * (suit - 1) + 1])
					playable = playable.getUnion(cards);
			}
			
			return playable;
		}
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
