/*----------------------------------------------------------------------------*/
/* Copyright (c) 2018 FIRST. All Rights Reserved.                             */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;


/**
 * Wrapper class for getting and setting Limelight NetworkTable values.
 * Limelight documentation and code examples can be found here:
 * http://docs.limelightvision.io/en/latest/getting_started.html
 * 
 * @author Dan Waxman
 */
public class Limelight {
	private NetworkTableInstance table = null;
	private String hostName;
	public  boolean bBaseIsOnTarget = false;
	public  boolean bShooterIsOnTraget = false;

	//NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
	//NetworkTableEntry tx = table.getEntry("tx");
	//NetworkTableEntry ty = table.getEntry("ty");
	//NetworkTableEntry ta = table.getEntry("ta");
	  

	public void update(Inputs inputs){

		bBaseIsOnTarget = false;
		bShooterIsOnTraget = false;
	
		if(inputs.bTargetting == true || inputs.bShooterLaunch == true){
				//LimelightXTargetting(inputs);
				setLedMode(LightMode.eOn);
		}else{
			setLedMode(LightMode.eOff);
		}

	}
  
	private void LimelightXTargetting(Inputs inputs){

		double dTx = getTx();

		SmartDashboard.putNumber("RB LL TX",  dTx);
		//SmartDashboard.putNumber("RB LL dDiff", dDiff);
		//SmartDashboard.putNumber("RB LL X Request", inputs.dRequestedBearing);

		bBaseIsOnTarget = true;
		
		if( Math.abs(dTx) < .5){
			bBaseIsOnTarget = true;
		} else {
			bBaseIsOnTarget = false;

			// is on target == false
			double dMax = .35;
			double dMin = .1;

			double dAnglePorportion = .05;
			inputs.dDriverTurn = dTx * dAnglePorportion;	// turn porportionally
			if(inputs.dDriverTurn > 0.0){
				if(inputs.dDriverTurn > dMax ){
					inputs.dDriverTurn = dMax; 
				} else {
					inputs.dDriverTurn = dMin;
				} 
			} else if(inputs.dDriverTurn < 0.0) {
				if(inputs.dDriverTurn < -dMax){
					inputs.dDriverTurn = -dMax; 
				} else {
					inputs.dDriverTurn = -dMin;
				}
			}
		}
	}

	public Limelight(String hostName) {
		this.hostName = hostName;
		setCameraMode(CameraMode.eVision);
	}

	/**
	 * Light modes for Limelight.
	 * 
	 * @author Dan Waxman
	 */
	public enum LightMode {
		eOn, eOff, eBlink
	}

	/**
	 * Camera modes for Limelight.
	 * 
	 * @author Dan Waxman
	 */
	public enum CameraMode {
		eVision, eDriver
	}


	/**
	 * Gets whether a target is detected by the Limelight.
	 * 
	 * @return true if a target is detected, false otherwise.
	 */
	public boolean isTarget() {
		return getValue("tv").getDouble(0) == 1;
	}

	/**
	 * Horizontal offset from crosshair to target (-27 degrees to 27 degrees).
	 * 
	 * @return tx as reported by the Limelight.
	 */
	public double getTx() {
		return getValue("tx").getDouble(0.00);
	}

	/**
	 * Vertical offset from crosshair to target (-20.5 degrees to 20.5 degrees).
	 * 
	 * @return ty as reported by the Limelight.
	 */
	public double getTy() {
		return getValue("ty").getDouble(0.00);
	}

	/**
	 * Area that the detected target takes up in total camera FOV (0% to 100%).
	 * 
	 * @return Area of target.
	 */
	public double getTa() {
		return getValue("ta").getDouble(0.00);
	}

	/**
	 * Gets target skew or rotation (-90 degrees to 0 degrees).
	 * 
	 * @return Target skew.
	 */
	public double getTs() {
		return getValue("ts").getDouble(0.00);
	}

	/**
	 * Gets target latency (ms).
	 * 
	 * @return Target latency.
	 */
	public double getTl() {
		return getValue("tl").getDouble(0.00);
	}

	// double[] cornX = mLimelightTable.getEntry("tcornx").getDoubleArray(new double[0]);
    //     double[] cornY = mLimelightTable.getEntry("tcorny").getDoubleArray(new double[0]);

	/**
	 * Gets raw corner x.
	 * 
	 * @return Target latency.
	 */
	public double[] getTcornX() {
		return getValue("tcornx").getDoubleArray(new double[0]);
	}

	/**
	 * Gets raw corner y.
	 * 
	 * @return Target latency.
	 */
	public double[] getTcornY() {
		return getValue("tcorny").getDoubleArray(new double[0]);
	}

	/**
	 * Sets LED mode of Limelight.
	 * 
	 * @param mode
	 *            Light mode for Limelight.
	 */
	public void setLedMode(LightMode mode) {
        getValue("ledMode").setNumber(mode.ordinal());
	}

	/**
	 * Sets camera mode for Limelight.
	 * 
	 * @param mode
	 *            Camera mode for Limelight.
	 */
	public void setCameraMode(CameraMode mode) {
		getValue("camMode").setNumber(mode.ordinal());
	}

	/**
	 * Sets pipeline number (0-9 value).
	 * 
	 * @param number
	 *            Pipeline number (0-9).
	 */
	public void setPipeline(int number) {
		getValue("pipeline").setNumber(number);
	}

	/**
	 * Helper method to get an entry from the Limelight NetworkTable.
	 * 
	 * @param key
	 *            Key for entry.
	 * @return NetworkTableEntry of given entry.
	 */
	private NetworkTableEntry getValue(String key) {
		if (table == null) {
			table = NetworkTableInstance.getDefault();
		}

        return table.getTable(hostName).getEntry(key);
	}

	public void outputToDashboard(final boolean b_MinDisplay)  {
		
		//SmartDashboard.putBoolean("LL isTarget", isTarget() );
		SmartDashboard.putNumber("LL dXCoord", getTx() );
		SmartDashboard.putNumber("LL dYCoord", getTy() );
		SmartDashboard.putNumber("LL dArea", getTa() );
		SmartDashboard.putBoolean("LL Sees Target", isTarget() ); 
		SmartDashboard.putBoolean("LL Base X Locked", bBaseIsOnTarget);

	}

	public void addTelemetryHeaders(final LCTelemetry telem ){
		telem.addColumn("LL TX");
		telem.addColumn("LL TY");
		telem.addColumn("LL TA");
		telem.addColumn("LL Sees Target");
		telem.addColumn("LL Base X Locked");

	}

	public void writeTelemetryValues(final LCTelemetry telem ){
		telem.saveDouble("LL TX", getTx() ); 
		telem.saveDouble("LL TY", getTy() ); 
		telem.saveDouble("LL TA", getTa() ); 
		telem.saveTrueBoolean("LL Sees Target", isTarget() ); 
		telem.saveTrueBoolean("LL Base X Locked", bBaseIsOnTarget);
	}
}
