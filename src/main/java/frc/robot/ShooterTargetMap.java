package frc.robot;

//import java.io.File;
//import java.io.FileWriter;
import java.io.*;
import java.util.TreeMap;			// usig TreeMap vs HashMap becasue TreeMap sorts by key or velocity. 



public class ShooterTargetMap{ 

  public String sFileName = null;
  
  public TreeMap<Double,TargetData> mapValues = new TreeMap<Double,TargetData>();


  public ShooterTargetMap(String sPassedFilePath, String sPassedFileName){

    sFileName = sPassedFilePath + File.separator + sPassedFileName;
    
  }

  public void saveTargetData(TargetData mTargetData) {

	mTargetData.saveTargetData(sFileName);

  }
    
  public void loadTable() {


    BufferedReader bufr = null;
	  
	try {
		FileReader fr = new FileReader(sFileName);
        bufr = new BufferedReader(fr);
	        
		String line = bufr.readLine();
		while(line != null){
			String[] sKVPair = line.split(",");	   								// break up line into Key and Value Strings
			
			Double dCameraPosition = Double.valueOf( sKVPair[0]);
			Integer iShooterVelocity = Integer.valueOf( sKVPair[1]);
			Double dHoodPosition = Double.valueOf( sKVPair[2]);
			
			TargetData mTargetData = new TargetData( dCameraPosition,
					iShooterVelocity,
					dHoodPosition);
										  
			mapValues.put(dCameraPosition, mTargetData);				  		// save to map as CamPos, TargetData class
			line = bufr.readLine();												// get next line
		}

		bufr.close();
	
	}
	catch(IOException e) {
	  e.printStackTrace();
	}

  }
  
  /***
  public void dumpMap() {
	  
	    /      // using for-each loop for iteration over Map.entrySet() 
        for (Map.Entry<Integer,Double> entry : mapValues.entrySet())  
            System.out.println("Key = " + entry.getKey() + 
                             ", Value = " + entry.getValue()); 
	  
  }
  ***/

  public TargetData getTargetData(Double dCameraPosition) {
	
	Double dKey = mapValues.floorKey(dCameraPosition); // get nearest camera position

	return mapValues.get(dKey);						// return the targetdata class looked up

	//return mTargetData;
    //System.out.println("Key = " + String.valueOf(iVelocity) + " | " + 
    //                         "floor: " + String.valueOf(floorKey) + " , " + mapValues.get(floorKey) +
	//						 "  next: " + String.valueOf(ceilingKey)  + " , " + mapValues.get(ceilingKey) );
	  
  }


  
}
