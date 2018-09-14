import java.io.IOException;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import java.io.FileOutputStream;
import common_java.common_sharemem;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public class kafkaCon {
  public static common_sharemem instance = null;
  public static int fileSize = 100000000;
  private static MappedByteBuffer [] buffs = null;
  private static int currentIndex = -1; //如果currentIndex > -1 则可以追加写入数据，否则需要判断共享内存标志位
  private static int blockCount = 0;
  private static int fileSize = 0;
  public static void main(String[] args)throws IOException{
    if (args.length > 0)
      instance = common_sharemem.getInstance(args[0]);
    else
      System.out.println("[gxx warning] pls input config file name ,command like : java sharemem_unit_creator /mydata/sharemem.conf"); 
    initialShm(); //初始化共享内存空间
  
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
      }
    }
  }

  private static bool initialShm(){
    fileSize = common_java.StrToInt_safe(common_java.getAttributeByElem(configfile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                 common_global_variant.GLOB_STRING_MEMSHARE_FILE_CAPCITY), 0);
    blockCount = common_java.StrToInt_safe(common_java.getAttributeByElem(configfile, common_global_variant.GLOB_STRING_MEMSHARE_ELEMENT,
                   common_global_variant.GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT), 0);
    while( (blocks = instance.createWriteSharemem()) == null){System.out.print("Try to initial share memory ");}
    System.out.println ("Create share memory  ... success!");
  }



  //打开文件开始写入，与追加写入的程序略微不同
  private static void writeDataToShmHead(String value){
    if (value.length() >fileSize - 2) {
      System.out.print("String save to share mem fail, Too many words or Too little memory capcity");
      System.out.println(value); //一次性传入了一大堆数据，超过了共享内存块的极限，为什么这样？后续添加到日志管理内
      return;
    }

    for(int j = 0; j < blockCount; ++j){
      if (blocks[j] != null){
        blocks[j].rewind();
        byte mode = blocks[j].get(0);
        if (mode != (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE && 
            mode != (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_READING &&
            mode != (byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITING){
          blocks[j].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_WRITING);
          blocks[j].put(value.getBytes());
          if (blocks[j].position() == blocks[j].limit()) //文件正好写到末尾，概率不高吧        
            blocks[j].put(0,(byte)common_global_variant.GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE);
          else 
            currentIndex = j; //buf未写到末尾，可以继续写
          System.out.print(System.currentTimeMillis());
          System.out.println("");
          try{
            Thread.sleep(1);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    } //(for( int j..))
  }

  
}


