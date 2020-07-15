package com.example.t5;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.graphics.drawable.Drawable;
import android.os.BatteryStats;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.internal.os.BatterySipper;
//import com.android.internal.os.BatteryStatsHelper;
import com.example.t5.BatteryStatsHelper;
import com.android.internal.os.PowerProfile;
//import com.android.settings.HelpUtils;
import com.example.t5.R;
//import com.android.settings.SettingsActivity;
import com.example.t5.R.string;

import java.util.List;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
		}
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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}
		
		private static final boolean DEBUG = false;

	    static final String TAG = "PowerUsageSummary";

	    private static final String KEY_APP_LIST = "app_list";

	    private static final String BATTERY_HISTORY_FILE = "tmp_bat_history.bin";

	    private static final int MENU_STATS_TYPE = Menu.FIRST;
	    private static final int MENU_STATS_REFRESH = Menu.FIRST + 1;
	    private static final int MENU_BATTERY_SAVER = Menu.FIRST + 2;
	    private static final int MENU_HELP = Menu.FIRST + 3;

	    private UserManager mUm;

	 //   private BatteryHistoryPreference mHistPref;
	    private PreferenceGroup mAppListGroup;
	    private String mBatteryLevel;
	    private String mBatteryStatus;

	    private int mStatsType = BatteryStats.STATS_SINCE_CHARGED;

	    private static final int MIN_POWER_THRESHOLD_MILLI_AMP = 5;
	    private static final int MAX_ITEMS_TO_LIST = 10;
	    private static final int MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP = 10;
	    private static final int SECONDS_IN_HOUR = 60 * 60;

	    private BatteryStatsHelper mStatsHelper;
	    
	    public TextView text1;
	    public ListView lv;
	    public int isrealtime=0;
	    public String[] strs = new String[] {"first", "second", "third", "fourth", "fifth"};
	    public SimpleAdapter mSimpleAdapter;
	    public ArrayList<HashMap<String, Object>> listItem;
	    public String filePath = "/sdcard/Test/";
	    public String fileName = "battery.txt";
	    public HashMap<Integer,Integer> map = new HashMap<>();
	    
	    public List<BatterySipper> usageList;

	    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {

	        @Override
	        public void onReceive(Context context, Intent intent) {
	            String action = intent.getAction();
	            if (Intent.ACTION_BATTERY_CHANGED.equals(action)
	                    && updateBatteryStatus(intent)) {
	                if (!mHandler.hasMessages(MSG_REFRESH_STATS)) {
	                    mHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATS, 500);
	                }
	            }
	        }
	    };
	    
		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
	        text1 = (TextView) rootView.findViewById(R.id.text1);
	        lv = (ListView) rootView.findViewById(R.id.lv);
//	        Button btn = (Button) rootView.findViewById(R.id.button1);
//	        btn.setOnClickListener(new View.OnClickListener() {
//	            @Override
//	            public void onClick(View v) {
//	            	if(isrealtime==1)
//	            		mHandler.removeMessages(MSG_REFRESH_STATS);
//	            	else
//	            		refreshperiod();
//	            	isrealtime = 1-isrealtime;
//	                //TODO Auto-generated method stub
//	                Log.i("widgetDemo", "button1 被用户点击了。");}});
	        listItem = new ArrayList<HashMap<String, Object>>();
	        for(int i=0;i<10;i++)  
	        {  
	            HashMap<String, Object> map = new HashMap<String, Object>();  
	            map.put("ItemTitle", "第"+i+"行");  
	            map.put("ItemText", "这是第"+i+"行");  
	            listItem.add(map);  
	        } 

	        mSimpleAdapter = new SimpleAdapter(getActivity(),listItem,R.layout.item,
	        new String[] {"ItemTitle", "ItemText"},   
	        new int[] {R.id.ItemTitle,R.id.ItemText});

	        lv.setAdapter(mSimpleAdapter);//为ListView绑定适配器
	        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//	               long x = usageList.get(position).cpuFgTime;
