/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
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

package org.cogroo.cmdline.chunker2;

import java.io.File;
import java.io.IOException;

import opennlp.tools.chunker.ChunkSample;
import opennlp.tools.cmdline.AbstractTrainerTool;
import opennlp.tools.cmdline.CmdLineUtil;
import opennlp.tools.cmdline.TerminateToolException;
import opennlp.tools.cmdline.params.TrainingToolParams;
import opennlp.tools.util.model.ModelUtil;

import org.cogroo.cmdline.chunker2.Chunker2TrainerTool.TrainerToolParams;
import org.cogroo.tools.chunker2.ChunkerFactory;
import org.cogroo.tools.chunker2.ChunkerME;
import org.cogroo.tools.chunker2.ChunkerModel;

public class Chunker2TrainerTool
    extends AbstractTrainerTool<ChunkSample, TrainerToolParams> {
  
  interface TrainerToolParams extends TrainingParams, TrainingToolParams {
  }

  public Chunker2TrainerTool() {
    super(ChunkSample.class, TrainerToolParams.class);
  }

  public String getShortDescription() {
    return "trainer for the learnable chunker";
  }

  public void run(String format, String[] args) {
    super.run(format, args);

    mlParams = CmdLineUtil.loadTrainingParameters(params.getParams(), false);
    if(mlParams == null) {
      mlParams = ModelUtil.createTrainingParameters(params.getIterations(),
          params.getCutoff());
    }

    File modelOutFile = params.getModel();
    CmdLineUtil.checkOutputFile("sentence detector model", modelOutFile);

    ChunkerModel model;
    try {
      ChunkerFactory chunkerFactory = ChunkerFactory
          .create(params.getFactory());
      model = ChunkerME.train(factory.getLang(), sampleStream, mlParams,
          chunkerFactory);
    } catch (IOException e) {
      throw new TerminateToolException(-1, "IO error while reading training data or indexing data: " +
          e.getMessage(), e);
    }
    finally {
      try {
        sampleStream.close();
      } catch (IOException e) {
        // sorry that this can fail
      }
    }
    
    CmdLineUtil.writeModel("chunker", modelOutFile, model);
  }
}
