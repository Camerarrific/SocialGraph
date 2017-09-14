/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.camerarrific.socialgraph;

import static com.camerarrific.socialgraph.api.storageConnectionString;
import com.google.gson.JsonObject;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobContainerPermissions;
import com.microsoft.azure.storage.blob.BlobContainerPublicAccessType;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.ListBlobItem;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.rmi.server.UID;
import java.security.InvalidKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.UUID;

/**
 *
 * @author adam@filterzilla.camera
 */


public class Blobs {
    
    private final static String storageAccountName = "photoblobs1";
    private final static String storageAccountKey = "mrQYPPAcRageXXnxtFD6qTordl7gMR2D+ChJn7t2alZxAvcJS13rrcB4RwJNd5Zn9boVkp0mQd3KDv6WYC2jHw==";
    private final static String blobsContainer = "photos";
    
    
    public static String uploadWithRescale (String url, int trimSize) throws IOException{
    
    return upload(LiquidRescaler.rescaleImageWithURL(url, trimSize, 1));
}

    public static String getSasKey() throws URISyntaxException, StorageException, InvalidKeyException {
    
            
                CloudStorageAccount storageAccount;

                // Use the connection string to create the storage account.
                storageAccount = CloudStorageAccount.parse(storageConnectionString);
            
                // Create the blob client.
                CloudBlobClient blobClient = storageAccount.createCloudBlobClient();

                // Create the container if it does not exist.
                // The container name must be lower case.
                CloudBlobContainer container = blobClient.getContainerReference(blobsContainer);
                container.createIfNotExists();
            
                // Create a shared access policy.
                SharedAccessBlobPolicy policy = new SharedAccessBlobPolicy();
            
                // Create a UTC Gregorian calendar value.
                GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
            
                // Specify the current time as the start time for the shared access signature.
                calendar.setTime(new Date());
                policy.setSharedAccessStartTime(calendar.getTime());
            
                // Use the start time + 1 hour as the end time for the shared access signature. 
                calendar.add(Calendar.MINUTE, 5);
                policy.setSharedAccessExpiryTime(calendar.getTime());
            
                // Set READ and WRITE permissions.
                policy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.READ));
                  
                // Create the container permissions. 
                BlobContainerPermissions containerPermissions = new BlobContainerPermissions();
            
                // Turn off public access.
                containerPermissions.setPublicAccess(BlobContainerPublicAccessType.OFF);
            
                // Set the policy using the values set above.
                containerPermissions.getSharedAccessPolicies().put("mypolicy", policy);
                container.uploadPermissions(containerPermissions);
            
                String blob = Util.UUID();
                String uri = "https://" + storageAccountName + ".blob.core.windows.net/" + blobsContainer + '/' + blob + "?";
                // Create a shared access signature for the container.
                String sas = uri + container.generateSharedAccessSignature(policy, null);
                
                JsonObject rootObj = new JsonObject();
                
                JsonObject blobObj = new JsonObject();
                           blobObj.addProperty("uuid", blob);
                           blobObj.addProperty("url", uri);
                
                JsonObject permissionsObj = new JsonObject();
                           permissionsObj.addProperty("saskey", sas);
                           permissionsObj.addProperty("readaccess", "TRUE");
                           permissionsObj.addProperty("writeaccess", "FALSE");
                
                rootObj.add("blob", blobObj);
                rootObj.add("permissions", permissionsObj);
                return rootObj.toString();
}
    
public static String upload (byte[] bytes) {
            
                try {
                    
                    
                    // String mimeType = request.headers("mimetype");
                    String FileUUID = Util.UUID();
                    String fileName = FileUUID + ".jpg";
                    
                    // Retrieve storage account from connection-string.
                    CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
                    
                    // Create the blob client.
                    CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
                    
                    // Retrieve reference to a previously created container.
                    CloudBlobContainer container = blobClient.getContainerReference("photos");
                    String blobURL = "https://" + storageAccountName + ".blob.core.windows.net/" + blobsContainer + '/' + fileName;
                    
                    CloudBlockBlob blob = container.getBlockBlobReference(fileName);
                    blob.uploadFromByteArray(bytes, 0, bytes.length);
                    return blobURL;
                }
                catch (Exception e) {
                    // Output the stack trace.
                    e.printStackTrace();
                }
          return null;
}

public static void download (String filename, OutputStream outputStream) {
    try {
        // Retrieve storage account from connection-string.
        CloudStorageAccount storageAccount = CloudStorageAccount.parse(storageConnectionString);
        // Create the blob client.
        CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
        
        // Retrieve reference to a previously created container.
        CloudBlobContainer container = blobClient.getContainerReference("photos");
        
        // Loop through each blob item in the container.
        for (ListBlobItem blobItem : container.listBlobs()) {
            // If the item is a blob, not a virtual directory.
            if (blobItem instanceof CloudBlob) {
                // Download the item and save it to a file with the same name.
                CloudBlob blob = (CloudBlob) blobItem;
                blob.download(outputStream);
            }
        }
    }
    catch (Exception e)
    {
        // Output the stack trace.
        e.printStackTrace();
    }
}
}
