package com.hari.se4911.stresstester;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hari.se4911.stresstester.recorders.AcceleratorRecorder;
import com.hari.se4911.stresstester.recorders.HygrometerRecorder;
import com.hari.se4911.stresstester.recorders.VoiceRecorder;
import com.hari.se4911.stresstester.recorders.sensors.StressSensorEventListener;
import com.hari.se4911.stresstester.results.DataAnalyzer;
import com.hari.se4911.stresstester.results.DataParser;
import com.hari.se4911.stresstester.results.NoResultsException;
import com.hari.se4911.stresstester.results.StressResult;

public class MainActivity extends ActionBarActivity {

	private SensorManager mSensorManager;
	private SensorEventListener mSensorListener;
	AcceleratorRecorder aa;
	HygrometerRecorder ha;
	VoiceRecorder va;
	
	File f;
	DataAnalyzer dataRes;
	StressResult currRes;
	private String permFolderPath = 
			Environment.getExternalStorageDirectory() + 
			File.separator + "StressDetection" + File.separator;
		
	private long ONE_MINUTE = 5*1000;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();
	}
	

	@Override
	protected void onResume() {
	    // Register a listener for the sensor.
		super.onResume();
		if (mSensorListener != null && mSensorManager != null) {
		    mSensorManager.registerListener(mSensorListener, 
		    		mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
		    		SensorManager.SENSOR_DELAY_NORMAL);
		    mSensorManager.registerListener(mSensorListener, 
		    		mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
		    		SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		if (va != null) {
			if (!va.isRecording()) {
				va.startRecord();	
			}
		}
	}
	
	@Override
	protected void onPause() {
		// Be sure to unregister the sensor when the activity pauses.
		super.onPause();
		stopSensors();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		/*int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}*/
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * ********************STRESS IMPLEMENTATION*********************
	 */

	private void initialize() {
		try {
			loadData("data.csv");
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Could not open basic file!"
					+ " Try restarting or re-installing the application.",
					Toast.LENGTH_LONG).show();
			dataRes = new DataAnalyzer();
		} catch (NumberFormatException ne) {
			ne.printStackTrace();
			Toast.makeText(getBaseContext(), "Invalid number data. "
					+ "Removing file.",
					Toast.LENGTH_LONG).show();
			Calendar now = Calendar.getInstance();
			StringBuilder sbr = new StringBuilder();
			sbr.append(now.get(Calendar.YEAR));
			sbr.append(now.get(Calendar.MONTH));
			sbr.append(now.get(Calendar.DATE));
			sbr.append("T");
			sbr.append(now.get(Calendar.HOUR)).append("-");
			sbr.append(now.get(Calendar.MINUTE)).append("-");
			sbr.append(now.get(Calendar.SECOND)).append("-");
			sbr.append(now.get(Calendar.MILLISECOND));
			sbr.append(".csv");
			
			File temp = new File(permFolderPath + sbr.toString());
			f.renameTo(temp);
			initialize();
		}
	}

	private void initSensors() {
		ha = new HygrometerRecorder();
		aa = new AcceleratorRecorder();
		
		mSensorManager = (SensorManager) this
		                .getSystemService(Context.SENSOR_SERVICE);
		mSensorListener = new StressSensorEventListener(aa, ha);

		mSensorManager.registerListener(mSensorListener, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(mSensorListener, 
				mSensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY),
				SensorManager.SENSOR_DELAY_NORMAL);
		
		va = new VoiceRecorder();
		va.startRecord();
	}

	private void loadData(String data) throws IOException {
		
		f = new File(permFolderPath + data);
		File dir = new File(permFolderPath);
		if (dir.mkdirs() || dir.isDirectory()) {
			if (!f.exists()) {
				createNewFile();
			}
			
			try {
				DataParser dp = new DataParser(f);
				dp.parse();
				dataRes = new DataAnalyzer(dp.getResults());
				dataRes.analyze();
			} catch (FileNotFoundException fe) {
				fe.printStackTrace();
				Toast.makeText(getBaseContext(), "Could not open data file! Make sure "
						+ "file is not in use. Try restarting or re-installing the "
						+ "application if problem persists.",
						Toast.LENGTH_LONG).show();
				createNewFile();
			}
		} else throw new IOException();
		
	}

	private void createNewFile() throws IOException {
	
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f, true)));
		BufferedReader br = new BufferedReader(
				new InputStreamReader(getAssets().open("data.csv")));

		String line;
		while ((line = br.readLine()) != null) {
			out.println(line);
		}
		
		Toast.makeText(getBaseContext(), 
				"File does not exist. Creating a "
				+ "new one...",
				Toast.LENGTH_LONG);
		
	    out.close();
						
	}


	public void testStress(View v) {
		initSensors();
		
		final TextView res = (TextView) findViewById(R.id.results);
		
		Timer tim = new Timer();
		TimerTask tm = new TimerTask() {
			
			@Override
			public void run() {
				stopSensors();
				
				String temp;
				try {
					long analysisTime = analyzeResults();
					temp = stringifyRes(analysisTime);
				} catch (NoResultsException e) {
					e.printStackTrace();
					temp = "Error in generated results.";
				}
				
				final String toPrint = new String(temp);
				
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						res.setText(toPrint);
						askIfCorrect();
					}
				});
			}
		};
		
		tim.schedule(tm, ONE_MINUTE);
		
		res.setText("Detecting stress...");
		
	}
	
	protected void askIfCorrect() {
		String stressAns;
		if (currRes.isStressed()) stressAns = "YES!";
		else stressAns = "No.";
		
		new AlertDialog.Builder(MainActivity.this)
		.setTitle("Done")
		.setMessage("Are you stressed? "+ stressAns + "\nAre you really though?")
		.setNegativeButton("No", isNotStressed())
		.setPositiveButton("Yes", isStressed())
		.show();
		
	}


	private OnClickListener isStressed() {
		return new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				currRes.setY("1");
				writeNewDataToFile();
			}
		};
	}


	private OnClickListener isNotStressed() {
		return new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				currRes.setY("-1");
				writeNewDataToFile();
			}
		};
	}
	
	protected void writeNewDataToFile() {
		try {
			currRes.writeToFile(permFolderPath + "data.csv");
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(getBaseContext(), "Did not write out! If problems "
					+ "persist, make sure file is"
					+ "not in use and restart the app.",
					Toast.LENGTH_LONG).show();
		}
	}


	protected void stopSensors() {
		if (mSensorListener != null && mSensorManager != null && 
				va != null) {
			mSensorManager.unregisterListener(mSensorListener);
		}
		
		if (va != null) {
			if (va.isRecording()) {
				va.stopRecord();	
			}
		}
	}

	private long analyzeResults() throws NoResultsException {
		Calendar start = Calendar.getInstance();
		currRes = new StressResult(aa.getResults(), ha.getResults(), 
				va.getResults());
		currRes.analyze(dataRes);
		Calendar end = Calendar.getInstance();
		
		long ans = end.getTimeInMillis() - start.getTimeInMillis(); 
		
		Log.v("MainActivity", "Analysis time: " + ans);
		return ans;
	}
	
	public String stringifyRes(long analysisTime) {
		StringBuilder sbr = new StringBuilder();
		sbr.append("Accelerometer turn count: ")
			.append("\t" + currRes.getAvgCountTurns()[0] + 
					" " + currRes.getAvgCountTurns()[1])
			.append("\n");
		sbr.append("Hygrometer average: ")
			.append(currRes.getAvgHydro())
			.append("\n");
		sbr.append("Average amplitude of voice: ")
			.append("\t" + currRes.getAvgVoice()[0] + " " +
					currRes.getAvgVoice()[1])
			.append("\n");
		sbr.append("\n");
		
		String stressAns;
		if (currRes.isStressed()) stressAns = "YES!";
		else stressAns = "No.";
		
		sbr.append("Are you stressed? ")
			.append(stressAns)
			.append("\n").append("\n");
		sbr.append("Time to analyse: ")
			.append(analysisTime);
		
		return sbr.toString();
		
	}
	  
}
