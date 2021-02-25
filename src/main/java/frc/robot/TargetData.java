package frc.robot;

import java.io.FileWriter;


public class TargetData{ 

    public Double dCameraPosition = 0.0;
    public Integer iShooterVelocity = 0;
    public Double dHoodPosition = 0.0;

    //private String sDescription = "";
    private String sCSVLine = "";

  public TargetData(Double dCameraPos, Integer iShooterVel,  Double dHoodPos){

    	dCameraPosition 		= dCameraPos;
		iShooterVelocity 		= iShooterVel;
		dHoodPosition 			= dHoodPos;

		sCSVLine =  String.valueOf(dCameraPosition)  + "," +
					String.valueOf(iShooterVelocity)  + "," +
					String.valueOf(dHoodPosition);
					
  }

  public String toText(){

	return  "CamPos: " + String.valueOf(dCameraPosition)  + System.lineSeparator() +	
			"ShVel.: " + String.valueOf(iShooterVelocity)  + System.lineSeparator() +	
			"HoodPos: " + String.valueOf(dHoodPosition)    + System.lineSeparator();
	  
  }

  public String getCSVLine(){
		return sCSVLine;
  }
  
  public void saveTargetData(String sFileName) {

	FileWriter fh = null;

	try {
		fh = new FileWriter(sFileName, true);                        //True means append
		fh.write(sCSVLine + System.lineSeparator());	
		fh.close();
	}
	catch(Exception e) {
	  e.printStackTrace();
	}

  }

}
