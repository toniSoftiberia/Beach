package cat.lafosca.platja.beach;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


/*CLASE AUXILIAR PARA EL ENVIO DE PETICIONES A NUESTRO SISTEMA
 * Y MANEJO DE RESPUESTA.*/
public class HttpMethod {

    private InputStream is = null;
    private String result = "";
    private int status;

    private static final String LOGTAG = "LogsHttpMethod";
    private static final String api = "http://lafosca-beach.herokuapp.com/api/v1";

	//public function to call from activities
    public JSONObject getRegisterData(JSONObject parameters, String urlwebserver ){
        httpPostRegister(parameters,api+urlwebserver);

        if (is!=null){//if get response
            getResponse();
            return getJsonArray();
        }else
            return null;
    }

	//public function to call from activities
    public JSONObject getLoginData(BasicHttpParams parameters, String urlwebserver ){
        httpGetLogin(parameters, api + urlwebserver);

        if (is!=null){//if get response
            getResponse();
            return getJsonArray();
        }else
            return null;
    }

	//public function to call from activities
    public JSONObject getBeachStatus(BasicHttpParams parameters, String urlwebserver, String token){
        httpGetBeachStatus(parameters, api + urlwebserver, token);

        if (is!=null){//if get response
            getResponse();
            return getJsonArray();
        }else
            return null;
    }

	//public function to call from activities
    public Boolean getBeachOpened(BasicHttpParams parameters, String urlwebserver, String token ){
        httpPut(parameters, api + urlwebserver, token);
        return getStatus();
    }

	//public function to call from activities
    public Boolean setBeachFlag(JSONObject parameters, String urlwebserver, String token ){
        httpPutParams(parameters, api + urlwebserver, token);
        return getStatus();
    }

	//public function to call from activities
    public Boolean getBeachClosed(BasicHttpParams parameters, String urlwebserver, String token  ){
        httpPut(parameters, api + urlwebserver, token);
        return getStatus();
    }

	//public function to call from activities
    public Boolean setBeachBalls(String urlwebserver, String token  ){
        httpPost(api + urlwebserver, token);
        return getStatus();
    }

	//public function to call from activities
    public Boolean setBeachClean(String urlwebserver, String token  ){
        httpPost(api + urlwebserver, token);
        return getStatus();
    }

    //request POST
    private void httpPostRegister(JSONObject parametros, String urlwebserver){
        Log.d(LOGTAG, "Mensaje de httpPostRegister: START");
        HttpClient hc = new DefaultHttpClient();
        String message;

        try{
            HttpPost httpPost = new HttpPost(urlwebserver);

            message = parametros.toString();
            Log.d(LOGTAG, "Mensaje de httpPostRegister: message >>"+parametros+"<<");

            httpPost.setEntity(new StringEntity(message, "UTF8"));
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPost.setHeader("Accept", "application/json");

            HttpResponse resp = hc.execute(httpPost);
            status = resp.getStatusLine().getStatusCode();
            Log.d(LOGTAG, "Mensaje de httpPostRegister: getStatusCode >>"+resp.getStatusLine().getStatusCode()+"<<");
            HttpEntity entity = resp.getEntity();

            status = resp.getStatusLine().getStatusCode();
            is = entity.getContent();

        } catch(Exception e){
            Log.e(LOGTAG, "Mensaje de httpPostRegister: Error in http connection " + e.toString());

        }
        Log.d(LOGTAG, "Mensaje de httpPostRegister: END");
    }

    //request GET
    private void httpGetLogin(BasicHttpParams parametros, String urlwebserver){
        Log.d(LOGTAG, "Mensaje de httpGetLogin: START");
        HttpClient hc = new DefaultHttpClient();

        try{
            HttpGet httpGet = new HttpGet(urlwebserver);

            Log.d(LOGTAG, "Mensaje de httpGetLogin: message >>"+parametros+"<<");

            httpGet.setParams(parametros);
            httpGet.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpGet.setHeader("Accept", "application/json");

            String credentials = parametros.getParameter("username")+":"+parametros.getParameter("password");
            String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
            httpGet.setHeader("Authorization", "Basic " + base64EncodedCredentials);

            HttpResponse resp = hc.execute(httpGet);
            Log.d(LOGTAG, "Mensaje de httpGetLogin: getStatusCode >>"+resp.getStatusLine().getStatusCode()+"<<");
            HttpEntity entity = resp.getEntity();

            status = resp.getStatusLine().getStatusCode();
            is = entity.getContent();

        } catch(Exception e){
            Log.e(LOGTAG, "Mensaje de httpGetLogin: Error in http connection " + e.toString());

        }
        Log.d(LOGTAG, "Mensaje de httpGetLogin: END");
    }

