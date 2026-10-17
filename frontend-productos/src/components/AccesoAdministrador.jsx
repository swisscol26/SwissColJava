import { API_BASE, TIENDA_URL } from "../config";
import { useEffect, useState } from "react";

function AccesoAdministrador({ children }) {
    const [estado, setEstado] = useState("cargando");

    useEffect(() => {
        const controlador = new AbortController();

        async function comprobarAcceso() {
            try {
                const respuesta = await fetch(
                    `${API_BASE}/auth/admin`,
                    {
                        method: "POST",
                        credentials: "include",
                        signal: controlador.signal
                    }
                );

                if (controlador.signal.aborted) return;

                if (respuesta.status === 204) {
                    setEstado("permitido");
                } else if (respuesta.status === 401) {
                    setEstado("sinSesion");
                } else if (respuesta.status === 403) {
                    setEstado("sinPermiso");
                } else {
                    setEstado("error");
                }
            } catch (error) {
                if (error.name !== "AbortError") {
                    setEstado("error");
                }
            }
        }

        comprobarAcceso();

        return () => controlador.abort();
    }, []);

    if (estado === "permitido") {
        return children;
    }

    const mensajes = {
        cargando: "Comprobando acceso...",
        sinSesion: "Inicia sesión con la cuenta administradora.",
        sinPermiso: "Tu cuenta no tiene permiso para acceder al panel.",
        error: "No fue posible comprobar el acceso. Intenta nuevamente en unos momentos."
    };

    return (
        <main className="container py-5">
            <h1>Acceso administrativo</h1>

            <p role="status">{mensajes[estado]}</p>

            {estado !== "cargando" && (
                <>
                    <a
                        className="btn btn-primary me-2"
                        href={TIENDA_URL}
                    >
                        Ir a la tienda
                    </a>

                    <button
                        className="btn btn-outline-secondary"
                        onClick={() => window.location.reload()}
                    >
                        Volver a comprobar
                    </button>
                </>
            )}
        </main>
    );
}

export default AccesoAdministrador;