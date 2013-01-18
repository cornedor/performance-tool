package info.corne.performancetool;

import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
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
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
/**
 * The main class. This will load all the current settings and
 * add them to the interface. It also controls all interaction
 * with the UI.
 * 
 * Copyright (C) 2013  Corné Dorrestijn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 * 
 * @author Corné Dorrestijn
 *
 */
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
	ProfilesActivity profilesActivity;
	ProgressDialog dialog;
	String[] hardwareInfo;
	String[] ioSchedulers;
	
	static String SELECTED_FREQ_SETTING = "info.corne.performancetool.selectedFrequencyCap";
	static String SELECTED_GOV_SETTING = "info.corne.performancetool.selectedGovernor";
	static String SET_ON_BOOT_SETTING = "info.corne.performancetool.setOnBootSetting";
	static String SELECTED_SCHEDULER_SETTING = "info.corne.performancetool.selectedScheduler";
	static String OC_ENABLED = "info.corne.performancetool.overclockEnabled";

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
		getHardwareInfo();
		
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem menu)
	{
		switch (menu.getItemId()) {
		case R.id.menu_refresh:
			getHardwareInfo();
			return true;
		case R.id.menu_about:
			// Show the about dialog.
			AlertDialog aboutDialog = new AlertDialog.Builder(this).create();
			aboutDialog.setTitle("About");
			aboutDialog.setMessage(getResources().getString(R.string.about_info));
			aboutDialog.setIcon(R.drawable.ic_launcher);
			aboutDialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(menu);
		}
		
	}
	/**
	 * This function will show a progress dialog and wil start a thread
	 * that will get all the required hardware info.
	 */
	public void getHardwareInfo()
	{
		dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.gathering_info));
		new GetHardwareInfoTask(this).execute(
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_governors",
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies",
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",
				"/sys/module/cpu_tegra/parameters/cpu_user_cap",
				"/sys/block/mmcblk0/queue/scheduler",
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
	/**
	 * This function will be triggered when the hardware info
	 * is loaded, It will then use that info to fill the view
	 * in the activities. 
	 * @param result A array with all the info needed.
	 */
	public void hardwareInfoLoaded(String[] result)
	{
		// Get the views
		Spinner governorSpinner = (Spinner) findViewById(R.id.governorSpinner);
		Spinner frequencyCapSpinner = (Spinner) findViewById(R.id.frequencyCapSpinner);
		Spinner ioSchedulerSpinner = (Spinner) findViewById(R.id.ioSchedulerSpinner);
		Switch ocSwitch = (Switch) findViewById(R.id.overclockSwitch);
		// The returned data will be stored in their variables.
		String[] governors = result[0].split(" ");
		String[] freqencies = result[1].split(" ");
		String[] freqenciesShort = new String[freqencies.length];
		ioSchedulers = result[4].split(" ");
		int currentFrequencyPos = freqencies.length-1;
		int currentIOScheduler = ioSchedulers.length-1;
		// Will loop trough the frequencies and convert them to MHz.
		for(int i = 0; i < freqencies.length; i++)
		{
			
			if(result[3].indexOf(freqencies[i]) != -1)
				currentFrequencyPos = i;
			freqenciesShort[i] = freqencies[i].replaceFirst("000", "") + getResources().getString(R.string.mhz);
		}
		// And that will also be stored in the adapter.
		frequencyCapSpinner.setAdapter(generateAdapter(freqenciesShort));
		// And the current selected freq will be selected.
		frequencyCapSpinner.setSelection(currentFrequencyPos);
		
		// All the governors will be add to the spinner.
		governorSpinner.setAdapter(generateAdapter(governors));
		// And the current selected governor will be selected
		// in the spinner.
		for(int i = 0; i < governors.length; i++)
		{
			if(result[2].indexOf(governors[i]) != -1)
				governorSpinner.setSelection(i);
		}
		
		// Will search for the currently selected IO scheduler.
		for(int i = 0; i < ioSchedulers.length; i++)
		{
			if(ioSchedulers[i].charAt(0) == '[')
			{
				currentIOScheduler = i;	
				ioSchedulers[i] = ioSchedulers[i].substring(1, ioSchedulers[i].length()-1);
			}
		}
		// And fill the spinners/set selection
		ioSchedulerSpinner.setAdapter(generateAdapter(ioSchedulers));
		ioSchedulerSpinner.setSelection(currentIOScheduler);
		
		// If overclock is one turn the switch on.
		if(result[5].indexOf('1') != -1)
		{
			ocSwitch.setChecked(true);
			onOverclockSwitchClick(ocSwitch);
		}
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		((Switch) findViewById(R.id.setCpuSettingsOnBootSwitch)).setChecked(pm.getBoolean(SET_ON_BOOT_SETTING, false));
		dialog.dismiss();
	}
	/**
	 * If the overclock switch is clicked this function
	 * will change the textview containing some information.
	 * @param view
	 */
	public void onOverclockSwitchClick(View view)
	{
		Switch ocSwitch = (Switch) view;
		TextView overclockInfo = (TextView) findViewById(R.id.overclockInfo);
		if(ocSwitch.isChecked())
			overclockInfo.setText(getResources().getString(R.string.allow_overclock_on));
		else 
			overclockInfo.setText(getResources().getString(R.string.allow_overclock_off));
		
	}
	/**
	 * When the apply button is clicked in the CPU tab this
	 * function will be triggered, this function will then
	 * start a thread that will write the settings to files.
	 * The settings will also be stored in the shared preferences.
	 * @param button
	 */
	public void applyCpuSettings(View button)
	{
		// Open a dialog
		dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.being_saved));
		// Get the data from the views.
		String selectedFrequencyCap = (String)(((Spinner) findViewById(R.id.frequencyCapSpinner)).getSelectedItem());
		String selectedGovernor = (String)(((Spinner) findViewById(R.id.governorSpinner)).getSelectedItem());
		Boolean onBootEnabled = (Boolean)(((Switch) findViewById(R.id.setCpuSettingsOnBootSwitch)).isChecked());
		int ocEnabled = 0;
		if(((Switch)findViewById(R.id.overclockSwitch)).isChecked()) ocEnabled = 1;
		
		// And run the commands in a thread.
		String[] files = {
				"/sys/module/cpu_tegra/parameters/cpu_user_cap",
				"/sys/module/cpu_tegra/parameters/enable_oc",
				"/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor",				
		};
		String[] values = {
				selectedFrequencyCap.replace(getResources().getString(R.string.mhz), "000"),
				"" + ocEnabled,
				selectedGovernor
		};
		new SetHardwareInfoTask(files, values, dialog).execute();
		// And store them in the shared preferences.
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Editor ed = pm.edit();
		ed.putString(SELECTED_FREQ_SETTING, selectedFrequencyCap.replace(getResources().getString(R.string.mhz), "000"));
		ed.putString(SELECTED_GOV_SETTING, selectedGovernor);
		ed.putBoolean(SET_ON_BOOT_SETTING, onBootEnabled);
		ed.putInt(OC_ENABLED, ocEnabled);
		ed.commit();
	}
	/**
	 * When the apply button is clicked in the adnaved tab this
	 * function will be triggered, this function will then
	 * start a thread that will write the settings to files.
	 * The settings will also be stored in the shared preferences.
	 * @param button
	 */
	public void applyAdvancedSettings(View button)
	{
		dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.being_saved));
		String selectedScheduler = (String)(((Spinner) findViewById(R.id.ioSchedulerSpinner)).getSelectedItem());
		String[] values = {
				selectedScheduler
		};
		String[] files = {
				"/sys/block/mmcblk0/queue/scheduler"
		};
		new SetHardwareInfoTask(values, files, dialog).execute();
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Editor ed = pm.edit();
		ed.putString(SELECTED_SCHEDULER_SETTING, selectedScheduler);
		ed.commit();
	}
	/**
	 * This function will generate a ArrayAdapter
	 * to use in a simple spinner.
	 * @param args A array of strings that should be put in
	 * 				the ArrayAdapter.
	 * @return An ArrayAdapter including the args. 
	 */
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
			case 0:
				profilesActivity = new ProfilesActivity();
				return profilesActivity;
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
				return getString(R.string.title_profiles_section).toUpperCase(Locale.US);
			case 1:
				return getString(R.string.title_cpu_section).toUpperCase(Locale.US);
			case 2:
				return getString(R.string.title_advanced_section).toUpperCase(Locale.US);
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
