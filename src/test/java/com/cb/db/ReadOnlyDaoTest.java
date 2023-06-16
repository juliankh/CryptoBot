package com.cb.db;

import com.cb.common.ObjectConverter;
import com.cb.model.config.ProcessConfig;
import com.cb.model.config.db.DbProcessConfig;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.util.List;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReadOnlyDaoTest {

    @Mock
    private Connection readConnection;

    @Spy
    private ObjectConverter objectConverter;

    @Mock
    private QueryRunner queryRunner;

    @InjectMocks
    private ReadOnlyDao readOnlyDao;

    @BeforeEach
    public void beforeEachTest() {
        reset(readConnection);
        reset(objectConverter);
        reset(queryRunner);
    }

    @Test
    @SneakyThrows
    public void processConfigs() {
        // setup
        DbProcessConfig dbConfig1 = new DbProcessConfig(1, "ActiveProcessToken1", null, true);
        DbProcessConfig dbConfig2 = new DbProcessConfig(2, "ActiveProcessToken2", "ActiveProcessSubtoken1", true);
        DbProcessConfig dbConfig3 = new DbProcessConfig(3, "ActiveProcessToken2", "ActiveProcessSubtoken2", true);
        DbProcessConfig dbConfig4 = new DbProcessConfig(4, "InactiveProcessToken1", null, false);
        DbProcessConfig dbConfig5 = new DbProcessConfig(5, "InactiveProcessToken2", "InactiveProcessSubtoken1", false);
        DbProcessConfig dbConfig6 = new DbProcessConfig(6, "InactiveProcessToken2", "InactiveProcessSubtoken2", false);
        List<DbProcessConfig> configs = Lists.newArrayList(dbConfig1, dbConfig2, dbConfig3, dbConfig4, dbConfig5, dbConfig6);
        when(queryRunner.query(any(Connection.class), anyString(), any(BeanListHandler.class))).thenReturn(configs);

        // engage test
        List<ProcessConfig> resultActive = readOnlyDao.processConfigs(true);
        List<ProcessConfig> resultInactive = readOnlyDao.processConfigs(false);

        // verify
        ProcessConfig config1 = new ProcessConfig().setId(1).setProcessToken("ActiveProcessToken1").setProcessSubToken(null).setActive(true);
        ProcessConfig config2 = new ProcessConfig().setId(2).setProcessToken("ActiveProcessToken2").setProcessSubToken("ActiveProcessSubtoken1").setActive(true);
        ProcessConfig config3 = new ProcessConfig().setId(3).setProcessToken("ActiveProcessToken2").setProcessSubToken("ActiveProcessSubtoken2").setActive(true);
        ProcessConfig config4 = new ProcessConfig().setId(4).setProcessToken("InactiveProcessToken1").setProcessSubToken(null).setActive(false);
        ProcessConfig config5 = new ProcessConfig().setId(5).setProcessToken("InactiveProcessToken2").setProcessSubToken("InactiveProcessSubtoken1").setActive(false);
        ProcessConfig config6 = new ProcessConfig().setId(6).setProcessToken("InactiveProcessToken2").setProcessSubToken("InactiveProcessSubtoken2").setActive(false);
        List<ProcessConfig> expectedActive = Lists.newArrayList(config1, config2, config3);
        List<ProcessConfig> expectedInactive = Lists.newArrayList(config4, config5, config6);

        assertEquals(expectedActive, resultActive);
        assertEquals(expectedInactive, resultInactive);
    }

    @Test
    @SneakyThrows
    public void activeProcessConfigMap() {
        // setup
        DbProcessConfig dbConfig1 = new DbProcessConfig(1, "ActiveProcessToken1", null, true);
        DbProcessConfig dbConfig2 = new DbProcessConfig(2, "ActiveProcessToken2", "ActiveProcessSubtoken1", true);
        DbProcessConfig dbConfig3 = new DbProcessConfig(3, "ActiveProcessToken2", "ActiveProcessSubtoken2", true);
        DbProcessConfig dbConfig4 = new DbProcessConfig(4, "InactiveProcessToken1", null, false);
        DbProcessConfig dbConfig5 = new DbProcessConfig(5, "InactiveProcessToken2", "InactiveProcessSubtoken1", false);
        DbProcessConfig dbConfig6 = new DbProcessConfig(6, "InactiveProcessToken2", "InactiveProcessSubtoken2", false);
        List<DbProcessConfig> configs = Lists.newArrayList(dbConfig1, dbConfig2, dbConfig3, dbConfig4, dbConfig5, dbConfig6);
        when(queryRunner.query(any(Connection.class), anyString(), any(BeanListHandler.class))).thenReturn(configs);

        // engage test
        TreeMap<String, List<ProcessConfig>> result = readOnlyDao.activeProcessConfigMap();

        // verify
        ProcessConfig expectedConfig1 = new ProcessConfig().setId(1).setProcessToken("ActiveProcessToken1").setProcessSubToken(null).setActive(true);
        ProcessConfig expectedConfig2 = new ProcessConfig().setId(2).setProcessToken("ActiveProcessToken2").setProcessSubToken("ActiveProcessSubtoken1").setActive(true);
        ProcessConfig expectedConfig3 = new ProcessConfig().setId(3).setProcessToken("ActiveProcessToken2").setProcessSubToken("ActiveProcessSubtoken2").setActive(true);

        assertEquals(2, result.size());
        assertEquals(Lists.newArrayList(expectedConfig1), result.get("ActiveProcessToken1"));
        assertEquals(Lists.newArrayList(expectedConfig2, expectedConfig3), result.get("ActiveProcessToken2"));
    }

}
