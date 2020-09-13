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

public class StepThree {
    private static class MyMapper extends Mapper<LongWritable, Text,Text, Text> {
        public void map(LongWritable key, Text val, Context context) throws IOException, InterruptedException {

            String[] lines = val.toString().split("\t"),
                     words = lines[0].split(" ");

            if (words.length>2){

                String w1 = words[0], w2 = words[1], w3 = words[2];
                Text text = new Text(), text1 = new Text();
                int occurs = Integer.parseInt(lines[2]);

                text.set(String.format("%s %s %s",w1,w2,w3));
                text1.set(String.format("%d",occurs));

                context.write(text,text1);

            }
        }
    }

    private static class MyReducer extends Reducer<Text, Text, Text, Text> {
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
        job.setJarByClass(StepThree.class);
        job.setMapperClass(StepThree.MyMapper.class);
        job.setCombinerClass(StepThree.MyReducer.class);
        job.setReducerClass(StepThree.MyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        job.setPartitionerClass(StepThree.MyPartitioner.class);
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
