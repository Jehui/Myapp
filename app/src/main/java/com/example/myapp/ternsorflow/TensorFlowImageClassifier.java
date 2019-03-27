/* Copyright 2016 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/

package com.example.myapp.ternsorflow;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ThumbnailUtils;
import android.os.Trace;
import android.util.Log;

import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import com.example.myapp.ternsorflow.TensorFlowImageClassifier;

/** A classifier specialized to label images using TensorFlow. */
public class TensorFlowImageClassifier implements Classifier {
    private static final String TAG = "TensorFlowImageClassifier";

    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 3;
    private static final float THRESHOLD = -100000f;

    // Config values.
    private String inputName;
    private String outputName;
    private int inputSize;
    private int imageMean;
    private float imageStd;

    // Pre-allocated buffers.
    private Vector<String> labels = new Vector<String>();
    private int[] intValues;
    private float[] floatValues;
    private float[] outputs;
    private String[] outputNames;

    private boolean logStats = false;

    private TensorFlowInferenceInterface inferenceInterface;

    private TensorFlowImageClassifier() {}

    /**
     * Initializes a native TensorFlow session for classifying images.
     *
     * @param assetManager The asset manager to be used to load assets.
     * @param modelFilename The filepath of the model GraphDef protocol buffer.
     * @param labelFilename The filepath of label file for classes.
     * @param inputSize The input size. A square image of inputSize x inputSize is assumed.
     * @param imageMean The assumed mean of the image values.
     * @param imageStd The assumed std of the image values.
     * @param inputName The label of the image input node.
     * @param outputName The label of the output node.
     * @throws IOException
     */
    @SuppressLint("LongLogTag")
    public static Classifier create(
            AssetManager assetManager,
            String modelFilename,
            String labelFilename,
            int inputSize,
            int imageMean,
            float imageStd,
            String inputName,
            String outputName) {
        // 1 构造TensorFlowImageClassifier分类器，inputName和outputName分别为模型输入节点和输出节点的名字
        TensorFlowImageClassifier c = new TensorFlowImageClassifier();
        c.inputName = inputName;
        c.outputName = outputName;
// 2 读取label文件内容，将内容设置到出classifier的labels数组中
        // Read the label names into memory.
        // TODO(andrewharp): make this handle non-assets.
        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        Log.i(TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;//读入文件中的字符
        try {
// 读取label文件流，label文件表征了可以识别出来的物体分类。我们预测的物体名称就是其中之一。
            br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
          // 将label存储到TensorFlowImageClassifier的labels数组中
            String line;
            while ((line = br.readLine()) != null) {
                c.labels.add(line);

            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!" , e);
        }
// 3 读取model文件名，并设置到classifier的interface变量中
        c.inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);//加载模型
// 4 利用输出节点名称，获取输出节点的shape，也就是最终分类的数目。
// 输出的shape为二维矩阵[N, NUM_CLASSES], N为batch size，也就是一批训练的图片个数。NUM_CLASSES为分类个数
// The shape of the output is [N, NUM_CLASSES], where N is the batch size.
        final Operation operation = c.inferenceInterface.graphOperation(outputName);
        final int numClasses = (int) operation.output(0).shape().size(1);
        Log.i(TAG, "Read " + c.labels.size() + " labels, output layer size is " + numClasses);

        // Ideally, inputSize could have been retrieved from the shape of the input operation.  Alas,
        // the placeholder node for input in the graphdef typically used does not specify a shape, so it
        // must be passed in as a parameter.
        // 5. 设置分类器的其他变量
        c.inputSize = inputSize; // 物体分类预测时输入图片的尺寸。也就是相机原始图片裁剪后的图片。默认为224*224
        c.imageMean = imageMean; // 像素点RGB通道的平均值，默认为117。用来将0~255的数值做归一化的
        c.imageStd = imageStd;// 像素点RGB通道的归一化比例，默认为1
// 6. 分配Buffer给输出变量
        // Pre-allocate buffers.
        c.outputNames = new String[] {outputName}; // 输出节点名字
        c.intValues = new int[inputSize * inputSize];
        c.floatValues = new float[inputSize * inputSize * 3]; // RGB三通道
        c.outputs = new float[numClasses]; // 预测完的结果，也就是图片对应到每个分类的概率。我们取概率最大的前三个显示在app中

        return c;
    }


    @SuppressLint("LongLogTag")
    @Override
    public List<Recognition> recognizeImage(final Bitmap bitmap) {
        // Log this method so that it can be analyzed with systrace.
        // 1 预处理输入图片，读取像素点，并将RGB三通道数值归一化. 归一化后分布于 -117 ~ 138
        Trace.beginSection("recognizeImage");

        Trace.beginSection("preprocessBitmap");
        // Preprocess the image data from 0-255 int to normalized float based
        // on the provided parameters.
//        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
//        for (int i = 0; i < intValues.length; ++i) {
//            final int val = intValues[i];
//            floatValues[i * 3 + 0] = (((val >> 16) & 0xFF) - imageMean) / imageStd;
//            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF) - imageMean) / imageStd;
//            floatValues[i * 3 + 2] = ((val & 0xFF) - imageMean) / imageStd;
//        }
        float[] floatValues=getPixels(bitmap);

        Trace.endSection();
        // 2 将输入数据填充到TensorFlow中，并feed数据给模型
        // inputName为输入节点
        // floatValues为输入tensor的数据源，
        // dims构成了tensor的shape, [batch_size, height, width, in_channel], 此处为[1, inputSize, inputSize, 3]
        // Copy the input data into TensorFlow.
        Trace.beginSection("feed");
        inferenceInterface.feed(inputName, floatValues, 1, inputSize, inputSize, 3);
        Trace.endSection();
        // 3 跑TensorFlow预测模型
        // outputNames为输出节点名， 通过session来run tensor
        // Run the inference call.
        Trace.beginSection("run");
        inferenceInterface.run(outputNames, logStats);
        Trace.endSection();
        // 4 将tensorflow预测模型输出节点的输出值拷贝出来
        // 找到输出节点outputName的tensor，并复制到outputs中。outputs为分类预测的结果，是一个一维向量，每个值对应labels中一个分类的概率。
        // Copy the output Tensor back into the output array.
        Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, outputs);
        Log.d(TAG, "定位 "+String.valueOf(outputs[0])+"  "+String.valueOf(outputs[1])
                +"  "+String.valueOf(outputs[2])+"  "+String.valueOf(outputs[3])+"  "+String.valueOf(outputs[4])
                +"  "+String.valueOf(inputSize)
        );
        Trace.endSection();
        // 5 得到概率最大的前三个分类，并组装为Recognition对象
        // Find the best classifications.
        PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        1,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(Recognition lhs, Recognition rhs) {
                                // Intentionally reversed to put high confidence at the head of the queue.
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });

