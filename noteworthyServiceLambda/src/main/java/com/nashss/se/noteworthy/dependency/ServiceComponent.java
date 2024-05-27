package com.nashss.se.noteworthy.dependency;

import com.nashss.se.noteworthy.activity.CreateNoteActivity;
import com.nashss.se.noteworthy.activity.GetNotesActivity;

import dagger.Component;

import javax.inject.Singleton;

/**
 * Dagger component for providing dependency injection in the Noteworthy Service.
 */
@Singleton
@Component(modules = DaoModule.class)
public interface ServiceComponent {

    /**
     * Provides the relevant activity.
     * @return GetNotesActivity
     */
    GetNotesActivity provideGetNotesActivity();

    /**
     * Provides the relevant activity.
     * @return CreateNoteActivity
     */
    CreateNoteActivity provideCreateNoteActivity();
}
