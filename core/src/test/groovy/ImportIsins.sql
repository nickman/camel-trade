import groovy.sql.*;
import java.text.SimpleDateFormat;

sql = Sql.newInstance( 'jdbc:h2:tcp://localhost:3083/mem:testdb', 'sa', '', 'org.h2.Driver');
conn = sql.getConnection();
println "Connected to ${conn.getMetaData().getURL()}";

df = new SimpleDateFormat("dd MMM yyyy");

dataFile = new File("/home/nwhitehead/hprojects/camel-trade/core/src/test/resources/data/isins.txt");
int maxName = 0;
int col = 2;
Object[] frags = new Object[11];
dataFile.eachLine() {
    sfrags = it.split("\t");
    for(i in 0..sfrags.length-1) {
        frags[i] = sfrags[i].trim();
        if(frags[i].isEmpty()) frags[i]=null;
        if((i==5||i==6||i==7||i==10) && frags[i]!=null) {
            frags[i] = df.parse(frags[i]);
        }
    }
    //if(frags[col].length() > maxName) maxName = frags[col].length();
    //println frags;
    //try {
        sql.execute("""INSERT INTO ISIN (NAME,ISIN,COMMON_CODE,RATE,CURRENCY,CLOSE_DATE,NEXT_COUPON_DATE,RECORD_DATE,MARKET,INSTRUMENT,LAST_UPDATE) VALUES ("""
        + """?,?,?,?,?,?,?,?,?,?,?"""
        + """)""", [frags[0], frags[1], frags[2], frags[3], frags[4], frags[5], frags[6], frags[7], frags[8], frags[9], frags[10]]);
    //} catch (e) {
    //    println "Failed to insert ${frags}, ${e}";
    //}
}
println maxName;