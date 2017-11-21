import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ryoto on 2017/11/20.
 */
public class AgentModupp extends AgentUpp {
	
	ArrayList<ArrayList<ArrayList<ArrayList<Integer>>>> allProbabilities;
	
	AgentModupp() {
		name = "upp   ";
		allProbabilities =new ArrayList<>();
	}
	
	
	
	Card strategy(Sevens sevens, int printDepth) {
		Cards playableAndHold = sevens.playableCards().getIntersection(sevens.turnPlayer.hand);
		if (sevens.turnPlayer.hand.size() == 1 && playableAndHold.size() == 1) return playableAndHold.get(0);
		int M = Test.M;
		if (sevens.turnPlayer.nums.get(Sevens.PASS_INDEX) > 1) {
			playableAndHold.add(Card.createCard(Card.PASS_RANK, Card.SPECIAL_SUIT));
		}
		ArrayList<AgentSevens> agents = new ArrayList<>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			agents.add(new AgentSevens());
		}
		Sevens simSevens;
		ArrayList<Integer> playersHandSize = new ArrayList<>();
		for (Player p : sevens.players) playersHandSize.add(p.hand.size());
		//Player player;
		Cards cards = new Cards();
		//		Result result;
		ArrayList<Integer> scores;
		MontecarloSimulation simulation;
		
		simulation = modupp(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards, sevens.history);
		
		scores = simulation.myScores;
		prevSimHistories = simulation.histories;
		simHistories.add(prevSimHistories);
		
