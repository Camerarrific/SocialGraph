package com.camerarrific.socialgraph;

import com.camerarrific.crypto.Cypher;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * @author adam
 */


public class Session {
    String uuid = Util.UUID();
    String publicKey;
    Boolean newKey;
    
    Session(String uuid){
        
        if (uuid == null)
            uuid = Util.UUID();
        
        this.uuid = uuid;
        
        try {
        
        this.publicKey = Cypher.getPublicKey(Cypher.SESSION, uuid);
                if (this.publicKey == null){
                    Cypher.generateKeys(Cypher.SESSION, uuid);
                    this.publicKey = Cypher.getPublicKey(Cypher.SESSION, uuid);
                    this.newKey = true;
                }
                       
                } catch (NoSuchPaddingException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeyException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalBlockSizeException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            } catch (BadPaddingException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(Session.class.getName()).log(Level.SEVERE, null, ex);
            }    
    }
   
   public String encrypt(String message) throws NoSuchAlgorithmException, InvalidKeySpecException{
       return Cypher.encrypt(message, Cypher.publicKeyFromBase64(this.publicKey));
   }
    
}
