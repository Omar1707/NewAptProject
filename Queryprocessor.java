
import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Queryprocessor extends Database
{

    private  String [] userstring;

    public Queryprocessor() throws IOException {
    }


    public  void ProcessString(String s) throws Exception {
        Database db = new Database();
        db.connectDatabase();
        /////////////////////////////////////////////////// empty table
        String query0 ="TRUNCATE TABLE targeted_documentfile";
        try {
            ResultSet stmt0 = db.con.createStatement().executeQuery(query0);  //create statement on the db connection
        }
        catch(Exception e)
        {
            System.out.println(e);
        }
        /////////////////////////////////////////////////// normalize string
        String stem = new PorterStemmer().stem(s);
        stem = stem.toLowerCase(); //all capital to small
        stem = stem.replaceAll("[^a-zA-Z0-9]", " "); //all special char to spaces
        stem = stem.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        stem = stem.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.userstring = stem.split("\\s");//split string at each white space
        /////////////////////////////////////////////////// process the string
        for (int i = 0; i < this.userstring.length; i++) {
            System.out.println(this.userstring[i]);

            String query = "SELECT id from tokens WHERE word='"+this.userstring[i] +"'";
            Statement stmt;
            ResultSet rs;
            try {
                stmt = db.con.createStatement();  //create statement on the db connection
                rs = stmt.executeQuery(query);  //execute above query

                while (rs.next()) {
                    System.out.println("entered first query");
                    int Wid = rs.getInt("id");
                    String query1 = "SELECT document FROM invertedfile WHERE Wid='" + Wid + "'";
                    Statement stmt1 = db.con.createStatement();  //create statement on the db connection
                    ResultSet rs1 = stmt1.executeQuery(query1);  //execute above query
                    while (rs1.next()) {
                        System.out.println("entered 2nd query");
                        int Uid = rs1.getInt("document");
                        String query2 = "select Uid, URL,FileName from documentfile WHERE Uid='" + Uid + "'";
                        Statement stmt2 = db.con.createStatement();  //create statement on the db connection
                        ResultSet rs2 = stmt2.executeQuery(query2);  //execute above query
                        while (rs2.next()) {
                            System.out.println("entered 3rd query");
                            int id = rs2.getInt("Uid");
                            String link = rs2.getString("URL");
                            int filename = rs2.getInt("FileName");
                            db.posttargeted_Documents(id, link, filename, 0);

                        }

                    }


                }
            }
            catch (Exception e){System.out.println(e);}

        }
        System.out.println("done processing");
    }


    public static void main(String[] args) throws Exception
    {
        Queryprocessor q=new Queryprocessor();
        q.ProcessString("travel is an a awsome");


    }

}
