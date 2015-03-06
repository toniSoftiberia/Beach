package cat.lafosca.platja.beach;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

// A screen that offers to user the beach options
public class SettingsActivity extends Activity {

    //Connection
    private HttpMethod connection;
    private static final String LOGTAG = "SettingsActivity";
    private static final String urlClose="/close";//URL API Flag
    private static final String urlOpen="/open";//URL API Open
    private static final String urlBalls="/nivea-rain";//URL API Balls
    private static final String urlClean="/clean";//URL API Clean
    private static final String urlFlag="/flag";//URL API Flag
    private static final String urlStatus="/state";//URL API State

    //Async Tasks
    private BeachStatusTask vStatusTask = null;
    private BeachFlagTask vFlagTask = null;
    private BeachOpenCloseTask vOpenTask = null, vCloseTask = null;
    private BeachJobsTask vBallsTask = null, vCleanTask = null;

    // UI references.
    private Button bRefresh, bSearch, bBalls, bClean, bBack;
    private Switch sSwitch;
    private LinearLayout openedLayout, closedLayout;
    private TextView lFlagRes, lHappyRes, lDirtRes;
    private RadioButton rGreen, rYellow, rRed;
    private RadioGroup rGroup;
    private ListView list;
    private EditText editKid;

    //user info
    private String authenticationToken = "";
    private String mUser = "";

    //beach info
    private String state;
    private int flag= -1;
    private int happiness= -1;
    private int dirtiness= -1;
    private JSONArray kids;
    private ArrayList<LostKid> sortedKids, sortedFiltredKids;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        //Prepare conection
        connection=new HttpMethod();
        sortedKids = new ArrayList();
        sortedFiltredKids = new ArrayList();

        //Get Extra Data
        Intent intent = getIntent();
        authenticationToken = intent.getStringExtra("authenticationToken");
        mUser = intent.getStringExtra("username");

