import java.util.concurrent.atomic.*;
import java.text.SimpleDateFormat;
import groovy.sql.*;


sql = Sql.newInstance( 'jdbc:h2:tcp://localhost:4083/mem:testdb', 'sa', '', 'org.h2.Driver');
conn = sql.getConnection();
println "Connected to ${conn.getMetaData().getURL()}";
boolean addError = true;
types = ['BUY', 'SELL'] as String[];


isins = [];
sql.eachRow("SELECT ISIN from ISIN",  {
    isins.add(it.ISIN);
});
isinCount = isins.size();
df = new SimpleDateFormat("yyyy/MM/dd");
dt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
r = {max -> return Math.abs(random.nextInt(max));}
seq = {name -> return sql.firstRow("SELECT ${name}_SEQ.NEXTVAL".toString()).getAt(0).intValue();}
today = df.format(new Date());
for(x in 0..100) {
        fileId = seq("FILE");
        outFile = new File("/tmp/tradein/test-trades-${fileId}.csv");
        println "Writing to ${outFile}";
        outFile.delete();
        random = new Random(System.currentTimeMillis());
       
        tradeId = new AtomicLong(seq("TRADE"));
        orderId = new AtomicLong(seq("ORDER"));
        timeOff = new AtomicLong(0L);
        //recordCount = r(100);
        recordCount = 10;
        int errorRecord = r(recordCount);
        for(i in 0..10) {
            b = new StringBuilder();
            b.append(tradeId.incrementAndGet()).append(",");
            isin = isins[r(isinCount)];
            if(addError && i==errorRecord) {
                isin = "XXX${isin}";
            }
            b.append(isin).append(",");
            b.append(orderId.incrementAndGet()).append("-${fileId},");
            priceStr = "${r(1000000)}.${r(99)}";
            price = new BigDecimal(priceStr);
            b.append(price).append(",");
            b.append(today).append(",");
            timeOff.addAndGet(r(5000));
            b.append(dt.format(new Date(System.currentTimeMillis() + timeOff.get()))).append(",");
            b.append(types[r(2)]);
            println b;
            b.append("\n");
            outFile.append(b.toString());
            //tradeId.addAndGet(r(200));
            //orderId.addAndGet(r(200));            
        }
        println "Generated File:${outFile}";
}