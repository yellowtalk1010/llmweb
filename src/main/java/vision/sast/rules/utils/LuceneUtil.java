package vision.sast.rules.utils;

import lombok.Data;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LuceneUtil {

    @Data
    public static class IndexDto {
        private String md5;
        private String fileName;
        private String filePath;
        private List<HighlightDto> highlightDtos = new ArrayList<>();
    }

    @Data
    public static class HighlightDto {
        private String lineStr;
        private Integer lineNum;
        HighlightDto(Integer lineNum, String lineStr){
            this.lineStr = lineStr;
            this.lineNum = lineNum;
        }
    }

    public static List<IndexDto> search(String search, String indexDir) throws Exception {
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser("fileContent", analyzer);
        Query query = queryParser.parse(search);
        Directory dir = FSDirectory.open(Paths.get(indexDir));
        IndexReader indexReader = DirectoryReader.open(dir);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        TopDocs topDocs = indexSearcher.search(query, 1000);
        System.out.println("=======count=======" + topDocs.totalHits);
        ScoreDoc[] scoreDocs = topDocs.scoreDocs;
        Formatter formatter = new SimpleHTMLFormatter("<font color='red'>","</font>");
        Scorer scorer = new QueryScorer(query);
        Highlighter highlighter = new Highlighter(formatter, scorer);

        List<IndexDto> indexDtos = new ArrayList<>();

        if (scoreDocs != null) {
            for (ScoreDoc scoreDoc : scoreDocs) {
                int  docID = scoreDoc.doc;
                //通过文档id, 读取文档
                Document doc = indexSearcher.doc(docID);

                //通过域名, 从文档中获取域值
//                System.out.println("===md5==" + doc.get("md5"));
//                System.out.println("===fileName==" + doc.get("fileName"));
//                System.out.println("===filePath==" + doc.get("filePath"));

                IndexDto indexDto = new IndexDto();
                indexDto.md5 = doc.get("md5");
                indexDto.fileName = doc.get("fileName");
                indexDto.filePath = doc.get("filePath");


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
                            if (subStr.contains(search)) {
                                int index = subStr.indexOf(search);
                                if(startOffset + index < fileContent.length()){
                                    String subStrStr = fileContent.substring(startOffset+index, startOffset+index + search.length()); //可以得到具体的位置信息
                                    int lineNum = fileContent.substring(0, startOffset+index + search.length()).split("\n").length;

                                    String highlightStr = fragment.toString();
                                    HighlightDto highlightDto = new HighlightDto(lineNum, highlightStr);
                                    indexDto.highlightDtos.add(highlightDto);
                                    indexDto.highlightDtos.stream().sorted(Comparator.comparing(HighlightDto::getLineNum));
                                }
                            }

                        }
                    }

                }

                indexDtos.add(indexDto);

            }
        }
        //10. 关闭流
        indexReader.close();

        return indexDtos.stream().sorted(Comparator.comparing(IndexDto::getFilePath)).toList();
    }

    private static int getStartOffset(TextFragment frag) throws Exception {
        Field field = TextFragment.class.getDeclaredField("textStartPos");
        field.setAccessible(true); // 取消 Java 访问检查
        return field.getInt(frag);
    }

    private static int getEndOffset(TextFragment frag) throws Exception {
        Field field = TextFragment.class.getDeclaredField("textEndPos");
        field.setAccessible(true);
        return field.getInt(frag);
    }

}
