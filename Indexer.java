import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    public void SetNewdocument(String url)throws IOException
    {
        this.url=url;
        this.document = Jsoup.connect(this.url).get();//for testing
        this.title=this.document.title().toString().toLowerCase();
        this.body=this.document.body().text();
        ////////////////////////////////////////////////////////////////// headers
        Elements hTags= this.document.select("h1,h2,h3,h4,h5,h6");
        Elements h1Tags = hTags.select("hadasda1");
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
        this.body = this.body.replaceAll("[^a-zA-Z0-9]", " "); //all special char to spaces
        this.body = this.body.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        this.body = this.body.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.tokensbody = this.body.split("\\s");//split string at each white space
        ////////////////////////////////////////////////////////////////
        this.title = this.title.replaceAll("[^a-zA-Z0-9]", " "); //all special char to spaces
        this.title = this.title.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        this.title = this.title.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.tokenstitle = this.title.split("\\s");//split string at each white space
        ////////////////////////////////////////////////////////////////
        this.header = this.header.replaceAll("[^a-zA-Z0-9]", " "); //all special char to spaces
        this.header = this.header.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
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
                "select Uid, URL " +
                        "from " + "DocumentFile" ;

        Statement stmt;
        ResultSet rs;
        try {
                I.db.postDocuments("https://ww1.gowatchseries.co/silicon-valley-season-5-episode-1");
                I.db.postDocuments("https://yesmovies.to/movie/silicon-valley-season-5-24194/1303731-14/watching.html");
                I.db.postDocuments("https://teamtreehouse.com/community/while-loop-to-fetch-mysql-data");
            }
        catch (Exception e){}
        while(run<2) {
            try {

                stmt = I.db.con.createStatement();  //create statement on the db connection
                rs = stmt.executeQuery(query);  //execute above query
                while (rs.next()) {
                    String url = rs.getString("URL");   //save current url link to string
                    int UID = rs.getInt("Uid");       //save current document number
                    I.SetNewdocument(url);
                    I.Splitter();
                    ////////////////////////////////////////////////////////////////
                    for (int i = 0; i < I.tokensheader.length; i++) {
                        try {
                            I.db.postWords(I.tokensheader[i], "Header"); //add current word to db
                            if ( run == 0 )
                                I.db.postInvertedfile(I.tokensheader[i], "Header", UID, i,0); //add current word with document & position in db
                            else if(run>0){
                                I.db.UpdateInvertedFile(I.tokensheader[i],"Header",UID,i);
                            }
                        } catch (Exception e) {
                        }
                    }
                    ////////////////////////////////////////////////////////////////
                    for (int i = 0; i < I.tokenstitle.length; i++) {
                        try {
                            I.db.postWords(I.tokenstitle[i], "Title"); //add current word to db
                            if ( run == 0 )
                            I.db.postInvertedfile(I.tokenstitle[i], "Title", UID, i,0); //add current word with document & position in db
                            else if(run>0){
                                I.db.UpdateInvertedFile(I.tokenstitle[i],"Title",UID,i);
                            }
                        } catch (Exception e) {
                        }
                    }
                    ////////////////////////////////////////////////////////////////
                    for (int i = 0; i < I.tokensbody.length; i++) {
                        try {
                            I.db.postWords(I.tokensbody[i], "Body"); //add current word to db
                            if ( run == 0 )
                            I.db.postInvertedfile(I.tokensbody[i], "Body", UID, i,0); //add current word with document & position in db
                            else if(run>0) {
                                I.db.UpdateInvertedFile(I.tokensbody[i], "Body", UID, i);
                            }
                        } catch (Exception e) {
                        }
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