
import java.io.*;
import java.sql.ResultSet;
import java.sql.Statement;

public class Queryprocessor extends Database
{

    private  Database db;

    public Queryprocessor(Database db) throws IOException {
        this.db = db;
    }
    public  void ProcessString(String s)
    {
        // stem(s);

    }
    public static void main(String[] args)throws IOException
    {



    }

}
