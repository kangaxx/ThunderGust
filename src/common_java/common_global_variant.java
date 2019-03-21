package common_java;
public class common_global_variant{
  public static String GLOB_STRING_MEMSHARE_ELEMENT = "share_memory";
  public static String GLOB_STRING_KAFKA_PROPERTY = "kafka_property";
  public static String GLOB_STRING_MEMSHARE_ATTRIBUTE_BLOCKCOUNT = "block_count";
  public static String GLOB_STRING_MEMSHARE_FILE_PREFIX_ATTRIBUTE = "file_prefix";
  public static String GLOB_STRING_MEMSHARE_LOG_FILE_PATH="log_file_prefix";
  public static int GLOB_INT_MEMSHARE_BLOCKCOUNT_MAX = 20;
  public static String GLOB_STRING_MEMSHARE_FILE_CAPCITY = "capacity";
  public static int GLOB_INT_MEMSHARE_FILE_STATUS_SLEEP = 0;
  public static int GLOB_INT_MEMSHARE_FILE_STATUS_WRITING = 1;
  public static int GLOB_INT_MEMSHARE_FILE_STATUS_READING = 2;
  public static int GLOB_INT_MEMSHARE_FILE_STATUS_AFTER_WRITE = 3;
  public static int GLOB_INT_MEMSHARE_FILE_STATUS_WRITE_CONTINUE = 5;
  public static int GLOB_INT_MEMSHARE_FILE_STATUS_WAIT = 9;
  public static int GLOB_INT_MEMSHARE_WRITE_STATUS_SUCCESS = 0;
  public static int GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_READING =1; //暂时不能写，至少有一个内存块在等待读取，所以请等待读取进程
  public static int GLOB_INT_MEMSHARE_WRITE_STATUS_WAIT_FLUSH = 2; //没有内存块等待读取，但至少有一个内存块可以强制读取并清理
  public static int GLOB_INT_MEMSHARE_WRITE_STATUS_INPUT_ERROR = 3; //输入语句过长，不必重试了，直接抛弃并记录日志吧
  public static int GLOB_INT_MEMSHARE_WRITE_STATUS_FATAL_ERROR = 4; //其他严重错误，记录日志,整个系统需要调试了？
  public static String GLOB_STRING_MEMSHARE_BOOTSTRAP_SERVICE_IP = "bootstrap.servers";
  public static String GLOB_STRING_MEMSHARE_GROUP_ID = "group.id";
  public static String GLOB_STRING_AUTO_COMMIT = "enable.auto.commit";
  public static String GLOB_STRING_AUTO_COMMIT_INTERVAL = "auto.commit.interval.ms";
  public static String GLOB_STRING_AUTO_TIME_OUT = "session.timeout.ms";
  public static String GLOB_STRING_KEY_DESERIALIZER = "key.deserializer";
  public static String GLOB_STRING_VALUE_DESERIALIZER = "value.deserializer";
}
