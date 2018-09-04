import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
 
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;


//hadoop package
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;



public class kafkaCon {
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
        while(true){
        	consumerRecords = consu.poll(1000);
        	for(ConsumerRecord<String, String> consumerRecord : consumerRecords){
        		//long offset = consumerRecord.offset();
        		//int partition = consumerRecord.partition();
        		//Object key = consumerRecord.key();
        		Object value = consumerRecord.value();
                        System.out.println(value);
        		//System.out.println(offset+" "+partition+" "+key+" "+value);
        	}
        }
    }
}