//	               long usageTime;
//	               long cpuTime;
//	               long gpsTime;
//	               long wifiRunningTime;
//	               long cpuFgTime;
//	               long wakeLockTime;
//	               long mobileRxPackets;
//	               long mobileTxPackets;
//	               long mobileActive;
//	               int mobileActiveCount;
//	               double mobilemspp;         // milliseconds per packet
//	               long wifiRxPackets;
//	               long wifiTxPackets;
//	               long mobileRxBytes;
//	               long mobileTxBytes;
//	               long wifiRxBytes;
//	               long wifiTxBytes;
	               BatterySipper bs = usageList.get(map.get(position));
	               String msg = "UsageTime:"+String.valueOf(bs.usageTime)+
	            		   "\ncpuTime:"+String.valueOf(bs.cpuTime)+"\nwakelocktime:"
	            		   +String.valueOf(bs.wakeLockTime)+"\nCpuFgTime"+
	            		   String.valueOf(bs.cpuFgTime)+"\nwifiRxBytes"+
	            		   String.valueOf(bs.wifiRxBytes)+"\nwifiTxBytes"+
	            		   String.valueOf(bs.wifiTxBytes);
                   AlertDialog alert = new AlertDialog.Builder(getActivity()).setTitle("Details").setMessage(msg).create();
                   alert.show();

	            }
	        });
			return rootView;
		}

	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        mUm = (UserManager) activity.getSystemService(Context.USER_SERVICE);
	        mStatsHelper = new BatteryStatsHelper(activity, true);
	    }

	    @Override
	    public void onCreate(Bundle icicle) {
	        super.onCreate(icicle);
	        mStatsHelper.create(icicle);
	        createfile();
	       // addPreferencesFromResource(R.xml.power_usage_summary);
	       // mAppListGroup = (PreferenceGroup) findPreference(KEY_APP_LIST);
	     //   setHasOptionsMenu(true);
	    }

	    @Override
	    public void onStart() {
	        super.onStart();
	        mStatsHelper.clearStats();
	        refreshStats();
	        refreshperiod();
	    }

	    @Override
	    public void onResume() {
	        super.onResume();
	        BatteryStatsHelper.dropFile(getActivity(), BATTERY_HISTORY_FILE);
	        updateBatteryStatus(getActivity().registerReceiver(mBatteryInfoReceiver,
	                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)));
	        if (mHandler.hasMessages(MSG_REFRESH_STATS)) {
	            mHandler.removeMessages(MSG_REFRESH_STATS);
	            mStatsHelper.clearStats();
	        }
	        refreshperiod();
	    }

	    @Override
	    public void onPause() {
	   //     BatteryEntry.stopRequestQueue();h
	        mHandler.removeMessages(1);
	        getActivity().unregisterReceiver(mBatteryInfoReceiver);
	        super.onPause();
	    }

	    @Override
	    public void onStop() {
	        super.onStop();
	      //  mHandler.removeMessages(MSG_REFRESH_STATS);
	    }

	    @Override
	    public void onDestroy() {
	        super.onDestroy();
	        if (getActivity().isChangingConfigurations()) {
	            mStatsHelper.storeState();
	          //  BatteryEntry.clearUidCache();
	        }
	    }
	/*
	    @Override
	    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	        if (preference instanceof BatteryHistoryPreference) {
	            mStatsHelper.storeStatsHistoryInFile(BATTERY_HISTORY_FILE);
	            Bundle args = new Bundle();
	            args.putString(BatteryHistoryDetail.EXTRA_STATS, BATTERY_HISTORY_FILE);
	            args.putParcelable(BatteryHistoryDetail.EXTRA_BROADCAST,
	                    mStatsHelper.getBatteryBroadcast());
	            SettingsActivity sa = (SettingsActivity) getActivity();
	            sa.startPreferencePanel(BatteryHistoryDetail.class.getName(), args,
	                    R.string.history_details_title, null, null, 0);
	            return super.onPreferenceTreeClick(preferenceScreen, preference);
	        }
	        if (!(preference instanceof PowerGaugePreference)) {
	            return false;
	        }
	        PowerGaugePreference pgp = (PowerGaugePreference) preference;
	        BatteryEntry entry = pgp.getInfo();
	        PowerUsageDetail.startBatteryDetailPage((SettingsActivity) getActivity(), mStatsHelper,
	                mStatsType, entry, true);
	        return super.onPreferenceTreeClick(preferenceScreen, preference);
	    }

	    @Override
	    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
	        if (DEBUG) {
	            menu.add(0, MENU_STATS_TYPE, 0, R.string.menu_stats_total)
	                    .setIcon(com.android.internal.R.drawable.ic_menu_info_details)
	                    .setAlphabeticShortcut('t');
	        }
	        MenuItem refresh = menu.add(0, MENU_STATS_REFRESH, 0, R.string.menu_stats_refresh)
	                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
	                .setAlphabeticShortcut('r');
	        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
	                MenuItem.SHOW_AS_ACTION_WITH_TEXT);

	        MenuItem batterySaver = menu.add(0, MENU_BATTERY_SAVER, 0, R.string.battery_saver);
	        batterySaver.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

	        String helpUrl;
	        if (!TextUtils.isEmpty(helpUrl = getResources().getString(R.string.help_url_battery))) {
	            final MenuItem help = menu.add(0, MENU_HELP, 0, R.string.help_label);
	            HelpUtils.prepareHelpMenuItem(getActivity(), help, helpUrl);
	        }
	    }

	    @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        switch (item.getItemId()) {
	            case MENU_STATS_TYPE:
	                if (mStatsType == BatteryStats.STATS_SINCE_CHARGED) {
	                    mStatsType = BatteryStats.STATS_SINCE_UNPLUGGED;
	                } else {
	                    mStatsType = BatteryStats.STATS_SINCE_CHARGED;
	                }
	                refreshStats();
	                return true;
	            case MENU_STATS_REFRESH:
	                mStatsHelper.clearStats();
	                refreshStats();
	                mHandler.removeMessages(MSG_REFRESH_STATS);
	                return true;
	            case MENU_BATTERY_SAVER:
	                final SettingsActivity sa = (SettingsActivity) getActivity();
	                sa.startPreferencePanel(BatterySaverSettings.class.getName(), null,
	                        R.string.battery_saver, null, null, 0);
	                return true;
	            default:
	                return false;
	        }
	    }
	*/
	/*    private void addNotAvailableMessage() {
	        Preference notAvailable = new Preference(getActivity());
	        notAvailable.setTitle(R.string.power_usage_not_available);
	        mHistPref.setHideLabels(true);
	        mAppListGroup.addPreference(notAvailable);
	    } */

	    private boolean updateBatteryStatus(Intent intent) {
	        if (intent != null) {
	            String batteryLevel = com.example.t5.Utils.getBatteryPercentage(intent);
	            String batteryStatus = com.example.t5.Utils.getBatteryStatus(getResources(),
	                    intent);
	            if (!batteryLevel.equals(mBatteryLevel) || !batteryStatus.equals(mBatteryStatus)) {
	                mBatteryLevel = batteryLevel;
	                mBatteryStatus = batteryStatus;
	                return true;
	            }
	        }
	        return false;
	    }
	    
	    public void createfile() {
	    	File file = null;
            try {
                file = new File(filePath);
                if (!file.exists()) {
                    file.mkdir();
                }
            } catch (Exception e) {
                Log.i("error:", e+"");
            }
            try {
                file = new File(filePath + fileName);
                if (!file.exists()) {
                    file.createNewFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
	    }
	    
	    public void refreshperiod() {
	    	refreshStats();
		    mHandler.sendEmptyMessageDelayed(MSG_REFRESH_STATS, 500);
	    }

	    @TargetApi(Build.VERSION_CODES.L)
		private void refreshStats() {
	      //  mAppListGroup.removeAll();
	      //  mAppListGroup.setOrderingAsAdded(false);
	      //  mHistPref = new BatteryHistoryPreference(getActivity(), mStatsHelper.getStats(),
	      //          mStatsHelper.getBatteryBroadcast());   //get stats
	      //  mHistPref.setOrder(-1);
	      //  mAppListGroup.addPreference(mHistPref);
	        boolean addedSome = false;
	        final PowerProfile powerProfile = mStatsHelper.getPowerProfile();
	        final BatteryStats stats = mStatsHelper.getStats();
	        final double averagePower = powerProfile.getAveragePower(PowerProfile.POWER_SCREEN_FULL);
	        int listcount=0;
            //listItem.remove(5);
            mSimpleAdapter.notifyDataSetChanged();
	        if (averagePower >= MIN_AVERAGE_POWER_THRESHOLD_MILLI_AMP) {
	            final List<UserHandle> profiles = mUm.getUserProfiles();

	            mStatsHelper.refreshStats(BatteryStats.STATS_SINCE_CHARGED, profiles);

	            usageList = mStatsHelper.getUsageList();

	            final int dischargeAmount = stats != null ? stats.getDischargeAmount(mStatsType) : 0;
	         
	            final int numSippers = usageList.size();
	            File file = new File(filePath + fileName);
	            FileWriter writer = null;
	            try {
					writer = new FileWriter(filePath + fileName,true);
		            writer.write("{");
		            writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.toString());
				}	  
	            listItem.clear();
	            for (int i = 0; i < numSippers; i++) {
	                final BatterySipper sipper = usageList.get(i);
	                sipper.getUid();
	                //sipper.userId;
	                //UserInfo info = mUm.getUserInfo(sipper.userId);
	                if ((sipper.value * SECONDS_IN_HOUR) < MIN_POWER_THRESHOLD_MILLI_AMP) {
	                    continue;
	                }
	                final double percentOfTotal =
	                        ((sipper.value / mStatsHelper.getTotalPower()) * dischargeAmount);
	                if (((int) (percentOfTotal + .5)) < 1) {
	                    continue;
	                }
	                String str1 ="";
	                if(i<10)
	                {
	                	try {
	                		writer = new FileWriter(filePath + fileName,true);
							writer.write(String.valueOf(sipper.getUid())+":"+String.valueOf(sipper.value)+",");
				            writer.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                }
//	                for(HashMap<String,Object> item:listItem)
//	                {
//	                	
//	                }
//	                
//	                FileOutputStream outputStream =new FileOutputStream(fs);
//	                outputStream.write(copyContent.getBytes());

	                if (sipper.drainType == BatterySipper.DrainType.OVERCOUNTED) {
	                    // Don't show over-counted unless it is at least 2/3 the size of
	                    // the largest real entry, and its percent of total is more significant
	                    if (sipper.value < ((mStatsHelper.getMaxRealPower()*2)/3)) {
	                        continue;
	                    }
	                    if (percentOfTotal < 10) {
	                        continue;
	                    }
	                    if ("user".equals(Build.TYPE)) {
	                        continue;
	                    }
	                }
	                if (sipper.drainType == BatterySipper.DrainType.UNACCOUNTED) {
	                    // Don't show over-counted unless it is at least 1/2 the size of
	                    // the largest real entry, and its percent of total is more significant
	                    if (sipper.value < (mStatsHelper.getMaxRealPower()/2)) {
	                        continue;
	                    }
	                    if (percentOfTotal < 5) {
	                        continue;
	                    }
	                    if ("user".equals(Build.TYPE)) {
	                        continue;
	                    }
	                }
	                final UserHandle userHandle = new UserHandle(UserHandle.getUserId(sipper.getUid()));
	               // final BatteryEntry entry = new BatteryEntry(getActivity(), mHandler, mUm, sipper);
	               // final Drawable badgedIcon = mUm.getBadgedIconForUser(entry.getIcon(),
	               //         userHandle);
	               // final CharSequence contentDescription = mUm.getBadgedLabelForUser(entry.getLabel(),
	               //         userHandle);
	           //     final PowerGaugePreference pref = new PowerGaugePreference(getActivity(),
	                    //    badgedIcon, contentDescription, entry);

	                final double percentOfMax = (sipper.value * 100) / mStatsHelper.getMaxPower();
	                sipper.percent = percentOfTotal;
	              //  pref.setTitle(entry.getLabel());
	              //  pref.setOrder(i + 1);
	              //  pref.setPercent(percentOfMax, percentOfTotal);
	              //  if (sipper.uidObj != null) {
	              //      pref.setKey(Integer.toString(sipper.uidObj.getUid()));
	              //  }
	                map.put(listcount, i);
	                String[] pkg = getActivity().getPackageManager().getPackagesForUid(sipper.getUid());
	                HashMap<String, Object> map = new HashMap<String, Object>();  
		            map.put("ItemTitle", "uid:"+String.valueOf(sipper.getUid()));  
		            map.put("ItemText", "value:"+String.valueOf(sipper.value)+"percent:"+String.valueOf(sipper.percent));  
		            listItem.add(map);  
		            mSimpleAdapter.notifyDataSetChanged();
		            listcount++;
	                addedSome = true;
	              //  mAppListGroup.addPreference(pref);
	              //  if (mAppListGroup.getPreferenceCount() > (MAX_ITEMS_TO_LIST + 1)) {
	              //      break;
	              //  }
	            }
	            try {
					writer = new FileWriter(filePath + fileName,true);
		            writer.write("}\n");
		            writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					System.out.println(e.toString());
				}	 
	        }
	        if (!addedSome) {
	            //addNotAvailableMessage();
	        }

	       // BatteryEntry.startRequestQueue();
	    }

	    static final int MSG_REFRESH_STATS = 100;

	    Handler mHandler = new Handler() {

	        @TargetApi(Build.VERSION_CODES.KITKAT)
			@Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	                case 1:
	                   /* BatteryEntry entry = (BatteryEntry) msg.obj;
	                    PowerGaugePreference pgp =
	                            (PowerGaugePreference) findPreference(
	                                    Integer.toString(entry.sipper.uidObj.getUid()));
	                    if (pgp != null) {
	                        final int userId = UserHandle.getUserId(entry.sipper.getUid());
	                        final UserHandle userHandle = new UserHandle(userId);
	                        pgp.setIcon(mUm.getBadgedIconForUser(entry.getIcon(), userHandle));
	                        pgp.setTitle(entry.name);
	                    }
	                    break;*/
	                case 2:
	                    Activity activity = getActivity();
	                    if (activity != null) {
	                        activity.reportFullyDrawn();
	                    }
	                    break;
	                case MSG_REFRESH_STATS:
	                    mStatsHelper.clearStats();
	                    refreshperiod();
	            }
	            super.handleMessage(msg);
	        }
	    };
	}
}
