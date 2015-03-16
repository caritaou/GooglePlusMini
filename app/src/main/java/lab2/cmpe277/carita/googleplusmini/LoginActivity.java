package lab2.cmpe277.carita.googleplusmini;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import com.google.android.gms.plus.PlusClient;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Person;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class LoginActivity extends Activity {

    //String token = GoogleAuthUtil.getToken(mActivity, mEmail, mScopes);
    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
    // /https://developers.google.com/+/domains/profiles
    // https://developers.google.com/+/domains/circles/
    //private static final String SCOPE = "oauth2: https://www.googleapis.com/auth/userinfo.profile https://www.googleapis.com/auth/plus.circles.read";
    private static final String SCOPE;
    private SignInButton sign_in_button;
    private static String[] scopes = new String[]{
            "https://www.googleapis.com/auth/plus.me",
            "https://www.googleapis.com/auth/plus.circles.read",
            "https://www.googleapis.com/auth/plus.profiles.read",
    };

    static {
        SCOPE = "oauth2: " + TextUtils.join(" ", scopes);
    }
    private GetUsernameTask user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUsername();

//                Intent activity = new Intent(getApplicationContext(), PlusActivity.class);
//                activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                activity.putExtra("accessToken", accessToken);
//                startActivity(activity);
            }
        });


    }


    private void getUsername() {
        if (mEmail == null) {
            pickUserAccount();
        } else {
            if (isDeviceOnline()) {
//                new GetUsernameTask(LoginActivity.this, mEmail, SCOPE).execute();
                user = new GetUsernameTask(LoginActivity.this, mEmail, SCOPE);
                user.execute();
            } else {
                Toast.makeText(this, "not online", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void pickUserAccount() {
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }

    String mEmail; // Received from newChooseAccountIntent(); passed to getToken()

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == RESULT_OK) {
                mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                // With the account name acquired, go get the auth token
                getUsername();
            } else if (resultCode == RESULT_CANCELED) {
                // The account picker dialog closed without selecting an account.
                // Notify users that they must pick an account to proceed.
                Toast.makeText(this, "Please pick an account", Toast.LENGTH_SHORT).show();
            }
        }
        // Later, more code will go here to handle the result from some exceptions...
    }

    static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;

    /**
     * This method is a hook for background threads and async tasks that need to
     * provide the user a response UI when an exception occurs.
     */
    public void handleException(final Exception e) {
        // Because this call comes from the AsyncTask, we must ensure that the following
        // code instead executes on the UI thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (e instanceof GooglePlayServicesAvailabilityException) {
                    // The Google Play services APK is old, disabled, or not present.
                    // Show a dialog created by Google Play services that allows
                    // the user to update the APK
                    int statusCode = ((GooglePlayServicesAvailabilityException)e).getConnectionStatusCode();
                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, LoginActivity.this, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                    dialog.show();
                } else if (e instanceof UserRecoverableAuthException) {
                    // Unable to authenticate, such as when the user has not yet granted
                    // the app access to the account, but the user can fix this.
                    // Forward the user to an activity in Google Play services.
                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
                    startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
                }
            }
        });
    }


    boolean isDeviceOnline() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        boolean isWifiConn = networkInfo.isConnected();
        networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isMobileConn = networkInfo.isConnected();
        return isMobileConn || isWifiConn;
    }
}





