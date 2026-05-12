/**
 * main.js - Punto de entrada de la aplicación
 * Inicializa la aplicación de forma inmediata
 */

function initializeApp() {
    try {
        console.log('🚀 Inicializando aplicación...');
        
        // Verificar que Bootstrap esté disponible
        if (!window.bootstrap) {
            throw new Error('Bootstrap no se pudo cargar desde CDN');
        }

        // Verificar que App esté cargado
        if (typeof App === 'undefined') {
            throw new Error('El script principal de la aplicación (app.js) no se cargó correctamente');
        }

        // Inicializar la aplicación inmediatamente
        // No esperamos a AppScripts.allReady() porque el navegador ya garantiza
        // el orden de ejecución de los scripts cargados de forma secuencial en index.html
        App.init();
        
        console.log('✅ Aplicación inicializada con éxito');
    } catch (error) {
        console.error('❌ Error de inicialización:', error);
        const outlet = document.getElementById('router-outlet');
        if (outlet) {
            outlet.innerHTML = `
                <div class="container mt-5">
                    <div class="alert alert-danger" role="alert">
                        <h4 class="alert-heading">Error de inicialización</h4>
                        <p>${error.message}</p>
                        <hr>
                        <p class="mb-0 small">Por favor recarga la página. Si el problema persiste, verifica la consola del navegador.</p>
                        <button class="btn btn-primary mt-3" onclick="location.reload()">Recargar página</button>
                    </div>
                </div>
            `;
        }
    }
}

// Esperar a que el DOM esté listo antes de inicializar
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initializeApp);
} else {
    // El DOM ya esté listo
    initializeApp();
}
