package com.manimahler.android.scheduler3g;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.util.Log;

public class RootCommandExecuter {

	private static final String TAG = RootCommandExecuter.class.getSimpleName();
	
	public static boolean canRunRootCommands()
    {
       boolean retval = false;
       Process suProcess;

       try
       {
          suProcess = Runtime.getRuntime().exec("su");

          DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
          DataInputStream osRes = new DataInputStream(suProcess.getInputStream());

          if (null != os && null != osRes)
          {
             // Getting the id of the current user to check if this is root
             os.writeBytes("id\n");
             os.flush();

             String currUid = osRes.readLine();
             boolean exitSu = false;
             if (null == currUid)
             {
                retval = false;
                exitSu = false;
                Log.d(TAG, "Can't get root access or denied by user");
             }
             else if (true == currUid.contains("uid=0"))
             {
                retval = true;
                exitSu = true;
                Log.d(TAG, "Root access granted");
             }
             else
             {
                retval = false;
                exitSu = true;
                Log.d(TAG, "Root access rejected: " + currUid);
             }

             if (exitSu)
             {
                os.writeBytes("exit\n");
                os.flush();
             }
          }
       }
       catch (Exception e)
       {
          // Can't get root !
          // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

          retval = false;
          Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
       }

       return retval;
    }
    
	public static int execute(String command) {
		
		ArrayList<String> commands = new ArrayList<String>();
		
		commands.add(command);
		
		return execute(commands);
	}
	
    public static int execute(ArrayList<String> commands)
    {
       
       
       int suProcessRetval = -1;
       
       try
       {
         
          if (null != commands && commands.size() > 0)
          {
             Process suProcess = Runtime.getRuntime().exec("su");

             DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

             // Execute commands that require root access
             for (String currCommand : commands)
             {
                os.writeBytes(currCommand + "\n");
                os.flush();
             }

             os.writeBytes("exit\n");
             os.flush();
             
             try
             {
                suProcessRetval = suProcess.waitFor();
                if (suProcessRetval != 0)
                {
                   // Root access granted
                	Log.d(TAG, "Command executed with error " + suProcessRetval);
                }
                else
                {
                   // Root access probably denied (or some other error occurred)
                	Log.w(TAG, "Command executed with return value " + suProcessRetval);
                }
             }
             catch (Exception ex)
             {
                Log.e(TAG, "Error executing root action", ex);
             }
             
             os.close();
          }
       }
       catch (IOException ex)
       {
          Log.w(TAG, "Can't get root access", ex);
       }
       catch (SecurityException ex)
       {
          Log.w(TAG, "Can't get root access", ex);
       }
       catch (Exception ex)
       {
          Log.w(TAG, "Error executing internal operation", ex);
       }
       
       return suProcessRetval;
    }
}
