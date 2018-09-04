import java.io.IOException;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.io.FileOutputStream;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class kafkaCon {
  public static int fileSize = 114857600;
  public static void main(String[] args)throws IOException{
    Properties props = new Properties();
    props.put("bootstrap.servers", "172.17.0.59:9092");
    props.put("group.id", "test-consumer-group");
    props.put("enable.auto.commit", "true");
    //props.put("enable.auto.commit", "false");
    props.put("auto.commit.interval.ms", "1000");
    props.put("session.timeout.ms", "30000");
    props.put("key.deserializer",
	    "org.apache.kafka.common.serialization.StringDeserializer");
    props.put("value.deserializer",
	    "org.apache.kafka.common.serialization.StringDeserializer");
    Consumer<String,String> consu = new KafkaConsumer<String,String>(props);
    Collection<String> topics = Arrays.asList("mysqltest");
    consu.subscribe(topics);
    ConsumerRecords<String,String>consumerRecords = null;
    String fileName = Long.toString(System.currentTimeMillis());
    int wordSize = 0;
    FileOutputStream outStr = new FileOutputStream(new File("/mydata/khFiles/" + fileName + ".end"));
    System.out.println("fileName : " + fileName);
    BufferedOutputStream Buff = new BufferedOutputStream(outStr);
    while(true){
      
      consumerRecords = consu.poll(100);
      for(ConsumerRecord<String, String> consumerRecord : consumerRecords){
	//long offset = consumerRecord.offset();
	//int partition = consumerRecord.partition();
	//Object key = consumerRecord.key();
	consumerRecord.value();
        wordSize += consumerRecord.value().length();
        Buff.write(consumerRecord.value().getBytes());
        if (wordSize > fileSize){
	  Buff.flush();
          Buff.close();
          File oldFile = new File("root/khFiles/" + fileName);
          fileName = Long.toString(System.currentTimeMillis());
          wordSize = 0;
          outStr = null;
          outStr = new FileOutputStream(new File("/mydata/khFiles/" + Long.toString(System.currentTimeMillis()) + ".end"));
          Buff = null;
          Buff = new BufferedOutputStream(outStr);
        }
        //System.out.println(value.toString());
	//System.out.println(offset+" "+partition+" "+key+" "+value);
      }
    }
  }
}


