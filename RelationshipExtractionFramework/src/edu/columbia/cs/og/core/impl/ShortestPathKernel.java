package edu.columbia.cs.og.core.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;


import opennlp.tools.util.InvalidFormatException;
import edu.berkeley.compbio.jlibsvm.kernel.KernelFunction;
import edu.columbia.cs.cg.candidates.CandidateSentence;
import edu.columbia.cs.og.core.Core;
import edu.columbia.cs.og.core.Kernel;
import edu.columbia.cs.og.features.FeatureGenerator;
import edu.columbia.cs.og.features.impl.EntityBasedChunkingFG;
import edu.columbia.cs.og.features.impl.OpenNLPTokenizationFG;
import edu.columbia.cs.og.features.impl.StanfordNLPDependencyGraphFG;
import edu.columbia.cs.og.structure.OperableStructure;
import edu.columbia.cs.og.structure.impl.TaggedBagOfNgrams;
import edu.columbia.cs.og.structure.impl.TaggedGraph;
import edu.columbia.cs.utils.Pair;
import edu.columbia.cs.utils.SimpleGraphNode;
import edu.columbia.cs.utils.TokenInformation;

public class ShortestPathKernel extends Kernel {
	static final double DEFAULT_GAMMA = 5;
	static final double DEFAULT_LAMBDA = 0.75;
	static int numExec=0;

	// gap penalty
	double m_lambda;
	
	private boolean m_bCache=true;
	protected Map<OperableStructure,Double> m_mapStoK;
	private boolean m_bNorm=true;

	public ShortestPathKernel(double gamma, double lambda)
	{
		m_lambda = lambda;
		m_mapStoK = Collections.synchronizedMap(new HashMap<OperableStructure,Double>());
	}


	public ShortestPathKernel() 
	{
		// Default values.
		m_lambda = DEFAULT_LAMBDA;
		m_mapStoK = Collections.synchronizedMap(new HashMap<OperableStructure,Double>());
	}
	
	
	public double normKernel(TaggedGraph s1, TaggedGraph s2) 
	{
		double k1 = selfKernel(s1);
		double k2 = selfKernel(s2);
		
		double k = kernel(s1, s2);
		if (k == 0)
			return 0;
		
		// normalize
		return k / Math.sqrt (k1 * k2);				
	}
	
	public double selfKernel(TaggedGraph s)
	{
		if (m_bCache) {
			// get string representation of relation instance
			
			//System.out.println(strText);
			//for (int i = 0; i < s.length; i++)
			//	strText += s[i][0] + " ";
			// get cached value
			Double dblk = (Double) m_mapStoK.get(s);
			if (dblk == null) {
				double k = kernel(s, s);
				m_mapStoK.put(s, k);
				return k;
			}
			
			return dblk.doubleValue();
		}

		return kernel(s, s);
	}
	
	public double kernel(TaggedGraph s1, TaggedGraph s2){
		List<Pair<Pair<Integer, Integer>, String>> path1 = s1.getGraph().getShortestPathEdges();
		List<Pair<Pair<Integer, Integer>, String>> path2 = s2.getGraph().getShortestPathEdges();
		
		if(path1.size()!=path2.size() || path1.size()==0 || path1.size()>10){
			return 0;
		}
		
		List<String[]> spath1 = getCorrectSequence(s1, path1);
		List<String[]> spath2 = getCorrectSequence(s2, path2);
		
		int size=spath1.size();
		double result=1;
		for(int i=0;i<size;i++){
			result*=count(spath1.get(i),spath2.get(i));
		}
		
		return result;
	}
	
