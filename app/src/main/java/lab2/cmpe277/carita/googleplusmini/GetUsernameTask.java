package lab2.cmpe277.carita.googleplusmini;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plusDomains.PlusDomains;
import com.google.api.services.plusDomains.model.Circle;
import com.google.api.services.plusDomains.model.CircleFeed;
import com.google.api.services.plusDomains.model.PeopleFeed;
import com.google.api.services.plusDomains.model.Person;

import java.io.IOException;
import java.util.List;

public class GetUsernameTask extends AsyncTask<String, Void, Boolean> {
    /** progress dialog to show user that the backup is processing. */
    private ProgressDialog dialog;

    LoginActivity mActivity;
    private Context context;
    String mScope;
    String mEmail;
    String token;

    //Person
    Person me;
    String displayName;
    String occupation;
    String aboutMe;

    //MyCircles
    String[] circle_list;
    String[][] circle_children;
    PlusDomains.Circles.List listCircles;
    CircleFeed circleFeed;
    List<Circle> circles;
    PlusDomains.People.ListByCircle listPeople;

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

    /**
     * Executes the asynchronous job. This runs when you call execute()
     * on the AsyncTask instance.
     */
    @Override
    protected Boolean doInBackground(String... params) {
        try {
            token = fetchToken();
            if (token != null) {
                System.out.println(token);

                GoogleCredential cred = new GoogleCredential().setAccessToken(token);
                PlusDomains plusDomains = new PlusDomains.Builder(new NetHttpTransport(), new JacksonFactory(), cred).build();
                //Retrieve circles, people
                me = plusDomains.people().get("me").execute();
                if (me != null) {
                    System.out.println(me);
                }
                if(me.getDisplayName() != null) {
                    System.out.println("display name: " + me.getDisplayName());
                }
                if(me.getOccupation() != null) {
                    System.out.println("occupation: " + me.getOccupation());
                }
                if(me.getAboutMe() != null) {
                    System.out.println("about me: " + me.getAboutMe());
                }
                if(me.getImage() != null){
                    System.out.println("image url: " + me.getImage());
                }

                listCircles = plusDomains.circles().list("me");
                if(listCircles != null) {
                    circleFeed = listCircles.execute();
                    circles = circleFeed.getItems();
                    circle_list = new String[circles.size()];

                    while (circles != null) {
                        int i = 0;
                        for (Circle circle : circles) {
                            String name = circle.getDisplayName();
                            circle_list[i] = name;

//                            listPeople = plusDomains.people().listByCircle(name);
//                            PeopleFeed peopleFeed = listPeople.execute();
//                            circle_children = new String[circles.size()][peopleFeed.size()];
//
//                            if(peopleFeed.getItems() != null && peopleFeed.getItems().size() > 0 ) {
//                                int j = 0;
//                                for(Person person : peopleFeed.getItems()) {
//                                    System.out.println("\t" + person.getDisplayName());
//                                    circle_children[i][j] = person.getDisplayName();
//                                    j++;
//                                }
//                            }
////                            System.out.println(circle.getDisplayName());
////                            System.out.println(circle_list[i]);
//                            i++;
                        }

                        // When the next page token is null, there are no additional pages of
                        // results. If this is the case, break.
                        if (circleFeed.getNextPageToken() != null) {
                            // Prepare the next page of results
                            listCircles.setPageToken(circleFeed.getNextPageToken());

                            // Execute and process the next page request
                            circleFeed = listCircles.execute();
                            circles = circleFeed.getItems();
                        } else {
                            circles = null;
                        }
                    }
                }

            }
        } catch (IOException e) {
            // The fetchToken() method handles Google-specific exceptions,
            // so this indicates something went wrong at a higher level.
            // TIP: Check for network connectivity before starting the AsyncTask.
            Log.d("", "exception", e);

        }
        return true;
    }

    @Override
    protected void onPostExecute(final Boolean success) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }

        Intent activity = new Intent(context, PlusActivity.class);
        activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.putExtra("accessToken", token);
        if(circle_list.length > 0) {
            activity.putExtra("circle_list", circle_list);
        }
//        if (circle_list.length > 0) {
//            Bundle b = new Bundle();
//            b.putSerializable("circle_children", circle_children);
//            activity.putExtras(b);
//        }
//        activity.putExtra("displayName", me.getDisplayName());
        context.startActivity(activity);
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