import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import Header from '../components/header';
import BindingClass from "../util/bindingClass";
import DataStore from "../util/DataStore";

/**
 * Logic needed to view main page of the website displaying all notes.
 */
class ViewNotes extends BindingClass {
    constructor() {
        super();
        this.bindClassMethods(['clientLoaded', 'mount', 'displayNotesOnPage', 'createNote', 'saveNote'], this);
        this.dataStore = new DataStore();
        this.dataStore.addChangeListener(this.displayNotesOnPage);
        this.header = new Header(this.dataStore);
        console.log("viewnotes constructor");
    }

    /**
     * Once the client is loaded, get the note data.
     */
    async clientLoaded() {
        const notes = await this.client.getNotes();
        this.dataStore.set('notes', notes);
    }

    /**
     * Add the header to the page and load the NoteworthyServiceClient.
     */
    mount() {
        document.getElementById('new-note').addEventListener('click', this.createNote);
        document.getElementById('save-note').addEventListener('click', this.saveNote);

        this.header.addHeaderToPage();

        this.client = new NoteworthyServiceClient();
        this.clientLoaded();
    }

    /**
     * When the notes are updated in the datastore, update the notes metadata on the page.
     * Display notes by generating html elements for the note previews and the current note view.
     */
    async displayNotesOnPage() {
        const notes = this.dataStore.get('notes');
        const notePreviewsContainer = document.querySelector(".note-previews-container");

        if (notes == null) {
            return;
        }

        let note;
        for (note of notes) {
            notePreviewsContainer.appendChild(
                this.createNotePreviewButtonHelper(note));
        }
    }

    /**
     * Method to run when the new note button is pressed. Call the NoteworthyService to create
     * a new empty note and set as current note.
     */
    async createNote() {
        const notePreviews = document.querySelector(".note-previews-container");
        const currentNoteTitle = document.querySelector(".current-note-title");
        const currentNoteContent = document.querySelector(".current-note-content");
        
        let newTitle = "Untitled";
        let newContent = "";

        currentNoteTitle.textContent = newTitle;
        currentNoteContent.textContent = newContent;
        const newNote = await this.client.createNote(newTitle, newContent);
        const newNotePreviewButton = this.createNotePreviewButtonHelper(newNote);
        notePreviews.prepend(newNotePreviewButton);
    }

    async saveNote() {
        const currentNoteTitle = document.querySelector(".current-note-title");
        const currentNoteContent = document.querySelector(".current-note-content");
        const newNote = await this.client.createNote(currentNoteTitle.textContent, currentNoteContent.textContent);

        const notePreviewsContainer = document.querySelector(".note-previews-container");
        notePreviewsContainer.appendChild(
            this.createNotePreviewButtonHelper(newNote));
    }

    createNotePreviewButtonHelper(note) {
        let notePreviewButton = document.createElement("button");
        notePreviewButton.className = "button";
        notePreviewButton.id = "note-preview-button";
        notePreviewButton.type = "button";
        notePreviewButton.textContent = note.title;
        notePreviewButton.noteTitle = note.title;
        notePreviewButton.noteContent = note.content;

        const currentNoteTitle = document.querySelector(".current-note-title");
        const currentNoteContent = document.querySelector(".current-note-content");
        notePreviewButton.addEventListener("click", function (evt) {
            currentNoteTitle.textContent = evt.target.noteTitle;
            currentNoteContent.textContent = evt.target.noteContent;
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
