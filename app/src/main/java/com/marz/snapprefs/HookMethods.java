package com.marz.snapprefs;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XModuleResources;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputFilter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.marz.snapprefs.Util.NotificationUtils;
import com.marz.snapprefs.Util.TypefaceUtil;
import com.marz.snapprefs.Util.XposedUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.ByteArrayBuffer;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalStaticField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalStaticField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;


public class HookMethods implements IXposedHookInitPackageResources, IXposedHookLoadPackage, IXposedHookZygoteInit {


    public static final String SNAPCHAT_PACKAGE_NAME = "com.snapchat.android";
    // Modes for saving Snapchats
    public static final int SAVE_AUTO = 0;
    public static final int SAVE_S2S = 1;
    public static final int DO_NOT_SAVE = 2;
    // Length of toasts
    public static final int TOAST_LENGTH_SHORT = 0;
    public static final int TOAST_LENGTH_LONG = 1;
    // Minimum timer duration disabled
    public static final int TIMER_MINIMUM_DISABLED = 0;
    private static final String PACKAGE_NAME = HookMethods.class.getPackage().getName();
    // Preferences and their default values
    public static int mModeSnapImage = SAVE_AUTO;
    public static int mModeSnapVideo = SAVE_AUTO;
    public static int mModeStoryImage = SAVE_AUTO;
    public static int mModeStoryVideo = SAVE_AUTO;
    public static int mToastLength = TOAST_LENGTH_LONG;
    public static int mTimerMinimum = TIMER_MINIMUM_DISABLED;
    public static boolean mCustomFilterBoolean = false;
    public static boolean mPaintTools = true;
    public static boolean mMultiFilterBoolean = true;
    public static int mCustomFilterType;
    public static boolean mTimerUnlimited = true;
    public static boolean mHideTimerStory = false;
    public static boolean mLoopingVids = true;
    public static boolean mHideTimer = false;
    public static boolean mToastEnabled = true;
    public static boolean mVibrationEnabled = true;
    public static String mSavePath = "";
    public static String mCustomFilterLocation = "";
    public static String mConfirmationID = "";
    public static String mDeviceID = "";
    public static boolean mSaveSentSnaps = false;
    public static boolean mSortByCategory = true;
    public static boolean mSortByUsername = true;
    public static boolean mDebugging = true;
    public static boolean mOverlays = false;
    public static boolean mSpeed = false;
    public static boolean mWeather = false;
    public static boolean mDiscoverSnap = false;
    public static boolean mDiscoverUI = false;
    public static boolean mCustomSticker = false;
    public static boolean mReplay = false;
    public static boolean mStealth = false;
    public static boolean mTyping = false;
    public static int mLicense = 0;
    static XSharedPreferences prefs;
    static XSharedPreferences license;
    static boolean selectStory;
    static boolean selectVenue;
    static boolean txtcolours;
    static boolean bgcolours;
    static boolean size;
    static boolean transparency;
    static boolean rainbow;
    static boolean bg_transparency;
    static boolean txtstyle;
    static boolean txtgravity;
    static boolean debug;
    static EditText editText;
    static Typeface defTypeface;
    static boolean haveDefTypeface;
    static XModuleResources modRes;
    static Activity SnapContext;
    static Context context;
    static int counter = 0;
    private static XModuleResources mResources;
    private static int snapchatVersion;
    public static String MODULE_PATH = null;
    private static boolean fullCaption;
    private static boolean selectAll;
    private static boolean hideBf;
    private static boolean hideRecent;
    private static boolean shouldAddGhost;
    private static boolean shouldAddVFilters;
    private static boolean mColours;
    private static boolean mLocation;
    private static boolean mTimerCounter;
    private static boolean mChatAutoSave;
    private static boolean mChatImageSave;
    private static boolean mIntegration;
    private static InitPackageResourcesParam resParam;
    private static ClassLoader classLoader;
    Class CaptionEditText;
    boolean latest = false;
    public static ImageButton upload = null;

    public static int px(float f) {
        return Math.round((f * SnapContext.getResources().getDisplayMetrics().density));
    }

    public static int pxC(float f, Context ctx) {
        return Math.round((f * ctx.getResources().getDisplayMetrics().density));
    }

