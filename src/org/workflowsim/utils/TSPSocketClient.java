package org.workflowsim.utils;

import com.mathworks.toolbox.javabuilder.external.org.json.JSONArray;
import com.mathworks.toolbox.javabuilder.external.org.json.JSONException;
import com.mathworks.toolbox.javabuilder.external.org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Utils class for sending the simulation's environment information to the placement server
 *
 * @author Julio Corona
 * @since WorkflowSim Toolkit 1.0 's TSP extension
 */
public class TSPSocketClient {
    // initialize socket and input output streams

    /**
     * The socket connection
     */
    private static Socket socket;

    /**
     * The output buffer
     */
    private static Writer out;

    /**
     * The input buffer
     */
    private static DataInputStream in;

    /**
     * Open the connection to the server
     * @param address the server' ip address
     * @param port the server' port address
     */
    public static void openConnection(String address, int port){
        try {
            socket = new Socket(address, port);
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
            in = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Sends to the placement agent the properties of the servers properties
     * @param cloudNodeFeatures the cloud server's properties
     * @param fogNodesFeatures the fog server's properties
     * @return the number of the server to place the task
     */
    public static String sendSeversSetup(double[] cloudNodeFeatures, double[][] fogNodesFeatures){
        try {
            // sends the info to the socket
            JSONObject json = new JSONObject();
            json.put("action", "setup");

            JSONObject data = new JSONObject();
            data.put("cloud", new JSONArray(cloudNodeFeatures));
            data.put("fog", new JSONArray(fogNodesFeatures));

            json.put("data", data);
            out.write(json.toString());
            out.flush();

            // waiting for the response
            return in.readUTF();
        }
        catch (IOException | JSONException i) {
            i.printStackTrace();
        }
        return "ERROR if reaches this point";
    }

    /**
     * Select a server for the placement
     * @param state the simulation's environment information
     * @param cloudletId the simulation's environment information
     * @return the number of the server to place the task
     */
    public static int askForDecision(int cloudletId, Long[] state){

        try {
            // sends the info to the socket
            JSONObject json = new JSONObject();
            json.put("action", "inference");

            JSONObject data = new JSONObject();
            data.put("cloudletId", cloudletId);
            data.put("state", new JSONArray(state));

            json.put("data", data);
            out.write(json.toString());
            out.flush();

            // waiting for the response
            String content= in.readUTF();

            return Integer.parseInt(content);
        }
        catch (IOException | JSONException i) {
            i.printStackTrace();
        }
        return -1;
    }

    /**
     * Save the placement reward
     * @param cloudletId the task cloudlet id
     * @param reward the reward for RL agents
     * @return the server result
     */
    public static String saveReward(int cloudletId, Double reward){

        try {
            // sends the info to the socket
            JSONObject json = new JSONObject();
            json.put("action", "retrain");

            JSONObject data = new JSONObject();
            data.put("cloudletId", cloudletId);
            data.put("reward", reward);

            json.put("data", data);
            out.write(json.toString());
            out.flush();

            // waiting for the response
            return in.readUTF();
        }
        catch (IOException | JSONException i) {
            i.printStackTrace();
        }

        return "saveReward reached a undesirable point";
    }

    /**
     * Send the new state for retraining the RL model
     * @param cloudletId the task cloudlet id
     * @param state the simulation's environment information
     * @return the number of the server to place the task
     */
    public static String retrain(int cloudletId, Long[] state){
        try {
            // sends the info to the socket
            JSONObject json = new JSONObject();
            json.put("action", "steep");

            JSONObject data = new JSONObject();
            data.put("cloudletId", cloudletId);
            data.put("state", new JSONArray(state));

            json.put("data", data);
            out.write(json.toString());
            out.flush();

            // waiting for the response
            return in.readUTF();
        }
        catch (IOException | JSONException i) {
            i.printStackTrace();
        }
        return "saveTheNewStateAfterDecision reached a undesirable point";
    }

    /**
     * Close the connection with the server
     */
    public static void closeConnection(){
        // close the connection
        try {
            out.close();
            in.close();
            socket.close();
            System.out.println("Disconnected");
        }
        catch (IOException e) {
            System.err.println(e);
        }
    }
}
