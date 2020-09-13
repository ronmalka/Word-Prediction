import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduce;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClient;
import com.amazonaws.services.elasticmapreduce.AmazonElasticMapReduceClientBuilder;
import com.amazonaws.services.elasticmapreduce.model.*;

public class Main {

    private final static String
            REGION = "us-east-1", KEY_NAME = "rmaKeysPair",
            JAR = "s3n://assignment2bucket1992/jars/",
            JAR1 ="s3n://assignment2bucket1992/jars1/",
            JAR2 ="s3n://assignment2bucket1992/jars2/",
            JAR3 ="s3n://assignment2bucket1992/jars3/",
            OUTPUT = "s3n://assignment2bucket1992/output/",
            LOGS = "s3n://assignment2bucket1992/logs/",
            DATA_SET_1GRAM = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/1gram/data",
            DATA_SET_2GRAM = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/2gram/data",
            DATA_SET_3GRAM = "s3://datasets.elasticmapreduce/ngrams/books/20090715/heb-all/3gram/data",
            TERMINATE = "TERMINATE_JOB_FLOW";


    public static void main(String[] args) {
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new ProfileCredentialsProvider().getCredentials());
        AmazonElasticMapReduce mapReduce =AmazonElasticMapReduceClientBuilder
                .standard()
                .withRegion(REGION)
                .withCredentials(credentialsProvider)
                .build();

        System.out.println("create emr");
//
//        HadoopJarStepConfig stepOneConfig = new HadoopJarStepConfig()
//                .withJar(JAR + "one.jar")
//                .withMainClass("StepOne")
//                .withArgs(DATA_SET_1GRAM, OUTPUT + "outputStepOne/");
//
//        StepConfig stepOne = new StepConfig()
//                .withName("StepOne")
//                .withHadoopJarStep(stepOneConfig)
//                .withActionOnFailure(TERMINATE);
//
//        System.out.println("create step one");
//
//        HadoopJarStepConfig stepTwoConfig = new HadoopJarStepConfig()
//                .withJar(JAR + "two.jar")
//                .withMainClass("StepTwo")
//                .withArgs(DATA_SET_2GRAM, OUTPUT + "outputStepTwo/");
//
//        StepConfig stepTwo = new StepConfig()
//                .withName("StepTwo")
//                .withHadoopJarStep(stepTwoConfig)
//                .withActionOnFailure(TERMINATE);
//
//        System.out.println("create step two");
//
//        HadoopJarStepConfig stepThreeConfig = new HadoopJarStepConfig()
//                .withJar(JAR + "three.jar")
//                .withMainClass("StepThree")
//                .withArgs(DATA_SET_3GRAM , OUTPUT + "outputStepThree/");
//
//        StepConfig stepThree = new StepConfig()
//                .withName("StepThree")
//                .withHadoopJarStep(stepThreeConfig)
//                .withActionOnFailure(TERMINATE);
//
//        System.out.println("create step three");
//
//        HadoopJarStepConfig stepFourConfig = new HadoopJarStepConfig()
//                .withJar(JAR + "four.jar")
//                .withMainClass("StepFour")
//                .withArgs(OUTPUT + "outputStepTwo/",OUTPUT + "outputStepThree/",OUTPUT + "outputStepFour/");
//
//        StepConfig stepFour = new StepConfig()
//                .withName("StepFour")
//                .withHadoopJarStep(stepFourConfig)
//                .withActionOnFailure(TERMINATE);
//
//        System.out.println("create step four");

        HadoopJarStepConfig stepFiveConfig = new HadoopJarStepConfig()
                .withJar(JAR + "five.jar")
                .withMainClass("StepFive")
                .withArgs(OUTPUT + "outputStepThree/",OUTPUT + "outputStepFour/", OUTPUT + "outputStepFive/");

        StepConfig stepFive = new StepConfig()
                .withName("StepFive")
                .withHadoopJarStep(stepFiveConfig)
                .withActionOnFailure(TERMINATE);

        System.out.println("create step five");

        HadoopJarStepConfig stepSixConfig = new HadoopJarStepConfig()
                .withJar(JAR + "six.jar")
                .withMainClass("StepSix")
                .withArgs(OUTPUT + "outputStepFive/", OUTPUT + "outputStepSix/");

        StepConfig stepSix = new StepConfig()
                .withName("StepSix")
                .withHadoopJarStep(stepSixConfig)
                .withActionOnFailure(TERMINATE);

        System.out.println("create step six");

        JobFlowInstancesConfig instances = new JobFlowInstancesConfig()
                .withInstanceCount(2)
                .withMasterInstanceType(InstanceType.M1Large.toString())
                .withSlaveInstanceType(InstanceType.M1Large.toString())
                .withHadoopVersion("2.7.3")
                .withEc2KeyName(KEY_NAME)
                .withKeepJobFlowAliveWhenNoSteps(false)
                .withPlacement(new PlacementType("us-east-1a"));

        System.out.println("create instances");

        RunJobFlowRequest runFlowRequest = new RunJobFlowRequest()
                .withName("Assignment2")
                .withInstances(instances)
                //.withSteps(stepOne,stepTwo,stepThree,stepFour,stepFive,stepSix)
                .withSteps(stepFive,stepSix)
                .withLogUri(LOGS)
                .withServiceRole("EMR_DefaultRole")
                .withJobFlowRole("EMR_EC2_DefaultRole")
                .withReleaseLabel("emr-5.11.0");

        System.out.println("create runFlowRequest");

        RunJobFlowResult runJobFlowResult = mapReduce.runJobFlow(runFlowRequest);
        String jobFlowId = runJobFlowResult.getJobFlowId();
        System.out.println("cluster id : " + jobFlowId);
    }



}
