package com.ceair.lucene5.l3_query;

import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class Q7_MultiPhraseQuery {

	   //初始化数据
	   @Test
	   public void create() throws Exception{
	       	Directory directory = FSDirectory.open(Paths.get("index"));
	       	Analyzer analyzer = new StandardAnalyzer();
	       	IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
	       	indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//设置每次都重新创建
	       	IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
	   		
	       	Document doc1 = new Document();
	   		doc1.add(new TextField("content", "the quick brown fox jumped over the lazy dog.", Field.Store.YES));
	   		indexWriter.addDocument(doc1);
	   		Document doc2 = new Document();
	   		doc2.add(new TextField("content", "the fast fox hopped over the hound.", Field.Store.YES));
	   		indexWriter.addDocument(doc2);
	   		
	   		indexWriter.close();
	   		directory.close();
	   }
	   
	   /*
	    * MultiPhraseQuery实现短语范围内的同义词查询
	    */
	   @Test
	   public void multiPhraseQuery() throws Exception {
		    Directory directory = FSDirectory.open(Paths.get("index"));
		    DirectoryReader reader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(reader);

	        MultiPhraseQuery query = new MultiPhraseQuery();
	        query.add(new Term[] {
	        		new Term("content", "quick"),
	        		new Term("content", "fast")
	        	});
	        query.add(new Term("content", "fox"));
	        query.setSlop(1);
	        
	        TopDocs topDocs = indexSearcher.search(query, 10);
	        System.out.println("查询语句：" + query.toString());
	        System.out.println("总命中数：" + topDocs.totalHits + "\r\n");
	        
	        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        	System.out.println("评分：" + scoreDoc.score);
	            Document doc = indexSearcher.doc(scoreDoc.doc);
	            System.out.println("content：" + doc.get("content"));
	        }
	        reader.close();
	        directory.close();
	    }
	   
	   /*
	    * 用PhraseQuery实现相同的效果
	    */
	   @Test
	   public void phraseQuery() throws Exception {
		    Directory directory = FSDirectory.open(Paths.get("index"));
		    DirectoryReader reader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(reader);

	        Query query1 = new PhraseQuery.Builder()
		    	.setSlop(1)
		    	.add(new Term("content", "quick"))
		    	.add(new Term("content", "fox"))
		    	.build();
	    	
	        Query query2 = new PhraseQuery.Builder()
		    	.setSlop(0)
		    	.add(new Term("content", "fast"))
		    	.add(new Term("content", "fox"))
		    	.build();
	    	
	        BooleanQuery query = new BooleanQuery.Builder()
	    		.add(query1, Occur.SHOULD)
	    		.add(query2, Occur.SHOULD)
	    		.build();
	    	
	        TopDocs topDocs = indexSearcher.search(query, 10);
	        System.out.println("查询语句：" + query.toString());
	        System.out.println("总命中数：" + topDocs.totalHits + "\r\n");
	        
	        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        	System.out.println("评分：" + scoreDoc.score);
	            Document doc = indexSearcher.doc(scoreDoc.doc);
	            System.out.println("content：" + doc.get("content"));
	        }
	        reader.close();
	        directory.close();
	    }
	   
	
}
