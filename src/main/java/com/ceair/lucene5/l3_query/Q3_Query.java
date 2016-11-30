package com.ceair.lucene5.l3_query;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanQuery.Builder;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class Q3_Query {

	   //初始化数据
	   @Test
	   public void create() throws Exception{
	       Directory directory = FSDirectory.open(Paths.get("index"));
	       Analyzer analyzer = new StandardAnalyzer();
	       IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
	       indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//设置每次都重新创建
	       IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
	       List<Document> docs = new ArrayList<Document>();
		   	for (int i = 0; i < 100; i++) {
		   		Document doc = new Document();
		   		doc.add(new LongField("id", i, Field.Store.YES));
		   		doc.add(new StringField("title", "apple phone " + i, Field.Store.YES));
		   		doc.add(new TextField("sellPoint", "苹果手机" + i +" is very good, phone we all like apple.", Field.Store.YES));
		   		doc.add(new FloatField("price", i + 0.01f, Field.Store.YES));
		   		docs.add(doc);
		   	}
	       indexWriter.addDocuments(docs);
	       indexWriter.close();
	       directory.close();
	   }
	   
	   private void searchByQuery(Query query) throws Exception {
		    Directory directory = FSDirectory.open(Paths.get("index"));
		    DirectoryReader reader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(reader);

	        TopDocs topDocs = indexSearcher.search(query, 10);
	        System.out.println("查询语句：" + query.toString());
	        System.out.println("总命中数：" + topDocs.totalHits + "\r\n");
	        
	        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        	System.out.println("评分：" + scoreDoc.score);
	            Document doc = indexSearcher.doc(scoreDoc.doc);
	            System.out.println("商品ID：" + doc.get("id"));
	            System.out.println("商品标题：" + doc.get("title"));
	            System.out.println("商品卖点：" + doc.get("sellPoint"));
	            System.out.println("商品价格：" + doc.get("price") + "\r\n");
	        }
	        reader.close();
	        directory.close();
	    }
	   
	//词查询，词是最小的单元，区分大小写
	@Test
	public void termQuery() throws Exception {
		// sellPoint:apple
	    Query query = new TermQuery(new Term("sellPoint", "apple"));
	    searchByQuery(query);//sellPoint中包含apple的文档
	    // sellPoint:very good
	    Query query2 = new TermQuery(new Term("sellPoint", "very good"));
	    searchByQuery(query2);//查不到very good，因为被StandardAnalyzer分为两个词了
	}
	
	//范围查询，字典顺序排序
	@Test
	public void termRangeQuery() throws Exception {
		// title:[a TO b]
	    Query query = TermRangeQuery.newStringRange("title", "a", "b", true ,true);
	    searchByQuery(query);//sellPoint中包含apple的文档
	}
	
	
	//数字范围查询
	// 设置查询字段、最小值、最大值、最小值是否包含边界，最大值是否包含边界
    @Test
    public void numericRangeQuery() throws Exception {
    	// price:{1.01 TO 5.01]
        Query query = NumericRangeQuery.newFloatRange("price", 1.01f, 5.01f, false, true);
        searchByQuery(query);
    }
    
    //前缀搜索，包含以指定字符串开头的词的文档
    @Test
    public void prefixQuery() throws Exception {
    	// title:apple phone 1*
        Query query = new PrefixQuery(new Term("title", "apple phone 1"));
        //new Term("sellPoint", "apple1") 将查不到，因为apple1被分词为apple和1
        searchByQuery(query);
    }
    
    //组合查询，默认情况下包含1024个查询子句
    @Test
    public void booleanQuery() throws Exception {
    	// +price:{13.01 TO 15.01] title:apple phone 1*
    	Builder builder = new BooleanQuery.Builder();
    	builder.add(NumericRangeQuery.newFloatRange("price", 13.01f, 15.01f, false, true), Occur.MUST);
    	builder.add(new PrefixQuery(new Term("title", "apple phone 1")), Occur.SHOULD);
    	BooleanQuery query = builder.build();
    	searchByQuery(query);
    }
    
    //短语搜索，可用于完全匹配
    //两个词的位置之间所允许的最大间隔距离称为slop，默认设为0
    //苹果手机11被分词为：苹 果 手 机 11，slop设为0，完全匹配
    //评分计算公司为：1 / (distance + 1)
    @Test
    public void phraseQuery() throws Exception {
    	// sellPoint:"苹 果 ? ? 11"
    	PhraseQuery.Builder builder = new PhraseQuery.Builder();
    	builder.setSlop(0);
    	builder.add(new Term("sellPoint", "苹"));
    	builder.add(new Term("sellPoint", "果"));//默认在前面的基础上+1
    	builder.add(new Term("sellPoint", "11"), 4);
    	Query query = builder.build();
        searchByQuery(query);
    }
    
    //通配符查询，?代表0个或1个任意字符,*代表0或多个任意字符
    //通配符查询，可能会降低效率，但对评分没有影响
    @Test
    public void wildcardQuery() throws Exception {
    	// sellPoint:?2*
        Query query = new WildcardQuery(new Term("sellPoint", "?2*"));
        searchByQuery(query);
    }
    
    //相似度查询，编辑距离算法，影响评分
    @Test
    public void fuzzyQuery() throws Exception {
    	// sellPoint:live~2
        Query query = new FuzzyQuery(new Term("sellPoint", "live"));
        searchByQuery(query);
    }
    
    
    //匹配所有文档
    @Test
    public void matchAllDocsQuery() throws Exception {
    	// *:*
        Query query = new MatchAllDocsQuery();
        searchByQuery(query);
    }
}
