package com.manimahler.android.scheduler3g;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.text.TextUtils;
import android.util.Log;

public class RootCommandExecuter {

    private static final String TAG = RootCommandExecuter.class.getSimpleName();

    // from https://stackoverflow.com/questions/1101380/determine-if-running-on-a-rooted-device
    public static boolean isRootAvailable(){

        String path = System.getenv("PATH");

        if (TextUtils.isEmpty(path))
        {
            return false;
        }

        for(String pathDir : path.split(":")){
            if(new File(pathDir, "su").exists()) {
                return true;
            }
        }
        return false;
    }


    public static boolean canRunRootCommands() {
        boolean result = false;
        Process suProcess = null;

        // improve performance, avoid zombie processes on non-rooted devices
        if (! isRootAvailable()) {
            Log.d(TAG, "su not found. Device not rooted.");
            return false;
        }

        try {
            // TODO: consider using ProcessBuilder also here
            suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
            DataInputStream osRes = new DataInputStream(suProcess.getInputStream());

            if (null != os && null != osRes) {
                // Getting the id of the current user to check if this is root
                os.writeBytes("id\n");
                os.flush();

                String currUid = osRes.readLine();
                boolean exitSu = false;
                if (null == currUid) {
                    result = false;
                    exitSu = false;
                    Log.d(TAG, "Can't get root access or denied by user");
                } else if (true == currUid.contains("uid=0")) {
                    result = true;
                    exitSu = true;
                    Log.d(TAG, "Root access granted");
                } else {
                    result = false;
                    exitSu = true;
                    Log.d(TAG, "Root access rejected: " + currUid);
                }

                if (exitSu) {
                    os.writeBytes("exit\n");
                    os.flush();
                }
            }
        } catch (Exception e) {
            // Can't get root !
            // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

            result = false;
            Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
        }
        finally {
            if (suProcess != null) {
                suProcess.destroy();
            }
        }

        return result;
    }

    public static int execute(String command) {

        Process process = null;
        OutputStreamWriter outputWriter = null;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("su");
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            outputWriter = new OutputStreamWriter(process.getOutputStream());

            outputWriter.write(command);
            outputWriter.flush();
            outputWriter.close();

        } catch (IOException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error executing internal operation", ex);
            throw ex;
        } finally {
            if (outputWriter != null) {
                try {
                    outputWriter.close();
                } catch (IOException ex) {
                    Log.e(TAG, "Can't close output stream writer.", ex);
                }
            }
        }
        try {
            if (process != null) {
                process.waitFor();
                process.destroy();
            } else {
                return -1;
            }
        } catch (InterruptedException ex) {
            Log.e(TAG, "Process interrupted.", ex);
            return -1;
        }
        return process.exitValue();
    }

    public static int execute(ArrayList<String> commands) {


        int suProcessRetval = -1;

        if (null == commands || commands.size() <= 0) {
            return suProcessRetval;
        }

        try {

            Process suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

            // Execute commands that require root access
            for (String currCommand : commands) {
                os.writeBytes(currCommand + "\n");
                os.flush();
            }

            os.writeBytes("exit\n");
            os.flush();

            try {
                suProcessRetval = suProcess.waitFor();
                if (suProcessRetval != 0) {
                    // Root access granted
                    Log.d(TAG, "Command executed with error " + suProcessRetval);
                } else {
                    // Root access probably denied (or some other error occurred)
                    Log.w(TAG, "Command executed with return value " + suProcessRetval);
                }
            } catch (Exception ex) {
                Log.e(TAG, "Error executing root action", ex);
            }

            os.close();

        } catch (IOException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w(TAG, "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w(TAG, "Error executing internal operation", ex);
        }

        return suProcessRetval;
    }
}
