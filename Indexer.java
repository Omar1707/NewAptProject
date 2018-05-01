import opennlp.tools.stemmer.PorterStemmer;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.sound.midi.SysexMessage;
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;
import  java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Indexer extends Database {
    private String url;
    private Document document;
    private String body;
    private String title;
    private String header;
    private String[] tokensbody;
    private String[] tokenstitle;
    private String[] tokensheader;
    private Database db;

    public Indexer(Database db) throws IOException {
        this.db=db;
    }

    public void SetNewdocument(Document doc)throws IOException
    {
        //this.url=url;
        //this.document = Jsoup.connect(this.url).get();//for testing
        this.title=doc.title().toString().toLowerCase();
        this.body=doc.body().text();
        ////////////////////////////////////////////////////////////////// headers
        Elements hTags= doc.select("h1,h2,h3,h4,h5,h6");
        Elements h1Tags = hTags.select("h1");
        Elements h2Tags = hTags.select("h2");
        Elements h3Tags = hTags.select("h3");
        Elements h4Tags = hTags.select("h4");
        Elements h5Tags = hTags.select("h5");
        Elements h6Tags = hTags.select("h6");
        for (Element h1 : h1Tags) {
            String header1 = h1.toString().toLowerCase();
           String noHTMLString = header1.replaceAll("\\<.*?\\>", "");
           this.header=this.header+noHTMLString;
        }
        for (Element h2 : h2Tags) {
            String header1 = h2.toString().toLowerCase();
            String noHTMLString = header1.replaceAll("\\<.*?\\>", "");
            this.header=this.header+noHTMLString;
        }
        for (Element h3 : h3Tags) {
            String header1 = h3.toString().toLowerCase();
            String noHTMLString = header1.replaceAll("\\<.*?\\>", "");
            this.header=this.header+noHTMLString;
        }
        for (Element h4 : h4Tags) {
            String header1 = h4.toString().toLowerCase();
            String noHTMLString = header1.replaceAll("\\<.*?\\>", "");
            this.header=this.header+noHTMLString;
        }
        for (Element h5 : h5Tags) {
            String header1 = h5.toString().toLowerCase();
            String noHTMLString = header1.replaceAll("\\<.*?\\>", "");
            this.header=this.header+noHTMLString;
        }
        for (Element h6 : h6Tags) {
            String header1 = h6.toString().toLowerCase();
            String noHTMLString = header1.replaceAll("\\<.*?\\>", "");
            this.header=this.header+noHTMLString;
        }


    }
    ///// this function split the document body into tokens then normalize it
    public void Splitter()throws IOException
    {
        ////////////////////////////////////////////////////////////////
        this.body = this.body.toLowerCase(); //all capital to small
        this.body = this.body.replaceAll("[^a-zA-Z0-9]|(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " "); //all special char to spaces
        //this.body = this.body.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        this.body = this.body.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.tokensbody = this.body.split("\\s");//split string at each white space
        ////////////////////////////////////////////////////////////////
        this.title = this.title.replaceAll("[^a-zA-Z0-9]|[^a-zA-Z0-9]|(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " "); //all special char to spaces
        //this.title = this.title.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        this.title = this.title.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.tokenstitle = this.title.split("\\s");//split string at each white space
        ////////////////////////////////////////////////////////////////
        this.header = this.header.replaceAll("[^a-zA-Z0-9]|(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " "); //all special char to spaces
        //.header = this.header.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        this.header = this.header.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.tokensheader = this.header.split("\\s");//split string at each white space
    }





    public static void main(String[] args)throws IOException
    {   Database d1=new Database();
        Indexer I=new Indexer(d1);
        int run=0;
        int flag=0;
        try {
                I.db.createTable();
            }
        catch (Exception e){}

        String query =
                "select Uid, URL, FileName, Indexed " +
                        "from " + "DocumentFile" ;
        Statement stmt,stmt1;
        ResultSet rs,rs1;
        while(true) {
            try {

                stmt = I.db.con.createStatement();  //create statement on the db connection
                rs = stmt.executeQuery(query);  //execute above query
                while (rs.next()) {
                    String url = rs.getString("URL");   //save current url link to string
                    int UID = rs.getInt("Uid");       //save current document number
                    int filename = rs.getInt("FileName");
                    int indexed = rs.getInt("Indexed");
                    System.out.println(Integer.toString(filename));
                    if (UID != 1) {
                        try {
                            File input = new File("Files/" + Integer.toString(filename) + ".txt");

                            Document doc = Jsoup.parse(input, "UTF-8", url);


                            I.SetNewdocument(doc);
                            I.Splitter();
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                        ////////////////////////////////////////////////////////////////

                            for (int i = 0; i < I.tokensheader.length; i++) {
                                try {
                                    String stem = new PorterStemmer().stem(I.tokensheader[i]);
                                    I.db.postWords(stem, "Header"); //add current word to db
                                    if (run == 0)
                                        I.db.postInvertedfile(stem, "Header", UID, i, 0); //add current word with document & position in db
                                    else if (run > 0) {
                                        I.db.UpdateInvertedFile(stem, "Header", UID, i);
                                    }
                                } catch (Exception e) {
                                }
                                I.header = "";
                            }
                            ////////////////////////////////////////////////////////////////
                            for (int i = 0; i < I.tokenstitle.length; i++) {
                                try {
                                    String stem = new PorterStemmer().stem(I.tokenstitle[i]);

                                    I.db.postWords(stem, "Title"); //add current word to db
                                    if (run == 0)
                                        I.db.postInvertedfile(stem, "Title", UID, i, 0); //add current word with document & position in db
                                    else if (run > 0) {
                                        I.db.UpdateInvertedFile(stem, "Title", UID, i);
                                    }
                                } catch (Exception e) {
                                }
                            }
                            ////////////////////////////////////////////////////////////////
                            for (int i = 0; i < I.tokensbody.length; i++) {
                                try {
                                    String stem = new PorterStemmer().stem(I.tokensbody[i]);
                                    I.db.postWords(stem, "Body"); //add current word to db
                                    if (run == 0)
                                        I.db.postInvertedfile(stem, "Body", UID, i, 0); //add current word with document & position in db
                                    else if (run > 0) {
                                        I.db.UpdateInvertedFile(stem, "Body", UID, i);
                                    }
                                } catch (Exception e) {
                                }
                            }
                           // stmt1=I.db.con.createStatement();
                          //rs1=stmt1.executeQuery("Update documentfile SET Indexed = 1  WHERE UID='"+UID+"'");

                    }
                }
            } catch (Exception e) {
            }
           if(run>8) I.db.Delete0Flag();
            System.out.println("Insertion complete");
            I.db.SetFlagDefault();
            run++;
        }

    }

}