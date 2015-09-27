import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Locale;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.core.DbxAccountInfo;
import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxHost;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import com.dropbox.core.DbxWriteMode;
import com.dropbox.core.http.HttpRequestor;
import com.dropbox.core.http.StandardHttpRequestor;

public class FileUpload
{

	private static final String DROP_BOX_APP_KEY = "3wywjt2ldfhnsb5";
	private static final String DROP_BOX_APP_SECRET = "jk2if1p5c2w86d3";
	DbxClient dbxClient;

	public DbxClient authDropbox(String dropBoxAppKey, String dropBoxAppSecret)
			throws IOException, DbxException
	{
		DbxAppInfo dbxAppInfo = new DbxAppInfo(dropBoxAppKey, dropBoxAppSecret);
		InetSocketAddress addr = new InetSocketAddress("10.3.100.207", 8080);
		Proxy proxy = new Proxy(Proxy.Type.HTTP, addr);
		StandardHttpRequestor proxyRequest = new StandardHttpRequestor(proxy);
		DbxRequestConfig dbxRequestConfig = new DbxRequestConfig(
				"JavaDropboxTutorial/1.0", Locale.getDefault().toString(), proxyRequest);
		DbxWebAuthNoRedirect dbxWebAuthNoRedirect = new DbxWebAuthNoRedirect(
				dbxRequestConfig, dbxAppInfo);
		String authorizeUrl = dbxWebAuthNoRedirect.start();
		System.out.println("1. Authorize: Go to URL and click Allow : "
				+ authorizeUrl);
		System.out
				.println("2. Auth Code: Copy authorization code and input here ");
		String dropboxAuthCode = new BufferedReader(new InputStreamReader(
				System.in)).readLine().trim();
		DbxAuthFinish authFinish = dbxWebAuthNoRedirect.finish(dropboxAuthCode);
		String authAccessToken = authFinish.accessToken;
		dbxClient = new DbxClient(dbxRequestConfig, authAccessToken);
		System.out.println("Dropbox Account Name: "
				+ dbxClient.getAccountInfo().displayName);
		
		/*************************************************************************************************************/
//		AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
//		DropboxAPI mDBApi;
//		AppKeyPair appKeys = new AppKeyPair(DROP_BOX_APP_KEY, DROP_BOX_APP_SECRET);
//		WebAuthSession session = new WebAuthSession(appKeys, ACCESS_TYPE);
//		mDBApi = new DropboxAPI(session);
//		System.out.println("Please go to this URL and hit \"Allow\": " + mDBApi.getSession().getAuthInfo().url); // tell user to go to app allowance URL
//		AccessTokenPair tokenPair = mDBApi.getSession().getAccessTokenPair();
//		// wait for user to allow app in above URL, 
//		// then return back to executing code below
//		RequestTokenPair tokens = new RequestTokenPair(tokenPair.key, tokenPair.secret);
//		((WebAuthSession) mDBApi.getSession()).retrieveWebAccessToken(tokens); // completes initial auth
//
//		//these two calls will retrive access tokens for future use
//		String Key = session.getAccessTokenPair().key;    // store String returned by this call somewhere
//		String Secret = session.getAccessTokenPair().secret; // same for this line
		/*************************************************************************************************************************/
		
		return dbxClient;
	}

	/* returns Dropbox size in GB */
	public long getDropboxSize() throws DbxException
	{
		long dropboxSize = 0;
		DbxAccountInfo dbxAccountInfo = dbxClient.getAccountInfo();
		// in GB :)
		dropboxSize = dbxAccountInfo.quota.total / 1024 / 1024 / 1024;
		return dropboxSize;
	}

	public void uploadToDropbox(String fileName) throws DbxException,
			IOException
	{
		File inputFile = new File(fileName);
		FileInputStream fis = new FileInputStream(inputFile);
		try
		{
			DbxEntry.File uploadedFile = dbxClient.uploadFile("/" + fileName,
					DbxWriteMode.add(), inputFile.length(), fis);
			String sharedUrl = dbxClient.createShareableUrl("/" + fileName);
			System.out.println("Uploaded: " + uploadedFile.toString() + " URL "
					+ sharedUrl);
		}
		finally
		{
			fis.close();
		}
	}

	public void createFolder(String folderName) throws DbxException
	{
		dbxClient.createFolder("/" + folderName);
	}

	public void listDropboxFolders(String folderPath) throws DbxException
	{
		DbxEntry.WithChildren listing = dbxClient
				.getMetadataWithChildren(folderPath);
		System.out.println("Files List:");
		for (DbxEntry child : listing.children)
		{
			System.out.println("	" + child.name + ": " + child.toString());
		}
	}

	public void downloadFromDropbox(String fileName) throws DbxException,
			IOException
	{
		FileOutputStream outputStream = new FileOutputStream(fileName);
		try
		{
			DbxEntry.File downloadedFile = dbxClient.getFile("/" + fileName,
					null, outputStream);
			System.out.println("Metadata: " + downloadedFile.toString());
		}
		finally
		{
			outputStream.close();
		}
	}

	public void fileUploadUtil() throws IOException, DbxException
	{
		FileUpload javaDropbox = new FileUpload();
		javaDropbox.authDropbox(DROP_BOX_APP_KEY, DROP_BOX_APP_SECRET);
		javaDropbox.createFolder("tutorial");
		javaDropbox.uploadToDropbox("test.jpg");
	}
}