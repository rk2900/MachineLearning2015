package project.ml.rk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import project.ml.hwy.*;

public class Baseline extends Model {

	@Override
	protected void init() {
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		ArrayList<Result> results = new ArrayList<Result>();
		for (Data data : dataList) {
			Result result = new Result(data.getWeiboId(), "Baseline", -1, 0.0);
			results.add(result);
		}
		return results;
	}

	@Override
	protected void train(Map<Data, Label> trainList) {
	}

}
