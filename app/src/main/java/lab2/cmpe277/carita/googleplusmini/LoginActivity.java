package lab2.cmpe277.carita.googleplusmini;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import com.google.android.gms.plus.PlusClient;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.view.View;

import java.io.IOException;


public class LoginActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    PlusActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    /* Request code used to invoke sign in user interactions. */
    private static final int RC_SIGN_IN = 0;
    /* Client used to interact with Google APIs. */
    private GoogleApiClient mGoogleApiClient;
    /* A flag indicating that a PendingIntent is in progress and prevents
     * us from starting further intents.
     */
    private boolean mIntentInProgress;
    /* Track whether the sign-in button has been clicked so that we know to resolve
     * all issues preventing sign-in without waiting.
     */
    private boolean mSignInClicked;
    /* Store the connection result from onConnectionFailed callbacks so that we can
     * resolve them when the user clicks sign-in.
     */
    private ConnectionResult mConnectionResult;
    private SignInButton sign_in_button;
    /**
     * Keeps track if user is currently signed in
     */
    private String loggedIn = "false";

    private final String SERVER_CLIENT_ID = "493596572952-nbe6uvb6k89oi62r0l82vb0gcfhn9829.apps.googleusercontent.com";
    //Google+ Scopes
    private final String SCOPE1 = "https://www.googleapis.com/auth/plus.me"; //Grants the app permission to use the special value me to represent the authenticated user. Does not apply to apps that use domain-wide delegation of authority.
    private final String SCOPE2 = "https://www.googleapis.com/auth/plus.profiles.read"; //Required - Grants the app permission to read the user's public profile data as well as profile data that the authorized user is granted access to view.
    private final String SCOPE3 = "https://www.googleapis.com/auth/plus.circles.read"; //Required - Grants the app permission to read the names of the user's circles, and the people and pages that are members of each circle.
    public String accessToken = null;
//    private String accountName;

//    static final int AUTH_CODE_REQUEST_CODE = 1;
    /**
     * Intent to go fom login to google plus activity, and sign out back to login activity
     */
//    private Intent activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);

        Intent activity = getIntent();
        String loggedIn = activity.getStringExtra("loggedIn");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        sign_in_button = (SignInButton) findViewById(R.id.sign_in_button);
        sign_in_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mGoogleApiClient.isConnecting()) {
                    mSignInClicked = true;
                    resolveSignInError();
                }
            }
        });
    }

    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        // Prior to disconnecting, run clearDefaultAccount().
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
            mGoogleApiClient.disconnect();
        }
    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnectionFailed(ConnectionResult result) {
//        if (!result.hasResolution()) {
//            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this,
//                    0).show();
//            return;
//        }

        if (!mIntentInProgress) {
            // Store the ConnectionResult so that we can use it later when the user clicks
            // 'sign-in'.
            mConnectionResult = result;

            if (mSignInClicked) {
                // The user has already clicked 'sign-in' so we attempt to resolve all
                // errors until the user is signed in, or they cancel.
                resolveSignInError();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
//        if (requestCode == RESULT_OK && responseCode == RESULT_OK) {
//            Bundle extra = intent.getExtras();
//            accessToken = extra.getString("authtoken");
//        }

        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting()) {
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnected(Bundle connectionHint) {
        if(loggedIn.equals("true")){
            signOut();
        }

        task.execute();
//        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
//        PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), credential).build();

        Intent activity = new Intent(getApplicationContext(), PlusActivity.class);
        activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.putExtra("accessToken", accessToken);
//        activity.putExtra("accountName", accountName);
//        activity.putExtra("scopes", scopes);
        startActivity(activity);
    }


    public void signOut() {
        // Prior to disconnecting, run clearDefaultAccount().
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();
        }
    }

    AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
        @Override
        protected String doInBackground(Void... params) {
//            Bundle appActivities = new Bundle();
//            appActivities.putString(GoogleAuthUtil.KEY_REQUEST_VISIBLE_ACTIVITIES,
//                    "http://schemas.google.com/AddActivity");

//        obtain access token
            String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
            String scopes = "oauth2:server:client_id:" + SERVER_CLIENT_ID + ":api_scope:" + SCOPE1 + " " + SCOPE2 + " " + SCOPE3;

            try {
                accessToken = GoogleAuthUtil.getToken(
                        getApplicationContext(),                                              // Context context
                        accountName,  // String accountName
                        scopes                                            // String scope
//                    appActivities                                      // Bundle bundle
                );
            } catch (IOException transientEx) {
                // network or server error, the call is expected to succeed if you try again later.
                // Don't attempt to call again immediately - the request is likely to
                // fail, you'll hit quotas or back-off.
//                return;
            } catch (UserRecoverableAuthException e) {
                // Requesting an authorization code will always throw
                // UserRecoverableAuthException on the first call to GoogleAuthUtil.getToken
                // because the user must consent to offline access to their data.  After
                // consent is granted control is returned to your activity in onActivityResult
                // and the second call to GoogleAuthUtil.getToken will succeed.
                startActivityForResult(e.getIntent(), RESULT_OK);
                e.printStackTrace();
//                return;
            } catch (GoogleAuthException authEx) {
                // Failure. The call is not expected to ever succeed so it should not be
                // retried.
                signOut();
                authEx.getStackTrace();
//                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return accessToken;
        }
    };
}