package frc.robot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.io.FileWriter;
import java.io.IOException;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/****************************************************************************************************
* General purpose tool to allow user to record and save robot variable data to .xls spreadsheet.
*
*<p>
*<pre>
* Changes ********************************************************************************************
* Date          Ver.    BY      Description
* Dec. 26, 2015 1.03    FWL	    1) Added comments to discuss how to get more precision for numbers.
*                               2) Fixed the Tele_FileName so if Preferences value is zero length (empty) 
*                                  the name reverts to telemetry without having to delete the preferences
*                                  key Tele_FileName
* 
* Dec. 17, 2015 1.02    FWL     Added Java docs and changed a few things. 
*                               1) changed method addHeadedr to addColumn. 
*                               2) changed headerName to columnName.
*                                
* Dec. 16, 2015 1.01    FWL     Added commenting and changed names to relate it is a spread sheet.
*
* Dec. 14, 2015 1.00    FWL     Initial submission
*</pre>
*<p>			 
* @author Frank Larkin, FRC272 - Lansdale Catholic Robotics  pafwl@aol.com
* @version 1.03
*   
*/
public class LCTelemetry { 

    private String[] listRows;								
    private String[] listColumns;				/** Arrays of columnNames, think ladder. Each step is data. They are numbered starting at 0.
     											* Order is maintained. This is how the column values can be added in any order and be
     											* printed in the correct column. 
     											*/
    
    private Map<String, String> dictColumnData; /** This is a string key, value map. You put in the key and get the value. */ 
    private int i_TotalRows;
    private int i_listColumnsIndex; 
    
    private FileWriter fileHandle;
    private Timer tim_Time;
    private DriverStation driverStation;
    private boolean bp_TimestampFile;
    private String sp_FileName;			    /** Default: telemetry.<b>Note: xls is added to the end.</b> */
    private String sp_FilePath;				/** Default: /tmp folder.(linked to /var/volitile/tmp) Lost after reboot, /home/lvuser is writable.*/

    public final int ki_MaxColumns 	= 300;				/** Number of columns. Increase if you need to. */ 
    public final int ki_MaxRows 	= 50  * 60 * 3; 	/** Default 9000: 50 times per second * 60 seconds * 3 minutes */

    private String s_TelemType = "new";


	/** Class Contructor initialize you variables here. 
	 * Here the size of the arrays can be adjusted to allow for more columns
	 * and more rows. The constants ki_MaxRos and ki_MaxColumns control that.
    */
    public LCTelemetry(String sPassedFilePath, String sPassedFileName ) {
    	tim_Time = new Timer();
    	tim_Time.start();
    	
        i_listColumnsIndex = 0;								 

        listColumns = new String[ki_MaxColumns];
        dictColumnData = new HashMap<String, String>();
        driverStation = DriverStation.getInstance();
        bp_TimestampFile =  false;
        sp_FilePath = sPassedFilePath;					/** /var/volatile/tmp folder. Lost after reboot, /home/lvuser is writable.*/
        sp_FileName = sPassedFileName;
        
        createNewList();
 
    }

    private void createNewList(){
        i_TotalRows = 0;
        listRows = new String[ ki_MaxRows + 2 ];
        s_TelemType = "new";

    }
    
    /**
    * Used to reset the Telemetry internal timer to zero. You have to add this 
    * method to be called in every Init method of the main Iterative Robot class, 
    * disabledInit, teleopInit, autonomousInit.<br> 
    * This way you will be able to see the actual time spent in each section.
    * <p>
    * Example:
    * <p>
    * <pre> 
    *   public void teleopInit(){
    *   
    *       telem.restartTimer();
    *   
    *   }
    * <pre>
    */
    public void restartTimer() {							// they can restart our internal time ever time we switch modes. 
        tim_Time.reset();
    }

    /**
    * Called to add a column header to the Telemetry object. The field should   
    * have spaces between words to allow them to easily word wrap in 
    * Excel. The exact spelling is needed to add column data.<br> 
    * Example: I Left Driver Power 
    * <b>Note: The I tells us this is from the Inputs Class.</b>
    * <p>
    * The order these are entered in will be the columns of the
    * final spread sheet. The order the data is add has no impact on this.
    * <p>
    * It is assumed that you will add a method to each class like 
    * addTelemetryHeaders(). In that class you will add the different fields
    * you want to track. This is best called in the constructor of the Robot
    * class after all the other classes have been initialized.
    * <p>
    * Example:
    * <pre>
    *   public void addTelemetryHeaders(LCTelemetry telem ){
    *       telem.addColumn("A Program Number");
    *       telem.addColumn("A Started");
    *       telem.addColumn("A Step Timer"); 
    *   }
    * <p>
    * @param	columnName string at the top of this column.  
    */
    public void addColumn(String columnName) {				// add a column to the list of columns
        listColumns[i_listColumnsIndex++] = columnName;		// this is an array so we use an index i_listColumnsIndex++ to tell use where to put it 
    }														// the ++ on the right tells Java to use the current value but then 
    														// increment AFTER you use it. 
    														// If it was ++i_listColumnsIndex it means increment and use value. 

