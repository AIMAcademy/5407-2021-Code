package frc.robot;

import java.util.HashMap;

public class ApplyPower {

    private static final int k_iArchade = 1;
    private static final int k_iMecannum = 2;
    private static final int k_iZDrive = 3;

    public static final int k_iLeftFrontDrive = 1;
    public static final int k_iRightFrontDrive = 2;
    public static final int k_iLeftRearDrive = 3;
    public static final int k_iRightRearDrive = 4;

    public static final int k_iFrontZDrive = 1;
    public static final int k_iRearZDrive = 2;
    public static final int k_iLeftZDrive = 3;
    public static final int k_iRightZDrive = 4;

    public static final double k_dCalculatedError = -10000000000.0;

    public HashMap<String,Double> mapEncoderValues = new HashMap<String,Double>();

    public Double dSavedStartLocation = 0.0;

    Double dPctOfDistance = 0.0;           // pct of how far we traveled
    double dPCTOfPower = 0.0;                       // pct power over full distance. 
    double dCalculatedPower = 0.0;      // default this in case of no changes 
    double dRampUpPoint = .20;
    double dRampDownPoint = .80;
    double dTicksToInches = 2987.234043;

    public ApplyPower(){
		
        // nothing to initialize for this class
    }

    public void saveEncoderLocation(String sEncoderName, Double dStartLocation){

        dSavedStartLocation = dStartLocation;
        //mapEncoderValues.put(sEncoderName, dStartLocation);

    }

	public void loadConfig(Config config){
		dRampUpPoint = config.getDouble("applypower.dRampUpPoint", .2);
		dRampDownPoint = config.getDouble("applypower.dRampDownPoint",.8);

	}
        
    public double getServoPower( double dRequestedPower, double dDeadBand, boolean bInvert){
		/** Convert requested power in range 1.0 to -1.0 to the servo range 
		 * This Servo motor is a continuois turn servo
		 * deadband we set at .2/-.2 up above 
		 * Range greater than .5 is up
		 * 		  .5 =  stop
		 * 		 less than .5 is down 
		 * We will pass the joystick power and modify the fit the
		 * new range for the servo.  
		 * */

        double temp = dRequestedPower;
        
		if( Math.abs(temp) < dDeadBand ){			// dead band to prevent accidental hits
			temp = 0.0;
        }
        
		//temp = temp * Math.abs(temp*temp);			// dsensitizie the lower end .5 stop.

		temp += 1.0;								// shift from 1.0 / -1.0 to 2.0 / 0.0
		temp /= 2;									// shift from 2.0 / 0.0 to 1.0 / 0.0, 
		dRequestedPower = temp;			            // save result 
	
		/** Range is now set 0.0 to 1.0.  .5 is stop.
		 * But 0.0 is up and 1.0 is down. 
		 * We may need to flip it so 1.0 is up and 0.0 is down
		 * It is a servo so we cannot just use motor invert 
		 **/
        if( bInvert == true){
            dRequestedPower -= .5;  	    // subtract the mid point .5. Convert from 0.0/1.0 to -.5/.5
            dRequestedPower *= -1;	        // now we can invert. Converts from -.5/5 to .5/-.5 	
            dRequestedPower += .5;	        // now add back .5   .5/-.5 becomes 1.0/0.0 which is what we want.
        }

		return( dRequestedPower);

    }


    public double getEncoderDistance(String sEncoderName, Double dCurrentLocation){

        //Double dStartLocation = mapEncoderValues.getOrDefault(sEncoderName, Double.valueOf(k_dCalculatedError));

        return (dCurrentLocation - dSavedStartLocation); 

    }

    public double RampPowerToEncoder( double dPower, String sEncoderName, Double dCurrentLocation, 
                    Double dTargetDistance, Double dMinPower){

        Double dCurrentDistance = getEncoderDistance(sEncoderName, dCurrentLocation);

        //if( dCurrentDistance == k_dCalculatedError){
        //    return k_dCalculatedError;
        //}

        dCalculatedPower = dPower;                              // default this in case of no changes 
        //System.out.println(">>>>>AP IN  dCalculatedPower: " + String.valueOf(dCalculatedPower));
        dPctOfDistance = dCurrentDistance/dTargetDistance;      // pct of how far we traveled, always positive
        dPCTOfPower = dPower * dPctOfDistance;                  //      pct power over full distance. 

        //if(dPctOfDistance > .95){                               // are we in the start up part
        //    dCalculatedPower = 0.0;
        //}
        //else 
        /**
        if(dPctOfDistance < dRampUpPoint){                // are we in the start up part   
            dCalculatedPower = dPCTOfPower/dRampUpPoint;        //      from beginning to ramp up point
        }else if(Math.abs(dPctOfDistance) >= dRampDownPoint){            // are we in the end part
            dCalculatedPower = (1.0-dPCTOfPower)/dRampDownPoint;        //      we are now at ramp down, subtrack overall pct from full power
        }
        //System.out.println(">>>>>AP MID dCalculatedPower: " + String.valueOf(dCalculatedPower));

        if( Math.abs(dCalculatedPower) < dMinPower){
            if( dPower > 0.0  ){ 
                dCalculatedPower = dMinPower;
            } else if( dPower < 0.0 ){
                dCalculatedPower = -dMinPower;
            }
        }
        **/
        //System.out.println(">>>>>AP OUT dCalculatedPower: " + String.valueOf(dCalculatedPower));
        return dCalculatedPower;

    }

