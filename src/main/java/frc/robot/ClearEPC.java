package frc.robot;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

class ClearEPC {

	int iStep = 0;
	int iNextStep = 0;
	int iLastStep = -1;
    boolean bStepIsSetUp = false;
    boolean bIsDone = false;
	Timer timStepTimer = null;
	String sState = "Reset";

    double dEPCReversePulseTime = .05;
    double dEPCSettleTime = .20; 

	public ClearEPC(){
		timStepTimer = new Timer();
		timStepTimer.start();
		System.out.println("ClearEPC: Constructore complete...");
	}

	void loadConfig(Config config){
		dEPCReversePulseTime = config.getDouble("clearepc.dEPCReversePulseTime", .10);
		dEPCSettleTime = config.getDouble("clearepc.dEPCSettleTime", .50); 
	}

	public void reset(){
		iStep = 0;
		iNextStep = 0;
		iLastStep = -1;
		sState = "Init";
        bIsDone = false;
    }

	public void execute(Inputs inputs, Shooter shooter){

		/**
		 * We need iNextStep as a way to indicate in a step where we are going next.
		 * Prefer this to changing iStep in a step before we record telemetry. 
		 * Without this we see iStep change and not reflect correct state when 
		 * recorded in telemetry.
		 * 
		 * Now iStep changes to iNextStep below before the actual next step section and 
		 * after the previous iStep is completed and recorded. 
		 */
		iStep = iNextStep;					// do these here to change to the next step. 

		if( iLastStep != iStep){			// we have move to a new step. Get ready. 
			timStepTimer.reset();			// reset the "step" timer so we know how long we have been in the step
			bStepIsSetUp = false;			// used to set up contitiones within a step before it starts
		}

		iLastStep = iStep;					// save this for the next loop

		SmartDashboard.putNumber("ClrEPC iStep", iStep);
		SmartDashboard.putNumber("ClrEPC Timer", timStepTimer.get() );
		SmartDashboard.putString("ClrEPC State", sState );

		switch (iStep) {

			case 0:			// Initilaize any thing you need to 
				sState = "Init";
				iNextStep = iStep+1;
				break;

			case 1:										    // check that there isn't an epc below the lifter
				sState = "wait for opening";
				if(shooter.bEPCInTheWay == true){           // see EPC reset the time
					sState = "see EPC reset";
					timStepTimer.reset();
				}

				if( shooter.bEPCInTheWay == false && timStepTimer.get() > .08){ // not seen ball for some time
					sState = "No EPC time up";
					iNextStep= iStep + 1;				    // next step
                    inputs.dRequestedCarouselPower = 0.0;   // turn off carousel
                    break;	
                }
                
                inputs.dRequestedCarouselPower = shooter.dSlowCarouselPower;	// turn one way slow
				break;

            case 2: // wait until the carousel settles
                sState = "Carousel Settle";
                inputs.dRequestedCarouselPower = 0.0;	
                if( timStepTimer.get() > dEPCSettleTime){   // wait to allow carousel to settls
					iNextStep= iStep + 1;				    // long enough, next step
                }

                break;

            case 3: // check for EPC or pulse backwards
                sState = "Carousel Pulse";
                if(shooter.bEPCInTheWay == false){		                // EPC is in the way
                    iNextStep= iStep + 1;				// no EPC, next step
                    break;
                }

                inputs.dRequestedCarouselPower = -shooter.dSlowCarouselPower; // rotate backwards, pulsed	
                if( timStepTimer.get() > dEPCReversePulseTime){
					iNextStep= 2;				// no EPC, back to settle
                }

                break;

			case 4:
			default:
                sState = "Done";
                this.bIsDone = true;

				break;

        }
    }

	public void addTelemetryHeaders(final LCTelemetry telem ){
		telem.addColumn("Sh ClrEPC Step Time");
		telem.addColumn("Sh ClrEPC Step");
		telem.addColumn("Sh ClrEPC State");
		telem.addColumn("Sh ClrEPC Next Step");
	}

    public void writeTelemetryValues(final LCTelemetry telem ){
		telem.saveDouble("Sh ClrEPC Step Time", timStepTimer.get(), 2 ); 
		telem.saveInteger("Sh ClrEPC Step", iStep );
		telem.saveInteger("Sh ClrEPC NextStep", iNextStep );
		telem.saveString("Sh ClrEPC State", sState );
    }
	
}
