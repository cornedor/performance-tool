package info.corne.performancetool;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	CPUSettingsActivity cpuSettingsActivity;
	AdvancedSettingsActivity advancedSettingsActivity;
	String[] hardwareInfo;
	String[] ioSchedulers;
	
	static String SELECTED_FREQ_SETTING = "info.corne.performancetool.selectedFrequencyCap";
	static String SELECTED_GOV_SETTING = "info.corne.performancetool.selectedGovernor";
	static String SELECTED_SUSPENDED_FREQ_SETTINGS = "info.corne.performancetool.selectedSuspendedCap";
	static String SET_ON_BOOT_SETTING = "info.corne.performancetool.setOnBootSetting";
	static String SELECTED_SCHEDULER_SETTING = "info.corne.performancetool.selectedScheduler";

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		getHardwareInfo(0);
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem menu)
	{
		switch (menu.getItemId()) {
		case R.id.menu_refresh:
			getHardwareInfo(0);
			return true;
		default:
			return super.onOptionsItemSelected(menu);
		}
		
	}
	public void getHardwareInfo(int fragment)
	{
		new GetHardwareInfoTask(this, fragment).execute(
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors",
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies",
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
				"/sys/module/cpu_tegra/parameters/cpu_user_cap",
				"/sys/block/mmcblk0/queue/scheduler",
				"/sys/htc/suspend_freq",
				"/sys/module/cpu_tegra/parameters/enable_oc");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
	public void hardwareInfoLoaded(String[] result, int fragment)
	{
		Spinner governorSpinner = (Spinner) findViewById(R.id.governorSpinner);
		Spinner frequencyCapSpinner = (Spinner) findViewById(R.id.frequencyCapSpinner);
		Spinner ioSchedulerSpinner = (Spinner) findViewById(R.id.ioSchedulerSpinner);
		Spinner suspendedSpinner = (Spinner) findViewById(R.id.suspendedSpinner);
		Switch ocSwitch = (Switch) findViewById(R.id.overclockSwitch);
		String[] governors = result[0].split(" ");
		String[] freqencies = result[1].split(" ");
		String[] freqenciesShort = new String[freqencies.length];
		ioSchedulers = result[4].split(" ");
		int currentFrequencyPos = freqencies.length-1;
		int currentIOScheduler = ioSchedulers.length-1;
		for(int i = 0; i < freqencies.length; i++)
		{
			
			if(result[3].indexOf(freqencies[i]) != -1)
				currentFrequencyPos = i;
			freqenciesShort[i] = freqencies[i].replaceFirst("000", "") + getResources().getString(R.string.mhz);
		}
		governorSpinner.setAdapter(generateAdapter(governors));
		
		
		frequencyCapSpinner.setAdapter(generateAdapter(freqenciesShort));
		suspendedSpinner.setAdapter(generateAdapter(freqenciesShort));
		for(int i = 0; i < governors.length; i++)
		{
			if(result[2].indexOf(governors[i]) != -1)
				governorSpinner.setSelection(i);
		}
		frequencyCapSpinner.setSelection(currentFrequencyPos);
		
		
		for(int i = 0; i < ioSchedulers.length; i++)
		{
			if(ioSchedulers[i].charAt(0) == '[')
			{
				currentIOScheduler = i;	
				ioSchedulers[i] = ioSchedulers[i].substring(1, ioSchedulers[i].length()-1);
			}
		}
		ioSchedulerSpinner.setAdapter(generateAdapter(ioSchedulers));
		ioSchedulerSpinner.setSelection(currentIOScheduler);
		
		for(int i = 0; i < freqencies.length; i++)
		{
			if(result[5].indexOf(freqencies[i]) != -1)
				suspendedSpinner.setSelection(i);
		}
		if(result[6].indexOf('1') != -1)
		{
			ocSwitch.setChecked(true);
			onOverclockSwitchClick(ocSwitch);
		}
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		((Switch) findViewById(R.id.setCpuSettingsOnBootSwitch)).setChecked(pm.getBoolean(SET_ON_BOOT_SETTING, false));
	}
	public void onOverclockSwitchClick(View view)
	{
		Switch ocSwitch = (Switch) view;
		TextView overclockInfo = (TextView) findViewById(R.id.overclockInfo);
		if(ocSwitch.isChecked())
			overclockInfo.setText(getResources().getString(R.string.allow_overclock_on));
		else 
			overclockInfo.setText(getResources().getString(R.string.allow_overclock_off));
		
	}
	public void applyCpuSettings(View button)
	{
		String selectedFrequencyCap = (String)(((Spinner) findViewById(R.id.frequencyCapSpinner)).getSelectedItem());
		String selectedGovernor = (String)(((Spinner) findViewById(R.id.governorSpinner)).getSelectedItem());
		String selectedSuspendedCap = (String)(((Spinner) findViewById(R.id.suspendedSpinner)).getSelectedItem());
		Boolean onBootEnabled = (Boolean)(((Switch) findViewById(R.id.setCpuSettingsOnBootSwitch)).isChecked());
		String[] frequencyCommand = {"su", "-c", "echo " + selectedFrequencyCap.replace(getResources().getString(R.string.mhz), "000") + " > /sys/module/cpu_tegra/parameters/cpu_user_cap"};
		String[] suspendedCapCommand = {"su", "-c", "echo " + selectedSuspendedCap.replace(getResources().getString(R.string.mhz), "000") + " > /sys/htc/suspend_freq"};
		new SetHardwareInfoTask(this, false).execute(frequencyCommand);
		new SetHardwareInfoTask(this, false).execute(suspendedCapCommand);
		String[] governorCommand = {"su", "-c", "echo " + selectedGovernor + " > /sys/devices/system/cpu/cpu[[CPU]]/cpufreq/scaling_governor"};
		new SetHardwareInfoTask(this, true).execute(governorCommand);
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Editor ed = pm.edit();
		System.out.println(pm.getAll().toString());
		ed.putString(SELECTED_FREQ_SETTING, selectedFrequencyCap.replace(getResources().getString(R.string.mhz), "000"));
		ed.putString(SELECTED_GOV_SETTING, selectedGovernor);
		ed.putString(SELECTED_SUSPENDED_FREQ_SETTINGS, selectedSuspendedCap.replace(getResources().getString(R.string.mhz), "000"));
		ed.putBoolean(SET_ON_BOOT_SETTING, onBootEnabled);
		ed.commit();
	}
	public void applyAdvancedSettings(View button)
	{
		String selectedScheduler = (String)(((Spinner) findViewById(R.id.ioSchedulerSpinner)).getSelectedItem());
		String[] schedulerCommand = {"su", "-c", "echo \"" + selectedScheduler + "\" > /sys/block/mmcblk0/queue/scheduler" };
		new SetHardwareInfoTask(this, false).execute(schedulerCommand);
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Editor ed = pm.edit();
		ed.putString(SELECTED_SCHEDULER_SETTING, selectedScheduler);
		ed.commit();
	}
	public ArrayAdapter<String> generateAdapter(String[] args)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, args);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			switch (position) {
			case 1:
				cpuSettingsActivity = new CPUSettingsActivity();
				return cpuSettingsActivity;
			case 2:
				advancedSettingsActivity = new AdvancedSettingsActivity();
				return advancedSettingsActivity;
			default:
				Fragment fragment = new DummySectionFragment();
				Bundle args = new Bundle();
				args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
				fragment.setArguments(args);
				return fragment;
			}
			
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return getString(R.string.title_profiles_section).toUpperCase();
			case 1:
				return getString(R.string.title_cpu_section).toUpperCase();
			case 2:
				return getString(R.string.title_advanced_section).toUpperCase();
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// Create a new TextView and set its text to the fragment's section
			// number argument value.
			TextView textView = new TextView(getActivity());
			textView.setGravity(Gravity.CENTER);
			textView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return textView;
		}
	}

}