    /**
    * Called to add an Integer to a column in the Telemetry instance.
    * The columnName is the exact spelling of the name entered in an  
    * addColumn call. The order this data is added will not change the
    * order of the columns.
    *<p>
    * Example:
    *<pre>
    *   public void writeTelemetryValues(LCTelemetry telem ){
    *       telem.saveInteger("A Program Number",this.ip_ProgramNumber);
    *       telem.saveTrueBoolean("A Started",this.b_Started);
    *       telem.saveTrueBoolean("A Is Done",this.b_IsDone); 
    *       telem.saveDouble("A Auton Timer",this.tim_AutonTimer.get());
    *       telem.saveString("A Program Desc",this.s_ProgramDescription);
    *       telem.saveString("A Step Desc",this.s_StepDescription);
    *       telem.saveInteger("A Step",this.i_Step);
    *       telem.saveDouble("A Step Timer",this.tim_StepTimer.get());
    *   }
    *</pre>
    * <p>		
    * @param	columnName string at the top of this column.  
    * @param	value Integer number to be written.  
    */
    public void saveInteger(String columnName, int value){
        dictColumnData.put(columnName,  String.format("%s",value) );		// format as an integer
    }

    
    
    /**
    * Called to add an Double float point number to a column in the
    * Telemetry instance. The columnName is the exact spelling   
    * of the name entered in an addColumn call. The order this data 
    * is added will not change the order of the columns. The resulting 
    * data will have 2 decimal points of precision.
    * <p>
    * Default precision is .2f or 2 decimal places. To save with more
    * precision convert to string and save as a sting. See saveString.
    *  
    * 
    * @param	columnName string at the top of this column.  
    * @param	value Double number to be written.  
    */
    public void saveDouble(String columnName, double value, int decimals ){
        String formatPercision = "%." + String.valueOf(decimals) + "f";
        dictColumnData.put(columnName,   String.format(formatPercision,value) );		// format as floating point with 2 decimals. 
    }

    public void saveDouble(String columnName, double value){
        saveDouble(columnName, value, 2 );		// format as floating point with 2 decimals. 
    }

    
    /**
    * Called to add a string to a column in the
    * Telemetry instance. The columnName is the exact spelling   
    * of the name entered in an addColumn call. The order this data 
    * is added will not change the order of the columns. No additional
    * formatting is done on the data. 
    * <p>
    * Example: See saveString()
    * <p>
    * If you have a double that you want to save to more precision
    * convert to a sting and save as a string. As in saving an 
    * accelerometer value you may want 6 decimal places. 
    * <pre>
    * telem.saveString("S ACCEL X", String.format("%.6f",this.d_BACC_XAxis) );
    * </pre>
    * <p>
    * @param	columnName string at the top of this column.  
    * @param	value String to be written.  
    */
    public void saveString(String columnName, String value){
        dictColumnData.put(columnName,value);								// no formatting needed
    }

