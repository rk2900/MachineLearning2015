package project.ml.chzhu;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class MutualInformation extends Model{

	int [] num;
	ArrayList<HashMap<String, Integer>> mapList;
	HashMap<String, Integer> wordMap;
	HashMap<String, Integer> indexMap;
	HashMap<String, Double> MIMap;
	String trainPath;
	String testPath;
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		int n = 3;
		num = new int [n];
		mapList = new ArrayList<HashMap<String,Integer>>();
		for(int i=0; i<n; ++i){
			mapList.add(new HashMap<String, Integer>());
		}
		wordMap = new HashMap<String, Integer>();
		indexMap = new HashMap<String, Integer>();
		MIMap = new HashMap<String, Double>();
		trainPath = "./data/libsvm/MI/train";
		testPath = "./data/libsvm/MI/test";
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		// TODO Auto-generated method stub
		LinkedList<Result> results = new LinkedList<Result>();
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter(testPath));
			StringBuilder sb = new StringBuilder();
			
			for (Data data : dataList) {
				sb.setLength(0);

				String content = data.getContent();
				String [] words = content.split(" ");
				
				HashSet<String> wordSet = new HashSet<String>();
				for (String word : words) {
					wordSet.add(word);
				}
				java.util.Iterator<String> it = wordSet.iterator();
				
				sb.append("1");
				while(it.hasNext()){
					String word = it.next();
					if(!indexMap.containsKey(word)){
						continue;
					}
					sb.append(" ");
					sb.append(indexMap.get(word));
					sb.append(":");
					sb.append(MIMap.get(word));
				}
				sb.append("\n");
				fout.write(sb.toString());
			}
			
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}

	/**
	 * calculate the average MI score for a word, using smoothing
	 * @param word : the word must be included in the wordMap
	 * @param N 
	 * @return
	 */
	private double calculateMI(String word){
		double score = 0;
		int N = 0;
		int A = 0;
		int B = wordMap.get(word);
		
		for(int i=0; i<num.length; ++i){
			N += num[i];
		}
		for(int i=0; i<num.length; ++i){
			A = 0;
			if(mapList.get(i).containsKey(word)){
				A = mapList.get(i).get(word);
			}
			score += Math.log( (double)(A+1) * (N+num.length) / ((num[i]+1) * (B+num.length)) );
		}
		return score/num.length;
	}
	
	@Override
	protected void train(Map<Data, Label> trainList) {
		// TODO Auto-generated method stub
		
		/*
		 * count the num needed to calculate MI
		 */
		for (Data data : trainList.keySet()) {
			Label label = trainList.get(data);
			int type = label.getIsreview() + 1;
			String content = data.getContent();
			String [] words = content.split(" ");
			
			num[type]++;
			
			/*
			 * eliminate the repetition words 
			 */
			HashSet<String> wordSet = new HashSet<String>();
			for (String word : words) {
				wordSet.add(word);
			}
			java.util.Iterator<String> it = wordSet.iterator();
			while(it.hasNext()){
				String word = it.next();
				if(!mapList.get(type).containsKey(word)){
					mapList.get(type).put(word, 1);
				}else{
					mapList.get(type).put(word, mapList.get(type).get(word) + 1);
				}
				if(!wordMap.containsKey(word)){
					wordMap.put(word, 1);
				}else{
					wordMap.put(word, wordMap.get(word) + 1);
				}
			}
		}
		
		int index = 1;
		for (String word : wordMap.keySet()) {
			indexMap.put(word, index);
			++index;
			MIMap.put(word, calculateMI(word));			;
		}
		
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter(trainPath));
			StringBuilder sb = new StringBuilder();
			
			for (Data data : trainList.keySet()) {
				sb.setLength(0);
				
				int type = trainList.get(data).getIsreview();
				sb.append(type);
				
				String content = data.getContent();
				String [] words = content.split(" ");
				
				HashSet<String> wordSet = new HashSet<String>();
				for (String word : words) {
					wordSet.add(word);
				}
				java.util.Iterator<String> it = wordSet.iterator();
				while(it.hasNext()){
					String word = it.next();
					sb.append(" ");
					sb.append(indexMap.get(word));
					sb.append(":");
					sb.append(MIMap.get(word));
				}
				sb.append("\n");
				
				fout.write(sb.toString());
			}
			fout.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		String [] argvScaleTrain = {"-s", "./data/libsvm/MI/scale", trainPath};
//		try {
//			svm_scale.main(argvScaleTrain);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
