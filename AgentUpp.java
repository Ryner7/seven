import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ryoto on 2017/10/30.
 */
public class AgentUpp extends AgentMontecarlo {
	//String name = "upp   ";
	int uppSimNum = 100;
	
	AgentUpp() {
		name = "upp   ";
	}
	
	Card strategy(Sevens sevens, int printDepth) {
		
		
		Cards playableAndHold = sevens.playableCards().getIntersection(sevens.turnPlayer.hand);
		
		//手札枚数が一枚で出せるときはそれを返す．
		//if (sevens.turnPlayer.hand.size() == 1 && playableAndHold.size() == 1) return playableAndHold.get(0);
		
		
		//パスが残っている場合は，パスを選択肢に入れる
		if (sevens.turnPlayer.nums.get(Sevens.PASS_INDEX) > 1) {
			playableAndHold.add(Card.createCard(Card.PASS_RANK, Card.SPECIAL_SUIT));
		}
		
		//相手のエージェントの戦略を決める
		ArrayList<AgentSevens> agents = new ArrayList<>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			agents.add(new AgentSevens());
		}
		Sevens simSevens;
		
		//手札枚数をカウント
		ArrayList<Integer> playersHandSize = new ArrayList<>();
		for (Player p : sevens.players) playersHandSize.add(p.hand.size());
		//Player player;
		Cards cards = new Cards();
		//Result result;
		ArrayList<Integer> scores;
		MontecarloSimulation simulation;
		
		
		//シミュレーション
		simulation = upp(sevens, printDepth, playableAndHold, agents, playersHandSize, cards, sevens.history);
		MontecarloSimulation montecarloSimulation = montecarlo(sevens, printDepth, playableAndHold, agents, playersHandSize, cards);
		
		scores = simulation.myScores;
		
		//履歴を残す
		prevSimHistories = montecarloSimulation.histories;
		simHistories.add(prevSimHistories);
		
		//最も値の高いカードを取り出す
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
	
	MontecarloSimulation upp(Sevens sevens, int printDepth, Cards playableAndHold, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards, History realHistory) {
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
		ArrayList<Integer> secretNums = new ArrayList<>();
		
		//手札枚数をカウント，非公開のカードの集合を得る(secret)
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			if (sevens.playersOrder.get(sevens.fakeTurn) == index) {
				secretNums.add(0);
			} else {
				secret = secret.getUnion(sevens.players.get(index).hand);
				secretNums.add(sevens.players.get(index).hand.size());
			}
		}
		
