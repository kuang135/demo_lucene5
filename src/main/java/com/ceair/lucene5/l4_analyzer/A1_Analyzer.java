package com.ceair.lucene5.l4_analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.Test;

public class A1_Analyzer {
	String text = "我28了，我是JAVA开发。I am a programmer, He likes the programming2.";
	
	private void analyzer(Analyzer analyzer, String text) throws IOException {
		TokenStream tokenStream = analyzer.tokenStream("fieldName", new StringReader(text));
		CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
		tokenStream.reset();
		while(tokenStream.incrementToken()) {
			System.out.println(termAtt.toString());
		}
		tokenStream.end();
		tokenStream.close();
	}
	
	/*
	 * 	空格分析：按空格分割
	 */
	@Test
	public void whitespaceAnalyzer() throws IOException {
		analyzer(new WhitespaceAnalyzer(), text);
	}
	
	/*
	 * 	简单分词：非字母符，空格分割，小写
	 */
	@Test
	public void simpleAnalyzer() throws IOException {
		analyzer(new SimpleAnalyzer(), text);
	}
	
	/*
	 * 	停词分析：非字母符，空格分割，小写，去除常用单词，如the，a
	 */
	@Test
	public void stopAnalyzer() throws IOException {
		analyzer(new StopAnalyzer(), text);
	}
	
	/*
	 * 	标准分析：非字母符，小写，去除常用单词，如the，a
	 * 		中文按字分割，英文按空格分隔
	 */
	@Test
	public void standardAnalyzer() throws IOException {
		analyzer(new StandardAnalyzer(), text);
	}


}