	public List<String[]> getCorrectSequence(TaggedGraph s, List<Pair<Pair<Integer, Integer>, String>> path){
		List<String[]> sPath = new ArrayList<String[]>();
		SimpleGraphNode<TokenInformation>[] nodes=s.getGraph().getNodes();
		int entity1=-1;
		int entity2=-1;
		
		for(Pair<Pair<Integer, Integer>, String> edge : path){
			Pair<Integer, Integer> p = edge.a();
			
			TokenInformation origin = nodes[p.a()].getLabel();
			if(origin.isEntity1()){
				entity1=p.a();
			}else if(origin.isEntity2()){
				entity2=p.a();
			}
			
			TokenInformation destiny = nodes[p.b()].getLabel();
			if(destiny.isEntity1()){
				entity1=p.b();
			}else if(destiny.isEntity2()){
				entity2=p.b();
			}
		}
		
		int currentToken=entity1;
		sPath.add(createTokenFeatureVector(nodes[currentToken].getLabel()));
		Set<Pair<Pair<Integer, Integer>, String>> processedEdges = new HashSet<Pair<Pair<Integer, Integer>, String>>();
		while(currentToken!=entity2){
			Pair<Integer, Integer> p=null;
			for(Pair<Pair<Integer, Integer>, String> edge : path){
				if(!processedEdges.contains(edge)){
					p = edge.a();
					if(p.a()==currentToken || p.b()==currentToken){
						processedEdges.add(edge);
						break;
					}
				}
			}
			if(p.a()==currentToken){
				sPath.add(createEdgeFeatureVector(true));
				currentToken=p.b();
			}else{
				sPath.add(createEdgeFeatureVector(false));
				currentToken=p.a();
			}
			sPath.add(createTokenFeatureVector(nodes[currentToken].getLabel()));
		}
		
		return sPath;
	}
	
	private String[] createTokenFeatureVector(TokenInformation t){
		List<String> feats=t.getOwnFeatures();
		String[] result=new String[feats.size()];

		result = feats.toArray(result);
		
		return result;
	}
	
	private String[] createEdgeFeatureVector(boolean isPos){
		if(isPos){
			return new String[]{"->"};
		}else{
			return new String[]{"<-"};
		}
	}
	
	public int count(String[] s1, String[] s2){
		int len = s1.length;
		int result=0;
		for(int i=0; i<len; i++){
			if(s1[i].equals(s2[i])){
				result++;
			}
		}
		return result;
	}
	
	
	

	@Override
	public OperableStructure createOperableStructure(CandidateSentence sent) {
		return new TaggedGraph(sent);
	}

	@Override
	protected List<FeatureGenerator> createMandatoryFeatureGenerators() {
		List<FeatureGenerator> fg = new ArrayList<FeatureGenerator>();

		//TODO: the tokenizer should be received in the constructor
		try {
			//The only mandatory feature is the chunker... However, the chunker
			//also depends on the results of the tokenizer. Thus, we need to create a
			//tokenizer and a DependentFeatureGenerator in this case
			fg.add(new StanfordNLPDependencyGraphFG("englishPCFG.ser.gz",new EntityBasedChunkingFG(new OpenNLPTokenizationFG("en-token.bin"))));
		} catch (InvalidFormatException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return fg;
	}

	
	static Map<OperableStructure,Map<OperableStructure,Double>> dynamic = Collections.synchronizedMap(new HashMap<OperableStructure,Map<OperableStructure,Double>>());
	
	@Override
	public double evaluate(OperableStructure s1, OperableStructure s2) {
		//lock.lock();
		//System.out.println("S1= " + normKernel(s1,s1));
		//System.out.println("S2= " + normKernel(s2,s2));
		
		if(s1.hashCode()>s2.hashCode()){
			OperableStructure temp=s2;
			s2=s1;
			s1=temp;
		}
		
		Map<OperableStructure,Double> dyn1 = dynamic.get(s1);
		if(dyn1!=null){
			Double dyn2 = dyn1.get(s2);
			if(dyn2!=null){
				//System.out.println("Reusing");
				return dyn2;
			}
		}
		
		double result;
		if(m_bNorm){
			result=normKernel((TaggedGraph)s1, (TaggedGraph)s2);
		}else{
			result=kernel((TaggedGraph)s1, (TaggedGraph)s2);
		}
		
		if(Double.isNaN(result)){
			System.out.println(result);
		}
		
		dyn1 = dynamic.get(s1);
		if(dyn1==null){
			dyn1=Collections.synchronizedMap(new HashMap<OperableStructure,Double>());
		}
		dyn1.put(s2, result);
		dynamic.put(s1, dyn1);
		
		numExec++;
		if(numExec%10000==0){
			System.out.println(numExec);
		}
		
		return result;
	}

}
