package project.ml.chzhu;

import java.util.LinkedList;
import java.util.List;

import project.ml.hwy.Data;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class SVM extends Model {

	@Override
	protected void init() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		// TODO Auto-generated method stub
		LinkedList<Result> results = new LinkedList<Result>();
		return results;
	}

	@Override
	protected void train(List<Data> dataList) {
		// TODO Auto-generated method stub
		System.out.println(dataList.size());
//		System.out.println(dataList.get(1).getPic());
//		System.out.println(dataList.get(1).getPoi());
		System.out.println(dataList.get(0).getContent());
	}

}
