
package frc.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class RobotLEDDisplay {

  private Spark motRevLEDSpark = null; 
  private HashMap< String, Double> mapLEDKVPairs = new HashMap< String, Double>();
  private String sLastLEDKey = "off";
  private Double dLastLEDSetting = .99;
  private String sLEDErrorKey = "error";        // default error setting
  private Double dLEDErrorValue = .69;          // default error setting
  private String sLEDState = "";


  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  public RobotLEDDisplay( int iRevLEDSparkPWMPort ) {
    motRevLEDSpark = new Spark(iRevLEDSparkPWMPort);
    motRevLEDSpark.set(dLastLEDSetting);

    mapLEDKVPairs.put("off", .99);
    mapLEDKVPairs.put(sLEDErrorKey, dLEDErrorValue);

  }

  public void addLEDMappedValue( String sKey, Double dValue ){

    if( isLEDKVPairOk(sKey, dValue) == true){
        mapLEDKVPairs.put(sKey,dValue);
        System.out.println("LEDSetValue:" + sKey + ", " + String.valueOf(dValue) );
    } 
  }

  public void importFromFile( String sPassedFilePath, String sPassedFileName ){

    System.out.println("LEDImportValue: Clearing the HashMap");

    mapLEDKVPairs.clear();
    mapLEDKVPairs.put("off", .99);              // defaults
    mapLEDKVPairs.put(sLEDErrorKey, dLEDErrorValue);              // defaults

    System.out.println("LEDImportValue: Setting off to .99");

    String sFileName = sPassedFilePath + File.separator + sPassedFileName;

    BufferedReader bufr = null;
	  
	try {
		FileReader fr = new FileReader(sFileName);
        bufr = new BufferedReader(fr);
	        
		String line = bufr.readLine();                                          // get first line
		while(line != null){
            String[] sKVPair = line.split(",");		
            String sKey = sKVPair[0].toLowerCase(); 							// break up line into Key and Value Strings
            Double dValue = Double.valueOf(sKVPair[1]);

            if( isLEDKVPairOk(sKey, dValue) == true){
                mapLEDKVPairs.put(sKey, dValue);		    // save to map as String, Double
                System.out.println("LEDImportValue:" + sKey + ", " + sKVPair[1]);
                line = bufr.readLine();											// get next line
            }
        }

		bufr.close();
	
    }
    catch(IOException e) {
      e.printStackTrace();
    }

  }

  public void setLEDErrorValue( String sKey, Double dValue){

    if( isLEDKVPairOk("error", dValue) == true){
        setLED("error", dValue);
        dLEDErrorValue = dValue;        
    }

  }

  public void setLED( String sKey){

    sKey = sKey.toLowerCase();

    if( sKey.equals(sLastLEDKey) == true){
        motRevLEDSpark.set(dLastLEDSetting);
        return;
    }

    if( mapLEDKVPairs.containsKey(sKey) == true ){   // test to see if this key is in our map
        Double dValue = mapLEDKVPairs.get(sKey);     
        motRevLEDSpark.set(dValue);
        sLastLEDKey = sKey;
        dLastLEDSetting = dValue;
    } else {
        sLEDState = "** WARN: LED Key Not found!!!:  Setting to ugly error color!!!!!!";
        motRevLEDSpark.set(dLEDErrorValue);
        sLastLEDKey = sKey;
        dLastLEDSetting = dLEDErrorValue;
    }

  }

  private boolean isLEDKVPairOk(String sKey, Double dValue){

    if( dValue > 1.0 || dValue < -.99 ){
      sLEDState = "** WARN:  LED Value Ignored!!!: " + 
                                "   Key=" + sKey.toLowerCase() + 
                                "   Value=" + String.valueOf(dValue) + 
                                "   Is out of range -.99 to .99!!!!";
        return false;
    }

    sLEDState = "";
    return true;

  }


  public void setLED( String sKey, Double dValue){
    sKey = sKey.toLowerCase();

    if( sKey.equals(sLastLEDKey) == true && dValue == dLastLEDSetting ){
        motRevLEDSpark.set(dLastLEDSetting);
        sLEDState = "";
        return;
    }

    if( isLEDKVPairOk(sKey, dValue) == true){
            mapLEDKVPairs.put(sKey,dValue);
            sLastLEDKey = sKey;
            dLastLEDSetting = dValue;
    } else {
        sLEDState = "*** WARN: LEDAddedInvalidKey: " + 
        "   Key=" + sKey + 
        "   Value=" + String.valueOf(dValue) + "   Setting to Error color!!!!!!";

        motRevLEDSpark.set(dLEDErrorValue);
        sLastLEDKey = sKey;
        dLastLEDSetting = dLEDErrorValue;
    }        

    motRevLEDSpark.set(dLastLEDSetting);

  }

  public void addTelemetryHeaders(LCTelemetry telem ){
  
    telem.addColumn("LED Key");
    telem.addColumn("LED Value");
    telem.addColumn("LED State");

  }

  public void writeTelemetryValues(LCTelemetry telem ){
    telem.saveString("LED Key", this.sLastLEDKey );
    telem.saveDouble("LED Value", this.dLastLEDSetting );
    telem.saveString("LED State", sLEDState );
  }

  public void testSetting( Double dValue ){

    double dLEDSetting = dValue;   // get value from input
    int iLEDSetting = (int) (dLEDSetting * 100);  // multiply by 100                   .99 becomes 99 int
    iLEDSetting  = iLEDSetting / 2;               // divid by 2 to drop force even.     99 becomes 49
    iLEDSetting = iLEDSetting * 2;                // mult by 2 to but back figh range.  49 becomes 98
    dLEDSetting = (double) iLEDSetting / 100.0;   // divide by 100.0 to make decimal    98 becomes .98
    dLEDSetting += .01;                           // add .01 to make odd for BLINKIN    .98 becomes .99

    setLED("test", dLEDSetting);

  }

  public void updateDashboard(){

    SmartDashboard.putString("LED Key", sLastLEDKey);
    SmartDashboard.putNumber("LED Value", dLastLEDSetting);
    SmartDashboard.putString("LED State", sLEDState);

  }
}