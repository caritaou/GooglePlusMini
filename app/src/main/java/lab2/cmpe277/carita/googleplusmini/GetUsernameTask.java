package lab2.cmpe277.carita.googleplusmini;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

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
    private static String displayName;
    private static String occupation;
    private static String organizations;
    private static String aboutMe;
    private static String image_url;

    //MyCircles
    String[] circle_list;                       //the list of circles the user has
    static String[][] circle_children;          //the list of names in each circle
    static Person[][] circle_children_people;   //the list of friends (Person) in each circle
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

    /**
     * Sets an in progress dialog while user sign's in to prevent an unpopulated profile
     */
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

                displayName = me.getDisplayName();
                aboutMe = me.getAboutMe();
                image_url = me.getImage().getUrl();
                occupation = me.getOccupation();

                organizations = "";
                if(me.getOrganizations() != null) {
                    List<Person.Organizations> tmp = me.getOrganizations();
                    for (Person.Organizations o: tmp){
                        organizations = organizations + " " + o.getName() + ",";
                    }
                    organizations = organizations.substring(0, organizations.length()-1); //remove the last comma
                }

                listCircles = plusDomains.circles().list("me");
                if(listCircles != null) {
                    circleFeed = listCircles.execute();
                    circles = circleFeed.getItems();
                    circle_list = new String[circles.size()];
                    circle_children = new String[circles.size()][];
                    circle_children_people = new Person[circles.size()][];

                    while (circles != null) {
                        int i = 0;
                        for (Circle circle : circles) {
                            String name = circle.getDisplayName();
                            circle_list[i] = name;

                            String id = circle.getId();
                            listPeople = plusDomains.people().listByCircle(id);
                            PeopleFeed peopleFeed = listPeople.execute();

                            if(peopleFeed.getItems() != null && peopleFeed.getItems().size() > 0 ) {
                                circle_children[i] = new String[peopleFeed.getItems().size()];
                                circle_children_people[i] = new Person[peopleFeed.getItems().size()];
                                int j = 0;
                                for(Person person : peopleFeed.getItems()) {
                                    System.out.println("\t" + person.getDisplayName());
                                    circle_children[i][j] = person.getDisplayName();
                                    circle_children_people[i][j] = person;
                                    j++;
                                }
                            }
                            else {
                                circle_children[i] = new String[0];
                                circle_children_people[i] = new Person[0];
                            }
                            i++;
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

        //pass the user's information to intent
        activity.putExtra("displayName", displayName);
        activity.putExtra("organizations", organizations);
        activity.putExtra("aboutMe", aboutMe);
        activity.putExtra("image_url", image_url);
        activity.putExtra("occupation", occupation);

        //Pass the list of circles to intent
        if(circle_list.length > 0) {
            activity.putExtra("circle_list", circle_list);
        }
        context.startActivity(activity);
    }

    public static String[][] getArray() {
        return circle_children;
    }

    public static Person[][] getCircle_children_people(){
        return circle_children_people;
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