package lab2.cmpe277.carita.googleplusmini;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Person;

import java.io.IOException;

public class GetUsernameTask extends AsyncTask<String, Void, Boolean> {
    /** progress dialog to show user that the backup is processing. */
    private ProgressDialog dialog;

    LoginActivity mActivity;
    private Context context;
    String mScope;
    String mEmail;

    GetUsernameTask(LoginActivity activity, String name, String scope) {
        this.mActivity = activity;
        context = activity;
        this.mScope = scope;
        this.mEmail = name;
    }

    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setMessage("Logging In...");
        dialog.show();
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        Intent activity = new Intent(context, PlusActivity.class);
        activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        activity.putExtra("accessToken", accessToken);
        context.startActivity(activity);
    }
    /**
     * Executes the asynchronous job. This runs when you call execute()
     * on the AsyncTask instance.
     */
    @Override
    protected Boolean doInBackground(String... params) {
        try {
            String token = fetchToken();
            if (token != null) {
                System.out.println(token);

                GoogleCredential cred = new GoogleCredential().setAccessToken(token);
                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), cred).build();
                Person mePerson = plusDomains.people().get("me").execute();

                if (mePerson != null) {
                    System.out.println(mePerson);
                }
//                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport, new JacksonFactory, credential​).build();
//                //Example of retrieving profile
//                Person mePerson = plusDomains.people().get("me").execute();
                //Retrieve circles, people


            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TIP: Check for network connectivity before starting the AsyncTask.
            Log.d("", "exception", e);

        }
        return true;
    }

    /**
     * Gets an authentication token from Google and handles any
     * GoogleAuthException that may occur.
     */
    protected String fetchToken() throws IOException {
        try {
            return GoogleAuthUtil.getToken(mActivity, mEmail, mScope);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
            mActivity.handleException(userRecoverableException);
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            fatalException.printStackTrace();
        }
        return null;
    }

}