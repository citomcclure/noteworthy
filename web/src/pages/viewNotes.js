import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import Header from '../components/header';
import AudioRecording from '../components/audioRecording';
import BindingClass from "../util/bindingClass";
import DataStore from "../util/DataStore";

/**
 * Logic needed to view main page of the website displaying all notes.
 */
// TODO: change name of class and file
class ViewNotes extends BindingClass {
    constructor() {
        super();
        this.bindClassMethods(['clientLoaded', 'mount', 'displayNotePreviews', 'displayFirstNoteAsPrimaryNote',
                                'createNote', 'updateNote', 'deleteNote',
                                'setDefaultNoteOrder', 'setDefaultReversedNoteOrder'
        ], this);
        this.dataStore = new DataStore();
        this.dataStore.addChangeListener(this.displayNotePreviews);
        this.header = new Header(this.dataStore);
        this.audioRecording = new AudioRecording(this.dataStore);
    }

    /**
     * Add the header to the page and load the NoteworthyServiceClient.
     */
    async mount() {
        document.getElementById('new-note').addEventListener('click', this.createNote);
        document.getElementById('save-note').addEventListener('click', this.updateNote);
        document.getElementById('delete-note').addEventListener('click', this.deleteNote);
        document.getElementById('sort-default').addEventListener('click', this.setDefaultNoteOrder);
        document.getElementById('sort-default-reversed').addEventListener('click', this.setDefaultReversedNoteOrder);

        this.header.addHeaderToPage();

        this.client = new NoteworthyServiceClient();
        await this.clientLoaded();
        this.displayFirstNoteAsPrimaryNote();
        this.audioRecording.transcribeAudio();
    }

    /**
     * Once the client is loaded, get the note data.
     */
        async clientLoaded() {
            const notes = await this.client.getNotes();
            this.dataStore.set('notes', notes);
            this.dataStore.set('noteOrder', "default");
        }

    /**
     * When the notes are updated in the datastore, update the notes metadata on the page.
     * Display note preview by generating button elements for each note.
     */
    async displayNotePreviews() {
        const notes = await this.dataStore.get('notes');

        if (notes == null) {
            return;
        }

        // Empties all current note previews
        const notePreviewsContainer = document.querySelector(".note-previews-container");
        notePreviewsContainer.replaceChildren();

        // Paints note preview area with all note previews
        let note;
        for (note of notes) {
            notePreviewsContainer.appendChild(this.createNotePreviewButtonHelper(note));
        }
    }

    /**
     * Takes the first note preview and displays it as the primary note.
     */
    async displayFirstNoteAsPrimaryNote() {
        // Get html elements for primary note
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");

        // Get first note preview if there is one
        const notes = await this.dataStore.get('notes');
        if (notes == null) {
            return;
        }
        let firstNote = notes[0];

        // Set primary note elements to first note preview values
        primaryNoteTitle.textContent = firstNote.title;
        primaryNoteContent.textContent = firstNote.content;
        primaryNoteDateCreated.textContent = firstNote.dateCreated;
    }

    /**
     * Method to run when the new note button is pressed. Creates a new empty note,
     * saves it on the backend, and displays as new preview and primary note.
     */
    async createNote() {
        // Create new note in database with generic values
        let newTitle = "Untitled";
        let newContent = "";
        const newNote = await this.client.createNote(newTitle, newContent);
        
        // Set primary note view to new note values
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
        primaryNoteTitle.textContent = newTitle;
        primaryNoteContent.textContent = newContent;
        primaryNoteDateCreated.textContent = newNote.dateCreated;
    
        // add new note preview to preview area
        const newNotePreviewButton = this.createNotePreviewButtonHelper(newNote);
        const notePreviews = document.querySelector(".note-previews-container");
        notePreviews.prepend(newNotePreviewButton);

        // Add new note in datastore
        const notes = await this.dataStore.get('notes');
        notes.unshift(newNote);
        this.dataStore.set('notes', notes);
    }

