package com.cb.property;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CryptoPropertiesRawImpl implements CryptoPropertiesRaw {
	
	protected Properties properties;
		
	public CryptoPropertiesRawImpl() throws IOException {
		this.properties = new Properties();
		this.properties.load(new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("crypto.properties").getPath()));
	}
	
	@Override
	public String getMainDirectory() {
		return properties.getProperty("mainDir");
	}
	
	@Override
	public String getEncryptionKeyFilePath() {
		return getEncryptionDirectory() + properties.getProperty("encryption.keyFile");
	}
	
	private String getEncryptionDirectory() {
		return getMainDirectory() + properties.getProperty("encryption.subDir");
	}

}
