import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Lucene_Indexer {

    static class Restaurant {
        String name;

        Restaurant(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) throws IOException, NumberFormatException {

        List<Restaurant> restaurants = new ArrayList<>();

        //Retrieving data from the datastore (Excel)
        FileInputStream fis = new FileInputStream("Model_Dataset_Chain.xlsx"); //Change this for debugging
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);

        //Iterating over excel file
        for (Row row : sheet) {
            restaurants.add(new Restaurant("" + row.getCell(1).toString()));
        }

        //Name Indexing
        Directory nameDirectory = FSDirectory.open(Paths.get("name_index"));
        IndexWriterConfig nameConfig = new IndexWriterConfig(new EnglishAnalyzer());
        IndexWriter nameIndexWriter  = new IndexWriter(nameDirectory, nameConfig);

        for(Restaurant restaurant: restaurants) {
            Document doc = new Document();
            doc.add(new TextField("name", restaurant.name, Field.Store.YES));
            nameIndexWriter.addDocument(doc);
        }
        nameIndexWriter.close();
        nameDirectory.close();
    }
}
