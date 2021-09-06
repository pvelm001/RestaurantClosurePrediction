import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lucene_Searcher {

    static class Restaurant {

        String id;
        String name;

        Restaurant(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    public static String getFranchiseName(String restaurantName) throws IOException, ParseException {

        //Reading indexed data
        Directory nameDirectory  = FSDirectory.open(Paths.get("name_index"));
        DirectoryReader nameIndexReader  = DirectoryReader.open(nameDirectory);
        IndexSearcher nameIndexSearcher  = new IndexSearcher(nameIndexReader);

        //Set the ranking algorithm (R&D this!)
        //nameIndexSearcher.setSimilarity(new BM25Similarity());

        //Create and set field weights (R&D this!)
        String[] restaurant_fields = {"name"};
        Map<String, Float> restaurant_weights = new HashMap<>();
        restaurant_weights.put(restaurant_fields[0], 1.0f);

        //Set the query analyzer (R&D this!)
        Analyzer nameAnalyzer = new EnglishAnalyzer();
        MultiFieldQueryParser nameDataParser  = new MultiFieldQueryParser(restaurant_fields, nameAnalyzer, restaurant_weights);
        Query nameQuery;
        try {
             nameQuery = nameDataParser.parse(restaurantName);
        }
        catch (ParseException PE) {
            restaurantName = restaurantName.substring(0, restaurantName.length() - 1);
            nameQuery = nameDataParser.parse(restaurantName);
        }

        //Set the number of results (R&D this!)
        int topCount = 10;
        ScoreDoc[] scores = nameIndexSearcher.search(nameQuery, topCount).scoreDocs;


        //This is when no matching results were found (worst case)
        if (scores.length < 2) {
            return null;
        }

        //This is when there is a exact match (best case)
        Document docFounds = nameIndexSearcher.doc(scores[0].doc);
        System.out.println(scores[0].score + " " + docFounds.get("name"));
        for (int i=1; i < scores.length; i++) {
            Document docFound = nameIndexSearcher.doc(scores[i].doc);
            System.out.println(scores[i].score + " " + docFound.get("name"));
            if (restaurantName.equals(docFound.get("name"))) {
                return docFound.get("name");
            }
        }

        float topScore = scores[0].score;
        float resScore = scores[1].score; //Second best score

        //Setting a threshold for best match (in case of a article 'The')
        if (topScore == resScore) {
            Document docFound = nameIndexSearcher.doc(scores[1].doc);
            if (docFound.get("name").charAt(0) == restaurantName.charAt(0)) {
                return docFound.get("name");
            }
            if (docFound.get("name").startsWith("The") || restaurantName.startsWith("The")) {
                return docFound.get("name");
            }
            else {
                return null;
            }
        }

        //General Threshold
        if ((resScore/topScore < 0.60) || (resScore <= 2)) {
            return null;
        }
        else {
            Document docFound = nameIndexSearcher.doc(scores[1].doc);
            if (docFound.get("name").charAt(0) == restaurantName.charAt(0)) {
                return docFound.get("name");
            }
            else {
                return null;
            }
        }

    }

    public static void main(String[] args) throws IOException, InvalidFormatException, ParseException {

        /** Debugger

        List<Restaurant> restaurants = new ArrayList<>();

        //Retrieving data from the datastore (Excel)
        FileInputStream fis = new FileInputStream("Model_Dataset_Chain.xlsx"); //Change this for debugging
        XSSFWorkbook wb = new XSSFWorkbook(fis);
        XSSFSheet sheet = wb.getSheetAt(0);

        //Iterating over excel file to get all the names
        for (Row row : sheet) {
            restaurants.add(new Restaurant("" + row.getCell(0).toString(), "" + row.getCell(1).toString()));
        }

        FileInputStream inputStream = new FileInputStream("Model_Dataset_Chain.xlsx"); //Change this for debugging
        Workbook workbook = WorkbookFactory.create(inputStream);

        Sheet newSheet = workbook.createSheet();

        int rowCount = 0;

        for (Restaurant restaurant : restaurants) {

            Row row = newSheet.createRow(++rowCount);

            int columnCount = 0;
            Cell cell = row.createCell(++columnCount);
            cell.setCellValue(restaurant.id);
            cell = row.createCell(++columnCount);
            cell.setCellValue(restaurant.name);
            cell = row.createCell(++columnCount);
            cell.setCellValue(getFranchiseName(restaurant.name));

        }

        FileOutputStream outputStream = new FileOutputStream("Model_Dataset_Chain.xlsx"); //Change this for debugging
        workbook.write(outputStream);
        outputStream.close();

        **/

        System.out.println(getFranchiseName("Domino's Pizza\n"));

    }

}
