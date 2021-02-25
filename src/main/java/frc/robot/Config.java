package frc.robot;

/****************************************************************************************************
* General purpose tool to allow users have variable configs saved in a file on the robot.
* This is prefered to using Prefercnes and you can have all the fields you want in the file
* set as comments uing a # symbol. Then when you need to use the you just uncomment. Doing the same
* in Preferecnes is not an option. Preferecnes can still be used to make changes that need to be done
* quickly. 
*
*<p>
*<pre>
* Changes ********************************************************************************************
* Date          Ver.    BY      Description
* Feb. 14, 2016 1.00    FWL     Initial submission
*</pre>
*<p>			 
* @author Frank Larkin, FRC272 - Lansdale Catholic Robotics  pafwl@aol.com
* @version 1.00
*   
*/


import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/************************
 * Class Constructor. You pass in the file name. Example: /c/MyConfigFile.cfg.
 * In the roboRio it is suggested you use the /c folder as it is available to the 
 * lvuser and only contain info text files.      
 * <p>
 * The config file uses a # symbol to indicate comment. The actual data lines have 2 required parts. 
 * The key is how you refer to your setting in code and the value is a text representation of the data. 
 * These are pipe separated. You can have as much white space as you like. 
 * The last pipe separated field is any comments. This is ignored when read. Below is an example...
 * <pre>    
 * shooter.ip_ClosePosition 	       | 100       | extra stuff
 * shooter.ip_OnTargetPosition         | 
 * shooter.dp_FastUpPower 		       | 30        | more extra stuff
 * shooter.dp_FastDownPower 	       | .5
 * shooter.dp_SlowUpPower		       | .2
 * shooter.dp_SlowDownPower 	       | .2
 *
 * #Camera stuff
 * #D_PickupPanPosition                |
 * #D_PickupTiltPosition               |
 * #D_LoweredPanPosition               |
 * #D_LoweredTiltPosition              |
 * #D_DrivingPanPosition               |
 * #D_DrivingTiltPosition              |
 * #D_TargetingPanPosition             |
 * #D_TargetingTiltPosition            |
 * #d_CameraLookingForwardPWMValue     |
 * #d_CameraLookingRearwardPWMValue    |
 * #I_CameraMaxFPS                     |
 * #S_CameraIpAddress                  |            | IPV 4 IP address
 * #S_CameraUser                       |
 * #S_CameraPassword                   |
 *
 * #Target
 * #I_FovPixelsX                       |
 * #I_FovPixelsY                       |
 *
 * #Telemetry values
 * Tele_FileName                       | FRC272_HH_Telemerty       | string just a file name. tele will add the .xls
 * #Tele_FilePath                      |             | string 
 * Tele_TimestampFile                  | true        | boolean
 * </pre> 
 *
 * You should get a copy of WINScp and load it on your driver station or programming computes. 
 * Then is is very easy to make and save changes on the robot and in your code.
 * <p>
 * This code will automatically try to read the config file when it is Constructed.
 * You can then call the load method anytime to reload the data. 
 * One suggested idea is to call it when you go into disabled init. 
 * Then it is read once every time you go into disable from teleop or auton. 
 * So you can make change, go into disabled and they will be read from the file. 
 * Then you have to all the appropriate method to get them into you variables.
 * See examples below...
 * <pre>
 *    // In robot class
 *     public void disabledInit() {
 *
 *    	telem.restartTimer();
 *    	config.load();
 *    	
 *    	this.telem.loadConfig(config);
 *    }
 *
 *    // In telemetry class
 *    public void loadConfig(Config config){
 *    	
 *        this.sp_FileName = config.getString("Tele_FileName", "telemetry");
 *        this.sp_FilePath = config.getString("Tele_FilePath", "/tmp"); 
 *        this.bp_TimestampFile = config.getBoolean("Tele_TimestampFile", false);
 *        
 *    }
 *
 */

public class Config {

    private Map<String, String> dictColumnData; /** This is a string key, value map. You put in the key and get the value. */ 
	String s_FileName;
	
