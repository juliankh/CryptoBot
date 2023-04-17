package com.cb.sandbox.ml;

import com.cb.common.ObjectConverter;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbProvider;
import com.cb.model.CbOrderBook;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.deeplearning4j.datasets.iterator.utilty.ListDataSetIterator;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.knowm.xchange.currency.CurrencyPair;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.time.Instant;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class ModelExp4 {

    private final ObjectConverter objectConverter = new ObjectConverter();

    @SneakyThrows
    public static void main(String[] args) {
        (new ModelExp4()).doStuff();
    }

    public void doStuff() {
        // get data from db
        DbProvider dbProvider = new DbProvider();
        Instant from = TimeUtils.instant(2023, Month.APRIL, 12, 17, 30, 45);
        Instant to = TimeUtils.instant(2023, Month.APRIL, 12, 17, 32, 45);
        List<CbOrderBook> orderBooks = dbProvider.retrieveKrakenOrderBooks(CurrencyPair.BTC_USDT, from, to);

        // generate model
        Pair<MultiLayerNetwork, Long> result = generateAndPersistModel(orderBooks);
        MultiLayerNetwork multiLayerNetwork = result.getLeft();
        long minsDuration = result.getRight();
        log.info("Took " + minsDuration + " mins to generate");

        // test the model
        double[] inputArray = objectConverter.primitiveArray(orderBooks.get(100).getBids().values());
        try (INDArray inputIndArray = Nd4j.create(inputArray, new int[]{1, inputArray.length})) {
            INDArray output = multiLayerNetwork.output(inputIndArray, false);
            List<Double> outputList = Arrays.stream(output.toDoubleVector()).boxed().toList();
            System.out.println(outputList);
        }
    }

    public Pair<MultiLayerNetwork, Long> generateAndPersistModel(Collection<CbOrderBook> orderBooks) {
        DataSetIterator trainingIterator = getDerivedDeltasDataSetIterator(orderBooks, 200);
        return generateModel(trainingIterator, ModelParams.defaultParams());
    }

    public DataSetIterator getDerivedDeltasDataSetIterator(Collection<CbOrderBook> data, int batchSize) {
        // TODO: change how the features and targets get determined
        List<List<Double>> featureLists = data.parallelStream().map(orderBook -> orderBook.getBids().values().stream().toList()).toList();
        List<List<Double>> targetLists = data.parallelStream().map(orderBook -> List.of(orderBook.getBids().values().stream().mapToDouble(Double::doubleValue).average().getAsDouble())).toList();

        double[][] featureMatrix = objectConverter.matrixOfDoubles(featureLists);
        double[][] targetMatrix = objectConverter.matrixOfDoubles(targetLists);
        INDArray input = Nd4j.create(featureMatrix);
        INDArray output = Nd4j.create(targetMatrix);
        DataSet dataSet = new DataSet(input, output);

        // TODO: normalize using NormalizerMinMaxScaler

        List<DataSet> listDs = dataSet.asList();
        Collections.shuffle(listDs);
        return new ListDataSetIterator<>(listDs, batchSize);
    }

    private Pair<MultiLayerNetwork, Long> generateModel(DataSetIterator trainingIterator, ModelParams modelParams) {
        log.info("Starting to build the model using: " + modelParams);
        int layerNum = 0;
        MultiLayerNetwork model = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                .seed(modelParams.getSeed())
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(modelParams.getLearningRate(), 0.9)) // TODO: see what happens when you change this value
                .list()
                .layer(layerNum, new DenseLayer.Builder()
                        .activation(Activation.TANH)
                        //.nIn(modelParams.getInputNodes()).nOut(modelParams.getHiddenNodes()).build())
                        .nIn(500).nOut(modelParams.getHiddenNodes()).build())
                //.layer(++layerNum, new DenseLayer.Builder()
                //			 .activation(Activation.TANH)
                //			 .nIn(hiddenNodes).nOut(500).build())
                //.layer(++layerNum, new DenseLayer.Builder()
                //			 .activation(Activation.TANH)
            	//		     .nIn(500).nOut(100).build())
                .layer(++layerNum, new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.IDENTITY)
                        .nIn(modelParams.getHiddenNodes()).nOut(1).build())
                .build()
        );
        model.init();
        model.setListeners(new ScoreIterationListener(100));
        Instant before = Instant.now();
        model.fit(trainingIterator, modelParams.getEpochs());
        long durationMins = ChronoUnit.MINUTES.between(before, Instant.now());
        return Pair.of(model, durationMins);
    }

}
