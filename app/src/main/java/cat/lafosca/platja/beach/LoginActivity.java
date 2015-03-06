package cat.lafosca.platja.beach;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


// A login screen that offers login and register via email/password.
public class LoginActivity extends Activity {//implements LoaderCallbacks<Cursor> {

    private static final String LOGTAG = "LogsLoginActivity";
    private static final String urlRegister="/users";//URL API Register
    private static final String urlLogin="/user";//URL API Login

    //Asinc Tasks
    private UserLoginTask vLoginTask = null;
    private UserRegisterTask vResgisterTask = null;
    private HttpMethod connection;

    // UI references.
    private AutoCompleteTextView mUserView;
    private EditText mPasswordView;
    private View mLoginFormView;
    private Button b_sign_in,b_register;

    private String authenticationToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Prepare conection
        connection=new HttpMethod();

        // Link layout elements
        mUserView = (AutoCompleteTextView) findViewById(R.id.user);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        b_sign_in = (Button)findViewById(R.id.sign_in_button);
        b_sign_in.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        b_register = (Button)findViewById(R.id.register_button);
        b_register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
        mLoginFormView = findViewById(R.id.login_form);
    }

    // Attempts to sign in the account specified by the login form.
    private void attemptLogin() {
        if (vLoginTask != null)
            return;

        // Reset errors in editText
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user login attempt.
            vLoginTask = new UserLoginTask(user, password);
            vLoginTask.execute((Void) null);
        }
    }


    // Attempts to or register the account specified by the login form.
    private void attemptRegister() {
        if (vResgisterTask != null)
            return;

        // Reset errors in editText
        mUserView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String user = mUserView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid user address.
        if (TextUtils.isEmpty(user)) {
            mUserView.setError(getString(R.string.error_field_required));
            focusView = mUserView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first form field with an error.
            focusView.requestFocus();
        } else {
            // perform the user register attempt.
            vResgisterTask = new UserRegisterTask(user, password);
            vResgisterTask.execute((Void) null);
        }
    }

	//Check if Pass is OK
    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    //Validate Log In
    private boolean loginUser(String pUser ,String pPass) {
        Log.e(LOGTAG, "Mensaje de loginUser: START");
        boolean logstatus = false;

        //Create ArrayList<NameValuePair> Object to send
        ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();

        //Set ArrayList<NameValuePair> Object data
        postparameters2send.add(new BasicNameValuePair("username",pUser));
        postparameters2send.add(new BasicNameValuePair("password",pPass));

        BasicHttpParams params=new BasicHttpParams();
        params.setParameter("username", pUser);
        params.setParameter("password", pPass);

        //Make the request
        JSONObject resultObject = connection.getLoginData(params, urlLogin);

        if (resultObject != null && resultObject.length() > 0) {
            try {
                //Save authenticationToken
                authenticationToken = resultObject.getString("authentication_token");
                //logstatus OK
                logstatus = true;
                Log.e(LOGTAG, "Mensaje de loginUser: authenticationToken: " + authenticationToken);
                Log.e(LOGTAG, "Mensaje de loginUser: logstatus: " + logstatus);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Log.e(LOGTAG, "Mensaje de loginUser: END");
        return logstatus;
    }

    /*Validate Register*/
    private boolean RegisterUser(String pUser ,String pPass) {
        Log.e(LOGTAG, "Mensaje de RegisterUser: START");
        boolean logstatus = false;

        //Create JSON Object to send
        JSONObject user = new JSONObject();
        JSONObject userData = new JSONObject();

        //Set JSON Object data
        try {
            userData.put("username", pUser);
            userData.put("password", pPass);
            user.put("user", userData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Make the request
        JSONObject resultObject = connection.getRegisterData(user, urlRegister);

        if (resultObject != null && resultObject.length() > 0) {
            try {
                //Save authenticationToken
                authenticationToken = resultObject.getString("authentication_token");
                //logstatus OK
                logstatus = true;
                Log.e(LOGTAG, "Mensaje de RegisterUser: authenticationToken: " + authenticationToken);
                Log.e(LOGTAG, "Mensaje de RegisterUser: logstatus: " + logstatus);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(LOGTAG, "Mensaje de RegisterUser: END");
        }
        return logstatus;
    }

    //Represents an asynchronous login task used to authenticate the user.
    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private String mUser;
        private String mPass;

        UserLoginTask(String email, String password) {
            mUser = email;
            mPass = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //obtnemos usr y pass
            Boolean resultat=loginUser(mUser, mPass);
            return resultat;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            vLoginTask = null;

            if (success) {
                Toast.makeText(getApplicationContext(), "Login OK", Toast.LENGTH_SHORT)
                        .show();
                Intent launchactivity= new Intent(getApplicationContext(),SettingsActivity.class);
                launchactivity.putExtra("authenticationToken", authenticationToken);
                launchactivity.putExtra("username", mUser);
                launchactivity.putExtra("password", mPass);
                startActivity(launchactivity);
                Log.d(LOGTAG, "Mensaje de onPostExecute: END OK LOGIN");
            } else {
                mPasswordView.setError(getString(R.string.error_login));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            vLoginTask = null;
        }
    }

    //Represents an asynchronous registration task used to authenticate the user.
    private class UserRegisterTask extends AsyncTask<Void, Void, Boolean> {

        private String mUser;
        private String mPass;

        UserRegisterTask(String email, String password) {
            mUser = email;
            mPass = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            //obtenemos user y pass
            Boolean resultat=RegisterUser(mUser, mPass);
            return resultat;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            vResgisterTask = null;

            if (success) {
                Toast.makeText(getApplicationContext(), "User Created", Toast.LENGTH_SHORT)
                        .show();
                Log.d(LOGTAG, "Mensaje de onPostExecute: END OK REGISTER");
            } else {
                mPasswordView.setError(getString(R.string.error_register));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            vResgisterTask = null;
        }
    }
}