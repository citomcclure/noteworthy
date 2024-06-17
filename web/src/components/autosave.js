export default class Autosave {
    constructor(viewNotes) {
        function debounce(func, timeout = 3000){
            let timer;
            return (...args) => {
              clearTimeout(timer);
              timer = setTimeout(() => { func.apply(this, args); }, timeout);
            };
        }

        function saveInput() {
            viewNotes.updateNote();
        }

        const processChanges = debounce(() => saveInput());
        document.getElementById('primary-note-content').addEventListener('input', processChanges);
    }
}