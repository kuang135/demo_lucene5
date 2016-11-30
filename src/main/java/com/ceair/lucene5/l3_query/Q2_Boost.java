package com.ceair.lucene5.l3_query;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;


/*
 */
public class Q2_Boost {
	
	 //创建文档
	 private List<Document> createDocuments(){
    	List<Document> list = new ArrayList<Document>();
    	
    	// 构造Document
    	Document doc1 = new Document();
    	doc1.add(new StringField("id", "1a2b3c", Field.Store.YES));
    	doc1.add(new TextField("content", "Students should be allowed to go out with their friends, but not allowed to drink beer.", Field.Store.YES));
    	doc1.add(new StringField("path", "/usr/local/files/doc1.txt", Field.Store.YES));
        
    	list.add(doc1);
        
    	Document doc2 = new Document();
        doc2.add(new StringField("id", "2a2b2c", Field.Store.YES));
    	doc2.add(new TextField("content", "My friend Jerry went to school to see his students but found them drunk which is not allowed.", Field.Store.YES));
    	doc2.add(new StringField("path", "/usr/local/files/doc2.txt", Field.Store.YES));
    	
    	list.add(doc2);
        return list;
    }
    
    //创建索引
    @Test
    public void create() throws Exception{
        Directory directory = FSDirectory.open(Paths.get("index"));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//设置每次都重新创建
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        List<Document> docs=this.createDocuments();
        indexWriter.addDocuments(docs);
        indexWriter.close();
        directory.close();
    }
    
    //索引搜索
    @Test
    public void search() throws Exception{
		  Directory directory = FSDirectory.open(Paths.get("index"));
		  IndexReader reader = DirectoryReader.open(directory);
		  IndexSearcher indexSearcher = new IndexSearcher(reader);
		  Analyzer analyzer = new StandardAnalyzer(); // 定义分词器(标准分词器)
	      QueryParser parser = new QueryParser("content", analyzer);
	      Query query = parser.parse("allowed to drink beer"); 
		  TopDocs topDocs = indexSearcher.search(query, 10);
		  
		  ScoreDoc[] scoreDocs = topDocs.scoreDocs;
		  for (ScoreDoc scoreDoc : scoreDocs) {
			  Document doc = indexSearcher.doc(scoreDoc.doc);
			  System.out.println("path: " + doc.get("path"));
			  //查看评分细节
			  Explanation explain = indexSearcher.explain(query, scoreDoc.doc);
			  System.out.println(explain.toString());
		  }
		  reader.close();
		  directory.close();
    }

}
