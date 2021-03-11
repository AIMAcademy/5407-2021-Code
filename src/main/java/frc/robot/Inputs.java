package frc.robot;

import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;

/**********************************************************************
 * Inputs Class -- This is used to collect all human input or what is considered input for the robot.
 *<p>
 *Here we will document how the joysticks accept the human input
 *
 * <pre>
 *     Source     Range     Usage
 * 
 * Driver Turn     Axis     -1 to 1
 * 
 * </pre>  
 */


import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Inputs {

	Config config = new Config("/home/lvuser","WPConfig_2020.cfg");	// declare you variable up here
	private XboxController gamepadOperator = null;
	private XboxController gamepadDriver = null;
	public Joystick joyTestController = null;


	//Test Code
	//Orginally named Daniel is cool but is pretty chill
	//Also brackets go like this {
	//}
	public double dDanielsMouthPower = 0.0; 
	private int iDanielCounts = 0;
	public double dIntakePower = 0.0; 

	public double dDriverPower = 0.0;
	public double dLeftWinchPower = 0.0;
	public double dRightWinchPower = 0.0;

	public double dDriverTurn = 0.0;
	public double dShooterPower = 0.0;
	public double dHoodPower = 0.0;
	public double dTestValue = 0.0;
	public double dRequestedVelocity = 0.0;
	public double dRequestedCarouselPower = 0.0;		//set this power anywhere to spin carousel
	public boolean bSpeed = false;	//True == fast - False == slow
	public double dFastSpeed;
	public double dSlowSpeed;
	
	public boolean bShooterLaunch = false;
	public boolean bTargetting = false;

	public boolean bIntakeIn = false;
	public boolean bIntakeOut = false;

	public boolean bTeainatorUp = false;
	public boolean bTeainatorDown = false;

	//public boolean bUpdateShooterPID = false;

	// valuse used in the base
	public boolean bShiftBaseToHigh = false;

	public boolean bSaveEncoderPosition = false;		// save the current position when using encoders
	public boolean bRampPower = false;					// request to use power ramping when using encoders
	public double dTargetDistanceUsingEncoder = 0.0;	// how far you want to go when using encoders

	public double dShooterHoodPower = 0.0;
	public double dRequestedCameraPosition = 0.0;

	public boolean bShooterVelocity_Raise = false;
	public boolean bShooterVelocity_Lower = false;
	public boolean bShooterVelocitySaveSetting = false;
	public boolean bSpinUpShooter = false;
	public boolean bCloseTargets = true;
	public boolean bFarTargets = false;

	public boolean bRunAuton = false;
	public boolean bSelectAuton = false;
	public boolean bLastSelectAuton = false;

	public boolean bInEndGame  = false;
	double dMaxWinchPower = .65;

	int iHoodRequestedToPosition = 0;
	double dRequestedBearing = 0.0;

	int iGyroRequest = Gyro.kGyro_None;
	

	// class Constructor initialize your variables here
    public Inputs() {
		dFastSpeed = config.getDouble("shooter.dFastCarouselPower", .6);
		dFastSpeed = config.getDouble("shooter.dSlowCarouselPower", .2);		
		joyTestController = new Joystick( RobotMap.kUSBPort_TestJoyStick );
		gamepadDriver  = new XboxController(RobotMap.kUSBPort_DriverControl );
    	gamepadOperator = new XboxController(RobotMap.kUSBPort_OperatorControl );
    	zeroInputs();      				// this will init many variables
    
    }
    
    // this is the order they will be in the spread sheet. 
    public void addTelemetryHeaders(LCTelemetry telem ){
		telem.addColumn("I Driver Power");
		telem.addColumn("I Driver Turn");
		telem.addColumn("I Shooter Power");
		telem.addColumn("I Shooter Launch");
		telem.addColumn("I Sh Hood Power");
		telem.addColumn("I Turret Power");
		telem.addColumn("I Intake In");
		telem.addColumn("I Intake Out");
		telem.addColumn("I Base Shift");
		telem.addColumn("I Update PID");
		telem.addColumn("I Req Vel");
		telem.addColumn("I PID F Lower");
		telem.addColumn("I PID F Raise");
		
		//telem.addColumn("I Fire By Camera");
    }

    // the order does not matter here 
    public void writeTelemetryValues(LCTelemetry telem ){
		telem.saveDouble("I Driver Power", this.dDriverPower );
		telem.saveDouble("I Driver Turn", this.dDriverTurn );
		telem.saveDouble("I Shooter Power", this.dShooterPower );
		telem.saveDouble("I Sh Hood Power", this.dHoodPower );
		telem.saveTrueBoolean("I Shooter Launch", this.bShooterLaunch );
		telem.saveTrueBoolean("I Intake In", this.bIntakeIn);
		telem.saveTrueBoolean("I Intake Out", this.bIntakeOut);
		telem.saveDouble("I Req Vel", this.dRequestedVelocity );
		telem.saveTrueBoolean("I Base Shift", this.bShiftBaseToHigh );
		//telem.saveTrueBoolean("I Update PID", this.bUpdateShooterPID);
		telem.saveTrueBoolean("I PID F Lower", this.bShooterVelocity_Lower );
		telem.saveTrueBoolean("I PID F Raise", this.bShooterVelocity_Raise );
		//telem.saveDouble("I Driver Arch Power", this.d_DriverArchadePower );
		//telem.saveTrueBoolean("I Fire By Camera", this.b_FireByCamera);
		
    }

    
    // This will read all the inputs, cook them and save them to the appropriate variables.
    public void readValues() {   

		// set defaults for the base and gyro
		dTargetDistanceUsingEncoder = 0.0;
		bRampPower = false;					// use to tell bas to ramp the power using the encoder
		bSaveEncoderPosition = false;		// force to false so someone else can set it later
		dRequestedBearing = -1.0;
		iGyroRequest = Gyro.kGyro_None;

		// set defaults for the shooter
		bSpinUpShooter = false;				// force to false until we assign a button
		iHoodRequestedToPosition = -1;      // force to -1 to indicate no requests.
		dRequestedCarouselPower = 0.0;
		dRequestedVelocity = 0.0;

		bShooterLaunch = joyTestController.getRawButton(9);
		dShooterHoodPower = joyTestController.getY();

		if(gamepadDriver.getBackButton() == true && gamepadOperator.getBackButton() == true){
			bInEndGame = true;
		}

		if(gamepadDriver.getStartButton() == true && gamepadOperator.getStartButton() == true){
			bInEndGame = false;
		}

		dLeftWinchPower = 0.0;
		dRightWinchPower = 0.0;

    	// you can overload the inputs to test different ideas. 
		//double temp  = F.getY(Hand.kLeft) ;	    //  we cook this down as full is too fast
		double temp  = -gamepadDriver.getY(Hand.kRight) ;	    // we cook this down as full is too fast
		//double temp = -joyTestController.getY();				// - to make it range 1.0 to -1.0 vs -1.0 to 1.0
		dDriverPower = temp * Math.abs(temp * temp * temp);     // quad it to desensatize the turn power 
		
		temp  = gamepadDriver.getX(Hand.kLeft) ;	    					// we cook this down as full is too fast
		//temp = joyTestController.getZ();
		dDriverTurn = temp * Math.abs(temp * temp * temp);      // quad it to desensatize the robot turn power 

		//temp  = gamepadOperator.getX(Hand.kLeft) ;	    		// we cook this down as full is too fast
		//dTurretPower = temp * Math.abs(temp * temp * temp);    	// quad it to desensatize the turret turn power 

		temp  = gamepadOperator.getY(Hand.kLeft) ;	    		// we cook this down as full is too fast
		dLeftWinchPower = temp * Math.abs(temp * temp * temp);  // set these now, adjust when we determine end game

		temp  = gamepadOperator.getY(Hand.kRight) ;	    		// we cook this down as full is too fast
		dRightWinchPower = temp * Math.abs(temp * temp * temp);  // set these now, adjust when we determine end game



		if( bInEndGame == true)
		{
			if(gamepadOperator.getPOV() == 0)
			{
				dRightWinchPower = dMaxWinchPower;
				dLeftWinchPower = dMaxWinchPower;
			} else if(gamepadOperator.getPOV() == 180)
			{
				dRightWinchPower = -dMaxWinchPower;
				dLeftWinchPower = -dMaxWinchPower;
			}
		} 
		else 
		{
			if(gamepadOperator.getPOV() == 0)
			{
				bCloseTargets = true;
				bFarTargets = false;

				bSpeed = true;
				
			} 
			else if(gamepadOperator.getPOV() == 180)
			{
				bSpeed = false;

				bCloseTargets = false;
				bFarTargets = true;
			}
		}

		if (gamepadOperator.getBButton()){

			if (bSpeed == true) {//Fast

				dRequestedCarouselPower = dFastSpeed;
				System.out.println("I works");

			} else { //Slow

				dRequestedCarouselPower = dSlowSpeed;
				System.out.println("Maybe?");

			}

		}

		if(gamepadOperator.getYButton())
		{
			dIntakePower = 0.5; 

		}
		else if(gamepadOperator.getAButton())
		{

			dIntakePower = -0.5; 

		} else 
		{
			dIntakePower = 0.00; 
			
		}

		if(joyTestController.getTop() == true){
			iGyroRequest = Gyro.kGyro_Assist;
		}
		bRunAuton = joyTestController.getRawButton(11);
		
		//temp = convertJoystickAxisToValueRange( joyTestController.getTwist(), 100 ) ; // force to + value only
		//temp = convertJoystickAxisToValueRange(  joyTestController.getThrottle(), 100 ) ;    // force to + value only
		if( temp < 100 ){
			dRequestedVelocity = 0.0;
		}else {
			dRequestedVelocity = 6000 + temp * 100;			// raise in 100 ticks increments
		}
		//dRequestedVelocity = 0.0;

		//temp =  convertJoystickAxisToValueRange( joyTestController.getTwist(), 1 ) ; // force to + value only
		//temp = joyTestController.getTwist();
		//dRequestedCameraPosition = temp;

		bShooterVelocitySaveSetting = joyTestController.getRawButtonPressed(11);

		/**
		 * Teainator in/out  processing
		 **/				
		bIntakeIn = false;
		bIntakeOut = false;
									
		// give priority to the operator in these operations
		if( gamepadOperator.getBumper(Hand.kLeft) == true){
			bIntakeIn = true;
		} else if( gamepadOperator.getBumper(Hand.kRight) == true){
			bIntakeOut = true;
		} else if (gamepadDriver.getTriggerAxis(Hand.kLeft) > 0.7 ){  //Prevent accident Hits
			bIntakeIn = true;			
		} else if (gamepadDriver.getTriggerAxis(Hand.kRight) > 0.7 ){ //Prevent accident Hits
			bIntakeOut = true;									
		}

		// only used during disbaled periodic mode.
		bSelectAuton = gamepadOperator.getYButtonPressed();
		//if( gamepadOperator.getYButtonRelease() == true && bLastSelectAuton == false){  
		//	bSelectAuton = true;
		//}
		//bLastSelectAuton = gamepadOperator.getYButton();

		/**
		 * Teainator up/down  processing
		 **/				
		bTeainatorDown = false;
		bTeainatorUp = false;

	
		bShiftBaseToHigh= gamepadDriver.getBumper(Hand.kLeft);

		// get axis and then it will be process in the shooter 
		//dShooterHoodPower = -gamepadOperator.getY(Hand.kRight); // Invert so 1 is up and -1 is down.

		// the end game is when we are ready to hang. Slower movements are better
		if( bInEndGame == false){
			dLeftWinchPower = 0.0;		// kill these as they should not be used until the end game. 
			dRightWinchPower = 0.0;
		} else if( bInEndGame == true){
			dDriverPower *= .3;			// allow only small movements
			dDriverTurn *= .3;
			//bShooterHeightP = false;
			//bShooterHeightLower = false;
		}
		
		if (gamepadOperator.getTriggerAxis(Hand.kLeft) > 0.7){		// Prevent accidental presses
			bTargetting = true;
		} else {
			bTargetting = false;
		}

		if (gamepadOperator.getTriggerAxis(Hand.kRight) > 0.7){		// Prevent accidental presses
			bShooterLaunch = true;
		}else{
			bShooterLaunch = false;		
		}
	
	
	}

    
	public int convertJoystickAxisToValueRange( double d_InputValue, int i_MaxValue )  {

		// Author Matt Hoffman LC2010 Alum.
		// use this to take an axis and convert into a range. 
		// Axis today it is 1.0 to -1.0, they have to invert outside when they pass in. 
		double d_temp = d_InputValue;						// get the current value    				   range: 1.0 to -1.0
		d_temp = d_temp + 1.0; 								// change range to positive only    		   range: 0.0 to 2.0
		d_temp = d_temp / 2.0;								// divide by 2 to get an average multiplier    range: 0.0 to 1.0  
		d_temp = (int) (d_temp * (double)i_MaxValue);	  	// multiply by highest, Example: 1000		   range:  0  to 1000 (int)
		return (int) d_temp;								// convert to int and return
		
															/* Truth table
															 *    getThrottle  change range	   convert to   mult by 
															 *                 positive +1     average /2   highest
															 *Down    -1.0        0.0             0.0            0            
															 *        -0.5        0.5             0.25         250            
															 *         0.0        1.0              .5          500            
															 *         0.5        1.5              .75         750            
															 *up       1.0        2.0             1.0         1000     								 */
	}
    
	public double convertJoystickAxisToValueRange( double d_InputValue, double d_MaxValue )  {

		// Author Matt Hoffman LC2010 Alum.
		// use this to take an axis and convert into a range. 
		// Axis today it is 1.0 to -1.0, they have to invert outside when they pass in. 
		double d_temp = d_InputValue;						// get the current value    				   range: 1.0 to -1.0
		d_temp = d_temp + 1.0; 								// change range to positive only    		   range: 0.0 to 2.0
		d_temp = d_temp / 2.0;								// divide by 2 to get an average multiplier    range: 0.0 to 1.0  
		d_temp = (d_temp * (double)d_MaxValue);	  			// multiply by highest, Example: 1,0		   range:  0  to 1000 (int)
		return d_temp;										// convert to int and return
		
															/* Truth table
															 *    getThrottle  change range	   convert to   mult by 
															 *                 positive +1     average /2   highest
															 *Down    -1.0        0.0             0.0            0            
															 *        -0.5        0.5             0.25         250            
															 *         0.0        1.0              .5          500            
															 *         0.5        1.5              .75         750            
															 *up       1.0        2.0             1.0         1000            
															 */
	}
    
	// Show what variables we want to the SmartDashboard
	public void outputToDashboard(boolean b_MinDisplay)  {

		SmartDashboard.putNumber("I_DriverPower",this.dDriverPower);
		SmartDashboard.putNumber("I_DriverTurn",this.dDriverTurn);
		SmartDashboard.putNumber("I_ShooterPower",this.dShooterPower);
		SmartDashboard.putNumber("I_TestValue",dTestValue);
		SmartDashboard.putBoolean("I In End Game",bInEndGame);
		SmartDashboard.putBoolean("I Sh Launch",bShooterLaunch);	
		SmartDashboard.putBoolean("I Sh Intake",bIntakeIn);	
		SmartDashboard.putNumber("I Hood Req Pos",iHoodRequestedToPosition);
		SmartDashboard.putNumber("I Hood Req Pow",dShooterHoodPower);
		SmartDashboard.putNumber("I Gyro Req Bear",dRequestedBearing);
		SmartDashboard.putBoolean("I Close Targets",bCloseTargets);
		SmartDashboard.putBoolean("I Far Targets",bFarTargets);
		SmartDashboard.putNumber("I Req Veloc",dRequestedVelocity);
		SmartDashboard.putNumber("I Req Camear Pos",dRequestedCameraPosition);
		SmartDashboard.putNumber("I Count Daniels", iDanielCounts);
		SmartDashboard.putNumber("I Daniel POWER", dDanielsMouthPower);
		
		if ( b_MinDisplay == false ){
		}
		
	}


	
	public void loadConfig(Config config)  {

		

        //bp_FastOperation = config.getBoolean("b_FastOperation", true);	// ****  we do not zero this **** 
//		b_IsTestMode = Preferences.getInstance().getBoolean("I_IsTestMode", false);
		//b_CameraTestMode = Preferences.getInstance().getBoolean("I_CameraTestMode", false);
	
	}


	

    public void zeroInputs() {					// reset all variables to stop or off state
     }
    

    
}
