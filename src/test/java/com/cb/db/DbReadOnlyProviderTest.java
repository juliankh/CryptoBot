package com.cb.db;

import com.cb.common.ObjectConverter;
import com.cb.model.config.SafetyNetConfig;
import com.cb.model.config.db.DbSafetyNetConfig;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.Connection;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DbReadOnlyProviderTest {

    @Mock
    private Connection readConnection;

    @Spy
    private ObjectConverter objectConverter;

    @Mock
    private QueryRunner queryRunner;

    @InjectMocks
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Before
    public void beforeEachTest() {
        reset(readConnection);
        reset(objectConverter);
        reset(queryRunner);
    }

    @Test
    @SneakyThrows
    public void safetyNetConfig() {
        // setup
        DbSafetyNetConfig dbConfig1 = new DbSafetyNetConfig(1, "ActiveProcessToken1", null, true);
        DbSafetyNetConfig dbConfig2 = new DbSafetyNetConfig(2, "ActiveProcessToken2", "ActiveProcessSubtoken1", true);
        DbSafetyNetConfig dbConfig3 = new DbSafetyNetConfig(3, "ActiveProcessToken2", "ActiveProcessSubtoken2", true);
        DbSafetyNetConfig dbConfig4 = new DbSafetyNetConfig(4, "InactiveProcessToken1", null, false);
        DbSafetyNetConfig dbConfig5 = new DbSafetyNetConfig(5, "InactiveProcessToken2", "InactiveProcessSubtoken1", false);
        DbSafetyNetConfig dbConfig6 = new DbSafetyNetConfig(6, "InactiveProcessToken2", "InactiveProcessSubtoken2", false);
        List<DbSafetyNetConfig> configs = Lists.newArrayList(dbConfig1, dbConfig2, dbConfig3, dbConfig4, dbConfig5, dbConfig6);
        when(queryRunner.query(any(Connection.class), anyString(), any(BeanListHandler.class))).thenReturn(configs);

        // engage test
        List<SafetyNetConfig> resultActive = dbReadOnlyProvider.safetyNetConfig(true);
        List<SafetyNetConfig> resultInactive = dbReadOnlyProvider.safetyNetConfig(false);

        // verify
        SafetyNetConfig config1 = new SafetyNetConfig().setId(1).setProcessToken("ActiveProcessToken1").setProcessSubToken(null).setActive(true);
        SafetyNetConfig config2 = new SafetyNetConfig().setId(2).setProcessToken("ActiveProcessToken2").setProcessSubToken("ActiveProcessSubtoken1").setActive(true);
        SafetyNetConfig config3 = new SafetyNetConfig().setId(3).setProcessToken("ActiveProcessToken2").setProcessSubToken("ActiveProcessSubtoken2").setActive(true);
        SafetyNetConfig config4 = new SafetyNetConfig().setId(4).setProcessToken("InactiveProcessToken1").setProcessSubToken(null).setActive(false);
        SafetyNetConfig config5 = new SafetyNetConfig().setId(5).setProcessToken("InactiveProcessToken2").setProcessSubToken("InactiveProcessSubtoken1").setActive(false);
        SafetyNetConfig config6 = new SafetyNetConfig().setId(6).setProcessToken("InactiveProcessToken2").setProcessSubToken("InactiveProcessSubtoken2").setActive(false);
        List<SafetyNetConfig> expectedActive = Lists.newArrayList(config1, config2, config3);
        List<SafetyNetConfig> expectedInactive = Lists.newArrayList(config4, config5, config6);

        assertEquals(expectedActive, resultActive);
        assertEquals(expectedInactive, resultInactive);
    }

    @Test
    @SneakyThrows
    public void activeSafetyNetConfigMap() {
        // setup
        DbSafetyNetConfig dbConfig1 = new DbSafetyNetConfig(1, "ActiveProcessToken1", null, true);
        DbSafetyNetConfig dbConfig2 = new DbSafetyNetConfig(2, "ActiveProcessToken2", "ActiveProcessSubtoken1", true);
        DbSafetyNetConfig dbConfig3 = new DbSafetyNetConfig(3, "ActiveProcessToken2", "ActiveProcessSubtoken2", true);
        DbSafetyNetConfig dbConfig4 = new DbSafetyNetConfig(4, "InactiveProcessToken1", null, false);
        DbSafetyNetConfig dbConfig5 = new DbSafetyNetConfig(5, "InactiveProcessToken2", "InactiveProcessSubtoken1", false);
        DbSafetyNetConfig dbConfig6 = new DbSafetyNetConfig(6, "InactiveProcessToken2", "InactiveProcessSubtoken2", false);
        List<DbSafetyNetConfig> configs = Lists.newArrayList(dbConfig1, dbConfig2, dbConfig3, dbConfig4, dbConfig5, dbConfig6);
        when(queryRunner.query(any(Connection.class), anyString(), any(BeanListHandler.class))).thenReturn(configs);

        // engage test
        TreeMap<String, TreeSet<String>> result = dbReadOnlyProvider.activeSafetyNetConfigMap();

        // verify
        assertEquals(2, result.size());
        assertEquals(Collections.emptySet(), result.get("ActiveProcessToken1"));
        assertEquals(Sets.newTreeSet(Arrays.asList("ActiveProcessSubtoken1", "ActiveProcessSubtoken2")), result.get("ActiveProcessToken2"));
    }

}
