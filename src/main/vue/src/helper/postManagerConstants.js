export const modes = {
    create: {
        id: 0,
        name: "Erstellen",
        description: "Beitrag erstellen",
        icon: "post_add"
    },
    edit: {
        id: 1,
        name: "Bearbeiten",
        description: "Beitrag bearbeiten / löschen",
        icon: "edit"
    },
    approve: {
        id: 2,
        name: "Genehmigen",
        description: "Beitrag genehmigen",
        icon: "check"
    },
    special: {
        id: 3,
        name: "Spezielle Texte",
        description: "Spezielle Texte",
        icon: "build"
    }
};

export const tabs = {
    selectEdit: {
        id: -3,
        name: "Auswahl",
        icon: "radio_button_checked",
        special: true,
        number: null
    },
    selectApprove: {
        id: -2,
        name: "Auswahl",
        icon: "radio_button_checked",
        special: true,
        number: null
    },
    selectSpecial: {
        id: -1,
        name: "Auswahl",
        icon: "radio_button_checked",
        special: true,
        number: null
    },
    textSpecial: {
        id: 0,
        name: "Inhalt",
        icon: "format_align_left",
        number: null
    },
    text: {
        id: 1,
        name: "Titel und Text",
        icon: "format_align_left",
        number: null
    },
    images: {
        id: 2,
        name: "Bilder",
        icon: "camera_alt",
        number: null
    },
    layout: {
        id: 3,
        name: "Layout",
        icon: "border_inner",
        number: null
    },
    assign: {
        id: 4,
        name: "Zuordnung",
        icon: "device_hub",
        number: null
    },
    finalize: {
        id: 5,
        name: "Fertigstellung",
        icon: "check_circle",
        number: null
    }
};

export const postLayouts = [0, 1, 2];
export const previewLayouts = [0, 1, 2];

export const specialData = {
    events: {
        id: 0,
        name: "Termine",
        info: "Hier können Sie Termine eintragen.<br><b>Format: Von - Bis, Beschreibung URL</b><br>Beispiel 1: 25.03.2019, Fest<br>Beispiel 2: 25.03.2019 19:30, Fest https://www.stephaneum.de<br>Beispiel 3: 25.03.2019 - 27.03.2019, Fest<br><b>Nullen z.B. bei '07' müssen mitgeschrieben werden!</b>",
        plain: true
    },
    coop: {
        id: 1,
        name: "Koop.-partner",
        info: "Mehrere Kooperationspartner mit Semikolon (;) trennen<br>Tooltips können mit runden Klammern und Links mit eckigen Klammern gekennzeichnet werden<br>Falls keine Partner angegeben, wird der Bereich versteckt.<br><b>Beispiel: Partner(Briefkontakte)[http://google.de]</b>",
        plain: true
    },
    coopURL: {
        id: 2,
        name: "Koop.-partner (URL)",
        info: "Bitte URL eingeben. Relative Links sind erlaubt.<br>Falls keine URL angegeben, wird der Button versteckt.",
        plain: true
    }
};

export const specialFragments = {
    copyright: {
        id: 3,
        name: "Copyright",
        info: "Der Copyright-Text wird unterhalb von 'Kontakt | Impressum | Sitemap' angezeigt.",
        plain: false
    },
    dev: {
        id: 4,
        name: "Entwicklung",
        info: "Dieser Text wird in 'Informationen' unter 'Über die Entwicklung' angezeigt.<br>Es wird ausgeblendet, wenn der Inhalt leer ist.",
        plain: false
    },
    liveticker: {
        id: 5,
        name: "Live-Ticker",
        info: "Der Live-Ticker wird auf der Homepage über den Beiträgen angezeigt.<br>Es wird ausgeblendet, wenn der Inhalt leer ist. Es wird nur der unformatierte Text gespeichert.",
        plain: true
    },
};

export const specialSites = {
    contact: {
        id: 6,
        name: "Kontakt",
        info: "Der Titel \"Kontakt\" ist vorgegeben.",
        plain: false
    },
    imprint: {
        id: 7,
        name: "Impressum",
        info: "Der Titel \"Impressum\" ist vorgegeben.",
        plain: false
    },
    history: {
        id: 8,
        name: "Geschichte",
        info: "Die Geschichte erreicht man über das Klicken auf das Stephaneum-Logo auf der linken Seite.<br>a) Link, dann bitte mit 'http' anfangen<br>b) (sonst) interne Lösung",
        plain: false
    },
    euSa: {
        id: 9,
        name: "EU und S.-A.",
        info: "Die Seite erreicht man über das Klicken auf das Stephaneum-Logo auf der linken Seite.<br>a) Link, dann bitte mit 'http' anfangen<br>b) (sonst) interne Lösung",
        plain: false
    },
};