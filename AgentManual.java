import java.util.Scanner;

/**
 * Created by ryoto on 2017/10/30.
 */
public class AgentManual extends SevensAgent {
	String name = "manual";
	AgentManual(){
		name="manual";
	}
	
	Card strategy(Sevens sevens, int printDepth) {
		Scanner scanner = new Scanner(System.in);
		String str;
		char[] ch = {'a'};
		ch[0] = 'a';
		for (int count = 0 ; count < sevens.turnPlayer.hand.size() ; count++) {//キーを表示
			MyUtil.always.p((ch[0]++) + "   ");
		}
		MyUtil.always.p("0  ");//パス用のキー
		int count;
		while (true) {//入力受付
			MyUtil.always.pln();
			str = scanner.nextLine();
			if (str.length() < 1) continue;//誤入力
			if (str.charAt(0) == '0') {//パスを選択
				return null;
			}
			count = str.charAt(0) - 'a';
			if (str.length() < 2 && 0 <= count && count < sevens.turnPlayer.hand.size() && sevens.canPlay(sevens.turnPlayer.hand.get(count))) {//正しい入力判定
				return sevens.turnPlayer.hand.get(count);
			}
		}
	}
	String getName(){
		return name;
	}
}
