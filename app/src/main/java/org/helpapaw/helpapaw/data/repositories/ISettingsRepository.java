package org.helpapaw.helpapaw.data.repositories;

public interface ISettingsRepository {
    void saveRadius(int radius);

    void saveTimeout(int timeout);

    int getRadius();

    int getTimeout();
}
