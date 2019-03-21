import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Random;
import common_java.common_java;


class SendDataThread implements Runnable {
  public static int sendNum = 500000;
  public volatile static int wordSize = 0;
  public static String[] device = {"Huawei","Apple","Mi"};
  public static String[] city = {"Shanghai","Beijin","Guangzhou"};
  public static String[] os = {"Android","iOs","other"};

  @Override
  public void run() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    System.out.println("begin at " +df.format(new Date()));
    send("MetisTest", "",df.format(new Date()), _thNum);
    System.out.println("end at " + df.format(new Date()) + "total size : " + wordSize);
  }

  public int _thNum;
  public SendDataThread(int num)
  {
     _thNum = num;
  }

  public void send(String topic,String key,String datetime, int thNum){
    Properties props = new Properties();
    props.put("bootstrap.servers", "101.201.234.219:9092");
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    KafkaProducer<String, String> producer = new KafkaProducer<String,String>(props);
    for(int i=0; i<sendNum; i++){
      StringBuilder sb = new StringBuilder("");
      for (int j  = 0 ; j < 1; ++j){
        int idxDev = (int)(Math.random() * device.length); 
        int idxCity =  (int)(Math.random() * city.length);
        int idxOs = (int)(Math.random() * os.length);
        sb.append( "{\"device\" : \"" + device[idxDev] + "\",\"city\" : \"" + city[idxCity]
              + "\", \"os\" : \"" + os[idxOs]  + "\"}");
      }
      String data = sb.toString();
   
      wordSize += data.length();
      producer.send(new ProducerRecord<String, String>(topic, ""+i, data));
    }
    
    producer.close();
  }
}

public class SendDataToKafka{
  public static void main(String []argc)
  {
    Runnable myThread1 = new SendDataThread(1);
    Runnable myThread2 = new SendDataThread(2);
    Runnable myThread3 = new SendDataThread(3);
    Runnable myThread4 = new SendDataThread(4);
    Runnable myThread5 = new SendDataThread(5);
    Runnable myThread6 = new SendDataThread(6);
    Runnable myThread7 = new SendDataThread(7);
    Runnable myThread8 = new SendDataThread(8);
    Runnable myThread9 = new SendDataThread(9);
    Runnable myThread0 = new SendDataThread(10);
    Runnable myThreada = new SendDataThread(11);
    Runnable myThreadb = new SendDataThread(12);
    Runnable myThreadc = new SendDataThread(13);
    Runnable myThreadd = new SendDataThread(14);
    Runnable myThreade = new SendDataThread(15);
    Runnable myThreadf = new SendDataThread(16);
    Runnable myThreadg = new SendDataThread(17);
    Runnable myThreadh = new SendDataThread(18);
    Runnable myThreadi = new SendDataThread(19);
    Runnable myThreadj = new SendDataThread(20);
 
    Thread t1 = new Thread(myThread1);
    Thread t2 = new Thread(myThread2);
    Thread t3 = new Thread(myThread3);
    Thread t4 = new Thread(myThread4);
    Thread t5 = new Thread(myThread5);
    Thread t6 = new Thread(myThread6);
    Thread t7 = new Thread(myThread7);
    Thread t8 = new Thread(myThread8);
    Thread t9 = new Thread(myThread9);
    Thread t0 = new Thread(myThread0);
    Thread ta = new Thread(myThreada);
    Thread tb = new Thread(myThreadb);
    Thread tc = new Thread(myThreadc);
    Thread td = new Thread(myThreadd);
    Thread te = new Thread(myThreade);
    Thread tf = new Thread(myThreadf);
    Thread tg = new Thread(myThreadg);
    Thread th = new Thread(myThreadh);
    Thread ti = new Thread(myThreadi);
    Thread tj = new Thread(myThreadj);



    t1.start();
//    t2.start();
//    t3.start();
//    t4.start();
//    t5.start();
//    t6.start();
//    t7.start();
//    t8.start();
//    t9.start();
//    t0.start();
//    ta.start();
//    tb.start();
//    tc.start();
//    td.start();
//    te.start();
    //tf.start();
    //te.start();
    //tf.start();
    //tg.start();
    //th.start();
    //ti.start();
    //tj.start();
  }


}