    static void refreshPreferences() {
        prefs = new XSharedPreferences(new File(
                Environment.getDataDirectory(), "data/"
                + PACKAGE_NAME + "/shared_prefs/" + PACKAGE_NAME
                + "_preferences" + ".xml"));
        prefs.reload();
        prefs.makeWorldReadable();
        fullCaption = prefs.getBoolean("pref_key_fulltext", false);
        selectAll = prefs.getBoolean("pref_key_selectall", false);
        selectStory = prefs.getBoolean("pref_key_selectstory", false);
        selectVenue = prefs.getBoolean("pref_key_selectvenue", false);
        hideBf = prefs.getBoolean("pref_key_hidebf", false);
        hideRecent = prefs.getBoolean("pref_key_hiderecent", false);
        txtcolours = prefs.getBoolean("pref_key_txtcolour", false);
        bgcolours = prefs.getBoolean("pref_key_bgcolour", false);
        size = prefs.getBoolean("pref_key_size", false);
        transparency = prefs.getBoolean("pref_key_size", false);
        rainbow = prefs.getBoolean("pref_key_rainbow", false);
        bg_transparency = prefs.getBoolean("pref_key_bg_transparency", false);
        txtstyle = prefs.getBoolean("pref_key_txtstyle", false);
        txtgravity = prefs.getBoolean("pref_key_txtgravity", false);
        mPaintTools = prefs.getBoolean("pref_key_paint_checkbox", mPaintTools);
        mTimerCounter = prefs.getBoolean("pref_key_timercounter", true);
        mChatAutoSave = prefs.getBoolean("pref_key_save_chat_text", true);
        mChatImageSave = prefs.getBoolean("pref_key_save_chat_image", true);
        mIntegration = prefs.getBoolean("pref_key_integration", true);
        mCustomFilterBoolean = prefs.getBoolean("pref_key_custom_filter_checkbox", mCustomFilterBoolean);
        mMultiFilterBoolean = prefs.getBoolean("pref_key_multi_filter_checkbox", mMultiFilterBoolean);
        mCustomFilterLocation = Environment.getExternalStorageDirectory().toString() + "/Snapprefs/Filters";
        mCustomFilterType = prefs.getInt("pref_key_filter_type", 0);
        mSpeed = prefs.getBoolean("pref_key_speed", false);
        mWeather = prefs.getBoolean("pref_key_weather", false);
        mLocation = prefs.getBoolean("pref_key_location", false);
        mDiscoverSnap = prefs.getBoolean("pref_key_discover", false);
        mDiscoverUI = prefs.getBoolean("pref_key_discover_ui", false);
        mCustomSticker = prefs.getBoolean("pref_key_sticker", false);
        mReplay = prefs.getBoolean("pref_key_replay", false);
        mStealth = prefs.getBoolean("pref_key_viewed", false);
        mTyping = prefs.getBoolean("pref_key_typing", false);
        mConfirmationID = prefs.getString("confirmation_id", "");
        debug = prefs.getBoolean("pref_key_debug", false);
        mDeviceID = prefs.getString("device_id", null);
        mLicense = prefs.getInt(mDeviceID, 0);

        //SAVING

        mModeSnapImage = prefs.getInt("pref_key_snaps_images", mModeSnapImage);
        mModeSnapVideo = prefs.getInt("pref_key_snaps_videos", mModeSnapVideo);
        mModeStoryImage = prefs.getInt("pref_key_stories_images", mModeStoryImage);
        mModeStoryVideo = prefs.getInt("pref_key_stories_videos", mModeStoryVideo);
        mTimerMinimum = prefs.getInt("pref_key_timer_minimum", mTimerMinimum);
        mToastEnabled = prefs.getBoolean("pref_key_toasts_checkbox", mToastEnabled);
        mVibrationEnabled = prefs.getBoolean("pref_key_vibration_checkbox", mVibrationEnabled);
        mToastLength = prefs.getInt("pref_key_toasts_duration", mToastLength);
        mSavePath = prefs.getString("pref_key_save_location", mSavePath);
        mSaveSentSnaps = prefs.getBoolean("pref_key_save_sent_snaps", mSaveSentSnaps);
        mSortByCategory = prefs.getBoolean("pref_key_sort_files_mode", mSortByCategory);
        mSortByUsername = prefs.getBoolean("pref_key_sort_files_username", mSortByUsername);
        mDebugging = prefs.getBoolean("pref_key_debug_mode", mDebugging);
        mOverlays = prefs.getBoolean("pref_key_overlay", mOverlays);
        mTimerUnlimited = prefs.getBoolean("pref_key_timer_unlimited", mTimerUnlimited);
        mHideTimerStory = prefs.getBoolean("pref_key_timer_story_hide", mHideTimerStory);
        mLoopingVids = prefs.getBoolean("pref_key_looping_video", mLoopingVids);
        mHideTimer = prefs.getBoolean("pref_key_timer_hide", mHideTimer);


        //SHARING

        Common.ROTATION_MODE = Integer.parseInt(prefs.getString("pref_rotation", Integer.toString(Common.ROTATION_MODE)));
        Common.ADJUST_METHOD = Integer.parseInt(prefs.getString("pref_adjustment", Integer.toString(Common.ADJUST_METHOD)));
        Common.CAPTION_UNLIMITED_VANILLA = prefs.getBoolean("pref_caption_unlimited_vanilla", Common.CAPTION_UNLIMITED_VANILLA);
        Common.CAPTION_UNLIMITED_FAT = prefs.getBoolean("pref_caption_unlimited_fat", Common.CAPTION_UNLIMITED_FAT);
        Common.DEBUGGING = prefs.getBoolean("pref_debug", Common.DEBUGGING);
        Common.CHECK_SIZE = !prefs.getBoolean("pref_size_disabled", !Common.CHECK_SIZE);
        Common.TIMBER = prefs.getBoolean("pref_timber", Common.TIMBER);

        if (txtcolours == true || bgcolours == true || size == true || rainbow == true || bg_transparency == true || txtstyle == true || txtgravity == true) {
            mColours = true;
        } else {
            mColours = false;
        }

        if (mSpeed || mColours || mLocation || mWeather) {
            shouldAddGhost = true;
        } else {
            shouldAddGhost = false;
        }
    }

    static void logging(String message) {
        if (mDebugging == true)
            XposedBridge.log(message);
    }

