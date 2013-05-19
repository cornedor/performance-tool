package info.corne.performancetool;

import info.corne.performancetool.activities.AdvancedSettingsActivity;
import info.corne.performancetool.activities.CPUSettingsActivity;
import info.corne.performancetool.activities.ProfilesActivity;
import info.corne.performancetool.activities.GPUSettingsActivity;
import info.corne.performancetool.statics.AudioSettings;
import info.corne.performancetool.statics.DefaultSettings;
import info.corne.performancetool.statics.FileNames;
import info.corne.performancetool.statics.PowerSettings;
import info.corne.performancetool.statics.Settings;
import info.corne.performancetool.utils.StringUtils;

import java.util.HashMap;
import java.util.Locale;

import org.xml.sax.Parser;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
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
		SetHardwareInterface, ActionBar.TabListener, OnItemClickListener {

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
	GPUSettingsActivity gpuSettingsActivity;
	ProfilesActivity profilesActivity;
	ProgressDialog dialog;
	String[] hardwareInfo;
	String[] ioSchedulers;
	ListAdapter profilesAdapter;
	int currentTab = 0;
	boolean onBootEnabled = false;
	ActionBar actionBar;
	
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		actionBar = getActionBar();
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
						currentTab = position;
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
		case R.id.menu_onboot:
			applySetOnBoot(menu);
			return true;
		case R.id.menu_apply:
			switch (currentTab){
				case 1:
					applyCpuSettings(null);
					break;
				case 2:
					applyAdvancedSettings(null);
					break;
				case 3:
					applyGpuSettings(null);
					break;
			};
			return true;
		}
		return super.onOptionsItemSelected(menu);       
	}
	
	/**
	 * This function will show a progress dialog and wil start a thread
	 * that will get all the required hardware info.
	 */
	public void getHardwareInfo()
	{
		dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.gathering_info));
		new GetHardwareInfoTask(this).execute(
				FileNames.SCALING_AVAILABLE_GOVERNORS,
				FileNames.SCALING_AVAILABLE_FREQUENCIES,
				FileNames.SCALING_GOVERNOR,
				FileNames.CPU_USER_CAP,
				FileNames.IO_SCHEDULERS,
				FileNames.ENABLE_OC,
				FileNames.MAX_CPUS_MPDEC,
				FileNames.MAX_CPUS_QUIET,
				FileNames.SUSPEND_FREQ,
				FileNames.AUDIO_MIN_FREQ,
				FileNames.CPUQUIET_AVAILABLE_GOVERNORS,
				FileNames.CPUQUIET_GOVERNOR,
				FileNames.ENABLE_LP_OC,
				FileNames.GPU_DECOUPLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		onBootEnabled = pm.getBoolean(Settings.SET_ON_BOOT_SETTING, false);
		MenuItem onBootMenu = menu.findItem(R.id.menu_onboot);
		onBootMenu.setChecked(onBootEnabled);
 
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
		Spinner suspendCapSpinner = (Spinner) findViewById(R.id.suspendCapSpinner);
		Spinner audioCapSpinner = (Spinner) findViewById(R.id.audioCapSpinner);
		Spinner ioSchedulerSpinner = (Spinner) findViewById(R.id.ioSchedulerSpinner);
		SeekBar maxCpusSeek = (SeekBar) findViewById(R.id.maxCpusSeek);
		Switch ocSwitch = (Switch) findViewById(R.id.overclockSwitch);
		Switch lpOcSwitch = (Switch) findViewById(R.id.lpOverclockSwitch);
		Switch gpuDecoupleSwitch = (Switch) findViewById(R.id.gpuDecoupleSwitch);
				
		// The returned data will be stored in their variables.
		String[] governors = result[0].split(" ");
		String[] freqencies = result[1].split(" ");
		// frequenciesShort will be Disabled + all the frequencies in MHz.
		String[] frequenciesShort = new String[freqencies.length + 1];
		String[] suspendFreqsShort = new String[freqencies.length + 1];
		String[] audioFreqsShort = new String[freqencies.length + 1];
		Spinner cpqGovernorSpinner = (Spinner) findViewById(R.id.cpqGovernorSpinner);
		String[] cpqGovernors = result[10].split(" ");
	
		frequenciesShort[0] = getResources().getString(R.string.disabled_string);
		suspendFreqsShort[0] = getResources().getString(R.string.disabled_string);
		audioFreqsShort[0] = getResources().getString(R.string.disabled_string);
		ioSchedulers = result[4].split(" ");
		int currentFrequencyPos = freqencies.length-1;
		int currentSuspendPos = 0;
		int currentAudioPos = 0;
		int currentIOScheduler = ioSchedulers.length-1;
		// Will loop trough the frequencies and convert them to MHz.
		for(int i = 0; i < freqencies.length; i++)
		{
			if(result[3].indexOf("000") == -1)
				currentFrequencyPos = 0;
			else if(result[3].compareTo(freqencies[i]) == 0)
				currentFrequencyPos = i + 1;
			
			if(result[8].indexOf("000") == -1)
				currentSuspendPos = 0;
			else if(result[8].compareTo(freqencies[i]) == 0)
				currentSuspendPos = i + 1;
			
			if(result[9].equals("Error"))
				audioCapSpinner.setVisibility(View.GONE);
			if(result[9].indexOf("000") == -1)
				currentAudioPos = 0;
			else if(result[9].compareTo(freqencies[i]) == 0)
				currentAudioPos = i + 1;

			frequenciesShort[i + 1] = freqencies[i].replaceFirst("000", "") + getResources().getString(R.string.mhz);
			suspendFreqsShort[i + 1] = freqencies[i].replaceFirst("000", "") + getResources().getString(R.string.mhz);
			audioFreqsShort[i + 1] = freqencies[i].replaceFirst("000", "") + getResources().getString(R.string.mhz);
		}
		// And that will also be stored in the adapter.
		frequencyCapSpinner.setAdapter(generateAdapter(frequenciesShort));
		suspendCapSpinner.setAdapter(generateAdapter(suspendFreqsShort));
		audioCapSpinner.setAdapter(generateAdapter(audioFreqsShort));
		// And the current selected freq will be selected.
		frequencyCapSpinner.setSelection(currentFrequencyPos);
		suspendCapSpinner.setSelection(currentSuspendPos);
		audioCapSpinner.setSelection(currentAudioPos);
		
		// All the governors will be add to the spinner.
		governorSpinner.setAdapter(generateAdapter(governors));
		// And the current selected governor will be selected
		// in the spinner.
		for(int i = 0; i < governors.length; i++)
		{
			if(result[2].compareTo(governors[i]) == 0)
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
		if(result[5].compareTo("1") == 0) ocSwitch.setChecked(true);
		else ocSwitch.setChecked(false);
		onOverclockSwitchClick(ocSwitch);
		
		if(!result[6].equals("Error") || !result[7].equals("Error")){
			if(!result[6].equals("Error")){
				maxCpusSeek.setProgress((int) Float.parseFloat(result[6])-1);
			}
			if(!result[7].equals("Error")){
				maxCpusSeek.setProgress((int) Float.parseFloat(result[7])-1);
			}
		} else {
			maxCpusSeek.setVisibility(View.GONE);
			((TextView) findViewById(R.id.maxCpusTextView)).setVisibility(View.GONE);
		}

		if(!result[11].equals("Error")){        
			cpqGovernorSpinner.setAdapter(generateAdapter(cpqGovernors));
			for(int i = 0; i < cpqGovernors.length; i++)
			{
				if(result[11].compareTo(cpqGovernors[i]) == 0)
					cpqGovernorSpinner.setSelection(i);
			}
		} else {
			cpqGovernorSpinner.setVisibility(View.GONE);
			((TextView) findViewById(R.id.cpqGovernorTextView)).setVisibility(View.GONE);
		}

		if(!result[12].equals("Error")){
			// If lp overclock is one turn the switch on.
			if(result[12].compareTo("1") == 0) lpOcSwitch.setChecked(true);
			else lpOcSwitch.setChecked(false);
			onLpOverclockSwitchClick(lpOcSwitch);
		} else {
			lpOcSwitch.setVisibility(View.GONE);
			((TextView) findViewById(R.id.lpOverclockInfo)).setVisibility(View.GONE);
		}

		if(!result[13].equals("Error")){
			if(result[13].compareTo("1") == 0) gpuDecoupleSwitch.setChecked(false);
			else gpuDecoupleSwitch.setChecked(true);
			onGpuDecoupleSwitchClick(gpuDecoupleSwitch);
		} else {
			gpuDecoupleSwitch.setVisibility(View.GONE);
			((TextView) findViewById(R.id.gpuDecoupleInfo)).setVisibility(View.GONE);
		}
		
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		((CheckBox) findViewById(R.id.autoWifi)).setChecked(pm.getBoolean(Settings.AUTO_WIFI, false));
		
		dialog.dismiss();
		
		refreshProfilesList();
		ListView profilesList = (ListView) findViewById(R.id.profilesListView);
		registerForContextMenu(profilesList);
		profilesList.setOnItemClickListener(this);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		AdapterContextMenuInfo aInfo = (AdapterContextMenuInfo) menuInfo;
		 
		String selectedItem = (String) profilesAdapter.getItem(aInfo.position);
		 
		menu.setHeaderTitle(selectedItem);
		menu.add(1, 1, 1, getResources().getString(R.string.details));
		menu.add(1, 2, 2, getResources().getString(R.string.delete));
	}
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		int itemId = item.getItemId();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		String selectedItem = (String) profilesAdapter.getItem(info.position);
		switch(itemId){
		case 1:
			StringBuilder message = new StringBuilder();
			message.append(getResources().getString(R.string.cpu_cap) + ": ");
			if(info.position == 1)
			{
				message.append(PowerSettings.CPU_USER_CAP.replaceFirst("000", "") + getResources().getString(R.string.mhz));
				message.append("\n" + getResources().getString(R.string.max_cpus) + ": ");
				message.append(PowerSettings.MAX_CPUS);
				message.append("\n" + getResources().getString(R.string.governor) + ": ");
				message.append(PowerSettings.SCALING_GOVERNOR);
			}
			else
			{
				
				if(sharedPreferences.getString(Settings.SELECTED_FREQ_SETTING + selectedItem, 
						"").indexOf("000") != -1)
					message.append(sharedPreferences.getString(Settings.SELECTED_FREQ_SETTING + selectedItem, 
						getResources().getString(R.string.disabled_string))
						.replaceFirst("000", "") + getResources().getString(R.string.mhz));
				else
					message.append(getResources().getString(R.string.disabled_string));
				message.append("\n" + getResources().getString(R.string.max_cpus) + ": ");
				message.append(sharedPreferences.getString(Settings.MAX_CPUS + selectedItem, "4"));
			}
			Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show();
			break;
		case 2:
			if(info.position == 0 || info.position == 1)
			{
				Toast.makeText(this, getResources().getString(R.string.default_no_remove), Toast.LENGTH_SHORT).show();
			}
			else
			{
				Editor editor = sharedPreferences.edit();
				String profiles = sharedPreferences.getString(Settings.PROFILES, "").replace("|" + selectedItem, "");
				editor.putString(Settings.PROFILES, profiles);
				editor.commit();
				refreshProfilesList();
			}
			break;
		}
		
		return true;
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
	 * If the lp overclock switch is clicked this function
	 * will change the textview containing some information.
	 * @param view
	 */
	public void onLpOverclockSwitchClick(View view)
	{
		Switch lpOcSwitch = (Switch) view;
		TextView lpOverclockInfo = (TextView) findViewById(R.id.lpOverclockInfo);
		if(lpOcSwitch.isChecked())
			lpOverclockInfo.setText(getResources().getString(R.string.allow_lpoverclock_on));
		else 
			lpOverclockInfo.setText(getResources().getString(R.string.allow_lpoverclock_off));
		
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
		int maxCpus = ((SeekBar) findViewById(R.id.maxCpusSeek)).getProgress() + 1;
		if(selectedFrequencyCap.compareTo(getResources().getString(R.string.disabled_string)) == 0) 
			selectedFrequencyCap = "0";
		else 
			selectedFrequencyCap = selectedFrequencyCap.replace(getResources().getString(R.string.mhz), "000");
		String selectedGovernor = (String)(((Spinner) findViewById(R.id.governorSpinner)).getSelectedItem());

		int ocEnabled = 0;
		if(((Switch)findViewById(R.id.overclockSwitch)).isChecked()) 
			ocEnabled = 1;

		int lpOcEnabled = 0;
		if(((Switch)findViewById(R.id.lpOverclockSwitch)).isChecked()) 
			lpOcEnabled = 1;
				
		// And run the commands in a thread.
		String[] files = {
				FileNames.CPU_USER_CAP,
				FileNames.ENABLE_OC,
				FileNames.SCALING_GOVERNOR,
				FileNames.MAX_CPUS_MPDEC,
				FileNames.MAX_CPUS_QUIET,
				FileNames.ENABLE_LP_OC
		};
		String[] values = {
				selectedFrequencyCap,
				"" + ocEnabled,
				selectedGovernor,
				maxCpus + "",
				maxCpus + "",
				"" + lpOcEnabled
		};
		new SetHardwareInfoTask(files, values, dialog).execute();
		// And store them in the shared preferences.
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Editor ed = pm.edit();
		ed.putString(Settings.SELECTED_FREQ_SETTING, selectedFrequencyCap.replace(getResources().getString(R.string.mhz), "000"));
		ed.putString(Settings.SELECTED_GOV_SETTING, selectedGovernor);
		ed.putInt(Settings.OC_ENABLED, ocEnabled);
		ed.putString(Settings.MAX_CPUS, maxCpus + "");
		ed.putInt(Settings.LP_OC_ENABLED, lpOcEnabled);
		ed.commit();
	}
	
	private void applySetOnBoot(MenuItem item)
	{
		onBootEnabled = !onBootEnabled;
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Editor ed = pm.edit();
		ed.putBoolean(Settings.SET_ON_BOOT_SETTING, onBootEnabled);
		ed.commit();
		item.setChecked(onBootEnabled);
	}
	
	/**
	 * When the apply button is clicked in the advanced tab this
	 * function will be triggered, this function will then
	 * start a thread that will write the settings to files.
	 * The settings will also be stored in the shared preferences.
	 * @param button
	 */
	public void applyAdvancedSettings(View button)
	{
		dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.being_saved));
		String selectedScheduler = (String)(((Spinner) findViewById(R.id.ioSchedulerSpinner)).getSelectedItem());
		String suspendFreq = (String)(((Spinner) findViewById(R.id.suspendCapSpinner)).getSelectedItem());
		String audioFreq = (String)(((Spinner) findViewById(R.id.audioCapSpinner)).getSelectedItem());
		boolean autoWifi = ((CheckBox) findViewById(R.id.autoWifi)).isChecked();
		if(suspendFreq.compareTo(getResources().getString(R.string.disabled_string)) == 0)
			suspendFreq = "0";
		else
			suspendFreq = suspendFreq.replace(getResources().getString(R.string.mhz), "000");
		if(audioFreq.compareTo(getResources().getString(R.string.disabled_string)) == 0)
			audioFreq = "0";
		else 
			audioFreq = audioFreq.replace(getResources().getString(R.string.mhz), "000");
		String selectedCPQGovernor = (String)(((Spinner) findViewById(R.id.cpqGovernorSpinner)).getSelectedItem());
				
		String[] values = {
				selectedScheduler,
				suspendFreq,
				audioFreq,
				selectedCPQGovernor
		};
		String[] files = {
				FileNames.IO_SCHEDULERS,
				FileNames.SUSPEND_FREQ,
				FileNames.AUDIO_MIN_FREQ,
				FileNames.CPUQUIET_GOVERNOR
		};
		new SetHardwareInfoTask(files, values, dialog).execute();
		
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor ed = pm.edit();
		ed.putString(Settings.SELECTED_SCHEDULER_SETTING, selectedScheduler);
		ed.putString(Settings.SUSPEND_FREQ, suspendFreq);
		ed.putString(Settings.AUDIO_MIN_FREQ, audioFreq);
		ed.putString(Settings.SELECTED_CPQGOV_SETTING, selectedCPQGovernor);
		ed.putBoolean(Settings.AUTO_WIFI, autoWifi);
		ed.commit();
	}

	public void onGpuDecoupleSwitchClick(View view)
	{
		Switch gpuDecoupleSwitch = (Switch) view;
		TextView gpuDecoupleInfo = (TextView) findViewById(R.id.gpuDecoupleInfo);
		if(gpuDecoupleSwitch.isChecked())
			gpuDecoupleInfo.setText(getResources().getString(R.string.allow_decouble_gpu_on));
		else 
			gpuDecoupleInfo.setText(getResources().getString(R.string.allow_decouble_gpu_off));
	}

	public void applyGpuSettings(View button)
	{
		// Open a dialog
		dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.being_saved));

		int gpuDecoupleEnabled = 0;
		if(((Switch)findViewById(R.id.gpuDecoupleSwitch)).isChecked()) 
			gpuDecoupleEnabled = 1;
				
		// And run the commands in a thread.
		String[] files = {
				FileNames.GPU_DECOUPLE
		};
		String[] values = {
				(gpuDecoupleEnabled==1?"0":"1")
		};
		new SetHardwareInfoTask(files, values, dialog).execute();
		// And store them in the shared preferences.
		SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
		Editor ed = pm.edit();
		ed.putInt(Settings.GPU_DECOUPLE_ENABLED, gpuDecoupleEnabled);
		ed.commit();
	}
	
	/**
	 * This function will generate a ArrayAdapter
	 * to use in a simple spinner.
	 * @param args A array of strings that should be put in
	 *              the ArrayAdapter.
	 * @return An ArrayAdapter including the args. 
	 */
	public ArrayAdapter<String> generateAdapter(String[] args)
	{
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, args);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		return adapter;
	}
	
	public void addProfile(View view)
	{
		String selectedFrequencyCap = (String)(((Spinner) findViewById(R.id.frequencyCapSpinner)).getSelectedItem());
		String selectedGovernor = (String)(((Spinner) findViewById(R.id.governorSpinner)).getSelectedItem());
		String selectedScheduler = (String)(((Spinner) findViewById(R.id.ioSchedulerSpinner)).getSelectedItem());
		int maxCpus = ((SeekBar) findViewById(R.id.maxCpusSeek)).getProgress() + 1;
		int ocEnabled = 0;
		if(((Switch)findViewById(R.id.overclockSwitch)).isChecked()) ocEnabled = 1;
		//boolean fixAudioLag = ((Switch)findViewById(R.id.audioLagSwitch)).isChecked();
		String suspendFreq = (String)(((Spinner) findViewById(R.id.suspendCapSpinner)).getSelectedItem());
		String audioFreq = DefaultSettings.AUDIO_MIN_FREQ;
		String selectedCPQGovernor = (String)(((Spinner) findViewById(R.id.cpqGovernorSpinner)).getSelectedItem());
		boolean autoWifi = ((CheckBox) findViewById(R.id.autoWifi)).isChecked();
		int lpOcEnabled = 0;
		if(((Switch)findViewById(R.id.lpOverclockSwitch)).isChecked()) lpOcEnabled = 1;
				
		EditText profileNameInput = (EditText) findViewById(R.id.profileNameInput);
		String profileName = profileNameInput.getText().toString();
		if(!profileName.isEmpty())
		{
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			String[] profiles = sharedPreferences.getString(Settings.PROFILES, 
					getResources().getString(R.string.default_profile) + "|" + 
					getResources().getString(R.string.power_profile) + "|" +
					getResources().getString(R.string.audio_profile)).split("\\|");
			String[] newProfiles = new String[profiles.length+1];
			for(int i = 0; i < profiles.length; i++)
				if(profileName.toUpperCase(Locale.US).compareTo(profiles[i].toUpperCase(Locale.US)) == 0)
					return;
				else
					newProfiles[i] = profiles[i];
			newProfiles[profiles.length] = profileName;
			
			Editor editor = sharedPreferences.edit();
			editor.putString(Settings.PROFILES, StringUtils.join(newProfiles, "|"));
			editor.putString(Settings.SELECTED_FREQ_SETTING + profileName, 
					selectedFrequencyCap.replace(getResources().getString(R.string.mhz), "000"));
			editor.putString(Settings.OC_ENABLED + profileName, "" + ocEnabled);
			editor.putString(Settings.SELECTED_GOV_SETTING + profileName, selectedGovernor);
			editor.putString(Settings.SELECTED_SCHEDULER_SETTING + profileName, selectedScheduler);
			editor.putString(Settings.MAX_CPUS + profileName, maxCpus + "");
			editor.putString(Settings.SUSPEND_FREQ + profileName, suspendFreq.replace(getResources().getString(R.string.mhz), "000"));
			editor.putString(Settings.AUDIO_MIN_FREQ + profileName, audioFreq);
			editor.putString(Settings.SELECTED_CPQGOV_SETTING + profileName, selectedCPQGovernor);
			editor.putString(Settings.LP_OC_ENABLED + profileName, "" + lpOcEnabled);
			editor.putBoolean(Settings.AUTO_WIFI, autoWifi);
			editor.commit();
			refreshProfilesList();
		}
		return;
	}
	public void refreshProfilesList()
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		ListView profilesList = (ListView) findViewById(R.id.profilesListView);
		String[] profiles = sharedPreferences.getString(Settings.PROFILES, 
				getResources().getString(R.string.default_profile) + "|" +
				getResources().getString(R.string.power_profile) + "|" +
				getResources().getString(R.string.audio_profile)).split("\\|");
		profilesAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, profiles);
		profilesList.setAdapter(profilesAdapter);
		return;
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
			case 3:
				gpuSettingsActivity = new GPUSettingsActivity();
				return gpuSettingsActivity;
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
			// Show 4 total pages.
			return 4;
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
			case 3:
				return getString(R.string.title_gpu_section).toUpperCase(Locale.US);
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

	@Override
	public void notifyOfHardwareInfoSaved(AsyncTask<String[], Void, Void> task) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				getHardwareInfo();
			}
		});
	}
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int pos,
			long id) {
		String selectedProfile = (String) profilesAdapter.getItem(pos);
		dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.being_saved));
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = sharedPreferences.edit();
		RemoteViews rv = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
		if(pos == 0)
		{
			String[] files = {
				FileNames.CPU_USER_CAP,
				FileNames.ENABLE_OC,
				FileNames.SCALING_GOVERNOR,
				FileNames.IO_SCHEDULERS,
				FileNames.MAX_CPUS_MPDEC,
				FileNames.MAX_CPUS_QUIET,
				FileNames.SUSPEND_FREQ,
				FileNames.AUDIO_MIN_FREQ,
				FileNames.CPUQUIET_GOVERNOR,
				FileNames.ENABLE_LP_OC
			};
			String[] values = {
				DefaultSettings.CPU_USER_CAP,
				DefaultSettings.ENABLE_OC,
				DefaultSettings.SCALING_GOVERNOR,
				DefaultSettings.IO_SCHEDULERS,
				DefaultSettings.MAX_CPUS,
				DefaultSettings.MAX_CPUS,
				DefaultSettings.SUSPEND_FREQ,
				DefaultSettings.AUDIO_MIN_FREQ,
				DefaultSettings.CPUQUIET_GOVERNOR,
				DefaultSettings.ENABLE_LP_OC
			};
			SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
			task.addListener(this);
			task.execute();
			editor.putInt(Settings.CURRENT_WIDGET_PROFILE, 0);
			editor.putBoolean(Settings.AUTO_WIFI, false);
			rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_default);
		}
		else if(pos == 1)
		{
			String[] files = {
				FileNames.CPU_USER_CAP,
				FileNames.ENABLE_OC,
				FileNames.SCALING_GOVERNOR,
				FileNames.IO_SCHEDULERS,
				FileNames.MAX_CPUS_MPDEC,
				FileNames.MAX_CPUS_QUIET,               
				FileNames.SUSPEND_FREQ,
				FileNames.AUDIO_MIN_FREQ,
				FileNames.CPUQUIET_GOVERNOR,
				FileNames.ENABLE_LP_OC
			};
			String[] values = {
				PowerSettings.CPU_USER_CAP,
				PowerSettings.ENABLE_OC,
				PowerSettings.SCALING_GOVERNOR,
				PowerSettings.IO_SCHEDULERS,
				PowerSettings.MAX_CPUS,
				PowerSettings.MAX_CPUS,
				PowerSettings.SUSPEND_FREQ,
				PowerSettings.AUDIO_MIN_FREQ,
				PowerSettings.CPUQUIET_GOVERNOR,
				PowerSettings.ENABLE_LP_OC
			};

			SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
			task.addListener(this);
			task.execute();
			editor.putInt(Settings.CURRENT_WIDGET_PROFILE, 2);
			editor.putBoolean(Settings.AUTO_WIFI, false);
			rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_power);
		}
		else if(pos == 2)
		{
			String[] files = {
				FileNames.CPU_USER_CAP,
				FileNames.ENABLE_OC,
				FileNames.SCALING_GOVERNOR,
				FileNames.IO_SCHEDULERS,
				FileNames.MAX_CPUS_MPDEC,
				FileNames.MAX_CPUS_QUIET,               
				FileNames.SUSPEND_FREQ,
				FileNames.AUDIO_MIN_FREQ,
				FileNames.ENABLE_LP_OC
			};
			String[] values = {
				AudioSettings.CPU_USER_CAP,
				AudioSettings.ENABLE_OC,
				AudioSettings.SCALING_GOVERNOR,
				AudioSettings.IO_SCHEDULERS,
				AudioSettings.MAX_CPUS,
				AudioSettings.MAX_CPUS,
				AudioSettings.SUSPEND_FREQ,
				AudioSettings.AUDIO_MIN_FREQ,
				AudioSettings.ENABLE_LP_OC
			};
			SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
			task.addListener(this);
			task.execute();
			editor.putInt(Settings.CURRENT_WIDGET_PROFILE, 1);
			editor.putBoolean(Settings.AUTO_WIFI, false);
			rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_audio);
		}
		else
		{
			String selectedFrequencyCap = sharedPreferences.getString(Settings.SELECTED_FREQ_SETTING + selectedProfile, "0");
			String ocEnabled = sharedPreferences.getString(Settings.OC_ENABLED + selectedProfile, "0");
			String selectedGovernor = sharedPreferences.getString(Settings.SELECTED_GOV_SETTING + selectedProfile, "");
			String selectedScheduler = sharedPreferences.getString(Settings.SELECTED_SCHEDULER_SETTING + selectedProfile, "");
			String maxCpus = sharedPreferences.getString(Settings.MAX_CPUS, "4");
			String suspendFreq = sharedPreferences.getString(Settings.SUSPEND_FREQ, DefaultSettings.SUSPEND_FREQ);
			String audioFreq = sharedPreferences.getString(Settings.AUDIO_MIN_FREQ, DefaultSettings.AUDIO_MIN_FREQ);
			String selectedCPQGovernor = sharedPreferences.getString(Settings.SELECTED_CPQGOV_SETTING + selectedProfile, "");
			String lpOcEnabled = sharedPreferences.getString(Settings.LP_OC_ENABLED + selectedProfile, "0");
			boolean autoWifi = sharedPreferences.getBoolean(Settings.AUTO_WIFI, false);
			
			String[] files = {
					FileNames.CPU_USER_CAP,
					FileNames.ENABLE_OC,
					FileNames.SCALING_GOVERNOR,
					FileNames.IO_SCHEDULERS,
					FileNames.MAX_CPUS_MPDEC,
					FileNames.MAX_CPUS_QUIET,
					FileNames.SUSPEND_FREQ,
					FileNames.AUDIO_MIN_FREQ,
					FileNames.CPUQUIET_GOVERNOR,
					FileNames.ENABLE_LP_OC
			};
	
			String[] values = {
					selectedFrequencyCap,
					ocEnabled,
					selectedGovernor,
					selectedScheduler,
					maxCpus,
					maxCpus,
					suspendFreq,
					audioFreq,
					selectedCPQGovernor,
					lpOcEnabled
			};
			SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
			task.addListener(this);
			task.execute();
			editor.putInt(Settings.CURRENT_WIDGET_PROFILE, 0);
			editor.putBoolean(Settings.AUTO_WIFI, autoWifi);
			rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_default);
		}
		editor.commit();
		ComponentName cn = new ComponentName(getApplicationContext(), WidgetReceiver.class);
		(AppWidgetManager.getInstance(getApplicationContext())).updateAppWidget(cn, rv);
	}
}
