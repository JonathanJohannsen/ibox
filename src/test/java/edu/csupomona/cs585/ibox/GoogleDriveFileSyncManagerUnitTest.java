package edu.csupomona.cs585.ibox;

import static org.mockito.Mockito.*;

import java.io.FileNotFoundException;
//import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.Drive.Files.Delete;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.Drive.Files.Insert;
import com.google.api.services.drive.Drive.Files.List;
import com.google.api.services.drive.Drive.Files.Update;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.common.util.concurrent.Service;

import edu.csupomona.cs585.ibox.sync.FileSyncManager;
import edu.csupomona.cs585.ibox.sync.GoogleDriveFileSyncManager;

public class GoogleDriveFileSyncManagerUnitTest 
{
	Drive service = mock(Drive.class);
	final Files myFiles = mock(Files.class);
	final Insert insert = mock(Insert.class);
	final Delete delete = mock(Delete.class);
	final Update update = mock(Update.class);
	final List list = mock(List.class);
	File testFile = new File();
	FileList googleFiles;

	//the FileSyncManager to perform tests on
	final FileSyncManager fileSyncManager = new GoogleDriveFileSyncManager(service);

	//the file to pass 
	final java.io.File localFile = new java.io.File("src/main/resources/Test.txt");
	
	//file that doesn't exist to test the exceptions
	final java.io.File badFile = new java.io.File("badFile.txt");
	
	@Before
    public void setup() throws IOException 
	{
		googleFiles = initGoogleFiles();
		when(service.files()).thenReturn(myFiles);
		when(myFiles.list()).thenReturn(list);
		when(list.execute()).thenReturn(googleFiles);
		when(myFiles.delete(any(String.class))).thenReturn(delete);
    }
	
	@Test
	public void testAddFile() throws IOException
	{
		when(myFiles.insert(any(File.class), any(FileContent.class))).thenReturn(insert);
		when(insert.execute()).thenReturn(testFile);

		fileSyncManager.addFile(localFile);
		verify(insert).execute();
	}
	
	@Test(expected=FileNotFoundException.class)
	public void testDeleteFileNotFoundException() throws IOException
	{
		//passes in a file that doesn't exist to verify that an exception is thrown when user
		//tries to delete a file that doesn't exist
		fileSyncManager.deleteFile(badFile);
	}

	@Test
	public void testDeleteFile() throws IOException
	{	
		fileSyncManager.deleteFile(localFile);
		verify(delete).execute();
	}
	
	@Test
	public void testUpdateFile() throws IOException
	{
		when(myFiles.update(any(String.class), any(File.class), 
				any(FileContent.class))).thenReturn(update);
		when(update.execute()).thenReturn(testFile);
		
		fileSyncManager.updateFile(localFile);
		verify(update).execute();
	}
	
	public FileList initGoogleFiles()
	{
		//creates a list with one Google File in it for testing purposes. 
		java.util.List<File> items = new java.util.ArrayList<File>();
		File googleFile = new File();
		googleFile.setTitle(localFile.getName()).setId("1");
		items.add(googleFile);
		FileList googleFiles = new FileList();
		googleFiles.setItems(items);
		return googleFiles;
	}
}