		int max = -1;
		int maxIndex = -1;
		for (int index = 0 ; index < scores.size() ; index++) {
			if (max < scores.get(index)) {
				max = scores.get(index);
				maxIndex = index;
			}
		}
		return playableAndHold.get(maxIndex);
	}
	
	String getName() {
		return name;
	}
	
	MontecarloSimulation modupp(Sevens sevens, int printDepth, Cards playableAndHold, int m, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards, History realHistory) {
		int score;
		Sevens simSevens;
		Player player;
		Result result;
		String string = "";
		ArrayList<Integer> scores = new ArrayList<>();
		MontecarloSimulation simulation = new MontecarloSimulation();
		History history;
		ArrayList<History> histories = new ArrayList<>();
		Cards secret = new Cards();
		ArrayList<Integer> handsNum = new ArrayList<>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			if (sevens.playersOrder.get(sevens.fakeTurn) == index) {
				handsNum.add(0);
			} else {
				secret = secret.getUnion(sevens.players.get(index).hand);
				handsNum.add(sevens.players.get(index).hand.size());
			}
		}
		ArrayList<ArrayList<ArrayList<Integer>>> probabilities = probabilityEstimation(prevSimHistories, handsNum, secret, realHistory, sevens.playersOrder.get(sevens.fakeTurn), sevens.totalTurn);
		ArrayList<Cards> world, worldCopy;
		
		for (Card card : playableAndHold) {
			score = 0;
			for (int loop = 0 ; loop < this.simNum / playableAndHold.size() ; loop++) {
				simSevens = new Sevens();
				simSevens.setupSevens(sevens.players, sevens.deck, sevens.layout, sevens.allCards, (sevens.fakeTurn + 1) % sevens.players.size(), sevens.totalTurn + 1, MyUtil.SIM, sevens.playersOrder, sevens.history, agents);
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					player = simSevens.players.get(index);
					if (sevens.playersOrder.get(sevens.fakeTurn) != index) {
						cards = cards.getUnion(player.hand);
						player.hand = new Cards();
					}
				}
				cards.sortSuitRank();
				world = genWorld(sevens.players.size(), secret, handsNum);
				worldCopy = Cards.cardsListDeepCopy(world);
				Cards test = new Cards();
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					world.get(index).sortSuitRank();
//					world.get(index).showCards(0);
//					MyUtil.always.pln();
//					if (sevens.playersOrder.get(sevens.fakeTurn) != index) {
					test = test.getUnion(worldCopy.get(index));
//					}
				}
				test.sortSuitRank();
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					if (sevens.playersOrder.get(sevens.fakeTurn) == index) continue;
					player = simSevens.players.get(index);
					player.hand = world.get(index);
					player.hand.sortSuitRank();
				}
				test = test.getUnion(sevens.layout.deepCopy());
				test = test.getUnion(sevens.turnPlayer.hand.deepCopy());
				test.checkDuplication();
				test.sortSuitRank();
				test = test;
				if (card.rank == Card.PASS_RANK && card.suit == Card.SPECIAL_SUIT) {
					simSevens.turnPlayer.nums.set(Sevens.PASS_INDEX, simSevens.turnPlayer.nums.get(Sevens.PASS_INDEX) - 1);
					string = Sevens.PASS_STR;
					
				} else {
					simSevens.layout.add(card);
					simSevens.turnPlayer.hand.removeCard(card);
					string = Sevens.PLAY_STR;
					
				}
				history = sevens.history.deepCopy();
				history.addPage(sevens.fakeTurn, sevens.totalTurn, Cards.getReadonlyCard(card), string, simSevens.players);
				simSevens.history = history;
				simSevens.history.simTurn = sevens.totalTurn;
				result = simSevens.startSevens(printDepth + 1);
				int tmp = scores.size() - 1;
				score += result.scores.get(sevens.playersOrder.get(sevens.fakeTurn));
				history.simHands = worldCopy;
				history.scores = result.scores;
				histories.add(result.history);
			}
			scores.add(score);
		}
		simulation.histories = histories;
		simulation.myScores = scores;
		return simulation;
	}
	
	
	ArrayList<ArrayList<ArrayList<Integer>>> probabilityEstimation(ArrayList<History> prevSimHistories, ArrayList<Integer> handsNum, Cards secret, History realHistory, int myIndex, int realTotalTurn) {//世界の確率推定
		ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability = new ArrayList<>();
		int player, rank, suit, index = 0, playerNum = handsNum.size();
		ArrayList<ArrayList<Integer>> playerRankProbability;
		ArrayList<Integer> playerProbability;
		int probability;
		int defaultProbability = (playerNum + 1) * Test.M;
		for (suit = 1; suit < 5 ; suit++) {
			playerRankSuitProbability.add(new ArrayList<ArrayList<Integer>>());
			for (rank = 1; rank < 14 ; rank++) {
				playerProbability = new ArrayList<Integer>();
				playerRankSuitProbability.get(suit - 1).add(playerProbability);
				for (player = 0; player < playerNum ; player++) {
					if (secret.containsCard(rank, suit) && (handsNum.get(player) != 0)) {
						//まだ出てないカード&&プレイヤーの手札が有る
						playerProbability.add(defaultProbability);
					} else {
						playerProbability.add(0);
					}
				}
			}
		}
		int myLastTurn = 0;
		Card realLastAction = null, simLastAction = null;
		Cards allCards = Cards.createCards(52);
		int lastActionSuit, lastActionRank, flag = 0, direction, flag1to13 = 0, flag13to1 = 0;
		if (Test.uppEstimationType == 0) {//点数から推測
			if (prevSimHistories != null) {
				myLastTurn = realHistory.getLastActionTurn(myIndex);
				for (ArrayList<History> histories : this.simHistories) {
					for (History prevSimHistory : histories) {//一つの世界に注目する．
						for (index = 0; index < playerNum ; index++) {//それぞれのプレイヤーについて，
							if (myIndex == index) continue;
							//一つ前の行動を取り出す．
							realLastAction = realHistory.getLastPlayerAction(index, myLastTurn);
							if (realLastAction == null) continue;
							
							simLastAction = prevSimHistory.getPlayerAction(index, myLastTurn, realTotalTurn - 1);
							if (simLastAction == null) continue;//自分のターンまでに行動していたか確認．
							if (!realLastAction.equal(simLastAction)) continue;
							//現実もシミュレーションも同じアクションの場合
							int xxx = prevSimHistory.scores.get(index);
							for (Card card : prevSimHistory.simHands.get(index)) {
//						Cards hand = prevSimHistory.simHands.get(index);
								probability = playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).get(index);
								if (secret.containsCard(card.rank, card.suit) && (handsNum.get(index) != 0)) {
									int yyy = probability + 3 * (xxx * 2 - playerNum);
									if (yyy <= 0)
										playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, 1);//System.out.println("error " + card.getInfoStr() + " " + index + " ");
									else
										playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, yyy);
									//System.out.println(card.getInfoStr());
								}
							}
							
						}
					}
				}
			}
			//System.out.println();
		} else if (Test.uppEstimationType == 1) {//場の状況から推測
			for (index = 0; index < playerNum ; index++) {//それぞれのプレイヤーについて，
				if (myIndex == index) continue;
				//一つ前の行動を取り出す．
				realLastAction = realHistory.getLastPlayerAction(index, myLastTurn);
				if (realLastAction == null) continue;
				
				int lastIndexplayerTurn = realHistory.getLastActionTurn(index);
				ArrayList<Cards> hands = realHistory.handsHistory.get(lastIndexplayerTurn);
				
				Cards layout = Cards.genDifferenceFromCards(hands, allCards);//場のカードを生成
				//場のカードと手札からカードを推測
				Cards knownCards = layout.getUnion(hands.get(myIndex));
				lastActionSuit = realLastAction.suit;
				lastActionRank = realLastAction.rank;
				flag1to13 = 1;
				flag13to1 = 1;
				if (Test.connected1And13) {
					//direction = (lastActionRank < 7) ? +1 : -1;
					for (rank = 7; 0 < rank && rank < 14 ; rank++) {
						if (!layout.containsCard(rank, lastActionSuit)) {
							flag13to1 = 0;
							break;
						}
					}
					for (rank = 7; 0 < rank && rank < 14 ; rank--) {
						if (!layout.containsCard(rank, lastActionSuit)) {
							flag1to13 = 0;
							break;
						}
					}
					
					
				}
				int xxx = defaultProbability;
				
				if (lastActionSuit == 5) continue;
				if (flag1to13 == 0 && flag13to1 == 0) {//******7 8 9 10 J **
					if (lastActionRank < 7) {
						for (rank = 1; rank < 7 ; rank++) {
							probability = playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).get(index);
							if (probability == 0) continue;
							playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).set(index, probability + xxx);
						}
					} else {
						for (rank = 7; rank < 14 ; rank++) {
							probability = playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).get(index);
							if (probability == 0) continue;
							playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).set(index, probability + xxx);
						}
					}
				} else if (flag1to13 == 0 && flag13to1 == 1) {// 1*****7 8 9 10 J Q K->
					for (rank = 1; rank < 14 ; rank++) {
						probability = playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).get(index);
						if (probability == 0) continue;
						playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).set(index, probability + xxx);
					}
				} else if (flag1to13 == 1 && flag13to1 == 0) {// <- 1 2 3 4 5 6 7 8 ****** K
					for (rank = 1; rank < 14 ; rank++) {
						probability = playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).get(index);
						if (probability == 0) continue;
						playerRankSuitProbability.get(lastActionSuit - 1).get(rank - 1).set(index, probability + xxx);
					}
				} else {// <- 1 2 3 4 5 6 7 8 9 10 J Q K ->
					//for ( ; ; ) System.out.println("ERROR @ AgentUpp flag");
				}
				
			}
		}
		allProbabilities.add(playerRankSuitProbability);
		return playerRankSuitProbability;
		
	}