        // UI references.
		//OPEN/CLOSE Beach
        sSwitch = (Switch)findViewById(R.id.switch_button);
        sSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                sSwitch.setEnabled(false);
                if (isChecked) {
                    attemptOpen();
                } else {
                    attemptClose();
                }
            }
        });
		
		//Refres Status
        bRefresh = (Button)findViewById(R.id.refresh);
        bRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptStatus();
            }
        });

		//OPEN screen
        openedLayout = (LinearLayout)findViewById(R.id.status_beach_open);
		
		//OPEN Operations		
		//search lost kids
        bSearch = (Button)findViewById(R.id.search);
        bSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
				//Set up lost kids list layout
                setContentView(R.layout.list);
                filterKids();
                LlistatAdaptador la = new LlistatAdaptador(getApplicationContext(),
                        R.layout.list_entry,
                        sortedFiltredKids) {
                    @Override
                    public void onEntrada(Object entrada, View view) {
                        if (entrada != null) {
                            TextView dataText = (TextView) view.findViewById(R.id.tv_data);
                            if (dataText != null){
                                String name = ((LostKid)entrada).getName();
                                int age = ((LostKid)entrada).getAge();
                                dataText.setText("Name: " + name + " Age: " + age);
                            }
                        }
                    }
                };

                list = (ListView)findViewById(R.id.List);
                list.setAdapter(la);

				//Set up Back Button
                bBack = (Button)findViewById(R.id.back);
                bBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setContentView(R.layout.settings);
                        Intent launchactivity= new Intent(getApplicationContext(),SettingsActivity.class);
                        launchactivity.putExtra("authenticationToken", authenticationToken);
                        launchactivity.putExtra("username", mUser);
                        startActivity(launchactivity);
                    }
                });
            }
        });
        list = (ListView)findViewById(R.id.List);
		
		//Filter fields
        editKid = (EditText)findViewById(R.id.edit_kid);

		//throw balls
        bBalls = (Button)findViewById(R.id.balls);
        bBalls.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptBalls();
            }
        });		

		//flag conf
        lFlagRes = (TextView)findViewById(R.id.lFlagRes);
        lHappyRes = (TextView)findViewById(R.id.lHappyRes);
        lDirtRes = (TextView)findViewById(R.id.lDirtRes);

        rGreen = (RadioButton)findViewById(R.id.radio_green);
        rYellow = (RadioButton)findViewById(R.id.radio_yellow);
        rRed = (RadioButton)findViewById(R.id.radio_red);
        rGroup=(RadioGroup) findViewById(R.id.radio_group);
        rGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId)
            {
                if(rGreen.isChecked()){
                    attemptFlag(0);
                }
                else if(rYellow.isChecked()){
                    attemptFlag(1);
                }
                else if(rRed.isChecked()){
                    attemptFlag(2);
                }
            }
        });
		
		//CLOSE screen
        closedLayout = (LinearLayout)findViewById(R.id.status_beach_close);
		
		//CLOSE Operations		
		//clean the beach
        bClean = (Button)findViewById(R.id.clean);
        bClean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptClean();
            }
        });
		
		//get initial STATUS
        attemptStatus();
    }

    //Get Status of The beach attempt
    private void attemptStatus() {
        if (vStatusTask != null)
            return;

        // perform the status attempt.
        vStatusTask = new BeachStatusTask();
        vStatusTask.execute((Void) null);
    }

    //Change Flag of The beach attempt
    private void attemptFlag(int flag) {
        if (vFlagTask != null)
            vFlagTask = null;
        // perform the change flag attempt.
        vFlagTask = new BeachFlagTask();
        vFlagTask.execute(flag);
    }

    //Throw Balls in The beach attempt
    private void attemptBalls() {
        if (vBallsTask != null)
            return;

        // perform the balls thrown attempt.
        vBallsTask = new BeachJobsTask();
        vBallsTask.execute("balls");
    }

    //Clean The beach attempt
    private void attemptClean() {
        if (vCleanTask != null)
            return;

        // perform the beach clean attempt.
        vCleanTask = new BeachJobsTask();
        vCleanTask.execute("clean");
    }

    //Throw Open beach attempt
    private void attemptOpen() {
        if (vOpenTask != null)
            return;

        // perform the open attempt.
        vOpenTask = new BeachOpenCloseTask();
        vOpenTask.execute("open");
    }

    //Throw Close beach attempt
    private void attemptClose() {
        if (vCloseTask != null)
            return;

        // perform the close attempt.
        vCloseTask = new BeachOpenCloseTask();
        vCloseTask.execute("closed");
    }

    //Filter the kids
    private void filterKids() {
        sortedFiltredKids.clear();
		
        for(int i=0; i<sortedKids.size(); i++){
            if ( editKid.getText().toString().equals("") || sortedKids.get(i).getName().toLowerCase().contains(editKid.getText().toString().toLowerCase())) {
                LostKid kid = new LostKid(sortedKids.get(i).getName(), sortedKids.get(i).getAge());
                sortedFiltredKids.add(kid);
            }
        }
    }

    //Throw Close beach attempt
    private void updateLayout() {
        Log.d(LOGTAG, "Mensaje de updateLayout: START");
        Log.d(LOGTAG, "Mensaje de updateLayout: state " + state);

        if (state.equals("open")){
            sSwitch.setChecked(true);
            openedLayout.setVisibility(View.VISIBLE);
            closedLayout.setVisibility(View.GONE);
            lHappyRes.setText(String.valueOf(happiness));
            lDirtRes.setText(String.valueOf(dirtiness));
            switch (flag){
                case 0 :
                    lFlagRes.setBackgroundColor(Color.GREEN);
                    rGreen.setChecked(true);
                    break;
                case 1 :
                    lFlagRes.setBackgroundColor(Color.YELLOW);
                    rYellow.setChecked(true);
                    break;
                case 2 :
                    lFlagRes.setBackgroundColor(Color.RED);
                    rRed.setChecked(true);
                    break;
            }
        }else if (state.equals("closed")){
            sSwitch.setChecked(false);
            closedLayout.setVisibility(View.VISIBLE);
            openedLayout.setVisibility(View.GONE);
        }
        Log.d(LOGTAG, "Mensaje de updateLayout: END");
    }

    //Validate beach status
    private boolean statusBeach() {
        boolean logstatus = false;
        Log.d(LOGTAG, "Mensaje de statusBeach: START");

        BasicHttpParams params=new BasicHttpParams();
        params.setParameter("username", mUser);
        params.setParameter("authenticationToken", authenticationToken);

        //realizamos una peticion y como respuesta obtenes un JSONObject
        JSONObject resultObject = connection.getBeachStatus(params, urlStatus, authenticationToken);
        if (resultObject != null && resultObject.length() > 0) {
            try {
                state = resultObject.getString("state");
                if (state.equals("open")) {
                    flag = resultObject.getInt("flag");
                    happiness = resultObject.getInt("happiness");
                    dirtiness = resultObject.getInt("dirtiness");
                    kids = resultObject.getJSONArray("kids");

                    sortedKids.clear();
					
					//get kids list
                    for (int i=0; i < kids.length(); i++) {
                        LostKid kid = new LostKid(kids.getJSONObject(i).getString("name"), kids.getJSONObject(i).getInt("age"));
                        sortedKids.add(kid);
					}
					
					//Sort kids list array
                    Collections.sort(sortedKids, new Comparator<LostKid>()
                    {
                        public int compare(LostKid left, LostKid right)
                        {
                            if(left.getAge() > right.getAge())
                                return 1;
                            if(left.getAge() < right.getAge())
                                return -1;
                            return left.getName().compareTo(right.getName());
                        }
                    });

                    Log.e(LOGTAG, "Mensaje de statusBeach: ORDER 2-> sortedKids.size()"+sortedKids.size());
                    for(int i=0; i<sortedKids.size(); i++)
                        Log.e(LOGTAG, "Mensaje de statusBeach: ORDER 2-> ("+i+")"+sortedKids.get(i).getName()+" : "+sortedKids.get(i).getAge());

                }
                logstatus = true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.e(LOGTAG, "Mensaje de statusBeach: END");
        }
        return logstatus;
    }

    //Validate Open/Close beach
    private boolean openCloseBeach(String BeachStatus) {
        Log.e(LOGTAG, "Mensaje de openCloseBeach: START");
        boolean logstatus = false;

		//set params
        BasicHttpParams params=new BasicHttpParams();
        params.setParameter("username", mUser);
        params.setParameter("authenticationToken", authenticationToken);

        //Make the request
        String urlToSend = urlOpen;
        if (BeachStatus == "closed") urlToSend = urlClose;
        Boolean result = connection.getBeachOpened(params, urlToSend, authenticationToken);
        Log.e(LOGTAG, "Mensaje de openCloseBeach: result = "+result);
        if (result == true) {
            state = BeachStatus;
            logstatus = true;
        }
        Log.e(LOGTAG, "Mensaje de openCloseBeach: END");
        return logstatus;
    }

    //Set flag status
    private boolean flagBeach(int pFlag) {
        boolean logstatus = false;
        Log.e(LOGTAG, "Mensaje de flagBeach: START");

		//set params
        JSONObject params = new JSONObject();
        try {
            params.put("flag", pFlag);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Make the request
        Boolean result = connection.setBeachFlag(params, urlFlag, authenticationToken);
        Log.e(LOGTAG, "Mensaje de flagBeach: result"+result);
        if (result == true) {
            flag = pFlag;
            logstatus = true;
        }
        Log.e(LOGTAG, "Mensaje de flagBeach: END");
        return logstatus;
    }

    //Set Happy status
    private boolean jobBeach(String job) {
        boolean logstatus = false;
        Log.e(LOGTAG, "Mensaje de jobBeach: START");

        //Make the request
        String urlToSend = urlClean;
        if (job == "balls") urlToSend = urlBalls;
        Boolean result = connection.setBeachClean(urlToSend, authenticationToken);
        Log.e(LOGTAG, "Mensaje de jobBeach: result"+result);
        if (result == true) {
            if (job == "clean" ) dirtiness = 0;
            if (job == "balls" ) happiness = 100;
            logstatus = true;
        }
        Log.e(LOGTAG, "Mensaje de jobBeach: END");
        return logstatus;
    }

    //AsyncTask to get Beach status
    private class BeachStatusTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            Log.d(LOGTAG, "Mensaje de BeachStatusTask: START");

            Boolean resultat=statusBeach();
            return resultat;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            vStatusTask = null;

            if (success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateLayout();
                    }
                });
                Log.d(LOGTAG, "Mensaje de BeachStatusTask: END");
            } else {
                Log.e(LOGTAG, "Mensaje de BeachStatusTask: ERROR");
            }
            sSwitch.setEnabled(true);
        }

        @Override
        protected void onCancelled() {
            vStatusTask = null;
        }
    }

    //AsyncTask to open beach
    private class BeachOpenCloseTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(LOGTAG, "Mensaje de BeachOpenTask: START");

            String beachStatus = params[0];
            Boolean resultat=openCloseBeach(beachStatus);
            return resultat;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            vOpenTask = null;

            if (success) {
                attemptStatus();
                Log.d(LOGTAG, "Mensaje de BeachOpenTask: END");
            } else {
                Log.d(LOGTAG, "Mensaje de BeachOpenTask: ERROR");
            }
            sSwitch.setEnabled(true);
        }

        @Override
        protected void onCancelled() {
            vOpenTask = null;
        }
    }

    //AsyncTask to Change Flag
    private class BeachFlagTask extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            Log.d(LOGTAG, "Mensaje de BeachFlagTask: START");
            Log.d(LOGTAG, "Mensaje de doInBackground: params[0]"+params[0]);
            Boolean resultat=flagBeach(params[0]);
            return resultat;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            vOpenTask = null;

            if (success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateLayout();
                    }
                });
                Log.d(LOGTAG, "Mensaje de BeachFlagTask: END");
            } else {
                Log.d(LOGTAG, "Mensaje de BeachFlagTask: ERROR");
            }
        }

        @Override
        protected void onCancelled() {
            vOpenTask = null;
        }
    }
	
	//AsyncTask to Jobs: throw balls & clean
    private class BeachJobsTask extends AsyncTask<String, Void, Boolean> {
	
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(LOGTAG, "Mensaje de BeachJobsTask: START");

            Boolean resultat=jobBeach(params[0]);
            return resultat;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            vOpenTask = null;

            if (success) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateLayout();
                    }
                });
                Log.d(LOGTAG, "Mensaje de BeachJobsTask: END");
            } else {
                Log.d(LOGTAG, "Mensaje de BeachJobsTask: ERROR");
            }
        }

        @Override
        protected void onCancelled() {
            vOpenTask = null;
        }
    }
}