    /**
    * Called to add a true/false boolean to a column in the
    * Telemetry instance. The columnName is the exact spelling   
    * of the name entered in an addColumn call. The order this data 
    * is added will not change the order of the columns. No additional
    * formatting is done on the data but Excel will fore these to CAPs. 
    * <p>
    * Example: See saveInteger()
    * <p>
    * @param	columnName string at the top of this column.  
    * @param	value Boolean to be written.  
    */
    public void saveBoolean(String columnName, Boolean value){			 
        dictColumnData.put(columnName, value.toString());					// save the Boolean as a string 
    }

    
    /**
    * Called to add a true boolean to a column in the Telemetry instance.
    * If the item is false then an empty field will be added. If true 
    * then the field will say true. Excel will set this to all CAPS. 
    * <p> 
    * The columnName is the exact spelling of the name entered in an 
    * addColumn call. The order this data is added will not change
    * the order of the columns.  
    * <p>
    * Example: See saveInteger()
    * <p>
    * @param	columnName string at the top of this column.  
    * @param	value boolean to be written.  
    */
    public void saveTrueBoolean(String columnName, Boolean value){		// we only want true or blank easier to read
        if(value == true)
            dictColumnData.put(columnName,"true");
        else
            dictColumnData.put(columnName,"");
    }

    
    /**
    * Called to add a false boolean to a column in the Telemetry instance.
    * If the item is true then an empty field will be added. If false 
    * then the field will say false. Excel will set this to all CAPS. 
    * <p> 
    * The columnName is the exact spelling of the name entered in an 
    * addColumn call. The order this data is added will not change
    * the order of the columns.  
    * <p>
    * Example: See saveInteger()
    * <p>
    * @param	columnName string at the top of this column.  
    * @param	value boolean to be written.  
    */
    public void saveFalseBoolean(String columnName, Boolean value){		// we only want false or blank, easier to read. 
        if(value == true)
            dictColumnData.put(columnName,"");
        else
            dictColumnData.put(columnName,"false");
    }

    
    /**
    * Called to tell telemetry to save the current saved
    * data into a row. This will cause the data to be saved under the
    * appropriate headers. Any columns missing data will show blank.  
    * <p>  
    * This is expected to be called at the bottom of the TeleopPeriodic
    * and the autonomousPeriodic methods of the Robot class.  
    * <p>
    * Example:
    * <p>
    * <pre>
    *     public void autonomousPeriodic() {
    *         
    *         bla...bla...bla
    *         
    *         inputs.writeTelemetryValues(telem);
    *         robotbase.writeTelemetryValues(telem);
    *         wedge.writeTelemetryValues(telem);
    *         telem.writeRow();	                          // write a record now
    *     }
	* </pre>
    */
    public void writeRow(){										// save a row of data to memory

    	//if( true ) return;
    	
        if( this.i_TotalRows >= this.ki_MaxRows )				// if we have already written MAXRows to memory then 
            return;												// return without adding more rows. The calculation for 
        														// total rows should be good for a match of 50 cycles per second * 180 seconds ( 3 minutes) = 9000
        														// they should not save during disabled mode

        String[] listColumnData = new String[ki_MaxColumns];	// temporary list of each of the column's data. Explained why later
        														// this is cleaned up when the this function is over. 
        
        int i_TotalColumns = 0;									// index for the column data being added to the temp list
        
        
       
        listColumnData[i_TotalColumns++] =						// Save the Telemetry generated columns first in the proper order, notice increment  
        		String.format( "%.2f", tim_Time.get() );
        
        listColumnData[i_TotalColumns++] = 
        			String.format( "%.2f", RobotController.getBatteryVoltage() );

        String s_Mode = "";
        
        if (driverStation.isAutonomous() == true) 			s_Mode = "auton";		// this form is allowed as Java ignores whitespace between things
        else if (driverStation.isOperatorControl() == true) s_Mode = "teleop";		// we don't like to do it as it may not be clear and harder to debug.
        else if (driverStation.isDisabled() == true) 		s_Mode = "disable";		// added here for your viewing pleasure and a learning moment 
        else if (driverStation.isTest() == true) 		    s_Mode = "test";		// added here for your viewing pleasure and a learning moment 

        if( this.i_TotalRows == 0)								// first row
        	this.s_TelemType = s_Mode;	    					// need a string to hold this Mode
        	
        listColumnData[i_TotalColumns++] = s_Mode;				// add mode to the column data list 
        

        for(String columnName : listColumns){					// go through all the headers looking for their data in the HashMap or dictionary 

        	if( columnName == null )							// if we hit a header that had no value (null), end of the header list, break out of the loop  
        			break; 
        	
        	String sValue = "";									// Create a tmp sting to hold the column data. "" assumes this column does not have any data 

            if(dictColumnData.containsKey(columnName))			// use the columnName to see if there is any column data for this header
                sValue = dictColumnData.get(columnName);		// yes their is, save it to sValue
            	
            if (sValue == null)									// this is an error get out
            	break;
            
            listColumnData[i_TotalColumns++] = sValue;			// add the value to the list. The important point here is this is how the 
            													// actual data is saved in the same position as the header
            													// no matter when the column data was written to the class 
            
        }
        
        while( i_TotalColumns < listColumnData.length  )		// here we set all of the rest of the column data in this list to ""
            listColumnData[i_TotalColumns++] = "";				// if we did not do this then the word null would show up for all the extra columns
        														// this extra data is annoying but is the price we pay for speed. 
        
        listRows[this.i_TotalRows++] = String.join("\t", listColumnData);	// save this row to the row list 
        																	// This is why we use the array listColumnData. Using the join method to save to the 
        																	// list of rows is very, very fast and does not use any robot cycles. 
        																	// we still run at 50 cycles per second. Other methods we use in development
        																	// made us run at 20 cycles per second.
        
        this.dictColumnData.clear();										// now clear out the old column data for the next pass. 
            
    }

    
    /**
    * Called to tell telemetry to read its Preferences.
    * The available fields are...<br>
    * 1) Tele_FileName - Default: telemetry - Name of the file to be written. This will have a .xls extension added.
    * <p>                     
    * 2) Tele_FilePath - Default: /tmp - Location on the roboRio where 
    *                    the file is written. This is in /var/volitile/tmp.
    *                    This will be erased when powered off. So get it 
    *                    before you power off. The folder /home/lvuser
    *                    is writable and is not volitile. <b><u><i>Use at your own risk.</i></u></b>  
    * <p>                     
    * 3) Tele_TimestampFile - Default: false - Use to tell the system to add a current time stamp to the file. 
    * 					 This is applied just before the data is written. <br>
    * 					 Example:  telemetry.2015-12-14-13-25-23.xls
    */
    public void loadConfig(Config config){
    	
        this.sp_FileName = config.getString("Tele_FileName", "telemetry");
        this.sp_FilePath = config.getString("Tele_FilePath", "/tmp"); 
        this.bp_TimestampFile = config.getBoolean("Tele_TimestampFile", false);
        
    }
 
    
    /**
    * Call see what the file name will be. You can include on 
    * the SmartDashboard so you can see what the name will look like based upon
    * the preferences.<br> 
    * If you have the time stamp option enabled the time will 
    * be when the final file is written. 
    * 
    *  @return String of the full file name with .xls extension.  
    */
    public String getFileName(){										// build up the file name

    	if(sp_FileName.length() == 0 )
    		sp_FileName = "telemetry";
    		
    	String s_FullFileName = sp_FilePath + "/" + sp_FileName + "_" + this.s_TelemType;
    	
    	if(this.bp_TimestampFile==true)
    		s_FullFileName += "_" +  new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

    	s_FullFileName += ".xls";										// .xls makes it look like an Excel spread spread

    	return s_FullFileName;
    }

    
    /**
    * Called to show a few of the fields that can aid you in setup.<br> 
    * <b>Note: This only works while in disabledPeriodic mode.</b> 
    * <p>  
    * 1) Tele_FileName - Final File name. Note: Is timestamp is used the
    *                    timestamp will updated until you actually write 
    *                    the file.  
    * <p>
    * 2) Tele_DataSaved - a boolen that will show true when the spreadsheet
    * 					 is written. Once written you cannot write again 
    * 					 until the code is restarted.
    * <p> 
    * 					 Example:  /tmp/telemetry.2015-12-14-13-25-23.xls
    * <p>                      
    * @param	b_MinDisplay Ignored but kept for compatibility with
    *                      other LC functions with this name.    
    */
    public void outputToDashBoard(Boolean b_MinDisplay){

    	if(driverStation.isDisabled()){									// only acted upon and displayed in disabled mode to save cycles 
    		SmartDashboard.putString("Tele_FileName", getFileName() );
    	}
    }
    
    
    /**
    * Called to tell Telemetry to save the rows and columns
    * that are the spreadsheet data. Note: It is expected that this is called
    * while in disabledPeriodic mode during testing or at the 
    * end of a competition round.
    * <p>
    * The best way is to have 2 buttons that must be pressed at the same time
    * while in disabledPeriodic mode. Possibly one on the driver stick and the 
    * other on the operator stick. We do not want this to be triggered
    * accidently during a round.
    *<p> 
    * Example:
    * <pre>
    * public void disabledPeriodic() {
    * 
    *	  // bla..bla..bla
    *	
    *     if( inputs.b_SaveTelemetry == true )
    *         telem.saveSpreadSheet();						// once done you cannot save more data there. 
    * }
    *</pre>
    *                      
    */
    public void saveSpreadSheet(){
    	
    	if( this.i_TotalRows == 0)
    		return;
    	
    
        try {
			fileHandle = new FileWriter( getFileName() );

			String headerRow = "Timer\tBatt Volts\tMode\t";			// these are common fields LCTelemetry adds
			//String headerRow = "Timer\tMode\t";			// these are common fields LCTelemetry adds
			
            for (int i = 0; listColumns[i] != null; i++) {
            	headerRow += listColumns[i] + '\t';					// the \t inserts a tab character easily read as a column in excel. 
            }
            
        	fileHandle.write(headerRow + "\n");						// write the header. the \n is a new line indicating the end of a line or row in the sheet. 
			
        	
            for (int i = 0; listRows[i] != null; i++) {
            	fileHandle.write(listRows[i] + "\n");				// write each row of telemetry data. Teh \n is a new line character.   
            }
            
            fileHandle.close();						
            
		} catch (IOException e) {
			e.printStackTrace();
		}
 
        createNewList();
            
    }
    
}