        for (int i = 0; i < outputs.length; ++i) {
            if (outputs[i] > THRESHOLD) {
                pq.add(
                        new Recognition(
                                "" + i, labels.size() > i ? labels.get(i) : "unknown", outputs[i], null));
            }
        }

        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        int recognitionsSize = Math.min(pq.size(), MAX_RESULTS);

        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        Trace.endSection(); // "recognizeImage"
        return recognitions;
    }

    private float[] getPixels(Bitmap bitmap){
        int []intValues=new int[inputSize*inputSize];
        float[]floatValues=new float[inputSize*inputSize*3];
        if(bitmap.getWidth()!=inputSize||bitmap.getHeight()!=inputSize){
            bitmap= ThumbnailUtils.extractThumbnail(bitmap,inputSize,inputSize);
        }

        bitmap.getPixels(intValues,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());

        for (int i=0;i<intValues.length;i++){
            final  int val=intValues[i];
            floatValues[i*3]= Color.red(val)/255.0f;
            floatValues[i*3+1]=Color.green(val)/255.0f;
            floatValues[i*3+2]=Color.blue(val)/255.0f;
        }
        return floatValues;
    }

    @Override
    public void enableStatLogging(boolean logStats) {
        this.logStats = logStats;
    }

    @Override
    public String getStatString() {
        return inferenceInterface.getStatString();
    }

    @Override
    public void close() {
        inferenceInterface.close();
    }


}