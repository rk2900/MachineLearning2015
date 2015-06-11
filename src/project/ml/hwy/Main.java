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
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
	private static final String labelPath = "data"+File.separator+"labeled_weibo",
			dataDir="data"+File.separator+"weibos"+File.separator;
	private static final String[] modelList = Configuration.read("modelList","").split(";");//, "project.ml.rk.DecisionTree"};
	private static final int nFold = Integer.valueOf(Configuration.read("nFold","5"));
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
		String pattern = "^([^,]*),(\\d+),(\\d+),(\\d+),(\\d+),\"([^\"]*)\",([^,]*),([^,]*),(.*)$";
		Pattern r = Pattern.compile(pattern);
		for(File file : files)
		{
			BufferedReader bufReader = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
			String line = new String();
			while((line = bufReader.readLine())!=null)
			{
				Matcher m = r.matcher(line);
				if(m.find())
				{
					Data data = new Data(m.group(1),Integer.parseInt(m.group(2)),Integer.parseInt(m.group(3)),Integer.parseInt(m.group(4)),Integer.parseInt(m.group(5)),m.group(6),m.group(7),m.group(8),m.group(9),file.getName());
					map.put(data.getWeiboId(), data);
				}
			}
			bufReader.close();
		}
		return map;
	}
	private static Map<Data,Label> getLabeledDataList(Map<String,Label> labelMap,Map<String,Data> dataMap)
	{
		Map<Data,Label> dataList = new HashMap<Data,Label>();
		for(Entry<String, Label> e : labelMap.entrySet())
		{
			dataList.put(dataMap.get(e.getKey()),e.getValue());
		}
		return dataList;
	}
	private static void dataGenerate(Map<Data,Label> sourceMap,int i,int nFold,Map<Data,Label> trainList,List<Data> testList)
	{
		double threshold = 0.2;
		int a=0;
		for(Entry<Data,Label> e : sourceMap.entrySet())
		{
			if(nFold==1)
			{
				Random r = new Random();
				double f = r.nextDouble();
				if(f<=threshold)
				{
					testList.add(e.getKey());
				}
				else trainList.put(e.getKey(),e.getValue());
			}
			else if(a%nFold==i)
			{
				testList.add(e.getKey());
			}
			else trainList.put(e.getKey(), e.getValue());
			a++;
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
		Map<Data,Label> labeledDataMap = getLabeledDataList(labelMap,dataMap);
		double[][] scores = new double[modelList.length][2];
		for(int a=0;a<modelList.length;a++)
		{
			String modelName = modelList[a];
			System.out.format("=========%s begins=========\n",modelName);
			double precision = 0.0;
			double recall = 0.0;
			for(int i=0;i<nFold;i++)
			{
				Map<Data,Label> trainList = new HashMap<Data,Label>();
				List<Data> testList = new ArrayList<Data>();
				dataGenerate(labeledDataMap,i,nFold,trainList,testList);
				Model model = (Model) Class.forName(modelName).newInstance();
				List<Result> results = model.run(trainList,testList);
				double[] eScores = evaluate(results,labelMap);
				precision += eScores[0];
				recall += eScores[1];
			}
			precision/=nFold;
			recall/=nFold;
			
			scores[a][0]=precision;
			scores[a][1]=recall;
			System.out.format("=========%s ends=========\n",modelName);
		}
		for(int i=0;i<modelList.length;i++)
		{
			System.out.format("Name:%s Precesion:%.2f Recall:%.2f F1:%.2f\n", modelList[i],scores[i][0],scores[i][1],2*(scores[i][0]*scores[i][1])/(scores[i][0]+scores[i][1]));
		}

	}

}
