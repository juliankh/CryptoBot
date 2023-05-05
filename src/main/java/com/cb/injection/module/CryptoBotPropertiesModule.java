package com.cb.injection.module;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

import static com.cb.injection.BindingName.*;

@Slf4j
public class CryptoBotPropertiesModule extends AbstractModule {

    public static Injector INJECTOR = Guice.createInjector(new CryptoBotPropertiesModule());

    @Override
    protected void configure() {
        Names.bindProperties(binder(), unencryptedProperties());
    }

    @Provides
    public Properties encryptedProperties() {
        return properties("encrypted.properties");
    }

    @Provides
    @SneakyThrows
    public StandardPBEStringEncryptor encryptor(@Named(MAIN_DIR) String mainDir, @Named(ENCRYPTION_SUBDIR) String encryptionSubDir, @Named(ENCRYPTION_KEY_FILE) String encryptionKeyFile) {
        String encryptionKeyFilePath = mainDir + encryptionSubDir + encryptionKeyFile;
        String encryptionKey = FileUtils.readFileToString(new File(encryptionKeyFilePath), StandardCharsets.ISO_8859_1);
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(encryptionKey);
        encryptor.setAlgorithm("PBEWithMD5AndTripleDES");
        return encryptor;
    }

    // ------------------------ PRIVATE METHODS ------------------------

    private Properties unencryptedProperties() {
        return properties("unencrypted.properties");
    }

    @SneakyThrows
    private Properties properties(String fileName) {
        Properties properties = new Properties();
        properties.load(new FileInputStream("property/" + fileName));
        return properties;
    }

}
