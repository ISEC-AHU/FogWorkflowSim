package org.workflowsim.utils;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.Writer;

public class TSPSocketRequest implements Runnable {
    /**
     * The output buffer
     */
    private Writer out;

    /**
     * The input buffer
     */
    private DataInputStream in;

    /**
     * The content to be sent
     */
    private String content;

    /**
     * The server response
     */
    private volatile String response = null;


    public String getResponse() {
        return response;
    }

    public TSPSocketRequest(Writer out, DataInputStream in, String content) {
        this.out = out;
        this.in = in;
        this.content = content;
    }


    @Override
    public void run() {
        try {
            out.write(this.content);
            out.flush();
            response = in.readUTF();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
