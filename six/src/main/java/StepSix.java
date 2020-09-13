import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;

public class StepSix {
    private static class MyMapper extends Mapper<LongWritable, Text,Text, Text> {
        public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException {

            String[] lines = val.toString().split("\t");
            Text newKey = new Text() , newVal = new Text();
            String w1 = lines[0] , w2 = lines[1];

            newKey.set(String.format("%s %s",w1,w2));
            newVal.set(String.format("%s" , ""));

            context.write(newKey,newVal);
        }
    }

    private static class MyReducer extends Reducer<Text, Text, Text, Text> {


        public void reduce (Text key,Iterable<Text> values, Context context) throws IOException, InterruptedException {

            String w1 = key.toString();
            Text newKey = new Text() , newVal = new Text();

            newKey.set(String.format("%s" , w1));
            newVal.set(String.format("%s", ""));

            context.write(newKey,newVal);

        }
    }

    private static class MyCompare extends WritableComparator{
        public MyCompare() {
            super(Text.class,true);
        }

        public int compare(WritableComparator key1 , WritableComparator key2){

            String[] words1 = key1.toString().split(" "),
                     words2 = key2.toString().split(" ");
            String w1 = words1[0] , w2 = words1[1] ,
                   word1 = words2[0] , word2 = words2[1];
            Double occurs1 = Double.parseDouble(words1[3]),
                 occurs2 =Double.parseDouble(words2[3]);

            return (w1.equals(word1) && w2.equals(word2))?
                    (occurs1>occurs2)? -1 :1 :
                    (w1 + " " +w2).compareTo( word1 + " " + word2);
            }


        }



    public static void main(String[] args) throws IOException,ClassNotFoundException, InterruptedException {
        Configuration configuration = new Configuration();
        Job job =Job.getInstance(configuration);
        job.setJarByClass(StepSix.class);
        job.setMapperClass(StepSix.MyMapper.class);
        job.setReducerClass(StepSix.MyReducer.class);
        job.setNumReduceTasks(1);
        job.setSortComparatorClass(StepSix.MyCompare.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        job.setInputFormatClass(TextInputFormat.class);
        String input1=args[0];
        String output=args[1];
        MultipleInputs.addInputPath(job, new Path(input1), TextInputFormat.class);
        FileOutputFormat.setOutputPath(job, new Path(output));
        job.waitForCompletion(true);
    }
}