    //request GET
    private void httpGetBeachStatus(BasicHttpParams parametros, String urlwebserver, String token){
        Log.d(LOGTAG, "Mensaje de httpGetBeachStatus: START");
        HttpClient hc = new DefaultHttpClient();

        try{
            HttpGet httpGet = new HttpGet(urlwebserver);

            Log.d(LOGTAG, "Mensaje de httpGetBeachStatus: message >>"+parametros+"<<");

            httpGet.setParams(parametros);
            httpGet.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpGet.setHeader("Accept", "application/json");
            httpGet.setHeader("Authorization", "Token token=" + token + "");

            HttpResponse resp = hc.execute(httpGet);

            Log.d(LOGTAG, "Mensaje de httpGetBeachStatus: getStatusCode >>"+resp.getStatusLine().getStatusCode()+"<<");
            HttpEntity entity = resp.getEntity();

            status = resp.getStatusLine().getStatusCode();
            is = entity.getContent();

        } catch(Exception e){
            Log.e(LOGTAG, "Mensaje de httpGetBeachStatus: Error in http connection " + e.toString());

        }
        Log.d(LOGTAG, "Mensaje de httpGetBeachStatus: END");
    }

    //request PUT for Flag
    private void httpPut(BasicHttpParams parametros, String urlwebserver, String token){
        Log.d(LOGTAG, "Mensaje de httpPut: START");
        HttpClient hc = new DefaultHttpClient();

        try{
            HttpPut httpPut = new HttpPut(urlwebserver);

            httpPut.setParams(parametros);
            httpPut.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Authorization", "Token token=" + token + "");

            HttpResponse resp = hc.execute(httpPut);

            Log.d(LOGTAG, "Mensaje de httpPut: getStatusCode >>"+resp.getStatusLine().getStatusCode()+"<<");
            HttpEntity entity = resp.getEntity();

            status = resp.getStatusLine().getStatusCode();
            is = entity.getContent();

        } catch(Exception e){
            Log.e(LOGTAG, "Mensaje de httpPut: Error in http connection " + e.toString());

        }
        Log.d(LOGTAG, "Mensaje de httpPut: END");
    }

    //request PUT for
    private void httpPutParams(JSONObject parametros, String urlwebserver, String token){
        Log.d(LOGTAG, "Mensaje de httpPutParams: START");
        HttpClient hc = new DefaultHttpClient();

        try{
            HttpPut httpPut = new HttpPut(urlwebserver);

            String message = parametros.toString();
            Log.d(LOGTAG, "Mensaje de httpPutParams: message >>"+parametros+"<<");

            httpPut.setEntity(new StringEntity(message, "UTF8"));

            //httpPut.setParams(flag);
            httpPut.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPut.setHeader("Accept", "application/json");
            httpPut.setHeader("Authorization", "Token token=" + token + "");

            HttpResponse resp = hc.execute(httpPut);

            Log.d(LOGTAG, "Mensaje de httpPutParams: getStatusCode >>"+resp.getStatusLine().getStatusCode()+"<<");
            status = resp.getStatusLine().getStatusCode();

        } catch(Exception e){
            Log.e(LOGTAG, "Mensaje de httpPutParams: Error in http connection " + e.toString());

        }
        Log.d(LOGTAG, "Mensaje de httpPutParams: END");
    }

    //request POST
    private void httpPost(String urlwebserver, String token){
        Log.d(LOGTAG, "Mensaje de httpPost: START");
        HttpClient hc = new DefaultHttpClient();

        try{
            HttpPost httpPost = new HttpPost(urlwebserver);

            //set headers
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Authorization", "Token token=" + token + "");

            HttpResponse resp = hc.execute(httpPost);
            status = resp.getStatusLine().getStatusCode();
            Log.d(LOGTAG, "Mensaje de httpPost: getStatusCode >>"+resp.getStatusLine().getStatusCode()+"<<");
            HttpEntity entity = resp.getEntity();

            status = resp.getStatusLine().getStatusCode();
            is = entity.getContent();

        } catch(Exception e){
            Log.e(LOGTAG, "Mensaje de httpPost: Error in http connection " + e.toString());

        }
        Log.d(LOGTAG, "Mensaje de httpPost: END");
    }

    //Parse the request to a strings
    private void getResponse(){
        try{
            //Buffer to read
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();

            //Get data
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            is.close();
            result=sb.toString();
        }catch(Exception e){
            Log.e(LOGTAG, "Mensaje de getpostresponse: Error converting result " + e.toString());
        }
    }

    //parse json data
    private JSONObject getJsonArray(){
        try{
            JSONObject jdata = new JSONObject(result);
            return jdata;
        }
        catch(JSONException e){
            Log.e(LOGTAG, "Mensaje de getjsonarray: Error parsing data " + e.toString());
            return null;
        }
    }

    //return the status of the result
    private Boolean getStatus(){
        return status == 200 || status == 201 || status == 204;
    }
}