package com.cb.property;

import lombok.SneakyThrows;

import java.io.FileInputStream;
import java.util.Properties;

public class CryptoPropertiesRaw {
	
	protected Properties properties;
		
	@SneakyThrows
	public CryptoPropertiesRaw() {
		this.properties = new Properties();
		this.properties.load(new FileInputStream(Thread.currentThread().getContextClassLoader().getResource("crypto.properties").getPath()));
	}

	public String getMainDirectory() {
		return properties.getProperty("mainDir");
	}

	public String getEncryptionKeyFilePath() {
		return getEncryptionDirectory() + properties.getProperty("encryption.keyFile");
	}

	private String getEncryptionDirectory() {
		return getMainDirectory() + properties.getProperty("encryption.subDir");
	}

}
