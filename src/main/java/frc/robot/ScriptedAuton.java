/**------------------------------------------------------------------------------
 * ScriptedAuton
 * This allows the user to script their autonimous moves.
 * This has to be updated for each year's code.
 * It relies on booleans in the inputs class that signal an action to be taken
 * line as in pressing a button (boolean) or requesting a position or speed (double).
 * 
 * The file is a simple text file, made up of statements. The statement can be
 * tabbed in for readability. Right now each action requires a description.
 * You can include empty lines and comments starting with # for readability. 
 *  
 * Example:
 * # basic auton description
 * auton 1
 *    step 1
 *        bearing, -90  ,  set the robot gyro bearing to -90 degrees (left).
 *        spin   ,    0  ,  spin up the shooter wheel to get ready to shoot. 
 *        timer  ,  3.5 ,  execte this step for 3.5 seconds
 *        power  ,  .75 ,  run at .75 power
 * 
 *    step 2
 *        bearing, 0     ,  turn back to target 0.0 degrees
 *        spin   ,    0  ,  spin up the shooter wheel to get ready to shoot. 
 *        timer  ,  1.0. ,  execute this step for 3.5 seconds
 *        power  ,  .75 ,  run at .75 power
 * 
 *    step 3
 *        shoot, 0     ,  start shooting process
 *
 * # next auton
 * auton 2
 *    step 1
 * 
 ***************************************************************************     
 * Key words
 * auton #  - defines the start of a new autonomous program.
 * step #   - The step in that auton program. 
 * 
 * The actions, defined in AutonStep class, for the 2020 season are below.
 * bearing   -- set the gyro bearing for the robot
 * distance  -- drive a certain distance as defined in code using encoder
 * high      -- shift drive to high gear
 * ingest    -- start / stop ingesting 0 = stop, non 0 = start
 * power     -- drive forward (+), backward (-) using gyro keep straight.
 * timer     -- run the step for a certain amount of time 
 * target    -- press and hold the target trigger on driver's control
 * shoot     -- press and hold the shoot trigger on driver's control
 * spin      -- pre-spin up the shooter so it is moving when you get there to shoot.   
/----------------------------------------------------------------------------**/

package frc.robot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

