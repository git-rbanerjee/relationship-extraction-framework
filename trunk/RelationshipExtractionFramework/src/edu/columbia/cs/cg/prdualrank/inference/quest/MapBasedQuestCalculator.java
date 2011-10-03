package edu.columbia.cs.cg.prdualrank.inference.quest;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.columbia.cs.cg.pattern.Pattern;
import edu.columbia.cs.cg.prdualrank.graph.PRDualRankGraph;
import edu.columbia.cs.cg.prdualrank.inference.convergence.ConvergenceFinder;
import edu.columbia.cs.cg.relations.Relationship;
import edu.columbia.cs.utils.Pair;

public class MapBasedQuestCalculator<T> implements QuestCalculator<T> {
	private Map<Pattern<T>,Pair<Double,Double>> patternTable;
	private Map<Relationship,Pair<Double,Double>> tupleTable;

	private Map<Pattern<T>,Double> patternPrecision = null;
	private Map<Pattern<T>,Double> patternRecall = null;
	private Map<Relationship,Double> tuplePrecision = null;
	private Map<Relationship,Double> tupleRecall = null;
	
	private ConvergenceFinder convergence;
	private Set<Relationship> seeds;

	public MapBasedQuestCalculator(Set<Relationship> seeds, ConvergenceFinder convergence) {
		
		this.convergence = convergence;
		this.seeds = seeds;
		patternTable = new HashMap<Pattern<T>, Pair<Double,Double>>();
		tupleTable = new HashMap<Relationship, Pair<Double,Double>>();

	}

	@Override
	public void runQuestP(PRDualRankGraph<T> gs) {
		convergence.reset();
		
		while(!convergence.converged()){
			
			for (Pattern<T> pattern : gs.getPatterns()) {
				
				double precision = calculatePrecision(pattern,gs);
				setPrecision(pattern,precision,patternTable);
				
			}
			
			for (Relationship tuple: gs.getTuples()){
				
				double precision = calculatePrecision(tuple,gs);
				setPrecision(tuple,precision,tupleTable);
				
			}
			
		}

		
	}

	@Override
	public void runQuestR(PRDualRankGraph<T> gs) {
		convergence.reset();
		
		while(convergence.converged()){
			
			for (Pattern<T> pattern : gs.getPatterns()) {
				
				double recall = calculateRecall(pattern, gs);
				setRecall(pattern,recall,patternTable);
				
			}
			
			for (Relationship tuple: gs.getTuples()){
				
				double recall = calculateRecall(tuple,gs);
				setRecall(tuple,recall,tupleTable);
				
			}
			
		}

		
	}

	private double calculatePrecision(Relationship tuple,PRDualRankGraph<T> gs) {
		
		if (seeds.contains(tuple))
			return getPrecision(tuple);
		
		double precision = 0.0;
		
		for (Pattern<T> pattern : gs.getMatchingPatterns(tuple)) {
			
			precision += getPrecision(pattern)*gs.getMatchingFrequency(tuple, pattern)/gs.getFrequency(tuple);
			
		}
		
		return precision;
		
	}

	private double getPrecision(Pattern<T> pattern) {
		return patternTable.get(pattern).a();
	}

	private <E> void setPrecision(E pattern, double precision, Map<E,Pair<Double,Double>> table) {
		Pair<Double, Double> value = table.get(pattern);
		
		double recall = 0.0;
		
		if (value != null)
			recall = value.b();
			
		table.put(pattern, new Pair<Double,Double>(precision,recall));
		
	}

	private double calculatePrecision(Pattern<T> pattern, PRDualRankGraph<T> gs) {
		
		double precision = 0.0;
		
		for (Relationship tuple : gs.getMatchingTuples(pattern)) {
			
			precision += getPrecision(tuple)*gs.getMatchingFrequency(pattern,tuple)/gs.getFreqency(pattern);
			
		}
		
		return precision;
	}

	private double getPrecision(Relationship tuple) {
		
		if (seeds.contains(tuple)){
			return P0(tuple);
		}
		
		return getPrecision(tuple,tupleTable);
		
	}

	private double P0(Relationship tuple) {
		return 1.0;
	}

	private <E> double getPrecision(E key,
			Map<Relationship, Pair<Double, Double>> table) {
		
		return table.get(key).a();
		
	}

	private double calculateRecall(Relationship tuple,PRDualRankGraph<T> gs) {
		
		double recall = 0.0;
		
		for (Pattern<T> pattern : gs.getMatchingPatterns(tuple)) {
			
			recall += getRecall(pattern,patternTable)*gs.getMatchingFrequency(tuple, pattern)/gs.getFreqency(pattern);
			
		}
		
		return recall;
	}

	private <E> double getRecall(E key, Map<E,Pair<Double,Double>> table) {
		return table.get(key).b();
	}

	private <E> void setRecall(E key, double recall, Map<E,Pair<Double,Double>> table) {
		
		Pair<Double,Double> value = table.get(key);
		
		double precision = 0.0;
		
		if (value != null)	
			precision = value.a();
			
		table.put(key, new Pair<Double,Double>(precision,recall));
		
	}

	private double calculateRecall(Pattern<T> pattern, PRDualRankGraph<T> gs) {
		
		double recall = 0.0;
		
		for (Relationship tuple : gs.getMatchingTuples(pattern)) {

			recall += getRecall(tuple,tupleTable)*gs.getMatchingFrequency(pattern, tuple)/gs.getFrequency(tuple);
			
		}
		
		return recall;
		
	}

	@Override
	public Map<Pattern<T>, Double> getPatternPrecisionMap() {
		
		if (patternPrecision == null){
			generatePatternMaps();
		}
	
		return patternPrecision;
	}

	@Override
	public Map<Relationship, Double> getTuplePrecisionMap() {
		
		if (tuplePrecision == null){
			generateTupleMaps();
		}
		return tuplePrecision;
	}

	private void generateTupleMaps() {
		
		tuplePrecision = new HashMap<Relationship, Double>();
		tupleRecall = new HashMap<Relationship, Double>();
		
		loadMap(tupleTable,tuplePrecision,tupleRecall);
		
	}

	private <E> void loadMap(Map<E, Pair<Double, Double>> table,
			Map<E, Double> precision,
			Map<E, Double> recall) {

		for (Entry<E, Pair<Double, Double>> entry : table.entrySet()) {
			
			precision.put(entry.getKey(), entry.getValue().a());
			recall.put(entry.getKey(), entry.getValue().b());
			
		}

		
	}

	@Override
	public Map<Pattern<T>, Double> getPatternRecallMap() {
		if (patternRecall == null){
			generatePatternMaps();
		}
		return patternRecall;
	}

	private void generatePatternMaps() {
		
		patternPrecision = new HashMap<Pattern<T>, Double>();
		patternRecall = new HashMap<Pattern<T>, Double>();
		
		loadMap(patternTable, patternPrecision, patternRecall);
	}

	@Override
	public Map<Relationship, Double> getTupleRecallMap() {
		
		if (tupleRecall == null){
			generateTupleMaps();
		}
		return tupleRecall;
		
	}
	
}
