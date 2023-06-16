package com.cb.db;

import com.cb.common.ObjectConverter;
import com.cb.db.kraken.KrakenTableNameResolver;

import javax.inject.Inject;
import java.util.Collections;

public abstract class AbstractDao {

    @Inject
    protected ObjectConverter objectConverter;

    @Inject
    protected KrakenTableNameResolver krakenTableNameResolver;

    protected String questionMarks(int numQuestionMarks) {
        return String.join(",", Collections.nCopies(numQuestionMarks, "?"));
    }

}
