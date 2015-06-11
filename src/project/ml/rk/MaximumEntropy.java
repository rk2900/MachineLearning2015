package project.ml.rk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class MaximumEntropy extends Model {
	@Override
	protected void init() {
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		ArrayList<Result> results = new ArrayList<Result>();
		return results;
	}

	@Override
	protected void train(Map<Data, Label> trainList) {
	}

}
