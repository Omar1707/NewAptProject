
import opennlp.tools.stemmer.PorterStemmer;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Queryprocessor extends Database
{

    private  String [] userstring;
    private  Database db;

    public Queryprocessor() throws IOException {
        this.db=new Database();

    }

    public void ProcessString(String s) throws Exception {
        this.db.connectDatabase();
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

        s = s.toLowerCase(); //all capital to small
        s = s.replaceAll("[^a-zA-Z0-9]", " "); //all special char to spaces
        s = s.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        s = s.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.userstring = s.split("\\s");//split string at each white space
        /////////////////////////////////////////////////// process the string
        for (int i = 0; i < this.userstring.length; i++) {

            String stem = new PorterStemmer().stem(this.userstring[i]);
            String query = "SELECT id from tokens WHERE word='"+stem +"'";
            System.out.println(stem);
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
    public  void ProcessPhrase(String s) throws Exception {
        this.db.connectDatabase();
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

        s = s.toLowerCase(); //all capital to small
        s = s.replaceAll("[^a-zA-Z0-9]", " "); //all special char to spaces
        s = s.replaceAll("(\\ban\\b)|(\\ba\\b)|(\\band\\b)|(\\bin\\b)", " ");
        s = s.replaceAll("\\s{2,}", " ");//any 2 or more whitespaces to 1 whitespace
        this.userstring = s.split("\\s");//split string at each white space

        /////////////////////////////////////////////////// process the string
        String stem0=new PorterStemmer().stem(this.userstring[0]);
        String query_init = "SELECT id,type from tokens WHERE word='"+stem0 +"'";
        System.out.println(stem0);
        Statement stmt_init;
        ResultSet rs_init;
        try {
            stmt_init = db.con.createStatement();  //create statement on the db connection
            rs_init = stmt_init.executeQuery(query_init);  //execute above query
            while (rs_init.next()) {
                System.out.println("entered first query");
                int Wid_init = rs_init.getInt("id");
                String type=rs_init.getString("type");
                String query1_init = "SELECT document,position FROM invertedfile WHERE Wid='" + Wid_init + "'";
                Statement stmt1_init = db.con.createStatement();  //create statement on the db connection
                ResultSet rs1_init = stmt1_init.executeQuery(query1_init);  //execute above query
                while (rs1_init.next()) {
                    int doc=rs1_init.getInt("document");
                    int pos=rs1_init.getInt("position");

                    for (int i = 1; i < this.userstring.length; i++) {
                        String stem = new PorterStemmer().stem(this.userstring[i]);
                        String query = "SELECT id from tokens WHERE word='" + stem + "' and type='"+type+"'";
                        System.out.println(stem);
                        Statement stmt;
                        ResultSet rs;
                        try {
                            stmt = db.con.createStatement();  //create statement on the db connection
                            rs = stmt.executeQuery(query);  //execute above query

                            if (rs.next()) {
                                System.out.println("entered first query");
                                int Wid = rs.getInt("id");
                                String query1 = "SELECT document,position FROM invertedfile WHERE Wid='" + Wid + "' and document='"+doc+"' and position='"+(pos+i)+"'";
                                Statement stmt1 = db.con.createStatement();  //create statement on the db connection
                                ResultSet rs1 = stmt1.executeQuery(query1);  //execute above query
                                if(rs1.next())
                                {                                   System.out.println("entered 2nd query");

                                    if (i==userstring.length -1 )
                                    {
                                        System.out.println("found document");

                                        int did=rs1.getInt("document");
                                        String query2 = "select Uid, URL,FileName from documentfile WHERE Uid='" + did + "'";
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
                                else break;
                            }
                            else break;
                        } catch (Exception e) {
                            System.out.println(e);
                        }

                    }
                    //System.out.println("done processing");
                }
            }
        }catch (Exception e){
            System.out.println(e);

        }

    }




}