package edu.csupomona.cs585.ibox;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import edu.csupomona.cs585.ibox.sync.FileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;

public class WatchDirIntegrationTest 
{
	Drive googleDriveClient;
	//the file to pass 
	final java.io.File localFile = new java.io.File("src/main/resources/Test.txt");
	final java.io.File dir = new java.io.File("src/test");
	
	public void initGoogleDriveServices() throws IOException 
	{
		//create a link to the Google Drive without requiring user agreement
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();
        try
        {
            GoogleCredential credential = new  GoogleCredential.Builder()
              .setTransport(httpTransport)
              .setJsonFactory(jsonFactory)
              .setServiceAccountId("670512217317-g9otg27ei61baup5rhrprd7frhto663r@developer.gserviceaccount.com")
              .setServiceAccountScopes(Collections.singleton(DriveScopes.DRIVE))
              .setServiceAccountPrivateKeyFromP12File(new java.io.File("src/main/resources/My Project-5c5ac64559b5.p12"))
              .build();

            googleDriveClient = new Drive.Builder(httpTransport, jsonFactory, credential).setApplicationName("My Project").build();  
        }
        catch(GeneralSecurityException e)
        {
            e.printStackTrace();
        }
	}
	
	public void watchDirectory(FileSyncManager fileSyncManager)
	{
		//create a thread to watch the directory and run processEvents()
		class oneShotTask implements Runnable
		{
			FileSyncManager fileSyncManager;
			
			oneShotTask(FileSyncManager fsm)
			{
				fileSyncManager=fsm;
			}
			@Override
            public void run() 
			{
				//the test directory where the test file will be placed, to be watched
				final java.io.File dirToWatch = new java.io.File("src/test");
				try 
				{
	                WatchDir myDir = new WatchDir(dirToWatch.toPath(), fileSyncManager);
	                myDir.processEvents();
                }
				catch (IOException e) 
				{
	                e.printStackTrace();
                }
            }
		}
		Thread t = new Thread(new oneShotTask(fileSyncManager));
		t.start();
	};
	
	@Test
	public void testProcessEvents() throws IOException, InterruptedException
	{
		//asserts that adding a file to the test directory will place the file in the Google Drive
		initGoogleDriveServices();
		FileSyncManager fileSyncManager = new GoogleDriveFileSyncManager(googleDriveClient);
		watchDirectory(fileSyncManager);
		Thread.sleep(5000);
		java.io.File tagFile=new java.io.File(dir, "fileTest.txt");
		if(!tagFile.exists())
		{
			tagFile.createNewFile();
		}
		Thread.sleep(5000);
		
		String newFileID = getFileId("fileTest.txt", googleDriveClient);
		tagFile.delete();
		
		Assert.assertNotNull(newFileID);
	}
	
	public String getFileId(String fileName, Drive googleDriveClient) 
	{
		try 
		{
			List request = googleDriveClient.files().list();
			FileList files = request.execute();
			for(File file : files.getItems()) 
			{
				if (file.getTitle().equals(fileName)) 
				{
					return file.getId();
				}
			}
		} 
		catch (IOException e)
		{
			System.out.println("An error occurred: " + e);
		}
		return null;
	}
	
}
