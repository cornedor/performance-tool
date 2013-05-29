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
import info.corne.performancetool.statics.ProfileSettings;
import info.corne.performancetool.utils.StringUtils;

import java.util.Locale;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
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
import android.widget.RadioButton;

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
    boolean cpuHotplugging = true;
    int[] activeCpus = new int[3];
    
    String selectedFrequencyCap;
    String selectedGovernor;
    String selectedScheduler;
    int maxCpus;
    int ocEnabled;
    String suspendFreq;
    String audioFreq;
    int lpOcEnabled;
    String selectedCPQGovernor;
    String activeCpusString;
    int gpuScalingEnabled;
    boolean autoWifi;
    int gpuQuickOCEnabled;
    String gpuOCValuesString;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        Intent service2 = new Intent(getApplicationContext(), UpdateService.class);
        getApplicationContext().startService(service2); 

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
                FileNames.GPU_SCALING,
                FileNames.MANUAL_HOTPLUG,
                FileNames.ACTIVE_CPUS,
                FileNames.GPU_QUICK_OC,
                FileNames.GPU_OC);
    }

    public void getHardwareInfo(HardwareInfoPostRunnable postHook, String... params)
    {
        dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.gathering_info));
        new GetHardwareInfoTask(postHook).execute(
                params);
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
        Switch gpuScalingSwitch = (Switch) findViewById(R.id.gpuScalingSwitch);
        Switch gpuQuickOCSwitch = (Switch) findViewById(R.id.gpuOCSwitch);
        TextView gpuOCValues = (TextView) findViewById(R.id.gpuOCValues);

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
        if(result[5].equals("1")) 
            ocSwitch.setChecked(true);
        else 
            ocSwitch.setChecked(false);
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
            if(result[12].equals("1")) 
                lpOcSwitch.setChecked(true);
            else 
                lpOcSwitch.setChecked(false);
            onLpOverclockSwitchClick(lpOcSwitch);
        } else {
            lpOcSwitch.setVisibility(View.GONE);
            ((TextView) findViewById(R.id.lpOverclockInfo)).setVisibility(View.GONE);
        }

        if(!result[13].equals("Error")){
            if(result[13].equals("1")) 
                gpuScalingSwitch.setChecked(true);
            else 
                gpuScalingSwitch.setChecked(false);
        } else {
            gpuScalingSwitch.setVisibility(View.GONE);
        }

        if(!result[14].equals("Error")){
            if(result[14].equals("0")) {
                cpuHotplugging = true;
                updateCpuHotpluggingView(true);
            } else {
                cpuHotplugging = false;
                updateCpuHotpluggingView(true);
            }
        } else {
            cpuHotplugging = true;
            updateCpuHotpluggingView(true);
            ((RadioButton) findViewById(R.id.cpu_hotplug_mode)).setVisibility(View.GONE);            
            ((RadioButton)findViewById(R.id.cpu_manual_mode)).setVisibility(View.GONE);                        
        }

        if(!result[15].equals("Error")){
           updateActiveCpusView(result[15]);
        }

        if(!result[16].equals("Error")){
            if(result[16].equals("1"))
                gpuQuickOCSwitch.setChecked(true);
            else
                gpuQuickOCSwitch.setChecked(false);
        } else {
            gpuQuickOCSwitch.setVisibility(View.GONE);
        }

        if(!result[17].equals("Error")){
            gpuOCValuesString = result[17];
            gpuOCValues.setText(gpuOCValuesString);
        } else {
            gpuOCValues.setVisibility(View.GONE);
        }

        dialog.dismiss();
        
        updateFromView();
        updatePreferences("");
        
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
        	showDetailsDialog(selectedItem, sharedPreferences);
        	break;
        case 2:
            if(info.position == 0 || info.position == 1 || info.position == 2)
            {
                Toast.makeText(this, getResources().getString(R.string.default_no_remove), Toast.LENGTH_SHORT).show();
            }
            else
            {
                Editor editor = sharedPreferences.edit();
                String profiles = sharedPreferences.getString(Settings.PROFILES, "").replace("|" + selectedItem, "");
                editor.putString(Settings.PROFILES, profiles);
                
                for (int i = 0; i < Settings.ALL_PROFILE.length; i++){
                    String settings = Settings.ALL_PROFILE[i];
                    editor.remove(settings + selectedItem);
                }
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
        
        updateFromView();
                        
        // And run the commands in a thread.
        String[] files = null;
        String[] values = null;
        
        if (cpuHotplugging){
            files = new String[]{
                FileNames.CPU_USER_CAP,
                FileNames.ENABLE_OC,
                FileNames.SCALING_GOVERNOR,
                FileNames.CPUQUIET_GOVERNOR,
                FileNames.ENABLE_LP_OC,
                FileNames.MANUAL_HOTPLUG,
                FileNames.MAX_CPUS_MPDEC,
                FileNames.MAX_CPUS_QUIET
            };
            values = new String[]{
                selectedFrequencyCap,
                "" + ocEnabled,
                selectedGovernor,
                selectedCPQGovernor,
                "" + lpOcEnabled,
                "0",
                maxCpus + "",
                maxCpus + ""
            };
        } else {
            files = new String[]{
                FileNames.CPU_USER_CAP,
                FileNames.ENABLE_OC,
                FileNames.SCALING_GOVERNOR,
                FileNames.ENABLE_LP_OC,
                FileNames.MANUAL_HOTPLUG,
                FileNames.ACTIVE_CPUS
            };
            values = new String[]{
                selectedFrequencyCap,
                "" + ocEnabled,
                selectedGovernor,
                "" + lpOcEnabled,
                "1",
                activeCpusString
            };
        }
        
        new SetHardwareInfoTask(files, values, dialog).execute();
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
        
        updateFromView();
                
        String[] values = {
                selectedScheduler,
                suspendFreq,
                audioFreq
        };
        String[] files = {
                FileNames.IO_SCHEDULERS,
                FileNames.SUSPEND_FREQ,
                FileNames.AUDIO_MIN_FREQ
        };
        new SetHardwareInfoTask(files, values, dialog).execute();
    }

    public void applyGpuSettings(View button)
    {
        // Open a dialog
        dialog = ProgressDialog.show(this, getResources().getString(R.string.please_wait), getResources().getString(R.string.being_saved));

        updateFromView();
                
        // And run the commands in a thread.
        String[] files = {
                FileNames.GPU_SCALING,
                FileNames.GPU_QUICK_OC
        };
        String[] values = {
                (gpuScalingEnabled ==1?"1":"0"),
                (gpuQuickOCEnabled ==1?"1":"0")
        };

        new SetHardwareInfoTask(files, values, dialog).execute();

        final TextView gpuOCValues = (TextView) findViewById(R.id.gpuOCValues);

        getHardwareInfo(new HardwareInfoPostRunnable() {
            @Override
            public void run() {
                if(!result[0].equals("Error")){
                    gpuOCValuesString = result[0];
                    gpuOCValues.setText(gpuOCValuesString);
                } else {
                    gpuOCValues.setVisibility(View.GONE);
                }
            }
        }, FileNames.GPU_OC);

        dialog.dismiss();
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
        updateFromView();
                        
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
            editor.commit();
            
            updatePreferences(profileName);
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

        RemoteViews rv = new RemoteViews(getApplicationContext().getPackageName(), R.layout.widget_layout);
        if(pos == 0)
        {
            DefaultSettings settings = new DefaultSettings();
            String[] files = settings.getFileNames();
            String[] values = settings.getValues();
            
            SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
            task.addListener(this);
            task.execute();
            rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_default);
        }
        else if(pos == 1)
        {
            PowerSettings settings = new PowerSettings();
            String[] files = settings.getFileNames();
            String[] values = settings.getValues();

            SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
            task.addListener(this);
            task.execute();
            rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_power);
        }
        else if(pos == 2)
        {
            AudioSettings settings = new AudioSettings();
            String[] files = settings.getFileNames();
            String[] values = settings.getValues();

            SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
            task.addListener(this);
            task.execute();
            rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_audio);
        }
        else
        {
            ProfileSettings settings = new ProfileSettings(selectedProfile, sharedPreferences);
            String[] files = settings.getFileNames();
            String[] values = settings.getValues();

            SetHardwareInfoTask task = new SetHardwareInfoTask(files, values, dialog, true);
            task.addListener(this);
            task.execute();
            rv.setImageViewResource(R.id.widgetButton, R.drawable.widget_default);
        }
        
        Editor editor = sharedPreferences.edit();
        editor.putInt(Settings.CURRENT_WIDGET_PROFILE, pos);
        editor.commit();
        ComponentName cn = new ComponentName(getApplicationContext(), WidgetReceiver.class);
        (AppWidgetManager.getInstance(getApplicationContext())).updateAppWidget(cn, rv);
    }
    
    private void updateCpuHotpluggingView(boolean updateButton)
    {
        if (cpuHotplugging){
            ((SeekBar) findViewById(R.id.maxCpusSeek)).setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.maxCpusTextView)).setVisibility(View.VISIBLE);
            ((Spinner) findViewById(R.id.cpqGovernorSpinner)).setVisibility(View.VISIBLE);
        
            ((CheckBox) findViewById(R.id.activeCpu1)).setVisibility(View.GONE);            
            ((CheckBox) findViewById(R.id.activeCpu2)).setVisibility(View.GONE);                        
            ((CheckBox) findViewById(R.id.activeCpu3)).setVisibility(View.GONE);            
            ((TextView) findViewById(R.id.activeCpusTextView)).setVisibility(View.GONE);
            
            if (updateButton){
                ((RadioButton) findViewById(R.id.cpu_hotplug_mode)).setChecked(true);
            }
        } else {
            ((SeekBar) findViewById(R.id.maxCpusSeek)).setVisibility(View.GONE);
            ((TextView) findViewById(R.id.maxCpusTextView)).setVisibility(View.GONE);
            ((Spinner) findViewById(R.id.cpqGovernorSpinner)).setVisibility(View.GONE);
            
            ((CheckBox) findViewById(R.id.activeCpu1)).setVisibility(View.VISIBLE);            
            ((CheckBox) findViewById(R.id.activeCpu2)).setVisibility(View.VISIBLE);                        
            ((CheckBox) findViewById(R.id.activeCpu3)).setVisibility(View.VISIBLE);            
            ((TextView) findViewById(R.id.activeCpusTextView)).setVisibility(View.VISIBLE);

            if (updateButton){
                ((RadioButton) findViewById(R.id.cpu_manual_mode)).setChecked(true);
            }
        }
    }
    
    public void onCpuHotplugModeClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
    
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.cpu_hotplug_mode:
                if (checked){
                    cpuHotplugging = true;
                    updateCpuHotpluggingView(false);
                }
                break;
            case R.id.cpu_manual_mode:
                if (checked){
                    cpuHotplugging = false;
                    updateCpuHotpluggingView(false);                
                }
                break;
        }
    }
    
    private void updateActiveCpusView(String activeCpusString) {
        if (activeCpusString.length()==0){
            return;
        }
        
        String[] parts = activeCpusString.split(" ");
        if (parts.length != 3){
            return;
        }
        
        activeCpus[0]=Integer.parseInt(parts[0]);
        activeCpus[1]=Integer.parseInt(parts[1]);
        activeCpus[2]=Integer.parseInt(parts[2]);
                
        ((CheckBox) findViewById(R.id.activeCpu1)).setChecked(activeCpus[0]==1);            
        ((CheckBox) findViewById(R.id.activeCpu2)).setChecked(activeCpus[1]==1);                        
        ((CheckBox) findViewById(R.id.activeCpu3)).setChecked(activeCpus[2]==1);            
    }
    
    private String getActiveCpusSettingString() {
        return Integer.valueOf(activeCpus[0]).toString()+" "+
            Integer.valueOf(activeCpus[1]).toString()+" "+
            Integer.valueOf(activeCpus[2]).toString();
    }
    
    public void onActiveCpuChangeClick(View view) {
        boolean checked = ((CheckBox) view).isChecked();
        
        switch(view.getId()) {
            case R.id.activeCpu1:
                activeCpus[0]=checked?1:0;
                break;
            case R.id.activeCpu2:
                activeCpus[1]=checked?1:0;
                break;
            case R.id.activeCpu3:
                activeCpus[2]=checked?1:0;
                break;
        }
    }
    
    private void showDetailsDialog(String selectedProfile, SharedPreferences sharedPreferences) {
    	StringBuilder message = new StringBuilder();

    	if (selectedProfile.equals(getResources().getString(R.string.default_profile))){
            DefaultSettings settings = new DefaultSettings();
            settings.dump(message, getResources());
    	} else if (selectedProfile.equals(getResources().getString(R.string.power_profile))){
            PowerSettings settings = new PowerSettings();
            settings.dump(message, getResources());
    	} else if (selectedProfile.equals(getResources().getString(R.string.audio_profile))) {
            AudioSettings settings = new AudioSettings();
            settings.dump(message, getResources());
    	} else {
            ProfileSettings settings = new ProfileSettings(selectedProfile, sharedPreferences);
            settings.dump(message, getResources());
    	}
    	
    	new AlertDialog.Builder(this).setTitle(selectedProfile).setMessage(message.toString()).setNeutralButton("Close", null).show();  
    }
    
    private String getFrequencyStringFromSpinner(String freqString) {
        if(freqString.equals(getResources().getString(R.string.disabled_string))) 
            return "0";
        
        return freqString.replace(getResources().getString(R.string.mhz), "000");
    }
    
    /* sets values based on actual ui */
    private void updateFromView(){
        selectedFrequencyCap = (String)(((Spinner) findViewById(R.id.frequencyCapSpinner)).getSelectedItem());
        selectedFrequencyCap = getFrequencyStringFromSpinner(selectedFrequencyCap);
        selectedGovernor = (String)(((Spinner) findViewById(R.id.governorSpinner)).getSelectedItem());
        selectedScheduler = (String)(((Spinner) findViewById(R.id.ioSchedulerSpinner)).getSelectedItem());
        maxCpus = ((SeekBar) findViewById(R.id.maxCpusSeek)).getProgress() + 1;
        ocEnabled = ((Switch)findViewById(R.id.overclockSwitch)).isChecked()?1:0;
        suspendFreq = (String)(((Spinner) findViewById(R.id.suspendCapSpinner)).getSelectedItem());
        suspendFreq = getFrequencyStringFromSpinner(suspendFreq);
        audioFreq = (String)(((Spinner) findViewById(R.id.audioCapSpinner)).getSelectedItem());
        audioFreq = getFrequencyStringFromSpinner(audioFreq);
        lpOcEnabled = ((Switch)findViewById(R.id.lpOverclockSwitch)).isChecked()?1:0;
        selectedCPQGovernor = (String)(((Spinner) findViewById(R.id.cpqGovernorSpinner)).getSelectedItem());
        activeCpusString = getActiveCpusSettingString();
        gpuScalingEnabled = ((Switch)findViewById(R.id.gpuScalingSwitch)).isChecked()?1:0;
        autoWifi = ((CheckBox) findViewById(R.id.autoWifi)).isChecked();
        gpuQuickOCEnabled = ((Switch)findViewById(R.id.gpuOCSwitch)).isChecked()?1:0;
    }
        
    /* saves all actual values in preferences */
    private void updatePreferences(String selectedProfile){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Editor editor = sharedPreferences.edit();
        editor.putString(Settings.SELECTED_FREQ_SETTING + selectedProfile, selectedFrequencyCap);
        editor.putString(Settings.OC_ENABLED + selectedProfile, "" + ocEnabled);
        editor.putString(Settings.SELECTED_GOV_SETTING + selectedProfile, selectedGovernor);
        editor.putString(Settings.SELECTED_SCHEDULER_SETTING + selectedProfile, selectedScheduler);
        editor.putString(Settings.MAX_CPUS + selectedProfile, maxCpus + "");
        editor.putString(Settings.SUSPEND_FREQ + selectedProfile, suspendFreq);
        editor.putString(Settings.AUDIO_MIN_FREQ + selectedProfile, audioFreq);
        editor.putString(Settings.SELECTED_CPQGOV_SETTING + selectedProfile, selectedCPQGovernor);
        editor.putString(Settings.LP_OC_ENABLED + selectedProfile, "" + lpOcEnabled);
        editor.putString(Settings.CPU_HOTPLUGGING + selectedProfile, cpuHotplugging ?"0":"1");
        editor.putString(Settings.ACTIVE_CPUS + selectedProfile, activeCpusString);
        editor.putString(Settings.GPU_SCALING + selectedProfile, gpuScalingEnabled ==1?"1":"0");
        editor.putBoolean(Settings.AUTO_WIFI + selectedProfile, autoWifi);
        editor.putString(Settings.GPU_QUICK_OC + selectedProfile, gpuQuickOCEnabled ==1?"1":"0");
        editor.commit();
        
        //Log.d("maxwen", "prefs="+sharedPreferences.getAll());
    }
}
