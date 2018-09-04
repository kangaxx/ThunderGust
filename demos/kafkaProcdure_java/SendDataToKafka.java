import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

class SendDataThread implements Runnable {
  public static int sendNum = 30000000;
  @Override
  public void run() {
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
    System.out.println(_sn + "begin at " +df.format(new Date()));
    send("mysqltest", "", _sn,df.format(new Date()));
    System.out.println(_sn+ "end at " + df.format(new Date()));
  }

  public String _sn;
  public SendDataThread(String sn)
  {
     _sn = sn;
  }

  public void send(String topic,String key,String data,String datetime){
    Properties props = new Properties();
    props.put("bootstrap.servers", "172.17.0.59:9092");
    props.put("acks", "all");
    props.put("retries", 0);
    props.put("batch.size", 16384);
    props.put("linger.ms", 1);
    props.put("buffer.memory", 33554432);
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

    KafkaProducer<String, String> producer = new KafkaProducer<String,String>(props);
    for(int i=1;i<sendNum;i++){
      producer.send(new ProducerRecord<String, String>(topic, ""+i, "[" + datetime + "] " + data + " msg no: " + i));
    }
    producer.close();
  }
}

public class SendDataToKafka{
  public static void main(String []argc)
  {
    String longWord = "s";
    for (int i = 0 ; i < 5000; ++i){
	longWord += "s";
    }
    
    Runnable myThread1 = new SendDataThread(longWord);
    Runnable myThread2 = new SendDataThread(longWord);
    Runnable myThread3 = new SendDataThread(longWord);
    Runnable myThread4 = new SendDataThread(longWord);
    Runnable myThread5 = new SendDataThread(longWord);
    Runnable myThread6 = new SendDataThread(longWord);
    Runnable myThread7 = new SendDataThread("thread 07");
    Runnable myThread8 = new SendDataThread("thread 08");
    Runnable myThread9 = new SendDataThread("thread 09");
    Runnable myThread0 = new SendDataThread("thread 10");
    Runnable myThreada = new SendDataThread("thread 0a");
    Runnable myThreadb = new SendDataThread("thread 0b");
    Runnable myThreadc = new SendDataThread("thread 0c");
    Runnable myThreadd = new SendDataThread("thread 0d");
    Runnable myThreade = new SendDataThread("thread 0e");
    Runnable myThreadf = new SendDataThread("thread 0f");
    Runnable myThreadg = new SendDataThread("thread 0g");
    Runnable myThreadh = new SendDataThread("thread 0h");
    Runnable myThreadi = new SendDataThread("thread 0i");
    Runnable myThreadj = new SendDataThread("thread 0j");
 
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
    t2.start();
    t3.start();
    t4.start();
    t5.start();
    t6.start();
    t7.start();
    t8.start();
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
