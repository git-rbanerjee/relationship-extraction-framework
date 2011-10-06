package edu.columbia.cs.cg.prdualrank.searchengine.querygenerator.impl;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import edu.columbia.cs.cg.document.Document;
import edu.columbia.cs.cg.document.TokenizedDocument;
import edu.columbia.cs.cg.pattern.Pattern;
import edu.columbia.cs.cg.pattern.prdualrank.SearchPattern;
import edu.columbia.cs.cg.prdualrank.index.Index;
import edu.columbia.cs.cg.prdualrank.index.analyzer.TokenBasedAnalyzer;
import edu.columbia.cs.cg.prdualrank.index.analyzer.TokenizerBasedAnalyzer;
import edu.columbia.cs.cg.prdualrank.index.reader.TokenBasedReader;
import edu.columbia.cs.cg.prdualrank.searchengine.querygenerator.QueryGenerator;
import edu.columbia.cs.cg.relations.Entity;
import edu.columbia.cs.cg.relations.Relationship;
import edu.columbia.cs.utils.Span;

public class LuceneQueryGenerator extends QueryGenerator<Query> {

	private QueryParser qp;
	private TokenBasedAnalyzer tbAnalyzer;

	public LuceneQueryGenerator(TokenBasedAnalyzer tbAnalyzer){
		this.tbAnalyzer = tbAnalyzer;
		qp = new QueryParser(Version.LUCENE_34, Index.CONTENT, tbAnalyzer);
	}
	
	
	@Override
	public Query generateQuery(Relationship relationship) {
		// TODO Implement before trying over local collection
		return null;
	}

	@Override
	public Query generateQuery(Entity role) {
		// TODO Implement before trying over local collection
		return null;
	}

	@Override
	public Query generateQuery(
			SearchPattern<Document, TokenizedDocument> pattern) {
		
		List<String[]> nGrams = pattern.getNGrams();
		
		BooleanQuery bq = new BooleanQuery();
		
		for (String[] strings : nGrams) {
			
			Query nGramQuery = generateNGramQuery(strings);
			
			bq.add(nGramQuery, Occur.MUST);
			
		}
		
		return bq;
	}

	private Query generateNGramQuery(String[] query) {

		Span[] spans = new Span[query.length];
		
		int offset = 0;
		
		for (int i = 0; i < query.length; i++) {
			spans[i] = new Span(offset,query[i].length());
			offset = offset + query[i].length();
		}
		
		TokenStream s = tbAnalyzer.tokenStream(Index.CONTENT, new TokenBasedReader(spans, query));
		
		try {
			PhraseQuery pq = new PhraseQuery();
			
			while(s.incrementToken()){
				pq.add(new Term(Index.CONTENT, s.getAttribute(CharTermAttribute.class).toString()));
			}
			
			return pq;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}



}