import java.util.Random;

/**
 * Created by ryoto on 2017/10/30.
 */
public class AgentRandom extends AgentSevens {
	String name="random";
	AgentRandom(){
		name="random";
	}
	
	Card strategy(Sevens sevens,int printDepth) {
		return randomStrategy(sevens);
	}
	Card randomStrategy(Sevens sevens){
		Card card;
		Cards cards=sevens.playableCards().getIntersection(sevens.turnPlayer.hand);
		Random rnd = new Random();
		int index = rnd.nextInt(cards.size());
		card=cards.get(index);
		return card;
	}
	String getName(){
		return name;
	}
}
