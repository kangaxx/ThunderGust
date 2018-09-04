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

public class testhdfs{
  public static void main(String[] args){
    while(true)
      rescureFile(); 

  }

  private static void rescureFile(){
    int fileSize = 104857600;
    File root = new File("/mydata/khFiles");
    if (root.exists()){
      for (File f : root.listFiles())
      {
	if (f.isFile() && f.getAbsolutePath().indexOf(".end") > 1 && f.getAbsolutePath().indexOf(".upload") < 1 && f.length()> fileSize )
          uploadFile(f.getAbsolutePath());
      }
    }



  }

  private static void uploadFile(String fileName){
    Configuration conf = new Configuration();
    String hdfsDir = "hdfs://localhost:9000/test/" + System.currentTimeMillis();
    try{
	Path localPath = new Path(fileName);
	Path hdfsPath = new Path(hdfsDir);
	FileSystem hdfs = FileSystem.get(conf);
        Long beg = System.currentTimeMillis();
        System.out.println("file upload begin at :" + Long.toString(beg));
	hdfs.copyFromLocalFile(localPath, hdfsPath);
        Long end = System.currentTimeMillis();
        System.out.println("file upload end at :" + end); 
        File oldFile = new File(fileName);
        File newFile = new File(fileName + ".upload");
        oldFile.renameTo(newFile);
    }catch(Exception e){
	e.printStackTrace();
    }

  }
  
  private static void writeNewFile(String filename, String text){
    Configuration configuration=new Configuration();
    FSDataOutputStream out=null;
    String charset="UTF-8";
    try {
	FileSystem fSystem=FileSystem.get(URI.create(filename),configuration);
	Path path=new Path(filename);
	if(!fSystem.exists(path)){
	    out=fSystem.create(new Path(filename));
	    out.write(text.getBytes(charset),0,text.getBytes(charset).length);
	    out.write("\n".getBytes(charset),0,"\n".getBytes(charset).length);
	    out.flush();
	}else{
	    out=fSystem.append(path);
	    out.write(text.getBytes(charset),0,text.getBytes(charset).length);
	    out.write("\n".getBytes(charset),0,"\n".getBytes(charset).length);
	    out.flush();
	}
    } catch (IOException e) {
	e.printStackTrace();
    }finally{
	if(out!=null){
	    try {
		out.close();
	    } catch (IOException e) {
		e.printStackTrace();
	    }
	}
    }

  } 
}
