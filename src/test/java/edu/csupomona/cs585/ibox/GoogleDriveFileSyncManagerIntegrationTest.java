package edu.csupomona.cs585.ibox;
import static org.mockito.Mockito.*;

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
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import edu.csupomona.cs585.ibox.sync.FileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveServiceProvider;

public class GoogleDriveFileSyncManagerIntegrationTest 
{
	Drive googleDriveClient;
	public void initGoogleDriveServices() throws IOException 
	{
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

	final java.io.File testFile = new java.io.File("src/main/resources/Test.txt");
	final java.io.File fileToTestDelete = new java.io.File("src/main/resources/DeleteTest.txt");
	final java.io.File fileToTestUpdate = new java.io.File("src/main/resources/FileToUpdate.txt");
	
    @Test
	public void testAddFileIntegration() throws IOException
	{
    	initGoogleDriveServices();
    	FileSyncManager fileSyncManager = new GoogleDriveFileSyncManager(googleDriveClient);
		fileSyncManager.addFile(testFile);
		String newFileID = getFileId("Test.txt", googleDriveClient);
		fileSyncManager.deleteFile(testFile);
		Assert.assertNotNull(newFileID);
	}
    
    @Test
    public void testUpdateFileIntegration() throws IOException
    {
    	initGoogleDriveServices();
    	FileSyncManager fileSyncManager = new GoogleDriveFileSyncManager(googleDriveClient);
    	fileSyncManager.addFile(fileToTestUpdate);
    	String dateAdded = getFileDate("FileToUpdate.txt", googleDriveClient);
    	fileSyncManager.updateFile(fileToTestUpdate);
    	String dateUpdated = getFileDate("FileToUpdate.txt", googleDriveClient);
    	//delete the file for the next run
    	fileSyncManager.deleteFile(fileToTestUpdate);
    	Assert.assertTrue(!dateUpdated.equals(dateAdded));
    }
    
    @Test
    public void testDeleteFileIntegration() throws IOException
    {
    	initGoogleDriveServices();
    	FileSyncManager fileSyncManager = new GoogleDriveFileSyncManager(googleDriveClient);
    	fileSyncManager.addFile(fileToTestDelete);
    	int filesBeforeDelete = getNumFiles(googleDriveClient);
    	fileSyncManager.deleteFile(fileToTestDelete);
    	int filesAfterDelete = getNumFiles(googleDriveClient);
    	Assert.assertTrue(filesBeforeDelete == filesAfterDelete+1);
    }
	
    public int getNumFiles(Drive googleDriveClient) throws IOException
    {
    	List request = googleDriveClient.files().list();
    	FileList files = request.execute();
    	return(files.getItems().size());
    }
    
    public String getFileDate(String fileName, Drive googleDriveClient)
    {
    	try 
		{
			List request = googleDriveClient.files().list();
			FileList files = request.execute();
			for(File file : files.getItems()) 
			{
				if (file.getTitle().equals(fileName)) 
				{
					return file.getModifiedDate().toString();
				}
			}
		} 
		catch (IOException e)
		{
			System.out.println("An error occurred: " + e);
		}
		return null;
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
