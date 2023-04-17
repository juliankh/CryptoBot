package com.cb.sandbox.ml;

public class ModelParams {

	private int inputNodes;
	private int hiddenNodes;
	private int epochs;
	private double learningRate;
	private int batchSize;
	private long seed;

	public static ModelParams defaultParams() {
		//return new ModelParams(2001, 3500, 70, 0.001, 200, 482571);
		return new ModelParams(1001, 952, 50, 0.001, 200, 482571);
	}

	public ModelParams(int inputNodes, int hiddenNodes, int epochs, double learningRate, int batchSize, long seed) {
		this.inputNodes = inputNodes;
		this.hiddenNodes = hiddenNodes;
		this.epochs = epochs;
		this.learningRate = learningRate;
		this.batchSize = batchSize;
		this.seed = seed;
	}

	public int getInputNodes() {
		return inputNodes;
	}

	public int getHiddenNodes() {
		return hiddenNodes;
	}

	public int getEpochs() {
		return epochs;
	}

	public double getLearningRate() {
		return learningRate;
	}

	public int getBatchSize() {
		return batchSize;
	}

	public long getSeed() {
		return seed;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + batchSize;
		result = prime * result + epochs;
		result = prime * result + hiddenNodes;
		result = prime * result + inputNodes;
		long temp;
		temp = Double.doubleToLongBits(learningRate);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (int) (seed ^ (seed >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ModelParams other = (ModelParams) obj;
		if (batchSize != other.batchSize)
			return false;
		if (epochs != other.epochs)
			return false;
		if (hiddenNodes != other.hiddenNodes)
			return false;
		if (inputNodes != other.inputNodes)
			return false;
		if (Double.doubleToLongBits(learningRate) != Double.doubleToLongBits(other.learningRate))
			return false;
		if (seed != other.seed)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ModelParams [inputNodes=" + inputNodes + ", hiddenNodes=" + hiddenNodes + ", epochs=" + epochs + ", learningRate=" + learningRate + ", batchSize=" + batchSize + ", seed=" + seed + "]";
	}

}
