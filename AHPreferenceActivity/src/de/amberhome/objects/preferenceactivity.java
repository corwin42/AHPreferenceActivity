// Changes to original PreferencesActivity by Erel:
// v1.0
// - Checkbox can show On and Off summary
// - Dependencies to other entries
// - Second Listview type with Map as key/entry pairs
// - AddPassword()
// - AddRingtone()
//
// v1.01
// - Bugfix for FC with nested PreferencesScreens
// - Added support for calling Intents (other activities)
// - Preferencescreens support dependency

// V1.02
// - Bugfix for getUpdatedKeys sometimes not working.

// V1.03
// - Uploaded to GitHub

// V1.04
// - AddEditText2() New with some more parameters

package de.amberhome.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.preference.RingtonePreference;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import anywheresoftware.b4a.BA;
import anywheresoftware.b4a.BA.Author;
import anywheresoftware.b4a.BA.Hide;
import anywheresoftware.b4a.BA.ShortName;
import anywheresoftware.b4a.BA.Version;
import anywheresoftware.b4a.keywords.Common;
import anywheresoftware.b4a.objects.collections.List;
import anywheresoftware.b4a.objects.collections.Map;
@Version(1.04f)
@Author("Erel Uziel / Markus Stipp")

public class preferenceactivity extends PreferenceActivity{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent in = getIntent();
		if (in == null || in.hasExtra("preferences") == false) {
			Common.Log("Intent missing root node.");
			return;
		}
		
		PreferenceScreenWrapper p = (PreferenceScreenWrapper) in.getSerializableExtra("preferences");
		PreferenceScreen ps = (PreferenceScreen) p.createPreference(this);
		this.setPreferenceScreen(ps);
		