//import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class ScriptedAuton{

  public String sScriptFileName = null;

  private Integer iAutonNumber = 0;
  private Integer iStepNumber = 0;
  private Integer iNextStepNumber = 0;
  private Integer iLastStepNumber = 0;
  private Integer iActionNumber = 0;               //This is an Action within a step

  boolean bStepIsSetup = false;
  boolean bStepIsComplete = false;

  private String sAction = "";
  private String sValue = "";
  private String sDesc = "";

  public int iLoadErrors = 0;

  LCTelemetry telem = null;
  Inputs inputs = null;
  RobotBase robotbase = null;
  Shooter shooter = null;
  Config config = null;
  RampPower rampDrivePower = null;
    
  Timer timStepTimer = null;
  boolean bAutonComplete = false;
  public int    iSelectedAutonId = 0;
  public String sSelectedAutonDesc = "";
  String sWhatCompleted = "";
  public String sStepDesc = "";
  

  public TreeMap<String,String>   mapDescriptions  = new TreeMap<String,String>();         // list of descriptions
  public TreeMap<String,AutonStep> mapValues       = new TreeMap<String,AutonStep>();     // map of auton#|step#| actions | optional a desc

  public  ScriptedAuton(String sPassedFilePath, String sPassedFileName, 
            Config mPassedConfig,
            LCTelemetry mPassedTelem, Inputs mPassedInputs, 
            RobotBase mPassedRobotBase, Shooter mPassedShooter){

    this.telem      = mPassedTelem;               // pass in the telem class
    this.inputs     = mPassedInputs;
    this.robotbase  = mPassedRobotBase;
    this.shooter    = mPassedShooter; 
    this.config     = mPassedConfig; 

    rampDrivePower = new RampPower(.10,           // minpower
                                    1.0,          // maxpower
                                    .20,          // ramp up pct
                                    .85,          // ramp down pct
                                    .005);        // proportional ramp down multiplier

    rampDrivePower.setStopOnArrival(true, .005);

    sScriptFileName = sPassedFilePath + "/" + sPassedFileName;
    loadScript();
    dumpMap();
    timStepTimer = new Timer();
    timStepTimer.start();

    iSelectedAutonId = config.getInt("scriptedauton.iSelectedAutonId", 0);
    sSelectedAutonDesc = getDescription(iSelectedAutonId);
    if( sSelectedAutonDesc.startsWith("*") == true  ){
      sSelectedAutonDesc = "disabled auton [" + String.valueOf("iSelectedAutonId") +"] loaded from config, reset to 0.";
      iSelectedAutonId = 0;
    }

 }

  public void loadScript(){

    mapValues.clear();
    mapDescriptions.clear();

    String sVerb = "";
    BufferedReader bufr = null;

    System.out.println("ScriptedAuton: Loading the script");

    try {
      FileReader fr = new FileReader(this.sScriptFileName);
      bufr = new BufferedReader(fr);
            
      while(true){
        String sLine = bufr.readLine();
        if( sLine == null){               // nothing more from the file
          break;                          // get out of while loop
        }

        sLine = sLine.strip();

        if( sLine.startsWith("#") == true){
          continue;
        } else if(sLine.length() == 0){
          continue;
        }

        System.out.println(sLine);
        
        //int iWordIndex = 0;
        //String[] sWords = new String[100];
        //String[] sTemp = sLine.trim().strip().replaceAll("\\s"," ").split(" ");
        //for(int i = 0; i< sTemp.length; i++ ){
        //  if( sTemp[i].equals(" ") == false){
        //    System.out.println( "  " + String.valueOf(iWordIndex) + ": " + sTemp[i] );
        //    sWords[iWordIndex++] = sTemp[i];
        // }
        //}

        /**
         * We are breaking up each line we read into words seperated by whitespace.
         * Whitespace is anything between words or numbers that appears blank on the screen.
         * This can be spaces, tabs(\t), carriage returns(\r), or newlines (\n).
         * '\s' is escape for single whitespace. Unfortunately \ is also the escape symbol. 
         * When used in a string declaration in code, if we use "\s" it means escape on lower case s. 
         * Which means nothing. But if we use "\\s" then the "\\" converts to \. When we now add 
         * that to 's' we get the whitespace nomenclature. If we use "\\s+", it means multiple white space.
         * Here we use \\s+ becasue we do not know how many white spaces are between words.   
        */
        String[] sWords = sLine.trim().split("\\s+");       //trim removes the white space around the line before we split. 
        //for( int i = 0; i<sWords.length; i++){
        //  System.out.println( "  " + String.valueOf(i) + ": " + sWords[i] );
        //}

        sVerb = sWords[0].strip().toLowerCase();

		    if (sVerb.startsWith("auton") == true){							    // example:  auton 3 many following description words

          iAutonNumber = Integer.valueOf(sWords[1]);            // reset the others params on start of each new auton
          iStepNumber = 0;
          iActionNumber = 0;
          iLastStepNumber = 0;

          sDesc = buildDescription(sWords);                     // auton REQUIRES a description
          if(sDesc.isEmpty() ){
            sDesc = "no description provided!!!!";              // no description so provide one
          }
          saveDescription(sDesc, iAutonNumber, 0, 0);           

        } else if (sVerb.startsWith("step") == true){						// example step 2  bla bla saved as description for the step.

          iStepNumber = Integer.valueOf(sWords[1]);
          sDesc = buildDescription(sWords);

          if(sDesc != "" ){
            saveDescription(sDesc, iAutonNumber, iStepNumber, 0); 
          }

        } else {                                                // everything below here is an action Action value strings after it.
          iActionNumber += 1;

          try{
            sValue = sWords[1];
          } catch( Exception e) {
            sValue = "0";
          }

          sDesc = buildDescription(sWords);
          System.out.println( "sVerb:" + sVerb + " | " +
                              "sValue:" + sValue + " | " + 
                              "sDesc:"  + sDesc );

          saveAction(sVerb, sValue, sDesc);
        }
      }
    } catch(IOException e) {
      e.printStackTrace();
    }

    try{
      bufr.close();
    } catch(IOException e) {
      e.printStackTrace();
    }

    sSelectedAutonDesc = getDescription(iSelectedAutonId);
    if(sSelectedAutonDesc.isEmpty() == true ){
      iSelectedAutonId = 0;
      sSelectedAutonDesc = getDescription(iSelectedAutonId);
    }

    if(iLoadErrors > 0){
      System.err.println("****");
      System.err.println("****");
      System.err.println("****");
      System.err.println("Warning: There were [" + String.valueOf(iLoadErrors) + "] in the Auton file." +
                            " [" + this.sScriptFileName  + "].");
      System.err.println("Warning: Your auton may not work as planned.");
      System.err.println("****");
      System.err.println("****");
      System.err.println("****");
    }
    reset();                  // all done reset
  }

  /**
   * Create a description from the words after the value. This will starting at words[2].
   * This will ignore the forst 2 words which are the verb and the value.
   * 
   * @param words - array of words from the line.
   * @return Combined description
   */

  String buildDescription( String[] words){

    String sTemp = "";

    if( words.length > 2 ){
      try { 
          for(int iIndex=2; words[iIndex] != null; iIndex++ ){
            sTemp += words[iIndex] + " "; 
          }
      } catch( Exception e ){
        // do nothing
      }
    }

    return sTemp.strip();
  }


  String buildKey( Integer iAutonNumber, Integer iStepNumber, Integer iActionNumber ){

    if( iAutonNumber > 0  && iStepNumber > 0 && iActionNumber > 0){
      return String.format("%02d|%02d|%02d", iAutonNumber, iStepNumber,iActionNumber); 
    } else if( iAutonNumber > 0  && iStepNumber > 0 ){
      return String.format("%02d|%02d", iAutonNumber, iStepNumber,iActionNumber); 
    } else {
      return String.format("%02d", iAutonNumber); 
    }

  }

  void saveDescription(String sDescription, Integer iAutonNumber, Integer iStepNumber, Integer iActionNumber){
    String sKey = buildKey(iAutonNumber, iStepNumber, iActionNumber);
    System.out.println("SaveDesc: Key:" + sKey + "   Desc" + sDescription);
    mapDescriptions.put(sKey, sDescription);								// save to map as key, description
  }

  /**
   * The is an example of over loading. Here we have the same method called with differet parameters. 
   * Java sees what we pass and picks the correct one.  
   */
  public String getDescription(Integer iAutonNumber, Integer iStepNumber, Integer iActionNumber){
    if( iAutonNumber == 0){
      return "no auton selected";
    }

    String sKey = buildKey(iAutonNumber, iStepNumber, iActionNumber);   // get the appropriate key
    return mapDescriptions.getOrDefault(sKey, "");								      // return the looked up desc or "" if none found.

  }
  // overload of above method to allow user to get step description without including action number
  public String getDescription(Integer iAutonNumber, Integer iStepNumber){
    return getDescription(iAutonNumber, iStepNumber, 0);					// call to original method with all 3 parameters. 0 indicates ignore that one
  }

  // overload of above method to allow user to get step description without including step number and action number
  public String getDescription(Integer iAutonNumber){
    return getDescription(iAutonNumber, 0, 0);								    // call to original method with all 3 parameters. 0 indicates ignore that one
  }

  // End of over loaded method

  void saveAction(String sAction, String sValue, String sDesc){

    if( iStepNumber != iLastStepNumber){
      iActionNumber = 1;
    }
  
    iLastStepNumber = iStepNumber;


    AutonStep mAutonStep = new AutonStep(
      iAutonNumber,   		// Auton Number
      iStepNumber,   		// Auton Step
      iActionNumber,   		// Action Step
      sAction.toLowerCase(),// Action
      sValue, 				// Param
      sDesc);				// description

    if(mAutonStep.bIsValid == false){
      iLoadErrors++;
    }

    String sKey = buildKey(iAutonNumber, iStepNumber, iActionNumber); 

    mapValues.put(sKey, mAutonStep);								// save to map as Distance, TargetData class

  }

  public void loadConfig(Config config){
    iSelectedAutonId = config.getInt("scriptedauton.iSelectedAutonId", 0);
  }


  public void selectAuton(){

    sDesc = "";
    while( true ){
      iSelectedAutonId += 1;
      sSelectedAutonDesc = getDescription(iSelectedAutonId);

      System.out.println("SA Id:" + String.valueOf(iSelectedAutonId) + "  Desc:" + sSelectedAutonDesc ); 

      if( sSelectedAutonDesc.isEmpty() == true){   // no description means no auton for that number
        iSelectedAutonId = 0;
        sSelectedAutonDesc = getDescription(0); 
        break;
      }

      if(sSelectedAutonDesc.startsWith("*") == true){   // disabled
        continue;
      } 

      break;
    }

  }

  public void dumpMap(){

  // using for-each loop for iteration over TreeMap.entrySet() 
    System.out.println( "Dump of: mapValues"); 
    for (Map.Entry<String, AutonStep> entry : mapValues.entrySet()){
          String sKey = entry.getKey();         // return the key 
          AutonStep autonStep = entry.getValue();   // get the associated AutonStep class value
          System.out.println( sKey + " : " + autonStep.getCSVLine() ); 
    } 
    System.out.println( "*** End"); 

  }

  public void dumpDecriptions(){
    // using for-each loop for iteration over TreeMap.entrySet() 
    System.out.println( "Dump of: mapDescriptions"); 
    for (Map.Entry<String, String> entry : mapDescriptions.entrySet()){
        String sKey = entry.getKey();         // return the key 
        String sDesc = entry.getValue();   // get the associated AutonStep class value
        System.out.println( sKey + " : " + sDesc ); 
    } 
    System.out.println( "*** End"); 
  
  }
  
  
  public void reset(){
    iStepNumber = 0;
    iActionNumber = 0;
    iLastStepNumber = -1;                             // must not be the same as iStepNumber
    iNextStepNumber = 1;                              // we want it to got to this once it is set up. 
    sStepDesc = "";
    sWhatCompleted = "";
  }
  
  public void execute(){

    sWhatCompleted = "";                                  // clear this so we only get one at the end of step.

    if( this.iStepNumber == 0 ){                          // will force us to reset and go to step 1
      reset();
    }

    if( iStepNumber != iNextStepNumber){
      iStepNumber = iNextStepNumber;
    }
    
    if( this.iStepNumber != this.iLastStepNumber){				// set conditions for a new step to process.
      bStepIsSetup = false;                               // use once in the step to set conditions if necessary
      bStepIsComplete = false;
      timStepTimer.reset();
      robotbase.SaveEncoderPosition();                    // allows us to calculate distance from last known position
      sStepDesc = getDescription(iSelectedAutonId,iStepNumber);   // get the description of this step
    }

    this.iLastStepNumber = this.iStepNumber;

    /**
     * Here we want to be sure that we go through the actions at leas once
     * so if this is false we will not test results
     */ 
    if( bStepIsSetup == true ){                           // force us to go through at least 1 pass
      testCompletion(iSelectedAutonId);                   // test for any actions to be complete

      if(bAutonComplete == true){                         // The Auton is done return doing nothing else. 
        //stopAll();
        return;
      }
        
    }
	
		if(this.bStepIsComplete == false){
      setAutonActions(iSelectedAutonId);
    }

		if(this.bStepIsComplete == true){
			this.iNextStepNumber = this.iStepNumber + 1;
    }
	
  	this.bStepIsSetup = true;                   // force at least 1 pass

  }  

  public void stopAll(){
    inputs.dDriverPower = 0.0;
    inputs.dDriverTurn = 0.0;
    inputs.bIntakeIn = false;
    inputs.bIntakeOut = false;
    inputs.dRequestedBearing = 0.0;
    inputs.bShooterLaunch = false;
    inputs.bSpinUpShooter = false;
    inputs.bTargetting = false;
    inputs.bTeainatorUp = true;
  }

  private void setAutonActions(Integer iSelectedAutonId){

    //String sStartKey = String.format( "%02d|%02d|01", iSelectedAutonId, this.iStepNumber);
    String sStartKey = buildKey(iSelectedAutonId, this.iStepNumber, 1);   // start at current step, action 1 

    // here we iterate through only the actions for this step.
    for (Map.Entry<String, AutonStep> entry : mapValues.tailMap(sStartKey).entrySet()) {
          String sKey = entry.getKey();                     // get the key which we may not need here 
          AutonStep autonStep = entry.getValue();           // get the associated AutonStep class value
      
        // we know we are done here when autonStep.iAutonNumber or 
        // the autonStep.iAutonStep on the action we just read from the map 
        // is not the current one being processed 
        if( autonStep.iAutonNumber != iSelectedAutonId ||              // not the current Auton 
                        autonStep.iAutonStep != this.iStepNumber){  // not the current step
            break;
        }

        if( autonStep.sAction.equals("bearing")){           // change the gyro bearing
          inputs.dRequestedBearing = autonStep.dValue;      //    set a Requested bearing for the gyro.

        } else if( autonStep.sAction.equals("distance")){   // drive a specific encoder distance

            if( this.bStepIsSetup == false){                // do this at beginning of the auton step
              rampDrivePower.setNewDistance(
                              robotbase.dEncoderPosition,   // encoder where we are starting 
                                        autonStep.dValue    // distance we want to go
                                                );   
            }

            inputs.dTargetDistanceUsingEncoder = 
                                          autonStep.dValue; // tell downstream this is our target encoder distance 
            //inputs.bRampPower = true;                       // tell downstream we want to ramp the power
            if( inputs.dDriverPower != 0.0){
              inputs.dDriverPower = rampDrivePower.calcPower(inputs.dDriverPower,robotbase.dEncoderPosition);  // current position 
            } else {
              inputs.dDriverPower = rampDrivePower.calcPower(robotbase.dEncoderPosition);  // current position 
            }
                                                          
            inputs.iGyroRequest = Gyro.kGyro_Correct;       //    tell gyro to correct on non encoder side

        } else if( autonStep.sAction.equals("ingest")){	    // test conditions for ingest
          if( autonStep.dValue == 0.0) {                    //    0.0 indicates to turn it off. 
            inputs.bTeainatorUp = true;                     //        hold the button to lift it up
          } else {                                          //    not 0.0 drop it and turn on ingest.
            inputs.bTeainatorDown = true;                   //        hold the button put it down
            inputs.bIntakeIn = true;                        //        hold the button to run it to intake
          }

        } else if( autonStep.sAction.equals("power")){      // update the variable for driver power
          inputs.dDriverPower = autonStep.dValue;           //    set the driver power


        } else if(autonStep.sAction.equals("shooting")) {   // start the shooter start machine
            inputs.bShooterLaunch = true;                   //    hold the trigger 
            inputs.bTargetting = true;                      //    keep holding this trigger too  
  
        //} else if( autonStep.sAction.equals("spin")){	    // if this is not in the step injest remains unchanged
        //  inputs.bSpinUpShooter = true;                   //    spin the shooter to a predetermined value

        } else if( autonStep.sAction.equals("settle")){	    // if this is not in the step injest remains unchanged
          inputs.dDriverPower = 0.0;              
          inputs.dDriverTurn = 0.0;           

        } else if(autonStep.sAction.equals("targetting")) { // start the targetting start machine
          inputs.bTargetting = true;                        //    hold this trigger  
          if( autonStep.dValue == 0.0 ){                    //    0.0, indicates close targets
            inputs.bCloseTargets = true;                    
            inputs.bFarTargets = false;
          } else {                                          //    not 0.0, indicates far targets.
            inputs.bCloseTargets = false;
            inputs.bFarTargets = true;
          }
  
        } else if( autonStep.sAction.equals("timer")){      // do nothing, will be used later to test we are done.
                                                            // remember timer is reset when we change steps.

        } else if( autonStep.sAction.equals("turnto")){     // spin robot to a new bearing
          inputs.dRequestedBearing = autonStep.dValue;      //    set a Requested bearing for the gyro.
          inputs.iGyroRequest = Gyro.kGyro_TurnTo;          //    tell gyro to spin to location
        }
    }
  }

  private void testCompletion(Integer iSelectedAutonId){

    sWhatCompleted = "";
    String sStartKey = buildKey(iSelectedAutonId, 
                                    this.iStepNumber, 1);   // start at current auton id, current step, action 1 

    for (Map.Entry<String, AutonStep> entry :               // iterate through each action in the current auton and step. 
                  mapValues.tailMap(sStartKey).entrySet()) {
          //String sKey = entry.getKey();                   // return the key, may not need this. 
          AutonStep autonStep = entry.getValue();           // get the associated AutonStep class value

                                                            // make sure we are in the same auton and step.
      if( autonStep.iAutonNumber != iSelectedAutonId ||     // test to see if we are still in the Selected Auton 
          autonStep.iAutonStep != this.iStepNumber){        // or in the current step from the Map. 
          break;                                            // get out of the loop if either is not current.
      }
      
      //////////////////////////////////////////////////////// test the actions to see if any are complete. 
      if( autonStep.sAction.equals("distance")){            // have we gone far enough 
          if( Math.abs(robotbase.dEncoderDistance) > 
                                      autonStep.dValue ){   // is encoder distance > expected
            this.bStepIsComplete = true; 
            sWhatCompleted = "distance (encoder)";
          }  
      } else if(autonStep.sAction.equals("shooting")) {     // check the fire sequence state machine
        if(shooter.bFireSequenceIsComplete == true){        // are we done?
          this.bStepIsComplete = true;             
          sWhatCompleted = "fire seq complete";
        }

      } else if( autonStep.sAction.equals("settle")){       // let robot settle after a move
          if(timStepTimer.get() > autonStep.dValue){        // have we waited long enough? 
            this.bStepIsComplete = true;
            sWhatCompleted = "time up";
          }

      } else if(autonStep.sAction.equals("stop")) {         // we can include a stop request ???
        if(timStepTimer.get() > autonStep.dValue ){
          this.bStepIsComplete = true;             
        }

      } else if(autonStep.sAction.equals("targetting")) {   // we are holding the targetting button

        if( robotbase.bBaseIsOnTarget == true &&            // is robot base aligned to X on camera image?
            shooter.bShooterOnTarget == true  ){            // is camera aligned to Y on camera image?
            this.bStepIsComplete = true;             
            sWhatCompleted = "RB and SH on target";
          }
        
      } else if( autonStep.sAction.equals("turnto")){       // we asked robot to spin to new gyro bearing
          if(robotbase.bIsOnGyroBearing == true){           // have we spun enough?
            this.bStepIsComplete = true;                    // are we on the right bearing
            sWhatCompleted = "Gyro on bearing";
          }

      } else if( autonStep.sAction.equals("timer")){        // we asked to check the step timer
          if(timStepTimer.get() > autonStep.dValue){        // are we past the time? 
            this.bStepIsComplete = true;
            sWhatCompleted = "timer up";
          }

      }

    }
  }


  public void addTelemetryHeaders(LCTelemetry telem ){
    telem.addColumn("SA Auton Id"); 
    telem.addColumn("SA Auton Desc"); 
		telem.addColumn("SA Step Number"); 
		telem.addColumn("SA Step Desc"); 
		telem.addColumn("SA Step Is Complete");
		telem.addColumn("SA What Completed");
    telem.addColumn("SA Auton Is Complete");
    telem.addColumn("SA Step Timer");
    telem.addColumn("SA Ramp Power");
    telem.addColumn("SA Gyro Bearing");
    telem.addColumn("SA Gyro On Bearing");
    telem.addColumn("SA Enc Distance");
    telem.addColumn("SA RB On Target");
    telem.addColumn("SA SH On Target");
    telem.addColumn("SA SH Fire Seq Done");
  }

  public void writeTelemetryValues(LCTelemetry telem ){
    telem.saveInteger("SA Auton Id", this.iSelectedAutonId);
    telem.saveString("SA Auton Desc", this.sSelectedAutonDesc);
		telem.saveInteger("SA Step Number", this.iStepNumber); 
    telem.saveString("SA Step Desc", this.sStepDesc );
    telem.saveDouble("SA Step Timer", this.timStepTimer.get() );
    telem.saveTrueBoolean("SA Ramp Power", inputs.bRampPower);
    telem.saveDouble("SA Gyro Bearing", robotbase.dRelativeGyroBearing);
    telem.saveTrueBoolean("SA Gyro On Bearing", robotbase.bIsOnGyroBearing);
    telem.saveDouble("SA Enc Distance", robotbase.dEncoderDistance);
    telem.saveTrueBoolean("SA RB On Target", robotbase.bBaseIsOnTarget);
    telem.saveTrueBoolean("SA SH On Target", shooter.bShooterOnTarget);
    telem.saveTrueBoolean("SA SH Fire Seq Done", shooter.bFireSequenceIsComplete);
    telem.saveTrueBoolean("SA Auton Is Complete", this.bAutonComplete);
		telem.saveTrueBoolean("SA Step Is Complete", this.bStepIsComplete);
		telem.saveString("SA What Completed", sWhatCompleted);
	}

  public void outputToDashboard(boolean b_MinDisplay)  {
    SmartDashboard.putString("SA Auton Id", String.valueOf(iSelectedAutonId));
    SmartDashboard.putString("SA Auton Desc", sSelectedAutonDesc);
		SmartDashboard.putNumber("SA I Step Number",this.iStepNumber);
		SmartDashboard.putNumber("SA I Next Step",this.iNextStepNumber);
    SmartDashboard.putBoolean("SA I Auton Comp",this.bAutonComplete);
    SmartDashboard.putNumber("SA I Step Timer",this.timStepTimer.get() );

    if ( b_MinDisplay == false ) return;
	
		
	}



}




