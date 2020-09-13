import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;

import java.io.IOException;
import java.util.HashSet;

public class StepOne {
    private static class MyMapper extends Mapper<LongWritable, Text,Text, Text>{

        public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException {
            HashSet<String> words = new HashSet<>();
            String[] line = val.toString().split("\t");
            System.out.println("line.length : " + line.length);
            String[] words2 = line[0].split(" ");

            for (String w : words2){
                if (!words.contains(w)){
                    if (!(w.equals("*"))){
                        int occurs = Integer.parseInt(line[2]);
                        Text text = new Text(), text1 = new Text(), text2 = new Text();
                        text.set(String.format("%s",w));
                        text1.set(String.format("%d",occurs));
                        text2.set("*");
                        words.add(w);
                        context.write(text,text1);
                        context.write(text2,text1);
                    }
                }
            }

        }
    }

    private static class MyReducer extends Reducer<Text, Text, Text, Text>{
        public void reduce (Text key,Iterable<Text> values, Context context) throws IOException, InterruptedException {

            String w1 = key.toString();
            Text newKey = new Text(), newValue = new Text();
            long totalOccurs = 0L ;

            for (Text value : values) {
                totalOccurs += Long.parseLong(value.toString());
            }

            newKey.set(String.format("%s",w1));
            newValue.set(String.format("%d",totalOccurs));

            context.write(newKey,newValue);

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
        job.setJarByClass(StepOne.class);
        job.setMapperClass(MyMapper.class);
        job.setCombinerClass(MyReducer.class);
        job.setReducerClass(MyReducer.class);
        job.setNumReduceTasks(1);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setPartitionerClass(StepOne.MyPartitioner.class);
        job.setOutputFormatClass(TextOutputFormat.class);
        //TODO change the input format
        job.setInputFormatClass(SequenceFileInputFormat.class);
        SequenceFileInputFormat.addInputPath(job, new Path(args[0]));
//        job.setInputFormatClass(TextInputFormat.class);
//        FileInputFormat.setInputPaths(job, new Path(args[0]));
        String output=args[1];
        FileOutputFormat.setOutputPath(job, new Path(output));
        job.waitForCompletion(true);
    }
}
