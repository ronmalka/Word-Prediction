import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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

import java.io.IOException;

public class StepFour {
    private static class MyMapper extends Mapper<LongWritable, Text,Text, Text> {
        public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException {

            String[] lines = val.toString().split("\t"),
                     words = lines[0].split(" ");

            if (words.length>1){
                String w1 = words[0], w2 = words[1];
                Text text = new Text(), text1 = new Text();
                int occurs = Integer.parseInt(lines[1]);

                text.set(String.format("%s %s", w1,w2));
                text1.set(String.format("%d",occurs));

                if (words.length>2){
                    String w3 = words[2];
                    Text text2 = new Text(), text3 = new Text();

                    text2.set(String.format("%s %s %s %d" , w1,w2,w3,occurs));
                    text3.set(String.format("%s %s", w2,w3));

                    context.write(text3,text2);
                    context.write(text,text2);
                }
                else {
                    context.write(text,text1);
                }

            }
        }
    }

    private static class MyReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce (Text key,Iterable<Text> values, Context context) throws IOException, InterruptedException {
           String[] words = key.toString().split(" ");
           String w1 = words[0], w2 = words[1];
           Text newKey = new Text(), newValue = new Text();
           long  totalOccurs = 0;
           boolean flag1 = false, flag2 = false;

           for (Text value : values ){
               String[] val = value.toString().split(" ");

               if (val.length > 2){
                   String word1 = val[0], word2 = val[1], word3 = val[2];
                   newKey.set(String.format("%s %s %s", word1, word2, word3));
                   flag1 = true;
               }
               else{
                   totalOccurs = Long.parseLong(val[0]);
                   newValue.set(String.format("%s %s %d" , w1,w2,totalOccurs));
                   flag2 = true;
               }
               if (flag1 && flag2){
                   context.write(newKey,newValue);
                   flag1 = false;
               }
           }
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
        job.setJarByClass(StepFour.class);
        job.setMapperClass(StepFour.MyMapper.class);
        job.setReducerClass(StepFour.MyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setPartitionerClass(StepFour.MyPartitioner.class);
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