    /**
     * Update a note's title and content. The last updated date will be updated to now on the backend.
     * The note preview will reflect the new title, if any.
     */
    async updateNote() {
        // Update note using primary note values
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
        const updatedNote = await this.client.updateNote(primaryNoteTitle.textContent, primaryNoteContent.textContent, primaryNoteDateCreated.textContent);

        // Find note in datastore and update with its new values
        const notes = await this.dataStore.get('notes');
        let note;
        for (note of notes) {
            if (note.dateCreated == updatedNote.dateCreated) {
                note.title = updatedNote.title;
                note.content = updatedNote.content;
                break;
            }
        }

        // Repaint note preview area
        this.displayNotePreviews();
    }

    /**
     * Deletes the primary note in view. After deletion, it will be removed from the note
     * preview area and the first note will now be displayed as the primary note.
     */
    async deleteNote() {
        // Delete note from backend using primary note dateCreated
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
        const deletedNote = await this.client.deleteNote(primaryNoteDateCreated.textContent);

        // Delete note in datastore
        let notes = await this.dataStore.get('notes');
        notes = notes.filter(note => note.dateCreated != deletedNote.dateCreated);
        this.dataStore.set('notes', notes);

        // Repaint note preview area and show first note preview as primary note
        this.displayNotePreviews();
        this.displayFirstNoteAsPrimaryNote();
    }

    /**
     * If the note order is not already set to default, reverses the note previews order 
     * and updates the datastore for notes and noteOrder.
     * This implementation does not make use of backend to order notes.
     */
    async setDefaultNoteOrder() {
        const notes = await this.dataStore.get('notes');
        const noteOrder = await this.dataStore.get('noteOrder');

        // If default order is currently shown, do nothing
        if (noteOrder == 'default') {
            return;
        }

        // reverse note order and update datastore
        notes.reverse();
        this.dataStore.set('notes', notes);
        this.dataStore.set('noteOrder', 'default')
    }

    /**
    * If the note order is not already set to reverse order, reverses the note previews order 
    * and updates the datastore for notes and noteOrder.
    * This implementation does not make use of backend to order notes.
    */
   async setDefaultReversedNoteOrder() {
       const notes = await this.dataStore.get('notes');
       const noteOrder = await this.dataStore.get('noteOrder');

       // If deafult reversed order is currently shown, do nothing
       if (noteOrder == 'default-reversed') {
           return;
       }

       // reverse note order and update datastore
       notes.reverse();
       this.dataStore.set('notes', notes);
       this.dataStore.set('noteOrder', 'default-reversed')
   }

    /**
     * Helper class to generate the note preview button for the note preview area.
     * @param {*} note the note a button is being made for.
     * @returns the note preview button with attached event listener.
     */
    createNotePreviewButtonHelper(note) {
        // Create button element, set button text to note title, and set values
        let notePreviewButton = document.createElement("button");
        notePreviewButton.className = "button";
        notePreviewButton.id = "note-preview-button";
        notePreviewButton.type = "button";
        notePreviewButton.textContent = note.title;
        notePreviewButton.noteTitle = note.title;
        notePreviewButton.noteContent = note.content;
        notePreviewButton.noteDateCreated = note.dateCreated;

        // Create listener. When note preview is clicked, update primary note with note values
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
        notePreviewButton.addEventListener("click", function (evt) {
            primaryNoteTitle.textContent = evt.target.noteTitle;
            primaryNoteContent.textContent = evt.target.noteContent;
            primaryNoteDateCreated.textContent = evt.target.noteDateCreated;
        });

        return notePreviewButton;
    }
}

/**
 * Main method to run when the page contents have loaded.
 */
const main = async () => {
    const viewNotes = new ViewNotes();
    viewNotes.mount();
};

window.addEventListener('DOMContentLoaded', main);
