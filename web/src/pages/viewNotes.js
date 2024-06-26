import NoteworthyServiceClient from '../api/noteworthyServiceClient';
import Header from '../components/header';
import AudioRecording from '../components/audioRecording';
import BindingClass from "../util/bindingClass";
import DataStore from "../util/DataStore";
import NoteworthyUtils from "../util/noteworthyUtils";
import Autosave from "../components/autosave";

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
        document.getElementById('sort-default').addEventListener('click', this.setDefaultNoteOrder);
        document.getElementById('sort-default-reversed').addEventListener('click', this.setDefaultReversedNoteOrder);
        document.getElementById('delete-note').addEventListener('click', this.deleteNote);

        this.header.addHeaderToPage();

        this.client = new NoteworthyServiceClient();
        await this.clientLoaded();
        this.displayFirstNoteAsPrimaryNote();
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

        // Check how many notes there are. 0: show onboarding, 1: remove onboarding and show Sort By
        if (notes == null) {
            return;
        } else if (notes.length == 0) {
            // Prompt user to make first note and stop painting
            NoteworthyUtils.showOnboarding();
        } else if (notes.length >= 1) {
            // Hide onboarding and show Sort By button
            NoteworthyUtils.hideOnboarding();
            document.getElementById('note-sort-and-search').style.display = "block";
        }

        // Empties all current note previews
        const notePreviewsContainer = document.querySelector(".note-previews-container");
        notePreviewsContainer.replaceChildren();

        // Paints note preview area with all note previews
        let note;
        for (note of notes) {
            notePreviewsContainer.appendChild(NoteworthyUtils.createNotePreviewButton(note));
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

        const notes = await this.dataStore.get('notes');

        if (notes == null || notes.length == 0) {
            return;
        }

        // Set primary note elements to first note preview values
        let firstNote = notes[0];
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

        // Remove onboarding UI and show primary note container
        NoteworthyUtils.hideVoiceNoteUI();
        NoteworthyUtils.hideOnboarding();

        // add new note preview to preview area
        const newNotePreviewButton = NoteworthyUtils.createNotePreviewButton(newNote);
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
        // Show autosave button with spinner
        document.getElementById("autosave-saving").style.display = "block";


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

        // Hide autosave button with spinner
        document.getElementById("autosave-saving").style.display = "none";
    }

    /**
     * Deletes the primary note in view. After deletion, it will be removed from the note
     * preview area and the first note will now be displayed as the primary note.
     */
    async deleteNote() {
        // Show spinner
        document.getElementById("trash-can-image").style.display = "none";
        document.getElementById("deleting-spinner-container").style.display = "block";

        // Delete note from backend using primary note dateCreated
        const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
        const deletedNote = await this.client.deleteNote(primaryNoteDateCreated.textContent);

        // Delete note in datastore
        let notes = await this.dataStore.get('notes');
        notes = notes.filter(note => note.dateCreated != deletedNote.dateCreated);
        this.dataStore.set('notes', notes);

        // If last note, remove contents from primary note view
        if (notes.length == 0) {
            const primaryNoteTitle = document.querySelector(".primary-note-title");
            const primaryNoteContent = document.querySelector(".primary-note-content");
            const primaryNoteDateCreated = document.querySelector(".primary-note-date-created");
            primaryNoteTitle.textContent = null;
            primaryNoteContent.textContent = null;
            primaryNoteDateCreated.textContent = null;
        }

        // Repaint note preview area and show first note preview as primary note
        this.displayNotePreviews();
        this.displayFirstNoteAsPrimaryNote();

        // Hide spinner
        document.getElementById("deleting-spinner-container").style.display = "none";
        document.getElementById("trash-can-image").style.display = "block";
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

        // Change active selector, which is distinguished by bold font weight
        document.getElementById("sort-default-reversed").style.fontWeight = "normal";
        document.getElementById("sort-default").style.fontWeight = "bold";

        // reverse note order and update datastore
        notes.reverse();
        this.dataStore.set('notes', notes);
        this.dataStore.set('noteOrder', 'default');
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

        // Change active selector, which is distinguished by bold font weight
        document.getElementById("sort-default").style.fontWeight = "normal";
        document.getElementById("sort-default-reversed").style.fontWeight = "bold";

        // reverse note order and update datastore
        notes.reverse();
        this.dataStore.set('notes', notes);
        this.dataStore.set('noteOrder', 'default-reversed');
   }
}

/**
 * Main method to run when the page contents have loaded.
 */
const main = async () => {
    const viewNotes = new ViewNotes();
    await viewNotes.mount();

    const autosave = new Autosave(viewNotes);
};

window.addEventListener('DOMContentLoaded', main);