    public static void printStackTraces() {
        StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            Logger.log("Class name :: " + element.getClassName() + "  || method name :: " + element.getMethodName());
        }
    }

    public boolean postData() {

        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost("http://snapprefs.com/checkversion.php");


        try {
            // Add your data
            List nameValuePairs = new ArrayList(2);
            nameValuePairs.add(new BasicNameValuePair("version", "1.5.0"));
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);

            InputStream is = response.getEntity().getContent();
            BufferedInputStream bis = new BufferedInputStream(is);
            final ByteArrayBuffer baf = new ByteArrayBuffer(20);

            int current = 0;

            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }
            String text = new String(baf.toByteArray());
            String status = null;
            String error_msg = null;
            try {

                JSONObject obj = new JSONObject(text);
                status = obj.getString("status");
                error_msg = obj.getString("error_msg");
                if (status.equals("0") && !error_msg.isEmpty()) {
                    latest = true;
                }
                if (status.equals("1") && error_msg.isEmpty()) {
                    //Toast.makeText(SnapContext, "New version available, update NOW from the Xposed repo.", Toast.LENGTH_SHORT).show();
                    latest = false;
                }
            } catch (Throwable t) {
                Log.e("Snapprefs", "Could not parse malformed JSON: \"" + text + "\"");
                latest = false;
            }
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            //saveIntPreference("license_status", 0);
            latest = false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //saveIntPreference("license_status", 0);
            latest = false;
        }
        return latest;
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        MODULE_PATH = startupParam.modulePath;
        mResources = XModuleResources.createInstance(startupParam.modulePath, null);
        //refreshPreferences();
    }

    @Override
    public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(Common.PACKAGE_SNAP))
            return;
        mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
        mCustomFilterLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/Filters";
        refreshPreferences();
        resParam = resparam;
        modRes = XModuleResources.createInstance(MODULE_PATH, resparam.res);
        if (shouldAddGhost) {
            addIcons(resparam);
        }
        addShareIcons(resparam);
        fullScreenFilter(resparam);
    }

    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(Common.PACKAGE_SNAP))
            return;
        try {
            mSavePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs";
            mCustomFilterLocation = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/Filters";
            XposedUtils.log("----------------- SNAPPREFS HOOKED -----------------", false);
            Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
            context = (Context) callMethod(activityThread, "getSystemContext");
            classLoader = (ClassLoader) lpparam.classLoader;

            PackageInfo piSnapChat = context.getPackageManager().getPackageInfo(lpparam.packageName, 0);
            XposedUtils.log("SnapChat Version: " + piSnapChat.versionName + " (" + piSnapChat.versionCode + ")", false);
            XposedUtils.log("SnapPrefs Version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")", false);
            if (!Obfuscator.isSupported(piSnapChat.versionCode)) {
                Logger.log("This Snapchat version is unsupported", true, true);
                Toast.makeText(context, "This Snapchat version is unsupported", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            XposedUtils.log("Exception while trying to get version info", e);
            return;
        }
        findAndHookMethod("android.app.Application", lpparam.classLoader, "attach", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                refreshPreferences();
                printSettings();
                if (mLicense == 1 || mLicense == 2) {

                    if (mReplay == true) {
                        //Premium.initReplay(lpparam, modRes, SnapContext);
                    }
                    if (mTyping == true) {
                        Premium.initTyping(lpparam, modRes, SnapContext);
                    }
                    if (mStealth == true && mLicense == 2) {
                        Premium.initViewed(lpparam, modRes, SnapContext);
                    }
                }
        /*findAndHookMethod("com.snapchat.android.Timber", lpparam.classLoader, "c", String.class, String.class, Object[].class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Logger.log("TIMBER: " + param.args[0] + " : " + param.args[1], true);
            }
        });*/
                //Showing lenses or not
                findAndHookMethod(Obfuscator.misc.SHARINGICON_CLASS, lpparam.classLoader, "a", boolean.class, boolean.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if((boolean)param.args[0]){
                            upload.setVisibility(View.INVISIBLE);
                        } else {
                            upload.setVisibility(View.VISIBLE);
                        }
                    }
                });
                //Recording of video ended
                findAndHookMethod(Obfuscator.misc.SHARINGICON_CLASS, lpparam.classLoader, "c", boolean.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        upload.setVisibility(View.VISIBLE);
                    }
                });
                for (String s : Obfuscator.ROOTDETECTOR_METHODS) {
                    findAndHookMethod(Obfuscator.ROOTDETECTOR_CLASS, lpparam.classLoader, s, XC_MethodReplacement.returnConstant(false));
                    Logger.log("ROOTCHECK: " + s, true);
                }
                findAndHookMethod("android.media.MediaRecorder", lpparam.classLoader, "setMaxDuration", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        param.args[0] = 120000;
                    }
                });

                final Class<?> receivedSnapClass = findClass(Obfuscator.save.RECEIVEDSNAP_CLASS, lpparam.classLoader);
                try {
                    XposedHelpers.setStaticIntField(receivedSnapClass, "SECOND_MAX_VIDEO_DURATION", 99999);
                    Logger.log("SECOND_MAX_VIDEO_DURATION set over 10", true);
                } catch (Throwable t) {
                    Logger.log("SECOND_MAX_VIDEO_DURATION set over 10 failed :(", true);
                    Logger.log(t.toString());
                } /*For viewing longer videos?*/

                XC_MethodHook initHook = new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        SnapContext = (Activity) param.thisObject;
                        prefs.reload();
                        refreshPreferences();
                        //SNAPPREFS
                        Saving.initSaving(lpparam, mResources, SnapContext);
                        Lens.initLens(lpparam, mResources, SnapContext);
                        File vfilters = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Snapprefs/VisualFilters/xpro_map.png");
                        if (vfilters.exists()) {
                            VisualFilters.initVisualFilters(lpparam);
                        } else {
                            Toast.makeText(context, "VisualFilter files are missing, download them!", Toast.LENGTH_SHORT).show();
                        }
                        if (mMultiFilterBoolean == true) {
                            MultiFilter.initMultiFilter(lpparam, mResources, SnapContext);
                        }
                        if (mDiscoverSnap == true) {
                            DataSaving.blockDsnap(lpparam);
                        }
                        if (mDiscoverUI == true) {
                            DataSaving.blockFromUi(lpparam);
                        }
                        if (mSpeed == true) {
                            Spoofing.initSpeed(lpparam, SnapContext);
                        }
                        if (mLocation == true) {
                            Spoofing.initLocation(lpparam, SnapContext);
                        }
                        if (mWeather == true) {
                            Spoofing.initWeather(lpparam, SnapContext);
                        }
                        if (mPaintTools == true) {
                            PaintTools.initPaint(lpparam, mResources);
                        }
                        if (mTimerCounter == true) {
                            Misc.initTimer(lpparam, mResources);
                        }
                        if (mChatAutoSave == true) {
                            Chat.initTextSave(lpparam, mResources);
                        }
                        if (mChatImageSave == true) {
                            Chat.initImageSave(lpparam, mResources);
                        }
                        if (mIntegration == true) {
                            initIntegration(lpparam);
                        }
                        getEditText(lpparam);
                        findAndHookMethod(Obfuscator.save.SCREENSHOTDETECTOR_CLASS, lpparam.classLoader, Obfuscator.save.SCREENSHOTDETECTOR_RUN, LinkedHashMap.class, XC_MethodReplacement.DO_NOTHING);
                        findAndHookMethod(Obfuscator.save.SNAPSTATEMESSAGE_CLASS, lpparam.classLoader, Obfuscator.save.SNAPSTATEMESSAGE_SETSCREENSHOTCOUNT, Long.class, new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                param.args[0] = 0L;
                                Logger.log("StateBuilder.setScreenshotCount set to 0L", true);
                            }
                        });
                        if (mCustomSticker == true) {
                            Stickers.initStickers(lpparam, modRes, SnapContext);
                        }
                    }
                };

                findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onCreate", Bundle.class, initHook);
                findAndHookMethod("com.snapchat.android.LandingPageActivity", lpparam.classLoader, "onResume", initHook);

                // VanillaCaptionEditText was moved from an inner-class to a separate class in 8.1.0
                String vanillaCaptionEditTextClassName = "com.snapchat.android.ui.caption.VanillaCaptionEditText";
                hookAllConstructors(findClass(vanillaCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (Common.CAPTION_UNLIMITED_VANILLA) {
                            XposedUtils.log("Unlimited vanilla captions");
                            EditText vanillaCaptionEditText = (EditText) param.thisObject;
                            // Set single lines mode to false
                            vanillaCaptionEditText.setSingleLine(false);

                            // Remove actionDone IME option, by only setting flagNoExtractUi
                            vanillaCaptionEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                            // Remove listener hiding keyboard when enter is pressed by setting the listener to null
                            vanillaCaptionEditText.setOnEditorActionListener(null);
                            // Remove listener for cutting of text when the first line is full by setting the text change listeners list to null
                            setObjectField(vanillaCaptionEditText, "mListeners", null);
                        }
                    }
                });

                // FatCaptionEditText was moved from an inner-class to a separate class in 8.1.0
                String fatCaptionEditTextClassName = "com.snapchat.android.ui.caption.FatCaptionEditText";
                hookAllConstructors(findClass(fatCaptionEditTextClassName, lpparam.classLoader), new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (Common.CAPTION_UNLIMITED_FAT) {
                            XposedUtils.log("Unlimited fat captions");
                            EditText fatCaptionEditText = (EditText) param.thisObject;
                            // Remove InputFilter with character limit
                            fatCaptionEditText.setFilters(new InputFilter[0]);

                            // Remove actionDone IME option, by only setting flagNoExtractUi
                            fatCaptionEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                            // Remove listener hiding keyboard when enter is pressed by setting the listener to null
                            fatCaptionEditText.setOnEditorActionListener(null);
                            // Remove listener for removing new lines by setting the text change listeners list to null
                            setObjectField(fatCaptionEditText, "mListeners", null);
                        }
                    }
                });
                //SNAPSHARE
                Sharing.initSharing(lpparam, mResources);
                //SNAPPREFS
                if (hideBf == true) {
                    findAndHookMethod("com.snapchat.android.model.Friend", lpparam.classLoader, Obfuscator.FRIENDS_BF, new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param)
                                throws Throwable {
                            //logging("Snap Prefs: Removing Best-friends");
                            return false;
                        }
                    });
                }
		/*if (hideRecent == true){
        findAndHookMethod(Common.Class_Friend, lpparam.classLoader, Common.Method_Recent, new XC_MethodReplacement(){
		@Override
		protected Object replaceHookedMethod(MethodHookParam param)
				throws Throwable {
			logging("Snap Prefs: Removing Recents");
			return false;
        }
		});
		}*/
                if (mCustomFilterBoolean == true) {
                    addFilter(lpparam);
                }
                if (selectAll == true) {
                    HookSendList.initSelectAll(lpparam);
                }


            }
        });
    }


    private void addFilter(LoadPackageParam lpparam) {
        //Replaces the batteryfilter with our custom one
        findAndHookMethod(ImageView.class, "setImageResource", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                try {
                    XModuleResources modRes = XModuleResources.createInstance(MODULE_PATH, null);
                    ImageView iv = (ImageView) param.thisObject;
                    int resId = (Integer) param.args[0];
                    if (iv != null)
                        if (iv.getContext().getPackageName().equals("com.snapchat.android"))
                            if (resId == iv.getContext().getResources().getIdentifier("camera_batteryfilter_full", "drawable", "com.snapchat.android"))
                                if (mCustomFilterLocation == null) {
                                    iv.setImageDrawable(modRes.getDrawable(R.drawable.custom_filter_1));
                                    Logger.log("Replaced batteryfilter from R.drawable", true);
                                } else {
                                    if (mCustomFilterType == 0) {
                                        iv.setImageDrawable(Drawable.createFromPath(mCustomFilterLocation + "/fullscreen_filter.png"));
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                    } else if (mCustomFilterType == 1) {
                                        //iv.setImageDrawable(modRes.getDrawable(R.drawable.imsafe));
                                        iv.setImageDrawable(Drawable.createFromPath(mCustomFilterLocation + "/banner_filter.png"));
                                    }
                                    Logger.log("Replaced batteryfilter from " + mCustomFilterLocation + " Type: " + mCustomFilterType, true);
                                }
                    //else if (resId == iv.getContext().getResources().getIdentifier("camera_batteryfilter_empty", "drawable", "com.snapchat.android"))
                    //    iv.setImageDrawable(modRes.getDrawable(R.drawable.custom_filter_1)); quick switch to a 2nd filter?
                } catch (Throwable t) {
                    XposedBridge.log(t);
                }
            }
        });
        //Used to emulate the battery status as being FULL -> above 90%
        final Class<?> batteryInfoProviderEnum = findClass("com.snapchat.android.location.smartFilterProviders.BatteryInfoProvider$BatteryLevel", lpparam.classLoader);
        findAndHookMethod(Obfuscator.spoofing.BATTERY_FILTER, lpparam.classLoader, "a", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object battery = getStaticObjectField(batteryInfoProviderEnum, "FULL_BATTERY");
                param.setResult(battery);
            }
        });
    }

    private void printSettings() {

        Logger.log("\nTo see the advanced output enable debugging mode in the Support tab", true);

        logging("\n~~~~~~~~~~~~ SNAPPREFS SETTINGS");
        logging("FullCaption: " + fullCaption);
        logging("SelectAll: " + selectAll);
        logging("SelectStory: " + selectStory);
        logging("SelectVenue: " + selectVenue);
        logging("HideBF: " + hideBf);
        logging("HideRecent: " + hideRecent);
        logging("ShouldAddGhost: " + shouldAddGhost);
        logging("TxtColours: " + txtcolours);
        logging("BgColours: " + bgcolours);
        logging("Size: " + size);
        logging("Transparency: " + transparency);
        logging("Rainbow: " + rainbow);
        logging("Background Transparency: " + bg_transparency);
        logging("TextStyle: " + txtstyle);
        logging("TextGravity: " + txtgravity);
        logging("mTimerCounter: " + mTimerCounter);
        logging("mChatAutoSave: " + mChatAutoSave);
        logging("mChatImageSave: " + mChatImageSave);
        logging("mIntegration: " + mIntegration);
        logging("mPaintTools: " + mPaintTools);
        logging("CustomFilters: " + mCustomFilterBoolean);
        logging("MultiFilters: " + mMultiFilterBoolean);
        logging("CustomFiltersLocation: " + mCustomFilterLocation);
        logging("CustomFilterType: " + mCustomFilterType);
        logging("mSpeed: " + mSpeed);
        logging("mWeather: " + mWeather);
        logging("mLocation: " + mLocation);
        logging("mDiscoverSnap: " + mDiscoverSnap);
        logging("mDiscoverUI: " + mDiscoverUI);
        logging("mCustomSticker: " + mCustomSticker);
        logging("mReplay: " + mReplay);
        logging("mStealth: " + mStealth);
        logging("mTyping: " + mTyping);
        logging("mColours: " + mColours);
        logging("*****Debugging: " + debug + " *****");
        logging("mLicense: " + mLicense);
        logging("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        Logger.setDebuggingEnabled(mDebugging);

        logging("----------------------- SAVING SETTINGS -----------------------");
        logging("Preferences have changed:");
        String[] saveModes = {"SAVE_AUTO", "SAVE_S2S", "DO_NOT_SAVE"};
        logging("~ mModeSnapImage: " + saveModes[mModeSnapImage]);
        logging("~ mModeSnapVideo: " + saveModes[mModeSnapVideo]);
        logging("~ mModeStoryImage: " + saveModes[mModeStoryImage]);
        logging("~ mModeStoryVideo: " + saveModes[mModeStoryVideo]);
        logging("~ mOverlays: " + mOverlays);
        logging("~ mTimerMinimum: " + mTimerMinimum);
        logging("~ mToastEnabled: " + mToastEnabled);
        logging("~ mVibrationEnabled: " + mVibrationEnabled);
        logging("~ mToastLength: " + mToastLength);
        logging("~ mSavePath: " + mSavePath);
        logging("~ mSaveSentSnaps: " + mSaveSentSnaps);
        logging("~ mSortByCategory: " + mSortByCategory);
        logging("~ mSortByUsername: " + mSortByUsername);
        logging("~ mTimerUnlimited: " + mTimerUnlimited);
        logging("~ mHideTimerStory: " + mHideTimerStory);
        logging("~ mLoopingVids: " + mLoopingVids);
        logging("~ mHideTimer: " + mHideTimer);
    }

    public void getEditText(LoadPackageParam lpparam) {
        this.CaptionEditText = XposedHelpers.findClass("com.snapchat.android.ui.caption.CaptionEditText", lpparam.classLoader);
        XposedBridge.hookAllConstructors(this.CaptionEditText, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws PackageManager.NameNotFoundException {
                refreshPreferences();
                editText = (EditText) param.thisObject;
                if (!haveDefTypeface) {
                    defTypeface = editText.getTypeface();
                    haveDefTypeface = true;
                }
            }
        });
    }
    public boolean setInt = false;
    public void initIntegration(LoadPackageParam lpparam) {
            findAndHookMethod("com.snapchat.android.fragments.addfriends.ProfileFragment", lpparam.classLoader, "onCreateView", LayoutInflater.class, ViewGroup.class, Bundle.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    TableLayout navigation = (TableLayout) ((LinearLayout) XposedHelpers.getObjectField(param.thisObject, "z")).getChildAt(0);
                    ImageView orig = (ImageView) ((TableRow) navigation.getChildAt(0)).getChildAt(0);
                    TextView orig1 = (TextView) ((TableRow) navigation.getChildAt(0)).getChildAt(1);
                    TableRow row = new TableRow(navigation.getContext());
                    row.setLayoutParams(navigation.getChildAt(0).getLayoutParams());
                    ImageView iv = new ImageView(navigation.getContext());
                    iv.setImageDrawable(mResources.getDrawable(R.drawable.profile_snapprefs));
                    iv.setLayoutParams(orig.getLayoutParams());
                    TextView textView = new TextView(navigation.getContext());
                    textView.setText("Open Snapprefs");
                    textView.setTextColor(orig1.getCurrentTextColor());
                    textView.setTextSize(24);
                    textView.setLayoutParams(orig1.getLayoutParams());
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (context==null){
                                Logger.log("CONTEXT IS NULL IN INITINTEGRATION");
                            }
                            //Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.marz.snapprefs");
                            Intent launchIntent = new Intent(Intent.ACTION_MAIN);
                            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            launchIntent.setComponent(new ComponentName("com.marz.snapprefs","com.marz.snapprefs.MainActivity2"));
                            context.startActivity(launchIntent);
                        }
                    });
                    row.addView(iv);
                    row.addView(textView);
                    navigation.addView(row);
                    if(setInt==false){
                        setInt=true;
                    }
                    else {//cheap ass fix
                        navigation.removeView(row);
                    }
                }
            });
        }

    public void fullScreenFilter(InitPackageResourcesParam resparam) {
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "battery_view", new XC_LayoutInflated() {
            LinearLayout.LayoutParams batteryLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

            @Override
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                View battery = (View) liparam.view.findViewById(liparam.res.getIdentifier("battery_icon", "id", "com.snapchat.android"));
                battery.setLayoutParams(batteryLayoutParams);
                battery.setPadding(0, 0, 0, 0);
                Logger.log("fullScreenFilter", true);
            }
        });
    }
    public void addShareIcons(InitPackageResourcesParam resparam){
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "camera_preview", new XC_LayoutInflated() {
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                final RelativeLayout relativeLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("camera_preview_layout", "id", Common.PACKAGE_SNAP));
                final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("camera_take_snap_button", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.bottomMargin = px(65.0f);
                upload = new ImageButton(SnapContext);
                upload.setBackgroundColor(0);
                Drawable uploadimg = SnapContext.getResources().getDrawable(+0x7f020024); //aa_chat_camera_upload - 0x7f020024
                //upload.setImageDrawable(mResources.getDrawable(R.drawable.triangle));
                upload.setImageDrawable(uploadimg);
                upload.setScaleX((float) 0.55);
                upload.setScaleY((float) 0.55);
                upload.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent launchIntent = new Intent(Intent.ACTION_RUN);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        launchIntent.setComponent(new ComponentName("com.marz.snapprefs","com.marz.snapprefs.PickerActivity"));
                        context.startActivity(launchIntent);
                    }
                });
                SnapContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        relativeLayout.addView(upload, layoutParams);
                    }
                });

            }
        });
    }
    public void addIcons(InitPackageResourcesParam resparam) {
        resparam.res.hookLayout(Common.PACKAGE_SNAP, "layout", "snap_preview", new XC_LayoutInflated() {
            public void handleLayoutInflated(LayoutInflatedParam liparam) throws Throwable {
                final RelativeLayout relativeLayout = (RelativeLayout) liparam.view.findViewById(liparam.res.getIdentifier("snap_preview_header", "id", Common.PACKAGE_SNAP)).getParent();

                final RelativeLayout outerOptionsLayout = new RelativeLayout(SnapContext);
                final GridView innerOptionsView = new GridView(SnapContext);
                innerOptionsView.setAdapter(new OptionsAdapter(SnapContext, mResources));
                innerOptionsView.setNumColumns(3);
                innerOptionsView.setHorizontalSpacing(px(2.0f));
                innerOptionsView.setVerticalSpacing(px(5.0f));
                innerOptionsView.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
                innerOptionsView.setPadding(0,px(7.5f), px(1.0f), px(7.5f));
                final RelativeLayout.LayoutParams outerOptionsLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                outerOptionsLayoutParams.topMargin = px(55.0f);
                outerOptionsLayoutParams.bottomMargin = px(300.0f);
                outerOptionsLayoutParams.leftMargin = px(75.0f);
                outerOptionsLayoutParams.rightMargin = px(10.0f);
                outerOptionsLayout.setVisibility(View.GONE);
                outerOptionsLayout.setBackgroundDrawable(mResources.getDrawable(R.drawable.optionsbackground));
                outerOptionsLayout.addView(innerOptionsView, GridLayout.LayoutParams.MATCH_PARENT, GridLayout.LayoutParams.MATCH_PARENT);

                final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                layoutParams.topMargin = px(45.0f);
                layoutParams.leftMargin = px(10.0f);
                final ImageButton textButton = new ImageButton(SnapContext);
                textButton.setBackgroundColor(0);
                textButton.setImageDrawable(mResources.getDrawable(R.drawable.triangle));
                textButton.setScaleX((float) 0.75);
                textButton.setScaleY((float) 0.75);
                textButton.setOnClickListener(new View.OnClickListener() {
                    boolean shouldHideOptions = true;
                    @Override
                    public void onClick(View v) {
                        if(editText.getText().length()>=1){
                            if(shouldHideOptions){
                                outerOptionsLayout.setVisibility(View.VISIBLE);
                                shouldHideOptions = false;
                            }else{
                                outerOptionsLayout.setVisibility(View.GONE);
                                shouldHideOptions = true;
                            }
                            logging("SnapPrefs: Displaying Options");
                        } else {
                            outerOptionsLayout.setVisibility(View.GONE);
                            shouldHideOptions = true;
                            Toast.makeText(context, "Your caption is missing", Toast.LENGTH_SHORT).show();
                            logging("SnapPrefs: Not displaying Options - edittext empty");
                        }
                    }
                });
                final RelativeLayout.LayoutParams paramsSpeed = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsSpeed.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsSpeed.topMargin = px(90.0f);
                paramsSpeed.leftMargin = px(10.0f);
                final ImageButton speed = new ImageButton(SnapContext);
                speed.setBackgroundColor(0);
                speed.setImageDrawable(mResources.getDrawable(R.drawable.speed));
                speed.setScaleX((float) 0.4);
                speed.setScaleY((float) 0.4);
                speed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.SpeedDialog(SnapContext);
                        logging("SnapPrefs: Displaying SpeedDialog");
                    }
                });
                final RelativeLayout.LayoutParams paramsWeather = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsWeather.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsWeather.topMargin = px(180.0f);
                paramsWeather.leftMargin = px(10.0f);
                final ImageButton weather = new ImageButton(SnapContext);
                weather.setBackgroundColor(0);
                weather.setImageDrawable(mResources.getDrawable(R.drawable.weather));
                weather.setScaleX((float) 0.4);
                weather.setScaleY((float) 0.4);
                weather.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Dialogs.WeatherDialog(SnapContext);
                        logging("SnapPrefs: Displaying WeatherDialog");
                    }
                });
                final RelativeLayout.LayoutParams paramsLocation = new RelativeLayout.LayoutParams(liparam.view.findViewById(liparam.res.getIdentifier("drawing_btn", "id", Common.PACKAGE_SNAP)).getLayoutParams());
                paramsLocation.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.ALIGN_PARENT_TOP);
                paramsLocation.topMargin = px(135.0f);
                paramsLocation.leftMargin = px(10.0f);
                final ImageButton location = new ImageButton(SnapContext);
                location.setBackgroundColor(0);
                location.setImageDrawable(mResources.getDrawable(R.drawable.location));
                location.setScaleX((float) 0.4);
                location.setScaleY((float) 0.4);
                location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setComponent(new ComponentName("com.marz.snapprefs", "com.marz.snapprefs.MapsActivity"));
                        SnapContext.startActivity(intent);
                        logging("SnapPrefs: Displaying Map");
                    }
                });
                SnapContext.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mColours == true) {
                            relativeLayout.addView(textButton, layoutParams);
                            relativeLayout.addView(outerOptionsLayout, outerOptionsLayoutParams);
                        }
                        if (mSpeed == true) {
                            relativeLayout.addView(speed, paramsSpeed);
                        }
                        if (mLocation == true) {
                            relativeLayout.addView(location, paramsLocation);
                        }
                        if (mWeather == true) {
                            relativeLayout.addView(weather, paramsWeather);
                        }
                    }
                });
            }
        });
    }

    private static class OptionsAdapter extends BaseAdapter {
        String[] options = {"Text Color", "Text Size", "Text Transparency", "Text Gradient","Text Alignment", "Text Style", "Text Font", "Background Color", "Background Transparency", "Background Gradient", "Reset"};
        Context context;
        XModuleResources mRes;
        int [] optionImageId = {R.drawable.text_color, R.drawable.text_size, R.drawable.text_transparency, R.drawable.text_gradient, R.drawable.text_alignment, R.drawable.text_style, R.drawable.text_font, R.drawable.bg_color, R.drawable.bg_transparency, R.drawable.bg_gradient, R.drawable.reset};
        private static LayoutInflater inflater=null;

        public OptionsAdapter(Activity snapContext, XModuleResources mRes) {
            this.context = snapContext;
            this.mRes = mRes;
            inflater = ( LayoutInflater ) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public class Holder
        {
            TextView tv;
            ImageView img;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final int[] colorsBg = new int[]{Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE};
            final int[] currentItemBg = {2};
            final int[] colorsText = new int[]{Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE};
            final int[] currentItemText = {2};
            Holder holder=new Holder();
            View rowView;

            rowView = inflater.inflate(mRes.getLayout(R.layout.optionlayout), null);
            holder.tv=(TextView) rowView.findViewById(mRes.getIdentifier("description", "id", "com.marz.snapprefs"));
            holder.img=(ImageView) rowView.findViewById(mRes.getIdentifier("textIcon", "id", "com.marz.snapprefs"));

            holder.tv.setText(options[position]);
            holder.img.setImageDrawable(mRes.getDrawable(optionImageId[position]));

            rowView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    switch (position) {
                        case 0: { //textColor
                            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, Color.WHITE, new ColorPickerDialog.OnColorSelectedListener() {

                                @Override
                                public void onColorSelected(int color) {
                                    // TODO Auto-generated method stub
                                    editText.setTextColor(color);
                                }
                            });
                            colorPickerDialog.setButton(-3, Common.dialog_default, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    // TODO Auto-generated method stub
                                    editText.setTextColor(Color.WHITE);
                                    editText.setAlpha(1);
                                }
                            });
                            colorPickerDialog.setTitle(Common.dialog_txtcolour);
                            colorPickerDialog.show();
                            return;
                        }
                        case 1: { //textSize
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            SeekBar seekBar = new SeekBar(context);
                            seekBar.setMax(150);
                            seekBar.setProgress((int) editText.getTextSize());
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar, int n, boolean bl) {
                                    editText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, n);
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar arg0) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar arg0) {
                                    // TODO Auto-generated method stub
                                }

                            });
                            builder.setNeutralButton(Common.dialog_default, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.5125f);
                                }
                            });
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.setView((View) seekBar);
                            builder.show();
                            return;
                        }
                        case 2: { //textAlpha
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            SeekBar seekBar = new SeekBar(context);
                            seekBar.setMax(100);
                            seekBar.setProgress((int) editText.getAlpha() * 100);
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar2, int n, boolean bl) {
                                    float alpha = (float) n / 100;
                                    editText.setAlpha(alpha);
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar arg0) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar arg0) {
                                    // TODO Auto-generated method stub
                                }

                            });
                            builder.setNeutralButton(Common.dialog_default, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editText.setAlpha(1);
                                }
                            });
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.setView((View) seekBar);
                            builder.show();
                            return;
                        }
                        case 3: { //textGradient
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Text Gradient");
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final int[] usedColors = new int[currentItemText[0]];
                                    System.arraycopy(colorsText, 0, usedColors, 0, currentItemText[0]);
                                    Shader textShader = new LinearGradient(0, 0, 0, editText.getHeight(), usedColors, null, Shader.TileMode.CLAMP);
                                    editText.getPaint().setShader(textShader);
                                    editText.setText(editText.getText());
                                }
                            });
                            LinearLayout rootLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            rootLayout.addView(inflater.inflate(modRes.getLayout(R.layout.gradient_layout), null), rootParams);
                            final LinearLayout listLayout = (LinearLayout) rootLayout.findViewById(R.id.itemLayout);
                            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                            for (int i = 1; i <= 5; i++) {
                                Button btn = new Button(context);
                                btn.setId(i);
                                final int id_ = btn.getId();
                                btn.setText("Color: " + id_);
                                btn.setBackgroundColor(colorsText[i - 1]);
                                listLayout.addView(btn, params);
                                final Button btn1 = ((Button) listLayout.findViewById(id_));
                                btn1.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View view) {
                                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, colorsText[id_-1], new ColorPickerDialog.OnColorSelectedListener() {
                                            @Override
                                            public void onColorSelected(int color) {
                                                // TODO Auto-generated method stub
                                                colorsText[id_-1] = color;
                                                btn1.setBackgroundColor(colorsText[id_-1]);
                                            }
                                        });
                                        colorPickerDialog.setTitle("Color: " + id_);
                                        colorPickerDialog.show();
                                    }
                                });
                                if (btn1.getId() <= currentItemText[0]) {
                                    btn1.setVisibility(View.VISIBLE);
                                } else {
                                    btn1.setVisibility(View.GONE);
                                }
                            }
                            Button add = (Button) rootLayout.findViewById(R.id.add);
                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemText[0] < 5) {
                                        currentItemText[0]++;
                                        listLayout.findViewById(currentItemText[0]).setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(context, "You cannot add more than 5 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Button remove = (Button) rootLayout.findViewById(R.id.remove);
                            remove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemText[0] > 2) {
                                        listLayout.findViewById(currentItemText[0]).setVisibility(View.GONE);
                                        currentItemText[0]--;
                                    } else {
                                        Toast.makeText(context, "You cannot have less than 2 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            builder.setView(rootLayout);
                            builder.show();
                            return;
                        }
                        case 4: { //textAlignment
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            Button button_left = new Button(context);
                            Button button_center = new Button(context);
                            Button button_right = new Button(context);
                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setOrientation(1);
                            button_left.setText(Common.dialog_left);
                            button_left.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    editText.setGravity(Gravity.LEFT);
                                }
                            });
                            button_center.setText(Common.dialog_center);
                            button_center.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    editText.setGravity(Gravity.CENTER);
                                }
                            });
                            button_right.setText(Common.dialog_right);
                            button_right.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    editText.setGravity(Gravity.RIGHT);
                                }
                            });
                            linearLayout.addView((View) button_left);
                            linearLayout.addView((View) button_center);
                            linearLayout.addView((View) button_right);
                            builder.setView((View) linearLayout);
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.show();
                            return;
                        }
                        case 5: { //textStyle
                            //TODO: checkboxes
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            Button button_bold = new Button(context);
                            Button button_italic = new Button(context);
                            Button button_bolditalic = new Button(context);
                            Button button_normal = new Button(context);
                            LinearLayout linearLayout = new LinearLayout(context);
                            linearLayout.setOrientation(LinearLayout.VERTICAL);
                            button_bold.setText(Common.dialog_bold);
                            button_bold.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    editText.setTypeface(null, Typeface.BOLD);
                                }
                            });
                            button_italic.setText(Common.dialog_italic);
                            button_italic.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    editText.setTypeface(null, Typeface.ITALIC);
                                }
                            });
                            button_bolditalic.setText(Common.dialog_bolditalic);
                            button_bolditalic.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    editText.setTypeface(null, Typeface.BOLD_ITALIC);
                                }
                            });
                            button_normal.setText(Common.dialog_normal);
                            button_normal.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View view) {
                                    editText.setTypeface(null, Typeface.NORMAL);
                                }
                            });
                            linearLayout.addView((View) button_bold);
                            linearLayout.addView((View) button_italic);
                            linearLayout.addView((View) button_bolditalic);
                            linearLayout.addView((View) button_normal);
                            builder.setView((View) linearLayout);
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.show();
                            return;
                        }
                        case 6: { //textFont
                            File folder = new File(Environment.getExternalStorageDirectory() + "/Snapprefs/Fonts");
                            if (folder.exists()) {
                                FilenameFilter filter = new FilenameFilter() {
                                    @Override
                                    public boolean accept(File dir, String filename) {
                                        if (filename.lastIndexOf('.') > 0) {
                                            int lastIndex = filename.lastIndexOf('.');
                                            String extension = filename.substring(lastIndex);
                                            if (extension.equalsIgnoreCase(".ttf")) {
                                                return true;
                                            }
                                        }
                                        return false;
                                    }
                                };
                                File[] fonts = folder.listFiles(filter);
                                if (fonts.length > 0) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setTitle("Font list");
                                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                        }
                                    });
                                    LinearLayout rootLayout = new LinearLayout(context);
                                    LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                                    LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    rootLayout.addView(inflater.inflate(modRes.getLayout(R.layout.font_list), null), rootParams);
                                    LinearLayout listLayout = (LinearLayout) rootLayout.findViewById(R.id.fontLayout);
                                    for (final File font : fonts) {
                                        String fontname = font.getName().substring(0, font.getName().toLowerCase().lastIndexOf("."));
                                        TextView item = new TextView(context);
                                        item.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                        item.setPadding(0, 0, 0, 2);
                                        item.setText(fontname);
                                        item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22.0f);
                                        item.setGravity(Gravity.CENTER_HORIZONTAL);
                                        item.setTypeface(TypefaceUtil.get(font));
                                        item.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                editText.setTypeface(TypefaceUtil.get(font));
                                            }
                                        });
                                        listLayout.addView(item);
                                    }
                                    builder.setView(rootLayout);
                                    builder.show();
                                } else {
                                    NotificationUtils.showMessage("Fonts folder is empty", Color.RED, NotificationUtils.LENGHT_SHORT, classLoader);
                                }
                            } else {
                                NotificationUtils.showMessage("Fonts folder is not available", Color.RED, NotificationUtils.LENGHT_SHORT, classLoader);
                            }
                            return;
                        }
                        case 7: { //bgColor
                            ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, Color.BLACK, new ColorPickerDialog.OnColorSelectedListener() {

                                @Override
                                public void onColorSelected(int color) {
                                    // TODO Auto-generated method stub
                                    editText.setBackgroundColor(color);
                                }
                            });
                            colorPickerDialog.setButton(-3, Common.dialog_default, new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialogInterface, int which) {
                                    // TODO Auto-generated method stub
                                    editText.setBackgroundColor((Color.parseColor("#000000")));
                                    editText.setAlpha(1);
                                }
                            });
                            colorPickerDialog.setTitle(Common.dialog_bgcolour);
                            colorPickerDialog.show();
                            return;
                        }
                        case 8: { //bgAlpha
                            AlertDialog.Builder builder = new AlertDialog.Builder(SnapContext);
                            SeekBar seekBar = new SeekBar(SnapContext);
                            seekBar.setMax(255);
                            seekBar.setProgress((int) editText.getBackground().getAlpha());
                            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                public void onProgressChanged(SeekBar seekBar3, int n, boolean bl) {
                                    editText.getBackground().setAlpha(n);
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar arg0) {
                                    // TODO Auto-generated method stub

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar arg0) {
                                    // TODO Auto-generated method stub

                                }

                            });
                            builder.setNeutralButton(Common.dialog_default, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    editText.getBackground().setAlpha(153);
                                }
                            });
                            builder.setPositiveButton(Common.dialog_done, null);
                            builder.setView((View) seekBar);
                            builder.show();
                            return;
                        }
                        case 9: { //bgGradient
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Background Gradient");
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            builder.setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    final int[] usedColors = new int[currentItemBg[0]];
                                    System.arraycopy(colorsBg, 0, usedColors, 0, currentItemBg[0]);
                                    PaintDrawable p = new PaintDrawable();
                                    p.setShape(new RectShape());
                                    ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
                                        @Override
                                        public Shader resize(int width, int height) {
                                            return new LinearGradient(0, 0, width, height,
                                                    usedColors,
                                                    null, Shader.TileMode.MIRROR);
                                        }
                                    };
                                    p.setShaderFactory(sf);
                                    editText.setBackgroundDrawable(p);
                                }
                            });
                            LinearLayout rootLayout = new LinearLayout(context);
                            LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            rootLayout.addView(inflater.inflate(modRes.getLayout(R.layout.gradient_layout), null), rootParams);
                            final LinearLayout listLayout = (LinearLayout) rootLayout.findViewById(R.id.itemLayout);
                            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT);

                            for (int i = 1; i <= 5; i++) {
                                Button btn = new Button(context);
                                btn.setId(i);
                                final int id_ = btn.getId();
                                btn.setText("Color: " + id_);
                                btn.setBackgroundColor(colorsBg[i - 1]);
                                listLayout.addView(btn, params);
                                final Button btn1 = ((Button) listLayout.findViewById(id_));
                                btn1.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View view) {
                                        ColorPickerDialog colorPickerDialog = new ColorPickerDialog(context, colorsBg[id_-1], new ColorPickerDialog.OnColorSelectedListener() {

                                            @Override
                                            public void onColorSelected(int color) {
                                                // TODO Auto-generated method stub
                                                colorsBg[id_-1] = color;
                                                btn1.setBackgroundColor(colorsBg[id_-1]);
                                            }
                                        });
                                        colorPickerDialog.setTitle("Color: " + id_);
                                        colorPickerDialog.show();
                                    }
                                });
                                if (btn1.getId() <= currentItemBg[0]) {
                                    btn1.setVisibility(View.VISIBLE);
                                } else {
                                    btn1.setVisibility(View.GONE);
                                }
                            }
                            Button add = (Button) rootLayout.findViewById(R.id.add);
                            add.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemBg[0] < 5) {
                                        currentItemBg[0]++;
                                        listLayout.findViewById(currentItemBg[0]).setVisibility(View.VISIBLE);
                                    } else {
                                        Toast.makeText(context, "You cannot add more than 5 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            Button remove = (Button) rootLayout.findViewById(R.id.remove);
                            remove.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (currentItemBg[0] > 2) {
                                        listLayout.findViewById(currentItemBg[0]).setVisibility(View.GONE);
                                        currentItemBg[0]--;
                                    } else {
                                        Toast.makeText(context, "You cannot have less than 2 colors", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            builder.setView(rootLayout);
                            builder.show();
                            return;
                        }
                        case 10: { //reset
                            editText.setBackgroundDrawable(null);
                            editText.getPaint().reset();
                            editText.setTextColor(Color.WHITE);
                            editText.setBackgroundColor((Color.parseColor("#000000")));
                            editText.setGravity(Gravity.CENTER);
                            editText.setAlpha(1);
                            editText.getBackground().setAlpha(153);
                            editText.setTypeface(defTypeface);
                            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX, 32.5125f);
                            return;
                        }
                        default:
                            return;
                    }
                }
            });

            return rowView;
        }
    }
}