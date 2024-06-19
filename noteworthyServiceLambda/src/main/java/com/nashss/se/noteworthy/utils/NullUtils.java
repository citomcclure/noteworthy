package com.nashss.se.noteworthy.utils;

import java.util.function.Supplier;

/**
 * Various utilities to deal with null.
 */
public class NullUtils {
    private NullUtils() { }

    /**
     * If obj is null, return valIfNull, otherwise return obj.
     * @param obj The object to check for null.
     * @param valIfNull The value to return if obj is null.
     * @param <T> The type of obj and valIfNull.
     * @return obj or valIfNull.
     */
    public static <T> T ifNull(T obj, T valIfNull) {
        return obj != null ? obj : valIfNull;
    }

    /**
     * If obj is null, return value supplied by valIfNullSupplier.
     * @param obj The object to check for null.
     * @param valIfNullSupplier The supplier of the value to return if obj is null.
     * @param <T> The type of obj and the supplier.
     * @return obj or value returned by valIfNullSupplier.
     */
    public static <T> T ifNull(T obj, Supplier<T> valIfNullSupplier) {
        return obj != null ? obj : valIfNullSupplier.get();
    }
}
