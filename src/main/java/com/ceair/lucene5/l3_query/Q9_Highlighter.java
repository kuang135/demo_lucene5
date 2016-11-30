package com.ceair.lucene5.l3_query;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.junit.Test;

public class Q9_Highlighter {
	
	@Test
	public void test1() throws IOException, InvalidTokenOffsetsException {
		String text = "The quick brown fox jumps over the lazy dog.";
		TokenStream tokenStream = new SimpleAnalyzer().tokenStream("field", new StringReader(text));
		
		// 构建Scorer,用于选取最佳切片
		TermQuery query = new TermQuery(new Term("filed", "fox"));
		QueryScorer queryScorer = new QueryScorer(query, "filed");

		// 构建Fragmenter对象，用于文档切片
		SimpleSpanFragmenter fragmenter = new SimpleSpanFragmenter(queryScorer);
		// 定义高亮组件
		Highlighter highlighter = new Highlighter(queryScorer);
		highlighter.setTextFragmenter(fragmenter);
		
		String bestFragment = highlighter.getBestFragment(tokenStream, text);
		System.out.println(bestFragment);
	}

}
