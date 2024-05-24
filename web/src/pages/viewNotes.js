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
        this.dataStore.addChangeListener(this.createNote);
        this.header = new Header(this.dataStore);
        console.log("viewnotes constructor");
    }

    /**
     * Once the client is loaded, get the playlist metadata and song list.
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
     * When the songs are updated in the datastore, update the list of songs on the page.
     */
    displayNotesOnPage() {
        const notes = this.dataStore.get('notes')

        if (notes == null) {
            return;
        }

        let noteHtml = '';
        let note;
        for (note of notes) {
            noteHtml += `
                <li class="song">
                    <span class="title">${note.title}</span>
                    <span class="content">${note.content}</span>
                </li>
            `;
        }
        document.getElementById('notes').innerHTML = noteHtml;
    }

    /**
     * Method to run when the add song playlist submit button is pressed. Call the NoteworthyService to add a song to the
     * playlist.
     */
    async createNote() {

        // const errorMessageDisplay = document.getElementById('error-message');
        // errorMessageDisplay.innerText = ``;
        // errorMessageDisplay.classList.add('hidden');

        // const playlist = this.dataStore.get('playlist');
        // if (playlist == null) {
        //     return;
        // }

        // document.getElementById('add-song').innerText = 'Adding...';
        // const asin = document.getElementById('album-asin').value;
        // const trackNumber = document.getElementById('track-number').value;
        // const playlistId = playlist.id;

        // const songList = await this.client.addSongToPlaylist(playlistId, asin, trackNumber, (error) => {
        //     errorMessageDisplay.innerText = `Error: ${error.message}`;
        //     errorMessageDisplay.classList.remove('hidden');
        // });

        // this.dataStore.set('songs', songList);

        // document.getElementById('add-song').innerText = 'Add Song';
        // document.getElementById("add-song-form").reset();
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
