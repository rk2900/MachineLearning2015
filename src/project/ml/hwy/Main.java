package project.ml.hwy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {
	private static final String labelPath = "data\\labeled_weibo",
			dataDir="data\\weibos\\";
	private static final String[] modelList = {};
	private static final int nFold = 5;
	private static Map<String,Label> getLabel() throws IOException
	{
		BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(labelPath)),"UTF-8"));
		String line = new String();
		Map<String,Label> map = new HashMap<String,Label>();
		while((line = bufReader.readLine())!=null)
		{
			String[] strings = line.split(",");
			Label label = new Label(strings[0],strings[1],Integer.parseInt(strings[2]),Integer.parseInt(strings[3]));
			map.put(strings[1], label);
		}
		bufReader.close();
		return map;
	}
	private static Map<String,Data> getData() throws IOException
	{
		File dir = new File(dataDir);
		File[] files = dir.listFiles();
		Map<String,Data> map = new HashMap<String,Data>();
		for(File file : files)
		{
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
			String line = new String();
			while((line = bufReader.readLine())!=null)
			{
				String[] strings = line.split(",");
				StringBuffer sb = new StringBuffer();
				for(int i=8;i<strings.length;i++)
				{
					sb.append(strings[i]);
				}
				Data data = new Data(strings[0],Integer.parseInt(strings[1]),Integer.parseInt(strings[2]),Integer.parseInt(strings[3]),Integer.parseInt(strings[4]),strings[5],strings[6],strings[7],sb.toString());
				map.put(strings[0], data);
			}
			bufReader.close();
		}
		return map;
	}
	private static List<Data> getLabeledDataList(Set<String> labelSet,Map<String,Data> dataMap)
	{
		List<Data> dataList = new ArrayList<Data>();
		for(String id : labelSet)
		{
			dataList.add(dataMap.get(id));
		}
		return dataList;
	}
	private static void dataGenerate(List<Data> sourceList,int i,int nFold,List<Data> trainList,List<Data> testList)
	{
		for(int a=0;a<sourceList.size();a++)
		{
			if(a%nFold==i)
			{
				testList.add(sourceList.get(a));
			}
			else trainList.add(sourceList.get(a));
		}
	}
	private static double[] evaluate(List<Result> results,Map<String,Label> labelMap)
	{
		double[] evaluateResult = {0.0,0.0};//0 precision 1 recall
		double truePositive=0.0,
				falsePositive=0.0,
				falseNegative=0.0;
		for(Result result : results)
		{
			String id = result.getId();
			int type = result.getType();
			Label label = labelMap.get(id);
			if(type == 1)
			{
				  if(label.getIsreview()==1) truePositive++;
				  else falsePositive++;
			}
			else
			{
				if(label.getIsreview()==1) falseNegative++;
			}
		}
		evaluateResult[0] = truePositive/(truePositive+falsePositive);
		evaluateResult[1] = truePositive/(truePositive+falseNegative);
		return evaluateResult;
	}
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
		Map<String,Label> labelMap= getLabel();
		Map<String,Data> dataMap = getData();
		List<Data> labeledDataList = getLabeledDataList(labelMap.keySet(),dataMap);
		for(String modelName : modelList)
		{
			double precision = 0.0;
			double recall = 0.0;
			for(int i=0;i<nFold;i++)
			{
				List<Data> trainList = new ArrayList<Data>();
				List<Data> testList = new ArrayList<Data>();
				dataGenerate(labeledDataList,i,nFold,trainList,testList);
				Model model = (Model) Class.forName(modelName).newInstance();
				List<Result> results = model.run(trainList,testList);
				double[] scores = evaluate(results,labelMap);
				precision += scores[0];
				recall += scores[1];
			}
			precision/=nFold;
			recall/=nFold;
			System.out.format("Name:%s Precesion:%.2f Recall:%.2f", modelName,precision,recall);
		}

	}

}
