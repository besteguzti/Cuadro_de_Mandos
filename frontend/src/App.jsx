import { useState } from "react";

import MainPage
from "./Pages/MainPage";

import ArubaPage
from "./Pages/ArubaPage";

import CitrixPage
from "./Pages/CitrixPage";

import Microsoft365Page
from "./Pages/Microsoft365Page";

import GlpiPage
from "./Pages/GlpiPage";

function App() {

    // =========================
    // Vista activa
    // =========================

    const [

        activePage,

        setActivePage

    ] =

    useState(

        "main"

    );

    // =========================
    // Selección página
    // =========================

    const renderPage =
    () => {

        switch (
            activePage
        ) {

            case "aruba":

                return (
                    <ArubaPage />
                );

            case "citrix":

                return (
                    <CitrixPage />
                );

            case "m365":

                return (
                    <Microsoft365Page />
                );

            case "glpi":

                return (
                    <GlpiPage />
                );

            default:

                return (
                    <MainPage />
                );
        }
    };

    return (

        <div>

            <nav>

                <button
                    onClick={() =>
                        setActivePage(
                            "main"
                        )
                    }
                >
                    Principal
                </button>

                <button
                    onClick={() =>
                        setActivePage(
                            "aruba"
                        )
                    }
                >
                    Aruba
                </button>

                <button
                    onClick={() =>
                        setActivePage(
                            "citrix"
                        )
                    }
                >
                    Citrix
                </button>

                <button
                    onClick={() =>
                        setActivePage(
                            "m365"
                        )
                    }
                >
                    Microsoft 365
                </button>

                <button
                    onClick={() =>
                        setActivePage(
                            "glpi"
                        )
                    }
                >
                    GLPI
                </button>

            </nav>

            <hr />

            {renderPage()}

        </div>

    );

}

export default App;
