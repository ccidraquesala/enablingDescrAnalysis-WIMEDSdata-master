package HBaseToWIMEDSDB;

import java.io.IOException;

import org.json.JSONArray;


public class mainExportUpdateOrgUnits {
	
	public static void main(String[] args) throws IOException, Exception{
		System.out.println("ˇˇapplication startedˇˇ");
		exportUpdateOrgUnitsToWIMEDS orgUnitsUpdate = new exportUpdateOrgUnitsToWIMEDS();
	
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		String ctrlPath = rootPath +"control.properties";

		orgUnitsUpdate.setProperties(ctrlPath);

		//creates a scanner with TimeRange config
		String scanner_id = orgUnitsUpdate.getTRScannerID();
		//updateRowsConetent has all the rows needed to be inserted/updated in the WIMEDS database
		JSONArray updateRowsContent = orgUnitsUpdate.updateData(scanner_id);
		
		//look if we want to use multithreading (parametrized in control.properties)
		int THREADS = 1;
		if(orgUnitsUpdate.getUseMultiThreading().equals("true"))THREADS = Runtime.getRuntime().availableProcessors();
		//call ParallelPartialQueryGenerator with isUpdate param = true (if its false it will generate a bulk operation with INSERT queries)
		ParallelPartialQueryGenerator partialQueryGen = new ParallelPartialQueryGenerator(THREADS, true);
		
		String SQLQuery="";
		//put workers to work and get the result query 
		SQLQuery += partialQueryGen.partialQueriesSum(updateRowsContent);
		
		if(SQLQuery.equals(""))System.out.println("Everything up-to-date");
		else {
			String DBurl = orgUnitsUpdate.getDBurl();
			String DBusr = orgUnitsUpdate.getDBusr();
			String DBpsw = orgUnitsUpdate.getDBpwd();
			//load to postgresDB
			orgUnitsUpdate.LoadInDB(SQLQuery, DBurl, DBusr, DBpsw);
		}
		
		//update extraction times, always the application correctly finalized
		orgUnitsUpdate.setExtractionTimes(ctrlPath);
		System.out.println("ˇˇapplication finishedˇˇ");
	}

}
