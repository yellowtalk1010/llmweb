package zuk.sast.rules;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.nio.file.Paths;

public class LuceneTest {

    public static void main(String[] args) throws Exception {
        //1. 创建分词器(对搜索的关键词进行分词使用)
        //注意: 分词器要和创建索引的时候使用的分词器一模一样
        Analyzer analyzer = new StandardAnalyzer();

        //2. 创建查询对象,
        //第一个参数: 默认查询域, 如果查询的关键字中带搜索的域名, 则从指定域中查询, 如果不带域名则从, 默认搜索域中查询
        //第二个参数: 使用的分词器
//        QueryParser queryParser = new QueryParser("md5", analyzer);
//        QueryParser queryParser = new QueryParser("fileName", analyzer);
//        QueryParser queryParser = new QueryParser("filePath", analyzer);
        QueryParser queryParser = new QueryParser("fileContent", analyzer);

        //3. 设置搜索关键词
        //华 OR  为   手   机
        Query query = queryParser.parse("foo");

        //4. 创建Directory目录对象, 指定索引库的位置
//        Directory dir = FSDirectory.open(Paths.get("E:\\dir"));
        Directory dir = FSDirectory.open(Paths.get("D:\\development\\github\\engine\\vision\\target\\workspace1\\CJ2000A\\indexDir"));
        //5. 创建输入流对象
        IndexReader indexReader = DirectoryReader.open(dir);
        //6. 创建搜索对象
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        //7. 搜索, 并返回结果
        //第二个参数: 是返回多少条数据用于展示, 分页使用
        TopDocs topDocs = indexSearcher.search(query, 100);

        //获取查询到的结果集的总数, 打印
        System.out.println("=======count=======" + topDocs.totalHits);

        //8. 获取结果集
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;

        //以下代码对查询结果进行高亮显示
        // 1.格式化对象，设置前缀和后缀
        Formatter formatter = new SimpleHTMLFormatter("<font color='red'>","</font>");
        // 2.关键词对象
        Scorer scorer = new QueryScorer(query);
        // 3. 高亮对象
        Highlighter highlighter = new Highlighter(formatter, scorer);

        //9. 遍历结果集
        if (scoreDocs != null) {
            for (ScoreDoc scoreDoc : scoreDocs) {

                System.out.println("==================================================");
                //获取查询到的文档唯一标识, 文档id, 这个id是lucene在创建文档的时候自动分配的
                int  docID = scoreDoc.doc;
                //通过文档id, 读取文档
                Document doc = indexSearcher.doc(docID);

                //通过域名, 从文档中获取域值
                System.out.println("===md5==" + doc.get("md5"));
                System.out.println("===fileName==" + doc.get("fileName"));
                System.out.println("===filePath==" + doc.get("filePath"));

                String fileContent = doc.get("fileContent");

                TokenStream tokenStream = analyzer.tokenStream("fileContent", new StringReader(fileContent));

                // 1. 关键词添加高亮
                //String highLightContent  = highlighter.getBestFragment(tokenStream, fileContent);
                TextFragment[] fragments = highlighter.getBestTextFragments(tokenStream, fileContent, false, 10);
                for (TextFragment fragment : fragments) {
                    float score = fragment.getScore();
                    if(score >= 0.99999999){
                        int startOffset = getStartOffset(fragment);
                        int endOffset = getEndOffset(fragment);
                        if(endOffset > startOffset && endOffset < fileContent.length()){

                            String subStr = fileContent.substring(startOffset, endOffset);
                            if (subStr.contains("foo")) {
                                int index = subStr.indexOf("foo");
                                if(startOffset + index < fileContent.length()){
                                    String subStrStr = fileContent.substring(startOffset+index, startOffset+index + "foo".length()); //可以得到具体的位置信息
                                    System.out.println();
                                }
                            }

                            String str = fragment.toString();
                            System.out.println(">>>>>>\n" +str);
                        }
                    }

                }
                System.out.println();


            }
        }
        //10. 关闭流
        indexReader.close();
    }


    public static int getStartOffset(TextFragment frag) throws Exception {
        Field field = TextFragment.class.getDeclaredField("textStartPos");
        field.setAccessible(true); // 取消 Java 访问检查
        return field.getInt(frag);
    }

    public static int getEndOffset(TextFragment frag) throws Exception {
        Field field = TextFragment.class.getDeclaredField("textEndPos");
        field.setAccessible(true);
        return field.getInt(frag);
    }

}
