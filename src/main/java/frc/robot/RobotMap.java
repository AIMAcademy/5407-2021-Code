/**
 * Simple class containing constants used throughout project
 */
package frc.robot;

class RobotMap {

    // USB Ports
    public final static int kUSBPort_DriverControl = 0; 
    public final static int kUSBPort_OperatorControl = 1;
    public final static int kUSBPort_TestJoyStick = 2;

    // CAN Bus IDs
    public final static int kCANId_PCM = 0;                 // aparently required

    public final static int kCANId_RightDriveMotorA = 1;
    public final static int kCANId_RightDriveMotorB = 2;
    public final static int kCANId_LeftDriveMotorA = 3;
    public final static int kCANId_LeftDriveMotorB = 4;
    public final static int kCANId_ShooterMotorRight = 5;
    public final static int kCANId_ShooterMotorLeft = 6;
    public final static int kCANId_WinchLeftMotor = 8;
    public final static int kCANId_WinchRightMotor = 7;

    public final static int kCANId_PDP = 11;

    // Pneumatic Control Module Ids
    public final static int kPCMPort_DriveShifter = 0;
    public final static int kPCMPort_Arming = 1;
    public final static int kPCMPort_Teainator = 2;
    public final static int kPCMPort_BallPusherUpper = 4;

   // Digital Inputs
   public final static int kDigitalInPort_EPCInTheWay = 7;

   // Analog Inputs
   public final static int kAnalogPort_ShooterHood = 0;
   public final static int kAnalogPort_TurretPos = 1;

   // PWM Ports
   public final static int kPWMPort_IntakeMoter = 0;
   public final static int kPWMPort_EPCLifter  = 6;
   public final static int kPWMPort_EPCCarousel = 2;
   public final static int kPWMPort_CameraServo = 3;
   public final static int kPWMPort_ShooterHoodMotor = 5; 

   // Input Bottons
   public final static int kButton_ShooterHoodRaise = 4;
   public final static int kButton_ShooterHoodLower = 6;

   public final static int kButton_ShooterVelocity_Raise = 8;
   public final static int kButton_ShooterVelocity_Lower = 9;

}