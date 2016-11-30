package com.ceair.lucene5.l2_index;

import java.io.IOException;
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
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;


/*
 * 	加权操作可以在索引期间完成，也可以在搜索期间完成
 * 	搜索期间，lucene会自动根据加权情况来加大或减小评分
 * 		加权可加在field上
 */
public class I3_Boost {
	
	private List<Document> createDocuments() throws IOException{
    	List<Document> list = new ArrayList<Document>();
    	
    	Document doc1 = new Document();
    	doc1.add(new StringField("id", "id111", Field.Store.YES));
    	doc1.add(new TextField("content", "good good study", Field.Store.YES));
    	list.add(doc1);
        
    	Document doc2 = new Document();
        doc2.add(new StringField("id", "id222", Field.Store.YES));
        TextField textField = new TextField("content", "good boy", Field.Store.YES);
    	//设置加权因子，默认为1.0
        textField.setBoost(2.0f);
        doc2.add(textField);
    	list.add(doc2);
    	
        return list;
    }
	
	@Test
	public void create() throws IOException {
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
	
    @Test
    public void search() throws Exception{
		  Directory directory = FSDirectory.open(Paths.get("index"));
		  IndexReader reader = DirectoryReader.open(directory);
		  IndexSearcher indexSearcher = new IndexSearcher(reader);
		  Query query = new TermQuery(new Term("content", "good"));
		  System.out.println("查询语句: " + query.toString());
		  TopDocs topDocs = indexSearcher.search(query, 10);
		  System.out.println("命中总数: " + topDocs.totalHits + "\r\n");
		  
		  for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			  System.out.println("得分： " + scoreDoc.score);
			  Document doc = indexSearcher.doc(scoreDoc.doc);
			  System.out.println("id: " + doc.get("id"));
			  System.out.println("content: " + doc.get("content"));
			  System.out.println("------------------------------");
		  }
		  reader.close();
		  directory.close();
    }

}
