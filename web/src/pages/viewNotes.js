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
        this.bindClassMethods(['clientLoaded', 'mount', 'displayNotePreviews', 'createNote', 'updateNote'], this);
        this.dataStore = new DataStore();
        this.dataStore.addChangeListener(this.displayNotePreviews);
        this.header = new Header(this.dataStore);
        console.log("viewnotes constructor");
    }

    /**
     * Add the header to the page and load the NoteworthyServiceClient.
     */
    mount() {
        document.getElementById('new-note').addEventListener('click', this.createNote);
        document.getElementById('save-note').addEventListener('click', this.updateNote);

        this.header.addHeaderToPage();

        this.client = new NoteworthyServiceClient();
        this.clientLoaded();
    }

    /**
     * Once the client is loaded, get the note data.
     */
        async clientLoaded() {
            const notes = await this.client.getNotes();
            this.dataStore.set('notes', notes);
        }

    /**
     * When the notes are updated in the datastore, update the notes metadata on the page.
     * Display note preview by generating button elements for each note.
     */
    async displayNotePreviews() {
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

    // async displayPrimaryNote() {
    //     const primaryNoteTitle = document.querySelector(".primary-note-title");
    //     const primaryNoteContent = document.querySelector(".primary-note-content");
        
    //     const newNote = await this.client.createNote(primaryNoteTitle.textContent, primaryNoteContent.textContent);

    //     const notePreviewsContainer = document.querySelector(".note-previews-container");
    //     notePreviewsContainer.appendChild(
    //         this.createNotePreviewButtonHelper(newNote));
    // }

    /**
     * Method to run when the new note button is pressed. Creates a new empty note,
     * saves it on the backend, and displays as new preview and primary note.
     */
    async createNote() {
        let newTitle = "Untitled";
        let newContent = "";
        const newNote = await this.client.createNote(newTitle, newContent);
        
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        primaryNoteTitle.textContent = newTitle;
        primaryNoteContent.textContent = newContent;
        
        const newNotePreviewButton = this.createNotePreviewButtonHelper(newNote);

        // TODO: update datastore and repaint the note preview area
        const notePreviews = document.querySelector(".note-previews-container");
        notePreviews.prepend(newNotePreviewButton);
    }

    async updateNote() {
        const primaryNoteTitle = document.querySelector(".primary-note-title");
        const primaryNoteContent = document.querySelector(".primary-note-content");
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");

        const updatedNote = await this.client.updateNote(primaryNoteTitle.textContent, primaryNoteContent.textContent, primaryNoteDateCreated.textContent);
        // TODO: also update datastore here (in case title updated for previews and so clicking previews shows right info)
        const updatedNotePreviewButton = this.createNotePreviewButtonHelper(updatedNote);
    }

    createNotePreviewButtonHelper(note) {
        let notePreviewButton = document.createElement("button");
        notePreviewButton.className = "button";
        notePreviewButton.id = "note-preview-button";
        notePreviewButton.type = "button";
        notePreviewButton.textContent = note.title;
        notePreviewButton.noteTitle = note.title;
        notePreviewButton.noteContent = note.content;
        notePreviewButton.noteDateCreated = note.dateCreated;

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