//	ArrayList<ArrayList<ArrayList<Integer>>> estimateActionScore(){
//		ArrayList<ArrayList<ArrayList<Integer>>> scores=new ArrayList<>();
//		int player, rank, suit, index, playerNum = handsNum.size();
//		ArrayList<ArrayList<Integer>> playerRankProbability;
//		ArrayList<Integer> playerProbability;
//		return null;
//	}
//		ArrayList<ArrayList<ArrayList<Integer>>> probabilityEstimation(ArrayList<History> prevSimHistories, ArrayList<Integer> handsNum, Cards secret) {//世界の確率推定
//ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability = new ArrayList<>();
//	int player, rank, suit, index, playerNum = handsNum.size();
//	ArrayList<ArrayList<Integer>> playerRankProbability;
//	ArrayList<Integer> playerProbability;
//	int probability;
//		for (suit = 1; suit < 5 ; suit++) {
//		playerRankSuitProbability.add(new ArrayList<ArrayList<Integer>>());
//		for (rank = 1; rank < 14 ; rank++) {
//			playerProbability = new ArrayList<Integer>();
//			playerRankSuitProbability.get(suit - 1).add(playerProbability);
//			for (player = 0; player < playerNum ; player++) {
//				if (secret.containsCard(rank, suit) && (handsNum.get(player) != 0)) {
//					//まだ出てないカード&&プレイヤーの手札が有る
//					playerProbability.add(playerNum * Test.M);
//				} else {
//					playerProbability.add(0);
//				}
//			}
//		}
//	}
//		if (prevSimHistories != null) {
//		for (History prevSimHistory : prevSimHistories) {
//			for (index = 0; index < playerNum ; index++) {
//				int xxx = prevSimHistory.scores.get(index);
//				for (Card card : prevSimHistory.simHands.get(index)) {
//					Cards hand = prevSimHistory.simHands.get(index);
//					probability = playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).get(index);
//					if (secret.containsCard(card.rank, card.suit) && (handsNum.get(index) != 0)) {
//						int yyy = probability * 2 - playerNum+1 + xxx;
//						playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, yyy);
//					}
//				}
//			}
//		}
//	}
//		return playerRankSuitProbability;
//}
//
	
	ArrayList<Cards> genWorld(int size, Cards secret, ArrayList<Integer> handsNum) {
		//自分・上がった他プレイヤーもリストに含む
		
		//リスト複製
		ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbabilityCopy = new ArrayList<>();
		int player = 0, rank = 0, suit = 0, index, playerNum = handsNum.size(), total = 0, tmp;
		//ArrayList<ArrayList<Integer>> playerRankProbability;
		ArrayList<Integer> playerProbabilityCopy;
		ArrayList<ArrayList<ArrayList<Integer>>> probabilities;
		for (suit = 1; suit < 5 ; suit++) {
			playerRankSuitProbabilityCopy.add(new ArrayList<ArrayList<Integer>>());
			for (rank = 1; rank < 14 ; rank++) {
				playerProbabilityCopy = new ArrayList<Integer>();
				playerRankSuitProbabilityCopy.get(suit - 1).add(playerProbabilityCopy);
				tmp=0;
				for (player = 0; player < playerNum ; player++) {
					if(secret.containsCard(rank,suit)) {
						for (int iii=0;iii<allProbabilities.size();iii++){
							probabilities =allProbabilities.get(iii);
							tmp += probabilities.get(suit - 1).get(rank - 1).get(player);
						}
						
						playerProbabilityCopy.add(tmp);
						total += tmp;
					}
					else playerProbabilityCopy.add(0);
					//	System.out.println(suit + "  " + rank + "  " + tmp + "  " + total + "  ");
				}
			}
		}
		
		ArrayList<Cards> playersCards = new ArrayList<>();
		for (int count = 0 ; count < size ; count++) {
			playersCards.add(new Cards());
		}
		int randNum, probability;
		Random rand = new Random();
		ArrayList<ArrayList<Integer>> playerRankProbability;
		int sum;
		int x = 0;
		//System.out.println();
//		Cards tmpCards=null;
		while (total > 0) {
			//	System.out.println(total);
//			tmpCards=new Cards();
			randNum = rand.nextInt(total);
			sum = 0;
		   LOOP:
			for (suit = 1; suit < 5 ; suit++) {
				for (rank = 1; rank < 14 ; rank++) {
					for (player = 0; player < handsNum.size() ; player++) {
						probability = playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).get(player);
						sum += probability;
						if (sum == 0 || probability == 0) continue;
						if (randNum < sum) {
							//playersCards.get(player).add(Card.createCard(rank,suit));
							break LOOP;
						}
					}
				}
			}
			x++;
			playersCards.get(player).add(Card.createCard(rank, suit));
			//	System.out.println(rank+", "+suit+".");
			for (index = 0; index < handsNum.size() ; index++) {
				total -= playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).get(index);
				playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).set(index, 0);
//				tmpCards=tmpCards.getUnion(playersCards.get(index));
			}
//			tmpCards.showCardsWithSpace(0);
			if (playersCards.get(player).size() >= handsNum.get(player)) {
				for (suit = 1; suit < 5 ; suit++) {
					for (rank = 1; rank < 14 ; rank++) {
						total -= playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).get(player);
						playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).set(player, 0);
					}
				}
			}
		}
//		tmpCards=tmpCards;
		return playersCards;
	}
}
