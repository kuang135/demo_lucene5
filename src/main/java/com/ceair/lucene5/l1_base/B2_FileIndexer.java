package com.ceair.lucene5.l1_base;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

/*
	Document -- 要索引的文档
	Field -- 文档的类型，比如标题，作者，内容，路径等
		存储：是否在返回的结果中显示该字段
		分词：是否用该字段的部分内容进行搜索，比如商品的ID不需要分词
 */
public class B2_FileIndexer {
	
	
    //创建索引
    @Test
    public void create() throws Exception{
        //索引位置
        Directory directory = FSDirectory.open(Paths.get("index"));
        //定义分析器(标准分析器)
        Analyzer analyzer = new StandardAnalyzer();
        //定义索引配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//设置每次都重新创建
        
        //定义索引对象
        final IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        //创建索引
        Files.walkFileTree(Paths.get("data"), new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				long lastModified = Files.getLastModifiedTime(file).toMillis();
				try (InputStream stream = Files.newInputStream(file)) {
					Document doc = new Document();
					//路径
					doc.add(new StringField("path", file.toString(), Field.Store.YES));
					//最后修改时间
					doc.add(new LongField("modified", lastModified, Field.Store.YES));
					//内容，不存储
					doc.add(new TextField("content", new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))));
					indexWriter.addDocument(doc);
				}
				return FileVisitResult.CONTINUE;
			}
        });
        //关闭索引
        indexWriter.close();
        directory.close();
    }
    
    //索引搜索
    @SuppressWarnings("deprecation")
	@Test
    public void search() throws Exception{
		  //定义索引位置
		  Directory directory = FSDirectory.open(Paths.get("index"));
		  //构造索引搜索器
		  IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
		  //构造查询对象
		  Analyzer analyzer = new StandardAnalyzer(); // 定义分词器(标准分词器)
		  //创建IndexReader
	      QueryParser parser = new QueryParser("content", analyzer);
	      //// 构造查询对象，分词查询
	      Query query = parser.parse("allowed to drink beer"); 
		  //查询
		  TopDocs topDocs = indexSearcher.search(query, 10);
		  System.out.println("查询数据总数：" + topDocs.totalHits + "\r\n");
		  
		  for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
		       System.out.println("得分：" + scoreDoc.score);
			   Document doc = indexSearcher.doc(scoreDoc.doc);
			   System.out.println("path: " + doc.get("path"));
			   System.out.println("modified: " + new Date(Long.valueOf(doc.get("modified"))).toLocaleString());
			   System.out.println("content: " + doc.get("content"));//内容没有存储，所以不会显示
			   System.out.println("------------------------------");
		  }
		  indexSearcher.getIndexReader().close();
		  directory.close();
    }

    
}
