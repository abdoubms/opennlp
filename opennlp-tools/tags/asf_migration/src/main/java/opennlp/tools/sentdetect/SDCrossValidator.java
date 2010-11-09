/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreemnets.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opennlp.tools.sentdetect;

import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.eval.CrossValidationPartitioner;
import opennlp.tools.util.eval.FMeasure;

/**
 * 
 */
public class SDCrossValidator {
  
  private final String languageCode;
  private final int cutoff;
  private final int iterations;
  
  private FMeasure fmeasure = new FMeasure();
  
  public SDCrossValidator(String languageCode, int cutoff, int iterations) {
    this.languageCode = languageCode;
    this.cutoff = cutoff;
    this.iterations = iterations;
  }
  
  public SDCrossValidator(String languageCode) {
    this(languageCode, 5, 100);
  }
  
  public void evaluate(ObjectStream<SentenceSample> samples, int nFolds) throws IOException {
    
    CrossValidationPartitioner<SentenceSample> partitioner = 
        new CrossValidationPartitioner<SentenceSample>(samples, nFolds);
    
   while (partitioner.hasNext()) {
     
     CrossValidationPartitioner.TrainingSampleStream<SentenceSample> trainingSampleStream =
         partitioner.next();
     
      SentenceModel model = SentenceDetectorME.train(languageCode, trainingSampleStream, true, null, cutoff, iterations);
      
      // do testing
      SentenceDetectorEvaluator evaluator = new SentenceDetectorEvaluator(
          new SentenceDetectorME(model));

      evaluator.evaluate(trainingSampleStream.getTestSampleStream());
      
      fmeasure.mergeInto(evaluator.getFMeasure());
    }
  }
  
  public FMeasure getFMeasure() {
    return fmeasure;
  }
  
  @Deprecated
  public static void main(String[] args) throws Exception {
    
    SDCrossValidator cv = new SDCrossValidator("en");
    
    cv.evaluate(new SentenceSampleStream(new PlainTextByLineStream(
       new FileInputStream("/home/joern/Infopaq/opennlp.data/en/eos/eos.all").getChannel(),
        "ISO-8859-1")), 10);
    
    System.out.println(cv.getFMeasure().toString());
  }
}
