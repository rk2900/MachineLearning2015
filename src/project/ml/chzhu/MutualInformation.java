package project.ml.chzhu;

import java.util.ArrayList;
import java.util.HashMap;
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
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		// TODO Auto-generated method stub
		LinkedList<Result> results = new LinkedList<Result>();
		
		for (Data data : dataList) {
			double [] scores = new double [3];
			
			
			results.add(new Result(data.getWeiboId(), this.getClass().getName(), -1, 1));
			System.out.println(this.getClass().getName());
			break;
		}
		
		return results;
	}

	@Override
	protected void train(Map<Data, Label> trainList) {
		// TODO Auto-generated method stub
		for (Data data : trainList.keySet()) {
			Label label = trainList.get(data);
			int type = label.getIsreview() + 1;
			String content = data.getContent();
			String [] words = content.split(" ");
			
			num[type]++;
			for (String word : words) {
				if(!mapList.get(type).containsKey(word)){
					mapList.get(type).put(word, 0);
				}else{
					mapList.get(type).put(word, mapList.get(type).get(word) + 1);
				}
				
				if(!wordMap.containsKey(word)){
					wordMap.put(word, 0);
				}else{
					wordMap.put(word, wordMap.get(word) + 1);
				}
			}
		}
	}

}