    public Config (String s_FilePath, String s_UserFileName){
	    s_FileName =    s_FilePath + "/" + s_UserFileName;
	    dictColumnData = new HashMap<String, String>();	// when we load we want to reload this too to clear out the old data
	    load();
    }

/************************
 * Used to open the config file and read in the variables. Everything is saved in a hash table (key value pair).
 * While reading anything that starts with a # will be ignored as well as blank lines even if filled with whitespace.
 * Message are sent to the console if you have it enabled so you can see problems in reading teh file.    
 */
	public void load() {

		System.out.println("Config.load(): INFO: Loading data from file [" + this.s_FileName + "]");
	    dictColumnData.clear(); // when we load we want to clear out the old data

    	try{
    		
		    BufferedReader in = new BufferedReader(new FileReader(this.s_FileName));
		    String str;
		    Integer line = 0; 
		    while ((str = in.readLine()) != null){
		    	
		    	line++;
		    	
		    	str = str.trim();
		    	if(str.length() == 0 || str.charAt(0) == '#')
		    			continue;
		    	
		    	// break it up 
		    	String[] fields = str.split("\\|");
		    	
		    	if (fields.length < 2){
		    		System.out.println("Config.load(): ****ERROR Line " + line.toString() + " is not a comment (#) but does not have a pipe (|) delimiter!");
		    		continue;
		    	}	    	
		    	
		    	String key = fields[0].trim();
		    	String value = fields[1].trim();
	  	
		    	if( key.length() == 0){
		    		System.out.println("Config.load(): *WARN: Line " + line.toString() + " Key has no data in it, zero length, ignoring!!!");
		    		continue;
		    	}
		    	
		    	if( value.length() == 0){
		    		System.out.println("Config.load(): *WARN: Line " + line.toString() + " Value has no data in it, zero length, ignoring!!!");
		    		continue;
		    	}
		    	
		    	System.out.println("Config.load(): INFO: Line=" + line.toString() + "  Key=[" + key + "  Value=[" + value + "]"  );
		    	dictColumnData.put(key,value);			// save the data 
		    	
		    }

		    in.close();

		    
		} catch (IOException e) {
    		System.out.println("Config.load(): Issue!!!!: Failed to load the file " + this.s_FileName + 
    				"   Exception:" + e + "  Reason:" + e.getMessage() );
		}

    }
    	

    private String getValue( String key ){
    	
        if(dictColumnData.containsKey(key))		
            return dictColumnData.get(key);
        else
        	return "";
    }
    	
    /************************
 * Used to pull an int value from the config file.     
 * You can monitor the console to see if you values was pulled correctly.      
 */
	public int getInt( String key, int defaultValue){

		String sValue = getValue(key);

		if( sValue == "")
			return defaultValue;

		try{ 
			double dValue = Double.parseDouble(sValue);  // read as double first in case there is decimal
			int retValue = (int) dValue;									      // now convert to int
    		System.out.println("Config.getInt(): INFO: Key=" + key +  "  Config value=[" + sValue + "]");
    		return retValue;
		} catch( Exception e ) {
    		System.out.println("Config.getInt(): ***ERROR converting saved string value to int! Key=" + key +  "  Value String=[" + sValue + "]" + 
    				"   Exception:" + e + "  Reason:" + e.getMessage() );
    		return defaultValue;
		}

	}


    /************************
 * Used to pull a double value from the config file.     
 * You can monitor the console to see if you values was pulled correctly.      
 */
	public double getDouble( String key, double defaultValue){

		String sValue = getValue(key);

		if( sValue == "")
			return defaultValue;
		
		try{
			double retValue = Double.parseDouble(sValue);
    		System.out.println("Config.getDouble(): INFO: Key=" + key +  "  Config value=[" + sValue + "]");
    		return retValue;
		} catch( Exception e ) {
    		System.out.println("Config.getDouble(): ***ERROR converting saved string value to double! Key=" + key +  "  Value String=[" + sValue + "]" + 
    				"   Exception:" + e + "  Reason:" + e.getMessage() );
    		return defaultValue;
		}
		
	}


    /************************
 * Used to pull a string value from the config file.     
 * You can monitor the console to see if you values was pulled correctly.      
 */
	public String getString( String key, String defaultValue){

		String sValue = getValue(key);

		if( sValue == "")
			return defaultValue;

  		System.out.println("Config.getString(): INFO: Key=" + key +  "  Config value=[" + sValue + "]");
		return sValue;	
		
	}

    /************************
 * Used to pull a boolean (true,false) value from the config file.
 * You can monitor the console to see if you values was pulled correctly.      
 */
	public boolean getBoolean( String key, boolean defaultValue){

		String sValue = getValue(key).toLowerCase();

		if( sValue.equalsIgnoreCase("true")){
    		System.out.println("Config.getBoolean(): INFO: Key=" + key +  "  Config value=[" + sValue + "]");
			return true;
		} else if( sValue.equalsIgnoreCase("false")) {
    		System.out.println("Config.getBoolean(): INFO: Key=" + key +  "  Config value=[" + sValue + "]");
			return false;
		}else {
    		System.out.println("Config.getBoolean(): *WARN: Key=" + key +  "  Value String=[" + sValue + "]" + 
    					"  Not true or false, returning default value." );
			return defaultValue;
		}
	}
	
}