	public double inchesToTicks( double dInches ){
		return dInches * dTicksToInches;
	}

	public double ticksToInches( double dTicks ){
		return dTicks / dTicksToInches;
	}

    public double calcZWheelPower(int iWheel, double dPower, double dTurn, double dCrab ) {

        double dWheelPower = 0.0;

        // Calculate the the zdrive power and crab

        //        Front(1)
        //          ===
        //     ||          ||
        //Left ||          ||  Right
        // (3) ||          ||   (4)
        //          ===
        //        Rear (2)

        // Apply the drive power (front/back) and crab power (left/right)
        switch(iWheel){
            case k_iFrontZDrive:     // these make the robot go left and right so they get crab power
            case k_iRearZDrive:
                dWheelPower = dCrab; // in inputs decide with direct positive is vs. negative.
                break;

            case k_iLeftZDrive:      // these make the robot go forward and back so they get driver power
            case k_iRightZDrive:
                dWheelPower = dPower;
                break;

            default:
                // Tell user that the wheel is not valid.
                //telemetry.addData("ERROR: ApplyWheelPower: getWheelPower", "iWheel is not known.");
                //telemetry.update();
                return 0.0;
        }

        switch(iWheel){              // now add in the turn power
            case k_iFrontZDrive:
                dWheelPower += dTurn;
                break;

            case k_iRearZDrive:
                dWheelPower -= dTurn;
                break;

            case k_iLeftZDrive:      // these make the robot go forward and back so they get driver power
                dWheelPower += dTurn;
                break;

            case k_iRightZDrive:
                dWheelPower -= dTurn;
                break;

            default:
                // Tell user that the wheel is not valid.
                //telemetry.addData("ERROR: ApplyWheelPower: getWheelPower", "iWheel is not known.");
                //telemetry.update();
                return 0.0;
        }


        return dWheelPower;

    }

    private double calcWheelPower(int iDriveType, int iWheel, double dPower, double dTurn, double dCrab ) {

        double dWheelPower = 0.0;

        if (iDriveType != k_iMecannum && iDriveType != k_iArchade){
            // Tell user that the wheel is not valid.
            //telemetry.addData("ERROR: ApplyWheelPower: getWheelPower", "iType is not mecannum or arcade.");
            //telemetry.update();
            return 0.0;
        }

        // Calculate the archade power
        switch(iWheel){
            case k_iLeftFrontDrive:
                    dWheelPower = dPower + dTurn;
                    break;
            case k_iRightFrontDrive:
                    dWheelPower = dPower - dTurn;
                    break;
            case k_iLeftRearDrive:
                    dWheelPower = dPower + dTurn;
                    break;
            case k_iRightRearDrive:
                    dWheelPower = dPower - dTurn;
                    break;

            default:
                    // Tell user that the wheel is not valid.
                    //telemetry.addData("ERROR: ApplyWheelPower: getWheelPower", "iWheel is not known.");
                    //telemetry.update();
                    return 0.0;

        }

        if( iDriveType == k_iArchade)
            return dWheelPower;


        // We are Mecannum so now apply the crab power
        switch( iWheel){
            case k_iLeftFrontDrive:
                dWheelPower += dCrab;
                break;
            case k_iRightFrontDrive:
                dWheelPower -= dCrab;
                break;
            case k_iLeftRearDrive:
                dWheelPower -= dCrab;
                break;
            case k_iRightRearDrive:
                dWheelPower += dCrab;
                break;
        }

        return dWheelPower;

    }

    // This is an example fo Java over loading. Same function name different parameters.
    public double getWheelPower(int iWheel, double dPower, double dTurn ) {
        return calcWheelPower(k_iArchade, iWheel, dPower, dTurn, 0.0 );
    }

    public double getWheelPower(int iWheel, double dPower, double dTurn, double dCrab ) {
        return calcWheelPower(k_iMecannum, iWheel, dPower, dTurn, dCrab );
    }

    public void addTelemetryHeaders(LCTelemetry telem ){
        telem.addColumn("AP Ramp Up"); 
        telem.addColumn("AP Ramp Down"); 
        telem.addColumn("AP Calc Power"); 
        telem.addColumn("AP PCT Power" );
        telem.addColumn("AP PCT Dist");
    
    }

  public void writeTelemetryValues(LCTelemetry telem ){

    telem.saveDouble("AP Ramp Up", this.dRampUpPoint); 
    telem.saveDouble("AP Ramp Down", this.dRampDownPoint); 
    telem.saveDouble("AP Calc Power", this.dCalculatedPower); 
    telem.saveDouble("AP PCT Power", this.dPCTOfPower );
    telem.saveDouble("AP PCT Dist", this.dPctOfDistance);
    

 }

};