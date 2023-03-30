package com.cb.encryption;

import com.cb.property.CryptoPropertiesRaw;
import com.cb.property.CryptoPropertiesRawImpl;
import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class EncryptionProvider {
	
	private static final Charset ISO_CHARSET = StandardCharsets.ISO_8859_1;
	private static final String ENCRYPTION_ALGORITHM = "PBEWithMD5AndTripleDES";
	
	private final StandardPBEStringEncryptor encryptor;

	public static void main(String[] a) throws Exception {
		EncryptionProvider encryptionProvider = new EncryptionProvider();
		
		/*  TODO: erase string after using */ String rawString = ""; /*  TODO: erase string after using */
		
		String encryptedText = encryptionProvider.encrypt(rawString);
    	System.out.println("Encrypted: " + encryptedText);
    	String decryptedText = encryptionProvider.decrypt(encryptedText);
    	System.out.println("Decrypted (to ensure will decrypt fine in the future): " + decryptedText + " <== NOW ERASE THIS IN CODE");
	}
	
	public EncryptionProvider() throws IOException {
		CryptoPropertiesRaw properties = new CryptoPropertiesRawImpl();
		String encryptionKey = FileUtils.readFileToString(new File(properties.getEncryptionKeyFilePath()), ISO_CHARSET);
		this.encryptor = new StandardPBEStringEncryptor();
		encryptor.setPassword(encryptionKey);            
    	encryptor.setAlgorithm(ENCRYPTION_ALGORITHM);  
	}
	
	public String encrypt(String s) {
		return encryptor.encrypt(s);
	}
	
	public String decrypt(String s) {
		return encryptor.decrypt(s);
	}
	
	
	/*  
	 
	//
	// Certificate/P12 file implementation: !!!!!!!! DO NOT DELETE !!!!!!!!
	//
	 
	public EncryptionProvider(CryptoProperties properties) throws Exception {
		//Security.setProperty("crypto.policy", "unlimited"); // probably not needed for java10?
		Security.addProvider(new BouncyCastleProvider());
		CertificateFactory certFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE, CERTIFICATE_PROVIDER);
		this.certificate = (X509Certificate) certFactory.generateCertificate(new FileInputStream(properties.getCertificateFilePath()));
		String keystorePassword = properties.getKeystorePassword();
		KeyStore keystore = KeyStore.getInstance(KEYSTORE_TYPE);
		keystore.load(new FileInputStream(properties.getKeyFilePath()), keystorePassword.toCharArray());
		this.key = (PrivateKey)keystore.getKey(properties.getKeystoreAlias(), keystorePassword.toCharArray()); 
	}

	public String encrypt(String s) throws CertificateEncodingException, CMSException, IOException {
		byte[] data = s.getBytes(ISO_CHARSET);
		byte[] encryptedData = null;
		if (data != null && certificate != null) {
			CMSEnvelopedDataGenerator cmsEnvelopedDataGenerator = new CMSEnvelopedDataGenerator();
			JceKeyTransRecipientInfoGenerator jceKey = new JceKeyTransRecipientInfoGenerator(certificate);
			cmsEnvelopedDataGenerator.addRecipientInfoGenerator(jceKey);
			CMSTypedData msg = new CMSProcessableByteArray(data);
			OutputEncryptor encryptor = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC).setProvider(CERTIFICATE_PROVIDER).build();
			CMSEnvelopedData cmsEnvelopedData = cmsEnvelopedDataGenerator.generate(msg, encryptor);
			encryptedData = cmsEnvelopedData.getEncoded();
		}
		return new String(encryptedData, ISO_CHARSET);
	}

	public String decrypt(String s) throws CMSException {
		byte[] encryptedData = s.getBytes(ISO_CHARSET);
		byte[] decryptedData = null;
		if (encryptedData != null && key != null) {
			CMSEnvelopedData envelopedData = new CMSEnvelopedData(encryptedData);
			Collection<RecipientInformation> recipients = envelopedData.getRecipientInfos().getRecipients();
			KeyTransRecipientInformation recipientInfo = (KeyTransRecipientInformation) recipients.iterator().next();
			JceKeyTransRecipient recipient = new JceKeyTransEnvelopedRecipient(key);
			return new String(recipientInfo.getContent(recipient));
		}
		return new String(decryptedData, ISO_CHARSET);
	}*/

}
