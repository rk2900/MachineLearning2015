package project.ml.chzhu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class MINaiveBayes extends Model{

	int [] num;
	ArrayList<HashMap<String, Integer>> mapList;
	HashMap<String, Integer> wordMap;
	ArrayList<String> indexList;
	HashMap<String, Double> MIMap;
	ArrayList<HashMap<String, Double>> MIList;
	
	int featureCount;
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		int n = 3;
		featureCount = 1500;
		num = new int [n];
		mapList = new ArrayList<HashMap<String,Integer>>();
		MIList = new ArrayList<HashMap<String,Double>>();
		for(int i=0; i<n; ++i){
			mapList.add(new HashMap<String, Integer>());
			MIList.add(new HashMap<String, Double>());
		}
		wordMap = new HashMap<String, Integer>();
		MIMap = new HashMap<String, Double>();
		indexList = new ArrayList<String>();
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		// TODO Auto-generated method stub
		LinkedList<Result> results = new LinkedList<Result>();
		int type;
		
		int N = num[0] + num[2];
		for (Data data : dataList) {
			String contents = data.getContent();
			String [] words = contents.split(" ");
			
			HashSet<String> set = new HashSet<String>();
			for (String word : words) {
				if(word.length() > 0){
					set.add(word);
				}
			}
			
			double [] scores = new double[3];
			for(int i=0; i<3; ++i){
				if(i==1){
					continue;
				}
				for (String word : indexList) {
					if(set.contains(word)){
						scores[i] += MIList.get(i).get(word);
					}
				}
				scores[i] *= (double)num[i]/N;
			}
			
			if(scores[0] > scores[2]){
				type = -1;
			}else{
				type = 1;
			}
			
			Result result = new Result(data.getWeiboId(), this.getClass().getName(), type, scores[type+1]);
			results.add(result);
		}
		return results;
	}

	/**
	 * calculate the average MI score for a word, using smoothing
	 * @param word : the word must be included in the wordMap
	 */
	private void calculateMI(String word){

		int N = 0;
		int A = 0;
		int B = wordMap.get(word);
		
		for(int i=0; i<num.length; ++i){
			if(i == 1){
				continue;
			}
			N += num[i];
		}
		
		for(int i=0; i<num.length; ++i){
			if(i==1){
				continue;
			}
			A = 0;
			if(mapList.get(i).containsKey(word)){
				A = mapList.get(i).get(word);
			}
			double tmp = Math.log( (double)(A+1) * (N+num.length) / ((num[i]+1) * (B+num.length)));
			MIList.get(i).put(word, tmp);
		}
	}
	
	private void sort(){
		for (int k=0; k<MIList.size(); ++k) {
			if(k==1){
				continue;
			}
			HashMap<String, Double> hashMap = MIList.get(k);
			List<Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>(hashMap.entrySet());
			Collections.sort(list,new Comparator<Entry<String, Double>>() {

				public int compare(Entry<String, Double> o1,
						Entry<String, Double> o2) {
					// TODO Auto-generated method stub
					if(o2.getValue() > o1.getValue()){
						return 1;
					}
					if(o2.getValue() < o1.getValue()){
						return -1;
					}
					return 0;
				}
			});
			
			int N = num[0]+num[2];
			for(int i=0; i<featureCount/2 && i<hashMap.size(); ++i){
				if(!MIMap.containsKey(list.get(i).getKey())){
					double avg = 0;
					for(int j=0; j<3; ++j){
						if(j==1){
							continue;
						}
						avg += MIList.get(j).get(list.get(i).getKey()) * num[j]/N;
					}
					MIMap.put(list.get(i).getKey(), avg);
					indexList.add(list.get(i).getKey());
				}
			}
		}
		
		
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
			
			if(type == 1){
				continue;
			}
			
			String content = data.getContent();
			String [] words = content.split(" ");
			
			num[type]++;
			
			/*
			 * eliminate the repetition words 
			 */
			HashSet<String> wordSet = new HashSet<String>();
			for (String word : words) {
				if(word.length() == 0){
					continue;
				}
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
		
		for (String word : wordMap.keySet()) {
			calculateMI(word);
		}
		sort();
		
//		System.err.println("******************************");
//		System.out.println(MIMap.size());
//		System.out.println(indexList.size());
	}

}