		setDependency(ps, p.childs);
	}
	
	private void setDependency(PreferenceScreen ps, ArrayList<B4APreference> childs) {
		for (B4APreference b : childs) {
			if (b.key != null) {
				if (b instanceof PreferenceCategoryWrapper) {
					setDependency(ps, ((PreferenceCategoryWrapper) b).childs);
				}
				else if (b instanceof PreferenceScreenWrapper) {
					if (b.dependency.length() > 0 && ps.findPreference(b.dependency) == null)
						Common.Log("Dependency key does not exist: " + b.dependency);
					else
						ps.findPreference(b.key).setDependency(b.dependency);

					setDependency(ps, ((PreferenceScreenWrapper) b).childs);
				}
				else {
					if (b.dependency.length() > 0 && ps.findPreference(b.dependency) == null)
						Common.Log("Dependency key does not exist: " + b.dependency);
					else
						ps.findPreference(b.key).setDependency(b.dependency);
				}
			}
		}
		
	}
	/**
	 * Provides access to the saved settings. Using PreferenceManager you can get the stored values and modify them.
	 */
	@ShortName("AHPreferenceManager")
	public static class PreferenceManager {
		private SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(BA.applicationContext);
		private HashSet<String> updatedKeys = new HashSet<String>();

		private final OnSharedPreferenceChangeListener listener;
		public PreferenceManager() {
			listener = new OnSharedPreferenceChangeListener() {

				@Override
				public void onSharedPreferenceChanged(
						SharedPreferences sharedPreferences, String key) {
					updatedKeys.add(key);
				}

			};
			sp.registerOnSharedPreferenceChangeListener(listener);
		}		
		
		/**
		 * PreferenceActivity library allows you to show the standard settings interface and provides an easy way to handle applications settings.
		 *This library requires you to modify AndroidManifest.xml. See the <link>tutorial|http://www.basic4ppc.com/forum/basic4android-getting-started-tutorials/10608-preferenceactivity-tutorial.html</link> for more information.
		 */
		public static void LIBRARY_DOC() {
			
		}
		/**
		 * Returns a list with the keys that were updated since the last call to GetUpdatedKeys.
		 *Note that the updated keys may include keys with unchanged values.
		 */
		public List GetUpdatedKeys() {
			List l = new List();
			l.Initialize();
			for (String s : updatedKeys) {
				l.Add(s);
			}
			updatedKeys.clear();
			return l;
		}
		/**
		 * Returns the String value mapped to the given key. Returns an empty string if the key is not found.
		 */
		public String GetString(String Key) {
			return sp.getString(Key, "");
		}
		/**
		 * Returns the Boolean value mapped to the given key. Returns False is the key is not found.
		 */
		public boolean GetBoolean(String Key) {
			return sp.getBoolean(Key, false);
		}
		/**
		 * Returns a Map with all the Keys and Values. Note that changes to this map will not affect the stored values.
		 */
		public Map GetAll() {
			Map m = new Map();
			m.Initialize();
			for (Entry<String, ?> e : sp.getAll().entrySet()) {
				m.Put(e.getKey(), e.getValue());
			}
			return m;
		}
		/**
		 * Maps the given key to the given String value.
		 */
		public void SetString(String Key, String Value) {
			Editor e = sp.edit();
			e.putString(Key, Value);
			e.commit();
		}
		/**
		 * Maps the given key to the given Boolean value.
		 */
		public void SetBoolean(String Key, boolean Value) {
			Editor e = sp.edit();
			e.putBoolean(Key, Value);
			e.commit();
		}
		/**
		 * Clears all stored entries.
		 */
		public void ClearAll() {
			Editor e = sp.edit();
			e.clear();
			e.commit();
		}
	}
	/**
	 * 
	 */
	@ShortName("AHPreferenceScreen")
	public static class PreferenceScreenWrapper extends B4APreference{
		
		protected ArrayList<B4APreference> childs;
		protected static int randomKey;
		public PreferenceScreenWrapper() {
			this("PreferenceScreen" + Integer.toString(++randomKey));
		}
		protected PreferenceScreenWrapper(String key) {
			super(key, null, null, null, (String) null);
		}
		/**
		 * Initializes the object and sets the title that will show. The summary will show for secondary PreferenceScreens.
		 */
		public void Initialize(String Title, String Summary) {
			childs = new ArrayList<B4APreference>();
			this.title = Title;
			this.summary = Summary;
		}
		/**
		 * Creates the Intent object that is required for showing the PreferencesActivity.
		 *Example:<code>StartActivity(PreferenceScreen1.CreateIntent)</code>
		 */
		public Intent CreateIntent() {
			Intent in = new Intent();
			in.setClass(BA.applicationContext, preferenceactivity.class);
			in.putExtra("preferences", this);
			return in;
		}
		@Override
		Preference createPreference(PreferenceActivity c) {
			PreferenceScreen ps = c.getPreferenceManager().createPreferenceScreen(c);
			handleDefaults(ps);
			for (B4APreference b : childs) {
				if (b instanceof PreferenceCategoryWrapper) {
					((PreferenceCategoryWrapper)b).parent = ps;
					b.createPreference(c);
				}
				else {
					ps.addPreference(b.createPreference(c));
				}
			}
			return ps;
		}
		/**
		 * Adds a preference entry with a check box. The entry values can be either True or False.
		 *Key - The preference key associated with the value.
		 *Title - Entry title.
		 *SummaryOn - Entry summary (second row).
		 *SummaryOff - Entry summary if checkbox is not checked (Set to empty string to always show SummaryOn text)
		 *DefaultValue - The default value of this preference entry if the key does not already exist.
		 *Dependency - A key of a preference this preference entry depends on.
		 */
		public void AddCheckBox(String Key, String Title, final String SummaryOn, final String SummaryOff, boolean DefaultValue, String Dependency) {
			childs.add(new B4APreference(Key, Title, SummaryOn, DefaultValue, Dependency) {
				@Override
				Preference createPreference(PreferenceActivity c) {
					CheckBoxPreference cbp = new CheckBoxPreference(c);
					handleDefaults(cbp);
					if (SummaryOff.length() > 0) {
						cbp.setSummaryOn(SummaryOn);
						cbp.setSummaryOff(SummaryOff);
					}
					return cbp;
				}
			});
		}
		/**
		 * Adds a preference entry which allows the user to choose a single item out of a list.
		 *Key - The preference key associated with the value.
		 *Title - Entry title.
		 *Summary - Entry summary (second row).
		 *DefaultValue - The default value of this preference entry if the key does not already exist. Should match one of the values.
		 *Dependency - A key of a preference this preference entry depends on.
		 *Values - A list of strings with the possible values.
		 */
		public void AddList(String Key, String Title, String Summary, String DefaultValue, String Dependency, List Values) {
			final CharSequence[] values = new String[Values.getSize()];
			for (int i = 0;i < values.length;i++) {
				values[i] = String.valueOf(Values.Get(i));
			}
			childs.add(new B4APreference(Key, Title, Summary, DefaultValue, Dependency) {
				@Override
				Preference createPreference(PreferenceActivity c) {
					ListPreference list = new ListPreference(c);
					handleDefaults(list);
					list.setDialogTitle(title);
					
					list.setEntries(values);
					list.setEntryValues(values);
					return list;
				}
			});
		}
		/**
		 * Adds a preference entry which allows the user to choose a single item out of a list.
		 *Key - The preference key associated with the value.
		 *Title - Entry title.
		 *Summary - Entry summary (second row).
		 *DefaultValue - The default value of this preference entry if the key does not already exist. Should match one of the value keys.
		 *Dependency - A key of a preference this preference entry depends on.
		 *Values - A map with the possible values. The key of the map is used as the value for the preference and the value is displayed in the list.
		 */
		public void AddList2(String Key, String Title, String Summary, String DefaultValue, String Dependency, Map Values) {
			final CharSequence[] values = new String[Values.getSize()];
			final CharSequence[] keys = new String[Values.getSize()];
			for (int i = 0;i < values.length;i++) {
				values[i] = String.valueOf(Values.GetValueAt(i));
				keys[i] = String.valueOf(Values.GetKeyAt(i));
			}
			childs.add(new B4APreference(Key, Title, Summary, DefaultValue, Dependency) {
				@Override
				Preference createPreference(PreferenceActivity c) {
					ListPreference list = new ListPreference(c);
					handleDefaults(list);
					list.setDialogTitle(title);
					
					list.setEntries(values);
					list.setEntryValues(keys);
					return list;
				}
			});
		}
		
		public int RT_RINGTONE = 1;
		public int RT_NOTIFICATION = 2;
		public int RT_ALARM = 4;
		
		/**
		 * Adds a preference entry which allows the user to choose a ring tone out of a list.
		 *Key - The preference key associated with the value.
		 *Title - Entry title.
		 *Summary - Entry summary (second row).
		 *DefaultValue - The default value of this preference entry if the key does not already exist. Should match one of the value keys.
		 *Dependency - A key of a preference this preference entry depends on.
		 *RingToneType - Type of the Ringtone. Use RT_RINGTONE, RT_NOTIFICATION, RT_ALARM.
		 */
		public void AddRingtone(String Key, String Title, String Summary, String DefaultValue, String Dependency, final int RingToneType) {
			childs.add(new B4APreference(Key, Title, Summary, DefaultValue, Dependency) {
				@Override
				Preference createPreference(PreferenceActivity c) {
					RingtonePreference ring = new RingtonePreference(c);
					handleDefaults(ring);
					ring.setRingtoneType(RingToneType);					
					return ring;
				}
			});
		}
		/**
		 * Adds a preference entry which allows the user to enter free text.
		 *Key - The preference key associated with the value.
		 *Title - Entry title.
		 *Summary - Entry summary (second row).
		 *DefaultValue - The default value of this preference entry if the key does not already exist.
		 *Dependency - A key of a preference this preference entry depends on.
		 */
		public void AddEditText(String Key, final String Title, final String Summary, final String DefaultValue, final String Dependency) {
			childs.add(new B4APreference(Key, Title, Summary, DefaultValue, Dependency) {
				@Override
				Preference createPreference(PreferenceActivity c) {
					EditTextPreference e = new EditTextPreference(c);
					e.setDialogTitle(title);
					handleDefaults(e);
					return e;
				}
			});
		}
		
		/**
		 * Adds a preference entry which allows the user to enter free text.
		 *Key - The preference key associated with the value.
		 *Title - Entry title.
		 *Summary - Entry summary (second row).
		 *DefaultValue - The default value of this preference entry if the key does not already exist.
		 *InputType - InputType for the EditText view. For valid values see <link>InputType|http://developer.android.com/reference/android/text/InputType.html</link>
		 *Password - Hidden input when true.
		 *SelectAllOnFocus - Select complete text on focus.
		 *Dependency - A key of a preference this preference entry depends on.
		 */
	    public void AddEditText2(String Key, String Title, String Summary, String DefaultValue, final int InputType, final boolean Password, final boolean SelectAllOnFocus, String Dependency)
	    {
	      this.childs.add(new B4APreference(Key, Title, Summary, DefaultValue, Dependency)
	      {
	        Preference createPreference(PreferenceActivity c) {
	          EditTextPreference e = new EditTextPreference(c);
	          e.setDialogTitle(this.title);
	          EditText et = e.getEditText();
	          et.setInputType(InputType);
	          et.setSelectAllOnFocus(SelectAllOnFocus);
	          if (Password) et.setTransformationMethod(new PasswordTransformationMethod());
	          handleDefaults(e);
	          return e;
	        }
	      });
	    }

		
		/**
		 * Adds a preference entry which allows the user to enter a password. Be aware that the password is saved uncrypted in the preferences file!
		 *Key - The preference key associated with the value.
		 *Title - Entry title.
		 *Summary - Entry summary (second row).
		 *DefaultValue - The default value of this preference entry if the key does not already exist.
		 *Dependency - A key of a preference this preference entry depends on.
		 */
		public void AddPassword(String Key, final String Title, final String Summary, final String DefaultValue, final String Dependency) {
			childs.add(new B4APreference(Key, Title, Summary, DefaultValue, Dependency) {
				@Override
				Preference createPreference(PreferenceActivity c) {
					EditTextPreference e = new EditTextPreference(c);
					e.setDialogTitle(title);
					EditText et = e.getEditText();
					et.setTransformationMethod(new PasswordTransformationMethod());
					handleDefaults(e);
					return e;
				}
			});
		}
		/**
		 * Adds a secondary PreferenceScreen. When the user presses on this entry the second screen will appear.
		 *
		 *PreferenceScreen - The screen to add.
		 *Dependency - A key of a preference this preference entry depends on.
		 */
		public void AddPreferenceScreen(PreferenceScreenWrapper PreferenceScreen, final String Dependency) {
			PreferenceScreen.dependency = Dependency;
			childs.add(PreferenceScreen);
		}
		/**
		 * Calls the given Intent.
		 * 
		 * This supports some types of intents. See the Example for AHPreferenceActivity. No Extra Information will be passed.
		 */
		public void AddIntent(String Title, String Summary, final Intent Intent, final String Dependency) {
			//Common.Log("Intent: " + Intent.toString()); 
			//Common.Log("DataString : " + Intent.getDataString());
			//if (Intent.getComponent() != null) {
			//	Common.Log("Package: " + Intent.getComponent().getPackageName());
			//	Common.Log("Class: " + Intent.getComponent().getClassName());
			//}
			childs.add(new B4APreference(Title, Summary, Dependency, Intent.getAction(), Intent.getComponent(), Intent.getData() ) {
				@Override
				Preference createPreference(PreferenceActivity c) {
					PreferenceScreen ps = c.getPreferenceManager().createPreferenceScreen(c);
					handleDefaults(ps);
/*					Common.Log("Action: " + intentAction);
					Common.Log("URI: " + intentDataString);
					Common.Log("Package: " + intentPackage);
					Common.Log("Class: " + intentClass);
*/					Intent in = new Intent(intentAction);
					if (intentPackage.length() > 0 & intentClass.length() > 0)
						in.setClassName(intentPackage, intentClass);
					if (intentDataString.length() > 0) {
						Uri uri = Uri.parse(intentDataString);
						in.setData(uri);
					}
					ps.setIntent(in);
					return ps;
				}
			});
		}
		
		/**
		 * Adds a PreferenceCategory. A preference category is made of a title and a group of entries.
		 *Note that a PreferenceCategory cannot hold other PreferenceCategories.
		 */
		public void AddPreferenceCategory(PreferenceCategoryWrapper PreferenceCategory) {
			childs.add(PreferenceCategory);
		}
	}
	/**
	 * PreferenceCategory holds a group of other preferences.
	 */
	@ShortName("AHPreferenceCategory")
	public static class PreferenceCategoryWrapper extends PreferenceScreenWrapper {
		PreferenceGroup parent;
		public PreferenceCategoryWrapper() {
			super("PreferenceCategory" + Integer.toString(++randomKey));
		}
		/**
		 * Initializes the object and sets the category title.
		 */
		public void Initialize(String Title) {
			childs = new ArrayList<B4APreference>();
			this.title = Title;
		}
		@Override
		Preference createPreference(PreferenceActivity c) {
			PreferenceCategory pc = new PreferenceCategory(c);
			handleDefaults(pc);
			if (parent == null) {
				Common.Log("Error: PreferenceCategories cannot be nested!");
			}
			parent.addPreference(pc);
			for (B4APreference b : childs) {
				pc.addPreference(b.createPreference(c));
			}
			return pc;
		}
		@Override
		@Hide
		public Intent CreateIntent() {
			return null;
		}
	}
	static abstract class B4APreference implements Serializable{
		String key;
		Serializable defaultValue;
		String dependency;
		String title, summary;
		String intentAction;
		String intentDataString;
		String intentPackage;
		String intentClass;
		protected static int dummyKey;
		public B4APreference(String key, String title, String summary, Serializable defaultValue, String dependency) {
			this.key = key;
			this.defaultValue = defaultValue;
			this.summary = summary;
			this.title = title;
			this.dependency = dependency;
		}
		public B4APreference(String title, String summary, String dependency, String intentAction, ComponentName intentComponent, Uri intentData) {
			this.key = "_B4APreferenceDummy" + Integer.toString(++dummyKey);
			this.summary = summary;
			this.title = title;
			this.dependency = dependency;
			this.intentAction = intentAction;
			if (intentData != null)
				this.intentDataString = intentData.toString();
			else
				this.intentDataString = "";
			
			if (intentComponent != null) {
				intentPackage = intentComponent.getPackageName();
				intentClass = intentComponent.getClassName();
			}
			else {
				intentPackage = "";
				intentClass = "";
			}
		}
		abstract Preference createPreference(PreferenceActivity c) ;
		protected void handleDefaults(Preference p) {
			p.setKey(key);
			p.setDefaultValue(defaultValue);
			p.setTitle(title);
			p.setSummary(summary);
		}
	}
}