//public class LoginActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
//
//    /**
//     * The {@link android.support.v4.view.PagerAdapter} that will provide
//     * fragments for each of the sections. We use a
//     * {@link FragmentPagerAdapter} derivative, which will keep every
//     * loaded fragment in memory. If this becomes too memory intensive, it
//     * may be best to switch to a
//     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
//     */
//    PlusActivity.SectionsPagerAdapter mSectionsPagerAdapter;
//
//    /* Request code used to invoke sign in user interactions. */
//    private static final int RC_SIGN_IN = 0;
//    /* Client used to interact with Google APIs. */
//    private GoogleApiClient mGoogleApiClient;
//    /* A flag indicating that a PendingIntent is in progress and prevents
//     * us from starting further intents.
//     */
//    private boolean mIntentInProgress;
//    /* Track whether the sign-in button has been clicked so that we know to resolve
//     * all issues preventing sign-in without waiting.
//     */
//    private boolean mSignInClicked;
//    /* Store the connection result from onConnectionFailed callbacks so that we can
//     * resolve them when the user clicks sign-in.
//     */
//    private ConnectionResult mConnectionResult;
//    private SignInButton sign_in_button;
//    /**
//     * Keeps track if user is currently signed in
//     */
//    private String loggedIn = "false";
//
////    private final String SERVER_CLIENT_ID = "493596572952-nbe6uvb6k89oi62r0l82vb0gcfhn9829.apps.googleusercontent.com";
//    private final String SERVER_CLIENT_ID = "493596572952-32n73ep7k3cmin32uv89kr0pcn55nsb4.apps.googleusercontent.com";
//    private final String SERVICE_CLIENT_ID = "493596572952-samut4amsfj17l8lf6t5si398tv4tm67.apps.googleusercontent.com";
//    //Google+ Scopes
//    private final String SCOPE1 = "https://www.googleapis.com/auth/plus.me"; //Grants the app permission to use the special value me to represent the authenticated user. Does not apply to apps that use domain-wide delegation of authority.
//    private final String SCOPE2 = "https://www.googleapis.com/auth/plus.profiles.read"; //Required - Grants the app permission to read the user's public profile data as well as profile data that the authorized user is granted access to view.
//    private final String SCOPE3 = "https://www.googleapis.com/auth/plus.circles.read"; //Required - Grants the app permission to read the names of the user's circles, and the people and pages that are members of each circle.
//    private final String SCOPE4 = "https://www.googleapis.com/auth/userinfo.profile";
//    private final String REDIRECT_URI = "urn:ietf:wg:oauth:2.0:oob";
//    public String accessToken = null;
//    private String accountName = null;
//    private String about = null;
//
//    private static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 1001;
//    private static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
//    private static final String SCOPE;
//    private static String[] scopes = new String[]{
//            "https://www.googleapis.com/auth/plus.me",
//            "https://www.googleapis.com/auth/plus.circles.read",
//            "https://www.googleapis.com/auth/plus.profiles.read",
//    };
//
//    static {
//        SCOPE = "oauth2: " + TextUtils.join(" ", scopes);
//    }
//
//    private GetUsernameTask user;
//
////    static final int AUTH_CODE_REQUEST_CODE = 1;
//    /**
//     * Intent to go fom login to google plus activity, and sign out back to login activity
//     */
////    private Intent activity;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.login_main);
//
//        Intent activity = getIntent();
//        String loggedIn = activity.getStringExtra("loggedIn");
//
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(Plus.API)
//                .addScope(Plus.SCOPE_PLUS_LOGIN)
//                .build();
//
//        sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);
//        sign_in_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!mGoogleApiClient.isConnecting()) {
//                    mSignInClicked = true;
//                    resolveSignInError();
//                }
//            }
//        });
//    }
//
//    protected void onStart() {
//        super.onStart();
//        mGoogleApiClient.connect();
//    }
//
//    protected void onStop() {
//        super.onStop();
//        // Prior to disconnecting, run clearDefaultAccount().
//        if (mGoogleApiClient.isConnected()) {
//            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
//            mGoogleApiClient.disconnect();
//        }
//    }
//
//    /* A helper method to resolve the current ConnectionResult error. */
//    private void resolveSignInError() {
//        if (mConnectionResult.hasResolution()) {
//            try {
//                mIntentInProgress = true;
//                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
//                        RC_SIGN_IN, null, 0, 0, 0);
//            } catch (IntentSender.SendIntentException e) {
//                // The intent was canceled before it was sent.  Return to the default
//                // state and attempt to connect to get an updated ConnectionResult.
//                mIntentInProgress = false;
//                mGoogleApiClient.connect();
//            }
//        }
//    }
//
//    public void onConnectionFailed(ConnectionResult result) {
////        if (!result.hasResolution()) {
////            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
////                    0).show();
////            return;
////        }
//
//        if (!mIntentInProgress) {
//            // Store the ConnectionResult so that we can use it later when the user clicks
//            // 'sign-in'.
//            mConnectionResult = result;
//
//            if (mSignInClicked) {
//                // The user has already clicked 'sign-in' so we attempt to resolve all
//                // errors until the user is signed in, or they cancel.
//                resolveSignInError();
//            }
//        }
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//        mGoogleApiClient.connect();
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
//            // Receiving a result from the AccountPicker
//            if (resultCode == RESULT_OK) {
//                accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
//                // With the account name acquired, go get the auth token
//                user = new GetUsernameTask(LoginActivity.this, accountName, SCOPE);
//                user.execute();
//            } else if (resultCode == RESULT_CANCELED) {
//                // The account picker dialog closed without selecting an account.
//                // Notify users that they must pick an account to proceed.
//                Toast.makeText(this, "Pick an Account", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        if (requestCode == RC_SIGN_IN) {
//            if (resultCode != RESULT_OK) {
//                mSignInClicked = false;
//            }
//
//            mIntentInProgress = false;
//
//            if (!mGoogleApiClient.isConnecting()) {
//                mGoogleApiClient.connect();
//            }
//        }
//    }
//
//    public void onConnected(Bundle connectionHint) {
//        if(loggedIn.equals("true")){
//            signOut();
//        }
//
//        mIntentInProgress = true;
////        task.execute();
//        accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
//        user = new GetUsernameTask(LoginActivity.this, accountName, SCOPE);
//        user.execute();
//        accessToken = user.accessToken;
//        Intent activity = new Intent(getApplicationContext(), PlusActivity.class);
//        activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.putExtra("accessToken", accessToken);
//        startActivity(activity);
//    }
//
//
//    public void signOut() {
//        // Prior to disconnecting, run clearDefaultAccount().
//        if (mGoogleApiClient.isConnected()) {
//            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
//            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
//            mGoogleApiClient.disconnect();
//            mGoogleApiClient.connect();
//        }
//    }
//
//        public void handleException(final Exception e) {
//        // Because this call comes from the AsyncTask, we must ensure that the following
//        // code instead executes on the UI thread.
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (e instanceof GooglePlayServicesAvailabilityException) {
//                    // The Google Play services APK is old, disabled, or not present.
//                    // Show a dialog created by Google Play services that allows
//                    // the user to update the APK
//                    int statusCode = ((GooglePlayServicesAvailabilityException)e).getConnectionStatusCode();
//                    Dialog dialog = GooglePlayServicesUtil.getErrorDialog(statusCode, LoginActivity.this, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
//                    dialog.show();
//                } else if (e instanceof UserRecoverableAuthException) {
//                    // Unable to authenticate, such as when the user has not yet granted
//                    // the app access to the account, but the user can fix this.
//                    // Forward the user to an activity in Google Play services.
//                    Intent intent = ((UserRecoverableAuthException)e).getIntent();
//                    startActivityForResult(intent, REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
//                }
//            }
//        });
//    }


//
//    AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
//        @Override
//        protected String doInBackground(Void... params) {
////        obtain access token
//            accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
////            String scopes = "oauth2:server:client_id:" + SERVER_CLIENT_ID + ":api_scope:" + SCOPE1 + " " + SCOPE2 + " " + SCOPE3;
////            String scopes = "oauth2:server:client_id:" + SERVER_CLIENT_ID + ":" + SCOPE1 + " " + SCOPE2 + " " + SCOPE3;
////            String scopes = "oauth2:server:client_id:AIzaSyA0-gcjGOCvtR3bR2XmhuPqOG4A5QS9TUo:"+ SCOPE1 + " " + SCOPE2 + " " + SCOPE3;
////            String scopes = "oauth2:" + Scopes.PLUS_LOGIN + " " + Scopes.PLUS_ME + " " + Scopes.PROFILE ;
//            String scopes = "oauth2:" + SCOPE1 + " " + SCOPE2 + " " + SCOPE3 + " " + SCOPE4;
//
//            try {
//                accessToken = GoogleAuthUtil.getToken(
//                        getApplicationContext(),                                              // Context context
//                        accountName,  // String accountName
//                        scopes                                            // String scope
////                        appActivities                                      // Bundle bundle
//                );
//
//                if (accessToken == null){
//                    Log.w("NO_ACCESS_TOKEN", "null access token: " + accessToken);
//                }
//
//                GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
//                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), credential).setApplicationName("GooglePlusMini").build();
//
//                Person mePerson = plusDomains.people().get("me").execute();
//                about = mePerson.getAboutMe();
////
////                Intent activity = new Intent(getApplicationContext(), PlusActivity.class);
////                activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
////                activity.putExtra("accessToken", accessToken);
////                activity.putExtra("accountName", accountName);
////                activity.putExtra("about", about);
////                startActivity(activity);
//
//            } catch (IOException transientEx) {
//                // network or server error, the call is expected to succeed if you try again later.
//                // Don't attempt to call again immediately - the request is likely to
//                // fail, you'll hit quotas or back-off.
////                return;
//                Log.e("IOException", "Unrecoverable I/O exception: " + transientEx.getMessage(), transientEx);
//            } catch (UserRecoverableAuthException e) {
//                // Requesting an authorization code will always throw
//                // UserRecoverableAuthException on the first call to GoogleAuthUtil.getToken
//                // because the user must consent to offline access to their data.  After
//                // consent is granted control is returned to your activity in onActivityResult
//                // and the second call to GoogleAuthUtil.getToken will succeed.
//                Log.w("UserRecoverableAuthException","Error retrieving the token: " + e.getMessage());
//                startActivityForResult(e.getIntent(), RESULT_OK);
//                e.printStackTrace();
////                return;
//            } catch (GoogleAuthException authEx) {
//                // Failure. The call is not expected to ever succeed so it should not be
//                // retried.
//                Log.e("GoogleAuthException", "Unrecoverable authentication exception: " + authEx.getMessage(), authEx);
//                authEx.getStackTrace();
////                return;
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//            return accessToken;
//        }
//    };
//}