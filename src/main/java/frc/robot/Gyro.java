package frc.robot;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj.SPI;


public class Gyro {
	// declare your variable up here
    AHRS ahrs = null; 												//multi point sensor board
	
	// Values returned by Navx-MXP
	boolean b_isMoving, b_isRotating;
	double d_navxAngle, d_navxPitch, d_navxRoll, d_navxYaw;
	double d_navxDisplacementX, d_navxDisplacementY, d_navxDisplacementZ;
	
	public final static int kGyro_None		= 0;
	public final static int kGyro_TurnTo	= 1;
	public final static int kGyro_Correct	= 2;
	public final static int kGyro_Assist	= 3;

	
	// class Constructor initialize you variables here
    public Gyro() {

    	b_isMoving = 
    	b_isRotating = false;

		d_navxAngle =
    	d_navxPitch =
    	d_navxRoll  =
    	d_navxYaw = 
    	d_navxDisplacementX =
    	d_navxDisplacementY =
    	d_navxDisplacementZ = 0.0;
    	
        try {
            /* Communicate w/navX MXP via the MXP SPI Bus.                                     */
            /* Alternatively:  I2C.Port.kMXP, SerialPort.Port.kMXP or SerialPort.Port.kUSB     */
            /* See http://navx-mxp.kauailabs.com/guidance/selecting-an-interface/ for details. */
            ahrs = new AHRS(SPI.Port.kMXP); 
        } catch (RuntimeException ex ) {
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
        }

    }

    
    // This will read all the inputs, cook them and save them to the appropriate variables.
    public void readValues() {
    	
		// navxMXP gyro, accelerometer, compass
		if(ahrs != null){
			if (ahrs.isConnected() && !ahrs.isCalibrating()) {
				b_isMoving = ahrs.isMoving();
				b_isRotating = ahrs.isRotating();
				d_navxAngle = ahrs.getAngle();
				d_navxPitch = ahrs.getPitch();
				d_navxRoll = ahrs.getRoll();
				d_navxYaw = ahrs.getYaw();
				d_navxDisplacementX = ahrs.getDisplacementX();
				d_navxDisplacementY = ahrs.getDisplacementY();
				d_navxDisplacementZ = ahrs.getDisplacementZ();
			}
		}
	}

	/**
	 * GetGyroRelativeBearing Here we will cook the value of the gyro. If we are
	 * turning right we go positive.<br>
	 * Turn left and we go negative. Gyro returns 360 on down the more you turn
	 * left. We cannot use that as it is.
	 * <p>
	 * Here we convert to relative angle. Positive we are going right. Negative
	 * left.
	 * 
	 * <pre>
	 * Angle Value | Compass Value | Relative Value No Turn 0 deg | 0 | 0 Right 5
	 * degrees | 5.0 | 5.0 Left 5 degrees | 355.0 | -5.0 (355-360)
	 * 
	 * @return
	 */

	public double getGyroRelativeBearing() {

		double d_CurrYaw = this.ahrs.getAngle(); // you have to read the angle from the gyro

		if (d_CurrYaw <= 180.0) // return positive if < 180
			return d_CurrYaw;
		else
			return (d_CurrYaw - 360.00); // return value offset from 360 if > 180

	}

	/**
	 * ZeroGyroBearing() Call this to reset the gyro to 0 degrees. You may need to
	 * experiment to get the value correct.
	 * 
	 * @return
	 */

	public void zeroGyroBearing() {

		this.ahrs.zeroYaw();

	}

	public double getPitch() {

		return this.ahrs.getRoll();

	}

	public void addTelemetryHeaders(LCTelemetry telem) {
		telem.addColumn("GY Bearing");
	}

	public void writeTelemetryValues(LCTelemetry telem) {
		telem.saveDouble("GY Bearing", this.getGyroRelativeBearing());
	}

	// Show what variables we want to the SmartDashboard
	public void outputToDashboard(boolean b_MinDisplay) {
		SmartDashboard.putNumber("G Bearing", this.getGyroRelativeBearing());

		if (b_MinDisplay == true) return;


		SmartDashboard.putNumber("G Bearing", getGyroRelativeBearing());
		SmartDashboard.putBoolean("G Moving", b_isMoving);
		SmartDashboard.putNumber("G NAVX Angle", d_navxAngle);
		
	}
	
	// Load config file
	public void loadConfig(Config config) {
		// Read these from config file
		// target.loadConfig(config);
	}

}
