import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class StepFive {

    private static class MyMapper extends Mapper<LongWritable, Text,Text, Text> {
        public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException {

            String[] lines = val.toString().split("\t");
            String[] words = lines[0].split(" ");
            String[] words2 = lines[1].split(" ");


            if (words.length>2){
                String w1 = words[0], w2 = words[1], w3 = words[2];
                long occurs = 0L;
                Text text = new Text();

                text.set(String.format("%s %s %s" , w1, w2,w3));

                if (words2.length>2){
                    occurs = Long.parseLong(words2[2]);
                    Text text1 = new Text();
                    String word1 = words2[0], word2 = words2[1];

                    text1.set(String.format("%s %s %d" ,word1,word2,occurs));

                    context.write(text,text1);
                }
                else{
                    occurs = Long.parseLong(lines[1]);
                    Text text1 = new Text();
                    text1.set(String.format("%d" , occurs));
                    context.write(text,text1);
                }
            }
        }
    }

    private static class MyReducer extends Reducer<Text, Text, Text, Text> {
        public static Long c0 = 0L;
        public static HashMap<String, Double> probMap = new HashMap<>();


        public void reduce (Text key,Iterable<Text> values, Context context) throws IOException, InterruptedException {

            String[] lines = key.toString().split(" ");
            boolean flag1 = false , flag2 = false;

             Double  n1 = 0.0 ,c1 = 0.0 ,
                     n2 = 0.0, n3 = 0.0, c2 = 0.0,
                     k2 = 0.0 , k3 = 0.0, prob = 0.0 ;

            Text newKey = new Text() , newVal = new Text();

            if (lines.length>2){
                String w1 =lines[0] , w2 = lines [1], w3 = lines[2];
                System.out.println("w1: " + w1 + " w2: " + w2 + " w3: "+w3);
                n1 = probMap.get(w3);
                c1 = probMap.get(w2);
                System.out.println("c1: " + c1 + "n1: " + n1);

                for (Text value : values){
                    String[] val = value.toString().split(" ");
                    if (val.length > 2 ){
                        if (val[0].equals(w1) && val[1].equals(w2)){
                            c2 = Double.parseDouble(val[2]);
                            System.out.println("c2: " + c2);
                            flag1=true;
                        }
                        else{
                            n2 = (val[0].equals(w2) && val[1].equals(w3))?
                                    Double.parseDouble(val[2]) : 0.0;
                            k2 = (Math.log(n2+1)+1)/(Math.log(n2+1)+2);
                            System.out.println("n2: " + n2 +" k2: " + k2);
                            flag2 = true;
                        }

                    }
                    else{
                        n3 =  Double.parseDouble(val[0]);
                        k3 = (Math.log(n3+1)+1)/(Math.log(n3+1)+2);
                        System.out.println("n3: " + n3 + " k3: " + k3);
                    }
                    if (c1 != null && n1 != null && flag1 && flag2){

                        prob =  (k3 * (n3 / c2)) +
                                ((1 - k3) * k2 * (n2 / c1)) +
                                ((1 - k3) * (1 - k2) * (n1 / c0)) ;
                        newKey.set(String.format("%s %s %s" , w1,w2,w3));
                        newVal.set(String.format("%f" , prob));
                        System.out.println("newKey: " + newKey.toString() + " newVal: " + newVal.toString());
                        context.write(newKey,newVal);
                    }
                }
            }
        }

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            FileSystem fileSystem = FileSystem.get(URI.create("s3://assignment2bucket1992"), context.getConfiguration());
            FSDataInputStream fsDataInputStream = fileSystem.open(new Path(("s3://assignment2bucket1992/output/outputStepOne/part-r-00000")));
            BufferedReader reader = new BufferedReader(new InputStreamReader(fsDataInputStream, "UTF-8"));
            String line=null;
            String[] words;
            while ((line = reader.readLine()) != null){
                words = line.split("\t");
                if(words[0].equals("*")){
                    c0=Long.parseLong(words[1]);
                }
                else{
                    probMap.put(words[0], (Double) Double.parseDouble(words[1]));
                }
            }
            reader.close();
        }
    }

    private static class MyPartitioner extends Partitioner<Text,Text> {
        @Override
        public int getPartition(Text key, Text value, int numPartitions){
            return Math.abs(key.hashCode()) % numPartitions;
        }
    }

    public static void main(String[] args) throws IOException,ClassNotFoundException, InterruptedException {
        Configuration configuration = new Configuration();
        Job job =Job.getInstance(configuration);
        job.setJarByClass(StepFive.class);
        job.setMapperClass(StepFive.MyMapper.class);
        job.setReducerClass(StepFive.MyReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setPartitionerClass(StepFive.MyPartitioner.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        String input1=args[0];
        String input2=args[1];
        String output=args[2];
        MultipleInputs.addInputPath(job, new Path(input1), TextInputFormat.class);
        MultipleInputs.addInputPath(job, new Path(input2), TextInputFormat.class);
        FileOutputFormat.setOutputPath(job, new Path(output));
        job.waitForCompletion(true);
    }
}
