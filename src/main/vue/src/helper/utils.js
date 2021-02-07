import Axios from "axios";
import M from "materialize-css";

export const TEXT_EDITOR_CONFIG = {
  semantic: false,
  lang: "de",
  btns: [
    ["strong", "em", "underline", "del"],
    ["foreColor", "backColor"],
    ["formatting", "fontsize"],
    ["superscript", "subscript"],
    ["link"],
    ["table"],
    ["justifyLeft", "justifyCenter", "justifyRight", "justifyFull"],
    ["orderedList"],
    ["horizontalRule"],
    ["removeformat"],
    ["viewHTML"],
    ["fullscreen"],
  ],
  plugins: {
    fontsize: {
      sizeList: ["10pt", "11pt", "12pt", "14pt", "16pt", "18pt", "24pt"],
      allowCustomSize: false,
    },
  },
};

export function initTooltips() {
  M.Tooltip.init(document.querySelectorAll(".tooltipped"), {});
}

export function storageReadable(size) {
  if (size < 1024) return size + " B";
  else if (size < 1024 * 1024) return Math.round(size / 1024) + " KB";
  else if (size < 1024 * 1024 * 1024)
    return Math.round(size / (1024 * 1024)) + " MB";
  else return Math.round(size / (1024 * 1024 * 1024)) + " GB";
}

export function uploadMultipleFiles(
  url,
  files,
  { params, uploaded, finished },
  index = 0
) {
  var infoStart =
    files.length === 1
      ? "Hochladen (0%)"
      : "[" + (index + 1) + "/" + files.length + "] [0%]" + files[index].name;
  showLoading(infoStart);
  var data = new FormData();
  data.append("file", files[index]);
  for (var key in params) {
    data.append(key, params[key]);
  }
  var config = {
    onUploadProgress: function(progressEvent) {
      var percentCompleted = Math.round(
        (progressEvent.loaded * 100) / progressEvent.total
      );
      var infoProcess =
        files.length === 1
          ? "Hochladen (" + percentCompleted + "%)"
          : "[" +
            (index + 1) +
            "/" +
            files.length +
            "] [" +
            percentCompleted +
            "%] " +
            files[index].name;
      showLoading(infoProcess, percentCompleted);
    },
  };
  Axios.post(url, data, config)
    .then((res) => {
      uploaded(res.data);
      if (index < files.length - 1)
        uploadMultipleFiles(
          url,
          files,
          { params, uploaded, finished },
          index + 1
        );
      else {
        if (files.length === 1) M.toast({ html: "Datei hochgeladen." });
        else M.toast({ html: "Dateien hochgeladen." });
        hideLoading(); // finished successfully
        finished();
      }
    })
    .catch(function(err) {
      if (err.response) {
        switch (err.response.status) {
          case 409:
            M.toast({ html: "Nicht genügend Speicherplatz." });
            break;
          case 423:
            M.toast({ html: "Cloud vorübergehend gesperrt." });
            break;
          default:
            M.toast({ html: "Ein Fehler ist aufgetreten." });
        }
      } else {
        M.toast({ html: "Ein Fehler ist aufgetreten." });
      }
      console.log(err);
      hideLoading(); // frontend error
    });
}

export function showLoading(text = "Verarbeitung...", percent = null) {
  document.getElementById("modal-loading-text").innerHTML = text;
  document.getElementById("modal-loading").style.display = "block";

  if (percent) {
    document.getElementById("modal-loading-bar").className = "determinate";
    document.getElementById("modal-loading-bar").style.width = percent + "%";
  } else {
    document.getElementById("modal-loading-bar").className = "indeterminate";
    document.getElementById("modal-loading-bar").style.width = "auto";
  }
}

export function showLoadingInvisible() {
  document.getElementById("modal-loading-invisible").style.display = "block";
}

export function hideLoading() {
  document.getElementById("modal-loading").style.display = "none";
  document.getElementById("modal-loading-invisible").style.display = "none";
}
