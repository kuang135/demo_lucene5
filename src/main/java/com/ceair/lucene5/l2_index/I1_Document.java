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
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class I1_Document {
	
	
	//创建文档
	private List<Document> createDocuments(){
    	List<Document> list = new ArrayList<Document>();
    	
    	// 构造Document
    	Document doc1 = new Document();
    	doc1.add(new StringField("id", "id111", Field.Store.YES));
    	doc1.add(new TextField("content", "good good study", Field.Store.YES));
        
    	list.add(doc1);
        
    	Document doc2 = new Document();
        doc2.add(new StringField("id", "id222", Field.Store.YES));
    	doc2.add(new TextField("content", "day day study", Field.Store.YES));
    	
    	list.add(doc2);
        return list;
    }
	
	//创建索引，并通过writer获取文档的一些信息
	@Test
	public void create() throws IOException {
		 //索引位置
        Directory directory = FSDirectory.open(Paths.get("index"));
        //定义分析器(标准分析器)
        Analyzer analyzer = new StandardAnalyzer();
        //定义索引配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//设置每次都重新创建
        
        //定义索引对象
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        //创建索引
        List<Document> docs=this.createDocuments();
        //将文档写入索引库
        indexWriter.addDocuments(docs);
        //通过indexWriter获取文档的一些信息
        System.out.println("hasDeleteions: " + indexWriter.hasDeletions());//是否有被标记为删除的文档
        System.out.println("maxDoc: " + indexWriter.maxDoc());//索引中被删除和未被删除的文档总数
        System.out.println("numDocs: " + indexWriter.numDocs());//索引中未被删除的文档数
        //关闭索引
        indexWriter.close();
        directory.close();
	}
	
	//通过reader可以有效的获取到文档的信息
	@Test
    public void getDocInfoByReader() throws Exception {
        Directory directory = FSDirectory.open(Paths.get("index"));
        IndexReader reader = DirectoryReader.open(directory);
        System.out.println("good in docFreq: "+reader.docFreq(new Term("content", "good")));
        System.out.println("study in docFreq: "+reader.docFreq(new Term("content", "study")));
        System.out.println("maxDoc: " + reader.maxDoc());
        System.out.println("numDocs: "+reader.numDocs());
        System.out.println("deleteDocs: "+reader.numDeletedDocs());
        reader.close();
        directory.close();
    }
	
	//搜索全部
    @Test
    public void searchAll() throws Exception{
		  //定义索引位置
		  Directory directory = FSDirectory.open(Paths.get("index"));
		  //创建IndexReader
		  IndexReader reader = DirectoryReader.open(directory);
		  //构造索引搜索器
		  IndexSearcher indexSearcher = new IndexSearcher(reader);
		  //查询
		  Query query = new MatchAllDocsQuery();
		  System.out.println("查询语句: " + query.toString());
		  TopDocs topDocs = indexSearcher.search(query, 10);
		  System.out.println("命中总数: " + topDocs.totalHits + "\r\n");
		  
		  for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			   Document doc = indexSearcher.doc(scoreDoc.doc);
			   System.out.println("id: " + doc.get("id"));
			   System.out.println("content: " + doc.get("content"));
			   System.out.println("------------------------------");
		  }
		  reader.close();
		  directory.close();
    }
	
	//删除索引，删除操作之后，还在缓存中，可以恢复，lucene会通过周期性刷新目录来执行删除操作
    //删除后，打开一个新的IndexReader才会被感知
    @Test
    public void delete() throws Exception{
    	//删除前查询
    	Directory directory = FSDirectory.open(Paths.get("index"));
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher indexSearcher = new IndexSearcher(reader);
		Query query = new MatchAllDocsQuery();
		TopDocs topDocs = indexSearcher.search(query, 10);
		System.out.println("命中总数: " + topDocs.totalHits + "\r\n");
    	
		//删除
    	Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
        writer.deleteDocuments(new Term("id", "id111"));//用于删除的field最好能保持唯一性
        writer.close();
        
        //用删除前的reader查询，数据未改变
		TopDocs topDocs2 = indexSearcher.search(query, 10);
		System.out.println("命中总数: " + topDocs2.totalHits + "\r\n");
		
		//关闭重新打开reader后，刷新缓冲区，数据改变
		reader.close();
		reader = DirectoryReader.open(directory);
		indexSearcher = new IndexSearcher(reader);
		TopDocs topDocs3 = indexSearcher.search(query, 10);
		System.out.println("命中总数: " + topDocs3.totalHits + "\r\n");
    }
    
    //实际是 先删除， 再添加
    //根据id更新，id最好能唯一标识一部文档，且 id 为 StringField
    @Test
    public void update() throws Exception {
    	Directory directory = FSDirectory.open(Paths.get("index"));
    	Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(directory, indexWriterConfig);
       
        Document doc = new Document();
        doc.add(new StringField("id", "id222", Field.Store.YES));
        doc.add(new TextField("content", "document has changed", Field.Store.YES));
        
        writer.updateDocument(new Term("id", "id222"), doc);
        writer.close();
        //更新后，查看索引中的文档信息
        getDocInfoByReader();
        //查询全部
        searchAll();
    }
    
}
