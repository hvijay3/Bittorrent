import Peer_Related.*;

import java.io.*;
import java.io.IOException;
import java.util.*;

import static java.lang.Runtime.*;

/**
 * Created by Harshit Vijayvargia on 4/3/2017.
 */
public class StartPeers {

    public static void main(String args[]) throws IOException {
        String path = System.getProperty("user.dir");

try {
    //Process pr = Runtime.getRuntime().exec ("cmd"+ "cd " + path );
    String command = "java PeerProcess 1001";
    Runtime.getRuntime().exec("ssh " + "localhost" + " cd " + path + " ; " +
            "javac Peer_Related//PeerProcess.java");





    } catch(Exception e) {
        System.out.println(e.toString());
        e.printStackTrace();
    }


}}

