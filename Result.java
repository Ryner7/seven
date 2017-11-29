import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * Created by ryoto on 2017/10/17.
 */
public class Result {
	History history;
	ArrayList<Integer> scores = new ArrayList<Integer>();
	ArrayList<Integer> playerOrder = new ArrayList<Integer>();
	
	static class EstimationAccuracy {
		static int SIZE = 60;
		static private ArrayList<Double> accuracyList;
		static private ArrayList<Double> randomList;
		static private ArrayList<Integer> estimationTimes;
		
		static void init() {
			accuracyList = new ArrayList<>();
			randomList = new ArrayList<>();
			estimationTimes = new ArrayList<>();
			
			for (int index = 0 ; index < SIZE ; index++) {
				accuracyList.add(0.0);
				randomList.add(0.0);
				estimationTimes.add(0);
			}
		}
		
		static void addData(int secretNum, double correctNum, double randomNum) {
			accuracyList.set(secretNum, accuracyList.get(secretNum) + correctNum);
			randomList.set(secretNum, randomList.get(secretNum) + randomNum);
			estimationTimes.set(secretNum, estimationTimes.get(secretNum) + 1);
		}
		
		static void outputData() {
			
			try {
				Calendar cal = Calendar.getInstance();
				String fileName = (System.getProperty("os.name").toLowerCase().startsWith("mac") ? "./csv/" : "../csv")
						+ String.format("%02d", cal.get(Calendar.MONTH)) + "_"
						+ String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "_"
						+ String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + "_"
						+ String.format("%02d", cal.get(Calendar.MINUTE)) + "_"
						+ String.format("%02d", cal.get(Calendar.SECOND)) + ".csv";
				FileWriter fw = new FileWriter(fileName, true);
				fw.write("uppVal= "+Test.uppVal+"\n");
				fw.write("secretNum,accuracy,random,time\n");
				int time;
				for (int secretNum = 0 ; secretNum < SIZE ; secretNum++) {
					time = estimationTimes.get(secretNum);
					fw.write(""+secretNum);
					if (0 < time) {
						fw.write("," + 1.0*accuracyList.get(secretNum) / time);
						fw.write("," + randomList.get(secretNum) / time);
						fw.write("," + time + "\n");
					} else {
						fw.write(",0,0,0\n");
					}
				}
				fw.close();
			} catch (Exception e) {
				System.out.println("Error at outputData");
			}
		}
	}
}
