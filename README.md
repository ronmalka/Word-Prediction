Knowledge base for Word Prediction
-------------------------------------------
Ron Malka - 203694013
The application generats a knowledge-base for Hebrew word-prediction system, based on Google 3-Gram Hebrew dataset, using Amazon Elastic Map-Reduce (EMR). 

The output is stored in the following bucket: s3://assignment2bucket1992/finaloutput.txt

Running instructions:
--------------------
1. Change the keyPair and bucketName fields in the Main.java file to be your key pair and bucket name for this run. In addition, change the log4jConfPath field to the appropriate path on your machine.
2. Store your AWS credentials in `~/.aws/credentials`
```
   [default]
   aws_access_key_id= ???
   aws_secret_access_key= ???
```
3. run using `java -jar ass2.jar

Steps summary:
-------------
1. Count the total occurrences of all the words in 1-gram corpus 
2. Count the occurrences of a word pair in 2-gram corpus.
3. Count the occurrences of a word triple in 3-gram corpus.
4. Calculate the produced knowledge-base indicates for a each pair of words the probability of their possible next words.
5. Sort the data.


Implemented a Comparator for step 6 in order to sort data (descending).

Implemented a Partitioner class that is responsible for distributing in steps1-5.

First Step:
----------
Mapper : take as input the 1-gram corpus and parse it liine by line, and create line that the key is the word and the value is the word's occurrence, and its creat another line with * as the key and the word's occurrence is the value.
Reducer: in the reducer we united all the occurences (the values) by the words (the keys). we also count the total words in the corpus by counting the values of *. 
   
Second Step:
-----------
Mapper : take as input the 2-gram corpus and parse it liine by line, and create line that the key is the two words and the value is the words occurrence.
Reducer: in the reducer we united all the occurences (the values) by the words (the keys). 

Third Step:
----------
Mapper : take as input the 3-gram corpus and parse it liine by line, and create line that the key is the three words and the value is the words occurrence.
Reducer: in the reducer we united all the occurences (the values) by the words (the keys). 
    
Fourth Step:
-----------
Mappers : there is two mappers, one is for the output of step two, and the other is for the input of step three.
The input of the first mapper it's a pair that the key is two words and the value is the occurence, the mapper parse it that the key is the words and the value is the occurence.
The input of the second mapper it's a pair that the key is three words and the value is the occurence, the mapper take the three words and split it into two pairs,
the first pair is the first and the second words and the second pair is the second and the third words. the pairs will be the keys and the three words and the occurence will be the values. 
Reducer: in the reducer we combine the information from the mappers that the three words will become the key, and the value will be the pair and it's occurence.   
    
Fifth Step:
----------
Mappers: there is two mappers, one is for the output of step three, and the other is for the input of step four.
The input of the first mapper it's a pair that the key is three words and the value is the occurence, the mapper parse it that the key is the words and the value is the occurence.
The input of the second mapper it's a pair that the key is three words and the value is two words and the occurence, the mapper parse it that the key is the words and the value is two words and the occurence.
Reducer: firs we start with setup function, that loads the word and its occurence from the ouput of step one, including the total words in the corpus.
in the reducer we calcluate the probability of the apperance of the three words in the corpus. We calculate it using the formula provided on the assignment page.
the key will be the three words and the value will be the probability.

Sixth Step:
----------
Mapper: take as input the output of step five, and the output will be the same key and value sorted by the probability.
Compare: we compare two string, if the first two words is the same, we return the one with the higer probability.
Reducer: the output will be the same key and value as the input.

Main:
-------
The main create the steps above and send it to a cluster that we creat.

Statistics:
----------

Step one: 
-----------

With local aggregation:
---------------------------
Map input records=44400490
Map output records=88800536
Combine input records=88800536
Combine output records=645290
Reduce input records=645290
Reduce output records=645262

Step two: 
-----------

With local aggregation:
---------------------------
Map input records=252069581
Map output records=233334882
Combine input records=233334882
Combine output records=4758948
Reduce input records=4758948
Reduce output records=4758948


Step three: 
------------

With local aggregation:
---------------------------
Map input records=163471963
Map output records=119255104
Combine input records=119255104
Combine output records=2804000
Reduce input records=2804000
Reduce output records=2803960

Step Four: 
------------

Without local aggregation:
---------------------------
Map input records=7562834
Map output records=10366794
Combine input records=0
Combine output records=0
Reduce input records=10366794
Reduce output records=4729970	

Step Five: 
-----------

Without local aggregation:
---------------------------
Map input records=8179192
Map output records=8179192
Combine input records=0
Combine output records=0
Reduce input records=8179192
Reduce output records=2786887

Step Six: 
-----------

Without local aggregation:
---------------------------
Map input records=2786887
Map output records=2786887
Combine input records=0
Combine output records=0
Reduce input records=2786887
Reduce output records=2786591

Analysis:	
-----------
example1 "את עול"
את עול האחריות 0.009674
את עול האמונה 0.003102
את עול הברזל 0.002705
את עול ההוצאות 0.000558	
את עול החינוך 0.000529

example2: "כאילו היו"

כאילו היו אלה 0.034342
כאילו היו אלו 0.004888
כאילו היו בני 0.004484
כאילו היו אנשים 0.003146
כאילו היו אחים 0.002320

example3: "אל ארץ"

אל ארץ אבותיהם 0.008860	
אל ארץ אבותיו 0.007083
אל ארץ אבותיכם 0.005199
אל ארץ אדום 0.002565	
אל ארץ אהרת 0.002217

example4: "זו הייתה"

זו הייתה גם 0.021695
זו הייתה אחת 0.013202
זו הייתה אמורה 0.005914
זו הייתה בעלת 0.003100		
זו הייתה דרך 0.001699

example5: "כל מעשה"

כל מעשה בראשית 0.080797	
כל מעשה אשר 0.013739
כל מעשה האדם 0.013690
כל מעשה גדול 0.002196
כל מעשה האלהים 0.001063

example6: "לא כמו"

לא כמו אצל 0.010497
לא כמו אלה 0.005410
לא כמו בבית 0.003395
לא כמו אז 0.003152	
לא כמו אל 0.003019

example7: "לשלם לה"

לשלם לה את 0.130419		
לשלם לה כתובתה 0.042740	
לשלם לה דמי 0.025590	
לשלם לה שכר 0.024732
לשלם לה כל 0.018667	

example8: "מי שיאמר"

מי שיאמר כי 0.059868
מי שיאמר קדיש 0.023007	
מי שיאמר להם 0.021910
מי שיאמר דבר 0.012847				
מי שיאמר לי 0.010421	
	
example9: "ניתן לשער"
		
ניתן לשער כי 0.154084
ניתן לשער את 0.028571	
ניתן לשער מה 0.012732	
ניתן לשער על 0.006206	
ניתן לשער שהיה 0.005730	

example10: "ראשי הקהל"
	
ראשי הקהל את 0.014190	
ראשי הקהל היו 0.013735
ראשי הקהל על 0.011291	
ראשי הקהל לא 0.010849		
ראשי הקהל של 0.007937	



