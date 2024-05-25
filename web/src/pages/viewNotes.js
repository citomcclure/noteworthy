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
        this.bindClassMethods(['clientLoaded', 'mount', 'displayNotesOnPage', 'createNote'], this);
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

        this.header.addHeaderToPage();

        this.client = new NoteworthyServiceClient();
        this.clientLoaded();
    }

    /**
     * When the notes are updated in the datastore, update the notes metadata on the page.
     * Display notes by generating html elements for the note previews and the current note view.
     */
    displayNotesOnPage() {
        const notes = this.dataStore.get('notes');
        const notePreviewsContainer = document.querySelector(".note-previews-container");
        const currentNoteContainer = document.querySelector(".current-note-container");

        if (notes == null) {
            return;
        }
        
        let firstNote = true;

        let note;
        for (note of notes) {
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

            notePreviewsContainer.appendChild(notePreviewButton);

            // if (firstNote === true) {
            //     firstNote = false;
            //     currentNoteTitle.textContent = 
            // }
        }


    }

    /**
     * Method to run when the new note button is pressed. Call the NoteworthyService to create
     * a new empty note and set as current note.
     */
    async createNote() {
        alert("not implemented yet!")
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
