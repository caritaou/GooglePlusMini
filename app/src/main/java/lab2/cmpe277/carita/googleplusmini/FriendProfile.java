package lab2.cmpe277.carita.googleplusmini;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.services.plusDomains.model.Person;


public class FriendProfile extends ActionBarActivity {
    String friendDisplayName;
    String friendOrganizations = "";
    String friendAboutMe;
    String friendImage_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_fragment);

        Intent activity = getIntent();
        friendAboutMe = activity.getExtras().getString("friendAboutMe");
        friendOrganizations = activity.getExtras().getString("friendOrganizations");
        friendDisplayName = activity.getExtras().getString("friendDisplayName");
        friendImage_url = activity.getExtras().getString("friendImage_url");

        TextView profile_name = (TextView) findViewById(R.id.profile_name);
        if (friendDisplayName != null) {
            profile_name.setText(friendDisplayName);
        }
        else{
            friendDisplayName = "User";
            profile_name.setText("User does not have a displayName");
        }

        TextView profile_info = (TextView) findViewById(R.id.profile_info);
        ImageView iv = (ImageView) findViewById(R.id.icon);
        new LoadImage(friendImage_url, iv);

        if (friendOrganizations != null) {
            profile_info.setText("Organizations: " + friendOrganizations + "\n");
        }
        else{
            profile_info.setText(friendDisplayName + " is not part of an organizations" + "\n");
        }

        if (friendAboutMe != null) {
            profile_info.append("About " + friendDisplayName + ": " + friendAboutMe + "\n");
        }
        else{
            profile_info.append(friendDisplayName + " does not have an About Me description" + "\n");
        }

        Button email = (Button) findViewById(R.id.button_email);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                startActivity(Intent.createChooser(emailIntent, "Choose an Email Client:"));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_sign_out) {
            Intent activity = new Intent(getApplicationContext(), LoginActivity.class);
            activity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(activity);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
