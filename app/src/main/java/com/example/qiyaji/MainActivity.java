package com.example.qiyaji;

import java.io.File;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;



public class MainActivity extends Activity {
	 TextView mPressureVal;
	 TextView mAltitude;
	 TextView RelativeAltitude;
	 TextView PressureRelativeAltitude;
	 TextView RelativeTime;
	 TextView angle;
	// private static int TIMEOUT =1000;	//1 second
    // private static final long NS_TO_MS_CONVERSION=(long)1E6;
    // private long lastPressureTimestamp=-1;
     
	private SensorManager sensorManager = null;
	SensorEventListener pressureListener;
	Sensor mPressure;
	Sensor mGyroscope;
	Sensor mAccelerate;
	float currentPressure;
	float lastPressure;
    double s;
   double S;
    
	private double currentheight;
    private long curTime=0;
    private long lastTime=0;
    private long durTime=0; 
    //地球半径
    double EARTH_RADIUS = 6378137;  
    private double l1;
    private double l2;
    double lat2;
    double lng2;
     double l;
	double ax,ay,az,wax,way,waz;
    //语音播放
	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);	
		 initSpeech() ;
		 
         //GPS
		 String serviceString=Context.LOCATION_SERVICE;
		 LocationManager locationManager=(LocationManager)getSystemService(serviceString);
		 String provider=LocationManager.GPS_PROVIDER;
		 Location location=locationManager.getLastKnownLocation(provider);
		 getLocationInfo(location);
		 locationManager.requestLocationUpdates(provider, 4000, 0, locationListener);//检测一次
		 
		 //文件
		final String fileName=System.currentTimeMillis()+".txt";
		// fileName.creatNewFile();
		//每次触发程序就建立新文件文件 文件名为年月日和时间
		//SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");  
	    // String time2 = format2.format(Calendar.getInstance().getTime());
		//final String fileName=time2+".txt";
		//mHandler.postDelayed(new MyRunnable(),1000);//1s后执行MyRunnabke程序
		 
		 
		mPressureVal = (TextView) findViewById(R.id.textView1);
		mAltitude = (TextView) findViewById(R.id.textView2);
		PressureRelativeAltitude=(TextView)findViewById(R.id.PressureRelativeAltitude);
		RelativeTime=(TextView)findViewById(R.id.Time);
		
		sensorManager = (SensorManager)getSystemService(this.SENSOR_SERVICE);
		mPressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		if(mPressure == null){
		     mPressureVal.setText("您的手机不支持气压传感器，无法使用本软件功能.");
		    // return;
		 }
		mAccelerate = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
	  pressureListener = new SensorEventListener() {	
			@Override
			public void onSensorChanged(SensorEvent event) {
				// TODO Auto-generated method stub
				 String message = new String();
				if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
					ax=event.values[0];ay=event.values[1];az=event.values[2];
					angle.setText(ax+","+ay+","+az);
				}
				else if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
					wax=event.values[0];way=event.values[1];waz=event.values[2];
				}
				else if (event.sensor.getType() == Sensor.TYPE_PRESSURE) {
					currentPressure=event.values[0];
					 curTime = System.currentTimeMillis(); 
					   if((curTime-lastTime)>=4000) {
					 double	delta=SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, currentPressure)
								 -SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, lastPressure);
					 double  altitude=SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, currentPressure);
					 if(lastPressure!=0) {
					   if(delta>=1.0) {
						   S=1;
						 if(delta>=4.0) {
							 s=+1;
							 Toast.makeText(MainActivity.this, "您正在上高架桥", Toast.LENGTH_LONG) .show();
							 speektext();
						 }
						}
					   if(delta<=-1.0) {
						   S=-1;
						 if(delta<=-4.0) {
							 s=-1;
							 Toast.makeText(MainActivity.this, "您正在下高架桥", Toast.LENGTH_LONG).show();
							 speektext();
						      }
					        }
						if(delta<4.0&&delta>-4.0) {
							s=0;
						}
						if(delta<1.0&&delta>-1.0) {
							S=0;
						}
					   }
						 durTime=(curTime-lastTime);
						 RelativeTime.setText(durTime+" ms");
						 lastPressure=currentPressure;
						 lastTime=curTime;
						 PressureRelativeAltitude.setText(delta+" m");
						currentheight=SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, currentPressure);
						mAltitude.setText(currentheight+" m");
						mPressureVal.setText(currentPressure+" mPa");
						//写入文件夹中
						 DecimalFormat df = new DecimalFormat("#,##0.000");  
				         SimpleDateFormat sdf=new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss"); 
			             String str=sdf.format(curTime);  
				         message=str +" "+df.format(altitude)+" "+s+" "+l1+" "+l2+" "+"\n";  
						 writeFileSdcard(message);
					 }
				}
			}
			//语音合成
			private void speektext() {
				// TODO Auto-generated method stub
				SpeechSynthesizer mTts= SpeechSynthesizer.createSynthesizer(MainActivity.this, null); 
				 mTts.setParameter(SpeechConstant. VOICE_NAME, "vixyun" ); // 设置发音人
			     mTts.setParameter(SpeechConstant. SPEED, "50" );// 设置语速
			     mTts.setParameter(SpeechConstant. VOLUME, "80" );// 设置音量，范围 0~100
			     mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); 
			     if(S==1 && l==1) {
			    	 mTts.startSpeaking("您正在缓慢的上高架",mSynListener );
			     }
			     if(S==-1 && l==1) {
			    	 mTts.startSpeaking("您正在缓慢的下高架",mSynListener );
			     }
			     if(s==+1) {
			    	 mTts.startSpeaking("您正在上高架",mSynListener );
			     }
			     if(s==-1) {
			    	 mTts.startSpeaking("您正在下高架",mSynListener );
			     }
			     }
			private SynthesizerListener mSynListener = new SynthesizerListener(){  
			    //会话结束回调接口，没有错误时，error为null  
			    public void onCompleted(SpeechError error) {}  
			    //缓冲进度回调  
			    //percent为缓冲进度0~100，beginPos为缓冲音频在文本中开始位置，endPos表示缓冲音频在文本中结束位置，info为附加信息。  
			    public void onBufferProgress(int percent, int beginPos, int endPos, String info) {}  
			    //开始播放  
			    public void onSpeakBegin() {}  
			    //暂停播放  
			    public void onSpeakPaused() {}  
			    //播放进度回调  
			    //percent为播放进度0~100,beginPos为播放音频在文本中开始位置，endPos表示播放音频在文本中结束位置.  
			    public void onSpeakProgress(int percent, int beginPos, int endPos) {}  
			    //恢复播放回调接口  
			    public void onSpeakResumed() {}  
			//会话事件回调接口  
			    public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {}
			
			};
			private void writeFileSdcard(String delta) {
				// TODO Auto-generated method stub
				  try {  
			            // 如果手机插入了SD卡，而且应用程序具有访问SD的权限  
			            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
			                // 获取SD卡的目录  
			                File sdCardDir = Environment.getExternalStorageDirectory();  
			               
			                File targitFile = new File(sdCardDir.getCanonicalPath() +"/"+fileName);  
			                // 以指定文件创建 RandomAccessFile对象  
			                RandomAccessFile raf = new RandomAccessFile(targitFile, "rw");  
			                // 将文件记录指针移动到最后  
			                raf.seek(targitFile.length());  
			                // 输出文件内容  
			                raf.write(delta.getBytes());  
			                // 关闭RandomAccessFile  
			                raf.close();  
			            }  
			        } catch (Exception e) {  
			            e.printStackTrace();  
			        }  
				}
			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub				
			}
		};		
	}
	//位置信息
	private void getLocationInfo(Location location) {
		// TODO Auto-generated method stub
		if(location!=null) {
		double lat1=location.getLatitude();
		double lng1=location.getLongitude();
		l1=lat1;
		l2=lng1;
		lat1=lat1*Math.PI/180.0;
		lng1=lng1*Math.PI/180.0;
		    if(lat2!=0&&lng2!=0) {
		                double a=lat1-lat2;
		                double b=lng1-lng2;
		                double sa2=Math.sin(a/2.0);
	                 	double sb2=Math.sin(b/2.0);
	                   double d=2*EARTH_RADIUS*Math.asin(Math.sqrt(sa2*sa2+Math.cos(lat1)*Math.cos(lat2)*sb2*sb2));
	                if(d<16) {
	                	l=1;
	                }
	                if(d>16) {
	                	l=0;
	                }
		}
		    lat2=lat1;
		    lng2=lng1; 
		    
		    
		    
		}else {
			Toast.makeText(MainActivity.this, "无法获取当前位置", Toast.LENGTH_LONG)
	        .show();
		}
	
	}
	private final LocationListener locationListener=new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			getLocationInfo(location);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
			Toast.makeText(MainActivity.this, "请开启GPS。。。", Toast.LENGTH_LONG) .show();
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			getLocationInfo(null);
		}	
	};
	//语音
	private void initSpeech() {
		// TODO Auto-generated method stub
		 SpeechUtility. createUtility( this, SpeechConstant. APPID + "=5afa7df2" );
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		sensorManager.registerListener(pressureListener, mPressure, 
				 SensorManager.SENSOR_DELAY_UI);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}  
}
