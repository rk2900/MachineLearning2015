package project.ml.chzhu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class NaiveBayes extends Model{

	int [] num;
	int [] unknow;
	int [] wordNum;
	
	ArrayList<HashMap<String, Integer>> mapList;
	HashMap<String, Integer> wordMap;
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		int n = 3;
		num = new int [n];
		unknow = new int [n];
		
		mapList = new ArrayList<HashMap<String,Integer>>();
		for(int i=0; i<n; ++i){
			mapList.add(new HashMap<String, Integer>());
		}
		wordMap = new HashMap<String, Integer>();
		wordNum = new int [n];
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		// TODO Auto-generated method stub
		LinkedList<Result> results = new LinkedList<Result>();
		
		int N = 0;
		for (int i=0; i<3; ++i) {
			N += num[i];
		}
		
		int A = 0;
		for (Data data : dataList) {
			double [] score = new double [3];
			String [] words = data.getContent().split(" ");
			double sum = 0;
			
			for(int i=0; i<3; ++i){
				score[i] = Math.log((double)num[i]/N);
				for (String word : words) {
					if(word.length() == 0){
						continue;
					}
					A = 0;
					if(!wordMap.containsKey(word)){
						A = unknow[i];
					}else{
						if(mapList.get(i).containsKey(word)){
							A = mapList.get(i).get(word);
						}
					}
					score[i] += Math.log((double)(A+1)/(wordNum[i]+wordMap.size()));
				}
				sum += score[i];
			}
			int max = 1;
			if(score[0] > score[1]){
				max = 0;
				if(score[2] > score[0]){
					max = 2;
				}
			}else{
				if(score[2] > score[1]){
					max = 2;
				}
			}
			Result result = new Result(data.getWeiboId(), this.getClass().getName(), max-1, score[max]/sum);
			results.add(result);
		}
		return results;
	}

//	private HashMap<String, Integer> getTopN(int N){
//		HashMap<String, V>
//	}
	
	@Override
	protected void train(Map<Data, Label> trainList) {
		// TODO Auto-generated method stub
		
		HashMap<String, Integer> tmpMap = new HashMap<String, Integer>();
		for (Data data : trainList.keySet()) {
			String contents = data.getContent();
			String [] words = contents.split(" ");
			
			for (String word : words) {
				if(word.length()==0){
					continue;
				}
				if(!tmpMap.containsKey(word)){
					tmpMap.put(word, 1);
				}else{
					tmpMap.put(word, tmpMap.get(word) + 1);
				}
			}
		}
		
		List<Entry<String,Integer>> list = new ArrayList<Map.Entry<String,Integer>>(tmpMap.entrySet());
		Collections.sort(list,new Comparator<Entry<String, Integer>>() {

			public int compare(Entry<String, Integer> o1,
					Entry<String, Integer> o2) {
				// TODO Auto-generated method stub
				return (o2.getValue() - o1.getValue());
			}
			
		});
		
		try {
			int startN = 21;
			int topN = 2500;
			BufferedWriter fout50 = new BufferedWriter(new FileWriter("./data/NaiveBayes/start"+startN));
			for(int i=startN; i<topN && i<list.size(); ++i){
				fout50.write(list.get(i).getKey()+"\t"+list.get(i).getValue());
				fout50.write("\n");
				
				wordMap.put(list.get(i).getKey(), list.get(i).getValue());
			}
			fout50.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (Data data : trainList.keySet()) {
			Label label = trainList.get(data);
			int type = label.getIsreview() + 1;
			
			num[type]++;
			
			String contents = data.getContent();
			String [] words = contents.split(" ");
			
			for (String word : words) {
				if(word.length() == 0){
					continue;
				}
				wordNum[type]++;
				if(wordMap.containsKey(word)){
					if(!mapList.get(type).containsKey(word)){
						mapList.get(type).put(word, 1);
					}else{
						mapList.get(type).put(word, mapList.get(type).get(word) + 1);
					}
				}else{
					unknow[type]++;
				}
			}
		}
		
	}

}
