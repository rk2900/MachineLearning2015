package project.ml.chzhu;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import project.ml.chzhu.libsvm.svm_predict;
import project.ml.chzhu.libsvm.svm_train;
import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class MutualInformation extends Model{

	int [] num;
	ArrayList<HashMap<String, Integer>> mapList;
	HashMap<String, Integer> wordMap;
	ArrayList<String> indexList;
	HashMap<String, Double> MIMap;
	ArrayList<HashMap<String, Double>> MIList;
	
	String trainPath;
	String testPath;
	String modelPath;
	String resultPath;
	
	int featureCount;
	
	@Override
	protected void init() {
		// TODO Auto-generated method stub
		int n = 3;
		featureCount = 2000;
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
		
		trainPath = "./data/libsvm/MI/train";
		testPath = "./data/libsvm/MI/test";
		modelPath = "./data/libsvm/MI/model";
		resultPath = "./data/libsvm/MI/result";
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
					if(word.length() == 0){
						continue;
					}
					wordSet.add(word);
				}
				
				sb.append("1");
				for (int k=0; k<indexList.size(); ++k) {
					if(!wordSet.contains(indexList.get(k))){
						continue;
					}
					sb.append(" ");
					sb.append(k+1);
					sb.append(":");
					sb.append(MIMap.get(indexList.get(k)));
				}
				java.util.Iterator<String> it = wordSet.iterator();
				
				sb.append("\n");
				fout.write(sb.toString());
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String [] argvTest = {"-b","1",testPath,modelPath,resultPath};
		try {
			svm_predict.main(argvTest);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(new File(resultPath))));
			String line;
			line = fin.readLine();
			for (Data data : dataList) {
				line = fin.readLine();
				if(line == null){
					System.err.println("null");
					continue;
				}
				String [] res = line.split(" ");
				int type = 1;
				double score;
				if(res[0].startsWith("-")){
					type = -1;
					score = Double.parseDouble(res[2]);
				}else{
					score = Double.parseDouble(res[1]);
				}
				
				Result result = new Result(data.getWeiboId(), this.getClass().getName(), type, score);
				results.add(result);
			}
			fin.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			
			try {
				BufferedWriter fout = new BufferedWriter(new FileWriter("./data/libsvm/MI/sortResult"));
				for(int i=0; i<100; ++i){
					fout.write(list.get(i).getKey()+"\t"+list.get(i).getValue()+"\n");
				}
				fout.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			int N = num[0]+num[2];
			for(int i=0; i<featureCount/2 && i<hashMap.size(); ++i){
				if(!MIMap.containsKey(list.get(i).getKey())){
					double avg = 0;
					for(int j=0; j<3; ++j){
						if(j==1){
							continue;
						}
//						avg += MIList.get(j).get(list.get(i).getKey());
						avg += MIList.get(j).get(list.get(i).getKey()) * num[j]/N;
					}
//					avg /= 2;
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
		
//		int index = 1;
		for (String word : wordMap.keySet()) {
//			indexMap.put(word, index);
//			++index;
//			MIMap.put(word, calculateMI(word));	
			calculateMI(word);
		}
		sort();
		
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter(trainPath));
			StringBuilder sb = new StringBuilder();
			
			for (Data data : trainList.keySet()) {
				sb.setLength(0);
				
				int type = trainList.get(data).getIsreview();
				if(type == 0){
					continue;
				}
				sb.append(type);
				
				String content = data.getContent();
				String [] words = content.split(" ");
				
				HashSet<String> wordSet = new HashSet<String>();
				for (String word : words) {
					if(word.length() == 0){
						continue;
					}
					wordSet.add(word);
				}
				
				for (int k=0; k<indexList.size(); ++k) {
					if(!wordSet.contains(indexList.get(k))){
						continue;
					}
					sb.append(" ");
					sb.append(k+1);
					sb.append(":");
					sb.append(MIMap.get(indexList.get(k)));
				}
//				java.util.Iterator<String> it = wordSet.iterator();
//				while(it.hasNext()){
//					String word = it.next();
//					if(!indexMap.containsKey(word)){
//						continue;
//					}
//					sb.append(" ");
//					sb.append(indexMap.get(word));
//					sb.append(":");
//					sb.append(MIMap.get(word));
//				}
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
		
		String [] argvTrain = {"-t","2","-h","0","-b","1",trainPath, modelPath};
		try {
			svm_train.main(argvTrain);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.err.println("******************************");
	}

}
