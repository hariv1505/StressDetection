package com.hari.se4911.stresstester.results;

import java.util.ArrayList;

public class StressResult {

	private float[] avgCountTurns;
	private float avgHydro;
	private float[] avgVoice;
	private float y;
	
	public StressResult() {
		initResults();
	}

	public StressResult(int[] accelRes,
			ArrayList<float[]> hydroRes, 
			ArrayList<Short> voiceRes) throws NoResultsException {
		initResults();
		analyzeAccel(accelRes);
		analyzeHydro(hydroRes);
		analyzeVoice(voiceRes);
		y = 0;
	}
	
	private void initResults() {
		avgCountTurns = new float[2];
		avgHydro = 0;
		avgVoice = new float[2];
		y = 0;
	}
	
	public void analyze(DataAnalyzer da) throws NoResultsException {
		this.y = da.substitute(this);
	}

	
	private void analyzeAccel(int[] accelRes) throws NoResultsException {
		int[] toComp = {0, 0, 0};
		if (accelRes == toComp) throw new NoResultsException();
		else {
			avgCountTurns[0] = (float)(accelRes[0]/accelRes[2]);
			avgCountTurns[1] = (float)(accelRes[1]/accelRes[2]);
		}
		
	}

	private void analyzeHydro(ArrayList<float[]> hydroRes) throws NoResultsException {
		if (hydroRes == null) throw new NoResultsException();
		else {
			int sum = 0;
			int total = hydroRes.size();
			for (float[] h: hydroRes) {
				sum += h[0];
			}
			avgHydro = (float) (sum/total);	
		}
		
	}

	private void analyzeVoice(ArrayList<Short> voiceRes) throws NoResultsException {
		if (voiceRes == null) throw new NoResultsException();
		else if (voiceRes.equals(new ArrayList<Short>())) {
			avgVoice[0] = -1;
			avgVoice[1] = -1;
		}
		else {
			int countShout = 0;
			int sum = 0;
			int total = voiceRes.size();
			for (Short v: voiceRes) {
				if (Math.abs(v) >= 75) {
					countShout++;
				}
				sum += Math.abs(v);
			}
			avgVoice[0] = (float) (countShout/total);
			avgVoice[1] = (float) (sum/total);
		}
		
	}
	
	/*
	 * *********************GETTERS AND SETTERS**********************
	 */

	public float[] getAvgCountTurns() {
		return avgCountTurns;
	}

	public void setAvgCountTurns(float[] avgCountTurns) {
		this.avgCountTurns = avgCountTurns;
	}

	public float getAvgHydro() {
		return avgHydro;
	}

	public void setAvgHydro(float avgHydro) {
		this.avgHydro = avgHydro;
	}

	public float[] getAvgVoice() {
		return avgVoice;
	}

	public void setAvgVoice(float[] avgVoice) {
		this.avgVoice = avgVoice;
	}

	public boolean isStressed() {
		return y >= 0;
	}

	/*
	 * ***********************FOR DataAnalyzer***********************
	 */
	
	public void setAccel(String xAvg, String yAvg) 
			throws NumberFormatException {
		float[] avgs = new float[2];
		avgs[0] = Float.parseFloat(xAvg);
		avgs[1] = Float.parseFloat(yAvg);
		setAvgCountTurns(avgs);
		
	}

	public void setHydro(String next) throws NumberFormatException {
		setAvgHydro(Float.parseFloat(next));
		
	}

	public void setVoice(String fracShout, String avgAmp) 
			throws NumberFormatException {
		float[] data = new float[2];
		data[0] = Float.parseFloat(fracShout);
		data[1] = Float.parseFloat(avgAmp);
		setAvgVoice(data);
		
	}

	public void setY(String next) {
		this.y = Integer.parseInt(next);
		
	}

}