		//世界の確率を求める
		ArrayList<ArrayList<ArrayList<Integer>>> probabilities = probabilityEstimation(prevSimHistories, secretNums, secret, realHistory, sevens.playersOrder.get(sevens.fakeTurn), sevens.totalTurn);
		ArrayList<Cards> world, worldCopy;
		int loop = 0, loopNum = 0;
		int matchNum = 0;//一致回数
		
		
		for (Card card : playableAndHold) {//可能な行動全てに対して
			score = 0;
			loopNum++;
			for ( ; loop < this.uppSimNum * loopNum / playableAndHold.size() ; loop++) {
				//同じ回数シミュレーションを行う
				
				simSevens = new Sevens();
				//シミュレーション環境を整える
				simSevens.setupSevens(sevens.players, sevens.deck, sevens.layout, sevens.allCards, (sevens.fakeTurn + 1) % sevens.players.size(), sevens.totalTurn + 1, MyUtil.SIM, sevens.playersOrder, sevens.history, agents);
				
				//他のプレイヤーの手札を取り"除く"
				for (int index = 0 ; index < simSevens.players.size() ; index++) {
					player = simSevens.players.get(index);
					if (sevens.playersOrder.get(sevens.fakeTurn) != index) {
						player.hand = new Cards();
					}
				}
				
				//cards.sortSuitRank();
				
				//非公開情報を仮定する．(世界の生成)
				world = genWorld(sevens.players.size(), probabilities, secretNums);
				worldCopy = Cards.cardsListDeepCopy(world);
				
				////手札を並び替え，あとで行ってたので削除
				//for (int index = 0 ; index < sevens.players.size() ; index++) {
				//world.get(index).sortSuitRank();
				//}
				
				//手札を配る
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					if (sevens.playersOrder.get(sevens.fakeTurn) == index) {//自分
						simSevens.players.get(index).hand = sevens.players.get(index).hand.deepCopy();//念のためコピー
					} else {//仮定した手札を配る(他者)
						player = simSevens.players.get(index);
						player.hand = world.get(index);
						//player.hand = sevens.players.get(index).hand.deepCopy();
						player.hand.sortSuitRank();
					}
				}
				
				//自分の行動がパスの場合
				if (card.rank == Card.PASS_RANK && card.suit == Card.SPECIAL_SUIT) {
					simSevens.turnPlayer.nums.set(Sevens.PASS_INDEX, simSevens.turnPlayer.nums.get(Sevens.PASS_INDEX) - 1);
					//simSevens.turnPlayer.hand.removeCard(Card.PASS_RANK,Card.SPECIAL_SUIT);
					string = Sevens.PASS_STR;
					
				} else {//自分の行動≠パス
					simSevens.layout.add(card);
					simSevens.turnPlayer.hand.removeCard(card);
					string = Sevens.PLAY_STR;
					
				}
				
				//推測が一致した枚数をカウント
				matchNum += countMatchNum(sevens.players, simSevens.players, sevens.playersOrder.get(sevens.fakeTurn));
				
				//シミュレーションの履歴をとるために，履歴をコピーして行動を追加する．
				history = sevens.history.deepCopy();
				history.addPage(sevens.fakeTurn, sevens.totalTurn, Cards.getReadonlyCard(card), string, simSevens.players);
				simSevens.history = history;
				simSevens.history.simTurn = sevens.totalTurn;
				
				//シミュレーション開始
				result = simSevens.startSevens(printDepth + 1);
				//int tmp = scores.size() - 1;
				
				//スコアをとる
				score += result.scores.get(sevens.playersOrder.get(sevens.fakeTurn));
				result.history.scores = result.scores;
				result.history.simHands = worldCopy;
				histories.add(result.history);
			}
			scores.add(score);
		}
		double meanMatchNum = (1.0 * matchNum) / this.uppSimNum;//シミュレーションの平均一致数
		double randomMatchNum = 0;//ランダムでの一致数
		for (int secretNum : secretNums) {
			randomMatchNum += Math.pow(secretNum, 2);
		}
		randomMatchNum /= secret.size();
		
		//推測精度のデータを追加
		Result.EstimationAccuracy.addData(secret.size(), meanMatchNum, randomMatchNum);
		
		simulation.histories = histories;
		simulation.myScores = scores;
		return simulation;
	}
	
	
	@Deprecated
	int preCheckEstimation(ArrayList<ArrayList<ArrayList<Integer>>> probabilities, ArrayList<Player> players, int myIndex) {
		//(A^2+B^2+C^2)/(A+B+C)
		int suit, rank, pIndex, probability, handNum;
		int sum = 0;
		Player player;
		for (suit = 1; suit < 5 ; suit++) {
			for (rank = 1; rank < 14 ; rank++) {
				for (pIndex = 0; pIndex < players.size() ; pIndex++) {
					sum += probabilities.get(suit).get(rank).get(pIndex);
				}
			}
		}
		int secretNum = 0;
		for (pIndex = 0; pIndex < players.size() ; pIndex++) {
			secretNum += players.get(pIndex).hand.size();
		}
		
		for (suit = 1; suit < 5 ; suit++) {
			for (rank = 1; rank < 14 ; rank++) {
				for (pIndex = 0; pIndex < players.size() ; pIndex++) {
					player = players.get(pIndex);
					handNum = player.hand.size();
					
					probability = probabilities.get(suit).get(rank).get(pIndex);
				}
			}
		}
		return 0;
	}
	
	ArrayList<ArrayList<ArrayList<Integer>>> probabilityEstimation(ArrayList<History> prevSimHistories, ArrayList<Integer> handsNum, Cards secret, History realHistory, int myIndex, int realTotalTurn) {//世界の確率推定
		ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability = new ArrayList<>();
		int player, rank, suit, index = 0, playerNum = handsNum.size();
		ArrayList<ArrayList<Integer>> playerRankProbability;
		ArrayList<Integer> playerProbability;
		int probability;
		
		//初期値
		int defaultProbability = (playerNum + 1) * Test.M * 100;
		
		//推測の初期値を入れる
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
			
			//行動していないときは，モンテカルロ法と同じ
			if (prevSimHistories != null) {
				
				//自分が最後に行動したターンを取り出す
				myLastTurn = realHistory.getLastActionTurn(myIndex);
				
				
				for (History prevSimHistory : prevSimHistories) {//一つの世界に注目する．
					for (index = 0; index < prevSimHistory.simHands.size() ; index++) {//自分を除くそれぞれのプレイヤーについて，
						if (myIndex == index) continue;
						
						//現実での一つ前の行動を取り出す．まだその人が行動していなかったら次の人に進む
						realLastAction = realHistory.getLastPlayerAction(index, myLastTurn);
						if (realLastAction == null) continue;
						
						//仮定での一つ前の行動を取り出す
						simLastAction = prevSimHistory.getPlayerAction(index, myLastTurn, realTotalTurn - 1);
						if (simLastAction == null) continue;//自分のターンまでに行動していたか確認．
						
						//現実とシミュレーションのアクションが異なる場合は次の人に
						if (!realLastAction.equal(simLastAction)) continue;
						
						//その人のスコアを取り出して
						int xxx = prevSimHistory.scores.get(index);
						for (Card card : prevSimHistory.simHands.get(index)) {
							//Cards hand = prevSimHistory.simHands.get(index);
							probability = playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).get(index);
							
							//その手札がまだ出てない & 手札枚数が0でないなら
							if (secret.containsCard(card.rank, card.suit) && (handsNum.get(index) != 0)) {
								
								//可能性を変える
								int yyy = probability + Test.uppVal * (xxx * 2 - playerNum);
								//if (yyy <= 0) playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, 1);//System.out.println("error " + card.getInfoStr() + " " + index + " ");
								//else playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, yyy);
								playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, yyy);
								//System.out.println(card.getInfoStr());
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
		return playerRankSuitProbability;
		
	}
	//ArrayList<ArrayList<ArrayList<Integer>>> estimateActionScore(){
	//ArrayList<ArrayList<ArrayList<Integer>>> scores=new ArrayList<>();
	//int player, rank, suit, index, playerNum = handsNum.size();
	//ArrayList<ArrayList<Integer>> playerRankProbability;
	//ArrayList<Integer> playerProbability;
	//return null;
	//}
	//ArrayList<ArrayList<ArrayList<Integer>>> probabilityEstimation(ArrayList<History> prevSimHistories, ArrayList<Integer> handsNum, Cards secret) {//世界の確率推定
//ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability = new ArrayList<>();
	//int player, rank, suit, index, playerNum = handsNum.size();
	//ArrayList<ArrayList<Integer>> playerRankProbability;
	//ArrayList<Integer> playerProbability;
	//int probability;
	//for (suit = 1; suit < 5 ; suit++) {
	//playerRankSuitProbability.add(new ArrayList<ArrayList<Integer>>());
	//for (rank = 1; rank < 14 ; rank++) {
	//playerProbability = new ArrayList<Integer>();
	//playerRankSuitProbability.get(suit - 1).add(playerProbability);
	//for (player = 0; player < playerNum ; player++) {
	//if (secret.containsCard(rank, suit) && (handsNum.get(player) != 0)) {
	////まだ出てないカード&&プレイヤーの手札が有る
	//playerProbability.add(playerNum * Test.M);
	//} else {
	//playerProbability.add(0);
	//}
	//}
	//}
	//}
	//if (prevSimHistories != null) {
	//for (History prevSimHistory : prevSimHistories) {
	//for (index = 0; index < playerNum ; index++) {
	//int xxx = prevSimHistory.scores.get(index);
	//for (Card card : prevSimHistory.simHands.get(index)) {
	//Cards hand = prevSimHistory.simHands.get(index);
	//probability = playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).get(index);
	//if (secret.containsCard(card.rank, card.suit) && (handsNum.get(index) != 0)) {
	//int yyy = probability * 2 - playerNum+1 + xxx;
	//playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, yyy);
	//}
	//}
	//}
	//}
	//}
	//return playerRankSuitProbability;
//}
//
	
	ArrayList<Cards> genWorld(int size, ArrayList<ArrayList<ArrayList<Integer>>>
			playerRankSuitProbability, ArrayList<Integer> handsNum) {
		//自分・上がった他プレイヤーもリストに含む
		
		//リスト複製
		ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbabilityCopy = new ArrayList<>();
		
		
		int player = 0, rank = 0, suit = 0, index, playerNum = handsNum.size(), total = 0, tmp;
		//ArrayList<ArrayList<Integer>> playerRankProbability;
		ArrayList<Integer> playerProbability;
		
		//正規化するために和を取る
		for (suit = 1; suit < 5 ; suit++) {
			playerRankSuitProbabilityCopy.add(new ArrayList<ArrayList<Integer>>());
			for (rank = 1; rank < 14 ; rank++) {
				playerProbability = new ArrayList<Integer>();
				playerRankSuitProbabilityCopy.get(suit - 1).add(playerProbability);
				for (player = 0; player < playerNum ; player++) {
					
					//
					tmp = playerRankSuitProbability.get(suit - 1).get(rank - 1).get(player);
					
					//負の場合は0にする．
					if (tmp <= 0) {
						tmp = 0;
						playerRankSuitProbability.get(suit - 1).get(rank - 1).set(player, 0);
					}
					
					playerProbability.add(tmp);
					total += tmp;
					//System.out.println(suit + "  " + rank + "  " + tmp + "  " + total + "  ");
				}
			}
		}
		
		//生成した世界を入れる
		ArrayList<Cards> playersCards = new ArrayList<>();
		for (int count = 0 ; count < size ; count++) {
			playersCards.add(new Cards());
		}
		
		
		int randNum, probability;
		Random rand = new Random();
		//ArrayList<ArrayList<Integer>> playerRankProbability;
		int sum;
		int x = 0;
		//System.out.println();
		//Cards tmpCards=null;
		
		//すべて配り終えるまで
		while (total > 0) {
			//System.out.println(total);
			//tmpCards=new Cards();
			
			//乱数を生成
			randNum = rand.nextInt(total);
			sum = 0;
		   LOOP:
			for (suit = 1; suit < 5 ; suit++) {
				for (rank = 1; rank < 14 ; rank++) {
					for (player = 0; player < handsNum.size() ; player++) {
						//累積分布的な方法でカードを決定する
						probability = playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).get(player);
						sum += probability;
						//if (sum == 0 || probability == 0) continue;
						if (randNum < sum) {
							//playersCards.get(player).add(Card.createCard(rank,suit));
							break LOOP;
						}
					}
				}
			}
			x++;
			
			//確率から求めたカードを手札に追加する
			playersCards.get(player).add(Card.createCard(rank, suit));
			//System.out.println(rank+", "+suit+".");
			
			//和を再計算し，取り出したカードの確率をゼロにする．
			for (index = 0; index < handsNum.size() ; index++) {
				total -= playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).get(index);
				playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).set(index, 0);
				//tmpCards=tmpCards.getUnion(playersCards.get(index));
			}
			
			//tmpCards.showCardsWithSpace(0);
			
			//もし，プレイヤーの手札枚数が，現実の枚数と同じになったら，
			//そのプレイヤーに関する確率をゼロにして，和を再計算
			if (playersCards.get(player).size() >= handsNum.get(player)) {
				for (suit = 1; suit < 5 ; suit++) {
					for (rank = 1; rank < 14 ; rank++) {
						total -= playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).get(player);
						playerRankSuitProbabilityCopy.get(suit - 1).get(rank - 1).set(player, 0);
					}
				}
			}
			
		}
		//tmpCards=tmpCards;
		return playersCards;
	}
	
}
