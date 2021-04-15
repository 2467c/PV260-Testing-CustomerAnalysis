package pv260.customeranalysis.interfaces;

import pv260.customeranalysis.exceptions.GeneralException;

import java.util.List;

public interface Storage {

    <T> T find(Class<T> type, long key);

    int update(Object record) throws GeneralException;

    int persist(Object record) throws GeneralException;

    int persist(List<Object> records) throws GeneralException;

}