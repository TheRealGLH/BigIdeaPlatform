package loginclient;

import PlatformGameShared.Enums.LoginState;
import PlatformGameShared.Enums.RegisterState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class PlatformLoginClientREST implements IPlatformLoginClient {

    HttpClient client;
    public static String domain = "http://localhost:8080";
    public static String ApiUrl = "/MartijnDaniels/PlatformLoginAPI/1.0.0";

    @Override
    public LoginState attemptLogin(String username, String password) {
        HttpResponse response = null;
        try {
            response = sendPostRequest("/login","{\n" +
                    "  \"name\": \""+username+"\",\n" +
                    "  \"password\": \""+password+"\"\n" +
                    "}");
            int status = response.getStatusLine().getStatusCode();
            switch (status){
                case 200:
                    return LoginState.SUCCESS;
                case 401:
                    return LoginState.BANNED;
                case 403:
                    return LoginState.INCORRECTDATA;
                default:
                    System.out.println("[PlatformLoginClientREST] Unexpected HTTP response code "+status+" on login for "+username+":");
                    System.out.println(response.toString());
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        System.out.println(line);
                    }
                    return LoginState.ERROR;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return LoginState.ERROR;
        }
    }

    @Override
    public int getPlayerWins(String username) {
        throw new UnsupportedOperationException("method attemptLogin has not yet been implemented");
    }

    @Override
    public RegisterState attemptRegistration(String username, String password) {
        HttpResponse response = null;
        try {
            response = sendPostRequest("/login","{\n" +
                    "  \"name\": \""+username+"\",\n" +
                    "  \"password\": \""+password+"\"\n" +
                    "}");
            int status = response.getStatusLine().getStatusCode();
            switch (status){
                case 200:
                    return RegisterState.SUCCESS;
                case 409:
                    return RegisterState.ALREADYEXISTS;
                case 403:
                    return RegisterState.INCORRECTDATA;
                default:
                    System.out.println("[PlatformLoginClientREST] Unexpected HTTP response code "+status+" on login for "+username+":");
                    System.out.println(response.toString());
                    BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    String line = "";
                    while ((line = rd.readLine()) != null) {
                        System.out.println(line);
                    }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RegisterState.ERROR;
    }

    @Override
    public String getLevelContent(String levelname) {
        try {
            HttpResponse response = sendGetRequest("/level/" + levelname);
            System.out.println(response.getStatusLine());
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line = "";
            StringBuilder sb = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "null";
    }


    private HttpResponse sendGetRequest(String path) throws IOException {
        client =  new DefaultHttpClient();
        HttpGet request = new HttpGet(domain + ApiUrl + path);
        request.setHeader(HttpHeaders.ACCEPT, "application/json");
        return client.execute(request);
    }

    private HttpResponse sendPostRequest(String path, String inputString) throws IOException {
        client =  new DefaultHttpClient();
        HttpPost post = new HttpPost(domain + ApiUrl + path);
        post.setHeader(HttpHeaders.ACCEPT, "application/json");
        StringEntity input = new StringEntity(inputString);
        input.setContentType("application/json");
        post.setEntity(input);
        return client.execute(post);
    }


}