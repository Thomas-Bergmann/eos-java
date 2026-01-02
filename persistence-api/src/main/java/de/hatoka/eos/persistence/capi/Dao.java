package de.hatoka.eos.persistence.capi;

/**
 * Generic access methods for storage
 */
public interface Dao<KEY, VALUE>
{
    /**
     * Creates or updates data
     *
     * @param key key of data
     * @param data values of data
     */
    void update(KEY key, VALUE data);

    /**
     * Deletes data
     *
     * @param key key of data
     */
    void delete(KEY key);

    /**
     * Retrieves data
     *
     * @param key key of data
     * @return values of data or null if data not exists
     */
    VALUE get(KEY key);
}