import javafx.scene.control.Tab;

import java.sql.*;
public class Database {
    protected Connection con;

    public Database() {
    }

    public static void main(String[] args) throws Exception {

    }

    public Connection connectDatabase() throws Exception {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/aptproject", "root", "12345");
            Statement stmt = con.createStatement();
            System.out.println("Connected");

            return con;
        } catch (Exception e) {
            System.out.println(e);
        }


        return null;
    }

    public void createTable() throws Exception {
        try {
            this.con = connectDatabase();
            //////Token for all documents
            {
                PreparedStatement create = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS tokens (" +
                        "id int NOT NULL AUTO_INCREMENT," +
                        "word varchar(255)," +
                        "type varchar(255)," +
                        "PRIMARY KEY(id)," +
                        "UNIQUE KEY `Word_Type` (`word`,`type`) )");
                create.executeUpdate();
            }
            ///////////Inverted index database

            {
                PreparedStatement create = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS documentfile (" +
                        "Uid int NOT NULL AUTO_INCREMENT," +
                        "URL varchar(255) UNIQUE," +
                        "FileName int," +
                        "PRIMARY KEY(Uid))");
                create.executeUpdate();
            }
            {
                PreparedStatement create = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS targeted_documentfile (" +
                        "document_number int NOT NULL ," +
                        "URL varchar(255) UNIQUE," +
                        "FileName int," +
                        "Rank int,"+
                        "FOREIGN KEY(document_number) REFERENCES documentfile(Uid))");
                create.executeUpdate();
            }
            {
                PreparedStatement create = this.con.prepareStatement("CREATE TABLE IF NOT EXISTS invertedfile (" +
                        "Wid int NOT NULL," +
                        "document int," +
                        "position int ," +
                        "flag int DEFAULT 0," +
                        "FOREIGN KEY (Wid)" +
                        "REFERENCES tokens(id)" +
                        "ON DELETE CASCADE," +
                        "FOREIGN KEY (document)" +
                        "REFERENCES DocumentFile(Uid)" +
                        "ON DELETE CASCADE," +
                        "UNIQUE KEY `Wid` (`Wid`,`document`,`position`))");
                create.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            System.out.println("Table Created");
        }
    }

    public void postWords(final String token ,final String type) throws Exception {
        //final String var1="john";
        try {

            {
                PreparedStatement posted = this.con.prepareStatement("INSERT INTO tokens(word,type) VALUES(('" + token + "'),('" + type + "'))");
                posted.executeUpdate();
            }

        } catch (Exception e) {
            // System.out.println(e);
        }

    }
    public void postDocuments(final String token, final int fileName) throws Exception {
        //final String var1="john";
        try {

            {
                PreparedStatement posted = this.con.prepareStatement("INSERT INTO documentfile(URL,FileName) VALUES('" + token + "','" + fileName + "')");
                posted.executeUpdate();
            }

        } catch (Exception e) {
            // System.out.println(e);
        }

    }

    public int getMaxFileName() throws Exception {
        try {
            String query = "SELECT max(FileName) FROM aptproject.documentfile";
            Statement stmt;
            ResultSet rs;

            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next())
            {
                return rs.getInt("max(FileName)");
            }
        }
        catch (Exception e)
        {}
        return 0;
    }

    public void postInvertedfile(final String token,final String type,int Did,int pos,int flag )  {
        //final String var1="john";
        try {


            {
                PreparedStatement posted = this.con.prepareStatement("INSERT INTO invertedfile(Wid,document,position,flag) VALUES" +
                        "((SELECT id FROM tokens t WHERE t.word=('"+token+"') AND t.type=('"+type+"')),('"+Did+"'),('"+pos+"'),('"+flag+"')) ");
                posted.executeUpdate();
            }
        } catch (Exception e) {
            // System.out.println(e);
        }

    }
    public void UpdateInvertedFile(final String token,final  String type,int doc,int pos)
    {  //ResultSet rs;
        try{
            PreparedStatement ps = this.con.prepareStatement("Select * FROM invertedfile WHERE Wid=(SELECT id FROM tokens t WHERE (t.word= ? AND t.type= ?) AND document=? AND position= ? )");
            ps.setString(1, token);
            ps.setString(2, type);
            ps.setInt   (3, doc);
            ps.setInt   (4, pos);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String query = "UPDATE invertedfile SET flag = ? WHERE Wid=(SELECT id FROM tokens t WHERE (t.word= ? AND t.type= ?) AND document=? AND position= ? )";
                PreparedStatement preparedStmt = this.con.prepareStatement(query);
                preparedStmt.setInt   (1, 1);
                preparedStmt.setString(2, token);
                preparedStmt.setString(3, type);
                preparedStmt.setInt   (4, doc);
                preparedStmt.setInt   (5, pos);
                // execute the java preparedstatement
                preparedStmt.executeUpdate();
                // Quest already completed
            } else {
                this.postWords(token,type);
                this.postInvertedfile(token,type,doc,pos,1);
                // Quest not completed yet
            }



        }
        catch(Exception e){}

    }
    public void SetFlagDefault()
    {
        try{
            String query = "UPDATE invertedfile SET flag = ?";
            PreparedStatement preparedStmt = this.con.prepareStatement(query);
            preparedStmt.setInt   (1, 0);
            preparedStmt.executeUpdate();
        }
        catch(Exception e){}

    }
    public void Delete0Flag()
    {
        try{
            String query = "DELETE FROM invertedfile WHERE flag = 0";
            PreparedStatement preparedStmt = this.con.prepareStatement(query);
            preparedStmt.executeUpdate();
        }
        catch(Exception e){}

    }
    public String getURL(int uid) throws Exception {
        try {
            String query = "SELECT URL FROM aptproject.documentfile WHERE Uid=" + uid +";";
            Statement stmt;
            ResultSet rs;

            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            while (rs.next())
            {
                return rs.getString("URL");
            }
        }
        catch (Exception e)
        {}
        return null;
    }

    public void getURLs(String urls[]) throws Exception {
        try {
            String query = "SELECT URL FROM aptproject.documentfile;";
            Statement stmt;
            ResultSet rs;

            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            int i = 0;
            while (rs.next())
            {
                urls[i] = rs.getString("URL");
                i++;
            }
        }
        catch (Exception e)
        {}
    }

    public int getURLCount() throws Exception
    {
        try {
            String query = "SELECT COUNT(*) FROM aptproject.documentfile";
            Statement stmt;
            ResultSet rs;

            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next())
            {
                return rs.getInt("COUNT(*)");
            }
        } catch(Exception e){}
        return 0;
    }

    public String checkURL(String url) throws Exception
    {
        try {
            String query = "SELECT URL FROM aptproject.documentfile WHERE URL = '" + url + "'";
            Statement stmt;
            ResultSet rs;

            stmt = con.createStatement();
            rs = stmt.executeQuery(query);

            while (rs.next())
            {
                return rs.getString("URL");
            }
        } catch(Exception e){}
        return null;
    }

}
