/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.cscore.HttpCamera;
import edu.wpi.first.wpilibj.RobotController;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {

  Inputs inputs = null;
  Shooter shooter = null;  
	RobotBase robotbase = null;
	LCTelemetry telem = null;
  Config config = null;
  Limelight limelight = null;
  public final String LimelightHostname = "limelight";   // Limelight http camera feeds
  public HttpCamera limelightFeed;
  ScriptedAuton scriptedauton = null; 
  int iSelectedAuton = 0;

  private String sTestProcess = "none";
  boolean bTestIsDone = false;
  boolean bTestIsSetup = false;
  RampPower testRampPower = null; 
  ApplyPower testApplyPower = null;


  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {

    config        = new Config("/home/lvuser","WPConfig_2020.cfg");  //init and open the config file
    telem         = new LCTelemetry("/home/lvuser","telemetry");	   // no extension needed
    inputs        = new Inputs();			
    limelight     = new Limelight("limelight");
		limelightFeed = new HttpCamera(LimelightHostname, "http://limelight.local:5800/stream.mjpg");
    shooter       = new Shooter(config, inputs);	// pass the config file here so that it has the configs to st up the shooter		
    robotbase     = new RobotBase(config, inputs, limelight);
    scriptedauton = new ScriptedAuton("/home/lvuser", "AutonScript.txt", config, telem, inputs, robotbase, shooter); 
    
    // add the telemetry fields for all parts
    inputs.addTelemetryHeaders( telem );
    shooter.addTelemetryHeaders( telem );
    robotbase.addTelemetryHeaders( telem );
    limelight.addTelemetryHeaders(telem);
    scriptedauton.addTelemetryHeaders(telem);
    
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
   @Override
  public void robotPeriodic() {
  }

  @Override
    public void disabledInit() {
      telem.saveSpreadSheet();            // this an be called repeatly. One spreadheet is written, it is cleared. 

      //config.load();
      //shooter.reloadTables();
      //robotbase.loadConfig();
      //robotbase.SetDevModes();

    }

  @Override
    public void disabledPeriodic() {
      inputs.readValues();

      if( inputs.bSelectAuton == true ) {             // during disabled mode before auton
        scriptedauton.selectAuton();                  // allow operator to toggle through auton options
      }                                               // options are visable in the scriptauton dashboard outputs


      inputs.outputToDashboard(false);
      limelight.outputToDashboard(false);
      shooter.outputToDashboard(false);
      robotbase.outputToDashboard(false, inputs);
      scriptedauton.outputToDashboard(false);
  
    }



  
  /**
   * This function is called at the start of autonomous.
   */
  @Override
  public void autonomousInit() {
    robotbase.gyro.zeroGyroBearing();
    shooter.restart();                              // restart our timers fro shooting and targetting
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {

    inputs.readValues();

    scriptedauton.execute();

    limelight.update(inputs);
    shooter.update(inputs,config,limelight);
    robotbase.update();
    
    inputs.outputToDashboard(false);
    limelight.outputToDashboard(false);
    shooter.outputToDashboard(false);
    robotbase.outputToDashboard(false, inputs);
    scriptedauton.outputToDashboard(false);
    
    inputs.writeTelemetryValues(telem);				// order does not matter
    limelight.writeTelemetryValues(telem);
    shooter.writeTelemetryValues(telem, inputs);
    robotbase.writeTelemetryValues(telem, inputs);
    scriptedauton.writeTelemetryValues(telem);
    
    telem.writeRow();					

  }

  /**
   * This function is called at the start of operator control.
   */
  @Override
  public void teleopInit(){
    config.load();              // durign testing reload the config file to be sure we got updates
    //shooter.reloadTables();
    robotbase.loadConfig();
    robotbase.gyro.zeroGyroBearing();
    robotbase.SetDevModes();
    shooter.loadConfig(config);
    scriptedauton.loadScript();
    scriptedauton.dumpMap();
    scriptedauton.dumpDecriptions();
    
    shooter.restart();                              // restart our timers fro shooting and targetting
    
    System.out.println("***** Teleop Init complete ********");
  
  }

  /**
   * This function is called periodically during operator control.
   * This is called 50 times per second or every 20ms.
   */
  @Override
  public void teleopPeriodic() {
    inputs.readValues();

    if( inputs.bRunAuton == true){        // allows us to test auton with beign in autom mode. 
      scriptedauton.execute();
    }

    limelight.update(inputs);
    shooter.update(inputs,config,limelight);
    robotbase.update();
    
    inputs.outputToDashboard(false);
    limelight.outputToDashboard(false);
    shooter.outputToDashboard(false);
    robotbase.outputToDashboard(false, inputs);
    scriptedauton.outputToDashboard(false);
    
    inputs.writeTelemetryValues(telem);				// order does not matter
    limelight.writeTelemetryValues(telem);
    shooter.writeTelemetryValues(telem, inputs);
    robotbase.writeTelemetryValues(telem, inputs);
    scriptedauton.writeTelemetryValues(telem);
    
    telem.writeRow();					
  }


  @Override
  public void testInit(){

    config.load();
    String sTemp = config.getString("robot.sTestProcess", "none");
    sTestProcess = sTemp.strip().toLowerCase().trim();
    bTestIsSetup = false;
    

    if( sTestProcess.compareTo("ramppower") == 0 ){
      robotbase.SaveEncoderPosition();
      System.out.println("testInit: Encoder Position ticks = " + String.valueOf(robotbase.getEncoderPosition()) );
      testApplyPower = new ApplyPower();
      System.out.println("testInit: Encoder Position inchs= " + 
              String.valueOf(testApplyPower.ticksToInches(robotbase.getEncoderPosition())) );
      

      testRampPower = new RampPower( .1,
              config.getDouble("testRampPower.dMaxPower", 1.0), 
              config.getDouble("testRampPower.dRampUpPct", .1), 
              config.getDouble("testRampPower.dRampDNPct", .6), 
              config.getDouble("testRampPower.dRampProp", .05));
      testRampPower.setNewDistance(    testApplyPower.ticksToInches(robotbase.getEncoderPosition())  , 
      config.getDouble("testRampPower.dDistance", 35.25));
      //testRampPower.setNewDistance(    robotbase.getEncoderPosition()  , testApplyPower.inchesToTicks(35.25));
      testRampPower.setDebug(true);
      testRampPower.setStopOnArrival( true, .01);
    }

    System.out.println("testInit: Process [" + sTestProcess + "] is initialized." );

  }


  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {

    if( bTestIsDone == true){
      return;
    }

    inputs.readValues();
    shooter.update(inputs, config, limelight);

    if( sTestProcess.compareTo("hood") == 0 ){

      // Shooter Hood position testing
      if( Math.abs(inputs.dShooterHoodPower) < .1){
        inputs.dShooterHoodPower = 0.0;
      }
      double temp = inputs.dShooterHoodPower;

      inputs.dShooterHoodPower = temp * Math.abs(temp*temp*temp);
      shooter.motShooterHood.set(inputs.dShooterHoodPower );
      System.out.println("Inputs Shooter Hood Power:" + String.valueOf(inputs.dShooterHoodPower));
      shooter.outputToDashboard(false);

    } else if( sTestProcess.compareTo("cameraclose") == 0 ){
      shooter.svoCamera.set( shooter.dCamera_CloseTargets);

    } else if( sTestProcess.compareTo("clearepc") == 0 ){

      //robotbase.mCompressor.enabled();                  // using compressor power for EPC sensor
      //robotbase.mCompressor.setClosedLoopControl(true);

      System.out.println("EPC: [" + String.valueOf(shooter.bEPCInTheWay) + "]  " + 
                "State:" + shooter.clearepc.sState + "  "  + 
                "Step:" + String.valueOf(shooter.clearepc.iStep)
              );
    
      if( inputs.joyTestController.getRawButton(8) == true){
        inputs.dRequestedCarouselPower = shooter.dSlowCarouselPower;
      } else if ( inputs.joyTestController.getRawButton(9) == true ){
        inputs.dRequestedCarouselPower = -shooter.dSlowCarouselPower;
      } else if ( inputs.joyTestController.getRawButton(7) == true ){
        shooter.clearepc.execute(inputs, shooter);	
      } else {
          inputs.dRequestedCarouselPower = 0.0;
          shooter.clearepc.reset();
      }

      shooter.motPWMEPCCarousel.set(inputs.dRequestedCarouselPower);

    } else if(sTestProcess.compareTo("dumpmaps") == 0 ){

      scriptedauton.dumpMap();
      scriptedauton.dumpDecriptions();
      System.out.println("******** Test is done ******************");
      bTestIsDone = true;

    } else if(sTestProcess.compareTo("selectauton") == 0 ){

      scriptedauton.dumpMap();
      scriptedauton.dumpDecriptions();
      System.out.println("******** Test is done ******************");
      bTestIsDone = true;

    } else if( sTestProcess.compareTo("ramppower") == 0 ){
      if( testRampPower.bArrived == false){
        inputs.dDriverPower = testRampPower.calcPower(   testApplyPower.ticksToInches( robotbase.getEncoderPosition())  );
      }

      robotbase.update();


    } else {

      System.out.println("sTestProcess: [" + String.valueOf(sTestProcess) + "]  " + 
                "is set to default. No process defined in config file. "  );
                bTestIsDone = true;
          
    }
  }
}
